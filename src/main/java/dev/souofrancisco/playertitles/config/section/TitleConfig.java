package dev.souofrancisco.playertitles.config.section;

import org.jetbrains.annotations.NotNull;

/**
 * Immutable configuration for one title entry, keyed by {@link #id()} in the root title map.
 *
 * @param id validated title identifier from the YAML map key
 * @param displayName raw MiniMessage template for the title's display label
 * @param prefix raw MiniMessage and PlaceholderAPI template for the rendered title prefix
 * @param icon immutable icon configuration for future title menu rendering
 */
public record TitleConfig(
        @NotNull String id,
        @NotNull String displayName,
        @NotNull String prefix,
        @NotNull TitleIconConfig icon
) {}
