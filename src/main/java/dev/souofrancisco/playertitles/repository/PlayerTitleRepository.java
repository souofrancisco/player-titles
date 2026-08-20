package dev.souofrancisco.playertitles.repository;

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
 *
 * <p>This class exposes {@link CompletableFuture} operations to runtime code and delegates every
 * blocking operation through {@link DatabaseExecutor} to {@link PlayerTitleJdbcStore}. It must not
 * contain JDBC statements, transactions, result-set mapping, or Bukkit/Folia scheduler logic.
 */
@RequiredArgsConstructor
public final class PlayerTitleRepository {

    private final @NotNull DatabaseExecutor databaseExecutor;
    private final @NotNull PlayerTitleJdbcStore jdbcStore;

    public @NotNull CompletableFuture<Void> initializeSchema() {
        return databaseExecutor.runAsync(jdbcStore::initializeSchema);
    }

    public @NotNull CompletableFuture<PlayerTitleState> load(@NotNull UUID playerId) {
        return databaseExecutor.supplyAsync(() -> jdbcStore.load(playerId));
    }

    public @NotNull CompletableFuture<Void> persistUnlock(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        return databaseExecutor.runAsync(() -> jdbcStore.persistUnlock(playerId, titleId));
    }

    public @NotNull CompletableFuture<Void> persistSelectedTitle(
            @NotNull UUID playerId,
            @Nullable String titleId
    ) {
        return databaseExecutor.runAsync(() -> jdbcStore.persistSelectedTitle(playerId, titleId));
    }

    public @NotNull CompletableFuture<Void> persistSelectedTitles(
            @NotNull Collection<@NotNull PlayerTitleState> states
    ) {
        if (states.isEmpty()) return CompletableFuture.completedFuture(null);

        List<PlayerTitleState> batch = List.copyOf(states);
        return databaseExecutor.runAsync(() -> jdbcStore.persistSelectedTitles(batch));
    }
}
