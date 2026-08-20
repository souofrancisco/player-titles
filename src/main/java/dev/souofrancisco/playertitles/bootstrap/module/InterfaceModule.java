package dev.souofrancisco.playertitles.bootstrap.module;

import dev.jorel.commandapi.CommandAPI;
import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.command.TitlesAdminCommand;
import dev.souofrancisco.playertitles.command.TitlesCommand;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.gui.PlayerTitlesMenu;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.text.PlayerTitlesTextRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * Bootstraps the plugin's player and administrator interaction layer.
 */
public final class InterfaceModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PluginConfig pluginConfig = context.pluginConfig();
        if (pluginConfig == null)
            throw new IllegalStateException("PluginConfig must be initialized before InterfaceModule.");

        PlayerTitlesController controller = context.playerTitlesController();
        if (controller == null)
            throw new IllegalStateException("PlayerTitlesController must be initialized before InterfaceModule.");

        PlayerTitlesMenu menu = new PlayerTitlesMenu(context.plugin(), pluginConfig, controller);
        PlayerTitlesTextRenderer textRenderer = new PlayerTitlesTextRenderer(context.plugin());

        new TitlesCommand(menu).register(context.plugin());
        new TitlesAdminCommand(controller, pluginConfig, textRenderer).register(context.plugin());
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        CommandAPI.unregister(TitlesCommand.NAME);
        CommandAPI.unregister(TitlesAdminCommand.NAME);
    }
}
