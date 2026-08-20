package dev.souofrancisco.playertitles.gui.item;

import dev.souofrancisco.playertitles.config.section.menu.ItemAppearanceConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.NavigationMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.NavigationDirection;
import dev.souofrancisco.playertitles.gui.TextRenderer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public final class NavigationItem extends PageItem {

    private final @NotNull NavigationMenuItemConfig config;
    private final @NotNull TextRenderer renderer;
    private final @NotNull Player viewer;

    public NavigationItem(
            @NotNull NavigationMenuItemConfig config,
            @NotNull TextRenderer renderer,
            @NotNull Player viewer
    ) {
        super(config.direction() == NavigationDirection.NEXT);
        this.config = config;
        this.renderer = renderer;
        this.viewer = viewer;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull PagedGui<?> gui) {
        ItemAppearanceConfig appearance = canChangePage(gui) ? config.available() : config.unavailable();
        return new ItemWrapper(renderer.renderItem(viewer, appearance, TextRenderer.RenderContext.menu()));
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType,
            @NotNull Player player,
            @NotNull InventoryClickEvent event
    ) {
        PagedGui<?> gui = getGui();
        if (gui == null || !canChangePage(gui)) return;
        super.handleClick(clickType, player, event);
    }

    private boolean canChangePage(@NotNull PagedGui<?> gui) {
        return config.direction() == NavigationDirection.NEXT
                ? gui.hasNextPage()
                : gui.hasPreviousPage();
    }
}
