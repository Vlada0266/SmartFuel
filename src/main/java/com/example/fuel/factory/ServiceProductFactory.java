package com.example.fuel.factory;

import com.example.fuel.model.ServiceProduct;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Фабрика для создания объектов ServiceProduct из ResultSet.
 * Используется DAO при чтении услуг из таблицы services.
 */
public class ServiceProductFactory implements ProductFactory {

    /**
     * Создаёт ServiceProduct из строки таблицы services (ResultSet).
     * Объекты ServiceProduct имеют условный stockQty = 1.0
     * (услуги не хранятся на складе в явном виде).
     */
    @Override
    public ServiceProduct createProduct(ResultSet rs) throws SQLException {
        return new ServiceProduct(
                rs.getInt("id"),            // ID услуги
                rs.getString("name"),       // Название
                rs.getDouble("price"),      // Цена
                1.0                          // Условное количество (услуги не ограничены по числу)
        );
    }
}
