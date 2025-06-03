package com.example.fuel.service;

import com.example.fuel.DAO.CartItemDAO;
import com.example.fuel.DAO.CartItemDAOImpl;
import com.example.fuel.model.CartItem;
import com.example.fuel.strategy.FuelPricingStrategy;
import com.example.fuel.strategy.PricingStrategy;
import com.example.fuel.strategy.ServicePricingStrategy;

import java.util.List;

/**
 * Сервис работы с корзиной: добавление, удаление, просмотр.
 * Теперь использует паттерн Стратегия для расчёта цены элементов корзины.
 */
public class CartService {

    private final CartItemDAO cartDao = new CartItemDAOImpl();

    private final FuelPricingStrategy fuelStrategy;
    private final ServicePricingStrategy serviceStrategy;

    public CartService(ProductService productService) {
        // создаём стратегии, передавая сервис продуктов для доступа к данным
        this.fuelStrategy = new FuelPricingStrategy(productService);
        this.serviceStrategy = new ServicePricingStrategy(productService);
    }

    public List<CartItem> getCartItems(int customerId) {
        return cartDao.getByCustomerId(customerId);
    }

    public void addToCart(CartItem item) {
        cartDao.insert(item);
    }

    public void removeCartItem(int id) {
        cartDao.delete(id);
    }

    public void removeFromCart(int customerId, String itemType, int itemId) {
        cartDao.deleteByItem(customerId, itemType, itemId);
    }

    public void clearCart(int customerId) {
        cartDao.deleteAllByCustomerId(customerId);
    }

    /**
     * Рассчитать итоговую сумму корзины, используя паттерн Стратегия.
     */
    public double calculateCartTotal(int customerId) {
        double total = 0.0;
        List<CartItem> items = getCartItems(customerId);
        for (CartItem item : items) {
            PricingStrategy strategy = selectStrategy(item.getItemType());
            if (strategy != null) {
                total += strategy.calculatePrice(item);
            }
        }
        return total;
    }

    /**
     * Выбрать стратегию расчёта цены по типу товара/услуги.
     */
    private PricingStrategy selectStrategy(String itemType) {
        if ("PRODUCT".equals(itemType)) {
            return fuelStrategy;
        } else if ("SERVICE".equals(itemType)) {
            return serviceStrategy;
        }
        return null; // можно кинуть исключение, если тип неизвестен
    }
}
