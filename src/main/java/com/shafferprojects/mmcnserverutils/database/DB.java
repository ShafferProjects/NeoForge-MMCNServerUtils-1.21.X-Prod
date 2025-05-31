package com.shafferprojects.mmcnserverutils.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {

    private static final String HOST = "localhost";
    private static final int PORT = 3306;
    private static final String DATABASE = "minecraft";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static final String SERVER_NAME = "ATM10";

    public static Connection get() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%d/%s", HOST, PORT, DATABASE);
        return DriverManager.getConnection(url, USER, PASSWORD);
    }

    public static void validateConnectionOrExit() {
        try (Connection conn = get()) {
            if (conn == null || conn.isClosed()) {
                throw new SQLException("Connection is null or closed.");
            }
        } catch (SQLException e) {
            System.err.println("""
            [MMCN DB] ❌ Failed to connect to MySQL!
            Host: %s
            Port: %d
            Database: %s
            User: %s
            -------------------------------
            ERROR: %s
            Shutting down server...
            """.formatted(HOST, PORT, DATABASE, USER, e.getMessage()));
            System.exit(1); // Force server shutdown
        }
    }

}
