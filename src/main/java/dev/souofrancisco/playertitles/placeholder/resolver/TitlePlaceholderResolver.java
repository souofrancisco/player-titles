package dev.souofrancisco.playertitles.placeholder.resolver;

import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves selected title placeholders through the internal PlayerTitles controller.
 */
@RequiredArgsConstructor
public final class TitlePlaceholderResolver implements PlaceholderResolver {

    private static final @NotNull String KEY = "title";

    private final @NotNull PlayerTitlesController controller;

    @Override
    public boolean supports(@NotNull String key) {
        return KEY.equals(key);
    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player) {
        if (player == null) return "";

        UUID playerId = player.getUniqueId();
        if (!controller.isLoaded(playerId)) return "";

        return controller.getSelectedTitlePrefix(playerId).orElse("");
    }
}
