package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
            if (messageText.equals("/start")) {
                userId = Math.toIntExact(update.getMessage().getFrom().getId());
                userName = update.getMessage().getFrom().getFirstName();

                String startMessage = logic.handleStartCommand(userId, userName);
                message.setText(startMessage);
            } else {
                // Обрабатываем остальные сообщения через логику
                String outputMessage = logic.processMessage(messageText, userId, userName);
                message.setText(outputMessage);
            }

            // Отправляем сообщение пользователю
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
}

}
