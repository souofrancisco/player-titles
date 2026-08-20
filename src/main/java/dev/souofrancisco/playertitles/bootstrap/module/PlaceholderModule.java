package dev.souofrancisco.playertitles.bootstrap.module;

import dev.souofrancisco.playertitles.api.PlayerTitlesApi;
import dev.souofrancisco.playertitles.bootstrap.BootstrapContext;
import dev.souofrancisco.playertitles.bootstrap.PluginModule;
import dev.souofrancisco.playertitles.placeholder.PlayerTitlesExpansion;
import dev.souofrancisco.playertitles.placeholder.PlayerTitlesPlaceholderParser;
import dev.souofrancisco.playertitles.placeholder.resolver.TitlePlaceholderResolver;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registers PlaceholderAPI support when the optional dependency is present.
 */
public final class PlaceholderModule implements PluginModule {

    private @Nullable PlayerTitlesExpansion expansion;

    @Override
    public void enable(@NotNull BootstrapContext context) {
        PluginManager pluginManager = context.plugin().getServer().getPluginManager();
        if (!pluginManager.isPluginEnabled("PlaceholderAPI")) return;

        PlayerTitlesApi api = context.playerTitlesApi();
        if (api == null)
            throw new IllegalStateException("PlayerTitlesApi must be initialized before PlaceholderModule.");

        PlayerTitlesPlaceholderParser parser = new PlayerTitlesPlaceholderParser(
                new TitlePlaceholderResolver(api)
        );

        expansion = new PlayerTitlesExpansion(context.plugin(), parser);
        expansion.register();
    }

    @Override
    public void disable(@NotNull BootstrapContext context) {
        if (expansion == null) return;

        expansion.unregister();
        expansion = null;
    }
}
