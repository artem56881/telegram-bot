package com.github.ArtemAndrew.PriceMonitoringBot.services;

import com.github.ArtemAndrew.PriceMonitoringBot.commands.AddCommand;
import com.github.ArtemAndrew.PriceMonitoringBot.MyTelegramBot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {
    private final MyTelegramBot tgBot;
    private final AddCommand addCommand;

    public NotificationService(AddCommand addCommand, MyTelegramBot tgBot) {
        this.addCommand = addCommand;
        this.tgBot = tgBot;

        startNotificationScheduler();
    }

    /**
     * Метод для отправки регулярных уведомлений всем пользователям.
     */
    private void sendPeriodicNotifications() {
        try {
            String testUsrid = "asdasd";
            List<Map<String, Object>> trackedProducts = addCommand.getTrackedProducts(testUsrid);

            for (Map<String, Object> product : trackedProducts) {
                String userId = (String) product.get("user_id");
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
                int currentPrice = (int) product.get("current_price");
//                int desiredPrice = (int) product.get("desired_price");
                int updatedPrice = (int) product.get("price");
                String productName = (String) product.get("name");
                String notificationMessage;
                String userId = (String) product.get("user_id");
                if (currentPrice < updatedPrice) {
                    notificationMessage = "🔔 Отличные новости! Цена товара " + productName + " снизилась до " + updatedPrice + "!";
                    sendMessageToUser(userId, notificationMessage);
                }
//                else if (currentPrice == updatedPrice) {
//                    notificationMessage = "Цена товара " + productName + " не поменялась.";
//                }
                else if (currentPrice > updatedPrice) {
                    notificationMessage = "Цена товара " + productName + " повысилась!.";
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
                this::checkPriceUpdatesAndNotify,
                0, // Задержка перед первым запуском (в секундах)
                15, // Интервал между выполнениями (в часах)
                TimeUnit.SECONDS // Единица измерения времени
        );

        System.out.println("Планировщик уведомлений успешно запущен.");
    }

    private void sendMessageToUser(String userId, String message) {
        System.out.printf("Уведомление для пользователя" + userId + message);
        tgBot.sendNotification(userId, message);
    }

}
