package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.config.section.DebugConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.MenuConfig;
import dev.souofrancisco.playertitles.config.section.message.PluginMessagesConfig;
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
 * @param messages typed command feedback templates
 * @param debug runtime debug toggle and verbosity level
 */
public record PluginConfig(
        @NotNull DatabaseConfig database,
        @NotNull Map<@NotNull String, @NotNull TitleConfig> titles,
        @NotNull MenuConfig menu,
        @NotNull PluginMessagesConfig messages,
        @NotNull DebugConfig debug
) {

    public PluginConfig {
        titles = Collections.unmodifiableMap(new LinkedHashMap<>(titles));
    }
}
