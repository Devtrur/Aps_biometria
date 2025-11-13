package com.apsbiometria.aps_biometria.model;

import java.util.Date;

public class BiometricData {
    private String id;
    private String userId;
    private String biometricType;
    private double[] featureVector;
    private String template;
    private double qualityScore;
    private Date captureDate;
    private Date lastUpdateDate;
    private boolean active;

    public BiometricData() {
        this.active = true;
        this.captureDate = new Date();
        this.lastUpdateDate = new Date();
    }

    public BiometricData(String userId, String biometricType) {
        this();
        this.userId = userId;
        this.biometricType = biometricType;
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

    public String getBiometricType() {
        return biometricType;
    }

    public void setBiometricType(String biometricType) {
        this.biometricType = biometricType;
    }

    public double[] getFeatureVector() {
        return featureVector;
    }

    public void setFeatureVector(double[] featureVector) {
        this.featureVector = featureVector;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public double getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(double qualityScore) {
        this.qualityScore = qualityScore;
    }

    public Date getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean hasValidFeatures() {
        return featureVector != null && featureVector.length > 0 && qualityScore > 30.0;
    }

    public String getQualityLevel() {
        if (qualityScore >= 80)
            return "EXCELENTE";
        if (qualityScore >= 60)
            return "BOA";
        if (qualityScore >= 40)
            return "REGULAR";
        return "BAIXA";
    }

    @Override
    public String toString() {
        return "BiometricData{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", type='" + biometricType + '\'' +
                ", quality=" + String.format("%.2f", qualityScore) +
                ", captureDate=" + captureDate +
                '}';
    }
}
