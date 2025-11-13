package com.apsbiometria.aps_biometria.Util;

public class CPFValidator {

    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.isEmpty()) {
            return false;
        }

        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != 11) {
            return false;
        }

        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10)
                firstDigit = 0;

            if (Character.getNumericValue(cpf.charAt(9)) != firstDigit) {
                return false;
            }

            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10)
                secondDigit = 0;

            return Character.getNumericValue(cpf.charAt(10)) == secondDigit;

        } catch (Exception e) {
            return false;
        }
    }

    public static String removeFormatting(String cpf) {
        if (cpf == null)
            return "";
        return cpf.replaceAll("[^0-9]", "");
    }

    public static String format(String cpf) {
        cpf = removeFormatting(cpf);

        if (cpf.length() != 11) {
            return cpf;
        }

        return String.format("%s.%s.%s-%s",
                cpf.substring(0, 3),
                cpf.substring(3, 6),
                cpf.substring(6, 9),
                cpf.substring(9, 11));
    }

    public static String generateRandom() {
        java.util.Random random = new java.util.Random();

        int[] cpf = new int[11];
        for (int i = 0; i < 9; i++) {
            cpf[i] = random.nextInt(10);
        }

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += cpf[i] * (10 - i);
        }
        cpf[9] = 11 - (sum % 11);
        if (cpf[9] >= 10)
            cpf[9] = 0;

        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += cpf[i] * (11 - i);
        }
        cpf[10] = 11 - (sum % 11);
        if (cpf[10] >= 10)
            cpf[10] = 0;

        StringBuilder result = new StringBuilder();
        for (int digit : cpf) {
            result.append(digit);
        }

        return format(result.toString());
    }
}
