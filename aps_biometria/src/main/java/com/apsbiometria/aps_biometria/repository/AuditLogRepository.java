package com.apsbiometria.aps_biometria.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.apsbiometria.aps_biometria.database.DatabaseConnection;
import com.apsbiometria.aps_biometria.model.AccessLevel;
import com.apsbiometria.aps_biometria.model.AuditLog;
import com.apsbiometria.aps_biometria.model.AuditLog.ActionType;

public class AuditLogRepository {

    private final DatabaseConnection dbConnection;

    public AuditLogRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public AuditLog create(AuditLog log) throws SQLException {
        if (log.getId() == null) {
            log.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO audit_logs (id, user_id, user_name, action_type, access_level, " +
                "success, ip_address, description, timestamp, biometric_score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, log.getId());
            stmt.setString(2, log.getUserId());
            stmt.setString(3, log.getUserName());
            stmt.setString(4, log.getActionType().name());

            if (log.getAccessLevel() != null) {
                stmt.setInt(5, log.getAccessLevel().getLevel());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setBoolean(6, log.isSuccess());
            stmt.setString(7, log.getIpAddress());
            stmt.setString(8, log.getDescription());
            stmt.setTimestamp(9, new Timestamp(log.getTimestamp().getTime()));
            stmt.setDouble(10, log.getBiometricScore());

            stmt.executeUpdate();
            System.out.println("✓ Log de auditoria criado: " + log.getActionType());

            return log;
        }
    }

    public AuditLog findById(String id) throws SQLException {
        String sql = "SELECT * FROM audit_logs WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAuditLog(rs);
            }

            return null;
        }
    }

    public List<AuditLog> findByUserId(String userId) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE user_id = ? ORDER BY timestamp DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public List<AuditLog> findByActionType(ActionType actionType) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE action_type = ? ORDER BY timestamp DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, actionType.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public List<AuditLog> findByDateRange(Date startDate, Date endDate) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs " +
                "WHERE timestamp BETWEEN ? AND ? " +
                "ORDER BY timestamp DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public List<AuditLog> findFailedAttempts() throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs WHERE success = FALSE ORDER BY timestamp DESC";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public List<AuditLog> findFailedAttemptsByUser(String userId) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs " +
                "WHERE user_id = ? AND success = FALSE " +
                "ORDER BY timestamp DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public List<AuditLog> findRecent(int limit) throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public List<AuditLog> findAll() throws SQLException {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        }

        return logs;
    }

    public int countRecentFailedAttempts(String userId, int minutes) throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_logs " +
                "WHERE user_id = ? " +
                "AND success = FALSE " +
                "AND timestamp > DATEADD('MINUTE', ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setInt(2, -minutes);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    public String getAccessStatistics() throws SQLException {
        StringBuilder stats = new StringBuilder();
        stats.append("=== ESTATÍSTICAS DE ACESSO ===\n");

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Total de logs
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM audit_logs");
            if (rs.next()) {
                stats.append("Total de Logs: ").append(rs.getInt(1)).append("\n");
            }

            // Sucessos vs Falhas
            rs = stmt.executeQuery("SELECT success, COUNT(*) FROM audit_logs GROUP BY success");
            while (rs.next()) {
                String status = rs.getBoolean(1) ? "Sucessos" : "Falhas";
                stats.append(status).append(": ").append(rs.getInt(2)).append("\n");
            }

            // Por tipo de ação
            stats.append("\nPor Tipo de Ação:\n");
            rs = stmt.executeQuery(
                    "SELECT action_type, COUNT(*) as total " +
                            "FROM audit_logs " +
                            "GROUP BY action_type " +
                            "ORDER BY total DESC");
            while (rs.next()) {
                stats.append("  ").append(rs.getString(1))
                        .append(": ").append(rs.getInt(2)).append("\n");
            }

            // Score biométrico médio
            rs = stmt.executeQuery(
                    "SELECT AVG(biometric_score) " +
                            "FROM audit_logs " +
                            "WHERE biometric_score > 0");
            if (rs.next()) {
                stats.append("\nScore Biométrico Médio: ")
                        .append(String.format("%.2f%%", rs.getDouble(1))).append("\n");
            }
        }

        stats.append("==============================\n");
        return stats.toString();
    }

    public int deleteOlderThan(int days) throws SQLException {
        String sql = "DELETE FROM audit_logs WHERE timestamp < DATEADD('DAY', ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, -days);
            int deleted = stmt.executeUpdate();

            System.out.println("✓ Logs removidos: " + deleted);
            return deleted;
        }
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getString("id"));
        log.setUserId(rs.getString("user_id"));
        log.setUserName(rs.getString("user_name"));
        log.setActionType(ActionType.valueOf(rs.getString("action_type")));

        int accessLevelInt = rs.getInt("access_level");
        if (!rs.wasNull()) {
            log.setAccessLevel(AccessLevel.fromLevel(accessLevelInt));
        }

        log.setSuccess(rs.getBoolean("success"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setDescription(rs.getString("description"));
        log.setTimestamp(rs.getTimestamp("timestamp"));
        log.setBiometricScore(rs.getDouble("biometric_score"));

        return log;
    }
}