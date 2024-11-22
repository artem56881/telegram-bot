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
import org.example.Config.DatabaseConnection;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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


    public Logic(){

        commandMap.put("/start", help);

        commandMap.put("/add", new Message("Введите ID товара для добавления в список отслеживания."));

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



    public Message handleStartCommand(long userId, String userName) {
        UserDatabaseService userDatabaseService = new UserDatabaseService();
        boolean userAdded = userDatabaseService.addUserToDatabase(userId, userName);
        if (userAdded) {
            return new Message("Спасибо, что пользуйтесь нашим ботом!");
        } else {
            return new Message("Мы вас уже запомнили");
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
        // Parse the JSON string
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Navigate through the JSON to extract the promotion price
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


    public Message processMessage(String inputMessage, long userId) {

        if (inputMessage.equals("/start")) {
            //userName = update.getMessage().getFrom().getFirstName();

            return handleStartCommand(userId, "default username");
        }

        if (commandMap.containsKey(inputMessage)) {
            return commandMap.get(inputMessage);
        }

        String response = ApiResponse(inputMessage);
        System.out.println(response);

        String errorCode = checkForJsonDataError(response);
        System.out.println(errorCode);

        if (errorCode.equals("error")) {
            return new Message("error");
        }

        String price = extractPromotionPrice(response);
        System.out.println(price);

        return new Message(errorCode + ": " + price);
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
}