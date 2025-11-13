package com.apsbiometria.aps_biometria.Util;

public class PasswordValidator {

    public enum PasswordStrength {
        MUITO_FRACA,
        FRACA,
        MEDIA,
        FORTE,
        MUITO_FORTE
    }

    public static boolean isValid(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        return password.length() >= 8 &&
                hasUpperCase(password) &&
                hasLowerCase(password) &&
                hasDigit(password);
    }

    public static boolean isValid(String password, int minLength,
            boolean requireUpperCase,
            boolean requireLowerCase,
            boolean requireDigit,
            boolean requireSpecialChar) {
        if (password == null || password.length() < minLength) {
            return false;
        }

        if (requireUpperCase && !hasUpperCase(password))
            return false;
        if (requireLowerCase && !hasLowerCase(password))
            return false;
        if (requireDigit && !hasDigit(password))
            return false;
        if (requireSpecialChar && !hasSpecialChar(password))
            return false;

        return true;
    }

    public static PasswordStrength getStrength(String password) {
        if (password == null || password.isEmpty()) {
            return PasswordStrength.MUITO_FRACA;
        }

        int score = 0;

        if (password.length() >= 8)
            score++;
        if (password.length() >= 12)
            score++;
        if (password.length() >= 16)
            score++;

        if (hasUpperCase(password))
            score++;
        if (hasLowerCase(password))
            score++;
        if (hasDigit(password))
            score++;
        if (hasSpecialChar(password))
            score++;

        if (hasMultipleDigits(password))
            score++;
        if (hasMultipleSpecialChars(password))
            score++;

        if (score <= 2)
            return PasswordStrength.MUITO_FRACA;
        if (score <= 4)
            return PasswordStrength.FRACA;
        if (score <= 6)
            return PasswordStrength.MEDIA;
        if (score <= 8)
            return PasswordStrength.FORTE;
        return PasswordStrength.MUITO_FORTE;
    }

    public static boolean hasUpperCase(String password) {
        return password.matches(".*[A-Z].*");
    }

    public static boolean hasLowerCase(String password) {
        return password.matches(".*[a-z].*");
    }

    public static boolean hasDigit(String password) {
        return password.matches(".*\\d.*");
    }

    public static boolean hasSpecialChar(String password) {
        return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }

    private static boolean hasMultipleDigits(String password) {
        int count = 0;
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c))
                count++;
            if (count >= 2)
                return true;
        }
        return false;
    }

    private static boolean hasMultipleSpecialChars(String password) {
        int count = 0;
        for (char c : password.toCharArray()) {
            if (!Character.isLetterOrDigit(c))
                count++;
            if (count >= 2)
                return true;
        }
        return false;
    }

    public static String getFeedback(String password) {
        if (password == null || password.isEmpty()) {
            return "Senha não pode ser vazia";
        }

        StringBuilder feedback = new StringBuilder();

        if (password.length() < 8) {
            feedback.append("• Senha deve ter no mínimo 8 caracteres\n");
        }

        if (!hasUpperCase(password)) {
            feedback.append("• Adicione pelo menos uma letra MAIÚSCULA\n");
        }

        if (!hasLowerCase(password)) {
            feedback.append("• Adicione pelo menos uma letra minúscula\n");
        }

        if (!hasDigit(password)) {
            feedback.append("• Adicione pelo menos um número\n");
        }

        if (!hasSpecialChar(password)) {
            feedback.append("• Adicione pelo menos um caractere especial (!@#$%)\n");
        }

        if (feedback.length() == 0) {
            PasswordStrength strength = getStrength(password);
            return "Força da senha: " + strength;
        }

        return "Senha fraca. Melhorias necessárias:\n" + feedback.toString();
    }

    public static boolean hasCommonPatterns(String password) {
        if (password == null)
            return false;

        String lower = password.toLowerCase();

        String[] numSequences = { "123", "234", "345", "456", "567", "678", "789" };
        for (String seq : numSequences) {
            if (lower.contains(seq))
                return true;
        }

        String[] alphaSequences = { "abc", "bcd", "cde", "def", "efg", "fgh", "xyz" };
        for (String seq : alphaSequences) {
            if (lower.contains(seq))
                return true;
        }

        String[] keyboardPatterns = { "qwerty", "asdf", "zxcv", "1qaz", "2wsx" };
        for (String pattern : keyboardPatterns) {
            if (lower.contains(pattern))
                return true;
        }

        return false;
    }
}