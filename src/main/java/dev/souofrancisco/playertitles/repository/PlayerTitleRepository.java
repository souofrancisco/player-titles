package dev.souofrancisco.playertitles.repository;

import dev.souofrancisco.playertitles.PlayerTitlesDebug;
import dev.souofrancisco.playertitles.internal.PlayerTitleState;
import dev.souofrancisco.playertitles.repository.executor.DatabaseExecutor;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Asynchronous persistence facade for player title state.
 */
@RequiredArgsConstructor
public final class PlayerTitleRepository {

    private final @NotNull DatabaseExecutor databaseExecutor;
    private final @NotNull PlayerTitleJdbcStore jdbcStore;
    private final @NotNull PlayerTitlesDebug debug;

    public @NotNull CompletableFuture<Void> initializeSchema() {
        return databaseExecutor.runAsync(jdbcStore::initializeSchema);
    }

    public @NotNull CompletableFuture<PlayerTitleState> load(@NotNull UUID playerId) {
        return databaseExecutor.supplyAsync(() -> {
            PlayerTitleState state = jdbcStore.load(playerId);
            debug.log("PERSIST", () -> "Loaded player " + playerId
                    + " unlocks=" + state.unlockedTitles().size()
                    + " selected=" + state.selectedTitleId());
            return state;
        });
    }

    public @NotNull CompletableFuture<Void> persistUnlock(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        return databaseExecutor.runAsync(() -> {
            jdbcStore.persistUnlock(playerId, titleId);
            debug.log("PERSIST", () -> "Persisted unlock player=" + playerId + " title=" + titleId);
        });
    }

    public @NotNull CompletableFuture<Void> persistRevoke(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        return databaseExecutor.runAsync(() -> {
            jdbcStore.persistRevoke(playerId, titleId);
            debug.log("PERSIST", () -> "Persisted revoke player=" + playerId + " title=" + titleId);
        });
    }

    public @NotNull CompletableFuture<Void> persistSelectedTitle(
            @NotNull UUID playerId,
            @Nullable String titleId
    ) {
        return databaseExecutor.runAsync(() -> {
            jdbcStore.persistSelectedTitle(playerId, titleId);
            debug.log("PERSIST", () -> "Persisted selected title player=" + playerId + " selected=" + titleId);
        });
    }

    public @NotNull CompletableFuture<Void> persistSelectedTitles(
            @NotNull Collection<@NotNull PlayerTitleState> states
    ) {
        if (states.isEmpty()) return CompletableFuture.completedFuture(null);

        List<PlayerTitleState> batch = List.copyOf(states);
        return databaseExecutor.runAsync(() -> jdbcStore.persistSelectedTitles(batch));
    }
}
