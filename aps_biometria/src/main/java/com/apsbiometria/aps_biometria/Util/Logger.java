package com.apsbiometria.aps_biometria.Util;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public enum Level {
        DEBUG, INFO, WARNING, ERROR, CRITICAL
    }

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Level currentLevel = Level.INFO;
    private static boolean logToFile = false;
    private static String logFilePath = "logs/application.log";
    private static boolean logToConsole = true;

    public static void setLevel(Level level) {
        currentLevel = level;
    }

    public static void enableFileLogging(String filePath) {
        logToFile = true;
        logFilePath = filePath;
    }

    public static void disableFileLogging() {
        logToFile = false;
    }

    public static void setConsoleLogging(boolean enabled) {
        logToConsole = enabled;
    }

    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }

    public static void debug(String message, Object... args) {
        log(Level.DEBUG, String.format(message, args), null);
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void info(String message, Object... args) {
        log(Level.INFO, String.format(message, args), null);
    }

    public static void warning(String message) {
        log(Level.WARNING, message, null);
    }

    public static void warning(String message, Object... args) {
        log(Level.WARNING, String.format(message, args), null);
    }

    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    public static void error(String message, Object... args) {
        log(Level.ERROR, String.format(message, args), null);
    }

    public static void critical(String message) {
        log(Level.CRITICAL, message, null);
    }

    public static void critical(String message, Throwable throwable) {
        log(Level.CRITICAL, message, throwable);
    }

    private static void log(Level level, String message, Throwable throwable) {

        if (level.ordinal() < currentLevel.ordinal()) {
            return;
        }

        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = formatLogMessage(timestamp, level, message, throwable);

        if (logToConsole) {
            if (level == Level.ERROR || level == Level.CRITICAL) {
                System.err.println(logMessage);
            } else {
                System.out.println(logMessage);
            }
        }

        if (logToFile) {
            try {
                FileUtils.append(logFilePath, logMessage);
            } catch (IOException e) {
                System.err.println("Erro ao escrever log em arquivo: " + e.getMessage());
            }
        }
    }

    private static String formatLogMessage(String timestamp, Level level,
            String message, Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        sb.append("[").append(timestamp).append("]");
        sb.append(" [").append(level).append("]");
        sb.append(" ").append(message);

        if (throwable != null) {
            sb.append("\n");
            sb.append("Exception: ").append(throwable.getClass().getName());
            sb.append(": ").append(throwable.getMessage());
            sb.append("\n");

            for (StackTraceElement element : throwable.getStackTrace()) {
                sb.append("  at ").append(element.toString()).append("\n");
            }
        }

        return sb.toString();
    }

    public static void clearLogFile() {
        if (logToFile) {
            try {
                FileUtils.writeString(logFilePath, "");
                info("Log file cleared");
            } catch (IOException e) {
                System.err.println("Erro ao limpar arquivo de log: " + e.getMessage());
            }
        }
    }

    public static void logSessionStart(String userId, String sessionId) {
        info("Session started - User: %s, Session: %s", userId, sessionId);
    }

    public static void logSessionEnd(String userId, String sessionId) {
        info("Session ended - User: %s, Session: %s", userId, sessionId);
    }

    public static void logAuthentication(String userId, boolean success, double score) {
        if (success) {
            info("Authentication SUCCESS - User: %s, Score: %.2f%%", userId, score * 100);
        } else {
            warning("Authentication FAILED - User: %s, Score: %.2f%%", userId, score * 100);
        }
    }

    public static void logDataAccess(String userId, String dataLevel, boolean granted) {
        if (granted) {
            info("Data access GRANTED - User: %s, Level: %s", userId, dataLevel);
        } else {
            warning("Data access DENIED - User: %s, Level: %s", userId, dataLevel);
        }
    }

    public static void logDatabaseOperation(String operation, String table, boolean success) {
        if (success) {
            debug("Database %s on %s - SUCCESS", operation, table);
        } else {
            error("Database %s on %s - FAILED", operation, table);
        }
    }

    public static void logSeparator() {
        info("================================================");
    }

    public static void logApplicationStart() {
        logSeparator();
        info("Application STARTED");
        info("Log Level: %s", currentLevel);
        info("File Logging: %s", logToFile ? "ENABLED (" + logFilePath + ")" : "DISABLED");
        logSeparator();
    }

    public static void logApplicationStop() {
        logSeparator();
        info("Application STOPPED");
        logSeparator();
    }
}
