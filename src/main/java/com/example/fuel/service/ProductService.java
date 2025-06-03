package com.example.fuel.service;


import com.example.fuel.DAO.ProductDAO;
import com.example.fuel.DAO.ProductDAOImpl;
import com.example.fuel.model.FuelProduct;
import com.example.fuel.model.ServiceProduct;

import java.util.List;

/**
 * Сервис для работы с продуктами и услугами.
 */
public class ProductService {

    private final ProductDAO productDAO = new ProductDAOImpl();

    // Получить все виды топлива
    public List<FuelProduct> getAllFuel() {
        return productDAO.getAllFuelProducts();
    }

    // Получить конкретное топливо по ID
    public FuelProduct getFuelById(int id) {
        return productDAO.getFuelProductById(id);
    }

    // Уменьшить запас топлива
    public void decreaseFuelStock(int id, double amount) {
        FuelProduct p = productDAO.getFuelProductById(id);
        if (p != null) {
            double newStock = p.getStockQty() - amount;
            if (newStock < 0) newStock = 0;
            productDAO.updateFuelStock(id, newStock);
        }
    }

    // Получить все услуги
    public List<ServiceProduct> getAllServices() {
        return productDAO.getAllServiceProducts();
    }

    // Получить конкретную услугу по ID
    public ServiceProduct getServiceById(int id) {
        return productDAO.getServiceById(id);
    }

    // (необязательный) обновить запас услуги
    public void decreaseServiceStock(int id) {
        productDAO.updateServiceStock(id, 0); // если есть logic
    }
}
