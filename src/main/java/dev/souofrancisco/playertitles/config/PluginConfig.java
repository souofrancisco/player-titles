package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Root immutable configuration object used by runtime code after YAML has been validated.
 *
 * @param database typed persistence configuration
 * @param titles immutable title configurations keyed by their validated title IDs
 * @param menu typed inventory menu configuration
 */
public record PluginConfig(
        @NotNull DatabaseConfig database,
        @NotNull Map<@NotNull String, @NotNull TitleConfig> titles,
        @NotNull MenuConfig menu
) {

    public PluginConfig {
        titles = Collections.unmodifiableMap(new LinkedHashMap<>(titles));
    }
}
