package com.example.fuel.model;

/**
 * Лог записи об оплате: сколько, какой метод.
 */
public class Payment {
    private int id;
    private int customerId;
    private PaymentMethod method;
    private double amount;

    public Payment() {}

    public Payment(int id, int customerId, PaymentMethod method, double amount) {
        this.id = id;
        this.customerId = customerId;
        this.method = method;
        this.amount = amount;
    }

    public Payment(int customerId, PaymentMethod method, double amount) {
        this(0, customerId, method, amount);
    }

    public int getId() {
        return id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public double getAmount() {
        return amount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
