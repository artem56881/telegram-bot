import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyTelegramBot extends TelegramLongPollingBot {

    @Override
    public String getBotUsername() {
        // Return your bot username here
        return "YourBotUsername";
    }

    @Override
    public String getBotToken() {
        // Return your bot token from BotFather
        return "YOUR_BOT_TOKEN";
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
            message.setChatId(chatId.toString());
            message.setText("You said: " + messageText);

            // Send the message back to the user
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
