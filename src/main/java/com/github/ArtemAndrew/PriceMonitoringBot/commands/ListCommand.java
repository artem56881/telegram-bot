package com.github.ArtemAndrew.PriceMonitoringBot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.github.ArtemAndrew.PriceMonitoringBot.config.DatabaseConnection;

public class ListCommand {

    // SQL-запрос для получения всех товаров из базы данных для конкретного пользователя
    private static final String SELECT_ALL_PRODUCTS_SQL = "SELECT id, name, price FROM products WHERE user_id = ?";

    public ListCommand() {
    }

    public String execute(String userId) {
        List<String> productsList;

        try {
            productsList = getAllProductsFromDatabase(userId);
            if (productsList.isEmpty()) {
                return "Нет товаров для отслеживания.";
            } else {
                return String.join("\n", productsList);  // Возвращаем список товаров как строку
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка при получении списка товаров из базы данных: " + e.getMessage();
        }
    }

    private List<String> getAllProductsFromDatabase(String userId) throws SQLException {
        List<String> products = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PRODUCTS_SQL)) {

            preparedStatement.setString(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productId = resultSet.getString("id");
                String productName = resultSet.getString("name");
                int productPrice = resultSet.getInt("price");

                // Добавляем продукт в список
                products.add("ID: " + productId + ", Название: " + productName + ", Цена: " + productPrice + "₽");
            }
        } catch (SQLException e) {
            throw new SQLException("Ошибка при получении списка товаров из БД: " + e.getMessage());
        }
        return products;
    }
}
