package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.listener.PlayerConnectionListener;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registers runtime Bukkit listeners after core services are available.
 */
public final class ListenerModule implements PluginModule {

    private @Nullable PlayerConnectionListener listener;

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PlayerTitlesController controller = context.playerTitlesController();
        if (controller == null)
            throw new IllegalStateException("PlayerTitlesController must be initialized before ListenerModule.");

        listener = new PlayerConnectionListener(controller);
        context.plugin().getServer().getPluginManager().registerEvents(listener, context.plugin());

        for (Player player : context.plugin().getServer().getOnlinePlayers()) {
            controller.loadPlayer(player);
        }
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        if (listener != null) {
            HandlerList.unregisterAll(listener);
            listener = null;
        }
    }
}
