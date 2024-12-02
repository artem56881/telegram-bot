package org.example.commands;

import org.example.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCommand {

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (id, name, price) VALUES (?, ?, ?)";
    private static final String CHECK_PRODUCT_SQL = "SELECT COUNT(*) FROM products WHERE id = ?";
    private static final String SELECT_TRACKED_PRODUCTS_SQL = "SELECT user_id, product_url, desired_price, current_price FROM tracked_products";
    private static final String UPDATE_PRODUCT_PRICE_SQL = "UPDATE tracked_products SET current_price = ? WHERE product_url = ?";

    public String execute(Long productId, String productName, int price) {
        try {
            if (isProductInDatabase(productId)) {
                return "Товар уже добавлен в отслеживаемые";
            }

            addProductToDatabase(productId, productName, price);
            return "Товар успешно добавлен для отслеживания";
        } catch (SQLException e) {
            return "Ошибка при добавлении товара в базу данных: " + e.getMessage();
        }
    }

    private boolean isProductInDatabase(Long productId) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(CHECK_PRODUCT_SQL)) {

            preparedStatement.setLong(1, productId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private void addProductToDatabase(Long productId, String productName, int price) throws SQLException {
        if (productName.length() > 50) {
            productName = productName.substring(0, 50);
        }

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCT_SQL)) {
            preparedStatement.setLong(1, productId);
            preparedStatement.setString(2, productName);
            preparedStatement.setDouble(3, price);

            preparedStatement.executeUpdate();
            System.out.println("Товар добавлен в базу данных.");
        }
    }

    /**
     * Получает список всех отслеживаемых продуктов.
     *
     * @return Список продуктов с информацией (user_id, product_url, desired_price, current_price).
     * @throws SQLException При ошибке SQL-запроса.
     */
    public List<Map<String, Object>> getTrackedProducts() throws SQLException {
        List<Map<String, Object>> trackedProducts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TRACKED_PRODUCTS_SQL);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("user_id", resultSet.getLong("user_id"));
                product.put("product_url", resultSet.getString("product_url"));
                product.put("desired_price", resultSet.getInt("desired_price"));
                product.put("current_price", resultSet.getInt("current_price"));

                trackedProducts.add(product);
            }
        }
        return trackedProducts;
    }

    /**
     * Обновляет текущую цену товара в базе данных.
     *
     * @param productUrl URL продукта для обновления.
     * @param newPrice   Новая цена продукта.
     * @throws SQLException При ошибке SQL-запроса.
     */
    public void updateProductPrice(String productUrl, int newPrice) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_PRODUCT_PRICE_SQL)) {

            preparedStatement.setInt(1, newPrice);
            preparedStatement.setString(2, productUrl);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Цена продукта обновлена: " + productUrl);
            } else {
                System.out.println("Не удалось обновить цену для: " + productUrl);
            }
        }
    }
}
