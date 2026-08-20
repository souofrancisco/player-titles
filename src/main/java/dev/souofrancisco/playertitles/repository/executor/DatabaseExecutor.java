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
 */
@RequiredArgsConstructor
public final class DatabaseExecutor implements AutoCloseable {

    private final @NotNull ExecutorService executorService;

    public static @NotNull DatabaseExecutor create() {
        return new DatabaseExecutor(Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "PlayerTitles Database");
            thread.setDaemon(false);
            return thread;
        }));
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
            if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                executorService.shutdownNow();

                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Database executor did not terminate.");
                }
            }
        } catch (InterruptedException exception) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Interrupted while shutting down database executor.",
                    exception
            );
        }
    }
}
