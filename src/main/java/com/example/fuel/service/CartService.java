package com.example.fuel.service;


import com.example.fuel.DAO.CartItemDAO;
import com.example.fuel.DAO.CartItemDAOImpl;
import com.example.fuel.model.CartItem;

import java.util.List;

/**
 * Сервис работы с корзиной: добавление, удаление, просмотр.
 */
public class CartService {

    private final CartItemDAO cartDao = new CartItemDAOImpl();

    // Получить все элементы корзины для клиента
    public List<CartItem> getCartItems(int customerId) {
        return cartDao.getByCustomerId(customerId);
    }

    // Добавить товар или услугу в корзину
    public void addToCart(CartItem item) {
        cartDao.insert(item);
    }

    // Удалить элемент корзины по его ID
    public void removeCartItem(int id) {
        cartDao.delete(id);
    }

    // Удалить по типу и ID товара/услуги
    public void removeFromCart(int customerId, String itemType, int itemId) {
        cartDao.deleteByItem(customerId, itemType, itemId);
    }

    // Очистить корзину клиента
    public void clearCart(int customerId) {
        cartDao.deleteAllByCustomerId(customerId);
    }

    // Рассчитать итоговую сумму корзины:
    // для каждого элемента берём цену (из ProductService) * quantity
    public double calculateCartTotal(int customerId, ProductService productService) {
        double total = 0.0;
        List<CartItem> items = getCartItems(customerId);
        for (CartItem item : items) {
            if ("PRODUCT".equals(item.getItemType())) {
                var p = productService.getFuelById(item.getItemId());
                if (p != null) {
                    total += p.getPrice() * item.getQuantity();
                }
            } else {
                var s = productService.getServiceById(item.getItemId());
                if (s != null) {
                    total += s.getPrice() * item.getQuantity(); // quantity обычно = 1
                }
            }
        }
        return total;
    }
}
