package org.example.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.example.config.DatabaseConnection;

public class RemoveCommand {
    // SQL-запросы
    private static final String DELETE_PRODUCT_SQL = "DELETE FROM products WHERE user_id = ? AND product_id = ?";

    public String execute(Long userId, Long productId) {
        try {
            if (productId != null) {
                deleteProductFromDatabase(userId, productId);
                return "Товар успешно удален из базы данных.";
            } else {
                return "Товар с указанной ссылкой не найден.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка при удалении товара из базы данных: " + e.getMessage();
        }
    }

    private void deleteProductFromDatabase(Long userId, Long productId) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PRODUCT_SQL)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, productId);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Товар удален из базы данных.");
            } else {
                System.out.println("Товар с данным ID не найден в базе данных.");
            }
        }
    }
}
