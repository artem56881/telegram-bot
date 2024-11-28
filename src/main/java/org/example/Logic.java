package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.commands.AddCommand;
import org.example.commands.ListCommand;
import org.example.commands.RemoveCommand;

import org.example.ozon.ProductInfoCollector;
import org.example.ozon.FetchHtml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logic {

//    ProductItems Items = new ProductItems();


    private final Map<Long, String> userStates = new HashMap<>();

    private final Map<String, Message> commandMap = new HashMap<>();

    private final static Message help = new Message("""
            Бот отслеживает цены на выбранные вами товары на AliExpress и отправляет уведомление, когда цена снижается до желаемого уровня.\s

            Команды:
            /add - добавить товар для отслеживания
            /list - показать список отслеживаемых товаров
            /remove - удалить товар из списка отслеживания
            /help - помощь""",
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
                /help - помощь"""));

        commandMap.put("/tst", new Message("FGDFG"));
    }

    private Message toString(int productPrice) {
        return new Message(String.valueOf(productPrice));
    }


    public Message handleStartCommand(long userId, String userName) {
        UserDatabaseService userDatabaseService = new UserDatabaseService();
        boolean userAdded = userDatabaseService.addUserToDatabase(userId, userName);
        if (userAdded) {
            return new Message("Спасибо, что пользуйтесь нашим ботом!");
        } else {
            return new Message("Мы вас уже запомнили");
        }
    }


    /**
     * @param inputMessage
     * @param userId
     * @return Добавил сохранение состояния пользователя в userStates
     */

    public Message processMessage(String inputMessage, long userId) {

        if (inputMessage.equals("/start")) {
            userStates.put(userId, "DEFAULT"); // Сбрасываем состояние пользователя
            return handleStartCommand(userId, "default username");
        }

        if (inputMessage.equals("/add")) {
            userStates.put(userId, "AWAITING_PRODUCT_LINK"); // Устанавливаем состояние ожидания ссылки
            return new Message("Введите ссылку на товар для отслеживания:");
        }


        if ("AWAITING_PRODUCT_LINK".equals(userStates.get(userId))) {
            if (isValidUrl(inputMessage)) {
                AddCommand addCommand = new AddCommand();

                Map<String, String> productInfo =  ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));

                Long productId = Long.valueOf(productInfo.get("item_id"));
                String productName = productInfo.get("item_name");
                int productPrice = Integer.parseInt(productInfo.get("base_price"));

                String result = addCommand.execute(productId, productName, productPrice);

                userStates.put(userId, "DEFAULT");
                return new Message(result);
            } else {
                return new Message("Введите корректную ссылку на товар или используйте /add, чтобы попробовать снова.");
            }
        }
        if (inputMessage.equals("/remove")) {
            userStates.put(userId, "AWAITING_PRODUCT_LINK_FOR_REMOVAL");
            return new Message("Введите ссылку на товар, который хотите удалить:");
        }

        if ("AWAITING_PRODUCT_LINK_FOR_REMOVAL".equals(userStates.get(userId))) {
            if (isValidUrl(inputMessage)) {
                // Извлечение ID товара из ссылки
                Map<String, String> productInfo =  ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml(inputMessage));

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

}
