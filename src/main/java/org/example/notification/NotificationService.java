package org.example.notification;

import org.example.commands.AddCommand;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {

    private final AddCommand addCommand;

    public NotificationService(AddCommand addCommand) {
        this.addCommand = addCommand;

        // Запуск периодических уведомлений
        startNotificationScheduler();
    }

    /**
     * Метод для отправки уведомлений.
     */
    private void sendPeriodicNotifications() {
        System.out.println("🔔 Привет! Это регулярное уведомление для всех пользователей.");
    }

    /**
     * Запуск планировщика уведомлений.
     */
    private void startNotificationScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Запуск задачи каждые 6 часов
        scheduler.scheduleAtFixedRate(
                this::sendPeriodicNotifications, // Задача для выполнения
                0, // Задержка перед первым запуском (в секундах)
                6, // Интервал между выполнениями (в часах)
                TimeUnit.HOURS // Единица измерения времени
        );

        System.out.println("Планировщик уведомлений успешно запущен.");
    }

    /**
     * Проверяет текущие цены товаров и отправляет уведомления пользователям.
     */
    public void checkPriceUpdatesAndNotify() {
        // Логика проверки цен, если она нужна дополнительно.
        System.out.println("Проверка цен и отправка уведомлений...");
    }
}
