package com.example.fuel.model;

/**
 * Конкретный товар — "услуга".
 * isWeighted всегда false, quantity = целое число (1 услуга).
 */
public class ServiceProduct extends Product {

    public ServiceProduct() {
        super();
        setWeighted(false);
    }

    public ServiceProduct(int id, String name, double price, double stockQty) {
        super(id, name, price, false, stockQty);
    }

    // quantity всегда 1; stockQty отражает сколько услуг осталось.
}
