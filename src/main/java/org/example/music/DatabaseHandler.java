package org.example.music;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHandler {
    private static final String URL = "jdbc:postgresql://localhost:5432/music";
    private static final String USER = "soundwave_user";
    private static final String PASS = "user123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static boolean registerUser(String username, String password) {
        if (userExists(username)) return false;

        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, BCrypt.hashpw(password, BCrypt.gensalt()));
            ps.setString(3, "user");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    private static boolean userExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Ошибка проверки пользователя: " + e.getMessage());
            return true;
        }
    }

    public static boolean loginUser(String username, String password) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if (BCrypt.checkpw(password, rs.getString("password_hash"))) {
                        UserSession.login(rs.getInt("id"), rs.getString("username"), rs.getString("role"));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка входа: " + e.getMessage());
        }
        return false;
    }
}