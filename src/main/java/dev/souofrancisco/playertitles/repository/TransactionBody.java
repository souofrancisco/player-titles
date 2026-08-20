package dev.souofrancisco.playertitles.repository;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface TransactionBody {

    void run(@NotNull Connection connection) throws SQLException;
}
