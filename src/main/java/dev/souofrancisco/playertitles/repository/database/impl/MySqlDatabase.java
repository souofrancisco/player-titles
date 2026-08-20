package dev.souofrancisco.playertitles.repository.database.impl;

import com.zaxxer.hikari.HikariConfig;
import dev.souofrancisco.playertitles.config.section.DatabaseConfig;
import dev.souofrancisco.playertitles.repository.database.Database;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * MySQL Hikari configuration.
 */
@RequiredArgsConstructor
public final class MySqlDatabase extends Database {

    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private final @NotNull DatabaseConfig config;

    @Override
    protected @NotNull HikariConfig hikariConfig() {
        HikariConfig hikariConfig = baseHikariConfig();
        hikariConfig.setDriverClassName(DRIVER_CLASS);
        hikariConfig.setJdbcUrl("jdbc:mysql://" + config.host() + ":" + config.port() + "/" + config.database()
                + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC"
                + "&rewriteBatchedStatements=true");
        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());
        hikariConfig.setMaximumPoolSize(config.maximumPoolSize());
        return hikariConfig;
    }
}
