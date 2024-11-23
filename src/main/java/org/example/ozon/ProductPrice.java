package org.example.ozon;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import static org.example.ozon.ProductInfoCollector.collectProductInfo;


public class ProductPrice {

    public String getPrice(String inputLink) {
        // Create an HttpClient instance
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create a GET request
            HttpGet httpGet = new HttpGet(inputLink);
//            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                String htmlContent = EntityUtils.toString(response.getEntity());
//                System.out.println(htmlContent);
                String ozonCardPrice = collectProductInfo(htmlContent).get("product_ozon_card_price");

                return ozonCardPrice;
            }
        } catch (Exception e) {
            System.err.println("Error fetching the HTML content: " + e.getMessage());
//            e.printStackTrace();
        }
        return null;
    }
}