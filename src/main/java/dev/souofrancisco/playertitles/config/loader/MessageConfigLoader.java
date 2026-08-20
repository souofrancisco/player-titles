package dev.souofrancisco.playertitles.config.loader;

import dev.souofrancisco.playertitles.config.ConfigReader;
import dev.souofrancisco.playertitles.config.section.message.AdminGiveMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminReloadMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.AdminRevokeMessagesConfig;
import dev.souofrancisco.playertitles.config.section.message.PluginMessagesConfig;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Loads and validates configured command feedback templates.
 */
@UtilityClass
public final class MessageConfigLoader {

    public static @NotNull PluginMessagesConfig load(@NotNull ConfigReader configReader) {
        ConfigReader messagesReader = configReader.requireSection("messages");
        return new PluginMessagesConfig(loadAdmin(messagesReader.requireSection("admin")));
    }

    private static @NotNull AdminMessagesConfig loadAdmin(@NotNull ConfigReader adminReader) {
        return new AdminMessagesConfig(
                loadGive(adminReader.requireSection("give")),
                loadRevoke(adminReader.requireSection("revoke")),
                loadReload(adminReader.requireSection("reload"))
        );
    }

    private static @NotNull AdminGiveMessagesConfig loadGive(@NotNull ConfigReader giveReader) {
        return new AdminGiveMessagesConfig(
                giveReader.requireString("success"),
                giveReader.requireString("already-unlocked"),
                giveReader.requireString("title-not-found"),
                giveReader.requireString("player-not-loaded")
        );
    }

    private static @NotNull AdminRevokeMessagesConfig loadRevoke(@NotNull ConfigReader revokeReader) {
        return new AdminRevokeMessagesConfig(
                revokeReader.requireString("success"),
                revokeReader.requireString("not-unlocked"),
                revokeReader.requireString("title-not-found"),
                revokeReader.requireString("player-not-loaded")
        );
    }

    private static @NotNull AdminReloadMessagesConfig loadReload(@NotNull ConfigReader reloadReader) {
        return new AdminReloadMessagesConfig(
                reloadReader.requireString("success"),
                reloadReader.requireString("success-database-restart-required"),
                reloadReader.requireString("failure")
        );
    }
}
