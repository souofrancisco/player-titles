package dev.souofrancisco.playertitles.placeholder.resolver;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves the selected title prefix from the cache-backed PlayerTitles API.
 */
@RequiredArgsConstructor
public final class TitlePlaceholderResolver implements PlaceholderResolver {

    private static final @NotNull String KEY = "title";

    private final @NotNull PlayerTitlesApi api;

    @Override
    public boolean supports(@NotNull String key) {
        return KEY.equals(key);
    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player) {
        if (player == null) return "";

        UUID playerId = player.getUniqueId();
        if (!api.isLoaded(playerId)) return "";

        return api.getSelectedTitlePrefix(playerId).orElse("");
    }
}
