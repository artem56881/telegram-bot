package org.example.services;

import org.example.commands.AddCommand;
import org.example.ozon.ProductInfoCollector;
import org.example.ozon.FetchHtml;

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

            List<Map<String, Object>> trackedProducts = addCommand.getTrackedProducts();

            for (Map<String, Object> product : trackedProducts) {
                String productUrl = (String) product.get("product_url");
                int desiredPrice = (int) product.get("desired_price");
                int currentPrice = (int) product.get("current_price");

                // Получение обновленной информации о товаре
                Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(productUrl));
                int updatedPrice = Integer.parseInt(productInfo.get("base_price"));

                if (updatedPrice != currentPrice) {
                    addCommand.updateProductPrice(productUrl, updatedPrice);

                    // Обновление данных в коллекции для использования в дальнейшем
                    product.put("current_price", updatedPrice);
                    product.put("price_dropped", updatedPrice <= desiredPrice); // Флаг снижения цены
                }
            }
            return trackedProducts; // Возвращаем список обновленных товаров
        } catch (Exception e) {
            System.err.println("Ошибка при проверке цен: " + e.getMessage());
            throw new RuntimeException("Ошибка проверки цен", e);
        }
    }
}
