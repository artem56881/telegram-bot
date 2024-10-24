package org.example;

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

            Message outputMessage = logic.processMessage(messageText, chatId);
            message.setText(outputMessage.text());
            List<InlineKeyboardButton> row1 = new ArrayList<>();
            if(outputMessage.buttonList() != null){
                InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

                for(Button b : outputMessage.buttonList()){
                    InlineKeyboardButton button1 = new InlineKeyboardButton();
                    button1.setText(b.name());
                    button1.setCallbackData(b.data());
                    row1.add(button1);
                    rowsInline.add(row1);
                }
                markupInline.setKeyboard(rowsInline);
                message.setReplyMarkup(markupInline);
            }

            // Send the message back to the user
            // Отправляем сообщение пользователю
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
}

}
