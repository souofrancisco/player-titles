package dev.souofrancisco.playertitles.internal;

import dev.souofrancisco.playertitles.PlayerTitlesPlugin;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.repository.PlayerTitleRepository;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Bridges asynchronous persistence results into Folia runtime player state.
 *
 * <p>Join loading flows from {@link PlayerTitleRepository} to the player's entity scheduler and only
 * then into {@link PlayerTitleCache}, so a completed database load is discarded if the player's
 * scheduler has retired. Quit unloading removes immutable cache state and delegates selected-title
 * persistence back to the repository. This class must not perform JDBC or invoke
 * {@code DatabaseExecutor} directly.
 */
@RequiredArgsConstructor
public final class PlayerTitlesController {

    private final @NotNull PlayerTitlesPlugin plugin;
    private final @NotNull PlayerTitleCache cache;
    private final @NotNull PluginConfig pluginConfig;
    private final @NotNull PlayerTitleRepository repository;
    private final @NotNull Logger logger;

    public void loadPlayer(@NotNull Player player) {
        repository.load(player.getUniqueId())
                .whenComplete((state, exception) -> {
                    if (exception != null) {
                        logger.log(Level.SEVERE, "Could not load player title state.", exception);
                        return;
                    }

                    player.getScheduler().execute(
                            plugin,
                            () -> cache.load(state),
                            () -> {
                            },
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
                .map(pluginConfig.titles()::get)
                .map(TitleConfig::prefix);
    }

    public boolean hasTitle(@NotNull UUID playerId, @NotNull String titleId) {
        return cache.get(playerId)
                .map(state -> state.hasTitle(titleId))
                .orElse(false);
    }

    public @NotNull TitleUnlockResult unlockTitle(
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

    private void persistSelectedTitle(@NotNull PlayerTitleState state) {
        repository.persistSelectedTitle(state.playerId(), state.selectedTitleId())
                .exceptionally(exception -> {
                    logger.log(Level.SEVERE, "Could not persist selected player title.", exception);
                    return null;
                });
    }

    private boolean titleExists(@NotNull String titleId) {
        return pluginConfig.titles().containsKey(titleId);
    }
}
