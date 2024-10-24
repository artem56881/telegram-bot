package org.example.Commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.example.Config.DatabaseConnection;


public class ListCommand {
    private HashMap<String, String> trackedProducts;

    // SQL-запрос для получения всех товаров из базы данных
    private static final String SELECT_ALL_PRODUCTS_SQL = "SELECT id, name, price FROM products";

    public ListCommand(HashMap<String, String> trackedProducts) {
        this.trackedProducts = trackedProducts;
    }

    public String execute() {
        List<String> productsList = new ArrayList<>();

        try {
            productsList = getAllProductsFromDatabase();
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

    private List<String> getAllProductsFromDatabase() throws SQLException {
        List<String> products = new ArrayList<>();
        DatabaseConnection databaseConnection = new DatabaseConnection();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_PRODUCTS_SQL)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String productId = resultSet.getString("id");
                String productName = resultSet.getString("name");
                double productPrice = resultSet.getDouble("price");

                // Добавляем продукт в список
                products.add("ID: " + productId + ", Название: " + productName + ", Цена: " + productPrice);
            }
        } catch (SQLException e) {
            throw new SQLException("Ошибка при получении списка товаров из БД: " + e.getMessage());
        }
        return products;
    }
}
