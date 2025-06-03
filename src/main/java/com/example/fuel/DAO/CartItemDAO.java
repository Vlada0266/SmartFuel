package com.example.fuel.DAO;

import com.example.fuel.model.CartItem;
import java.util.List;

/**
 * DAO-интерфейс для элементов корзины.
 */
public interface CartItemDAO {
    List<CartItem> getByCustomerId(int customerId);
    void insert(CartItem item);
    void delete(int id);
    void deleteAllByCustomerId(int customerId);
    void deleteByItem(int customerId, String itemType, int itemId);
}
