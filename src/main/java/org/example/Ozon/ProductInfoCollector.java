package org.example;

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

            // Extract product ID
            String productId = null;
            Element productIdElement = document.selectFirst("div:contains(Артикул: )");
            if (productIdElement != null) {
                String text = productIdElement.text();
                if (text.contains("Артикул: ")) {
                    productId = text.split("Артикул: ")[1].trim();
                }
            }
            productInfo.put("product_id", productId);

            // Extract product name
            String productName = null;
            Element productNameElement = document.selectFirst("div[data-widget=webProductHeading] h1");
            if (productNameElement != null) {
                productName = productNameElement.text().trim();
            }
            productInfo.put("product_name", productName);

            // Extract product statistics
            String productStatistic = null, productStars = null, productReviews = null;
            Element statisticElement = document.selectFirst("div[data-widget=webSingleProductScore]");
            if (statisticElement != null) {
                productStatistic = statisticElement.text().trim();
                if (productStatistic.contains(" • ")) {
                    String[] parts = productStatistic.split(" • ");
                    productStars = parts[0].trim();
                    productReviews = parts[1].trim();
                }
            }
            productInfo.put("product_statistic", productStatistic);
            productInfo.put("product_stars", productStars);
            productInfo.put("product_reviews", productReviews);

            // Extract product prices
            String ozonCardPrice = null, discountPrice = null, basePrice = null;

            // Ozon card price
            Element ozonCardPriceElement = document.selectFirst("span:contains(c Ozon Картой)");
            if (ozonCardPriceElement != null) {
                Element priceElement = ozonCardPriceElement.parent().selectFirst("div span");
                if (priceElement != null) {
                    ozonCardPrice = priceElement.text().trim();
                }
            }

            // Discount and base prices
            Element priceContainer = document.selectFirst("div[data-widget=webPrice]");
            if (priceContainer != null) {
                var priceElements = priceContainer.select("span");
                if (priceElements.size() == 2) {
                    discountPrice = priceElements.get(0).text().trim();
                    basePrice = priceElements.get(1).text().trim();
                } else if (priceElements.size() == 1) {
                    discountPrice = ozonCardPrice == null ? priceElements.get(0).text().trim() : null;
                    basePrice = discountPrice != null ? discountPrice : null;
                }
            }
            productInfo.put("product_ozon_card_price", ozonCardPrice);
            productInfo.put("product_discount_price", discountPrice);
            productInfo.put("product_base_price", basePrice);

        } catch (Exception e) {
            System.err.println("Error parsing HTML content: " + e.getMessage());
            e.printStackTrace();
        }

        return productInfo;
    }

    public static void main(String[] args) {
        // Example usage
        String sampleHtml = "<html>...</html>"; // Replace with actual HTML content
        Map<String, String> productInfo = collectProductInfo(sampleHtml);

        // Print the extracted product information
        productInfo.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}