package com.example.fuel.model;

/**
 * Абстрактный класс Product — общая основа для всех товаров и услуг.
 * У каждого продукта есть ID, название, цена и количество (stockQty).
 * Наследуется классами FuelProduct и ServiceProduct.
 */
public abstract class Product {
    protected int id;
    protected String name;
    protected double price;
    protected double stockQty;

    public Product(int id, String name, double price, double stockQty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQty = stockQty;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getStockQty() {
        return stockQty;
    }

    public void setStockQty(double stockQty) {
        this.stockQty = stockQty;
    }

    /**
     * Абстрактный метод — возвращает тип продукта (используется при отображении или логике).
     */
    public abstract String getProductType();
}
