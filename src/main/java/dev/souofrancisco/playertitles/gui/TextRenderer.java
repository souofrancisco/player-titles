package dev.souofrancisco.playertitles.gui;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.TitleIconConfig;
import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.TitleStatus;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Centralized renderer for player-facing configurable text.
 */
public final class TextRenderer {

    private final @NotNull JavaPlugin plugin;
    private final @NotNull PlayerTitlesApi api;
    private final @NotNull Map<@NotNull String, @NotNull TitleConfig> titles;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    public TextRenderer(
            @NotNull JavaPlugin plugin,
            @NotNull PlayerTitlesApi api,
            @NotNull Map<@NotNull String, @NotNull TitleConfig> titles
    ) {
        this.plugin = plugin;
        this.api = api;
        this.titles = Map.copyOf(titles);
    }

    public @NotNull Component render(
            @NotNull Player player,
            @NotNull String raw,
            @NotNull RenderContext context
    ) {
        return miniMessage.deserialize(applyExternalPlaceholders(player, raw), tagResolver(player, context));
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

    public @NotNull ItemStack renderItem(
            @NotNull Player player,
            @NotNull ItemAppearanceConfig appearance,
            @NotNull RenderContext context
    ) {
        return buildItem(
                player,
                appearance.material(),
                appearance.itemModel(),
                appearance.name(),
                appearance.lore(),
                context
        );
    }

    public @NotNull ItemStack renderTitleItem(
            @NotNull Player player,
            @NotNull TitleIconConfig icon,
            @NotNull List<@NotNull String> statusLore,
            @NotNull RenderContext context
    ) {
        List<String> lore = new ArrayList<>(icon.lore());
        lore.addAll(statusLore);
        return buildItem(player, icon.material(), icon.itemModel(), icon.name(), lore, context);
    }

    private @NotNull ItemStack buildItem(
            @NotNull Player player,
            @NotNull org.bukkit.Material material,
            @Nullable NamespacedKey itemModel,
            @NotNull String rawName,
            @NotNull List<@NotNull String> rawLore,
            @NotNull RenderContext context
    ) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;

        meta.displayName(render(player, rawName, context));
        meta.lore(renderLore(player, rawLore, context));
        applyItemModel(meta, itemModel);
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void applyItemModel(@NotNull ItemMeta meta, @Nullable NamespacedKey itemModel) {
        if (itemModel == null) return;

        try {
            Method method = meta.getClass().getMethod("setItemModel", NamespacedKey.class);
            method.invoke(meta, itemModel);
        } catch (NoSuchMethodException ignored) {
            // Older compatible server APIs do not expose item_model yet.
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not apply configured item-model.", exception);
        }
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
                        Tag.selfClosingInserting(selectedTitleName(player))))
                .resolver(TagResolver.resolver("title_name", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titleName(player, context))))
                .resolver(TagResolver.resolver("title_prefix", (arguments, tagContext) ->
                        Tag.selfClosingInserting(titlePrefix(player, context))))
                .resolver(TagResolver.resolver("title_status", (arguments, tagContext) ->
                        Tag.selfClosingInserting(Component.text(context.statusName()))))
                .build();
    }

    private @NotNull Component selectedTitleName(@NotNull Player player) {
        Optional<String> selectedTitleId = api.getSelectedTitle(player.getUniqueId());
        return selectedTitleId
                .map(titles::get)
                .map(title -> miniMessage.deserialize(applyExternalPlaceholders(player, title.displayName())))
                .orElse(Component.empty());
    }

    private @NotNull Component titleName(
            @NotNull Player player,
            @NotNull RenderContext context
    ) {
        TitleConfig title = context.title();
        if (title == null) return Component.empty();
        return miniMessage.deserialize(applyExternalPlaceholders(player, title.displayName()));
    }

    private @NotNull Component titlePrefix(
            @NotNull Player player,
            @NotNull RenderContext context
    ) {
        TitleConfig title = context.title();
        if (title == null) return Component.empty();
        return miniMessage.deserialize(applyExternalPlaceholders(player, title.prefix()));
    }

    public record RenderContext(
            @Nullable TitleConfig title,
            @Nullable TitleStatus status
    ) {

        public static @NotNull RenderContext menu() {
            return new RenderContext(null, null);
        }

        public static @NotNull RenderContext title(
                @NotNull TitleConfig title,
                @NotNull TitleStatus status
        ) {
            return new RenderContext(title, status);
        }

        private @NotNull String statusName() {
            return status == null ? "" : status.name();
        }
    }
}
