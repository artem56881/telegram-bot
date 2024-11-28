package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.units.qual.A;
import org.example.commands.AddCommand;
import org.example.commands.ListCommand;
import org.example.commands.RemoveCommand;
import org.example.ozon.ProductItems;
import org.python.antlr.ast.Str;

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
                Long productId = getProductID(inputMessage);
                String productName = getProductName(inputMessage);
                int productPrice = getProductPrice(inputMessage);

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
                // Извлечение имени товара из ссылки
                String productName = getProductName(inputMessage);

                if (productName != null) {
                    RemoveCommand removeCommand = new RemoveCommand();
                    String result = removeCommand.execute(productName);

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


    public static int getProductPrice(String inputLink) {
        ProductItems productPrice = new ProductItems();
        String priceString = productPrice.getPrice(inputLink);

        if (priceString == null || priceString.isEmpty()) {
            System.err.println("Failed to retrieve product price. Price string is null or empty.");
            return 0;
        }

        // Регулярное выражение для извлечения числового значения цены
        System.out.println(priceString);
        Pattern pattern = Pattern.compile("\\d+(?:\\s\\d+)*");
        Matcher matcher = pattern.matcher(priceString);
        int price = 0;

        if (matcher.find()) {
//          priceString = matcher.group().replaceAll("\\s", "");
            priceString = priceString.replaceAll("₽", " ");
            priceString = priceString.replaceAll(" ", "");
            priceString = priceString.replaceAll(" ", "");
            System.out.println(priceString);
            try {
                price = Integer.parseInt(priceString);
            } catch (NumberFormatException e) {
                System.err.println("Error parsing price string to integer: " + e.getMessage());
            }
        } else {
            System.err.println("Price not found in the response string.");
        }

        return price;
    }

    public static Long getProductID(String inputLink) {
        try {
            ProductItems items = new ProductItems();
            return items.getId(inputLink);
        } catch (Exception e) {
            System.err.println("Ошибка при получении ID продукта: " + e.getMessage());
            return null;
        }
    }


    public static String getProductName(String inputLink) {
        ProductItems Items = new ProductItems();
        return Items.getName(inputLink);
    }

}
