package com.example.netty.Database;

import java.nio.file.Path;
import java.sql.*;

public class DBAuthService implements AuthService{

    private static final String DB_ConnectionURL = "jdbc:mysql://localhost/db_cloud";

    private Connection connection;

    public DBAuthService() {
        run();
    }

    @Override
    public Path getPathByLoginAndPassword(String login, String password) {
        try (final PreparedStatement statement = connection.prepareStatement("select path_location from users where Login = ? and Password = ?")) {
            statement.setString(1, login);
            statement.setString(2, password);
            final ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                return Path.of(rs.getString("path_location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        try {
            connection = DriverManager.getConnection(DB_ConnectionURL, "bestuser", "Password123!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
