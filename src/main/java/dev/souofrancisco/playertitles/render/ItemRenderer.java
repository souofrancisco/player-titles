package dev.souofrancisco.playertitles.render;

import dev.souofrancisco.playertitles.config.section.TitleIconConfig;
import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Builds GUI ItemStacks from configured item appearance and delegates all text component rendering
 * to {@link TextRenderer}.
 */
@RequiredArgsConstructor
public final class ItemRenderer {

    private final @NotNull TextRenderer textRenderer;

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

        meta.displayName(textRenderer.render(player, rawName, context));
        meta.lore(textRenderer.renderLore(player, rawLore, context));
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
}
