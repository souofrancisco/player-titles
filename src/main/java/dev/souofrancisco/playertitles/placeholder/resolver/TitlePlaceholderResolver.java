package dev.souofrancisco.playertitles.placeholder.resolver;

import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.render.TextRenderer;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Resolves {@code %playertitles_title%} from the selected title prefix.
 *
 * <p>This path is pure: UUID lookup, cache/config read, MiniMessage, then legacy serialization.
 * It does not obtain a Bukkit {@code Player} or recursively resolve external PlaceholderAPI
 * placeholders embedded in the prefix.
 */
@RequiredArgsConstructor
public final class TitlePlaceholderResolver implements PlaceholderResolver {

    private static final @NotNull String KEY = "title";
    private static final @NotNull LegacyComponentSerializer PAPI_TEXT = LegacyComponentSerializer.builder()
            .character(LegacyComponentSerializer.SECTION_CHAR)
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private final @NotNull PlayerTitlesController controller;
    private final @NotNull TextRenderer textRenderer;

    @Override
    public boolean supports(@NotNull String key) {
        return KEY.equals(key);
    }

    @Override
    public @NotNull String resolve(@Nullable OfflinePlayer player) {
        if (player == null) return "";

        UUID playerId = player.getUniqueId();
        if (!controller.isLoaded(playerId)) return "";

        return controller.getSelectedTitlePrefix(playerId)
                .map(prefix -> PAPI_TEXT.serialize(textRenderer.render(prefix)))
                .orElse("");
    }
}
