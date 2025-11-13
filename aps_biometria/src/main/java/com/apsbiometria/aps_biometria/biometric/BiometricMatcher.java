package com.apsbiometria.aps_biometria.biometric;

import com.apsbiometria.aps_biometria.model.BiometricData;

public class BiometricMatcher {

    private static final double VERIFICATION_THRESHOLD = 0.75;
    private static final double IDENTIFICATION_THRESHOLD = 0.80;

    public static class MatchResult {
        private boolean matched;
        private double score;
        private double confidence;
        private String matchedUserId;

        public MatchResult(boolean matched, double score, double confidence, String matchedUserId) {
            this.matched = matched;
            this.score = score;
            this.confidence = confidence;
            this.matchedUserId = matchedUserId;
        }

        public boolean isMatched() {
            return matched;
        }

        public double getScore() {
            return score;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getMatchedUserId() {
            return matchedUserId;
        }

        @Override
        public String toString() {
            return String.format("Match: %s | Score: %.2f%% | Confidence: %.2f%% | User: %s",
                    matched ? "SIM" : "NÃO", score * 100, confidence * 100, matchedUserId);
        }
    }

    public MatchResult verify(BiometricData sample, BiometricData enrolled) {
        if (sample == null || enrolled == null) {
            return new MatchResult(false, 0.0, 0.0, null);
        }

        double score = calculateSimilarity(
                sample.getFeatureVector(),
                enrolled.getFeatureVector());

        double qualityFactor = (sample.getQualityScore() + enrolled.getQualityScore()) / 200.0;
        double adjustedScore = score * qualityFactor;

        boolean matched = adjustedScore >= VERIFICATION_THRESHOLD;
        double confidence = calculateConfidence(adjustedScore, sample.getQualityScore());

        return new MatchResult(matched, adjustedScore, confidence, enrolled.getUserId());
    }

    public MatchResult identify(BiometricData sample, BiometricData[] database) {
        if (sample == null || database == null || database.length == 0) {
            return new MatchResult(false, 0.0, 0.0, null);
        }

        MatchResult bestMatch = null;
        double bestScore = 0.0;

        for (BiometricData enrolled : database) {
            MatchResult result = verify(sample, enrolled);

            if (result.getScore() > bestScore) {
                bestScore = result.getScore();
                bestMatch = result;
            }
        }

        if (bestMatch != null && bestMatch.getScore() >= IDENTIFICATION_THRESHOLD) {
            return bestMatch;
        }

        return new MatchResult(false, bestScore, 0.0, null);
    }

    private double calculateSimilarity(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }

        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("Vetores de características com tamanhos diferentes");
        }

        double distance = euclideanDistance(vector1, vector2);

        double maxDistance = Math.sqrt(vector1.length);
        double similarity = 1.0 - (distance / maxDistance);

        return Math.max(0.0, Math.min(1.0, similarity));
    }

    private double euclideanDistance(double[] v1, double[] v2) {
        double sum = 0.0;

        for (int i = 0; i < v1.length; i++) {
            double diff = v1[i] - v2[i];
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    private double cosineSimilarity(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double calculateConfidence(double score, double quality) {
        double scoreConfidence = Math.pow(score, 2);
        double qualityConfidence = quality / 100.0;

        return (scoreConfidence * 0.7 + qualityConfidence * 0.3);
    }

    public double estimateFAR(double threshold) {
        return Math.exp(-10 * threshold);
    }

    public double estimateFRR(double threshold) {
        return 1.0 - Math.exp(-5 * (1.0 - threshold));
    }

    public double findEER() {
        double minDiff = Double.MAX_VALUE;
        double eerThreshold = 0.5;

        for (double t = 0.0; t <= 1.0; t += 0.01) {
            double far = estimateFAR(t);
            double frr = estimateFRR(t);
            double diff = Math.abs(far - frr);

            if (diff < minDiff) {
                minDiff = diff;
                eerThreshold = t;
            }
        }

        return eerThreshold;
    }

    public void setVerificationThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Threshold deve estar entre 0 e 1");
        }
    }

    public String generatePerformanceReport(MatchResult result) {
        StringBuilder report = new StringBuilder();
        report.append("===== RELATÓRIO DE MATCHING BIOMÉTRICO =====\n");
        report.append("Status: ").append(result.isMatched() ? "AUTENTICADO" : "REJEITADO").append("\n");
        report.append("Score de Similaridade: ").append(String.format("%.2f%%", result.getScore() * 100)).append("\n");
        report.append("Confiança: ").append(String.format("%.2f%%", result.getConfidence() * 100)).append("\n");
        report.append("Threshold Verificação: ").append(String.format("%.2f%%", VERIFICATION_THRESHOLD * 100))
                .append("\n");
        report.append("Threshold Identificação: ").append(String.format("%.2f%%", IDENTIFICATION_THRESHOLD * 100))
                .append("\n");
        report.append("FAR Estimado: ").append(String.format("%.4f%%", estimateFAR(result.getScore()) * 100))
                .append("\n");
        report.append("FRR Estimado: ").append(String.format("%.4f%%", estimateFRR(result.getScore()) * 100))
                .append("\n");

        if (result.isMatched()) {
            report.append("Usuário Identificado: ").append(result.getMatchedUserId()).append("\n");
        }

        report.append("==========================================\n");

        return report.toString();
    }
}