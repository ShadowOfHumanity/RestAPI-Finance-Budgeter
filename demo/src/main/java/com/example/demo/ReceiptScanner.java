package com.example.demo;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

class ReceiptScanner {
    // Replace these with your values from Azure portal
    private static String key = System.getenv("AZURE_VISION_KEY");
    private static String endpoint = System.getenv("AZURE_VISION_ENDPOINT");

    private static final String uriBase = endpoint + "/vision/v3.1/ocr";
    private static final String imageToAnalyze = "C:\\Users\\daria.THE_FLASH\\Downloads\\reciept3.jpg";

    public ReceiptScanner() {
        System.out.println("key" + key);
        System.out.println("endpoint" + endpoint);
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        try {
            // Read the local image file
            File imageFile = new File(imageToAnalyze);
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

            URIBuilder builder = new URIBuilder(uriBase);
            builder.setParameter("visualFeatures", "Categories,Description,Color");

            // getting request
            HttpPost request = new HttpPost(builder.build());


            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", key);

            // Send the actual image IN bytes
            ByteArrayEntity requestEntity = new ByteArrayEntity(imageBytes);
            request.setEntity(requestEntity);

            // RestApi call and Response
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Formatting the JSON response
                String jsonString = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(jsonString);
                System.out.println("REST Response:\n");
                System.out.println(json.toString(2));
                extractInfo(json);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void extractInfo(JSONObject json) {
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
                if ((totalPrice == null ) && (text.contains("Total") || text.contains("TOTAL") || text.contains("total") || text.contains("Total:") || text.contains("EUR")|| text.contains("Amount") || text.contains("AMOUNT") || text.contains("amount") || text.contains("Due") || text.contains("DUE") || text.contains("due") || text.contains("TOTAL Euro") || text.contains("Total Euro") || text.contains("total Euro") || text.contains("TOTAL EUR") || text.contains("Total EUR") || text.contains("total EUR") )) {

                    totalPrice = lines.getJSONObject(j + 1).getJSONArray("words").getJSONObject(1).getString("text");
                }
                else {
                    for (int k = 0; k < words.length(); k++) {
                        String word = words.getJSONObject(k).getString("text");
                        if (word.matches("€?\\d+\\.\\d{2}")) { // match is a number with opt € symbol
                            double amount = Double.parseDouble(word.replace("€", ""));
                            totalPrice = String.format("%.2f", amount);

                        }
                    }
                }
            }


        }


        System.out.println("Company Name: " + companyName);
        System.out.println("Date: " + date);
        System.out.println("Total Price: €" + totalPrice);

        System.out.println();
    }
}
