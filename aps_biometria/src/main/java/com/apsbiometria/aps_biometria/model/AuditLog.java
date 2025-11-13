package com.apsbiometria.aps_biometria.model;

import java.util.Date;

public class AuditLog {
    private String id;
    private String userId;
    private String userName;
    private ActionType actionType;
    private AccessLevel accessLevel;
    private boolean success;
    private String ipAddress;
    private String description;
    private Date timestamp;
    private double biometricScore;

    public enum ActionType {
        LOGIN_ATTEMPT("Tentativa de Login"),
        LOGIN_SUCCESS("Login Bem-sucedido"),
        LOGIN_FAILED("Login Falhou"),
        LOGOUT("Logout"),
        ACCESS_GRANTED("Acesso Concedido"),
        ACCESS_DENIED("Acesso Negado"),
        DATA_VIEW("Visualização de Dados"),
        DATA_EXPORT("Exportação de Dados"),
        USER_REGISTERED("Usuário Cadastrado"),
        USER_UPDATED("Usuário Atualizado"),
        USER_DELETED("Usuário Removido"),
        BIOMETRIC_ENROLLED("Biometria Cadastrada"),
        BIOMETRIC_UPDATED("Biometria Atualizada"),
        SYSTEM_ERROR("Erro de Sistema");

        private final String description;

        ActionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public AuditLog() {
        this.timestamp = new Date();
    }

    public AuditLog(String userId, ActionType actionType, boolean success) {
        this();
        this.userId = userId;
        this.actionType = actionType;
        this.success = success;
    }

    public AuditLog(String userId, String userName, ActionType actionType,
            boolean success, String description) {
        this(userId, actionType, success);
        this.userName = userName;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getBiometricScore() {
        return biometricScore;
    }

    public void setBiometricScore(double biometricScore) {
        this.biometricScore = biometricScore;
    }

    public String getSeverity() {
        if (!success) {
            return "ALTA";
        }
        if (actionType == ActionType.ACCESS_DENIED ||
                actionType == ActionType.LOGIN_FAILED) {
            return "MÉDIA";
        }
        return "BAIXA";
    }

    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(timestamp);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s: %s | Status: %s | Score: %.2f%%",
                getFormattedTimestamp(),
                userName != null ? userName : userId,
                actionType.getDescription(),
                description != null ? description : "",
                success ? "SUCESSO" : "FALHA",
                biometricScore);
    }

    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("========== LOG DE AUDITORIA ==========\n");
        sb.append("ID: ").append(id).append("\n");
        sb.append("Timestamp: ").append(getFormattedTimestamp()).append("\n");
        sb.append("Usuário: ").append(userName).append(" (").append(userId).append(")\n");
        sb.append("Ação: ").append(actionType.getDescription()).append("\n");
        sb.append("Status: ").append(success ? "SUCESSO" : "FALHA").append("\n");
        sb.append("Nível de Acesso: ").append(accessLevel != null ? accessLevel.toDisplayString() : "N/A").append("\n");
        sb.append("Score Biométrico: ").append(String.format("%.2f%%", biometricScore)).append("\n");
        sb.append("IP: ").append(ipAddress != null ? ipAddress : "N/A").append("\n");
        sb.append("Severidade: ").append(getSeverity()).append("\n");
        sb.append("Descrição: ").append(description != null ? description : "N/A").append("\n");
        sb.append("======================================\n");
        return sb.toString();
    }
}
