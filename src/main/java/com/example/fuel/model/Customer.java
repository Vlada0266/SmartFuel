package com.example.fuel.model;

/**
 * Клиент: хранит балансы и бонусы.
 */
public class Customer {
    private int id;
    private String name;
    private double walletBalance; // наличные
    private double cardBalance;   // средства на карте
    private double bonusPoints;   // бонусы

    public Customer() {}

    public Customer(int id, String name, double walletBalance, double cardBalance, double bonusPoints) {
        this.id = id;
        this.name = name;
        this.walletBalance = walletBalance;
        this.cardBalance = cardBalance;
        this.bonusPoints = bonusPoints;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public double getCardBalance() {
        return cardBalance;
    }

    public double getBonusPoints() {
        return bonusPoints;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public void setCardBalance(double cardBalance) {
        this.cardBalance = cardBalance;
    }

    public void setBonusPoints(double bonusPoints) {
        this.bonusPoints = bonusPoints;
    }

    @Override
    public String toString() {
        return String.format("%s [ID=%d, cash=%.2f, card=%.2f, bonus=%.2f]",
                name, id, walletBalance, cardBalance, bonusPoints);
    }
}
