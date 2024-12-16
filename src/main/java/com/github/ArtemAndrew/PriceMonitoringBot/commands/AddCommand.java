package com.github.ArtemAndrew.PriceMonitoringBot.commands;

import com.github.ArtemAndrew.PriceMonitoringBot.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCommand {

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (user_id, product_id, name, price) VALUES (?, ?, ?, ?)";
    private static final String CHECK_PRODUCT_SQL = "SELECT COUNT(*) FROM products WHERE user_id = ? AND product_id = ?";
    private static final String SELECT_TRACKED_PRODUCTS_SQL = "SELECT user_id, product_id, name, price FROM products WHERE user_id = ?";
    private static final String UPDATE_PRODUCT_PRICE_SQL = "UPDATE products SET price = ? WHERE product_id = ?";

    public String execute(String userId, Long productId, String productName, int price) {
        try {
            if (isProductInDatabase(userId, productId)) {
                return "Товар уже добавлен в отслеживаемые";
            }

            addProductToDatabase(userId, productId, productName, price);
            return "Товар успешно добавлен для отслеживания";
        } catch (SQLException e) {
            return "Ошибка при добавлении товара в базу данных: " + e.getMessage();
        }
    }

    private boolean isProductInDatabase(String userId, Long productId) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_PRODUCT_SQL)) {

            preparedStatement.setString(1, userId);
            preparedStatement.setLong(2, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void addProductToDatabase(String userId, Long productId, String productName, int price) throws SQLException {
        if (productName.length() > 50) {
            productName = productName.substring(0, 50);
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCT_SQL)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setLong(2, productId);
            preparedStatement.setString(3, productName);
            preparedStatement.setInt(4, price); // Используем int для price

            preparedStatement.executeUpdate();
            System.out.println("Товар добавлен в базу данных.");
        }
    }

    public List<Map<String, Object>> getTrackedProducts(String userId) throws SQLException {
        List<Map<String, Object>> trackedProducts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TRACKED_PRODUCTS_SQL)) {

            preparedStatement.setString(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("user_id", resultSet.getString("user_id"));
                    product.put("product_id", resultSet.getLong("product_id"));
                    product.put("name", resultSet.getString("name"));
                    product.put("price", resultSet.getInt("price")); // Используем int для price

                    trackedProducts.add(product);
                }
            }
        }
        return trackedProducts;
    }

    public List<Map<String, Object>> getAllTrackedProducts() throws SQLException {
        List<Map<String, Object>> trackedProducts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PRODUCTS")) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Map<String, Object> product = new HashMap<>();
                    product.put("user_id", resultSet.getString("user_id"));
                    product.put("product_id", resultSet.getLong("product_id"));
                    product.put("name", resultSet.getString("name"));
                    product.put("price", resultSet.getInt("price")); // Используем int для price

                    trackedProducts.add(product);
                }
            }
        }
        return trackedProducts;
    }

    public void updateProductPrice(Long productId, int newPrice) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PRODUCT_PRICE_SQL)) {

            preparedStatement.setInt(1, newPrice); // Используем int для price
            preparedStatement.setLong(2, productId);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Цена продукта обновлена: " + productId);
            } else {
                System.out.println("Не удалось обновить цену для: " + productId);
            }
        }
    }
}
