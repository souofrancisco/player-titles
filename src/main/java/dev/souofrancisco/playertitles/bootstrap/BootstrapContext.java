package dev.souofrancisco.playertitles.bootstrap;

import dev.souofrancisco.playertitles.PlayerTitlesPlugin;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.repository.Database;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the components shared between modules during plugin bootstrap.
 */
@Getter
@Setter
@Accessors(fluent = true)
@RequiredArgsConstructor
public final class BootstrapContext {

    private final @NotNull PlayerTitlesPlugin plugin;

    private @Nullable PluginConfig pluginConfig;
    private @Nullable Database database;
}