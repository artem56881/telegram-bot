package com.github.ArtemAndrew.PriceMonitoringBot;

import com.github.ArtemAndrew.PriceMonitoringBot.config.DatabaseConnection;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {
    public static void main(String[] args) {


        String token = System.getenv("TOKEN");
        String name = System.getenv("NAME");


        DatabaseConnection.createAllTables();

        Logic logic = new Logic();
        MyTelegramBot tgBot = new MyTelegramBot(token, name, logic);
        logic.setTelegranBot(tgBot);
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(tgBot);
        } catch (TelegramApiException _) {
            System.out.println("Error class Telegram exeption");
        }

    }
}