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
 * Turns configured templates into Adventure components.
 *
 * <p>Two paths — do not mix them:
 * <ul>
 *     <li>{@link #render(String)} / {@link #render(String, TagResolver)} — pure MiniMessage only.
 *         Safe from arbitrary threads (PAPI expansion, ForkJoinPool, etc.). No {@code Player}, no PAPI.</li>
 *     <li>{@link #render(Player, String)} overloads — PAPI first, then MiniMessage. GUI/commands only;
 *         needs a live {@code Player} on the correct thread. Never call from {@code %playertitles_title%}.</li>
 * </ul>
 */
@RequiredArgsConstructor
public final class TextRenderer {

    private final @NotNull JavaPlugin plugin;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    // Pure path: thread-safe. Use from TitlePlaceholderResolver and internal title tag inserts.
    public @NotNull Component render(@NotNull String raw) {
        return render(raw, TagResolver.empty());
    }

    public @NotNull Component render(@NotNull String raw, @NotNull TagResolver tagResolver) {
        return miniMessage.deserialize(raw, tagResolver);
    }

    // Player-aware path: touches PlaceholderAPI — GUI / admin commands only, not PAPI expansion callbacks.
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
        return render(player, raw, tagResolver(context));
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

    // Re-entrant PAPI here caused Folia issues when called from async expansion threads.
    private @NotNull String applyExternalPlaceholders(
            @NotNull Player player,
            @NotNull String raw
    ) {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) return raw;
        return PlaceholderAPI.setPlaceholders(player, raw);
    }

    private @NotNull TagResolver tagResolver(@NotNull RenderContext context) {
        return TagResolver.builder()
                .resolver(TagResolver.resolver("selected_title", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titleDisplayName(context.selectedTitle()))))
                .resolver(TagResolver.resolver("title_name", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titleDisplayName(context.title()))))
                .resolver(TagResolver.resolver("title_prefix", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titlePrefix(context.title()))))
                .resolver(TagResolver.resolver("title_status", (arguments, tagContext) ->
                        Tag.selfClosingInserting(Component.text(context.statusName()))))
                .build();
    }

    // PlayerTitles-owned values: pure render only — avoids PAPI → expansion → PAPI recursion.
    private @NotNull Component titleDisplayName(@Nullable TitleConfig title) {
        if (title == null) return Component.empty();
        return render(title.displayName());
    }

    private @NotNull Component titlePrefix(@Nullable TitleConfig title) {
        if (title == null) return Component.empty();
        return render(title.prefix());
    }
}
