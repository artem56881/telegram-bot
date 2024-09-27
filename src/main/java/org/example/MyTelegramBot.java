package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramBot;

public class MyTelegramBot extends TelegramLongPollingBot {
    private String token;
    private String name;

    public MyTelegramBot(String token, String name) {
        this.name = name;
        this.token = token;
    }

    @Override
    public String getBotUsername() {
        // Return your bot username here
        return name;
    }

    @Override
    public String getBotToken() {
        // Return your bot token from BotFather
        return token;
    }


    private String reverse(String message){
        String reveres = "";
        char[] reversedMessageArray = message.toCharArray();

        for (int i = reversedMessageArray.length - 1; i >= 0; i--) {
            reveres = reveres + reversedMessageArray[i];
        }
        System.out.println(reveres);
        return reveres;
    }


    @Override
    public void onUpdateReceived(Update update) {
        // Check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Retrieve the message text
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Create a SendMessage object with the received chat ID and message text
            SendMessage message = new SendMessage();
            String reversedString = reverse(messageText);
            message.setChatId(chatId.toString());
            message.setText(reversedString);

            // Send the message back to the user
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
