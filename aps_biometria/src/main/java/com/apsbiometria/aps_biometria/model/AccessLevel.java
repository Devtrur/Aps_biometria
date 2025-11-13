package com.apsbiometria.aps_biometria.model;

public enum AccessLevel {
    NIVEL_1(1, "Público", "Acesso a informações públicas"),
    NIVEL_2(2, "Diretor", "Acesso a informações estratégicas de divisões"),
    NIVEL_3(3, "Ministro", "Acesso total a informações confidenciais");

    private final int level;
    private final String name;
    private final String description;

    AccessLevel(int level, String name, String description) {
        this.level = level;
        this.name = name;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean canAccess(AccessLevel required) {
        return this.level >= required.level;
    }

    public static AccessLevel fromLevel(int level) {
        for (AccessLevel al : values()) {
            if (al.level == level) {
                return al;
            }
        }
        throw new IllegalArgumentException("Nível de acesso inválido: " + level);
    }

    public static AccessLevel fromName(String name) {
        for (AccessLevel al : values()) {
            if (al.name.equalsIgnoreCase(name)) {
                return al;
            }
        }
        throw new IllegalArgumentException("Nome de nível de acesso inválido: " + name);
    }

    @Override
    public String toString() {
        return "Nível " + level + " - " + name + ": " + description;
    }

    public String toDisplayString() {
        return String.format("Nível %d - %s", level, name);
    }
}
