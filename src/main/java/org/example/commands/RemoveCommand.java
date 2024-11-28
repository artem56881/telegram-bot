package org.example.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.config.DatabaseConnection;

public class RemoveCommand {
    // SQL-запросы
    private static final String GET_PRODUCT_ID_SQL = "SELECT id FROM products WHERE name = ?";
    private static final String DELETE_PRODUCT_SQL = "DELETE FROM products WHERE id = ?";

    public String execute(String productName) {
        try {
            Long productId = getProductIdByName(productName);

            if (productId != null) {
                deleteProductFromDatabase(productId);
                return "Товар успешно удален из базы данных.";
            } else {
                return "Товар с указанной ссылкой не найден.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка при удалении товара из базы данных: " + e.getMessage();
        }
    }

    private Long getProductIdByName(String productName) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(GET_PRODUCT_ID_SQL)) {

            preparedStatement.setString(1, productName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
            }
        }
        return null;
    }

    private void deleteProductFromDatabase(Long productId) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PRODUCT_SQL)) {

            preparedStatement.setLong(1, productId);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Товар удален из базы данных.");
            } else {
                System.out.println("Товар с данным ID не найден в базе данных.");
            }
        }
    }
}
