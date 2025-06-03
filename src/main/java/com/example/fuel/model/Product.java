package com.example.fuel.model;

/**
 * Абстрактная сущность "товар".
 * Поле isWeighted = true → нужно указывать количество (литры).
 */
public abstract class Product {
    private int id;
    private String name;
    private double price;
    private boolean isWeighted;
    private double stockQty;

    public Product() {}

    public Product(int id, String name, double price, boolean isWeighted, double stockQty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.isWeighted = isWeighted;
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

    public boolean isWeighted() {
        return isWeighted;
    }

    public double getStockQty() {
        return stockQty;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setWeighted(boolean weighted) {
        isWeighted = weighted;
    }

    public void setStockQty(double stockQty) {
        this.stockQty = stockQty;
    }

    @Override
    public String toString() {
        String weightedStr = isWeighted ? " (литры)" : " (шт.)";
        return String.format("%s [ID=%d, цена=%.2f%s, в наличии=%.2f]", name, id, price, weightedStr, stockQty);
    }
}
