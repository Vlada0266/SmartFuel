package com.example.fuel.test;

import com.example.fuel.db.DatabaseInitializer;
import com.example.fuel.model.Customer;
import com.example.fuel.model.PaymentMethod;
import com.example.fuel.service.CartService;
import com.example.fuel.service.CustomerService;
import com.example.fuel.service.PaymentService;
import com.example.fuel.service.ProductService;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private CustomerService customerService;
    private CartService cartService;
    private ProductService productService;

    @BeforeEach
    void resetDatabase() {
        // Удаляем старый файл базы данных
        File dbFile = new File("smart_fuel.db");
        if (dbFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dbFile.delete();
        }

        // Инициализируем БД и сервисы
        DatabaseInitializer.initialize();

        productService = new ProductService();                  // Сначала создаем ProductService
        cartService = new CartService(productService);          // Затем передаём его в CartService
        customerService = new CustomerService();
        paymentService = new PaymentService();
    }

    @Test
    void testPayPartialWithSufficientCash() {
        boolean result = paymentService.payPartial(1, PaymentMethod.Наличные, 500.0);
        assertTrue(result);

        Customer c = customerService.getCustomer(1);
        assertEquals(500.0, c.getWalletBalance(), 0.001);
    }

    @Test
    void testPayPartialWithInsufficientCash() {
        boolean result = paymentService.payPartial(1, PaymentMethod.Наличные, 1500.0);
        assertFalse(result);

        Customer c = customerService.getCustomer(1);
        assertEquals(1000.0, c.getWalletBalance(), 0.001);
    }

    @Test
    void testPayExactAmountWithMethod_SuccessAndFail() {
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 1, 2.0));
        double sum = cartService.calculateCartTotal(1);
        assertEquals(112.0, sum, 0.001);

        boolean ok = paymentService.payExactAmountWithMethod(1, PaymentMethod.Наличные, sum);
        assertTrue(ok);
        assertTrue(cartService.getCartItems(1).isEmpty());

        Customer c = customerService.getCustomer(1);
        assertEquals(888.0, c.getWalletBalance(), 0.001);

        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 2, 1000.0));
        double largeSum = cartService.calculateCartTotal(1);
        assertTrue(largeSum > 888.0);

        boolean fail = paymentService.payExactAmountWithMethod(1, PaymentMethod.Карта, largeSum);
        assertFalse(fail);
        assertFalse(cartService.getCartItems(1).isEmpty());
    }

    @Test
    void testPayExactAmountCombined_SucceedsWhenCombinedFundsSufficient() {
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 2, 10.0));
        double sum = cartService.calculateCartTotal(1);
        assertEquals(500.0, sum, 0.001);

        boolean ok = paymentService.payExactAmountCombined(1, sum);
        assertTrue(ok);
        assertTrue(cartService.getCartItems(1).isEmpty());

        Customer c = customerService.getCustomer(1);
        assertEquals(500.0, c.getWalletBalance(), 0.001);
    }

    @Test
    void testPayExactAmountCombined_FailsWhenNotEnoughTotalFunds() {
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 1, 1000.0));
        double sum = cartService.calculateCartTotal(1);
        assertTrue(sum > 3150.0);

        boolean ok = paymentService.payExactAmountCombined(1, sum);
        assertFalse(ok);
        assertFalse(cartService.getCartItems(1).isEmpty());
    }

    @Test
    void testPayPartialWithBonusAndCardMix() {
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 2, 2.0)); // 100₽
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "SERVICE", 1, 1.0)); // 300₽
        double sum = cartService.calculateCartTotal(1);
        assertEquals(400.0, sum, 0.001);

        boolean okBonus = paymentService.payPartial(1, PaymentMethod.Бонусы, 150.0);
        assertTrue(okBonus);

        Customer afterBonus = customerService.getCustomer(1);
        assertEquals(0.0, afterBonus.getBonusPoints(), 0.001);

        boolean okCard = paymentService.payPartial(1, PaymentMethod.Карта, 250.0);
        assertTrue(okCard);

        Customer finalCustomer = customerService.getCustomer(1);
        assertEquals(2000 - 250, finalCustomer.getCardBalance(), 0.001);
        assertEquals(1000, finalCustomer.getWalletBalance(), 0.001);

        assertEquals(2, cartService.getCartItems(1).size());
    }
}
