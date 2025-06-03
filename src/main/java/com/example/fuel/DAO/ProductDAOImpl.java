package com.example.fuel.DAO;

import com.example.fuel.db.DatabaseManager;
import com.example.fuel.model.FuelProduct;
import com.example.fuel.model.ServiceProduct;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация ProductDAO: работа с таблицами products и services.
 */
public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<FuelProduct> getAllFuelProducts() {
        List<FuelProduct> list = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                FuelProduct p = new FuelProduct(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("stock_qty")
                );
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public FuelProduct getFuelProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new FuelProduct(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("stock_qty")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

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

    @Override
    public List<ServiceProduct> getAllServiceProducts() {
        List<ServiceProduct> list = new ArrayList<>();
        String sql = "SELECT * FROM services";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ServiceProduct s = new ServiceProduct(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getDouble("price") > 0 ? 1.0 : 0.0  // stockQty = число доступных услуг (условно 1 на услугу)
                );
                // Здесь: мы не храним отдельное количество услуг, пока допускаем, что их множество.
                // Если нужно: можно добавить колонку stock_qty для services.
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ServiceProduct getServiceById(int id) {
        String sql = "SELECT * FROM services WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ServiceProduct(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        1.0 // stockQty условно не отслеживается
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updateServiceStock(int id, double newStock) {
        // Если бы у услуг была колонка stock_qty, обновили бы так:
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
