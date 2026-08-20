# Database Threading

## Execution model

All blocking JDBC work runs on a dedicated non-daemon thread named `PlayerTitles Database`. No database operation ever executes on a Paper tick thread, a Folia region thread, or a Folia entity thread.

```text
Paper / Folia thread
        ↓
PlayerTitleRepository
        ↓
DatabaseExecutor
        ↓
dedicated database thread
        ↓
PlayerTitleJdbcStore
        ↓
HikariCP / JDBC
```

## Repository vs JDBC store

```text
PlayerTitleRepository
→ asynchronous persistence facade
→ returns CompletableFuture
→ delegates blocking calls through DatabaseExecutor

DatabaseExecutor
→ database thread boundary
→ single-threaded ExecutorService
→ wraps checked exceptions in IllegalStateException

PlayerTitleJdbcStore
→ blocking JDBC implementation
→ owns Connection, PreparedStatement, ResultSet, and transaction lifecycle
→ engine-agnostic (works with both SQLite and MySQL queries)
```

`PlayerTitleRepository` is the only class that interacts with `DatabaseExecutor`. No runtime layer calls `PlayerTitleJdbcStore` directly.

## Why a single database executor

- Database operations remain ordered. A write queued before a read always completes first.
- SQLite does not benefit from concurrent writers; serialized execution avoids `SQLITE_BUSY` contention.
- MySQL configurations also use a single executor. The pool size controls connection availability, not executor parallelism.
- No JDBC operation can run on a Paper or Folia server thread, regardless of how the caller schedules the API call.

## Shutdown

The `PersistenceModule` coordinates an ordered shutdown:

```text
1. flush selected titles
   → batch-persist all cached PlayerTitleState snapshots
   → blocks until the batch completes

2. terminate database executor
   → executor.shutdown()
   → await up to 30 seconds for queued work to drain
   → shutdownNow() if the timeout expires
   → await 5 more seconds for forced termination

3. close HikariCP
   → dataSource.close()
```

If any step fails, the next steps still execute. Errors are combined with `addSuppressed` and thrown after the full sequence completes.

The database thread is non-daemon (`thread.setDaemon(false)`), so the JVM will not exit while queued persistence work is still running even if the shutdown timeout has not expired.

## Folia guarantees

- `DatabaseExecutor` uses a plain `Executors.newSingleThreadExecutor`, not a Folia scheduler. The database thread is not a region thread.
- After an async database operation completes, cache writes are dispatched to the target player's `EntityScheduler` so they execute on the correct Folia entity thread.
- `PlayerTitleRepository.load()` returns a `CompletableFuture` that completes on the database thread. The caller (`PlayerTitlesController.loadPlayer`) then uses `player.getScheduler().execute()` to publish the loaded state into the cache on the player's entity thread.
