package pablo.tzeliks.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/simple_db";
    private static final String USER = "db_user";
    private static final String PASS = "db_pass";

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
            }

            System.out.println("Connected to the database successfully.");
            return connection;

        } catch (SQLException e) {
            System.out.println("Failed to connect to the database. Check: " + e.getMessage());

            e.printStackTrace();
            return null;
        }
    }
}
