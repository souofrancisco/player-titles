package dev.souofrancisco.playertitles.config.section.message;

import org.jetbrains.annotations.NotNull;

/**
 * Configured feedback templates for /titlesadmin revoke outcomes.
 */
public record AdminRevokeMessagesConfig(
        @NotNull String success,
        @NotNull String notUnlocked,
        @NotNull String titleNotFound,
        @NotNull String playerNotLoaded
) {}
