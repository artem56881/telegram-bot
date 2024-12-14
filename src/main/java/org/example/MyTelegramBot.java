package org.example;

import org.example.entity.Button;
import org.example.entity.Message;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
        
        String messageText = null;
        Long chatId = null;

        if (update.hasMessage() && update.getMessage().hasText()) { //проверка на то есть ли текст сообщения
            messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) { 
            CallbackQuery callbackQuery = update.getCallbackQuery();
            messageText = callbackQuery.getData();
            chatId = callbackQuery.getMessage().getChatId();
        }
        if (messageText != null && chatId != null) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());

            Message outputMessage = logic.processMessage(messageText, chatId);
            message.setText(outputMessage.text());
            if (outputMessage.buttonList() != null) {
                InlineKeyboardMarkup markupInline = getInlineKeyboardMarkup(outputMessage);
                message.setReplyMarkup(markupInline);
            }

            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.out.println("Error with TelegramAPI");
            }
        }
    }

    private static @NotNull InlineKeyboardMarkup getInlineKeyboardMarkup(Message outputMessage) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Button b : outputMessage.buttonList()) {
            
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(b.name());
            button.setCallbackData(b.data());
            row.add(button);
            rowsInline.add(row);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}