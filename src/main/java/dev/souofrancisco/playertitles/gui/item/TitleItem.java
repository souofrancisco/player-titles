package dev.souofrancisco.playertitles.gui.item;

import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import dev.souofrancisco.playertitles.config.section.menu.TitleStatusLoreConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.TitleStatus;
import dev.souofrancisco.playertitles.gui.model.MenuState;
import dev.souofrancisco.playertitles.render.ItemRenderer;
import dev.souofrancisco.playertitles.render.RenderContext;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import java.util.List;
import java.util.UUID;
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
 * Renders and handles clicks for one configured title using the menu's shared state snapshot.
 */
@RequiredArgsConstructor
public final class TitleItem extends AbstractItem {

    private final @NotNull PlayerTitlesController controller;
    private final @NotNull MenuConfig menuConfig;
    private final @NotNull TitleConfig title;
    private final @NotNull ItemRenderer renderer;
    private final @NotNull Supplier<@NotNull MenuState> stateSupplier;
    private final @NotNull Supplier<@NotNull RenderContext> renderContextSupplier;
    private final @NotNull Runnable refreshMenu;

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        TitleStatus status = stateSupplier.get().statusOf(title.id());
        return new ItemWrapper(renderer.renderTitleItem(
                viewer,
                title.icon(),
                loreFor(status),
                RenderContext.title(renderContextSupplier.get().selectedTitle(), title, status)
        ));
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType,
            @NotNull Player player,
            @NotNull InventoryClickEvent event
    ) {
        UUID playerId = player.getUniqueId();
        if (stateSupplier.get().statusOf(title.id()) != TitleStatus.UNLOCKED) return;

        TitleSelectionResult result = controller.selectTitle(playerId, title.id());
        if (result == TitleSelectionResult.SELECTED) refreshMenu.run();
    }

    private @NotNull List<@NotNull String> loreFor(@NotNull TitleStatus status) {
        TitleStatusLoreConfig titleStatus = menuConfig.titleStatus();
        return switch (status) {
            case LOCKED -> titleStatus.locked();
            case UNLOCKED -> titleStatus.unlocked();
            case SELECTED -> titleStatus.selected();
        };
    }
}
