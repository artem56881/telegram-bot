package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
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

    private void sendMessageWithButtons(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Меню бота:");

        // Создаем кнопки
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Кнопка 1");
        button1.setCallbackData("button1");

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Кнопка 2");
        button2.setCallbackData("button2");

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Кнопка 3");
        button3.setCallbackData("button3");

        // Добавляем каждую кнопку в отдельную строку
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(button1);
        rowsInline.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(button2);
        rowsInline.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(button3);
        rowsInline.add(row3);


        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        // Проверяем, что обновление содержит сообщение и текст
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            // Создаем объект SendMessage для ответа
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());

            // Обрабатываем команду /start
            int userId = 0;
            String userName = null;
            if (messageText.equals("/start")) {
                userId = Math.toIntExact(update.getMessage().getFrom().getId());
                userName = update.getMessage().getFrom().getFirstName();

                String startMessage = logic.handleStartCommand(userId, userName);
                message.setText(startMessage);
            }
            else {
                // Обрабатываем остальные сообщения через логику
                String outputMessage = logic.processMessage(messageText, userId, userName);
                message.setText(outputMessage);
            }


            if(true) {
                sendMessageWithButtons(chatId);//сообщение с кнопками
            }
            else {
                // Отправляем сообщение пользователю
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();  //формирование данных с кнопки
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(callbackData);
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

}
