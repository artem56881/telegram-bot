package org.example;

import org.example.entity.Button;
import org.example.entity.Message;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final String token;
    private final String name;
    private final Logic logic;

    public MyTelegramBot(String token, String name, Logic logic) {
        this.name = name;
        this.token = token;
        this.logic = logic;

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


    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();


            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());

            int userId = 0;
            String userName = null;

            Message outputMessage = logic.processMessage(messageText, chatId);
            message.setText(outputMessage.text());
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            if (outputMessage.buttonList() != null) {
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                for (Button b : outputMessage.buttonList()) {
                    // Create a new row for each button
                    List<InlineKeyboardButton> row = new ArrayList<>();
                    InlineKeyboardButton button = new InlineKeyboardButton();
                    button.setText(b.name());
                    button.setCallbackData(b.data());
                    row.add(button);
                    rowsInline.add(row); // Add the row to rowsInline
                }
                markupInline.setKeyboard(rowsInline);
                message.setReplyMarkup(markupInline);
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

}
