package com.example.fuel.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Отвечает за подключение к SQLite-базе данных.
 * При первом вызове создаёт файл smart_fuel.db в корне проекта.
 */
public class DatabaseManager {

    private static final String DATABASE_URL = "jdbc:sqlite:smart_fuel.db";

    // Получить соединение
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }
}
