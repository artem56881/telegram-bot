package org.example;


import com.google.gson.JsonObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//import json.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;


public class Logic {

    private final HashMap<String, String> commandMap = new HashMap<>();


    public Logic(){

        commandMap.put("/start", """
                Бот отслеживает цены на выбранные вами товары на AliExpress и отправляет уведомление, когда цена снижается до желаемого уровня.\s

                Команды:
                /add - добавить товар для отслеживания
                /list - показать список отслеживаемых товаров
                /remove - удалить товар из списка отслеживания
                /help - помощь""");

        commandMap.put("/add", "Введите ID товара для добавления в список отслеживания.");

        commandMap.put("/list", "Список отслеживаемых товаров:");

        commandMap.put("/remove", "Введите ID товара для удаления из списка отслеживания.");

        commandMap.put("/help", """
                Список доступных команд:
                /start - начать работу бота
                /add - добавить товар для отслеживания
                /list - показать список отслеживаемых товаров
                /remove - удалить товар из списка отслеживания
                /help - помощь""");

        commandMap.put("/tst", ":DDDD");
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


    public String processMessage(String inputMessage){

        if (commandMap.containsKey(inputMessage)) {
            return commandMap.get(inputMessage);
        }

        String itemId = "";
        try {//теперь бот принимет ссылку и выделаяет из нее id
            itemId = LinkToId.extractItemId(inputMessage);
            System.out.println(itemId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        };
        String response = ApiResponse(itemId);
        System.out.println(response);

        String errorCode = checkForJsonDataError(response);
        System.out.println(errorCode);

        if(errorCode.equals("error")){
            return "error";
        }

        String price = extractPromotionPrice(response);
        System.out.println(price);

        return errorCode + ": " + price;


    }

}
