package com.apsbiometria.aps_biometria.authentication;

import java.awt.image.BufferedImage;
import java.sql.SQLException;

import com.apsbiometria.aps_biometria.model.AccessLevel;
import com.apsbiometria.aps_biometria.model.AuditLog;
import com.apsbiometria.aps_biometria.model.User;
import com.apsbiometria.aps_biometria.repository.AuditLogRepository;
import com.apsbiometria.aps_biometria.repository.UserRepository;

public class AuthenticationService {

    private final BiometricAuthenticator bioAuth;
    private final SessionManager sessionManager;
    private final AccessController accessController;
    private final UserRepository userRepo;
    private final AuditLogRepository auditRepo;

    public AuthenticationService() {
        this.bioAuth = new BiometricAuthenticator();
        this.sessionManager = SessionManager.getInstance();
        this.accessController = new AccessController();
        this.userRepo = new UserRepository();
        this.auditRepo = new AuditLogRepository();
        sessionManager.startAutoCleanup();
    }

    public Session login(String userId, BufferedImage biometricImage, String ipAddress) {
        AuditLog.ActionType actionType = AuditLog.ActionType.LOGIN_ATTEMPT;
        try {
            BiometricAuthenticator.AuthenticationResult authResult = bioAuth.authenticateUser(userId, biometricImage);
            if (authResult.isAuthenticated()) {
                User user = authResult.getUser();
                Session session = sessionManager.createSession(
                        user, ipAddress, authResult.getScore());
                userRepo.resetFailedAttempts(userId);
                userRepo.updateLastAccess(userId);
                logAudit(user, AuditLog.ActionType.LOGIN_SUCCESS, true,
                        "Login bem-sucedido via biometria", ipAddress, authResult.getScore());
                System.out.println("✓ Login bem-sucedido: " + user.getName());
                return session;
            } else {
                if (authResult.getUser() != null) {
                    userRepo.incrementFailedAttempts(userId);
                    logAudit(authResult.getUser(), AuditLog.ActionType.LOGIN_FAILED, false,
                            authResult.getMessage(), ipAddress, authResult.getScore());
                }
                System.err.println("✗ Login falhou: " + authResult.getMessage());
                return null;
            }
        } catch (Exception e) {
            System.err.println("✗ Erro no login: " + e.getMessage());
            try {
                User user = userRepo.findById(userId);
                if (user != null) {
                    logAudit(user, AuditLog.ActionType.SYSTEM_ERROR, false,
                            "Erro no processo de login: " + e.getMessage(), ipAddress, 0.0);
                }
            } catch (SQLException ex) {
                System.err.println("Erro ao registrar log: " + ex.getMessage());
            }
            return null;
        }
    }

    public Session loginByIdentification(BufferedImage biometricImage, String ipAddress) {
        try {
            BiometricAuthenticator.AuthenticationResult authResult = bioAuth.identifyUser(biometricImage);
            if (authResult.isAuthenticated()) {
                User user = authResult.getUser();
                Session session = sessionManager.createSession(
                        user, ipAddress, authResult.getScore());
                userRepo.resetFailedAttempts(user.getId());
                userRepo.updateLastAccess(user.getId());
                logAudit(user, AuditLog.ActionType.LOGIN_SUCCESS, true,
                        "Login por identificação biométrica", ipAddress, authResult.getScore());
                System.out.println("✓ Usuário identificado e autenticado: " + user.getName());
                return session;
            } else {
                AuditLog log = new AuditLog();
                log.setUserId("unknown");
                log.setUserName("Desconhecido");
                log.setActionType(AuditLog.ActionType.LOGIN_FAILED);
                log.setSuccess(false);
                log.setDescription("Identificação biométrica falhou: " + authResult.getMessage());
                log.setIpAddress(ipAddress);
                log.setBiometricScore(authResult.getScore());
                auditRepo.create(log);
                System.err.println("✗ Identificação falhou: " + authResult.getMessage());
                return null;
            }
        } catch (Exception e) {
            System.err.println("✗ Erro na identificação: " + e.getMessage());
            return null;
        }
    }

    public boolean logout(String sessionId) {
        Session session = sessionManager.getSession(sessionId);
        if (session != null) {
            try {
                logAudit(session.getUser(), AuditLog.ActionType.LOGOUT, true,
                        "Logout realizado", session.getIpAddress(), session.getAuthenticationScore());
                sessionManager.removeSession(sessionId);
                System.out.println("✓ Logout realizado: " + session.getUser().getName());
                return true;
            } catch (SQLException e) {
                System.err.println("Erro ao registrar logout: " + e.getMessage());
            }
        }
        return false;
    }

    public boolean isSessionActive(String sessionId) {
        Session session = sessionManager.getSession(sessionId);
        return session != null && session.isActive();
    }

    public Session getSession(String sessionId) {
        return sessionManager.getSession(sessionId);
    }

    public boolean checkAndLogAccess(String sessionId, AccessLevel requiredLevel, String description) {
        AccessController.AccessResult result = accessController.checkAccess(sessionId, requiredLevel);
        Session session = sessionManager.getSession(sessionId);
        if (session != null) {
            try {
                AuditLog.ActionType actionType = result.isGranted() ? AuditLog.ActionType.ACCESS_GRANTED
                        : AuditLog.ActionType.ACCESS_DENIED;
                logAudit(session.getUser(), actionType, result.isGranted(),
                        description + " | " + result.getMessage(),
                        session.getIpAddress(), session.getAuthenticationScore());
            } catch (SQLException e) {
                System.err.println("Erro ao registrar acesso: " + e.getMessage());
            }
        }
        return result.isGranted();
    }

    public String getPublicData(String sessionId) {
        checkAndLogAccess(sessionId, AccessLevel.NIVEL_1, "Acesso a dados públicos");
        return accessController.getPublicData(sessionId);
    }

    public String getDirectorData(String sessionId) {
        checkAndLogAccess(sessionId, AccessLevel.NIVEL_2, "Acesso a dados de diretores");
        return accessController.getDirectorData(sessionId);
    }

    public String getMinisterData(String sessionId) {
        checkAndLogAccess(sessionId, AccessLevel.NIVEL_3, "Acesso a dados confidenciais");
        return accessController.getMinisterData(sessionId);
    }

    private void logAudit(User user, AuditLog.ActionType actionType, boolean success,
            String description, String ipAddress, double biometricScore)
            throws SQLException {
        AuditLog log = new AuditLog(user.getId(), user.getName(), actionType, success, description);
        log.setAccessLevel(user.getAccessLevel());
        log.setIpAddress(ipAddress);
        log.setBiometricScore(biometricScore * 100);
        auditRepo.create(log);
    }

    public String getSessionReport() {
        return sessionManager.getSessionReport();
    }

    public String getAccessStatistics() throws SQLException {
        return auditRepo.getAccessStatistics();
    }

    public boolean forceLogout(String userId) {
        Session session = sessionManager.getUserSession(userId);
        if (session != null) {
            try {
                logAudit(session.getUser(), AuditLog.ActionType.LOGOUT, true,
                        "Logout forçado pelo sistema", session.getIpAddress(),
                        session.getAuthenticationScore());
            } catch (SQLException e) {
                System.err.println("Erro ao registrar logout forçado: " + e.getMessage());
            }
            sessionManager.removeUserSession(userId);
            return true;
        }
        return false;
    }

    public void cleanExpiredSessions() {
        sessionManager.cleanExpiredSessions();
    }

    public int getActiveSessionCount() {
        return sessionManager.getActiveSessionCount();
    }
}