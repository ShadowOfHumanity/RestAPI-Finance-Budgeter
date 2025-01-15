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

import java.io.File;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecieptPhotoScanner {
    private static String key = System.getenv("AZURE_VISION_KEY");
    private static String endpoint = System.getenv("AZURE_VISION_ENDPOINT");

    private static final String uriBase = endpoint + "/vision/v3.1/ocr";
    private static String imageToAnalyze;


    public static String scanReceipt(ByteArrayEntity requestEntity){
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        try {
            URIBuilder builder = new URIBuilder(uriBase);
            builder.setParameter("visualFeatures", "Categories,Description,Color");

            // getting request
            HttpPost request = new HttpPost(builder.build());

            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", key);

            request.setEntity(requestEntity);

            // RestApi call and Response
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
                if (companyName == null) {
                    // Match common company name patterns, including more business types and suffixes
                    Pattern companyPattern = Pattern.compile("(?i)(\\b[A-Za-z]+(?:\\s+[A-Za-z]+)*\\s*(Pharmacy|Store|Shop|Clinic|Market|Mini\\s?Market|Grocery|Retail|Supermarket|Laboratory|Pharmaceuticals|Corporation|Inc|Ltd|Enterprise|Services|Consulting|Group|Systems|Technologies|International|Associates|Industries|Co|LLC|LLP|Network|Solutions|Works|Firm|Manufacturing|Wholesale|Supplies|Furniture|Construction|Import|Export|Foods|Toys|Jewelry|Boutique|Bakery|Electronics|Automotive|Cosmetics|Textiles|Crafts|Apparel|Technology|Stationary|Confectionary|Beverages|Discount|Convenience|Pet\\s?Store|Health|Vape\\s?Shop|Mobile|Fashion|Clothing|Sport|Outdoors|Toy\\s?Store|Cellular|Optical|Bedding|Home\\s?Goods|Tech|Luxury|Restaurant|Bar|Café|Catering|Florist|Gifts|Home\\s?Improvements|Arts\\s?Crafts)\\b)");
                    Matcher companyMatcher = companyPattern.matcher(text);

                    if (companyMatcher.find()) {
                        companyName = companyMatcher.group(0).trim();  // Capture the whole company name
                    }
                }


                // Check for date (assuming date format is DD/MM/YYYY) aka EUROPEAN FORMAT
                if (date == null && text.matches("\\d{2}/\\d{2}/\\d{4}")) {
                    date = text;
                }

                // get total price, if it has "Total before it"

                if ((totalPrice == null) && (text.contains("Total") || text.contains("TOTAL") || text.contains("total") || text.contains("Total:") || text.contains("EUR") || text.contains("Amount") || text.contains("AMOUNT") || text.contains("amount") || text.contains("Due") || text.contains("DUE") || text.contains("due") || text.contains("TOTAL Euro") || text.contains("Total Euro") || text.contains("total Euro") || text.contains("TOTAL EUR") || text.contains("Total EUR") || text.contains("total EUR"))) {

                    totalPrice = lines.getJSONObject(j + 1).getJSONArray("words").getJSONObject(1).getString("text");
                } else {
                    for (int k = 0; k < words.length(); k++) {
                        String word = words.getJSONObject(k).getString("text");
                        if (word.matches("€?\\d+\\.\\d{2}")) { // match is a number with opt € symbol
                            double amount = Double.parseDouble(word.replace("€", ""));
                            if (totalPrice != null) {
                                double currentTotal = Double.parseDouble(totalPrice);
                                if (currentTotal < amount) {
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


}
