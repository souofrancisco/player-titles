package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * Root immutable configuration object used by runtime code after YAML has been validated.
 *
 * @param database typed persistence configuration
 * @param titles immutable title configurations keyed by their validated title IDs
 */
public record PluginConfig(
        @NotNull DatabaseConfig database,
        @NotNull Map<@NotNull String, @NotNull TitleConfig> titles
) {

    public PluginConfig {
        titles = Map.copyOf(titles);
    }
}
