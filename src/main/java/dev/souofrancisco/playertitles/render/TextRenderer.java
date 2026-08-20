package dev.souofrancisco.playertitles.render;

import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.text.PlayerTitlesTextRenderer;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Renders configured GUI text with GUI-specific PlayerTitles internal MiniMessage tags.
 */
public final class TextRenderer {

    private final @NotNull PlayerTitlesTextRenderer textRenderer;

    public TextRenderer(@NotNull JavaPlugin plugin) {
        this.textRenderer = new PlayerTitlesTextRenderer(plugin);
    }

    public @NotNull Component render(
            @NotNull Player player,
            @NotNull String raw,
            @NotNull RenderContext context
    ) {
        return textRenderer.render(player, raw, tagResolver(player, context));
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
        return textRenderer.render(player, title.displayName());
    }

    private @NotNull Component titlePrefix(
            @NotNull Player player,
            @Nullable TitleConfig title
    ) {
        if (title == null) return Component.empty();
        return textRenderer.render(player, title.prefix());
    }
}
