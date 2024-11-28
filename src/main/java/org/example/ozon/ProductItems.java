package org.example.ozon;

import jnr.ffi.annotations.In;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import static org.example.ozon.ProductInfoCollector.collectProductInfo;


public class ProductItems {

    public String getPrice(String url) {
        // Create an HttpClient instance
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Create a GET request
            HttpGet httpGet = new HttpGet(url);
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

    public Long getId(@NotNull String url) {
//        String[] TempArr = url.split("/");
//
//        String[] IdStroke = Arrays.toString(TempArr).split("-");
//
//        String test = Arrays.toString(IdStroke);
//
//        System.out.println(test);
//        for (String s : IdStroke) {
//            try {
//                System.out.println(Long.parseLong(s));
//            } catch (NumberFormatException e) {
//                System.out.println("gool");
//            }
//        }
        String[] parts = url.split("/");
        String lastPart = parts[parts.length - 2];
        String[] idParts = lastPart.split("-");

        Long id = Long.parseLong(idParts[idParts.length - 1]);

        return id;
    }


    public String getName(String url) {
        String[] parts = url.split("/");
        String[] idParts = parts[parts.length - 2].split("-");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idParts.length - 1; i++) {
            sb.append(idParts[i]);
            if (i < idParts.length - 2) {
                sb.append("-");
            }
        }

        String b = sb.toString();
        return b;
    }

}


