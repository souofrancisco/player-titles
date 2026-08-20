package dev.souofrancisco.playertitles.bootstrap.module;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.executors.PlayerCommandExecutor;
import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.gui.PlayerTitlesMenu;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import org.jetbrains.annotations.NotNull;

/**
 * Registers the internal command entry point for opening the controller-backed titles menu.
 */
public final class CommandModule implements PluginModule {

    private static final @NotNull String COMMAND_NAME = "playertitles";

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PluginConfig pluginConfig = context.pluginConfig();
        if (pluginConfig == null)
            throw new IllegalStateException("PluginConfig must be initialized before CommandModule.");

        PlayerTitlesController controller = context.playerTitlesController();
        if (controller == null)
            throw new IllegalStateException("PlayerTitlesController must be initialized before CommandModule.");

        PlayerTitlesMenu menu = new PlayerTitlesMenu(context.plugin(), pluginConfig, controller);
        new CommandAPICommand(COMMAND_NAME)
                .withAliases("titles")
                .withShortDescription("Opens the PlayerTitles menu.")
                .withUsage("/playertitles")
                .executesPlayer((PlayerCommandExecutor) (player, args) -> menu.open(player))
                .register(context.plugin());
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        CommandAPI.unregister(COMMAND_NAME);
    }
}
