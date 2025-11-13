package com.apsbiometria.aps_biometria.biometric;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.List;

public class BiometricCapture {

    private static final int TARGET_WIDTH = 640;
    private static final int TARGET_HEIGHT = 480;

    public BufferedImage captureFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("Arquivo não encontrado: " + filePath);
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Formato de imagem não suportado");
        }

        return resizeImage(image);
    }

    public List<BufferedImage> captureFromVideo(String videoFramesPath) throws IOException {
        List<BufferedImage> frames = new ArrayList<>();
        File directory = new File(videoFramesPath);

        if (!directory.exists() || !directory.isDirectory()) {
            throw new IOException("Diretório de frames não encontrado");
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") ||
                name.toLowerCase().endsWith(".png"));

        if (files != null) {
            for (File file : files) {
                BufferedImage frame = ImageIO.read(file);
                if (frame != null) {
                    frames.add(resizeImage(frame));
                }
            }
        }

        return frames;
    }

    private BufferedImage resizeImage(BufferedImage original) {
        if (original.getWidth() == TARGET_WIDTH &&
                original.getHeight() == TARGET_HEIGHT) {
            return original;
        }

        BufferedImage resized = new BufferedImage(
                TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);

        java.awt.Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, TARGET_WIDTH, TARGET_HEIGHT, null);
        g.dispose();

        return resized;
    }

    public boolean validateImageQuality(BufferedImage image) {
        if (image == null)
            return false;

        if (image.getWidth() < 100 || image.getHeight() < 100) {
            return false;
        }

        int totalPixels = 0;
        long sumBrightness = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                sumBrightness += (r + g + b) / 3;
                totalPixels++;
            }
        }

        double avgBrightness = (double) sumBrightness / totalPixels;

        return avgBrightness > 10 && avgBrightness < 245;
    }

    public void saveImage(BufferedImage image, String outputPath) throws IOException {
        File output = new File(outputPath);
        output.getParentFile().mkdirs();
        ImageIO.write(image, "jpg", output);
    }
}
