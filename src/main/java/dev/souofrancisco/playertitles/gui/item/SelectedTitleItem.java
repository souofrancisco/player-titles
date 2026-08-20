package dev.souofrancisco.playertitles.gui.item;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.SelectedTitleMenuItemConfig;
import dev.souofrancisco.playertitles.gui.TextRenderer;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.impl.AbstractItem;

@RequiredArgsConstructor
public final class SelectedTitleItem extends AbstractItem {

    private final @NotNull PlayerTitlesApi api;
    private final @NotNull SelectedTitleMenuItemConfig config;
    private final @NotNull TextRenderer renderer;
    private final @NotNull Runnable refreshMenu;

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        ItemAppearanceConfig appearance = api.getSelectedTitle(viewer.getUniqueId()).isPresent()
                ? config.selected()
                : config.none();
        return new ItemWrapper(renderer.renderItem(viewer, appearance, TextRenderer.RenderContext.menu()));
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType,
            @NotNull Player player,
            @NotNull InventoryClickEvent event
    ) {
        if (api.getSelectedTitle(player.getUniqueId()).isEmpty()) return;

        TitleSelectionResult result = api.clearSelectedTitle(player.getUniqueId());
        if (result == TitleSelectionResult.CLEARED) refreshMenu.run();
    }
}
