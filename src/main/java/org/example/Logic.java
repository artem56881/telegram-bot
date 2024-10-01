package org.example;


import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//import json.JSONObject;

import java.io.IOException;
import java.util.Objects;


public class Logic {
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
                .addHeader("x-rapidapi-key", "xxx")
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
/*  Не видит объект JSONObject хзхз
    public String extractPrice(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        // Navigate to the price using the keys from the JSON structure
        double price = jsonObject
                .getJSONObject("result")
                .getJSONObject("item")
                .getJSONObject("sku")
                .getJSONObject("def")
                .getDouble("price");

        return "Price: $" + price;
    }

*/
    public String processMessage(String inputMessage){
        //if (Objects.equals(inputMessage, "ali")){

            return ApiResponse(inputMessage);
        //}
        /*
        if (Objects.equals(inputMessage, "/option2")){
            return ":DDDD";
        }

        if (Objects.equals(inputMessage, "/start")){
            return "Hello";
        }
        */
        //return reverse(inputMessage);

    }

}
