package org.example.Commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import org.example.Config.DatabaseConnection;


public class AddCommand {
    private HashMap<String, String> trackedProducts;

    private static final String INSERT_PRODUCT_SQL = "INSERT INTO products (id, name, price) VALUES (?, ?, ?)";

    public AddCommand() {
        this.trackedProducts = trackedProducts;
    }

    public String execute(String productId, String productName, double price) {
        if (trackedProducts.containsKey(productId)) {
            return "Товар уже добавлен в отслеживаемые";
        } else {

            trackedProducts.put(productId, "Цена появится позже");


            try {
                addProductToDatabase(productId, productName, price);
                return "Товар успешно добавлен для отслеживания";
            } catch (SQLException e) {
                e.printStackTrace();
                return "Ошибка при добавлении товара в базу данных: " + e.getMessage();
            }
        }
    }

    private void addProductToDatabase(String productId, String productName, double price) throws SQLException {
        DatabaseConnection databaseConnection = new DatabaseConnection();

        try (Connection connection = databaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_PRODUCT_SQL)) {

            preparedStatement.setString(1, productId);
            preparedStatement.setString(2, productName);
            preparedStatement.setDouble(3, price);

            preparedStatement.executeUpdate();
            System.out.println("Товар добавлен в базу данных.");
        } catch (SQLException e) {
            throw new SQLException("Ошибка при добавлении товара в БД: " + e.getMessage());
        }
    }

}
