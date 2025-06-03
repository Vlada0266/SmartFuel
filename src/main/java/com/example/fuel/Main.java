package com.example.fuel;


import javafx.application.Application;
import com.example.fuel.db.DatabaseInitializer;
import com.example.fuel.view.MainApp;

/**
 * Точка входа в приложение.
 * Сначала инициализируем базу данных, затем запускаем JavaFX через MainApp.
 */
public class Main {
    public static void main(String[] args) {
        // 1) Инициализация SQLite-базы (таблицы и начальные данные)
        DatabaseInitializer.initialize();
        // 2) Запуск JavaFX-приложения (MainApp наследует Application и содержит свой main)
        Application.launch(MainApp.class, args);
    }
}

