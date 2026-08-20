package dev.souofrancisco.playertitles.gui.item;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import dev.souofrancisco.playertitles.config.section.menu.TitleStatusLoreConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.TitleStatus;
import dev.souofrancisco.playertitles.gui.TextRenderer;
import dev.souofrancisco.playertitles.result.TitleSelectionResult;
import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.ItemWrapper;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public final class TitleItem extends AbstractItem {

    private final @NotNull PlayerTitlesApi api;
    private final @NotNull MenuConfig menuConfig;
    private final @NotNull TitleConfig title;
    private final @NotNull TextRenderer renderer;
    private final @NotNull Runnable refreshMenu;

    public TitleItem(
            @NotNull PlayerTitlesApi api,
            @NotNull MenuConfig menuConfig,
            @NotNull TitleConfig title,
            @NotNull TextRenderer renderer,
            @NotNull Runnable refreshMenu
    ) {
        this.api = api;
        this.menuConfig = menuConfig;
        this.title = title;
        this.renderer = renderer;
        this.refreshMenu = refreshMenu;
    }

    @Override
    public @NotNull ItemProvider getItemProvider(@NotNull Player viewer) {
        TitleStatus status = status(viewer.getUniqueId());
        return new ItemWrapper(renderer.renderTitleItem(
                viewer,
                title.icon(),
                loreFor(status),
                TextRenderer.RenderContext.title(title, status)
        ));
    }

    @Override
    public void handleClick(
            @NotNull ClickType clickType,
            @NotNull Player player,
            @NotNull InventoryClickEvent event
    ) {
        UUID playerId = player.getUniqueId();
        if (status(playerId) != TitleStatus.UNLOCKED) return;

        TitleSelectionResult result = api.selectTitle(playerId, title.id());
        if (result == TitleSelectionResult.SELECTED) refreshMenu.run();
    }

    private @NotNull TitleStatus status(@NotNull UUID playerId) {
        if (api.getSelectedTitle(playerId).filter(title.id()::equals).isPresent())
            return TitleStatus.SELECTED;

        return api.hasTitle(playerId, title.id()) ? TitleStatus.UNLOCKED : TitleStatus.LOCKED;
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
