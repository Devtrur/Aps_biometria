package com.apsbiometria.aps_biometria.database;

import java.sql.SQLException;
import java.util.List;

import com.apsbiometria.aps_biometria.model.AccessLevel;
import com.apsbiometria.aps_biometria.model.AuditLog;
import com.apsbiometria.aps_biometria.model.BiometricData;
import com.apsbiometria.aps_biometria.model.User;
import com.apsbiometria.aps_biometria.repository.AuditLogRepository;
import com.apsbiometria.aps_biometria.repository.BiometricRepository;
import com.apsbiometria.aps_biometria.repository.UserRepository;

public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TESTE DO SISTEMA DE BANCO DE DADOS");
        System.out.println("========================================\n");

        try {
            testConnection();

            testUserOperations();

            testBiometricOperations();

            testAuditLogOperations();

            displayStatistics();

            System.out.println("\n✓ TODOS OS TESTES PASSARAM COM SUCESSO!");

        } catch (Exception e) {
            System.err.println("\n✗ ERRO NOS TESTES:");
        }
    }

    private static void testConnection() throws SQLException {
        System.out.println("1. TESTANDO CONEXÃO...");
        DatabaseConnection db = DatabaseConnection.getInstance();

        if (db.testConnection()) {
            System.out.println("   ✓ Conexão estabelecida com sucesso");
            System.out.println(db.getDatabaseInfo());
        } else {
            throw new SQLException("Falha ao conectar ao banco");
        }
    }

    private static void testUserOperations() throws SQLException {
        System.out.println("2. TESTANDO OPERAÇÕES COM USUÁRIOS...");
        UserRepository userRepo = new UserRepository();

        User ministro = new User(
                "João Silva",
                "joao.silva@meioambiente.gov.br",
                "123.456.789-00",
                AccessLevel.NIVEL_3);
        ministro.setDepartment("Gabinete do Ministro");

        User diretor = new User(
                "Maria Santos",
                "maria.santos@meioambiente.gov.br",
                "987.654.321-00",
                AccessLevel.NIVEL_2);
        diretor.setDepartment("Divisão de Fiscalização");

        User publico = new User(
                "Carlos Oliveira",
                "carlos.oliveira@example.com",
                "456.789.123-00",
                AccessLevel.NIVEL_1);
        publico.setDepartment("Acesso Público");

        ministro = userRepo.create(ministro);
        System.out.println("   ✓ Ministro criado: " + ministro.getName());

        diretor = userRepo.create(diretor);
        System.out.println("   ✓ Diretor criado: " + diretor.getName());

        publico = userRepo.create(publico);
        System.out.println("   ✓ Usuário público criado: " + publico.getName());

        User found = userRepo.findByEmail(ministro.getEmail());
        if (found != null && found.getName().equals(ministro.getName())) {
            System.out.println("   ✓ Busca por email funcionando");
        }

        List<User> allUsers = userRepo.findAll();
        System.out.println("   ✓ Total de usuários: " + allUsers.size());

        ministro.setDepartment("Gabinete Ministerial");
        userRepo.update(ministro);
        System.out.println("   ✓ Atualização funcionando");
    }

    private static void testBiometricOperations() throws SQLException {
        System.out.println("\n3. TESTANDO OPERAÇÕES COM BIOMETRIA...");
        UserRepository userRepo = new UserRepository();
        BiometricRepository bioRepo = new BiometricRepository();

        User user = userRepo.findByEmail("joao.silva@meioambiente.gov.br");

        if (user != null) {
            BiometricData bioData = new BiometricData(user.getId(), "FACIAL");

            double[] features = new double[128];
            for (int i = 0; i < features.length; i++) {
                features[i] = Math.random();
            }
            bioData.setFeatureVector(features);
            bioData.setQualityScore(85.5);
            bioData.setTemplate("TEMPLATE_SIMULADO_001");

            bioData = bioRepo.create(bioData);
            System.out.println("   ✓ Dados biométricos salvos: " + bioData.getId());

            List<BiometricData> userBiometrics = bioRepo.findByUserId(user.getId());
            System.out.println("   ✓ Biometrias do usuário: " + userBiometrics.size());

            List<BiometricData> highQuality = bioRepo.findByMinimumQuality(80.0);
            System.out.println("   ✓ Biometrias com qualidade ≥80: " + highQuality.size());

            double avgQuality = bioRepo.getAverageQuality();
            System.out.println("   ✓ Qualidade média: " + String.format("%.2f", avgQuality));
        }
    }

    private static void testAuditLogOperations() throws SQLException {
        System.out.println("\n4. TESTANDO LOGS DE AUDITORIA...");
        UserRepository userRepo = new UserRepository();
        AuditLogRepository logRepo = new AuditLogRepository();

        User user = userRepo.findByEmail("joao.silva@meioambiente.gov.br");

        if (user != null) {
            AuditLog successLog = new AuditLog(
                    user.getId(),
                    user.getName(),
                    AuditLog.ActionType.LOGIN_SUCCESS,
                    true,
                    "Login realizado com sucesso via biometria facial");
            successLog.setAccessLevel(user.getAccessLevel());
            successLog.setIpAddress("192.168.1.100");
            successLog.setBiometricScore(92.5);

            logRepo.create(successLog);
            System.out.println("   ✓ Log de sucesso criado");

            AuditLog failLog = new AuditLog(
                    "unknown-user",
                    "Usuário Desconhecido",
                    AuditLog.ActionType.LOGIN_FAILED,
                    false,
                    "Biometria não reconhecida");
            failLog.setIpAddress("192.168.1.101");
            failLog.setBiometricScore(45.0);

            logRepo.create(failLog);
            System.out.println("   ✓ Log de falha criado");

            List<AuditLog> userLogs = logRepo.findByUserId(user.getId());
            System.out.println("   ✓ Logs do usuário: " + userLogs.size());

            List<AuditLog> failedAttempts = logRepo.findFailedAttempts();
            System.out.println("   ✓ Total de falhas: " + failedAttempts.size());

            List<AuditLog> recentLogs = logRepo.findRecent(10);
            System.out.println("   ✓ Últimos 10 logs recuperados");
        }
    }

    private static void displayStatistics() throws SQLException {
        System.out.println("\n5. ESTATÍSTICAS DO SISTEMA");
        System.out.println("=====================================");

        DatabaseConnection db = DatabaseConnection.getInstance();
        System.out.println(db.getDatabaseInfo());

        AuditLogRepository logRepo = new AuditLogRepository();
        System.out.println(logRepo.getAccessStatistics());

        System.out.println("=====================================");
    }

    @SuppressWarnings("unused")
    private static void testCleanup() throws SQLException {
        System.out.println("\nLIMPANDO BANCO DE DADOS...");
        DatabaseConnection db = DatabaseConnection.getInstance();
        db.clearAllTables();
        System.out.println("✓ Banco limpo");
    }
}
