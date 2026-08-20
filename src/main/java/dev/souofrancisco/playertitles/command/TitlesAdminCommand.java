package dev.souofrancisco.playertitles.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.souofrancisco.playertitles.config.ConfigLoader;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminGiveMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminReloadMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminRevokeMessagesConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.result.TitleRevokeResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import dev.souofrancisco.playertitles.render.TextRenderer;
import java.util.logging.Level;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Administrative title ownership and runtime configuration operations.
 */
@RequiredArgsConstructor
public final class TitlesAdminCommand {

    public static final @NotNull String NAME = "titlesadmin";

    private static final @NotNull String GIVE_PERMISSION = "playertitles.admin.give";
    private static final @NotNull String REVOKE_PERMISSION = "playertitles.admin.revoke";
    private static final @NotNull String RELOAD_PERMISSION = "playertitles.admin.reload";

    private final @NotNull JavaPlugin plugin;
    private final @NotNull PlayerTitlesController controller;
    private final @NotNull TextRenderer textRenderer;
    private final @NotNull MiniMessage miniMessage = MiniMessage.miniMessage();

    public void register(@NotNull JavaPlugin plugin) {
        new CommandAPICommand(NAME)
                .withShortDescription("Manages player title ownership.")
                .withSubcommand(new CommandAPICommand("give")
                        .withPermission(GIVE_PERMISSION)
                        .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                        .withArguments(titleArgument())
                        .executes((CommandExecutor) (sender, args) -> give(sender, args.get("player"), args.get("title"))))
                .withSubcommand(new CommandAPICommand("revoke")
                        .withPermission(REVOKE_PERMISSION)
                        .withArguments(new EntitySelectorArgument.OnePlayer("player"))
                        .withArguments(titleArgument())
                        .executes((CommandExecutor) (sender, args) -> revoke(sender, args.get("player"), args.get("title"))))
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(RELOAD_PERMISSION)
                        .executes((CommandExecutor) (sender, args) -> reload(sender)))
                .register(plugin);
    }

    private @NotNull Argument<String> titleArgument() {
        return new StringArgument("title")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                        currentConfig().titles().keySet().toArray(String[]::new)
                ));
    }

    private void give(
            @NotNull CommandSender sender,
            @NotNull Object target,
            @NotNull Object title
    ) {
        Player player = (Player) target;
        String titleId = (String) title;
        TitleUnlockResult result = controller.unlockTitle(player.getUniqueId(), titleId);

        sender.sendMessage(render(player, titleId, giveMessage(result)));
    }

    private void revoke(
            @NotNull CommandSender sender,
            @NotNull Object target,
            @NotNull Object title
    ) {
        Player player = (Player) target;
        String titleId = (String) title;
        TitleRevokeResult result = controller.revokeTitle(player.getUniqueId(), titleId);

        sender.sendMessage(render(player, titleId, revokeMessage(result)));
    }

    private void reload(@NotNull CommandSender sender) {
        try {
            PluginConfig previous = ConfigLoader.current();
            PluginConfig next = ConfigLoader.reload(plugin);
            AdminReloadMessagesConfig messages = next.messages().admin().reload();
            String rawMessage = previous.database().equals(next.database())
                    ? messages.success()
                    : messages.successDatabaseRestartRequired();
            sender.sendMessage(miniMessage.deserialize(rawMessage));
        } catch (RuntimeException exception) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Could not reload PlayerTitles configuration from config.yml, titles.yml, and menu.yml."
                            + " Keeping the previous working configuration active.",
                    exception
            );
            sender.sendMessage(miniMessage.deserialize(
                    ConfigLoader.current().messages().admin().reload().failure(),
                    TagResolver.resolver("error", (arguments, context) ->
                            Tag.selfClosingInserting(Component.text(failureMessage(exception))))
            ));
        }
    }

    private @NotNull String giveMessage(@NotNull TitleUnlockResult result) {
        AdminGiveMessagesConfig messages = currentConfig().messages().admin().give();
        return switch (result) {
            case UNLOCKED -> messages.success();
            case ALREADY_UNLOCKED -> messages.alreadyUnlocked();
            case TITLE_NOT_FOUND -> messages.titleNotFound();
            case PLAYER_NOT_LOADED -> messages.playerNotLoaded();
        };
    }

    private @NotNull String revokeMessage(@NotNull TitleRevokeResult result) {
        AdminRevokeMessagesConfig messages = currentConfig().messages().admin().revoke();
        return switch (result) {
            case REVOKED -> messages.success();
            case NOT_UNLOCKED -> messages.notUnlocked();
            case TITLE_NOT_FOUND -> messages.titleNotFound();
            case PLAYER_NOT_LOADED -> messages.playerNotLoaded();
        };
    }

    private @NotNull Component render(
            @NotNull Player player,
            @NotNull String titleId,
            @NotNull String rawMessage
    ) {
        return textRenderer.render(player, rawMessage, tagResolver(player, titleId));
    }

    private @NotNull TagResolver tagResolver(
            @NotNull Player player,
            @NotNull String titleId
    ) {
        return TagResolver.builder()
                .resolver(TagResolver.resolver("player", (arguments, context) ->
                        Tag.selfClosingInserting(Component.text(player.getName()))))
                .resolver(TagResolver.resolver("title", (arguments, context) ->
                        Tag.selfClosingInserting(titleDisplayName(player, titleId))))
                .build();
    }

    private @NotNull Component titleDisplayName(
            @NotNull Player player,
            @NotNull String titleId
    ) {
        TitleConfig title = currentConfig().titles().get(titleId);
        return titleName(player, title, titleId);
    }

    private @NotNull Component titleName(
            @NotNull Player player,
            @Nullable TitleConfig title,
            @NotNull String titleId
    ) {
        return title == null ? Component.text(titleId) : textRenderer.render(player, title.displayName());
    }

    private @NotNull PluginConfig currentConfig() {
        return ConfigLoader.current();
    }

    private @NotNull String failureMessage(@Nullable Throwable failure) {
        if (failure == null) return "unknown error";
        String message = failure.getMessage();
        return message == null || message.isBlank() ? failure.getClass().getSimpleName() : message;
    }
}
