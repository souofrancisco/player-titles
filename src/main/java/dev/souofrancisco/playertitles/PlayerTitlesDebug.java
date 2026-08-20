package dev.souofrancisco.playertitles;

import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.section.DebugConfig;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Single gate for optional PlayerTitles debug output when {@code debug: true} in {@code config.yml}.
 */
@RequiredArgsConstructor
public final class PlayerTitlesDebug {

    private final @NotNull Logger logger;

    public void log(@NotNull String category, @NotNull Supplier<@NotNull String> message) {
        DebugConfig config;
        try {
            config = ConfigLoader.current().debug();
        } catch (IllegalStateException exception) {
            return;
        }

        if (!config.enabled()) return;

        logger.log(Level.INFO, "[PlayerTitles][" + category + "] " + message.get());
    }
}
