package com.example.fuel.test;


import com.example.fuel.db.DatabaseInitializer;
import com.example.fuel.model.CartItem;
import com.example.fuel.service.CartService;
import com.example.fuel.service.ProductService;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    private CartService cartService;
    private ProductService productService;

    @BeforeEach
    void resetDatabase() {
        // Удаляем файл smart_fuel.db, если он существует, чтобы начать с чистого состояния
        File dbFile = new File("smart_fuel.db");
        if (dbFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dbFile.delete();
        }
        // Инициализируем структуру и начальные данные
        DatabaseInitializer.initialize();

        cartService = new CartService();
        productService = new ProductService();
    }

    @Test
    void testAddAndRemoveItems() {
        // Изначально корзина должна быть пуста
        List<CartItem> initial = cartService.getCartItems(1);
        assertTrue(initial.isEmpty(), "Корзина должна быть пуста после инициализации.");

        // Добавим в корзину топливо (ID = 1, взвешенное)
        CartItem itemFuel = new CartItem(1, "PRODUCT", 1, 10.0);
        cartService.addToCart(itemFuel);

        List<CartItem> afterAdd = cartService.getCartItems(1);
        assertEquals(1, afterAdd.size(), "Должен быть один элемент в корзине после добавления.");
        CartItem retrieved = afterAdd.get(0);
        assertEquals("PRODUCT", retrieved.getItemType());
        assertEquals(1, retrieved.getItemId());
        assertEquals(10.0, retrieved.getQuantity());

        // Удалим этот элемент по внутреннему ID
        cartService.removeCartItem(retrieved.getId());
        List<CartItem> afterRemove = cartService.getCartItems(1);
        assertTrue(afterRemove.isEmpty(), "Корзина должна быть пуста после удаления элемента.");
    }

    @Test
    void testCalculateCartTotalWithFuelAndService() {
        // Предположим, что в initial data есть топливо ID=1 (цена=56.0) и услуга ID=1 (цена=300.0)
        // Добавляем 5 литров топлива ID=1 (цена 56.0 → 5 * 56.0 = 280.0)
        CartItem fuel5 = new CartItem(1, "PRODUCT", 1, 5.0);
        cartService.addToCart(fuel5);

        // Добавляем услугу ID=1 (цена=300.0, quantity=1)
        CartItem service1 = new CartItem(1, "SERVICE", 1, 1.0);
        cartService.addToCart(service1);

        double total = cartService.calculateCartTotal(1, productService);
        // Ожидаем 280.0 + 300.0 = 580.0
        assertEquals(580.0, total, 0.001, "Итоговая сумма корзины должна быть 580.0.");
    }

    @Test
    void testClearCart() {
        // Добавляем элемент в корзину
        CartItem fuel3 = new CartItem(1, "PRODUCT", 2, 3.0); // ID=2: дизель, цена=50.0
        cartService.addToCart(fuel3);
        List<CartItem> beforeClear = cartService.getCartItems(1);
        assertFalse(beforeClear.isEmpty());

        // Очистим корзину
        cartService.clearCart(1);
        List<CartItem> afterClear = cartService.getCartItems(1);
        assertTrue(afterClear.isEmpty(), "Корзина должна быть пустой после clearCart.");
    }
}
