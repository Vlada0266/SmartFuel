package com.example.fuel.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Инициализатор базы данных: создаёт таблицы, а затем вставляет
 * начальные данные только в том случае, если таблицы пусты.
 */
public class DatabaseInitializer {

    private static final String CREATE_PRODUCTS_TABLE = """
        CREATE TABLE IF NOT EXISTS products (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            price REAL NOT NULL,
            is_weighted INTEGER NOT NULL,
            stock_qty REAL NOT NULL
        );
        """;

    private static final String CREATE_SERVICES_TABLE = """
        CREATE TABLE IF NOT EXISTS services (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            price REAL NOT NULL
        );
        """;

    private static final String CREATE_CUSTOMERS_TABLE = """
        CREATE TABLE IF NOT EXISTS customers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            wallet_balance REAL NOT NULL,
            card_balance REAL NOT NULL,
            bonus_points REAL NOT NULL
        );
        """;

    private static final String CREATE_CART_ITEMS_TABLE = """
        CREATE TABLE IF NOT EXISTS cart_items (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            customer_id INTEGER NOT NULL,
            item_type TEXT NOT NULL,
            item_id INTEGER NOT NULL,
            quantity REAL NOT NULL,
            FOREIGN KEY(customer_id) REFERENCES customers(id)
        );
        """;

    private static final String CREATE_PAYMENTS_TABLE = """
        CREATE TABLE IF NOT EXISTS payments (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            customer_id INTEGER NOT NULL,
            method TEXT NOT NULL,
            amount REAL NOT NULL,
            FOREIGN KEY(customer_id) REFERENCES customers(id)
        );
        """;

    // Начальные данные
    private static final String INSERT_INITIAL_PRODUCTS = """
        INSERT INTO products (name, price, is_weighted, stock_qty) VALUES
        ('АИ-95', 56.0, 1, 10000.0),
        ('Дизель', 50.0, 1, 8000.0),
        ('Электро', 8.0, 1, 5000.0);
        """;

    private static final String INSERT_INITIAL_SERVICES = """
        INSERT INTO services (name, price) VALUES
        ('Автомойка', 300.0),
        ('Подкачка шин', 150.0),
        ('Кофе', 70.0);
        """;

    private static final String INSERT_INITIAL_CUSTOMER = """
        INSERT INTO customers (name, wallet_balance, card_balance, bonus_points) VALUES
        ('Иван Иванов', 1000.00, 2000.00, 150.00);
        """;

    public static void initialize() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // Шаг 1: создаём все таблицы, если их ещё нет
            stmt.execute(CREATE_PRODUCTS_TABLE);
            stmt.execute(CREATE_SERVICES_TABLE);
            stmt.execute(CREATE_CUSTOMERS_TABLE);
            stmt.execute(CREATE_CART_ITEMS_TABLE);
            stmt.execute(CREATE_PAYMENTS_TABLE);

            // Шаг 2: проверяем, пусты ли таблицы, и только тогда вставляем начальные данные

            // Проверка продуктов
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM products");
            if (rs.next() && rs.getInt("cnt") == 0) {
                stmt.execute(INSERT_INITIAL_PRODUCTS);
            }
            rs.close();

            // Проверка услуг
            rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM services");
            if (rs.next() && rs.getInt("cnt") == 0) {
                stmt.execute(INSERT_INITIAL_SERVICES);
            }
            rs.close();

            // Проверка клиентов
            rs = stmt.executeQuery("SELECT COUNT(*) AS cnt FROM customers");
            if (rs.next() && rs.getInt("cnt") == 0) {
                stmt.execute(INSERT_INITIAL_CUSTOMER);
            }
            rs.close();

            System.out.println("Инициализация БД завершена (таблицы созданы, начальные данные проверены).");
        } catch (SQLException e) {
            System.err.println("Ошибка инициализации БД: " + e.getMessage());
        }
    }
}
