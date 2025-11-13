package com.apsbiometria.aps_biometria.biometric;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.apsbiometria.aps_biometria.model.BiometricData;

public class FeatureExtractor {

    public static class FeaturePoint {
        public int x;
        public int y;
        public double orientation;
        public double strength;

        public FeaturePoint(int x, int y, double orientation, double strength) {
            this.x = x;
            this.y = y;
            this.orientation = orientation;
            this.strength = strength;
        }
    }

    public BiometricData extractFeatures(BufferedImage image, String userId) {
        ImagePreprocessor preprocessor = new ImagePreprocessor();
        BufferedImage processed = preprocessor.preprocess(image);

        List<FeaturePoint> features = extractKeyPoints(processed);

        double[] featureVector = computeFeatureVector(processed, features);

        BiometricData biometricData = new BiometricData();
        biometricData.setUserId(userId);
        biometricData.setFeatureVector(featureVector);
        biometricData.setTemplate(serializeFeatures(features));
        biometricData.setQualityScore(calculateQualityScore(features));
        biometricData.setCaptureDate(new java.util.Date());

        return biometricData;
    }

    private List<FeaturePoint> extractKeyPoints(BufferedImage image) {
        List<FeaturePoint> keyPoints = new ArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        int windowSize = 5;
        int threshold = 100000;

        for (int y = windowSize; y < height - windowSize; y += 3) {
            for (int x = windowSize; x < width - windowSize; x += 3) {
                double response = computeHarrisResponse(image, x, y, windowSize);

                if (response > threshold) {
                    double orientation = computeOrientation(image, x, y, windowSize);
                    keyPoints.add(new FeaturePoint(x, y, orientation, response));
                }
            }
        }

        keyPoints.sort((a, b) -> Double.compare(b.strength, a.strength));
        return keyPoints.subList(0, Math.min(100, keyPoints.size()));
    }

    private double computeHarrisResponse(BufferedImage image, int cx, int cy, int windowSize) {
        double ixx = 0, iyy = 0, ixy = 0;

        for (int y = -windowSize; y <= windowSize; y++) {
            for (int x = -windowSize; x <= windowSize; x++) {
                int px = Math.max(0, Math.min(image.getWidth() - 1, cx + x));
                int py = Math.max(0, Math.min(image.getHeight() - 1, cy + y));

                double gx = getGradientX(image, px, py);
                double gy = getGradientY(image, px, py);

                ixx += gx * gx;
                iyy += gy * gy;
                ixy += gx * gy;
            }
        }

        double det = ixx * iyy - ixy * ixy;
        double trace = ixx + iyy;
        double k = 0.04;

        return det - k * trace * trace;
    }

    private double computeOrientation(BufferedImage image, int cx, int cy, int windowSize) {
        double sumGx = 0, sumGy = 0;

        for (int y = -windowSize; y <= windowSize; y++) {
            for (int x = -windowSize; x <= windowSize; x++) {
                int px = Math.max(0, Math.min(image.getWidth() - 1, cx + x));
                int py = Math.max(0, Math.min(image.getHeight() - 1, cy + y));

                sumGx += getGradientX(image, px, py);
                sumGy += getGradientY(image, px, py);
            }
        }

        return Math.atan2(sumGy, sumGx);
    }

    private double getGradientX(BufferedImage image, int x, int y) {
        if (x == 0 || x == image.getWidth() - 1)
            return 0;

        int left = image.getRGB(x - 1, y) & 0xFF;
        int right = image.getRGB(x + 1, y) & 0xFF;

        return (right - left) / 2.0;
    }

    private double getGradientY(BufferedImage image, int x, int y) {
        if (y == 0 || y == image.getHeight() - 1)
            return 0;

        int top = image.getRGB(x, y - 1) & 0xFF;
        int bottom = image.getRGB(x, y + 1) & 0xFF;

        return (bottom - top) / 2.0;
    }

    private double[] computeFeatureVector(BufferedImage image, List<FeaturePoint> keyPoints) {
        int vectorSize = 128; // Tamanho fixo do vetor
        double[] vector = new double[vectorSize];

        int width = image.getWidth();
        int height = image.getHeight();

        int gridSize = 8;
        int cellWidth = width / gridSize;
        int cellHeight = height / gridSize;

        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                int startX = i * cellWidth;
                int startY = j * cellHeight;

                double avgIntensity = 0;
                int count = 0;

                for (int y = startY; y < startY + cellHeight && y < height; y++) {
                    for (int x = startX; x < startX + cellWidth && x < width; x++) {
                        avgIntensity += image.getRGB(x, y) & 0xFF;
                        count++;
                    }
                }

                int index = i * gridSize + j;
                if (index < vectorSize) {
                    vector[index] = count > 0 ? avgIntensity / count : 0;
                }
            }
        }

        double norm = 0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }

        return vector;
    }

    private String serializeFeatures(List<FeaturePoint> features) {
        StringBuilder sb = new StringBuilder();

        for (FeaturePoint fp : features) {
            sb.append(fp.x).append(",")
                    .append(fp.y).append(",")
                    .append(fp.orientation).append(",")
                    .append(fp.strength).append(";");
        }

        return sb.toString();
    }

    private double calculateQualityScore(List<FeaturePoint> features) {
        if (features.isEmpty())
            return 0.0;

        double avgStrength = features.stream()
                .mapToDouble(f -> f.strength)
                .average()
                .orElse(0.0);

        double quantityScore = Math.min(1.0, features.size() / 100.0);
        double strengthScore = Math.min(1.0, avgStrength / 1000000.0);

        return (quantityScore * 0.4 + strengthScore * 0.6) * 100;
    }
}