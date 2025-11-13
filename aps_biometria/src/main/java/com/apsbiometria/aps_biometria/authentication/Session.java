package com.apsbiometria.aps_biometria.authentication;

import java.util.Date;
import java.util.UUID;

import com.apsbiometria.aps_biometria.model.AccessLevel;
import com.apsbiometria.aps_biometria.model.User;

public class Session {
    private String sessionId;
    private User user;
    private Date loginTime;
    private Date lastActivityTime;
    private String ipAddress;
    private boolean active;
    private double authenticationScore;

    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    public Session(User user, String ipAddress, double authenticationScore) {
        this.sessionId = UUID.randomUUID().toString();
        this.user = user;
        this.ipAddress = ipAddress;
        this.authenticationScore = authenticationScore;
        this.loginTime = new Date();
        this.lastActivityTime = new Date();
        this.active = true;
    }

    public void updateActivity() {
        this.lastActivityTime = new Date();
    }

    public boolean isExpired() {
        long inactiveTime = System.currentTimeMillis() - lastActivityTime.getTime();
        return inactiveTime > SESSION_TIMEOUT;
    }

    public boolean hasAccess(AccessLevel requiredLevel) {
        if (!active || isExpired()) {
            return false;
        }
        return user.getAccessLevel().getLevel() >= requiredLevel.getLevel();
    }

    public void invalidate() {
        this.active = false;
    }

    public long getSessionDurationMinutes() {
        long duration = System.currentTimeMillis() - loginTime.getTime();
        return duration / (60 * 1000);
    }

    public long getInactiveTimeMinutes() {
        long inactive = System.currentTimeMillis() - lastActivityTime.getTime();
        return inactive / (60 * 1000);
    }

    public String getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public Date getLastActivityTime() {
        return lastActivityTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public boolean isActive() {
        return active && !isExpired();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getAuthenticationScore() {
        return authenticationScore;
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId='" + sessionId + '\'' +
                ", user=" + user.getName() +
                ", loginTime=" + loginTime +
                ", active=" + isActive() +
                ", duration=" + getSessionDurationMinutes() + "min" +
                '}';
    }
}
