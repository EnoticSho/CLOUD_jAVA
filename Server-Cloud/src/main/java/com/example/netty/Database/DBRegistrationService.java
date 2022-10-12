package com.example.netty.Database;

import java.io.Closeable;
import java.sql.*;

public class DBRegistrationService implements Closeable {
    private final String DB_ConnectionURL = "jdbc:mysql://localhost/db_cloud";

    private Connection connection;

    public DBRegistrationService() {
        try {
            connection = DriverManager.getConnection(DB_ConnectionURL, "bestuser", "Password123!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void regUser(String login, String password) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO users(Login, Password, path_location) VALUE(?, ?, ?)")){
            statement.setString(1, login);
            statement.setString(2, password);
            statement.setString(3, login);
            statement.executeUpdate();
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
