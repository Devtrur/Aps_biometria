package com.apsbiometria.aps_biometria.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String H2_URL = "jdbc:h2:./data/biometric_db;AUTO_SERVER=TRUE";
    private static final String H2_USER = "sa";
    private static final String H2_PASSWORD = "";

    private static final String PG_URL = "jdbc:postgresql://localhost:5432/biometric_db";
    private static final String PG_USER = "postgres";
    private static final String PG_PASSWORD = "postgres";

    private static DatabaseConnection instance;
    private Connection connection;
    private DatabaseType currentDbType;

    public enum DatabaseType {
        H2, POSTGRESQL
    }

    private DatabaseConnection() {
        this.currentDbType = DatabaseType.H2; // Padrão H2
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    private void connect() throws SQLException {
        try {
            if (currentDbType == DatabaseType.H2) {
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
                System.out.println("✓ Conectado ao H2 Database");
            } else {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(PG_URL, PG_USER, PG_PASSWORD);
                System.out.println("✓ Conectado ao PostgreSQL Database");
            }

            initializeTables();

        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver do banco de dados não encontrado: " + e.getMessage());
        }
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL," +
                    "email VARCHAR(255) UNIQUE NOT NULL," +
                    "cpf VARCHAR(14) UNIQUE NOT NULL," +
                    "access_level INTEGER NOT NULL," +
                    "department VARCHAR(255)," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_access_date TIMESTAMP," +
                    "failed_attempts INTEGER DEFAULT 0," +
                    "locked BOOLEAN DEFAULT FALSE" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS biometric_data (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "user_id VARCHAR(36) NOT NULL," +
                    "biometric_type VARCHAR(50) NOT NULL," +
                    "feature_vector TEXT NOT NULL," +
                    "template TEXT," +
                    "quality_score DOUBLE NOT NULL," +
                    "capture_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "last_update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "active BOOLEAN DEFAULT TRUE," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_biometric_user " +
                    "ON biometric_data(user_id)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_biometric_type " +
                    "ON biometric_data(biometric_type)");

            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id VARCHAR(36) PRIMARY KEY," +
                    "user_id VARCHAR(36)," +
                    "user_name VARCHAR(255)," +
                    "action_type VARCHAR(50) NOT NULL," +
                    "access_level INTEGER," +
                    "success BOOLEAN NOT NULL," +
                    "ip_address VARCHAR(45)," +
                    "description TEXT," +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "biometric_score DOUBLE," +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                    ")");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_user " +
                    "ON audit_logs(user_id)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_timestamp " +
                    "ON audit_logs(timestamp)");

            stmt.execute("CREATE INDEX IF NOT EXISTS idx_audit_action " +
                    "ON audit_logs(action_type)");

            System.out.println("✓ Tabelas inicializadas com sucesso");
        }
    }

    public void switchDatabase(DatabaseType dbType) throws SQLException {
        this.currentDbType = dbType;
        closeConnection();
        connect();
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexão com banco fechada");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    public boolean testConnection() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            return false;
        }
    }

    public void clearAllTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM audit_logs");
            stmt.execute("DELETE FROM biometric_data");
            stmt.execute("DELETE FROM users");
            System.out.println("✓ Todas as tabelas foram limpas");
        }
    }

    public String getDatabaseInfo() throws SQLException {
        StringBuilder info = new StringBuilder();
        info.append("=== INFORMAÇÕES DO BANCO ===\n");
        info.append("Tipo: ").append(currentDbType).append("\n");
        info.append("Status: ").append(testConnection() ? "CONECTADO" : "DESCONECTADO").append("\n");

        if (testConnection()) {
            try (Statement stmt = connection.createStatement()) {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next()) {
                    info.append("Usuários: ").append(rs.getInt(1)).append("\n");
                }

                rs = stmt.executeQuery("SELECT COUNT(*) FROM biometric_data");
                if (rs.next()) {
                    info.append("Dados Biométricos: ").append(rs.getInt(1)).append("\n");
                }

                rs = stmt.executeQuery("SELECT COUNT(*) FROM audit_logs");
                if (rs.next()) {
                    info.append("Logs de Auditoria: ").append(rs.getInt(1)).append("\n");
                }
            }
        }
        info.append("===========================\n");
        return info.toString();
    }
}
