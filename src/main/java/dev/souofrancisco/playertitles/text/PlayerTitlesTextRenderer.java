package dev.souofrancisco.playertitles.text;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Renders configured text through PlaceholderAPI, PlayerTitles tag resolvers, and MiniMessage.
 */
public final class PlayerTitlesTextRenderer {

    private final @NotNull JavaPlugin plugin;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    public PlayerTitlesTextRenderer(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public @NotNull Component render(@NotNull Player player, @NotNull String raw) {
        return render(player, raw, TagResolver.empty());
    }

    public @NotNull Component render(
            @NotNull Player player,
            @NotNull String raw,
            @NotNull TagResolver tagResolver
    ) {
        return miniMessage.deserialize(applyExternalPlaceholders(player, raw), tagResolver);
    }

    private @NotNull String applyExternalPlaceholders(
            @NotNull Player player,
            @NotNull String raw
    ) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) return raw;
        return PlaceholderAPI.setPlaceholders(player, raw);
    }
}
