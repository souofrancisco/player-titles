package dev.souofrancisco.playertitles.internal;

import dev.souofrancisco.playertitles.PlayerTitlesPlugin;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.repository.PlayerTitleRepository;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import dev.souofrancisco.playertitles.result.TitleRevokeResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Internal runtime and business entry point for PlayerTitles systems.
 */
@RequiredArgsConstructor
public final class PlayerTitlesController {

    private final @NotNull PlayerTitlesPlugin plugin;
    private final @NotNull PlayerTitleCache cache;
    private final @NotNull PlayerTitleRepository repository;
    private final @NotNull Logger logger;

    public void loadPlayer(@NotNull Player player) {
        UUID playerId = player.getUniqueId();

        repository.load(playerId)
                .whenComplete((state, exception) -> {
                    if (exception != null) {
                        logger.log(Level.SEVERE, "Could not load player title state.", exception);
                        return;
                    }

                    player.getScheduler().execute(
                            plugin,
                            () -> cache.load(state),
                            () -> {},
                            1L
                    );
                });
    }

    public @NotNull Optional<PlayerTitleState> unloadPlayer(@NotNull UUID playerId) {
        Optional<PlayerTitleState> unloadedState = cache.unload(playerId);
        unloadedState.ifPresent(this::persistSelectedTitle);
        return unloadedState;
    }

    public boolean isLoaded(@NotNull UUID playerId) {
        return cache.isLoaded(playerId);
    }

    public @NotNull Set<@NotNull String> getUnlockedTitles(@NotNull UUID playerId) {
        return cache.get(playerId)
                .map(PlayerTitleState::unlockedTitles)
                .orElseGet(Set::of);
    }

    public @NotNull Optional<String> getSelectedTitle(@NotNull UUID playerId) {
        return cache.get(playerId)
                .map(PlayerTitleState::selectedTitleId);
    }

    public @NotNull Optional<String> getSelectedTitlePrefix(@NotNull UUID playerId) {
        return getSelectedTitle(playerId)
                .map(ConfigLoader.current().titles()::get)
                .map(TitleConfig::prefix);
    }

    public boolean hasTitle(@NotNull UUID playerId, @NotNull String titleId) {
        return cache.get(playerId)
                .map(state -> state.hasTitle(titleId))
                .orElse(false);
    }

    /**
     * Unlocks a title for a loaded or offline player.
     *
     * <p>Loaded players use the cache-first path. Offline players mutate persistence directly and
     * never enter the runtime cache unless they become loaded meanwhile (mutation-only reconcile).
     */
    public @NotNull CompletableFuture<TitleUnlockResult> unlockTitleAsync(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        if (!titleExists(titleId)) {
            return CompletableFuture.completedFuture(TitleUnlockResult.TITLE_NOT_FOUND);
        }

        if (isLoaded(playerId)) {
            TitleUnlockResult loadedResult = unlockTitleLoaded(playerId, titleId);
            if (loadedResult != TitleUnlockResult.PLAYER_NOT_LOADED) {
                return CompletableFuture.completedFuture(loadedResult);
            }
        }

        return repository.persistUnlock(playerId, titleId)
                .thenApply(inserted -> {
                    TitleUnlockResult result = inserted
                            ? TitleUnlockResult.UNLOCKED
                            : TitleUnlockResult.ALREADY_UNLOCKED;

                    if (inserted) reconcile(playerId, () -> applyUnlockToCache(playerId, titleId));

                    return result;
                });
    }

    /**
     * Revokes a title from a loaded or offline player.
     *
     * <p>Loaded players use the cache-first path. Offline players mutate persistence directly and
     * never enter the runtime cache unless they become loaded meanwhile (mutation-only reconcile).
     */
    public @NotNull CompletableFuture<TitleRevokeResult> revokeTitleAsync(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        if (!titleExists(titleId)) {
            return CompletableFuture.completedFuture(TitleRevokeResult.TITLE_NOT_FOUND);
        }

        if (isLoaded(playerId)) {
            TitleRevokeResult loadedResult = revokeTitleLoaded(playerId, titleId);
            if (loadedResult != TitleRevokeResult.PLAYER_NOT_LOADED) {
                return CompletableFuture.completedFuture(loadedResult);
            }
        }

        return repository.persistRevoke(playerId, titleId)
                .thenApply(deleted -> {
                    TitleRevokeResult result = deleted
                            ? TitleRevokeResult.REVOKED
                            : TitleRevokeResult.NOT_UNLOCKED;

                    if (deleted) reconcile(playerId, () -> applyRevokeToCache(playerId, titleId));

                    return result;
                });
    }

