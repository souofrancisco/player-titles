package dev.souofrancisco.playertitles.render;

import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.menu.type.TitleStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Values available to PlayerTitles internal MiniMessage tags during GUI rendering.
 */
public record RenderContext(
        @Nullable TitleConfig selectedTitle,
        @Nullable TitleConfig title,
        @Nullable TitleStatus status
) {

    public static @NotNull RenderContext menu(@Nullable TitleConfig selectedTitle) {
        return new RenderContext(selectedTitle, null, null);
    }

    public static @NotNull RenderContext title(
            @Nullable TitleConfig selectedTitle,
            @NotNull TitleConfig title,
            @NotNull TitleStatus status
    ) {
        return new RenderContext(selectedTitle, title, status);
    }

    @NotNull String statusName() {
        return status == null ? "" : status.name();
    }
}
