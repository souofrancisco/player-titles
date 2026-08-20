package dev.souofrancisco.playertitles.gui;

import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.NavigationMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.SelectedTitleMenuItemConfig;
import dev.souofrancisco.playertitles.config.section.menu.impl.StaticMenuItemConfig;
import dev.souofrancisco.playertitles.gui.item.NavigationItem;
import dev.souofrancisco.playertitles.gui.item.SelectedTitleItem;
import dev.souofrancisco.playertitles.gui.item.TitleItem;
import dev.souofrancisco.playertitles.gui.model.MenuState;
import dev.souofrancisco.playertitles.gui.render.ItemRenderer;
import dev.souofrancisco.playertitles.gui.render.RenderContext;
import dev.souofrancisco.playertitles.gui.render.TextRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.gui.structure.Structure;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

/**
 * Internal GUI consumer of {@link PlayerTitlesController} that owns the runtime state snapshot.
 */
public final class PlayerTitlesMenu {

    private final @NotNull PluginConfig pluginConfig;
    private final @NotNull PlayerTitlesController controller;

    private final @NotNull TextRenderer textRenderer;
    private final @NotNull ItemRenderer itemRenderer;

    public PlayerTitlesMenu(
            @NotNull JavaPlugin plugin,
            @NotNull PluginConfig pluginConfig,
            @NotNull PlayerTitlesController controller
    ) {
        this.pluginConfig = pluginConfig;
        this.controller = controller;
        this.textRenderer = new TextRenderer(plugin);
        this.itemRenderer = new ItemRenderer(textRenderer);
    }

    public void open(@NotNull Player player) {
        UUID playerId = player.getUniqueId();
        AtomicReference<MenuState> state = new AtomicReference<>(loadState(playerId));
        Supplier<MenuState> stateSupplier = state::get;
        List<Item> refreshableItems = new ArrayList<>();
        Runnable refreshMenu = () -> {
            state.set(loadState(playerId));
            refreshableItems.forEach(Item::notifyWindows);
        };

        Structure structure = structure(player, stateSupplier, refreshableItems, refreshMenu);
        List<Item> titleItems = titleItems(stateSupplier, refreshableItems, refreshMenu);
        PagedGui<Item> gui = PagedGui.items()
                .setStructure(structure)
                .setContent(titleItems)
                .build();

        Component title = textRenderer.render(
                player,
                pluginConfig.menu().title(),
                renderContext(state.get())
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
            @NotNull Supplier<@NotNull MenuState> stateSupplier,
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
                        symbol, new SimpleItem(itemRenderer.renderItem(
                                player,
                                staticItem.appearance(),
                                renderContext(stateSupplier.get())
                        ))
                );

                case NavigationMenuItemConfig navigation -> structure.addIngredient(
                        symbol,
                        new NavigationItem(navigation, itemRenderer, player, () -> renderContext(stateSupplier.get()))
                );

                case SelectedTitleMenuItemConfig selectedTitle -> {
                    SelectedTitleItem item = new SelectedTitleItem(
                            controller,
                            selectedTitle,
                            itemRenderer,
                            stateSupplier,
                            () -> renderContext(stateSupplier.get()),
                            refreshMenu
                    );

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
            @NotNull Supplier<@NotNull MenuState> stateSupplier,
            @NotNull List<@NotNull Item> refreshableItems,
            @NotNull Runnable refreshMenu
    ) {
        List<Item> items = new ArrayList<>();
        for (TitleConfig title : pluginConfig.titles().values()) {
            TitleItem item = new TitleItem(
                    controller,
                    pluginConfig.menu(),
                    title,
                    itemRenderer,
                    stateSupplier,
                    () -> renderContext(stateSupplier.get()),
                    refreshMenu
            );
            refreshableItems.add(item);
            items.add(item);
        }

        return List.copyOf(items);
    }

    private @NotNull MenuState loadState(@NotNull UUID playerId) {
        return new MenuState(
                controller.getUnlockedTitles(playerId),
                controller.getSelectedTitle(playerId).orElse(null)
        );
    }

    private @NotNull RenderContext renderContext(@NotNull MenuState state) {
        return RenderContext.menu(selectedTitle(state));
    }

    private @Nullable TitleConfig selectedTitle(@NotNull MenuState state) {
        String selectedTitleId = state.selectedTitleId();
        return selectedTitleId == null ? null : pluginConfig.titles().get(selectedTitleId);
    }
}
