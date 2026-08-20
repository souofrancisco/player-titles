package dev.souofrancisco.playertitles.internal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.jetbrains.annotations.NotNull;

/**
 * Thread-safe runtime cache for loaded player title state.
 *
 * <p>This class is the concurrency boundary for immutable {@link PlayerTitleState} snapshots. The
 * backing map is fully encapsulated and mutating operations use atomic {@link ConcurrentHashMap}
 * methods. It must not contain database, repository, executor, or Bukkit/Folia scheduler logic.
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

    public @NotNull Collection<@NotNull PlayerTitleState> snapshot() {
        return List.copyOf(states.values());
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

    /**
     * Atomically revokes a title from a loaded state when the caller-approved guard passes.
     */
    public @NotNull Optional<PlayerTitleState> revokeIfLoaded(
            @NotNull UUID playerId,
            @NotNull String titleId,
            @NotNull Predicate<PlayerTitleState> guard
    ) {
        return updateIfLoaded(playerId, state -> guard.test(state) ? state.revoke(titleId) : state);
    }
}
