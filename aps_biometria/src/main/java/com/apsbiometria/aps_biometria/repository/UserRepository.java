package com.apsbiometria.aps_biometria.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.apsbiometria.aps_biometria.database.DatabaseConnection;
import com.apsbiometria.aps_biometria.model.AccessLevel;
import com.apsbiometria.aps_biometria.model.User;

public class UserRepository {

    private final DatabaseConnection dbConnection;

    public UserRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public User create(User user) throws SQLException {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID().toString());
        }

        String sql = "INSERT INTO users (id, name, email, cpf, access_level, department, " +
                "active, registration_date, failed_attempts, locked) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getId());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getCpf());
            stmt.setInt(5, user.getAccessLevel().getLevel());
            stmt.setString(6, user.getDepartment());
            stmt.setBoolean(7, user.isActive());
            stmt.setTimestamp(8, new Timestamp(user.getRegistrationDate().getTime()));
            stmt.setInt(9, user.getFailedAttempts());
            stmt.setBoolean(10, user.isLocked());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Usuário criado: " + user.getName());
                return user;
            }

            throw new SQLException("Falha ao criar usuário");
        }
    }

    public User findById(String id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

            return null;
        }
    }

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

            return null;
        }
    }

    public User findByCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM users WHERE cpf = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }

            return null;
        }
    }

    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY name";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    public List<User> findByAccessLevel(AccessLevel accessLevel) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE access_level = ? ORDER BY name";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accessLevel.getLevel());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    public List<User> findActive() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE active = TRUE ORDER BY name";

        try (Connection conn = dbConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }

        return users;
    }

    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET " +
                "name = ?, email = ?, cpf = ?, access_level = ?, " +
                "department = ?, active = ?, last_access_date = ?, " +
                "failed_attempts = ?, locked = ? " +
                "WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getCpf());
            stmt.setInt(4, user.getAccessLevel().getLevel());
            stmt.setString(5, user.getDepartment());
            stmt.setBoolean(6, user.isActive());

            if (user.getLastAccessDate() != null) {
                stmt.setTimestamp(7, new Timestamp(user.getLastAccessDate().getTime()));
            } else {
                stmt.setNull(7, Types.TIMESTAMP);
            }

            stmt.setInt(8, user.getFailedAttempts());
            stmt.setBoolean(9, user.isLocked());
            stmt.setString(10, user.getId());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Usuário atualizado: " + user.getName());
                return true;
            }

            return false;
        }
    }

    public boolean updateLastAccess(String userId) throws SQLException {
        String sql = "UPDATE users SET last_access_date = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean incrementFailedAttempts(String userId) throws SQLException {
        String sql = "UPDATE users " +
                "SET failed_attempts = failed_attempts + 1, " +
                "locked = CASE WHEN failed_attempts >= 2 THEN TRUE ELSE locked END " +
                "WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean resetFailedAttempts(String userId) throws SQLException {
        String sql = "UPDATE users SET failed_attempts = 0, locked = FALSE WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean softDelete(String id) throws SQLException {
        String sql = "UPDATE users SET active = FALSE WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Usuário desativado: " + id);
                return true;
            }

            return false;
        }
    }

    public boolean hardDelete(String id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            int rows = stmt.executeUpdate();

            if (rows > 0) {
                System.out.println("✓ Usuário removido permanentemente: " + id);
                return true;
            }

            return false;
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;
        }
    }

    public boolean cpfExists(String cpf) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE cpf = ?";

        try (Connection conn = dbConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cpf);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

            return false;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setCpf(rs.getString("cpf"));
        user.setAccessLevel(AccessLevel.fromLevel(rs.getInt("access_level")));
        user.setDepartment(rs.getString("department"));
        user.setActive(rs.getBoolean("active"));
        user.setRegistrationDate(rs.getTimestamp("registration_date"));

        Timestamp lastAccess = rs.getTimestamp("last_access_date");
        if (lastAccess != null) {
            user.setLastAccessDate(lastAccess);
        }

        user.setFailedAttempts(rs.getInt("failed_attempts"));
        user.setLocked(rs.getBoolean("locked"));

        return user;
    }
}