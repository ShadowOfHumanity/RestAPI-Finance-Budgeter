package com.example.demo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecieptPhotoScanner {
    private static final String key = System.getenv("AZURE_VISION_KEY");
    private static final String endpoint = System.getenv("AZURE_VISION_ENDPOINT");

    private static final String uriBase = endpoint + "/vision/v3.1/ocr";
    private static String imageToAnalyze;
    private static boolean isDiscounted = false;


    public static String scanReceipt(ByteArrayEntity requestEntity){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            URIBuilder builder = new URIBuilder(uriBase); // URI builder for the request to OCR RESTApi
            builder.setParameter("visualFeatures", "Categories,Description,Color");

            // getting request
            HttpPost request = new HttpPost(builder.build());

            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", key);

            request.setEntity(requestEntity);

            // RestApi call and Response (ocr)
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Formatting the JSON response
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                System.out.println(json.toString(2));
                return extractInfo(json);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "{null}";
    }

    private static String extractInfo(JSONObject json) {
        JSONArray regions = json.getJSONArray("regions");
        String companyName = null;
        String date = null;
        String totalPrice = null;

        // iterate through lines, words in lines, and text in words
        for (int i = 0; i < regions.length(); i++) {
            JSONArray lines = regions.getJSONObject(i).getJSONArray("lines");
            for (int j = 0; j < lines.length(); j++) {
                JSONArray words = lines.getJSONObject(j).getJSONArray("words");
                StringBuilder lineText = new StringBuilder();
                for (int k = 0; k < words.length(); k++) {
                    lineText.append(words.getJSONObject(k).getString("text")).append(" ");
                }
                String text = lineText.toString().trim();


                // Check for company name
                String cleanedText = text.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                if (CompanyService.isCompanyPresent(cleanedText)){
                    companyName = cleanedText;
                } else {
                    if (companyName == null) {
                        // Match common company name patterns,business types and suffixes
                        // also checks if there is a number before the company name or after, so it can rule it out as a name.
                        Pattern companyPattern = Pattern.compile(
                                "(?i)(?<!\\d\\.?)\\b[A-Za-z]+(?:\\s+[A-Za-z]+)*\\s*(Pharmacy|Store|Shop|Clinic|Market|Mini\\s?Market|Grocery|Retail|Supermarket|Laboratory|Pharmaceuticals|Corporation|Inc|Ltd|Enterprise|Services|Consulting|Group|Systems|Technologies|International|Associates|Industries|Co|LLC|LLP|Network|Solutions|Works|Firm|Manufacturing|Wholesale|Supplies|Furniture|Construction|Import|Export|Foods|Toys|Jewelry|Boutique|Bakery|Electronics|Automotive|Cosmetics|Textiles|Crafts|Apparel|Technology|Stationary|Confectionary|Beverages|Convenience|Pet\\s?Store|Health|Vape\\s?Shop|Mobile|Fashion|Clothing|Sport|Outdoors|Toy\\s?Store|Cellular|Optical|Bedding|Home\\s?Goods|Tech|Luxury|Restaurant|Bar|Café|Catering|Florist|Gifts|Home\\s?Improvements|Arts\\s?Crafts)\\b(?!\\.?\\d)");
                        Matcher companyMatcher = companyPattern.matcher(cleanedText);

                        if (companyMatcher.find()) {
                            companyName = companyMatcher.group(0).trim();  // attempt to  Capture the whole company name
                        }
                    }
                }



                // Check for date (assuming date format is DD/MM/YYYY) aka EUROPEAN FORMAT
                String trimmedText = text.trim(); // Remove prefix and suffixes to the texts
                Pattern datePattern = Pattern.compile("(\\d{2}/\\d{2}/\\d{4})|(\\d{2}-\\d{2}-\\d{4})");
                Matcher dateMatcher = datePattern.matcher(trimmedText);

                if (date == null && dateMatcher.find()) {
                    date = dateMatcher.group(0);
                }



                if (text.contains("Discount") || text.contains("discount") || text.contains("DISCOUNT")) {
                    isDiscounted = true;
                } // tries to ignore the biggest decimal number if it has "Discount" in it, to try and get the real total price


                // get total price, if it has "Total before it"
                if ((totalPrice == null) && (text.contains("Total €") || (text.contains("Total") || text.contains("TOTAL") || text.contains("total") || text.contains("Total:") || text.contains("EUR") || text.contains("Amount") || text.contains("AMOUNT") || text.contains("amount") || text.contains("Due") || text.contains("DUE") || text.contains("due") || text.contains("TOTAL Euro") || text.contains("Total Euro") || text.contains("total Euro") || text.contains("TOTAL EUR") || text.contains("Total EUR") || text.contains("total EUR")))) {

                    try {
                        totalPrice = lines.getJSONObject(j + 1).getJSONArray("words").getJSONObject(1).getString("text");
                    } catch (Exception e) {
                        continue;
                    }

                    } else {
                    for (int k = 0; k < words.length(); k++) {
                        String word = words.getJSONObject(k).getString("text");
                        if (word.matches("€?\\d+[.,]\\d{2}")) { // match is a number with opt € symbol

                            double amount = Double.parseDouble(word.replace("€", "").replace(",", "."));
                            if (totalPrice != null) {
                                double currentTotal = 0.00;
                                try {
                                    currentTotal = Double.parseDouble(totalPrice);
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid input for totalPrice: " + totalPrice);
                                }
                                //double currentTotal = Double.parseDouble(totalPrice);
                                if (isDiscounted) {
                                    isDiscounted = false;
                                }
                                else if (currentTotal < amount) {
                                    totalPrice = String.format("%.2f", amount);
                                }
                            } else {
                                totalPrice = String.format("%.2f", amount);
                            }

                        }
                    }
                }
            }


        }


        System.out.println("Company Name: " + companyName);
        System.out.println("Date: " + date);
        System.out.println("Total_Price: €" + totalPrice);


        isDiscounted = false;
        if (companyName != null && date != null && totalPrice != null) {
            return "{Company_Name: " + companyName + ", Date: " + date + ", Total_Price: €" + totalPrice + "}";
        } else if (companyName != null && date != null) {
            return "{Company_Name: " + companyName + ", Date: " + date + "}";
        } else if (companyName != null && totalPrice != null) {
            return "{Company_Name: " + companyName + ", Total_Price: €" + totalPrice + "}";
        } else if (date != null && totalPrice != null) {
            return "{Date: " + date + ", Total_Price: €" + totalPrice + "}";
        } else if (companyName != null) {
            return "{Company_Name: " + companyName + "}";
        } else if (date != null) {
            return "{Date: " + date + "}";
        } else if (totalPrice != null) {
            return "{Total_Price: €" + totalPrice + "}";
        } else {
            return "{null}";
        }


    }

    public static byte[] adjustContrast(byte[] imageBytes, float contrast) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        BufferedImage adjusted = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                original.getType()
        );

        RescaleOp rescaleOp = new RescaleOp(contrast, 15, null);
        rescaleOp.filter(original, adjusted);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(adjusted, "jpg", baos);
        return baos.toByteArray();
    }


}
