package org.example;

//import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.example.entity.Button;
import org.example.entity.Message;
import org.example.commands.AddCommand;
import org.example.commands.ListCommand;
import org.example.commands.RemoveCommand;


import org.example.services.UserDatabaseService;
import org.example.services.NotificationService;
import org.example.ozon.ProductInfoCollector;
import org.example.ozon.FetchHtml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logic {

    private final Map<Long, String> userStates = new HashMap<>();

    private final Map<String, Message> commandMap = new HashMap<>();

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    private final static Message help = new Message("""
            Бот отслеживает цены на выбранные вами товары на Ozon и отправляет уведомление, когда цена снижается до желаемого уровня.\s

            Команды:    
            /add - добавить товар для отслеживания
            /list - показать список отслеживаемых то`варов
            /remove - удалить товар из списка отслеживания
            /help - помощь
            /check_price - вывести цену товара по ссылке""",
            List.of(new Button("start", "start")));


    public Logic() {

        commandMap.put("/start", help);

        commandMap.put("/add", new Message("Введите ссылку на товар"));

        commandMap.put("/list", new Message("Список отслеживаемых товаров:"));

        commandMap.put("/remove", new Message("Введите ID товара для удаления из списка отслеживания."));

        commandMap.put("/help", new Message("""
                Список доступных команд:
                /start - начать работу бота
                /add - добавить товар для отслеживания
                /list - показать список отслеживаемых товаров
                /remove - удалить товар из списка отслеживания
                /help - помощь
                /check_price - вывести цену товара по ссылке"""));

        commandMap.put("/tst", new Message("FGDFG"));
    }


    public Message handleStartCommand(long userId, String userName) {
        UserDatabaseService userDatabaseService = new UserDatabaseService();
        boolean userAdded = userDatabaseService.addUserToDatabase(userId, userName);
        List<Button> buttons = List.of(
                new Button("Добавить товар для отслеживания", "/add"),
                new Button("Показать список отслеживаемых товаров", "/list"),
                new Button("Удалить товар из списка отслеживания", "/remove"),
                new Button("Помощь", "/help"),
                new Button("Проверить цену товара", "/check_price")
        );
        if (userAdded) {
            return new Message("Спасибо, что пользуйтесь нашим ботом!", buttons);
        } else {
            return new Message("Мы вас уже запомнили", buttons);
        }
    }


    /**
     * @return Добавил сохранение состояния пользователя в userStates
     */
    public Message processMessage(String inputMessage, long userId) {

        if (inputMessage.equals("/start")) {
            userStates.put(userId, "DEFAULT"); // Сбрасываем состояние пользователя
            startPeriodicNotifications();
            return handleStartCommand(userId, "default username");
        }

        if (inputMessage.equals("/add")) {
            userStates.put(userId, "AWAITING_PRODUCT_LINK"); // Устанавливаем состояние ожидания ссылки
            return new Message("Введите ссылку на товар для отслеживания:");
        }
        AddCommand addCommand = new AddCommand();
        if ("AWAITING_PRODUCT_LINK".equals(userStates.get(userId))) {
            if (isValidUrl(inputMessage)) {
                Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));

                Long productId = Long.valueOf(productInfo.get("item_id"));
                String productName = productInfo.get("item_name");
                int productPrice = Integer.parseInt(productInfo.get("base_price"));

                String result = addCommand.execute(productId, productName, productPrice);


                userStates.put(userId, "DEFAULT");
                return new Message(result);
            } else {

                List<Button> buttons = List.of(
                        new Button("/add", "/add"));

                return new Message("Введите корректную ссылку на товар или используйте /add, чтобы попробовать снова.", buttons);
            }
        }

        if (inputMessage.equals("/remove")) {
            userStates.put(userId, "AWAITING_PRODUCT_LINK_FOR_REMOVAL");
            return new Message("Введите ссылку на товар, который хотите удалить:");
        }

        if ("AWAITING_PRODUCT_LINK_FOR_REMOVAL".equals(userStates.get(userId))) {
            if (isValidUrl(inputMessage)) {
                // Извлечение ID товара из ссылки
                Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));

                String productId = productInfo.get("item_id");

                if (productId != null) {
                    RemoveCommand removeCommand = new RemoveCommand();
                    //удаляем товар по ID
                    String result = removeCommand.execute(Long.valueOf(productId));

                    // Сбрасываем состояние пользователя
                    userStates.put(userId, "DEFAULT");
                    return new Message(result);
                } else {
                    return new Message("Не удалось найти товар по указанной ссылке.");
                }
            } else {
                return new Message("Пожалуйста, введите корректную ссылку на товар.");
            }
        }

        if (inputMessage.equals("/list")) {
            userStates.put(userId, "DEFAULT"); // Сбрасываем состояние
            ListCommand listCommand = new ListCommand(new HashMap<>());
            String result = listCommand.execute();
            return new Message(result);
        }

        if (inputMessage.equals("/check_price")) {
            // Установить состояние ожидания ссылки от пользователя
            userStates.put(userId, "AWAITING_PRODUCT_LINK_FOR_CHECK");
            return new Message("Введите ссылку на товар, цену которого вы хотите проверить:");
        }

        if ("AWAITING_PRODUCT_LINK_FOR_CHECK".equals(userStates.get(userId))) {
            if (isValidUrl(inputMessage)) {
                try {
                    Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));
                    String productName = productInfo.get("item_name");
                    int currentPrice = Integer.parseInt(productInfo.get("base_price"));

                    // Создаем экземпляр NotificationService и проверяем цены
                    NotificationService notificationService = new NotificationService(addCommand);
                    notificationService.checkPriceUpdatesAndNotify(); // Проверяем обновления цен и отправляем уведомления

                    userStates.put(userId, "DEFAULT");
                    return new Message("Текущая цена товара: " + currentPrice + "₽");
                } catch (Exception e) {
                    System.err.println("Ошибка при проверке цены: " + e.getMessage());
                    userStates.put(userId, "DEFAULT");
                    return new Message("Произошла ошибка при проверке цены. Убедитесь в правильности ссылки или попробуйте позже.");
                }
            } else {
                return new Message("Введите корректную ссылку на товар.");
            }
        }

        if ("AWAITING_PRODUCT_LINK_FOR_CHECK".equals(userStates.get(userId))) {
            if (isValidUrl(inputMessage)) {
                try {
                    Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));

                    String productName = productInfo.get("item_name");
                    int currentPrice = Integer.parseInt(productInfo.get("base_price"));

                    userStates.put(userId, "DEFAULT");


                    return new Message("Текущая цена товара: " + currentPrice + "₽");
                } catch (Exception e) {
                    System.err.println("Ошибка при проверке цены: " + e.getMessage());
                    userStates.put(userId, "DEFAULT");
                    return new Message("Произошла ошибка при проверке цены. Убедитесь в правильности ссылки или попробуйте позже.");
                }
            } else {
                return new Message("Введите корректную ссылку на товар.");
            }
        }

        if (inputMessage.equals("/stop_notifications")) {
            stopPeriodicNotifications();
            return new Message("Периодические уведомления остановлены.");
        }


        if (commandMap.containsKey(inputMessage)) {
            return commandMap.get(inputMessage);
        }

        return new Message("Неизвестная команда. Используйте /help для списка доступных команд.");
    }


    private boolean isValidUrl(String url) {
        String regex = "https?://\\S+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }


    public void startPeriodicNotifications() {
        if (scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        scheduler.isShutdown();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                AddCommand add = new AddCommand();
                NotificationService notify = new NotificationService(add);
                notify.checkPriceUpdatesAndNotify();
            } catch (Exception e) {
                System.err.println("Ошибка при отправке периодических уведомлений: " + e.getMessage());
            }
        }, 0, 10, TimeUnit.MINUTES);
    }


    public void stopPeriodicNotifications() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }


}
