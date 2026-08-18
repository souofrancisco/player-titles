package dev.souofrancisco.playertitles.config;

import dev.souofrancisco.playertitles.config.section.TitleConfig;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Root immutable configuration object used by runtime code after YAML has been validated.
 *
 * @param titles immutable title configurations keyed by their validated title IDs
 */
public record PluginConfig(
        @NotNull Map<@NotNull String, @NotNull TitleConfig> titles
) {

    public PluginConfig {
        Objects.requireNonNull(titles, "titles");
        titles = Map.copyOf(titles);
    }
}
