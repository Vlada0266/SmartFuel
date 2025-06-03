package com.example.fuel.model;

/**
 * Элемент корзины:
 * либо содержит FuelProduct с указанным количеством литров,
 * либо ServiceProduct (quantity = 1 всегда).
 */
public class CartItem {
    private int id;
    private int customerId;
    private String itemType;   // "PRODUCT" или "SERVICE"
    private int itemId;        // id из таблицы products или services
    private double quantity;   // литры или 1.0 для услуги

    public CartItem() {}

    public CartItem(int id, int customerId, String itemType, int itemId, double quantity) {
        this.id = id;
        this.customerId = customerId;
        this.itemType = itemType;
        this.itemId = itemId;
        this.quantity = quantity;
    }

    public CartItem(int customerId, String itemType, int itemId, double quantity) {
        this(0, customerId, itemType, itemId, quantity);
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getItemType() {
        return itemType;
    }

    public int getItemId() {
        return itemId;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("CartItem[id=%d, customerId=%d, type=%s, itemId=%d, qty=%.2f]",
                id, customerId, itemType, itemId, quantity);
    }
}
