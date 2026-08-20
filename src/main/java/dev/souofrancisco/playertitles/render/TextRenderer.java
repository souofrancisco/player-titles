package dev.souofrancisco.playertitles.render;

import dev.souofrancisco.playertitles.config.section.TitleConfig;
import java.util.List;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Turns configured templates into Adventure components: PlaceholderAPI first, then MiniMessage.
 */
@RequiredArgsConstructor
public final class TextRenderer {

    private final @NotNull JavaPlugin plugin;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

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

    public @NotNull Component render(
            @NotNull Player player,
            @NotNull String raw,
            @NotNull RenderContext context
    ) {
        return render(player, raw, tagResolver(player, context));
    }

    public @NotNull List<@NotNull Component> renderLore(
            @NotNull Player player,
            @NotNull List<@NotNull String> rawLore,
            @NotNull RenderContext context
    ) {
        return rawLore.stream()
                .map(line -> render(player, line, context))
                .toList();
    }

    private @NotNull String applyExternalPlaceholders(
            @NotNull Player player,
            @NotNull String raw
    ) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) return raw;
        return PlaceholderAPI.setPlaceholders(player, raw);
    }

    private @NotNull TagResolver tagResolver(
            @NotNull Player player,
            @NotNull RenderContext context
    ) {
        return TagResolver.builder()
                .resolver(TagResolver.resolver("selected_title", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titleDisplayName(player, context.selectedTitle()))))
                .resolver(TagResolver.resolver("title_name", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titleDisplayName(player, context.title()))))
                .resolver(TagResolver.resolver("title_prefix", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titlePrefix(player, context.title()))))
                .resolver(TagResolver.resolver("title_status", (arguments, tagContext) ->
                        Tag.selfClosingInserting(Component.text(context.statusName()))))
                .build();
    }

    private @NotNull Component titleDisplayName(
            @NotNull Player player,
            @Nullable TitleConfig title
    ) {
        if (title == null) return Component.empty();
        return render(player, title.displayName());
    }

    private @NotNull Component titlePrefix(
            @NotNull Player player,
            @Nullable TitleConfig title
    ) {
        if (title == null) return Component.empty();
        return render(player, title.prefix());
    }
}
