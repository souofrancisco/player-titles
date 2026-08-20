# Runtime Cache

## What the cache owns

`PlayerTitleCache` holds one `PlayerTitleState` per loaded player, keyed by `UUID`. It is backed by a `ConcurrentHashMap` and is the single concurrency boundary for runtime title state.

`PlayerTitleCache` only contains loaded players. An offline player never has a cache entry, even during an offline API mutation.

Cache entries are immutable `PlayerTitleState` snapshots. Mutations replace the snapshot atomically rather than modifying shared collections in place.

```java
public record PlayerTitleState(
        UUID playerId,
        Set<String> unlockedTitles,   // defensively copied, unmodifiable
        @Nullable String selectedTitleId
) { }
```

State-transition methods (`unlock`, `revoke`, `select`) return a new `PlayerTitleState` instance. The `unlockedTitles` set is `Set.copyOf` on construction.

## Join lifecycle

```text
PlayerJoinEvent
      ↓
PlayerConnectionListener.onJoin
      ↓
PlayerTitlesController.loadPlayer(Player)
      ↓
PlayerTitleRepository.load(UUID)
      ↓
DatabaseExecutor → database thread
      ↓
PlayerTitleJdbcStore.load(UUID) → JDBC
      ↓
CompletableFuture completes on database thread
      ↓
player.getScheduler().execute()
      ↓
PlayerTitleCache.load(state)   (on player's entity thread)
```

The `EntityScheduler` dispatch ensures the cache write runs on the correct Folia entity thread. If the player disconnects before the scheduler tick fires, the retired callback is a no-op.

## Runtime mutations

### Loaded player (cache-first path)

For loaded players, the cache is updated atomically first, then persistence is scheduled asynchronously:

```text
API call (unlock / revoke)
      ↓
PlayerTitlesController
      ↓
PlayerTitleCache.updateIfLoaded / revokeIfLoaded
      ↓
atomically produce new PlayerTitleState
      ↓
schedule PlayerTitleRepository.persistUnlock / persistRevoke
      ↓
DatabaseExecutor → database thread → JDBC
```

The caller receives the result immediately from the cache. Database persistence is fire-and-forget with exception logging.

### Selection and clear

`selectTitle` and `clearSelectedTitle` are cache-only. No database write happens at call time. Selected titles are persisted:

- On `PlayerQuitEvent` for the individual player.
- In a batch during server shutdown for all remaining loaded players.

### Offline player (database-first path)

```text
API call (unlock / revoke)
      ↓
player not loaded → skip cache
      ↓
PlayerTitleRepository.persistUnlock / persistRevoke
      ↓
DatabaseExecutor → database thread → JDBC
      ↓
reconcile: if player became loaded meanwhile,
replay the mutation onto the cache via EntityScheduler
```

Offline API mutations never create temporary cache entries.

## Quit lifecycle

```text
PlayerQuitEvent
      ↓
PlayerConnectionListener.onQuit
      ↓
PlayerTitlesController.unloadPlayer(UUID)
      ↓
PlayerTitleCache.unload(UUID)    → removes and returns the state
      ↓
PlayerTitleRepository.persistSelectedTitle(UUID, selectedTitleId)
      ↓
DatabaseExecutor → database thread → JDBC
```

Only the selected title is persisted on quit. Unlock state was already written to the database when the unlock or revoke occurred.

## Thread safety

- `PlayerTitleCache` uses `ConcurrentHashMap` with atomic operations (`computeIfPresent`, `put`, `remove`).
- `PlayerTitleState` is an immutable record. State transitions return a new instance.
- `updateIfLoaded` uses `computeIfPresent`, which holds the segment lock while the updater runs. The updater must not perform blocking work.
- Read methods (`isLoaded`, `get`) can be called from any thread without synchronization.

### Listener → Controller → Cache relationship

```text
PlayerConnectionListener
→ thin Bukkit listener
→ only forwards join/quit events to PlayerTitlesController

PlayerTitlesController
→ internal orchestration layer
→ coordinates between cache, repository, and Folia schedulers
→ not exposed to API consumers

PlayerTitleCache
→ thread-safe runtime state container
→ no persistence, no scheduling, no Bukkit dependencies
```

The public API (`PlayerTitlesApi`) delegates through `PlayerTitlesService` to `PlayerTitlesController`. API consumers never interact with the cache or controller directly.
