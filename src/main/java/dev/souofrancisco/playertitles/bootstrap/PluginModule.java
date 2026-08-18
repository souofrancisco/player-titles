package dev.souofrancisco.playertitles.bootstrap;

import org.jetbrains.annotations.NotNull;

/**
 * Startup unit owned by the plugin bootstrap.
 *
 * <p>Modules are enabled in the order registered by the bootstrap and disabled in reverse order.
 */
public interface PluginModule {

    void enable(@NotNull BootstrapContext context);

    default void disable(@NotNull BootstrapContext context) {
    }

}
