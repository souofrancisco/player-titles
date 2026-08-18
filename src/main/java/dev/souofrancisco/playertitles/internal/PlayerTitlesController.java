package dev.souofrancisco.playertitles.internal;

import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Internal orchestration layer for player title cache operations.
 *
 * <p>This class owns business rules such as loaded-player checks, configured title validation, unlock
 * idempotency, and selection constraints. It delegates all cached state mutation to
 * {@link PlayerTitleCache}.
 */
@RequiredArgsConstructor
public final class PlayerTitlesController {

    private final @NotNull PlayerTitleCache cache;
    private final @NotNull PluginConfig pluginConfig;

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

        return result.get();
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

    private boolean titleExists(@NotNull String titleId) {
        return pluginConfig.titles().containsKey(titleId);
    }
}
