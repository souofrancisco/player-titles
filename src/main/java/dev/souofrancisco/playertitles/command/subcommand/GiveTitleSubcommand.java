package dev.souofrancisco.playertitles.command.subcommand;

import com.destroystokyo.paper.profile.PlayerProfile;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminGiveMessagesConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.render.TextRenderer;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * {@code /titlesadmin give <player> <title>} for loaded and offline targets.
 *
 * <p>The target is resolved to a UUID through an async profile lookup, so no {@code Player} is
 * required; feedback is rendered through the pure MiniMessage path and delivered on a Folia-safe
 * scheduler because both the lookup and the unlock complete off the command thread.
 */
@RequiredArgsConstructor
public final class GiveTitleSubcommand {

    private static final @NotNull String PERMISSION = "playertitles.admin.give";

    private final @NotNull JavaPlugin plugin;
    private final @NotNull PlayerTitlesController controller;
    private final @NotNull TextRenderer textRenderer;

    public @NotNull CommandAPICommand create() {
        return new CommandAPICommand("give")
                .withPermission(PERMISSION)
                .withArguments(new AsyncPlayerProfileArgument("player"))
                .withArguments(titleArgument())
                .executes((sender, args) -> {
                    CompletableFuture<List<PlayerProfile>> profiles = args.getUnchecked("player");
                    String titleId = args.getUnchecked("title");

                    if (profiles == null || titleId == null)
                        throw new IllegalStateException("Required command arguments are missing.");

                    execute(sender, profiles, titleId);
                });
    }

    private void execute(
            @NotNull CommandSender sender,
            @NotNull CompletableFuture<List<PlayerProfile>> profilesFuture,
            @NotNull String titleId
    ) {
        profilesFuture.whenComplete((profiles, exception) -> {
            if (exception != null) {

                plugin.getLogger().log(
                        Level.FINE,
                        "Could not resolve a profile for /titlesadmin give.",
                        exception
                );

                reply(sender, textRenderer.render(messages().playerNotFound()));
                return;
            }

            ResolvedTarget resolved = resolveSingleTarget(profiles);
            if (resolved == null) {
                reply(sender, textRenderer.render(messages().playerNotFound()));
                return;
            }

            unlock(sender, resolved, titleId);
        });
    }

    private void unlock(
            @NotNull CommandSender sender,
            @NotNull ResolvedTarget target,
            @NotNull String titleId
    ) {
        controller.unlockTitleAsync(target.playerId(), titleId)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        plugin.getLogger().log(
                                Level.SEVERE,
                                "Could not grant title " + titleId + " to " + target.playerId() + ".",
                                exception
                        );
                        reply(sender, renderFailure(exception));
                        return;
                    }

                    reply(sender, render(target.playerName(), titleId, message(result)));
                });
    }

    /**
     * @return the single resolved target, or {@code null} when the input matched no player or more
     *     than one, which a selector-like argument can do
     */
    private @Nullable ResolvedTarget resolveSingleTarget(@Nullable List<PlayerProfile> profiles) {
        if (profiles == null || profiles.size() != 1) {
            return null;
        }

        PlayerProfile profile = profiles.getFirst();
        UUID playerId = profile.getId();
        if (playerId == null) {
            return null;
        }

        String playerName = profile.getName();
        if (playerName == null || playerName.isBlank()) {
            playerName = playerId.toString();
        }

        return new ResolvedTarget(playerId, playerName);
    }

    private @NotNull Argument<String> titleArgument() {
        return new StringArgument("title")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                        ConfigLoader.current().titles().keySet().toArray(String[]::new)
                ));
    }

    private @NotNull AdminGiveMessagesConfig messages() {
        return ConfigLoader.current().messages().admin().give();
    }

    private @NotNull String message(@NotNull TitleUnlockResult result) {
        AdminGiveMessagesConfig messages = messages();
        return switch (result) {
            case UNLOCKED -> messages.success();
            case ALREADY_UNLOCKED -> messages.alreadyUnlocked();
            case TITLE_NOT_FOUND -> messages.titleNotFound();
            // Unreachable for the persistent path; kept for the loaded-only cache result.
            case PLAYER_NOT_LOADED -> messages.playerNotLoaded();
        };
    }

    private @NotNull Component render(
            @NotNull String playerName,
            @NotNull String titleId,
            @NotNull String rawMessage
    ) {
        return textRenderer.render(rawMessage, tagResolver(playerName, titleId));
    }

    private @NotNull Component renderFailure(@NotNull Throwable exception) {
        Throwable cause = exception instanceof CompletionException && exception.getCause() != null
                ? exception.getCause()
                : exception;
        String reason = cause.getMessage() == null || cause.getMessage().isBlank()
                ? cause.getClass().getSimpleName()
                : cause.getMessage();

        return textRenderer.render(
                messages().failure(),
                TagResolver.resolver("error", (arguments, context) ->
                        Tag.selfClosingInserting(Component.text(reason)))
        );
    }

    private @NotNull TagResolver tagResolver(
            @NotNull String playerName,
            @NotNull String titleId
    ) {
        return TagResolver.builder()
                .resolver(TagResolver.resolver("player", (arguments, context) ->
                        Tag.selfClosingInserting(Component.text(playerName))))
                .resolver(TagResolver.resolver("title", (arguments, context) ->
                        Tag.selfClosingInserting(titleDisplayName(titleId))))
                .build();
    }

    private @NotNull Component titleDisplayName(@NotNull String titleId) {
        TitleConfig title = ConfigLoader.current().titles().get(titleId);
        return title == null ? Component.text(titleId) : textRenderer.render(title.displayName());
    }

    private void reply(@NotNull CommandSender sender, @NotNull Component message) {
        if (sender instanceof Player player) {
            player.getScheduler().execute(
                    plugin,
                    () -> player.sendMessage(message),
                    () -> {},
                    1L
            );
            return;
        }

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> sender.sendMessage(message));
    }

    private record ResolvedTarget(@NotNull UUID playerId, @NotNull String playerName) {}
}
