package com.apsbiometria.aps_biometria.authentication;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.List;

import com.apsbiometria.aps_biometria.biometric.BiometricCapture;
import com.apsbiometria.aps_biometria.biometric.BiometricMatcher;
import com.apsbiometria.aps_biometria.biometric.FeatureExtractor;
import com.apsbiometria.aps_biometria.biometric.ImagePreprocessor;
import com.apsbiometria.aps_biometria.model.BiometricData;
import com.apsbiometria.aps_biometria.model.User;
import com.apsbiometria.aps_biometria.repository.BiometricRepository;
import com.apsbiometria.aps_biometria.repository.UserRepository;

public class BiometricAuthenticator {

    private final BiometricCapture capture;
    private final ImagePreprocessor preprocessor;
    private final FeatureExtractor extractor;
    private final BiometricMatcher matcher;
    private final UserRepository userRepo;
    private final BiometricRepository bioRepo;

    public BiometricAuthenticator() {
        this.capture = new BiometricCapture();
        this.preprocessor = new ImagePreprocessor();
        this.extractor = new FeatureExtractor();
        this.matcher = new BiometricMatcher();
        this.userRepo = new UserRepository();
        this.bioRepo = new BiometricRepository();
    }

    public static class AuthenticationResult {
        private boolean authenticated;
        private User user;
        private double score;
        private String message;
        private BiometricMatcher.MatchResult matchResult;

        public AuthenticationResult(boolean authenticated, User user, double score, String message) {
            this.authenticated = authenticated;
            this.user = user;
            this.score = score;
            this.message = message;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public User getUser() {
            return user;
        }

        public double getScore() {
            return score;
        }

        public String getMessage() {
            return message;
        }

        public BiometricMatcher.MatchResult getMatchResult() {
            return matchResult;
        }

        public void setMatchResult(BiometricMatcher.MatchResult matchResult) {
            this.matchResult = matchResult;
        }

        @Override
        public String toString() {
            return String.format("Authentication: %s | User: %s | Score: %.2f%% | %s",
                    authenticated ? "SUCCESS" : "FAILED",
                    user != null ? user.getName() : "N/A",
                    score * 100,
                    message);
        }
    }

    public BiometricData enrollBiometric(String userId, BufferedImage image, String biometricType)
            throws SQLException {
        if (!capture.validateImageQuality(image)) {
            throw new IllegalArgumentException("Qualidade da imagem insuficiente para cadastro");
        }
        BiometricData bioData = extractor.extractFeatures(image, userId);
        bioData.setBiometricType(biometricType);
        if (bioData.getQualityScore() < 60.0) {
            throw new IllegalArgumentException(
                    String.format("Qualidade biométrica muito baixa: %.2f%% (mínimo: 60%%)",
                            bioData.getQualityScore()));
        }
        bioData = bioRepo.create(bioData);
        System.out.println("✓ Biometria cadastrada: " + bioData.getId() +
                " | Qualidade: " + String.format("%.2f%%", bioData.getQualityScore()));
        return bioData;
    }

    public BiometricData enrollBiometricFromFile(String userId, String imagePath, String biometricType)
            throws Exception {
        BufferedImage image = capture.captureFromFile(imagePath);
        return enrollBiometric(userId, image, biometricType);
    }

    public AuthenticationResult authenticateUser(String userId, BufferedImage image)
            throws SQLException {
        User user = userRepo.findById(userId);
        if (user == null) {
            return new AuthenticationResult(false, null, 0.0, "Usuário não encontrado");
        }
        if (!user.isActive()) {
            return new AuthenticationResult(false, user, 0.0, "Usuário inativo");
        }
        if (user.isLocked()) {
            return new AuthenticationResult(false, user, 0.0,
                    "Usuário bloqueado por excesso de tentativas falhadas");
        }
        if (!capture.validateImageQuality(image)) {
            return new AuthenticationResult(false, user, 0.0, "Qualidade da imagem insuficiente");
        }
        BiometricData sample = extractor.extractFeatures(image, userId);
        List<BiometricData> enrolledData = bioRepo.findByUserId(userId);
        if (enrolledData.isEmpty()) {
            return new AuthenticationResult(false, user, 0.0,
                    "Nenhuma biometria cadastrada para este usuário");
        }
        BiometricData bestEnrolled = enrolledData.stream()
                .max((a, b) -> Double.compare(a.getQualityScore(), b.getQualityScore()))
                .orElse(enrolledData.get(0));
        BiometricMatcher.MatchResult matchResult = matcher.verify(sample, bestEnrolled);
        AuthenticationResult result;
        if (matchResult.isMatched()) {
            result = new AuthenticationResult(true, user, matchResult.getScore(),
                    "Autenticação bem-sucedida");
        } else {
            result = new AuthenticationResult(false, user, matchResult.getScore(),
                    "Biometria não reconhecida");
        }
        result.setMatchResult(matchResult);
        return result;
    }

