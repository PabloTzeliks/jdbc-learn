package pablo.tzeliks;

import pablo.tzeliks.utils.DatabaseConnection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello and welcome to Simple DB!");

        DatabaseConnection.getConnection();
    }
}