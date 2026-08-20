package dev.souofrancisco.playertitles.config.section.message;

import org.jetbrains.annotations.NotNull;

/**
 * Root configured feedback templates for plugin-facing messages.
 */
public record PluginMessagesConfig(@NotNull AdminMessagesConfig admin) {}
