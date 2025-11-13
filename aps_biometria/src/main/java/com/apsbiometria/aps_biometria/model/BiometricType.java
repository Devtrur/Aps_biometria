package com.apsbiometria.aps_biometria.model;

public enum BiometricType {
    FINGERPRINT("Digital", "Impressão digital"),
    FACE("Facial", "Reconhecimento facial"),
    IRIS("Íris", "Reconhecimento de íris");

    private final String name;
    private final String description;

    BiometricType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}