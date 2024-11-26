package org.example;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.Config.DatabaseConnection;

import org.example.Ozon.ProductInfoCollector;
import org.example.Ozon.FetchHtml;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Logic {


    private final Map<String, Message> commandMap = new HashMap<>();

    private final static Message help = new Message("""
            Бот отслеживает цены на выбранные вами товары на AliExpress и отправляет уведомление, когда цена снижается до желаемого уровня.\s

            Команды:
            /add - добавить товар для отслеживания
            /list - показать список отслеживаемых товаров
            /remove - удалить товар из списка отслеживания
            /help - помощь""",
            List.of(new Button("start", "start")));


    public Logic() {

        commandMap.put("/start", help);

        commandMap.put("/add", new Message("Введите ID товара для добавления в список отслеживания."));

        commandMap.put("/list", new Message("Список отслеживаемых товаров:"));

        commandMap.put("/remove", new Message("Введите ID товара для удаления из списка отслеживания."));

        commandMap.put("/help", new Message("""
                Список доступных команд:
                /start - начать работу бота
                /add - добавить товар для отслеживания
                /list - показать список отслеживаемых товаров
                /remove - удалить товар из списка отслеживания
                /help - помощь"""));

        commandMap.put("/tst", new Message("FGDFG"));
        commandMap.put("/price", new Message(ProductInfoCollector.collectProductInfo(FetchHtml.ExtarctHtml("https://www.ozon.ru/product/mysh-oklik-385m-chernyy-krasnyy-opticheskaya-1000dpi-usb-dlya-noutbuka-3but-210972580/?avtc=1&avte=4&avts=1732557196")).get("base_price")));
    }

    private Message toString(int productPrice) {
        return new Message(String.valueOf(productPrice));
    }


    public Message handleStartCommand(long userId, String userName) {
        UserDatabaseService userDatabaseService = new UserDatabaseService();
        boolean userAdded = userDatabaseService.addUserToDatabase(userId, userName);
        if (userAdded) {
            return new Message("Спасибо, что пользуйтесь нашим ботом!");
        } else {
            return new Message("Мы вас уже запомнили");
        }
    }


    public Message processMessage(String inputMessage, long userId) {

        if (inputMessage.equals("/start")) {
            //userName = update.getMessage().getFrom().getFirstName();
            return handleStartCommand(userId, "default username");

        }
        if (commandMap.containsKey(inputMessage)) {
            return commandMap.get(inputMessage);
        }

        return null;
    }

}
