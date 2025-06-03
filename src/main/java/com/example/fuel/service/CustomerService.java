package com.example.fuel.service;


import com.example.fuel.DAO.CustomerDAO;
import com.example.fuel.DAO.CustomerDAOImpl;
import com.example.fuel.model.Customer;

/**
 * Сервис для работы с клиентом.
 */
public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAOImpl();

    // Получить клиента по ID
    public Customer getCustomer(int id) {
        return customerDAO.getById(id);
    }

    // Обновить балансы клиента (после оплаты)
    public void updateCustomer(Customer customer) {
        customerDAO.update(customer);
    }

    // Проверить, достаточно ли денег в кошельке
    public boolean canPayWithCash(Customer customer, double amount) {
        return customer.getWalletBalance() >= amount;
    }

    // Проверить, достаточно ли денег на карте
    public boolean canPayWithCard(Customer customer, double amount) {
        return customer.getCardBalance() >= amount;
    }

    // Проверить, достаточно ли бонусов
    public boolean canPayWithBonus(Customer customer, double amount) {
        return customer.getBonusPoints() >= amount;
    }
}
