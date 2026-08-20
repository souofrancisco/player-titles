package dev.souofrancisco.playertitles.gui;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.NavigationMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.SelectedTitleMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.StaticMenuItemConfig;
import dev.souofrancisco.playertitles.gui.item.NavigationItem;
import dev.souofrancisco.playertitles.gui.item.SelectedTitleItem;
import dev.souofrancisco.playertitles.gui.item.TitleItem;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

/**
 * Opens the configured title browser and selection menu for a player.
 */
public final class PlayerTitlesMenu {

    private final @NotNull PluginConfig pluginConfig;
    private final @NotNull PlayerTitlesApi api;
    private final @NotNull TextRenderer renderer;

    public PlayerTitlesMenu(
            @NotNull JavaPlugin plugin,
            @NotNull PluginConfig pluginConfig,
            @NotNull PlayerTitlesApi api
    ) {
        this.pluginConfig = pluginConfig;
        this.api = api;
        this.renderer = new TextRenderer(plugin, api, pluginConfig.titles());
    }

    public void open(@NotNull Player player) {
        List<Item> refreshableItems = new ArrayList<>();
        Runnable refreshMenu = () -> refreshableItems.forEach(Item::notifyWindows);

        Structure structure = structure(player, refreshableItems, refreshMenu);
        List<Item> titleItems = titleItems(refreshableItems, refreshMenu);
        PagedGui<Item> gui = PagedGui.items()
                .setStructure(structure)
                .setContent(titleItems)
                .build();

        Component title = renderer.render(
                player,
                pluginConfig.menu().title(),
                TextRenderer.RenderContext.menu()
        );
        Window.single()
                .setViewer(player)
                .setTitle(new AdventureComponentWrapper(title))
                .setGui(gui)
                .build()
                .open();
    }

    private @NotNull Structure structure(
            @NotNull Player player,
            @NotNull List<@NotNull Item> refreshableItems,
            @NotNull Runnable refreshMenu
    ) {
        Structure structure = new Structure(pluginConfig.menu().layout().toArray(String[]::new));
        structure.addIngredient(pluginConfig.menu().titleSlot(), Markers.CONTENT_LIST_SLOT_HORIZONTAL);

        for (var entry : pluginConfig.menu().items().entrySet()) {
            char symbol = entry.getKey();
            MenuItemConfig itemConfig = entry.getValue();
            switch (itemConfig) {
                case StaticMenuItemConfig staticItem -> structure.addIngredient(
                        symbol,
                        new SimpleItem(renderer.renderItem(
                                player,
                                staticItem.appearance(),
                                TextRenderer.RenderContext.menu()
                        ))
                );
                case NavigationMenuItemConfig navigation -> structure.addIngredient(
                        symbol,
                        new NavigationItem(navigation, renderer, player)
                );
                case SelectedTitleMenuItemConfig selectedTitle -> {
                    SelectedTitleItem item = new SelectedTitleItem(api, selectedTitle, renderer, refreshMenu);
                    refreshableItems.add(item);
                    structure.addIngredient(symbol, item);
                }
                default -> throw new IllegalStateException(
                        "Unsupported menu item config: " + itemConfig.getClass().getName()
                );
            }
        }

        return structure;
    }

    private @NotNull List<@NotNull Item> titleItems(
            @NotNull List<@NotNull Item> refreshableItems,
            @NotNull Runnable refreshMenu
    ) {
        List<Item> items = new ArrayList<>();
        for (TitleConfig title : pluginConfig.titles().values()) {
            TitleItem item = new TitleItem(api, pluginConfig.menu(), title, renderer, refreshMenu);
            refreshableItems.add(item);
            items.add(item);
        }

        return List.copyOf(items);
    }
}
