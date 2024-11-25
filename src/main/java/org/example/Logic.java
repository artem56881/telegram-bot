package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.example.ozon.ProductPrice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logic {


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


    public Message processMessage(String inputMessage, long userId) {

        if (inputMessage.equals("/start")) {
            //userName = update.getMessage().getFrom().getFirstName();
            return handleStartCommand(userId, "default username");

        }
        if (commandMap.containsKey(inputMessage)) {
            return commandMap.get(inputMessage);
        }

        if (isValidUrl(inputMessage)) {
            int price = getProductPrice(inputMessage);
            String priceText = "Цена товара: " + price + " ₽";
            return new Message(priceText);
        }

        return null;
    }

    private boolean isValidUrl(String url) {
        String regex = "https?://\\S+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }


    public static int getProductPrice(String inputLink) {
        ProductPrice productPrice = new ProductPrice();
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
}
