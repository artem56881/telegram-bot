package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.Ozon.ProductPrice;

import java.io.IOException;
import java.util.HashMap;

public class Logic {


    private final HashMap<String, String> commandMap = new HashMap<>();


    public Logic() {

        commandMap.put("/start", """
                Бот отслеживает цены на выбранные вами товары на AliExpress и отправляет уведомление, когда цена снижается до желаемого уровня.\s

                Команды:
                /add - добавить товар для отслеживания
                /list - показать список отслеживаемых товаров
                /remove - удалить товар из списка отслеживания
                /help - помощь""");

        commandMap.put("/add", "Введите ссылку на товар для добавления в список отслеживания.");

        commandMap.put("/list", "Список отслеживаемых товаров:");

        commandMap.put("/remove", "Введите ID товара для удаления из списка отслеживания.");

        commandMap.put("/help", """
                Список доступных команд:
                /start - начать работу бота
                /add - добавить товар для отслеживания
                /list - показать список отслеживаемых товаров
                /remove - удалить товар из списка отслеживания
                /help - помощь""");
        commandMap.put("/цена", "Показывает цену определённого товара по ссылке");
    }


    public String handleStartCommand(int userId, String userName) {
        UserDatabaseService userDatabaseService = new UserDatabaseService();
        boolean userAdded = userDatabaseService.addUserToDatabase(userId, userName);

        if (userAdded) {
            return "Спасибо, что пользуйтесь нашим ботом!";
        } else {
            return "Мы вас уже запомнили";
        }
    }


    public String ApiResponse(String itemId) {
        OkHttpClient client = new OkHttpClient();
        String itemLink = "https://aliexpress-datahub.p.rapidapi.com/item_detail_2?itemId=" + itemId;
        Request request = new Request.Builder()
                .url(itemLink)
                .get()
                .addHeader("x-rapidapi-key", System.getenv("ALITOKEN"))
                .addHeader("x-rapidapi-host", "aliexpress-datahub.p.rapidapi.com")
                .build();
        System.out.println(itemLink);
        try {
            Call call = client.newCall(request);
            Response response = call.execute(); // Blocking call, may throw IOException

            if (response.isSuccessful()) {
                return response.body().string(); // Return the body as a string
            } else {
                return "Error: " + response.code(); // Handle error responses
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Request failed: " + e.getMessage(); // Handle failure
        }
    }


    public String checkForJsonDataError(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        String errorCode;
        errorCode = jsonObject
                .getAsJsonObject("result")
                .getAsJsonObject("status")
                .get("data")
                .getAsString();

        return errorCode;
    }


    public String extractPromotionPrice(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        String promotionPrice = jsonObject
                .getAsJsonObject("result")
                .getAsJsonObject("item")
                .getAsJsonObject("sku")
                .getAsJsonObject("def")
                .get("promotionPrice")
                .getAsString();

        return "Promotion Price: $" + promotionPrice;
    }


    public String processMessage(String messageText, int userId, String userName) {
        if (messageText != null) {
            switch (messageText) {
                case "/add":
                    // Логика для добавления товара
                    return commandMap.get("/add");

                case "/list":
                    // Логика для вывода списка товаров
                    return commandMap.get("/list");

                case "/remove":
                    // Логика для удаления товара
                    return commandMap.get("/remove");

                case "/help":
                    // Логика для помощи
                    return commandMap.get("/help");

                case "/цена":
                    return to_String(getProductPrice());

                case "абоба":
                    return "test";
                default:
                    return "Команда введена не верно";
            }
        } else {
            String response = ApiResponse(messageText);
            System.out.println(response);

            String errorCode = checkForJsonDataError(response);
            System.out.println(errorCode);

            if (errorCode.equals("error")) {
                return "error";
            }

            String price = extractPromotionPrice(response);
            System.out.println(price);

            return errorCode + ": " + price;
        }
    }

    private String to_String(int productPrice) {
        String res = String.valueOf(productPrice);
        System.out.println(res);
        return res;
    }

    public static int getProductPrice() {
        ProductPrice responce = new ProductPrice();
        Pattern pattern = Pattern.compile("\\d+(?:\\s\\d+)*");
        Matcher matcher = pattern.matcher((CharSequence) responce.GetPrice());
        int price = 0;
        if (matcher.find()) {
            String priceString = matcher.group();
            priceString = priceString.replaceAll("\\s", "");
            price = Integer.parseInt(priceString);
        }
        return price;
    }

}