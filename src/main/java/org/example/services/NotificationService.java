package org.example.services;

import org.example.commands.AddCommand;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.example.ozon.ProductInfoCollector;
import org.example.ozon.FetchHtml;

public class NotificationService {

    private final AddCommand addCommand;

    public NotificationService(AddCommand addCommand) {
        this.addCommand = addCommand;

        startNotificationScheduler();
    }

    /**
     * Метод для отправки регулярных уведомлений всем пользователям.
     */
    private void sendPeriodicNotifications() {
        try {
            List<Map<String, Object>> trackedProducts = addCommand.getTrackedProducts();

            for (Map<String, Object> product : trackedProducts) {
                long userId = (long) product.get("user_id");
                String productName = (String) product.get("product_name");
                int currentPrice = (int) product.get("current_price");

                String notificationMessage = String.format(
                        "🔔 Напоминание: Товар '%s' сейчас стоит %d₽. Проверьте, может быть пора обновить ваши цели?",
                        productName, currentPrice
                );

                sendMessageToUser(userId, notificationMessage);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при отправке регулярных уведомлений: " + e.getMessage());
        }
    }


    /**
     * Проверяет текущие цены товаров, обновляет их и отправляет уведомления при снижении цен.
     */
    public void checkPriceUpdatesAndNotify() {
        try {
            // Создаем экземпляр PriceMonitoringService
            PriceMonitoringService priceMonitoringService = new PriceMonitoringService(addCommand);

            List<Map<String, Object>> updatedProducts = priceMonitoringService.checkAndUpdatePrices();

            for (Map<String, Object> product : updatedProducts) {
                boolean priceDropped = (boolean) product.get("price_dropped");

                if (priceDropped) {
                    long userId = (long) product.get("user_id");
                    String productName = (String) product.get("product_name");
                    int currentPrice = (int) product.get("current_price");
                    int desiredPrice = (int) product.get("desired_price");

                    String notificationMessage = String.format(
                            "🔔 Отличные новости! Цена на товар '%s' снизилась до %d₽ (ваша целевая цена: %d₽).",
                            productName, currentPrice, desiredPrice
                    );
                    sendMessageToUser(userId, notificationMessage);
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при проверке цен и отправке уведомлений: " + e.getMessage());
        }
    }


    /**
     * Запуск планировщика уведомлений.
     */
    private void startNotificationScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(
                this::sendPeriodicNotifications,
                0, // Задержка перед первым запуском (в секундах)
                6, // Интервал между выполнениями (в часах)
                TimeUnit.HOURS // Единица измерения времени
        );

        System.out.println("Планировщик уведомлений успешно запущен.");
    }

    private void sendMessageToUser(long userId, String message) {
        System.out.printf("Уведомление для пользователя %d: %s%n", userId, message);
    }


}
