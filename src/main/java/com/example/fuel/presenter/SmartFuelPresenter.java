package com.example.fuel.presenter;

import com.example.fuel.model.CartItem;
import com.example.fuel.model.Customer;
import com.example.fuel.model.FuelProduct;
import com.example.fuel.model.PaymentMethod;
import com.example.fuel.model.ServiceProduct;
import com.example.fuel.service.CartService;
import com.example.fuel.service.CustomerService;
import com.example.fuel.service.PaymentService;
import com.example.fuel.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Presenter в паттерне MVP: хранит информацию о частичных платежах (paidMap),
 * чтобы UI мог показывать НЕ полную сумму, а остаток к оплате.
 */
public class SmartFuelPresenter {

    private final ProductService productService = new ProductService();
    private final CustomerService customerService = new CustomerService();
    private final CartService cartService = new CartService();
    private final PaymentService paymentService = new PaymentService();

    /** Сколько уже оплатил клиент частями (по customerId). */
    private final Map<Integer, Double> paidMap = new HashMap<>();

    // 1) Продукты и услуги

    public List<FuelProduct> getAllFuel() {
        return productService.getAllFuel();
    }

    public List<ServiceProduct> getAllServices() {
        return productService.getAllServices();
    }

    // 2) Корзина

    /** Добавляет элемент и сбрасывает “paid” (т.к. счёт изменился). */
    public void addToCart(int customerId, String itemType, int itemId, double quantity) {
        cartService.addToCart(new CartItem(customerId, itemType, itemId, quantity));
        resetPaid(customerId);
    }

    /** Удалить элемент из корзины по внутреннему ID записи и сбрасывает paid. */
    public void removeCartItemById(int cartItemId, int customerId) {
        cartService.removeCartItem(cartItemId);
        resetPaid(customerId);
    }

    /** Удалить по (customerId, itemType, itemId) и сбросить paid. */
    public void removeFromCart(int customerId, String itemType, int itemId) {
        cartService.removeFromCart(customerId, itemType, itemId);
        resetPaid(customerId);
    }

    /** Полностью очистить корзину и сбросить paid. */
    public void clearCart(int customerId) {
        cartService.clearCart(customerId);
        resetPaid(customerId);
    }

    /** Возвращает все элементы корзины */
    public List<CartItem> getCartItems(int customerId) {
        return cartService.getCartItems(customerId);
    }

    /** Полная стоимость корзины (не учитывая частичные платежи) */
    public double getCartTotal(int customerId) {
        return cartService.calculateCartTotal(customerId, productService);
    }

    /** Сброс "оплачено частями" для данного клиента */
    private void resetPaid(int customerId) {
        paidMap.put(customerId, 0.0);
    }

    /** Возвращает, сколько клиент уже оплатил частями (0.0, если не было) */
    public double getPaid(int customerId) {
        return paidMap.getOrDefault(customerId, 0.0);
    }

    /** Остаток к оплате = полная сумма − уже оплачено частями */
    public double getRemaining(int customerId) {
        double total = getCartTotal(customerId);
        double paid = getPaid(customerId);
        double remaining = total - paid;
        return remaining < 0 ? 0 : remaining;
    }

    // 3) Полная оплата

    /**
     * Полная оплата одним способом: списывает РОВНО остаток (getRemaining),
     * сбрасывает paid, очищает корзину и списывает товары со склада.
     */
    public boolean checkoutFullWithMethod(int customerId, PaymentMethod method) {
        double remaining = getRemaining(customerId);
        if (remaining <= 0) return false;

        // пытаемся оплатить ровно оставшуюся сумму одним методом
        boolean ok = paymentService.payExactAmountWithMethod(customerId, method, remaining);
        if (!ok) return false;

        // успешная оплата: сброс этих значений и очистка
        resetPaid(customerId);
        // списание со склада и очистка корзины происходит внутри payExactAmountWithMethod
        return true;
    }

    /**
     * Полная комбинированная оплата (cash→card→bonus) ровно оставшейся суммы.
     */
    public boolean checkoutFullCombined(int customerId) {
        double remaining = getRemaining(customerId);
        if (remaining <= 0) return false;

        boolean ok = paymentService.payExactAmountCombined(customerId, remaining);
        if (!ok) return false;

        resetPaid(customerId);
        return true;
    }

    // 4) Частичная оплата

    /**
     * Частичная оплата: списывает сумму amount с указанного метода.
     * Если успешно, увеличиваем paid и возвращаем true.
     */
    public boolean checkoutPartial(int customerId, PaymentMethod method, double amount) {
        if (amount <= 0) return false;

        double remaining = getRemaining(customerId);
        if (amount > remaining) {
            // Можно запретить платить больше, чем остаток
            return false;
        }

        boolean ok = paymentService.payPartial(customerId, method, amount);
        if (!ok) return false;

        // Увеличиваем сумму, которую клиент уже оплатил
        double newPaid = getPaid(customerId) + amount;
        paidMap.put(customerId, newPaid);
        return true;
    }

    // 5) Клиент

    public Customer getCustomer(int customerId) {
        return customerService.getCustomer(customerId);
    }
}
