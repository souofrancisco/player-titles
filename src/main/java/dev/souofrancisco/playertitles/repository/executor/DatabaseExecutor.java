package dev.souofrancisco.playertitles.repository.executor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Executes blocking database operations away from Paper/Folia server threads.
 *
 * <p>This class is only an executor boundary for JDBC work. It must not contain repository logic,
 * SQL, Bukkit/Folia scheduler calls, or runtime player lifecycle decisions.
 */
@RequiredArgsConstructor
public final class DatabaseExecutor implements AutoCloseable {

    private static final long SHUTDOWN_WAIT_SECONDS = 30L;

    private final @NotNull ExecutorService executorService;

    public static @NotNull DatabaseExecutor create() {
        return new DatabaseExecutor(Executors.newSingleThreadExecutor(new DatabaseThreadFactory()));
    }

    public <T> @NotNull CompletableFuture<T> supplyAsync(@NotNull ThrowingSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (Exception exception) {
                throw new IllegalStateException("Database operation failed.", exception);
            }
        }, executorService);
    }

    public @NotNull CompletableFuture<Void> runAsync(@NotNull ThrowingRunnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception exception) {
                throw new IllegalStateException("Database operation failed.", exception);
            }
        }, executorService);
    }

    @Override
    public void close() {
        executorService.shutdown();
        try {
            while (!executorService.awaitTermination(SHUTDOWN_WAIT_SECONDS, TimeUnit.SECONDS)) {
                // Keep waiting so queued persistence can finish before Hikari closes.
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while shutting down database executor.", exception);
        }
    }

    private static final class DatabaseThreadFactory implements ThreadFactory {

        @Override
        public @NotNull Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "PlayerTitles Database");
            thread.setDaemon(false);
            return thread;
        }
    }
}
