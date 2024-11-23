package org.example.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import org.example.config.DatabaseConnection;

public class RemoveCommand {
    private HashMap<String, String> trackedProducts;

    // SQL-запрос для удаления товара из базы данных
    private static final String DELETE_PRODUCT_SQL = "DELETE FROM products WHERE id = ?";

    public RemoveCommand() {
    }
    public String execute(String productId) {
        if (trackedProducts.containsKey(productId)) {
            trackedProducts.remove(productId);  // Удаление товара из отслеживаемых

            try {
                deleteProductFromDatabase(productId);
                return "Товар успешно удален из отслеживаемых и базы данных";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Ошибка при удалении товара из базы данных: " + e.getMessage();
            }
        } else {
            return "Товар с данным ID не найден среди отслеживаемых";
        }
    }

    private void deleteProductFromDatabase(String productId) throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection();

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(DELETE_PRODUCT_SQL)) {

            preparedStatement.setString(1, productId);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Товар удален из базы данных.");
            } else {
                System.out.println("Товар с данным ID не найден в базе данных.");
            }
        } catch (SQLException e) {
            throw new SQLException("Ошибка при удалении товара из БД: " + e.getMessage());
        }
    }
}
