package dev.souofrancisco.playertitles.bootstrap;

import dev.souofrancisco.playertitles.PlayerTitlesPlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import dev.souofrancisco.playertitles.bootstrap.module.ConfigModule;
import dev.souofrancisco.playertitles.bootstrap.module.CoreModule;
import dev.souofrancisco.playertitles.bootstrap.module.ListenerModule;
import dev.souofrancisco.playertitles.bootstrap.module.PersistenceModule;
import dev.souofrancisco.playertitles.bootstrap.module.PlaceholderModule;
import org.jetbrains.annotations.NotNull;

/**
 * Coordinates PlayerTitles startup and shutdown while keeping the Bukkit plugin entrypoint small.
 */
public final class PlayerTitlesBootstrap {

    private final @NotNull BootstrapContext context;
    private final @NotNull List<@NotNull PluginModule> modules;
    private final @NotNull List<@NotNull PluginModule> enabledModules = new ArrayList<>();
    private boolean enabled;

    public PlayerTitlesBootstrap(@NotNull PlayerTitlesPlugin plugin) {
        context = new BootstrapContext(Objects.requireNonNull(plugin, "plugin"));
        modules = List.of(
                new ConfigModule(),
                new PersistenceModule(),
                new CoreModule(),
                new ListenerModule(),
                new PlaceholderModule()
        );
    }

    public void enable() {
        if (enabled) {
            throw new IllegalStateException("PlayerTitles bootstrap is already enabled.");
        }

        try {
            for (PluginModule module : modules) {
                module.enable(context);
                enabledModules.add(module);
            }

            enabled = true;
        } catch (RuntimeException exception) {
            disable();
            throw exception;
        }
    }

    public void disable() {
        disableEnabledModules();
        context.playerTitlesApi(null);
        context.playerTitlesController(null);
        context.playerTitleCache(null);
        context.playerTitleRepository(null);
        context.databaseExecutor(null);
        context.database(null);
        context.pluginConfig(null);
        enabled = false;
    }

    private void disableEnabledModules() {
        for (int index = enabledModules.size() - 1; index >= 0; index--) {
            PluginModule module = enabledModules.get(index);
            module.disable(context);
        }

        enabledModules.clear();
    }
}
