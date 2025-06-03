package com.example.fuel.factory;

import com.example.fuel.model.FuelProduct;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Фабрика для создания объектов FuelProduct из ResultSet.
 * Применяем паттерн Factory: инкапсулируем создание объектов,
 * чтобы отделить его от логики DAO.
 */
public class FuelProductFactory implements ProductFactory {

    /**
     * Создаёт FuelProduct из строки таблицы products (ResultSet).
     * Используется DAO при чтении из БД.
     */
    @Override
    public FuelProduct createProduct(ResultSet rs) throws SQLException {
        return new FuelProduct(
                rs.getInt("id"),            // ID топлива
                rs.getString("name"),       // Название топлива
                rs.getDouble("price"),      // Цена за литр
                rs.getDouble("stock_qty")   // Количество на складе (в литрах)
        );
    }
}
