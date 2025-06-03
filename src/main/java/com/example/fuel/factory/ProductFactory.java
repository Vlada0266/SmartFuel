package com.example.fuel.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Интерфейс фабрики для создания продуктов из ResultSet.
 * Позволяет использовать единый метод createProduct для всех типов продуктов.
 */
public interface ProductFactory {
    Object createProduct(ResultSet rs) throws SQLException;
}
