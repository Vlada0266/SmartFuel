package com.example.fuel.model;

/**
 * Конкретный товар — "топливо".
 * isWeighted всегда true.
 */
public class FuelProduct extends Product {

    public FuelProduct() {
        super();
        setWeighted(true);
    }

    public FuelProduct(int id, String name, double price, double stockQty) {
        super(id, name, price, true, stockQty);
    }

    // Никаких дополнительных полей не нужно.
}
