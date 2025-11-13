package com.apsbiometria.aps_biometria.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.apsbiometria.aps_biometria.model.User;

public class SessionManager {

    private static SessionManager instance;
    private final Map<String, Session> activeSessions;
    private final Map<String, Session> userSessions; // userId -> Session

    private SessionManager() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.userSessions = new ConcurrentHashMap<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public Session createSession(User user, String ipAddress, double authScore) {
        removeUserSession(user.getId());

        Session session = new Session(user, ipAddress, authScore);
        activeSessions.put(session.getSessionId(), session);
        userSessions.put(user.getId(), session);

        System.out.println("✓ Sessão criada: " + session.getSessionId() +
                " para usuário: " + user.getName());

        return session;
    }

    public Session getSession(String sessionId) {
        Session session = activeSessions.get(sessionId);

        if (session != null) {
            if (session.isExpired()) {
                removeSession(sessionId);
                return null;
            }

            session.updateActivity();
        }

        return session;
    }

    public Session getUserSession(String userId) {
        Session session = userSessions.get(userId);

        if (session != null && session.isExpired()) {
            removeUserSession(userId);
            return null;
        }

        return session;
    }

    public boolean hasActiveSession(String userId) {
        Session session = getUserSession(userId);
        return session != null && session.isActive();
    }

    public void removeSession(String sessionId) {
        Session session = activeSessions.remove(sessionId);

        if (session != null) {
            session.invalidate();
            userSessions.remove(session.getUser().getId());
            System.out.println("✓ Sessão removida: " + sessionId);
        }
    }

    public void removeUserSession(String userId) {
        Session session = userSessions.remove(userId);

        if (session != null) {
            session.invalidate();
            activeSessions.remove(session.getSessionId());
            System.out.println("✓ Sessão do usuário removida: " + userId);
        }
    }

    public List<Session> getActiveSessions() {
        cleanExpiredSessions();
        return new ArrayList<>(activeSessions.values());
    }

    public int getActiveSessionCount() {
        cleanExpiredSessions();
        return activeSessions.size();
    }

    public void cleanExpiredSessions() {
        List<String> expiredSessions = new ArrayList<>();

        for (Map.Entry<String, Session> entry : activeSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredSessions.add(entry.getKey());
            }
        }

        for (String sessionId : expiredSessions) {
            removeSession(sessionId);
        }

        if (!expiredSessions.isEmpty()) {
            System.out.println("✓ Limpeza: " + expiredSessions.size() + " sessões expiradas removidas");
        }
    }

    public void clearAllSessions() {
        for (Session session : activeSessions.values()) {
            session.invalidate();
        }
        activeSessions.clear();
        userSessions.clear();
        System.out.println("✓ Todas as sessões foram removidas");
    }

    public String getSessionReport() {
        cleanExpiredSessions();

        StringBuilder report = new StringBuilder();
        report.append("===== RELATÓRIO DE SESSÕES =====\n");
        report.append("Total de Sessões Ativas: ").append(activeSessions.size()).append("\n\n");

        for (Session session : activeSessions.values()) {
            report.append("Sessão ID: ").append(session.getSessionId()).append("\n");
            report.append("  Usuário: ").append(session.getUser().getName()).append("\n");
            report.append("  Nível: ").append(session.getUser().getAccessLevel().toDisplayString()).append("\n");
            report.append("  IP: ").append(session.getIpAddress()).append("\n");
            report.append("  Login: ").append(session.getLoginTime()).append("\n");
            report.append("  Duração: ").append(session.getSessionDurationMinutes()).append(" min\n");
            report.append("  Score Auth: ").append(String.format("%.2f%%", session.getAuthenticationScore()))
                    .append("\n");
            report.append("  Status: ").append(session.isActive() ? "ATIVA" : "INATIVA").append("\n\n");
        }

        report.append("================================\n");
        return report.toString();
    }

    public void startAutoCleanup() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5 * 60 * 1000);
                    cleanExpiredSessions();
                } catch (InterruptedException e) {
                    System.err.println("Thread de limpeza interrompida");
                    break;
                }
            }
        });

        cleanupThread.setDaemon(true);
        cleanupThread.setName("SessionCleanup");
        cleanupThread.start();

        System.out.println("✓ Auto-limpeza de sessões iniciada");
    }
}