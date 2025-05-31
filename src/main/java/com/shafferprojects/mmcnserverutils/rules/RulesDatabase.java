package com.shafferprojects.mmcnserverutils.rules;

import com.shafferprojects.mmcnserverutils.database.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RulesDatabase {

    public static void initTable() {
        try (Connection conn = DB.get()) {
            PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS rules_agreed (
                    uuid CHAR(36) PRIMARY KEY,
                    agreed_at BIGINT NOT NULL
                )
            """);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL] Failed to create rules_agreed table:");
            e.printStackTrace();
        }
    }

    public static boolean hasAgreed(String uuid) {
        try (Connection conn = DB.get()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM rules_agreed WHERE uuid = ?");
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[MySQL] Failed to check rules agreement:");
            e.printStackTrace();
            return false;
        }
    }

    public static void markAgreed(String uuid) {
        try (Connection conn = DB.get()) {
            PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO rules_agreed (uuid, agreed_at)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE agreed_at = VALUES(agreed_at)
            """);
            stmt.setString(1, uuid);
            stmt.setLong(2, System.currentTimeMillis() / 1000L);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[MySQL] Failed to mark rules as agreed:");
            e.printStackTrace();
        }
    }

    public static void deleteAgreement(String uuid, String warnedBy) {
        try (Connection conn = DB.get()) {
            conn.setAutoCommit(false);

            // Step 1: Delete from rules_agreed
            PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM rules_agreed WHERE uuid = ?");
            deleteStmt.setString(1, uuid);
            deleteStmt.executeUpdate();

            // Step 2: Insert warning
            PreparedStatement insertWarning = conn.prepareStatement("""
                INSERT INTO warnings (user, warnedBy, issuedAt, reason)
                VALUES (?, ?, ?, ?)
            """);
            insertWarning.setString(1, uuid);
            insertWarning.setString(2, warnedBy);
            insertWarning.setString(3, getCurrentTimestamp());
            insertWarning.setString(4, "Shown rules due to behavior observed in " + DB.SERVER_NAME + ".");
            insertWarning.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            System.err.println("[MySQL] Failed to delete rules agreement or log warning:");
            e.printStackTrace();
        }
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
