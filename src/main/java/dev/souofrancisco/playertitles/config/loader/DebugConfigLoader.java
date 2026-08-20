package dev.souofrancisco.playertitles.config.loader;

import dev.souofrancisco.playertitles.config.ConfigReader;
import dev.souofrancisco.playertitles.config.section.DebugConfig;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Loads the debug toggle from the root {@code config.yml}.
 */
@UtilityClass
public final class DebugConfigLoader {

    public static @NotNull DebugConfig load(@NotNull ConfigReader root) {
        return new DebugConfig(root.optionalBoolean("debug", false));
    }
}
