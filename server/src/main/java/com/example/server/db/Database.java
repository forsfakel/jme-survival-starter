package com.example.server.db;

import java.sql.*;

public class Database {
    private static Connection connection;

    public static void init() throws SQLException {
        if (connection != null) return;
        connection = DriverManager.getConnection("jdbc:sqlite:game.db");

        try (Statement st = connection.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS players (
                    id TEXT PRIMARY KEY,
                    name TEXT,
                    x INTEGER,
                    y INTEGER
                )
            """);
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}
