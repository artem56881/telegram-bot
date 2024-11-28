package org.example.commands;

import org.example.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class AddCommand {

    private HashMap<Long, String> trackedProducts;

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (id, name, price) VALUES (?, ?, ?)";
    private static final String CHECK_PRODUCT_SQL = "SELECT COUNT(*) FROM products WHERE id = ?";

    // Конструктор для инициализации локального хранилища
    public AddCommand() {
        this.trackedProducts = new HashMap<>();
    }

    /**
     * Основной метод добавления продукта.
     *
     * @param productId   Идентификатор продукта.
     * @param productName Название продукта.
     * @param price       Цена продукта.
     * @return Результат операции (сообщение для пользователя).
     */
    public String execute(Long productId, String productName, int price) {
        try {
            if (isProductInDatabase(productId)) {
                return "Товар уже добавлен в отслеживаемые";
            }

            // Добавление продукта в базу данных
            addProductToDatabase(productId, productName, price);

            // Локальное добавление продукта
            trackedProducts.put(productId, "Цена появится позже");
            return "Товар успешно добавлен для отслеживания";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Ошибка при добавлении товара в базу данных: " + e.getMessage();
        }
    }

    /**
     * Проверяет, существует ли продукт в базе данных.
     *
     * @param productId Идентификатор продукта.
     * @return true, если продукт уже есть в базе данных; иначе false.
     * @throws SQLException При ошибке SQL-запроса.
     */
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

    /**
     * Добавляет продукт в базу данных.
     *
     * @param productId   Идентификатор продукта.
     * @param productName Название продукта.
     * @param price       Цена продукта.
     * @throws SQLException При ошибке SQL-запроса.
     */
    private void addProductToDatabase(Long productId, String productName, int price) throws SQLException {
        if (productName.length() > 50) {
            productName = productName.substring(0, 50); // Обрезаем до 50 символов
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

}
