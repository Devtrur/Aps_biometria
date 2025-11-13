package com.apsbiometria.aps_biometria.Util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

public class ImageUtils {

    public static BufferedImage resize(BufferedImage original, int targetWidth, int targetHeight) {
        if (original == null)
            return null;

        double aspectRatio = (double) original.getWidth() / original.getHeight();
        int newWidth = targetWidth;
        int newHeight = targetHeight;

        if (targetWidth / aspectRatio <= targetHeight) {
            newHeight = (int) (targetWidth / aspectRatio);
        } else {
            newWidth = (int) (targetHeight * aspectRatio);
        }

        BufferedImage resized = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = resized.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return resized;
    }

    public static BufferedImage resizeExact(BufferedImage original, int width, int height) {
        if (original == null)
            return null;

        BufferedImage resized = new BufferedImage(width, height, original.getType());
        Graphics2D g = resized.createGraphics();
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        return resized;
    }

    public static BufferedImage crop(BufferedImage original, int x, int y, int width, int height) {
        if (original == null)
            return null;

        // Valida limites
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;
        if (x + width > original.getWidth())
            width = original.getWidth() - x;
        if (y + height > original.getHeight())
            height = original.getHeight() - y;

        return original.getSubimage(x, y, width, height);
    }

    public static BufferedImage toGrayscale(BufferedImage original) {
        if (original == null)
            return null;

        BufferedImage grayscale = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        Graphics2D g = grayscale.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        return grayscale;
    }

    public static BufferedImage adjustBrightness(BufferedImage original, float factor) {
        if (original == null)
            return null;

        BufferedImage adjusted = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                original.getType());

        for (int y = 0; y < original.getHeight(); y++) {
            for (int x = 0; x < original.getWidth(); x++) {
                int rgb = original.getRGB(x, y);

                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = Math.min(255, (int) (r * factor));
                g = Math.min(255, (int) (g * factor));
                b = Math.min(255, (int) (b * factor));

                int newRgb = (r << 16) | (g << 8) | b;
                adjusted.setRGB(x, y, newRgb);
            }
        }

        return adjusted;
    }

    public static void save(BufferedImage image, String path, String format) throws IOException {
        if (image == null)
            throw new IllegalArgumentException("Imagem não pode ser nula");

        File output = new File(path);
        output.getParentFile().mkdirs();
        ImageIO.write(image, format, output);
    }

    public static void saveAsJPG(BufferedImage image, String path) throws IOException {
        save(image, path, "jpg");
    }

    public static void saveAsPNG(BufferedImage image, String path) throws IOException {
        save(image, path, "png");
    }

    public static BufferedImage load(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    public static String toBase64(BufferedImage image, String format) throws IOException {
        if (image == null)
            return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] bytes = baos.toByteArray();

        return Base64.getEncoder().encodeToString(bytes);
    }

    public static BufferedImage fromBase64(String base64) throws IOException {
        if (base64 == null || base64.isEmpty())
            return null;

        byte[] bytes = Base64.getDecoder().decode(base64);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);

        return ImageIO.read(bais);
    }

    public static BufferedImage createBlank(int width, int height, Color backgroundColor) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    public static String getDimensions(BufferedImage image) {
        if (image == null)
            return "N/A";
        return image.getWidth() + "x" + image.getHeight();
    }

    public static double getSizeKB(BufferedImage image, String format) throws IOException {
        if (image == null)
            return 0;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.size() / 1024.0;
    }

    public static BufferedImage rotate(BufferedImage original, double degrees) {
        if (original == null)
            return null;

        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int newWidth = (int) Math.floor(original.getWidth() * cos + original.getHeight() * sin);
        int newHeight = (int) Math.floor(original.getHeight() * cos + original.getWidth() * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, original.getType());
        Graphics2D g = rotated.createGraphics();

        g.translate((newWidth - original.getWidth()) / 2, (newHeight - original.getHeight()) / 2);
        g.rotate(radians, original.getWidth() / 2.0, original.getHeight() / 2.0);
        g.drawRenderedImage(original, null);
        g.dispose();

        return rotated;
    }
}
