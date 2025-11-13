package com.apsbiometria.aps_biometria.model;

public enum AuditAction {
    LOGIN_ATTEMPT("Tentativa de Login"),
    LOGIN_SUCCESS("Login Bem-sucedido"),
    LOGIN_FAILED("Login Falhado"),
    LOGOUT("Logout"),
    BIOMETRIC_REGISTRATION("Cadastro Biométrico"),
    ACCESS_DENIED("Acesso Negado"),
    DATA_ACCESS("Acesso a Dados"),
    USER_CREATED("Usuário Criado"),
    USER_UPDATED("Usuário Atualizado"),
    USER_DELETED("Usuário Deletado");

    private final String description;

    AuditAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
