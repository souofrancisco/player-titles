package dev.souofrancisco.playertitles.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.executors.CommandExecutor;
import dev.souofrancisco.playertitles.config.PluginConfig;
import dev.souofrancisco.playertitles.config.section.TitleConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminGiveMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminRevokeMessagesConfig;
import dev.souofrancisco.playertitles.internal.PlayerTitlesController;
import dev.souofrancisco.playertitles.result.TitleRevokeResult;
import dev.souofrancisco.playertitles.result.TitleUnlockResult;
import dev.souofrancisco.playertitles.text.PlayerTitlesTextRenderer;
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
 * Administrative title ownership operations.
 */
@RequiredArgsConstructor
public final class TitlesAdminCommand {

    public static final @NotNull String NAME = "titlesadmin";

    private static final @NotNull String GIVE_PERMISSION = "playertitles.admin.give";
    private static final @NotNull String REVOKE_PERMISSION = "playertitles.admin.revoke";

    private final @NotNull PlayerTitlesController controller;
    private final @NotNull PluginConfig pluginConfig;

    private final @NotNull PlayerTitlesTextRenderer textRenderer;

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
                .register(plugin);
    }

    private @NotNull Argument<String> titleArgument() {
        return new StringArgument("title")
                .replaceSuggestions(ArgumentSuggestions.strings(info ->
                        pluginConfig.titles().keySet().toArray(String[]::new)
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

    private @NotNull String giveMessage(@NotNull TitleUnlockResult result) {
        AdminGiveMessagesConfig messages = pluginConfig.messages().admin().give();
        return switch (result) {
            case UNLOCKED -> messages.success();
            case ALREADY_UNLOCKED -> messages.alreadyUnlocked();
            case TITLE_NOT_FOUND -> messages.titleNotFound();
            case PLAYER_NOT_LOADED -> messages.playerNotLoaded();
        };
    }

    private @NotNull String revokeMessage(@NotNull TitleRevokeResult result) {
        AdminRevokeMessagesConfig messages = pluginConfig.messages().admin().revoke();
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
        TitleConfig title = pluginConfig.titles().get(titleId);
        return titleName(player, title, titleId);
    }

    private @NotNull Component titleName(
            @NotNull Player player,
            @Nullable TitleConfig title,
            @NotNull String titleId
    ) {
        return title == null ? Component.text(titleId) : textRenderer.render(player, title.displayName());
    }
}
