package com.example.fuel.strategy;

import com.example.fuel.service.ProductService;

/**
 * Фабрика для получения стратегии расчёта стоимости
 * в зависимости от типа элемента корзины.
 */
public class PricingStrategyFactory {

    private final ProductService productService;

    public PricingStrategyFactory(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Получить стратегию расчёта стоимости для типа элемента ("PRODUCT" или "SERVICE").
     * @param itemType тип элемента корзины
     * @return объект стратегии
     */
    public PricingStrategy getStrategy(String itemType) {
        if ("PRODUCT".equalsIgnoreCase(itemType)) {
            return new FuelPricingStrategy(productService);
        } else if ("SERVICE".equalsIgnoreCase(itemType)) {
            return new ServicePricingStrategy(productService);
        } else {
            throw new IllegalArgumentException("Неизвестный тип элемента корзины: " + itemType);
        }
    }
}

