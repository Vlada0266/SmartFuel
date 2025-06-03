package com.example.fuel.DAO;

import com.example.fuel.model.FuelProduct;
import com.example.fuel.model.ServiceProduct;
import java.util.List;

/**
 * Интерфейс DAO для продуктов и услуг.
 * Мы храним топливо в таблице products и услуги в таблице services.
 */
public interface ProductDAO {
    // Получить все FuelProduct из таблицы products
    List<FuelProduct> getAllFuelProducts();

    // Получить FuelProduct по ID
    FuelProduct getFuelProductById(int id);

    // Обновить количество топлива (stockQty) у конкретного FuelProduct
    void updateFuelStock(int id, double newStock);

    // Получить все ServiceProduct из таблицы services
    List<ServiceProduct> getAllServiceProducts();

    // Получить ServiceProduct по ID
    ServiceProduct getServiceById(int id);

    // Обновить количество услуг (stockQty) у конкретного ServiceProduct
    void updateServiceStock(int id, double newStock);
}
