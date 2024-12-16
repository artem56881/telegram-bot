package com.github.ArtemAndrew.PriceMonitoringBot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.ArtemAndrew.PriceMonitoringBot.entity.Button;
import com.github.ArtemAndrew.PriceMonitoringBot.entity.Message;
import com.github.ArtemAndrew.PriceMonitoringBot.entity.UserState;
import com.github.ArtemAndrew.PriceMonitoringBot.commands.AddCommand;
import com.github.ArtemAndrew.PriceMonitoringBot.commands.ListCommand;
import com.github.ArtemAndrew.PriceMonitoringBot.commands.RemoveCommand;
import com.github.ArtemAndrew.PriceMonitoringBot.services.UserDatabaseService;
import com.github.ArtemAndrew.PriceMonitoringBot.services.NotificationService;
import com.github.ArtemAndrew.PriceMonitoringBot.ozon.ProductInfoCollector;
import com.github.ArtemAndrew.PriceMonitoringBot.ozon.FetchHtml;

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
            /list - показать список отслеживаемых товаров
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

    public Message handleStartCommand() {
//        UserDatabaseService userDatabaseService = new UserDatabaseService();
//        boolean userAdded = userDatabaseService.addUserToDatabase(userId, userName);
        List<Button> buttons = List.of(
                new Button("Добавить товар для отслеживания", "/add"),
                new Button("Показать список отслеживаемых товаров", "/list"),
                new Button("Удалить товар из списка отслеживания", "/remove"),
                new Button("Помощь", "/help"),
                new Button("Проверить цену товара", "/check_price")
        );

        return new Message("Спасибо, что пользуйтесь нашим ботом!", buttons);
//        if (userAdded) {
//        } else {
//            return new Message("Мы вас уже запомнили", buttons);
//        }
    }

    public Message processMessage(String inputMessage, long userId) {
        String default_state = UserState.DEFAULT.getUserState();
        if (inputMessage.equals("/start")) {
            userStates.put(userId, default_state); // Сбрасываем состояние пользователя
            startPeriodicNotifications();
            return handleStartCommand();
        }

        if (inputMessage.equals("/add")) {
            String await_link = UserState.AWAITING_PRODUCT_LINK.getUserState();
            userStates.put(userId, await_link); // Устанавливаем состояние ожидания ссылки
            return new Message("Введите ссылку на товар для отслеживания:");
        }

        AddCommand addCommand = new AddCommand();
        if (UserState.AWAITING_PRODUCT_LINK.getUserState().equals(userStates.get(userId))) {
            List<Button> buttons = List.of(
                    new Button("/add", "/add"));
            if (isInvalidUrl(inputMessage)) { //проверка валидности ссылки
                userStates.put(userId, default_state); // Сбрасываем состояние пользователя
                return new Message("Некоректная ссылка, нажмите /add чтобы попробовать снова", buttons);
            }
            try {
                Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));
                Long productId = Long.valueOf(productInfo.get("item_id"));
                String productName = productInfo.get("item_name");
                int productPrice = Integer.parseInt(productInfo.get("base_price"));
                String result = addCommand.execute(Long.toString(userId), productId, productName, productPrice, inputMessage);
                userStates.put(userId, UserState.DEFAULT.getUserState());
                return new Message(result);
            }
            catch (Exception e) {
                System.err.println("Ошибка парсинга сайта: " + e.getMessage());
                userStates.put(userId, default_state); // Сбрасываем состояние пользователя
                return new Message("Не удалось получить доступ к товару, проверьте правильность ссылки и нажмите /add чтобы попробовать снова", buttons);
            }
        }

        if (inputMessage.equals("/remove")) {
            userStates.put(userId, UserState.AWAITING_PRODUCT_LINK_FOR_REMOVAL.getUserState());
            ListCommand listCommand = new ListCommand();
            String result = listCommand.execute(Long.toString(userId));
            return new Message(result + "\n \nВведите ID товара который вы хотите удалить.");
        }

        if (UserState.AWAITING_PRODUCT_LINK_FOR_REMOVAL.getUserState().equals(userStates.get(userId))) { //Ожидаем ID товара для удаления от пользователя.
            if (!addCommand.isProductExists(Long.valueOf(inputMessage))){
                userStates.put(userId, default_state); // Сбрасываем состояние пользователя
                List<Button> buttons = List.of(
                        new Button("/remove", "/remove"));
                return new Message("Товара с таким ID не существует. Нажмите /remove чтобы попробовать снова.", buttons);
            }
            RemoveCommand removeCommand = new RemoveCommand();
            //удаляем товар по ID
            String result = removeCommand.execute(Long.toString(userId), Long.valueOf(inputMessage)); //

            // Сбрасываем состояние пользователя
            userStates.put(userId, UserState.DEFAULT.getUserState());
            return new Message(result);
        }

        if (inputMessage.equals("/list")) {
            userStates.put(userId, UserState.DEFAULT.getUserState()); // Сбрасываем состояние
            ListCommand listCommand = new ListCommand();
            String result = listCommand.execute(Long.toString(userId));
            return new Message(result);
        }

        if (inputMessage.equals("/check_price")) {
            // Установить состояние ожидания ссылки от пользователя
            userStates.put(userId, UserState.AWAITING_PRODUCT_LINK_FOR_CHECK.getUserState());
            return new Message("Введите ссылку на товар, цену которого вы хотите проверить:");
        }

        if (UserState.AWAITING_PRODUCT_LINK_FOR_CHECK.getUserState().equals(userStates.get(userId))) {
            List<Button> buttons = List.of(
                    new Button("/check_price", "/check_price"));
            if (isInvalidUrl(inputMessage)) { //проверка валидности ссылки
                userStates.put(userId, default_state); // Сбрасываем состояние пользователя
                return new Message("Некоректная ссылка на товар, Нажмите /check_price чтобы попробовать снова", buttons);

            }
            try {
                Map<String, String> productInfo = ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));
                int currentPrice = Integer.parseInt(productInfo.get("base_price"));

                userStates.put(userId, UserState.DEFAULT.getUserState());
                return new Message("Текущая цена товара: " + currentPrice + "₽");
            } catch (Exception e) {
                System.err.println("Ошибка при проверке цены: " + e.getMessage());
                userStates.put(userId, UserState.DEFAULT.getUserState());
                return new Message("Произошла ошибка при проверке цены. Убедитесь в правильности ссылки или попробуйте позже.", buttons);
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

    private boolean isInvalidUrl(String url) {
        String regex = "https?://\\S+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return !matcher.matches();
    }

    public void startPeriodicNotifications() {
        if (scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
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
