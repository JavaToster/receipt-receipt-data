package com.example.receipt_data.util.QRCode;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.*;
import com.google.zxing.common.HybridBinarizer;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.InputStream;
import java.util.*;

@Component
public class QRCodeDecoder {

    private static final int MAX_ATTEMPTS = 5;
    private static final int CONTRAST_THRESHOLD = 20;

    public String readQRFromReceipt(MultipartFile image) throws Exception {
        BufferedImage originalImage = loadImage(image);

        // Попытка 1: Базовая обработка
        String result = tryBasicDecoding(originalImage);
        if (result != null) return result;

        // Попытка 2-5: Постепенное усложнение обработки
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            result = tryEnhancedDecoding(originalImage, attempt);
            if (result != null) return result;
        }

        throw NotFoundException.getNotFoundInstance();
    }

    private BufferedImage loadImage(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            return ImageIO.read(inputStream);
        }
    }

    private String tryBasicDecoding(BufferedImage original) {
        try {
            // Минимальная обработка для быстрого распознавания
            BufferedImage processed = convertToGrayscale(original);
            return decodeQRCode(processed);
        } catch (Exception e) {
            return null;
        }
    }

    private String tryEnhancedDecoding(BufferedImage original, int attempt) {
        try {
            BufferedImage processed = original;

            processed = convertToGrayscale(processed);
            // накопительно заходим во все if для проверки
            if (attempt > 1) {
                processed = enhanceContrast(processed);
            }
            if (attempt > 2) {
                processed = applyDenoising(processed);
            }
            if (attempt > 3) {
                processed = sharpenImage(processed);
            }
            if (attempt > 4) {
                processed = scaleImage(processed, 1.5);
            }
            return decodeQRCode(processed);
        } catch (Exception e) {
            return null;
        }
    }

    private String decodeQRCode(BufferedImage image) throws NotFoundException {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.of(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.ALSO_INVERTED, Boolean.TRUE);

        // Попробовать разные бинаризаторы
        LuminanceSource source = new BufferedImageLuminanceSource(image);

        // Сначала быстрый метод
        Binarizer binarizer = new HybridBinarizer(source);
        try {
            return tryDecode(binarizer, hints);
        } catch (NotFoundException e) {
            // Затем более медленный но точный
            binarizer = new GlobalHistogramBinarizer(source);
            return tryDecode(binarizer, hints);
        }
    }

    private String tryDecode(Binarizer binarizer, Map<DecodeHintType, Object> hints) throws NotFoundException {
        BinaryBitmap bitmap = new BinaryBitmap(binarizer);
        return new MultiFormatReader().decode(bitmap, hints).getText();
    }

    private BufferedImage convertToGrayscale(BufferedImage original) {
        BufferedImage grayscale = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );
        grayscale.getGraphics().drawImage(original, 0, 0, null);
        return grayscale;
    }

    private BufferedImage scaleImage(BufferedImage image, double scale) {
        if (scale == 1.0) return image;

        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);

        BufferedImage scaled = new BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_BYTE_GRAY
        );

        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, newWidth, newHeight, null);
        g.dispose();

        return scaled;
    }

    private BufferedImage enhanceContrast(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        // Быстрая оценка контраста
        int min = 255;
        int max = 0;
        for (int y = 0; y < height; y += 2) {
            for (int x = 0; x < width; x += 2) {
                int pixel = image.getRGB(x, y) & 0xFF;
                if (pixel < min) min = pixel;
                if (pixel > max) max = pixel;
            }
        }

        if (max - min < CONTRAST_THRESHOLD) return image;

        // Простое растяжение контраста
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        float scale = 255.0f / (max - min);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y) & 0xFF;
                int newPixel = Math.min(255, Math.max(0, (int) ((pixel - min) * scale)));
                result.setRGB(x, y, (newPixel << 16) | (newPixel << 8) | newPixel);
            }
        }

        return result;
    }

    private BufferedImage applyDenoising(BufferedImage image) {
        // Быстрый медианный фильтр 3x3 (только центр изображения)
        BufferedImage denoised = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        // Копируем границы
        for (int x = 0; x < image.getWidth(); x++) {
            denoised.setRGB(x, 0, image.getRGB(x, 0));
            denoised.setRGB(x, image.getHeight()-1, image.getRGB(x, image.getHeight()-1));
        }
        for (int y = 1; y < image.getHeight()-1; y++) {
            denoised.setRGB(0, y, image.getRGB(0, y));
            denoised.setRGB(image.getWidth()-1, y, image.getRGB(image.getWidth()-1, y));
        }

        // Обрабатываем только центр
        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                int[] pixels = {
                        image.getRGB(x-1, y-1) & 0xFF,
                        image.getRGB(x, y-1) & 0xFF,
                        image.getRGB(x+1, y-1) & 0xFF,
                        image.getRGB(x-1, y) & 0xFF,
                        image.getRGB(x, y) & 0xFF,
                        image.getRGB(x+1, y) & 0xFF,
                        image.getRGB(x-1, y+1) & 0xFF,
                        image.getRGB(x, y+1) & 0xFF,
                        image.getRGB(x+1, y+1) & 0xFF
                };

                // Быстрая сортировка 9 элементов
                Arrays.sort(pixels);
                int median = pixels[4];
                denoised.setRGB(x, y, (median << 16) | (median << 8) | median);
            }
        }
        return denoised;
    }

    private BufferedImage sharpenImage(BufferedImage image) {
        // Упрощенный фильтр резкости
        BufferedImage sharpened = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY
        );

        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                int center = image.getRGB(x, y) & 0xFF;
                int sum = 0;
                sum += image.getRGB(x-1, y) & 0xFF;
                sum += image.getRGB(x+1, y) & 0xFF;
                sum += image.getRGB(x, y-1) & 0xFF;
                sum += image.getRGB(x, y+1) & 0xFF;

                int value = Math.min(255, Math.max(0, (5 * center - sum) / 2));
                sharpened.setRGB(x, y, (value << 16) | (value << 8) | value);
            }
        }
        return sharpened;
    }
}
