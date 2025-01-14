package com.example.demo;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class JavaRecieptReaderOLD {
    private Tesseract tesseract;

    public JavaRecieptReaderOLD() {
        // Initialize Tesseract OCR engine
        tesseract = new Tesseract();

        tesseract.setDatapath(System.getenv("TESSDATA_PREFIX")); // feeding the OCR the AI Data
        tesseract.setPageSegMode(6); // Assume uniform block of text
        tesseract.setLanguage("eng"); // english lang
    }

    public String scanReceipt(String imagePath) throws TesseractException {
        File imageFile = new File(imagePath);
        return tesseract.doOCR(imageFile);
    }

    public Map<String, String> extractReceiptInfo(String receiptText) {
        Map<String, String> receiptInfo = new HashMap<>();

        // Extract total amount
        System.out.println(receiptText);

        Pattern totalPattern = Pattern.compile("(?i)total[:\\s]+\\$?(\\d+\\.\\d{2})");
        Matcher totalMatcher = totalPattern.matcher(receiptText);
        if (totalMatcher.find()) {
            receiptInfo.put("total", totalMatcher.group(1));
        } else {
            Pattern amountPattern = Pattern.compile("\\$?(\\d+\\.\\d{2})");
            Matcher amountMatcher = amountPattern.matcher(receiptText);
            double maxAmount = 0.0;
            String maxAmountStr = null;

            while (amountMatcher.find()) {
                String amountStr = amountMatcher.group(1);
                double amount = Double.parseDouble(amountStr);
                if (amount > maxAmount) {
                    maxAmount = amount;
                    maxAmountStr = amountStr;
                }
            }

            if (maxAmountStr != null) {
                receiptInfo.put("total", maxAmountStr);
            }
        }

        // Extract date
        Pattern datePattern = Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");
        Matcher dateMatcher = datePattern.matcher(receiptText);
        if (dateMatcher.find()) {
            receiptInfo.put("date", dateMatcher.group(1));
        }

        // Extract store name (usually first or second line)
        String[] lines = receiptText.split("\n");
        if (lines.length > 0) {
            receiptInfo.put("store", lines[0].trim());
        }

        return receiptInfo;
    }

    /**
     * Preprocesses image for better OCR results
     */
    public BufferedImage preprocessImage(String imagePath) {
        try {
            BufferedImage image = ImageIO.read(new File(imagePath));

            // Convert to grayscale
            BufferedImage grayscale = new BufferedImage(
                    image.getWidth(), image.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY);
            grayscale.getGraphics().drawImage(image, 0, 0, null);

            return grayscale;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
