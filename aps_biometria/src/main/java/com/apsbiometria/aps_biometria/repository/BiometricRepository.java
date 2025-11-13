package com.apsbiometria.aps_biometria.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.apsbiometria.aps_biometria.database.DatabaseConnection;
import com.apsbiometria.aps_biometria.model.BiometricData;

public class BiometricRepository {

    private final DatabaseConnection dbConnection;

    public BiometricRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public BiometricData create(BiometricData biometricData) throws SQLException {
        if (biometricData.getId() == null) {
            biometricData.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO biometric_data (id, user_id, biometric_type, feature_vector, " +
                "template, quality_score, capture_date, last_update_date, active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, biometricData.getId());
            stmt.setString(2, biometricData.getUserId());
            stmt.setString(3, biometricData.getBiometricType());
            stmt.setString(4, serializeFeatureVector(biometricData.getFeatureVector()));
            stmt.setString(5, biometricData.getTemplate());
            stmt.setDouble(6, biometricData.getQualityScore());
            stmt.setTimestamp(7, new Timestamp(biometricData.getCaptureDate().getTime()));
            stmt.setTimestamp(8, new Timestamp(biometricData.getLastUpdateDate().getTime()));
            stmt.setBoolean(9, biometricData.isActive());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Dados biométricos salvos: " + biometricData.getId());
                return biometricData;
            }

            throw new SQLException("Falha ao salvar dados biométricos");
        }
    }

    public BiometricData findById(String id) throws SQLException {
        String sql = "SELECT * FROM biometric_data WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBiometricData(rs);
            }

            return null;
        }
    }

    public List<BiometricData> findByUserId(String userId) throws SQLException {
        List<BiometricData> dataList = new ArrayList<>();
        String sql = "SELECT * FROM biometric_data WHERE user_id = ? AND active = TRUE";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataList.add(mapResultSetToBiometricData(rs));
            }
        }

        return dataList;
    }

    public List<BiometricData> findByType(String biometricType) throws SQLException {
        List<BiometricData> dataList = new ArrayList<>();
        String sql = "SELECT * FROM biometric_data WHERE biometric_type = ? AND active = TRUE";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, biometricType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataList.add(mapResultSetToBiometricData(rs));
            }
        }

        return dataList;
    }

    public BiometricData findByUserIdAndType(String userId, String biometricType) throws SQLException {
        String sql = "SELECT * FROM biometric_data " +
                "WHERE user_id = ? AND biometric_type = ? AND active = TRUE " +
                "ORDER BY capture_date DESC LIMIT 1";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            stmt.setString(2, biometricType);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBiometricData(rs);
            }

            return null;
        }
    }

    public List<BiometricData> findAll() throws SQLException {
        List<BiometricData> dataList = new ArrayList<>();
        String sql = "SELECT * FROM biometric_data WHERE active = TRUE ORDER BY capture_date DESC";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                dataList.add(mapResultSetToBiometricData(rs));
            }
        }

        return dataList;
    }

    public List<BiometricData> findByMinimumQuality(double minQuality) throws SQLException {
        List<BiometricData> dataList = new ArrayList<>();
        String sql = "SELECT * FROM biometric_data " +
                "WHERE quality_score >= ? AND active = TRUE " +
                "ORDER BY quality_score DESC";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, minQuality);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataList.add(mapResultSetToBiometricData(rs));
            }
        }

        return dataList;
    }

    public boolean update(BiometricData biometricData) throws SQLException {
        String sql = "UPDATE biometric_data SET " +
                "biometric_type = ?, feature_vector = ?, template = ?, " +
                "quality_score = ?, last_update_date = ?, active = ? " +
                "WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, biometricData.getBiometricType());
            stmt.setString(2, serializeFeatureVector(biometricData.getFeatureVector()));
            stmt.setString(3, biometricData.getTemplate());
            stmt.setDouble(4, biometricData.getQualityScore());
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            stmt.setBoolean(6, biometricData.isActive());
            stmt.setString(7, biometricData.getId());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Dados biométricos atualizados: " + biometricData.getId());
                return true;
            }

            return false;
        }
    }

    public boolean softDelete(String id) throws SQLException {
        String sql = "UPDATE biometric_data SET active = FALSE WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Dados biométricos desativados: " + id);
                return true;
            }

            return false;
        }
    }

    public boolean deleteByUserId(String userId) throws SQLException {
        String sql = "UPDATE biometric_data SET active = FALSE WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Dados biométricos do usuário desativados: " + userId);
                return true;
            }

            return false;
        }
    }

    public boolean hardDelete(String id) throws SQLException {
        String sql = "DELETE FROM biometric_data WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Dados biométricos removidos permanentemente: " + id);
                return true;
            }

            return false;
        }
    }

    public int countByUserId(String userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM biometric_data WHERE user_id = ? AND active = TRUE";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }

            return 0;
        }
    }

    public double getAverageQuality() throws SQLException {
        String sql = "SELECT AVG(quality_score) FROM biometric_data WHERE active = TRUE";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }

            return 0.0;
        }
    }

    private String serializeFeatureVector(double[] vector) {
        if (vector == null || vector.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private double[] deserializeFeatureVector(String serialized) {
        if (serialized == null || serialized.isEmpty()) {
            return new double[0];
        }

        String[] parts = serialized.split(",");
        double[] vector = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i].trim());
        }

        return vector;
    }

    private BiometricData mapResultSetToBiometricData(ResultSet rs) throws SQLException {
        BiometricData data = new BiometricData();
        data.setId(rs.getString("id"));
        data.setUserId(rs.getString("user_id"));
        data.setBiometricType(rs.getString("biometric_type"));
        data.setFeatureVector(deserializeFeatureVector(rs.getString("feature_vector")));
        data.setTemplate(rs.getString("template"));
        data.setQualityScore(rs.getDouble("quality_score"));
        data.setCaptureDate(rs.getTimestamp("capture_date"));
        data.setLastUpdateDate(rs.getTimestamp("last_update_date"));
        data.setActive(rs.getBoolean("active"));

        return data;
    }
}
