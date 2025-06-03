package com.example.fuel.DAO;

import com.example.fuel.db.DatabaseManager;
import com.example.fuel.factory.FuelProductFactory;
import com.example.fuel.factory.ProductFactory;
import com.example.fuel.factory.ServiceProductFactory;
import com.example.fuel.model.FuelProduct;
import com.example.fuel.model.ServiceProduct;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация ProductDAO: работа с таблицами products и services.
 * Используем фабричный паттерн (Factory), чтобы создавать объекты FuelProduct и ServiceProduct
 * прямо из ResultSet, инкапсулируя логику создания в соответствующих фабриках.
 */
public class ProductDAOImpl implements ProductDAO {

    /**
     * Получить все FuelProduct из таблицы products.
     * Создание объектов делегируется FuelProductFactory.
     */
    @Override
    public List<FuelProduct> getAllFuelProducts() {
        List<FuelProduct> list = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ProductFactory factory = new FuelProductFactory();
            while (rs.next()) {
                try {
                    FuelProduct p = (FuelProduct) factory.createProduct(rs);
                    list.add(p);
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Можно логировать ошибку и продолжать
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Получить один FuelProduct по ID из таблицы products.
     */
    @Override
    public FuelProduct getFuelProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    ProductFactory factory = new FuelProductFactory();
                    return (FuelProduct) factory.createProduct(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Обновить количество топлива (stock_qty) у конкретного FuelProduct.
     */
    @Override
    public void updateFuelStock(int id, double newStock) {
        String sql = "UPDATE products SET stock_qty = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newStock);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Получить все ServiceProduct из таблицы services.
     * Используем ServiceProductFactory для создания объектов.
     */
    @Override
    public List<ServiceProduct> getAllServiceProducts() {
        List<ServiceProduct> list = new ArrayList<>();
        String sql = "SELECT * FROM services";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ProductFactory factory = new ServiceProductFactory();
            while (rs.next()) {
                try {
                    ServiceProduct s = (ServiceProduct) factory.createProduct(rs);
                    list.add(s);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Получить ServiceProduct по ID из таблицы services.
     */
    @Override
    public ServiceProduct getServiceById(int id) {
        String sql = "SELECT * FROM services WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                try {
                    ProductFactory factory = new ServiceProductFactory();
                    return (ServiceProduct) factory.createProduct(rs);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Обновить "запас" услуги.
     * Сейчас просто заглушка: если у services появится колонка stock_qty — здесь обновим.
     */
    @Override
    public void updateServiceStock(int id, double newStock) {
        // Пока что не обновляем ничего, но структура метода сохранена.
        String sql = "UPDATE services SET price = price WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
