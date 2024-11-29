package org.example.services;

import org.example.config.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class UserDatabaseService {

    public boolean isUserIdExists(long userId) {
        String sql = "SELECT id FROM users WHERE id = ?";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.next(); // Возвращает true, если запись найдена
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean addUserToDatabase(long userId, String userName) {
        if (isUserIdExists(userId)) {
            // Пользователь с таким ID уже существует
            return false;
        }

        String sql = "INSERT INTO users (id, username) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, userId);
            preparedStatement.setString(2, userName);
            int rowsAffected = preparedStatement.executeUpdate();

            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

}
