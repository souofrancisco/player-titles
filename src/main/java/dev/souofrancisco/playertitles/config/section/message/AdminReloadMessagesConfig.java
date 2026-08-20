package dev.souofrancisco.playertitles.config.section.message;

import org.jetbrains.annotations.NotNull;

/**
 * Configured administrative configuration reload feedback templates.
 */
public record AdminReloadMessagesConfig(
        @NotNull String success,
        @NotNull String successDatabaseRestartRequired,
        @NotNull String failure
) {}
