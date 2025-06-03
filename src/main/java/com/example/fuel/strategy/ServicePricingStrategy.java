package com.example.fuel.strategy;

import com.example.fuel.model.CartItem;
import com.example.fuel.model.ServiceProduct;
import com.example.fuel.service.ProductService;

/**
 * Стратегия расчёта стоимости для услуги (ServiceProduct).
 * Стоимость — фиксированная цена услуги.
 */
public class ServicePricingStrategy implements PricingStrategy {

    private final ProductService productService;

    public ServicePricingStrategy(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public double calculatePrice(CartItem item) {
        ServiceProduct service = productService.getServiceById(item.getItemId());
        if (service == null) {
            throw new IllegalArgumentException("Услуга с id " + item.getItemId() + " не найдена");
        }
        return service.getPrice();
    }
}
