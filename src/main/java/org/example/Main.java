package org.example;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {
    public static void main(String[] args) {


        String token = System.getenv("TOKEN");
        String name = System.getenv("NAME");

        if (token == null) {
            System.out.println("no token assigned");
        }
        if (name == null) {
            System.out.println("no name assigned");
        }

        Logic logic = new Logic();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyTelegramBot(token, name, logic));
        } catch (TelegramApiException _) {
        }
    }
}