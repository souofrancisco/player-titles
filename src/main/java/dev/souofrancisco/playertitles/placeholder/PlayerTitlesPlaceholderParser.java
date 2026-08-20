package dev.souofrancisco.playertitles.placeholder;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dev.souofrancisco.playertitles.placeholder.resolver.PlaceholderResolver;
import lombok.RequiredArgsConstructor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Routes PlaceholderAPI params to the first resolver that supports the requested key.
 */
@RequiredArgsConstructor
public final class PlayerTitlesPlaceholderParser {

    private final @NotNull List<@NotNull PlaceholderResolver> resolvers;

    public PlayerTitlesPlaceholderParser(@NotNull PlaceholderResolver... resolvers) {
        this.resolvers = List.copyOf(Arrays.asList(resolvers));
    }

    public @NotNull String parse(
            @Nullable OfflinePlayer player,
            @NotNull String params
    ) {
        String key = params.toLowerCase(Locale.ROOT);
        return resolvers.stream()
                .filter(resolver -> resolver.supports(key))
                .findFirst()
                .map(resolver -> resolver.resolve(player))
                .orElse("");
    }
}
