package com.apsbiometria.aps_biometria.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static String readAsString(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }

    public static List<String> readLines(String path) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(path, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    public static void writeString(String path, String content) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

    public static void writeLines(String path, List<String> lines) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, StandardCharsets.UTF_8))) {

            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    public static void append(String path, String content) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(file, StandardCharsets.UTF_8, true))) {
            writer.write(content);
            writer.newLine();
        }
    }

    public static boolean exists(String path) {
        return new File(path).exists();
    }

    public static boolean createDirectory(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }

    public static boolean delete(String path) {
        return new File(path).delete();
    }

    public static boolean deleteDirectory(String path) {
        File directory = new File(path);

        if (!directory.exists()) {
            return false;
        }

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file.getAbsolutePath());
                    } else {
                        file.delete();
                    }
                }
            }
        }

        return directory.delete();
    }

    public static void copy(String source, String destination) throws IOException {
        Files.copy(Paths.get(source), Paths.get(destination));
    }

    public static void move(String source, String destination) throws IOException {
        Files.move(Paths.get(source), Paths.get(destination));
    }

    public static long getSize(String path) {
        return new File(path).length();
    }

    public static String getFormattedSize(String path) {
        long bytes = getSize(path);

        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    public static List<String> listFiles(String directory) {
        List<String> files = new ArrayList<>();
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isFile()) {
                        files.add(file.getName());
                    }
                }
            }
        }

        return files;
    }

    public static List<String> listDirectories(String directory) {
        List<String> directories = new ArrayList<>();
        File dir = new File(directory);

        if (dir.exists() && dir.isDirectory()) {
            File[] fileList = dir.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    if (file.isDirectory()) {
                        directories.add(file.getName());
                    }
                }
            }
        }

        return directories;
    }

    public static String getExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot > 0) {
            return path.substring(lastDot + 1).toLowerCase();
        }
        return "";
    }

    public static String getNameWithoutExtension(String path) {
        String name = new File(path).getName();
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            return name.substring(0, lastDot);
        }
        return name;
    }

    public static boolean isImage(String path) {
        String ext = getExtension(path);
        return ext.equals("jpg") || ext.equals("jpeg") ||
                ext.equals("png") || ext.equals("gif") ||
                ext.equals("bmp");
    }

    public static byte[] readBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static void writeBytes(String path, byte[] data) throws IOException {
        File file = new File(path);
        file.getParentFile().mkdirs();
        Files.write(Paths.get(path), data);
    }
}
