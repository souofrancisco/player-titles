package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.listener.PlayerConnectionListener;
import org.jetbrains.annotations.NotNull;

/**
 * Registers runtime Bukkit listeners after core services are available.
 */
public final class ListenerModule implements PluginModule {

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PlayerTitlesController controller = context.playerTitlesController();
        if (controller == null)
            throw new IllegalStateException("PlayerTitlesController must be initialized before ListenerModule.");

        PlayerConnectionListener listener = new PlayerConnectionListener(controller);

        context.plugin().getServer().getPluginManager().registerEvents(listener, context.plugin());
    }
}
