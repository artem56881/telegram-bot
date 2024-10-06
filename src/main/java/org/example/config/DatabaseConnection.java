package org.example.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/TelegramBot";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Andrew80t7!";

    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Подключение к базе данных выполнено успешно!");
        } catch (ClassNotFoundException e) {
            System.out.println("Драйвер PostgreSQL не найден: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        }
        return connection;
    }

    public void createUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Users ("
                + "id SERIAL PRIMARY KEY, "
                + "username VARCHAR(50) NOT NULL, "
                + "email VARCHAR(50) NOT NULL UNIQUE"
                + ");";

        try (Connection conn = connect(); // Используем метод connect для получения соединения
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate(sql);
            System.out.println("Таблица Users успешно создана.");
        } catch (SQLException e) {
            System.out.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }
}
