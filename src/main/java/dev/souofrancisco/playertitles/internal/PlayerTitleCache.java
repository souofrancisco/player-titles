package dev.souofrancisco.playertitles.internal;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Thread-safe runtime cache for loaded player title state.
 *
 * <p>The backing map is fully encapsulated. Mutating operations use atomic {@link ConcurrentHashMap}
 * methods so updates are safe when called from different Paper/Folia scheduler threads.
 */
public final class PlayerTitleCache {

    private final @NotNull ConcurrentHashMap<@NotNull UUID, @NotNull PlayerTitleState> states =
            new ConcurrentHashMap<>();

    public boolean isLoaded(@NotNull UUID playerId) {
        return states.containsKey(playerId);
    }

    public @NotNull Optional<PlayerTitleState> get(@NotNull UUID playerId) {
        return Optional.ofNullable(states.get(playerId));
    }

    public void load(@NotNull PlayerTitleState state) {
        states.put(state.playerId(), state);
    }

    public @NotNull Optional<PlayerTitleState> unload(@NotNull UUID playerId) {
        return Optional.ofNullable(states.remove(playerId));
    }

    public @NotNull Optional<PlayerTitleState> updateIfLoaded(
            @NotNull UUID playerId,
            @NotNull UnaryOperator<PlayerTitleState> updater
    ) {
        return Optional.ofNullable(
                states.computeIfPresent(
                        playerId,
                        (ignored, currentState)
                                -> updater.apply(currentState)
                )
        );
    }
}
