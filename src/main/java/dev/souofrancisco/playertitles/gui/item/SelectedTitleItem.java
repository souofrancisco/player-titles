package dev.souofrancisco.playertitles.gui.item;

import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.SelectedTitleMenuItemConfig;
import dev.souofrancisco.playertitles.gui.model.MenuState;
import dev.souofrancisco.playertitles.render.ItemRenderer;
import dev.souofrancisco.playertitles.render.RenderContext;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.impl.AbstractItem;

/**
 * Shows and clears the selected-title control using the menu's shared state snapshot.
 */
@RequiredArgsConstructor
public final class SelectedTitleItem extends AbstractItem {

    private final @NotNull PlayerTitlesController controller;
    private final @NotNull SelectedTitleMenuItemConfig config;
    private final @NotNull ItemRenderer renderer;
    private final @NotNull Supplier<@NotNull MenuState> stateSupplier;
    private final @NotNull Supplier<@NotNull RenderContext> renderContextSupplier;
    private final @NotNull Runnable refreshMenu;

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        ItemAppearanceConfig appearance = stateSupplier.get().selectedTitleId() == null
                ? config.none()
                : config.selected();
        return new ItemWrapper(renderer.renderItem(viewer, appearance, renderContextSupplier.get()));
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType,
            @NotNull Player player,
            @NotNull InventoryClickEvent event
    ) {
        if (stateSupplier.get().selectedTitleId() == null) return;

        TitleSelectionResult result = controller.clearSelectedTitle(player.getUniqueId());
        if (result == TitleSelectionResult.CLEARED) refreshMenu.run();
    }
}