    /**
     * Cache-first unlock for a loaded player. Returns {@link TitleUnlockResult#PLAYER_NOT_LOADED}
     * when the player is not in the runtime cache.
     */
    public @NotNull TitleUnlockResult unlockTitleLoaded(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        AtomicReference<TitleUnlockResult> result =
                new AtomicReference<>(TitleUnlockResult.PLAYER_NOT_LOADED);

        cache.updateIfLoaded(playerId, state -> {
            if (!titleExists(titleId)) {
                result.set(TitleUnlockResult.TITLE_NOT_FOUND);
                return state;
            }

            if (state.hasTitle(titleId)) {
                result.set(TitleUnlockResult.ALREADY_UNLOCKED);
                return state;
            }

            result.set(TitleUnlockResult.UNLOCKED);
            return state.unlock(titleId);
        });

        TitleUnlockResult unlockResult = result.get();
        if (unlockResult == TitleUnlockResult.UNLOCKED) {
            repository.persistUnlock(playerId, titleId)
                    .exceptionally(exception -> {
                        logger.log(Level.SEVERE, "Could not persist unlocked player title.", exception);
                        return null;
                    });
        }

        return unlockResult;
    }

    /**
     * Cache-first revoke for a loaded player. Returns {@link TitleRevokeResult#PLAYER_NOT_LOADED}
     * when the player is not in the runtime cache.
     */
    public @NotNull TitleRevokeResult revokeTitleLoaded(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        AtomicReference<TitleRevokeResult> result =
                new AtomicReference<>(TitleRevokeResult.PLAYER_NOT_LOADED);

        cache.revokeIfLoaded(playerId, titleId, state -> {
            if (!titleExists(titleId)) {
                result.set(TitleRevokeResult.TITLE_NOT_FOUND);
                return false;
            }

            if (!state.hasTitle(titleId)) {
                result.set(TitleRevokeResult.NOT_UNLOCKED);
                return false;
            }

            result.set(TitleRevokeResult.REVOKED);
            return true;
        });

        TitleRevokeResult revokeResult = result.get();
        if (revokeResult == TitleRevokeResult.REVOKED) {
            repository.persistRevoke(playerId, titleId)
                    .exceptionally(exception -> {
                        logger.log(Level.SEVERE, "Could not persist revoked player title.", exception);
                        return null;
                    });
        }

        return revokeResult;
    }

    public @NotNull TitleSelectionResult selectTitle(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        AtomicReference<TitleSelectionResult> result =
                new AtomicReference<>(TitleSelectionResult.PLAYER_NOT_LOADED);

        cache.updateIfLoaded(playerId, state -> {
            if (!titleExists(titleId)) {
                result.set(TitleSelectionResult.TITLE_NOT_FOUND);
                return state;
            }

            if (!state.hasTitle(titleId)) {
                result.set(TitleSelectionResult.TITLE_NOT_UNLOCKED);
                return state;
            }

            if (state.isSelected(titleId)) {
                result.set(TitleSelectionResult.ALREADY_SELECTED);
                return state;
            }

            result.set(TitleSelectionResult.SELECTED);
            return state.select(titleId);
        });

        return result.get();
    }

    public @NotNull TitleSelectionResult clearSelectedTitle(@NotNull UUID playerId) {
        AtomicReference<TitleSelectionResult> result =
                new AtomicReference<>(TitleSelectionResult.PLAYER_NOT_LOADED);

        cache.updateIfLoaded(playerId, state -> {
            if (state.selectedTitleId() == null) {
                result.set(TitleSelectionResult.NOTHING_SELECTED);
                return state;
            }

            result.set(TitleSelectionResult.CLEARED);
            return state.select(null);
        });

        return result.get();
    }

    /**
     * Replays a persisted mutation onto the runtime cache when the target became loaded while the
     * database write was running.
     *
     * <p>Only the mutated title is touched: full state is never reloaded, so a selection that is
     * still cache-only cannot be overwritten. {@code updateIfLoaded} never inserts, so an offline
     * player still cannot enter the cache. The player lookup runs on the global region thread and the
     * cache write on the target's {@link org.bukkit.entity.Player#getScheduler() EntityScheduler},
     * the same publication path {@link #loadPlayer(Player)} uses, so a join snapshot read before this
     * write is always published first and then corrected here.
     */
    private void reconcile(@NotNull UUID playerId, @NotNull Runnable cacheMutation) {
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null) return;

            player.getScheduler().execute(plugin, cacheMutation, () -> {}, 1L);
        });
    }

    private void applyUnlockToCache(@NotNull UUID playerId, @NotNull String titleId) {
        cache.updateIfLoaded(playerId, state ->
                state.hasTitle(titleId) ? state : state.unlock(titleId)
        );
    }

    private void applyRevokeToCache(@NotNull UUID playerId, @NotNull String titleId) {
        cache.revokeIfLoaded(playerId, titleId, state -> state.hasTitle(titleId));
    }

    private void persistSelectedTitle(@NotNull PlayerTitleState state) {
        repository.persistSelectedTitle(state.playerId(), state.selectedTitleId())
                .exceptionally(exception -> {
                    logger.log(Level.SEVERE, "Could not persist selected player title.", exception);
                    return null;
                });
    }

    private boolean titleExists(@NotNull String titleId) {
        return ConfigLoader.current().titles().containsKey(titleId);
    }
}
