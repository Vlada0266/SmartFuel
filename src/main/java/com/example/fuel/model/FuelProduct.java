package com.example.fuel.model;

/**
 * FuelProduct — модель для топлива (хранится в таблице products).
 * Наследуется от Product.
 */
public class FuelProduct extends Product {

    public FuelProduct(int id, String name, double price, double stockQty) {
        super(id, name, price, stockQty);
    }

    @Override
    public String getProductType() {
        return "Fuel";
    }
}
