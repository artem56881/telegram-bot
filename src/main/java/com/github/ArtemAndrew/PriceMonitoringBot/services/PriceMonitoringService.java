package com.github.ArtemAndrew.PriceMonitoringBot.services;

import com.github.ArtemAndrew.PriceMonitoringBot.commands.AddCommand;
import com.github.ArtemAndrew.PriceMonitoringBot.ozon.ProductInfoCollector;
import com.github.ArtemAndrew.PriceMonitoringBot.ozon.FetchHtml;

import java.util.List;
import java.util.Map;

public class PriceMonitoringService {

    private final AddCommand addCommand;

    public PriceMonitoringService(AddCommand addCommand) {
        this.addCommand = addCommand;
    }

    /**
     * Проверяет текущие цены отслеживаемых товаров и обновляет базу данных, если цена изменилась.
     *
     * @return Список товаров с обновленными ценами и флагом, сигнализирующим о снижении.
     */
    public List<Map<String, Object>> checkAndUpdatePrices() {
        try {
            List<Map<String, Object>> trackedProducts = addCommand.getAllTrackedProducts();

            for (Map<String, Object> product : trackedProducts) {
                Long productId = (Long) product.get("product_id");
                String productName = (String) product.get("name");
                int desiredPrice = (int) product.get("desired_price");
                int currentPrice = (int) product.get("price");

                Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(productName));
                int updatedPrice = Integer.parseInt(productInfo.get("base_price"));

                if (updatedPrice != currentPrice) {
                    addCommand.updateProductPrice(productId, updatedPrice);

                    product.put("price", updatedPrice);
                    product.put("price_dropped", updatedPrice <= desiredPrice); // Флаг снижения цены
                }
            }
            return trackedProducts;
        } catch (Exception e) {
            System.err.println("Ошибка при проверке цен: " + e.getMessage());
            throw new RuntimeException("Ошибка проверки цен", e);
        }
    }
}
