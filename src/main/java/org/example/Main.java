package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {
    public static void main(String[] args) {


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

//        DatabaseConnection databaseConnection = new DatabaseConnection();
//        databaseConnection.createAllTable();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyTelegramBot(token, name, logic));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}