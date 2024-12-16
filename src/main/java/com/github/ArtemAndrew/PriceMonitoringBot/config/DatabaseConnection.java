package com.github.ArtemAndrew.PriceMonitoringBot.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/telegrambot";
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

    public void createAllTable() {
        String sql1 = """
                CREATE TABLE IF NOT EXISTS Users (
                id SERIAL PRIMARY KEY, 
                username VARCHAR(50) NOT NULL 
                );""";

        String sql2 = """
                CREATE TABLE IF NOT EXISTS Products (
                id SERIAL PRIMARY KEY, 
                name VARCHAR(50) NOT NULL, 
                price INTEGER NOT NULL
                );""";

        String sql3 = """
                CREATE TABLE IF NOT EXISTS Users_Products (
                id SERIAL PRIMARY KEY,
                user_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL
                );""";

        try {
            Connection conn = connect();
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql1);
                    stmt.executeUpdate(sql2);
                    stmt.executeUpdate(sql3);
                    System.out.println("Таблицы успешно созданы или уже существуют.");
                }
            } else {
                System.out.println("Не удалось создать таблицу: соединение с базой данных не установлено.");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка создания таблицы: " + e.getMessage());
        }
    }
}
