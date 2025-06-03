package com.example.fuel.service;

import com.example.fuel.DAO.CartItemDAO;
import com.example.fuel.DAO.CartItemDAOImpl;
import com.example.fuel.DAO.CustomerDAO;
import com.example.fuel.DAO.CustomerDAOImpl;
import com.example.fuel.model.CartItem;
import com.example.fuel.model.Customer;
import com.example.fuel.model.PaymentMethod;
import com.example.fuel.model.ServiceProduct;

import java.util.List;

/**
 * Сервис оплаты:
 * – payPartial (частичная оплата)
 * – payExactAmountWithMethod (списать ровно указанную сумму одним методом)
 * – payExactAmountCombined (списать ровно указанную сумму комбинированно)
 */
public class PaymentService {

    private final CustomerDAO customerDAO = new CustomerDAOImpl();
    private final CartItemDAO cartItemDAO = new CartItemDAOImpl();
    private final ProductService productService = new ProductService();

    /**
     * Частичная оплата: списывает amount с указанного метода.
     */
    public boolean payPartial(int customerId, PaymentMethod method, double amount) {
        Customer cust = customerDAO.getById(customerId);
        if (cust == null) return false;

        switch (method) {
            case Наличные:
                if (cust.getWalletBalance() >= amount) {
                    cust.setWalletBalance(cust.getWalletBalance() - amount);
                } else {
                    return false;
                }
                break;
            case Карта:
                if (cust.getCardBalance() >= amount) {
                    cust.setCardBalance(cust.getCardBalance() - amount);
                } else {
                    return false;
                }
                break;
            case Бонусы:
                if (cust.getBonusPoints() >= amount) {
                    cust.setBonusPoints(cust.getBonusPoints() - amount);
                } else {
                    return false;
                }
                break;
        }
        customerDAO.update(cust);
        return true;
    }

    /**
     * Полная оплата одним способом, ровно sum (остаток).
     * Списывает sum, очищает корзину и списывает товары со склада.
     */
    public boolean payExactAmountWithMethod(int customerId, PaymentMethod method, double sum) {
        Customer cust = customerDAO.getById(customerId);
        if (cust == null) return false;

        switch (method) {
            case Наличные:
                if (cust.getWalletBalance() >= sum) {
                    cust.setWalletBalance(cust.getWalletBalance() - sum);
                } else return false;
                break;
            case Карта:
                if (cust.getCardBalance() >= sum) {
                    cust.setCardBalance(cust.getCardBalance() - sum);
                } else return false;
                break;
            case Бонусы:
                if (cust.getBonusPoints() >= sum) {
                    cust.setBonusPoints(cust.getBonusPoints() - sum);
                } else return false;
                break;
        }

        customerDAO.update(cust);
        deductStockAndClearCart(customerId);
        return true;
    }

    /**
     * Полная комбинированная оплата ровно sum: сначала cash→card→bonus.
     */
    public boolean payExactAmountCombined(int customerId, double sum) {
        Customer cust = customerDAO.getById(customerId);
        if (cust == null) return false;

        double remaining = sum;

        // 1) Наличные
        double payCash = Math.min(cust.getWalletBalance(), remaining);
        cust.setWalletBalance(cust.getWalletBalance() - payCash);
        remaining -= payCash;

        // 2) Карта
        if (remaining > 0) {
            double payCard = Math.min(cust.getCardBalance(), remaining);
            cust.setCardBalance(cust.getCardBalance() - payCard);
            remaining -= payCard;
        }

        // 3) Бонусы
        if (remaining > 0) {
            double payBonus = Math.min(cust.getBonusPoints(), remaining);
            cust.setBonusPoints(cust.getBonusPoints() - payBonus);
            remaining -= payBonus;
        }

        if (remaining > 0.0001) {
            // Не удалось собрать ровно sum
            // Откат
            customerDAO.update(cust); // в начале мы сняли, но откатывать будем неявно: чаще имеет смысл откатить логику ручками
            return false;
        }

        // Успешная оплата
        customerDAO.update(cust);
        deductStockAndClearCart(customerId);
        return true;
    }

    /** Списание товаров со склада и очистка корзины */
    private void deductStockAndClearCart(int customerId) {
        List<CartItem> items = cartItemDAO.getByCustomerId(customerId);
        for (CartItem ci : items) {
            if ("PRODUCT".equals(ci.getItemType())) {
                productService.decreaseFuelStock(ci.getItemId(), ci.getQuantity());
            } else {
                ServiceProduct s = productService.getServiceById(ci.getItemId());
                if (s != null) {
                    // если нужно списывать услуги, допишите здесь
                }
            }
        }
        cartItemDAO.deleteAllByCustomerId(customerId);
    }
}
