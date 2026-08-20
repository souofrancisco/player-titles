package dev.souofrancisco.playertitles.config.loader;

import dev.souofrancisco.playertitles.config.ConfigReader;
import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.repository.database.DatabaseType;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * Loads and validates the database section of the plugin YAML.
 */
@UtilityClass
public final class DatabaseConfigLoader {

    private static final Pattern SQLITE_FILE_PATTERN = Pattern.compile("[A-Za-z0-9_.-]+\\.db");
    private static final Pattern MYSQL_NAME_PATTERN = Pattern.compile("[A-Za-z0-9_-]+");
    private static final Pattern MYSQL_HOST_PATTERN = Pattern.compile("[A-Za-z0-9_.-]+");

    public static @NotNull DatabaseConfig load(@NotNull ConfigReader root) {
        DatabaseConfig defaults = DatabaseConfig.sqliteDefaults();
        if (!root.isSet("database")) return defaults;

        ConfigReader databaseReader = root.requireSection("database");
        DatabaseType type = loadType(databaseReader, defaults);

        String sqliteFile = databaseReader.optionalStringIn("sqlite", "file", defaults.sqliteFile());
        if (!SQLITE_FILE_PATTERN.matcher(sqliteFile).matches()) {
            throw databaseReader.invalid("sqlite.file", "file name must stay local and end with .db");
        }

        String host = databaseReader.optionalStringIn("mysql", "host", defaults.host());
        if (!MYSQL_HOST_PATTERN.matcher(host).matches()) {
            throw databaseReader.invalid("mysql.host", "host must contain only letters, numbers, dots, underscores, or hyphens");
        }

        int port = databaseReader.optionalIntIn("mysql", "port", defaults.port(), 1, 65535);
        String database = databaseReader.optionalStringIn("mysql", "database", defaults.database());
        if (!MYSQL_NAME_PATTERN.matcher(database).matches()) {
            throw databaseReader.invalid("mysql.database", "database name must contain only letters, numbers, underscores, or hyphens");
        }

        String username = databaseReader.optionalStringIn("mysql", "username", defaults.username());
        if (!MYSQL_NAME_PATTERN.matcher(username).matches()) {
            throw databaseReader.invalid("mysql.username", "username must contain only letters, numbers, underscores, or hyphens");
        }

        String password = databaseReader.optionalStringIn("mysql", "password", defaults.password(), true);
        int maximumPoolSize = databaseReader.optionalIntIn(
                "mysql",
                "maximum-pool-size",
                defaults.maximumPoolSize(),
                1,
                32
        );

        return new DatabaseConfig(type, sqliteFile, host, port, database, username, password, maximumPoolSize);
    }

    private static @NotNull DatabaseType loadType(
            @NotNull ConfigReader databaseReader,
            @NotNull DatabaseConfig defaults
    ) {
        String rawType = databaseReader.optionalString("type", defaults.type().name());
        try {
            return DatabaseType.parse(rawType);
        } catch (IllegalArgumentException exception) {
            throw databaseReader.invalid("type", "unsupported database type '" + rawType + "'");
        }
    }

}
