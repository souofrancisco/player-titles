package dev.souofrancisco.playertitles.service;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Public service facade for the PlayerTitles API.
 *
 * <p>This class keeps API callers decoupled from the internal orchestration layer while delegating
 * all cache-backed business behavior to {@link PlayerTitlesController}.
 */
@RequiredArgsConstructor
public final class PlayerTitlesService implements PlayerTitlesApi {

    private final @NotNull PlayerTitlesController controller;

    @Override
    public boolean isLoaded(@NotNull UUID playerId) {
        return controller.isLoaded(playerId);
    }

    @Override
    public @NotNull Set<@NotNull String> getUnlockedTitles(@NotNull UUID playerId) {
        return controller.getUnlockedTitles(playerId);
    }

    @Override
    public @NotNull Optional<String> getSelectedTitle(@NotNull UUID playerId) {
        return controller.getSelectedTitle(playerId);
    }

    @Override
    public boolean hasTitle(@NotNull UUID playerId, @NotNull String titleId) {
        return controller.hasTitle(playerId, titleId);
    }

    @Override
    public @NotNull TitleUnlockResult unlockTitle(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        return controller.unlockTitle(playerId, titleId);
    }

    @Override
    public @NotNull TitleSelectionResult selectTitle(
            @NotNull UUID playerId,
            @NotNull String titleId
    ) {
        return controller.selectTitle(playerId, titleId);
    }

    @Override
    public @NotNull TitleSelectionResult clearSelectedTitle(@NotNull UUID playerId) {
        return controller.clearSelectedTitle(playerId);
    }
}
