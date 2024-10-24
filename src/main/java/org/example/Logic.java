package org.example;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.Config.DatabaseConnection;
import org.json.JSONException;
import org.json.JSONObject;

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

        commandMap.put("/tst", new Message(":DDDD"));
    }

    public String reverse(String message){
        StringBuilder reveres = new StringBuilder();
        char[] reversedMessageArray = message.toCharArray();

        for (int i = reversedMessageArray.length - 1; i >= 0; i--) {
            reveres.append(reversedMessageArray[i]);
        }
        System.out.println(reveres);
        return reveres.toString();
    }


    public Message handleStartCommand(long userId) {
        if (addUserToDatabase(userId)) {
            return new Message("Вы успешно добавлены в базу данных!");
        } else {

            return commandMap.get("/start");
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
        // Parse the JSON string
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

        // Navigate through the JSON to extract the promotion price
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

            return handleStartCommand(userId);
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


    private boolean addUserToDatabase(long userId) {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        String sql = "INSERT INTO users (id, username) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, (int) userId);
            preparedStatement.setString(2, "null");
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
    }

    }
}