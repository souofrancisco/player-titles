package dev.souofrancisco.playertitles.config.section.message;

import org.jetbrains.annotations.NotNull;

/**
 * Configured administrative command feedback templates.
 */
public record AdminMessagesConfig(
        @NotNull AdminGiveMessagesConfig give,
        @NotNull AdminRevokeMessagesConfig revoke,
        @NotNull AdminReloadMessagesConfig reload
) {}
