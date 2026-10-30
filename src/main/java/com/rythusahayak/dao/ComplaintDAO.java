package com.rythusahayak.dao;

import com.rythusahayak.model.Complaint;
import com.rythusahayak.model.ComplaintSummary;
import com.rythusahayak.model.PublicComplaint;
import com.rythusahayak.util.ComplaintReference;
import com.rythusahayak.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComplaintDAO {
    private static final int REFERENCE_ATTEMPTS = 5;

    public Complaint createComplaint(String farmerName, String mobile, String village, String district,
                                     String crop, String category, String description,
                                     LocalDate observedDate, Long userId) throws SQLException {
        String sql = """
                INSERT INTO complaints
                    (reference_number, user_id, farmer_name, mobile, village, district,
                     crop, category, description, observed_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
                """;
        for (int attempt = 0; attempt < REFERENCE_ATTEMPTS; attempt++) {
            String reference = ComplaintReference.generate();
            try (Connection connection = DBConnection.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    long complaintId;
                    try (PreparedStatement statement =
                                 connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        statement.setString(1, reference);
                        if (userId == null) {
                            statement.setNull(2, Types.BIGINT);
                        } else {
                            statement.setLong(2, userId);
                        }
                        statement.setString(3, farmerName);
                        statement.setString(4, mobile);
                        statement.setString(5, village);
                        statement.setString(6, district);
                        statement.setString(7, crop);
                        statement.setString(8, category);
                        statement.setString(9, description);
                        statement.setObject(10, observedDate);
                        statement.executeUpdate();
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("Database did not return the new complaint ID.");
                            }
                            complaintId = keys.getLong(1);
                        }
                    }
                    Complaint created = findById(connection, complaintId)
                            .orElseThrow(() -> new SQLException("Created complaint could not be reloaded."));
                    connection.commit();
                    return created;
                } catch (SQLException exception) {
                    try {
                        connection.rollback();
                    } catch (SQLException rollbackException) {
                        exception.addSuppressed(rollbackException);
                    }
                    throw exception;
                }
            } catch (SQLException exception) {
                if (exception.getErrorCode() != 1062 || attempt == REFERENCE_ATTEMPTS - 1) {
                    throw exception;
                }
            }
        }
        throw new SQLException("Unable to generate a unique complaint reference.");
    }

    public Optional<PublicComplaint> findPublicByReference(String reference) throws SQLException {
        String sql = """
                SELECT c.reference_number, c.crop, c.category, c.observed_date, c.created_at, c.status,
                       (SELECT u.public_remark
                        FROM complaint_updates u
                        WHERE u.complaint_id = c.id
                        ORDER BY u.created_at DESC, u.id DESC
                        LIMIT 1) AS public_remark
                FROM complaints c
                WHERE c.reference_number = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, reference);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                String remark = result.getString("public_remark");
                return Optional.of(new PublicComplaint(
                        result.getString("reference_number"),
                        result.getString("crop"),
                        result.getString("category"),
                        result.getDate("observed_date").toLocalDate(),
                        result.getTimestamp("created_at").toInstant(),
                        Complaint.Status.valueOf(result.getString("status")),
                        remark == null ? "Your report has been received and is awaiting review." : remark));
            }
        }
    }

    public Optional<Complaint> findById(long id) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findById(connection, id);
        }
    }

    public List<Complaint> findAll(Complaint.Status status, int limit, int offset) throws SQLException {
        String sql = status == null
                ? "SELECT * FROM complaints ORDER BY created_at DESC LIMIT ? OFFSET ?"
                : "SELECT * FROM complaints WHERE status = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        List<Complaint> complaints = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int parameter = 1;
            if (status != null) {
                statement.setString(parameter++, status.name());
            }
            statement.setInt(parameter++, Math.min(Math.max(limit, 1), 100));
            statement.setInt(parameter, Math.max(offset, 0));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    complaints.add(mapComplaint(result));
                }
            }
        }
        return complaints;
    }

    public List<Complaint> findByUserId(long userId, int limit) throws SQLException {
        String sql = "SELECT * FROM complaints WHERE user_id = ? ORDER BY created_at DESC LIMIT ?";
        List<Complaint> complaints = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setInt(2, Math.min(Math.max(limit, 1), 100));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    complaints.add(mapComplaint(result));
                }
            }
        }
        return complaints;
    }

    public ComplaintSummary summarizeByUserId(long userId) throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total,
                       COALESCE(SUM(status = 'PENDING'), 0) AS pending,
                       COALESCE(SUM(status IN ('UNDER_REVIEW', 'ACTION_RECORDED')), 0) AS in_progress,
                       COALESCE(SUM(status = 'RESOLVED'), 0) AS resolved
                FROM complaints
                WHERE user_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Complaint summary query returned no result.");
                }
                return new ComplaintSummary(
                        result.getLong("total"),
                        result.getLong("pending"),
                        result.getLong("in_progress"),
                        result.getLong("resolved"));
            }
        }
    }

    public ComplaintSummary summarizeAll() throws SQLException {
        String sql = """
                SELECT COUNT(*) AS total,
                       COALESCE(SUM(status = 'PENDING'), 0) AS pending,
                       COALESCE(SUM(status IN ('UNDER_REVIEW', 'ACTION_RECORDED')), 0) AS in_progress,
                       COALESCE(SUM(status = 'RESOLVED'), 0) AS resolved
                FROM complaints
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) {
                throw new SQLException("Complaint summary query returned no result.");
            }
            return new ComplaintSummary(
                    result.getLong("total"),
                    result.getLong("pending"),
                    result.getLong("in_progress"),
                    result.getLong("resolved"));
        }
    }

    public boolean updateStatus(long complaintId, long adminUserId, Complaint.Status newStatus,
                                String publicRemark) throws SQLException {
        String selectSql = "SELECT status FROM complaints WHERE id = ? FOR UPDATE";
        String updateSql = "UPDATE complaints SET status = ? WHERE id = ?";
        String historySql = """
                INSERT INTO complaint_updates
                    (complaint_id, admin_user_id, previous_status, new_status, public_remark)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Complaint.Status previousStatus;
                try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                    select.setLong(1, complaintId);
                    try (ResultSet result = select.executeQuery()) {
                        if (!result.next()) {
                            connection.rollback();
                            return false;
                        }
                        previousStatus = Complaint.Status.valueOf(result.getString("status"));
                    }
                }

                try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                    update.setString(1, newStatus.name());
                    update.setLong(2, complaintId);
                    update.executeUpdate();
                }
                try (PreparedStatement history = connection.prepareStatement(historySql)) {
                    history.setLong(1, complaintId);
                    history.setLong(2, adminUserId);
                    history.setString(3, previousStatus.name());
                    history.setString(4, newStatus.name());
                    history.setString(5, publicRemark);
                    history.executeUpdate();
                }
                connection.commit();
                return true;
            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
        }
    }

    private Optional<Complaint> findById(Connection connection, long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM complaints WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(mapComplaint(result)) : Optional.empty();
            }
        }
    }

    private Complaint mapComplaint(ResultSet result) throws SQLException {
        long userIdValue = result.getLong("user_id");
        Long userId = result.wasNull() ? null : userIdValue;
        Timestamp updatedAt = result.getTimestamp("updated_at");
        Instant updatedInstant = updatedAt == null ? null : updatedAt.toInstant();
        return new Complaint(
                result.getLong("id"),
                result.getString("reference_number"),
                userId,
                result.getString("farmer_name"),
                result.getString("mobile"),
                result.getString("village"),
                result.getString("district"),
                result.getString("crop"),
                result.getString("category"),
                result.getString("description"),
                result.getDate("observed_date").toLocalDate(),
                Complaint.Status.valueOf(result.getString("status")),
                result.getTimestamp("created_at").toInstant(),
                updatedInstant);
    }

}
