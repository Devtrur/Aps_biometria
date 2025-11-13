package com.apsbiometria.aps_biometria.Util;

import java.util.regex.Pattern;

public class EmailValidator {

    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    public static boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        email = email.trim().toLowerCase();

        if (email.length() > 254) {
            return false;
        }

        return pattern.matcher(email).matches();
    }

    public static boolean isGovernmentEmail(String email) {
        if (!isValid(email)) {
            return false;
        }

        email = email.toLowerCase();
        return email.endsWith(".gov.br") ||
                email.endsWith(".gov");
    }

    public static String extractDomain(String email) {
        if (!isValid(email)) {
            return "";
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(atIndex + 1);
        }

        return "";
    }

    public static String extractUsername(String email) {
        if (!isValid(email)) {
            return "";
        }

        int atIndex = email.indexOf('@');
        if (atIndex > 0) {
            return email.substring(0, atIndex);
        }

        return "";
    }

    public static String normalize(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase();
    }

    public static String mask(String email) {
        if (!isValid(email)) {
            return email;
        }

        String username = extractUsername(email);
        String domain = extractDomain(email);

        if (username.length() <= 2) {
            return username.charAt(0) + "***@" + domain;
        }

        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
    }
}
