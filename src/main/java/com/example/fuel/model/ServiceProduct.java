package com.example.fuel.model;

/**
 * ServiceProduct — модель для услуги (хранится в таблице services).
 * Наследуется от Product.
 */
public class ServiceProduct extends Product {

    public ServiceProduct(int id, String name, double price, double stockQty) {
        super(id, name, price, stockQty);
    }

    @Override
    public String getProductType() {
        return "Service";
    }
}
