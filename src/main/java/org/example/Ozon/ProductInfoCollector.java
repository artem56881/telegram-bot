package org.example.Ozon;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;

public class ProductInfoCollector {

    public static Map<String, String> collectProductInfo(String htmlContent) {
        Map<String, String> productInfo = new HashMap<>();

        try {
            // Parse the HTML content with Jsoup
            Document document = Jsoup.parse(htmlContent);

            Element scriptElement = document.selectFirst("script[type=\"application/ld+json\"]");
            System.out.println(scriptElement.html());

            String ldJson = scriptElement.html();
            String basePrice = ldJson.substring(ldJson.indexOf("\"price\":\"")+9, ldJson.indexOf("\",\"priceC"));

            String itemId = ldJson.substring(ldJson.indexOf("\"sku\":\"")+7, ldJson.lastIndexOf("\"}"));

            System.out.println(basePrice);
            System.out.println(itemId);

            productInfo.put("base_price", basePrice);
            productInfo.put("item_id", itemId);

        } catch (Exception e) {
            System.err.println("Error parsing HTML content: " + e.getMessage());
            e.printStackTrace();
        }

        return productInfo;
    }
}
