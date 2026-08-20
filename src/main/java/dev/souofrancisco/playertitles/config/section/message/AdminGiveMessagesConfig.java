package dev.souofrancisco.playertitles.config.section.message;

import org.jetbrains.annotations.NotNull;

/**
 * Configured feedback templates for /titlesadmin give outcomes.
 */
public record AdminGiveMessagesConfig(
        @NotNull String success,
        @NotNull String alreadyUnlocked,
        @NotNull String titleNotFound,
        @NotNull String playerNotLoaded
) {}
