package com.apsbiometria.aps_biometria.model;

import java.util.Date;

public class User {
    private String id;
    private String name;
    private String email;
    private String cpf;
    private AccessLevel accessLevel;
    private String department;
    private boolean active;
    private Date registrationDate;
    private Date lastAccessDate;
    private int failedAttempts;
    private boolean locked;

    public User() {
        this.active = true;
        this.registrationDate = new Date();
        this.failedAttempts = 0;
        this.locked = false;
    }

    public User(String name, String email, String cpf, AccessLevel accessLevel) {
        this();
        this.name = name;
        this.email = email;
        this.cpf = cpf;
        this.accessLevel = accessLevel;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public AccessLevel getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(AccessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(int failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3) {
            this.locked = true;
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.locked = false;
    }

    public boolean canAccess(AccessLevel requiredLevel) {
        if (!active || locked) {
            return false;
        }
        return this.accessLevel.getLevel() >= requiredLevel.getLevel();
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", accessLevel=" + accessLevel +
                ", active=" + active +
                ", locked=" + locked +
                '}';
    }
}
