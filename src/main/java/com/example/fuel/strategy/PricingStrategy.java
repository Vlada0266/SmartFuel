package com.example.fuel.strategy;

import com.example.fuel.model.CartItem;

/**
 * Интерфейс стратегии расчёта стоимости элемента корзины.
 */
public interface PricingStrategy {

    /**
     * Рассчитать стоимость элемента корзины.
     * @param item элемент корзины
     * @return стоимость
     */
    double calculatePrice(CartItem item);
}
