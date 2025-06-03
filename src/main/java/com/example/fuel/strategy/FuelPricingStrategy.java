package com.example.fuel.strategy;

import com.example.fuel.model.CartItem;
import com.example.fuel.model.FuelProduct;
import com.example.fuel.service.ProductService;

/**
 * Стратегия расчёта стоимости для топлива (FuelProduct).
 * Стоимость = цена за литр * количество литров.
 */
public class FuelPricingStrategy implements PricingStrategy {

    private final ProductService productService;

    public FuelPricingStrategy(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public double calculatePrice(CartItem item) {
        FuelProduct product = productService.getFuelById(item.getItemId());
        if (product == null) {
            throw new IllegalArgumentException("Топливо с id " + item.getItemId() + " не найдено");
        }
        return product.getPrice() * item.getQuantity();
    }
}
