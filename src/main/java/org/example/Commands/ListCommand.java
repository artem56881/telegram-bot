package org.example.Commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.example.Config.DatabaseConnection;

public class ListCommand {
    private static final String QUERY = """
        SELECT p.name, p.price FROM Products p
        JOIN Users_Products up ON p.id = up.product_id
        WHERE up.user_id = ?;
    """;

    public String execute(int userId) {
        List<String> productList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(QUERY)) {

            // Устанавливаем userId как параметр для SQL-запроса
            preparedStatement.setInt(1, userId);

            // Выполняем запрос и получаем результат
            ResultSet resultSet = preparedStatement.executeQuery();

            // Обрабатываем результат запроса, добавляем товары в список
            while (resultSet.next()) {
                String productName = resultSet.getString("name");
                int productPrice = resultSet.getInt("price");
                productList.add(productName + " - $" + productPrice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка при получении списка товаров: " + e.getMessage();
        }

        // Если список товаров пуст, возвращаем соответствующее сообщение
        if (productList.isEmpty()) {
            return "Вы не подписаны ни на один товар.";
        }

        // Возвращаем список товаров в виде строки
        return "Список отслеживаемых товаров:\n" + String.join("\n", productList);
    }
}
