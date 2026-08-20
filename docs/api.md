# PlayerTitles API

## Accessing the API

The public API is exposed through the `PlayerTitlesApi` interface. Obtain an instance from the Bukkit services manager after PlayerTitles has enabled:

```java
RegisteredServiceProvider<PlayerTitlesApi> provider =
        Bukkit.getServicesManager().getRegistration(PlayerTitlesApi.class);

PlayerTitlesApi api = provider.getProvider();
```

All API methods identify players by `UUID`. No Bukkit `Player` object is required for persistent operations.

## Reading player titles

Cache-only reads. These methods never touch the database and are safe to call from any thread.

```java
// Whether title data is loaded in memory.
boolean loaded = api.isLoaded(playerId);

// Unlocked title IDs. Empty set when not loaded.
Set<String> unlocked = api.getUnlockedTitles(playerId);

// Currently selected title ID.
Optional<String> selected = api.getSelectedTitle(playerId);

// Raw MiniMessage prefix of the selected title.
Optional<String> prefix = api.getSelectedTitlePrefix(playerId);

// Whether a player owns a specific title.
boolean owns = api.hasTitle(playerId, "explorer");
```

`getUnlockedTitles` returns a defensive copy. Mutating the returned set does not affect the cache.

## Unlocking and revoking titles

Both `unlockTitle` and `revokeTitle` accept any `UUID` and work for both loaded and offline players.

```java
api.unlockTitle(playerId, "explorer")
        .thenAccept(result -> {
            switch (result) {
                case UNLOCKED         -> { /* title was granted */ }
                case ALREADY_UNLOCKED -> { /* player already owned it */ }
                case TITLE_NOT_FOUND  -> { /* title ID is not configured */ }
            }
        });

api.revokeTitle(playerId, "explorer")
        .thenAccept(result -> {
            switch (result) {
                case REVOKED         -> { /* title was removed */ }
                case NOT_UNLOCKED    -> { /* player did not own it */ }
                case TITLE_NOT_FOUND -> { /* title ID is not configured */ }
            }
        });
```

For loaded players, the cache is updated first and persistence happens asynchronously. For offline players, the database is mutated directly and no cache entry is created.

When a revoked title was the player's active selection, the selection is cleared in the same database transaction.

## Selecting titles

Selection is a cache-only operation. The player must be loaded.

```java
TitleSelectionResult selectResult = api.selectTitle(playerId, "explorer");
// SELECTED, ALREADY_SELECTED, TITLE_NOT_FOUND, TITLE_NOT_UNLOCKED, PLAYER_NOT_LOADED

TitleSelectionResult clearResult = api.clearSelectedTitle(playerId);
// CLEARED, NOTHING_SELECTED, PLAYER_NOT_LOADED
```

Selected titles are persisted on quit and during server shutdown. There is no immediate database write on selection.

## Async behavior

`unlockTitle` and `revokeTitle` return `CompletableFuture`. Database work runs on a dedicated database thread and never on Paper or Folia server threads.

Callers must not block a server thread waiting on the returned future:

```java
// WRONG — blocks the server thread
TitleUnlockResult result = api.unlockTitle(playerId, "explorer").join();

// CORRECT
api.unlockTitle(playerId, "explorer")
        .thenAccept(result -> { /* ... */ });
```

Database failures complete the future exceptionally. Callers should handle them:

```java
api.unlockTitle(playerId, "explorer")
        .exceptionally(exception -> {
            logger.log(Level.SEVERE, "Unlock failed.", exception);
            return null;
        });
```

## Result types

| Enum | Values |
| --- | --- |
| `TitleUnlockResult` | `UNLOCKED`, `ALREADY_UNLOCKED`, `TITLE_NOT_FOUND`, `PLAYER_NOT_LOADED` |
| `TitleRevokeResult` | `REVOKED`, `NOT_UNLOCKED`, `TITLE_NOT_FOUND`, `PLAYER_NOT_LOADED` |
| `TitleSelectionResult` | `SELECTED`, `ALREADY_SELECTED`, `TITLE_NOT_FOUND`, `TITLE_NOT_UNLOCKED`, `CLEARED`, `NOTHING_SELECTED`, `PLAYER_NOT_LOADED` |

`PLAYER_NOT_LOADED` is only returned by cache-first loaded-player operations. The async `unlockTitle` and `revokeTitle` methods fall through to direct database mutation for offline targets instead of returning this value.

## Important guarantees

- `unlockTitle` and `revokeTitle` support offline UUIDs without a Bukkit `Player` instance.
- Read methods (`isLoaded`, `getUnlockedTitles`, `getSelectedTitle`, `getSelectedTitlePrefix`, `hasTitle`) are cache-only with no database access.
- `selectTitle` and `clearSelectedTitle` require loaded runtime state.
- All API methods are thread-safe across Paper and Folia server threads.
- The returned `Set` from `getUnlockedTitles` is never a reference to internal cache state.