    public AuthenticationResult identifyUser(BufferedImage image) throws SQLException {
        if (!capture.validateImageQuality(image)) {
            return new AuthenticationResult(false, null, 0.0, "Qualidade da imagem insuficiente");
        }
        BiometricData sample = extractor.extractFeatures(image, "unknown");
        List<BiometricData> database = bioRepo.findAll();
        if (database.isEmpty()) {
            return new AuthenticationResult(false, null, 0.0,
                    "Nenhuma biometria cadastrada no sistema");
        }
        BiometricData[] dbArray = database.toArray(new BiometricData[0]);
        BiometricMatcher.MatchResult matchResult = matcher.identify(sample, dbArray);
        if (matchResult.isMatched()) {
            User user = userRepo.findById(matchResult.getMatchedUserId());
            if (user != null && user.isActive() && !user.isLocked()) {
                AuthenticationResult result = new AuthenticationResult(true, user,
                        matchResult.getScore(), "Usuário identificado com sucesso");
                result.setMatchResult(matchResult);
                return result;
            }
        }
        return new AuthenticationResult(false, null, matchResult.getScore(),
                "Não foi possível identificar o usuário");
    }

    public AuthenticationResult authenticateFromFile(String userId, String imagePath)
            throws Exception {
        BufferedImage image = capture.captureFromFile(imagePath);
        return authenticateUser(userId, image);
    }

    public AuthenticationResult identifyFromFile(String imagePath) throws Exception {
        BufferedImage image = capture.captureFromFile(imagePath);
        return identifyUser(image);
    }

    public BiometricData updateBiometric(String biometricId, BufferedImage newImage)
            throws SQLException {
        BiometricData existing = bioRepo.findById(biometricId);
        if (existing == null) {
            throw new IllegalArgumentException("Biometria não encontrada: " + biometricId);
        }
        BiometricData newData = extractor.extractFeatures(newImage, existing.getUserId());
        if (newData.getQualityScore() < 60.0) {
            throw new IllegalArgumentException("Qualidade insuficiente para atualização");
        }
        existing.setFeatureVector(newData.getFeatureVector());
        existing.setTemplate(newData.getTemplate());
        existing.setQualityScore(newData.getQualityScore());
        bioRepo.update(existing);
        System.out.println("✓ Biometria atualizada: " + biometricId);
        return existing;
    }

    public boolean removeBiometric(String biometricId) throws SQLException {
        return bioRepo.softDelete(biometricId);
    }

    public String generateAuthReport(AuthenticationResult result) {
        StringBuilder report = new StringBuilder();
        report.append("===== RELATÓRIO DE AUTENTICAÇÃO =====\n");
        report.append("Status: ").append(result.isAuthenticated() ? "SUCESSO" : "FALHA").append("\n");
        if (result.getUser() != null) {
            report.append("Usuário: ").append(result.getUser().getName()).append("\n");
            report.append("Email: ").append(result.getUser().getEmail()).append("\n");
            report.append("Nível de Acesso: ").append(result.getUser().getAccessLevel().toDisplayString()).append("\n");
        }
        report.append("Score: ").append(String.format("%.2f%%", result.getScore() * 100)).append("\n");
        report.append("Mensagem: ").append(result.getMessage()).append("\n");
        if (result.getMatchResult() != null) {
            report.append("\n").append(matcher.generatePerformanceReport(result.getMatchResult()));
        }
        report.append("====================================\n");
        return report.toString();
    }
}
