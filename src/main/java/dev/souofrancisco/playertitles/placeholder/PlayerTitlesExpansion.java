package dev.souofrancisco.playertitles.placeholder;

import dev.souofrancisco.playertitles.PlayerTitlesPlugin;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI expansion metadata and request delegation for PlayerTitles.
 *
 * <p>Available placeholders:
 * <ul>
 *     <li>{@code %playertitles_title%} - Returns the selected title prefix for PlaceholderAPI.</li>
 * </ul>
 */
@RequiredArgsConstructor
public final class PlayerTitlesExpansion extends PlaceholderExpansion {

    private static final @NotNull String IDENTIFIER = "playertitles";

    private final @NotNull PlayerTitlesPlugin plugin;
    private final @NotNull PlayerTitlesPlaceholderParser parser;

    @Override
    public @NotNull String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @NotNull String onRequest(
            @Nullable OfflinePlayer player,
            @NotNull String params
    ) {
        return parser.parse(player, params);
    }
}
