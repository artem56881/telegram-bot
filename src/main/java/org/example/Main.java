package org.example;

import org.example.Config.DatabaseConnection;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {

        try {
            String itemId = LinkToId.extractItemId("https://aliexpress.ru/item/32864414179.html?spm=a2g2w.home.10009201.8.75df274cOKThmM&mixer_rcmd_bucket_id=aerabtestalgoRecommendAbV2_controlRu1&ru_algo_pv_id=807ae3-d46482-e65f0e-9725ba-1729580400&scenario=aerUserItemRcmdSellTabGroupOne&sku_id=10000002669840615&traffic_source=recommendation&type_rcmd=core");
            System.out.println(itemId);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        String token = System.getenv("TOKEN");
        String name = System.getenv("NAME");
        String alitoken = System.getenv("ALITOKEN");

        if(token == null) {
            System.out.println("no token assigned");
        }
        if(name == null) {
            System.out.println("no name assigned");
        }
        if(alitoken == null) {
            System.out.println("no alitoken assigned");
        }

        Logic logic = new Logic();

        DatabaseConnection databaseConnection = new DatabaseConnection();
        databaseConnection.createAllTable();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyTelegramBot(token, name, logic));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}