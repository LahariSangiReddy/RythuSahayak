package com.rythusahayak.dao;

import com.rythusahayak.model.User;
import com.rythusahayak.model.FarmerProfile;
import com.rythusahayak.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {
    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = """
                SELECT id, full_name, email, mobile, password_hash, role, created_at, is_active
                FROM users
                WHERE email = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapUser(result)) : Optional.empty();
            }
        }
    }

    public Optional<FarmerProfile> findFarmerProfile(long userId) throws SQLException {
        String sql = "SELECT id, full_name, email, mobile FROM users WHERE id = ? AND role = 'FARMER'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(new FarmerProfile(
                        result.getLong("id"),
                        result.getString("full_name"),
                        result.getString("email"),
                        result.getString("mobile")));
            }
        }
    }

    public long createUser(String fullName, String email, String mobile,
                           String passwordHash, User.Role role) throws SQLException {
        String sql = """
                INSERT INTO users (full_name, email, mobile, password_hash, role)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, email);
            if (mobile == null || mobile.isBlank()) {
                statement.setNull(3, java.sql.Types.VARCHAR);
            } else {
                statement.setString(3, mobile);
            }
            statement.setString(4, passwordHash);
            statement.setString(5, role.name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Database did not return the new user ID.");
                }
                return keys.getLong(1);
            }
        }
    }

    public boolean hasAdministrator() throws SQLException {
        String sql = "SELECT 1 FROM users WHERE role = 'ADMIN' LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            return result.next();
        }
    }

    private User mapUser(ResultSet result) throws SQLException {
        return new User(
                result.getLong("id"),
                result.getString("full_name"),
                result.getString("email"),
                result.getString("mobile"),
                result.getString("password_hash"),
                User.Role.valueOf(result.getString("role")),
                result.getTimestamp("created_at").toInstant(),
                result.getBoolean("is_active"));
    }
}
