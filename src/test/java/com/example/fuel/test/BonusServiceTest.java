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

class BonusServiceTest {

    private CustomerService customerService;
    private PaymentService paymentService;
    private CartService cartService;
    private ProductService productService;

    @BeforeEach
    void resetDatabase() {
        // Удаляем старую БД
        File dbFile = new File("smart_fuel.db");
        if (dbFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dbFile.delete();
        }
        // Создаём заново
        DatabaseInitializer.initialize();

        customerService = new CustomerService();
        paymentService = new PaymentService();
        productService = new ProductService();
        cartService = new CartService(productService);
    }

    @Test
    void testBonusDeductionWhenPayingWithBonus() {
        // У клиентa Изначально бонусы = 150.0
        Customer c = customerService.getCustomer(1);
        assertNotNull(c);
        assertEquals(150.0, c.getBonusPoints(), 0.001);

        // Положим в корзину услугу ID=2 (Мойка, цена=150)
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "SERVICE", 2, 1.0));
        double sum = cartService.calculateCartTotal(1);
        assertEquals(150.0, sum, 0.001);

        // Попробуем оплатить бонусами (у клиента бонусов 150, суммы 150 хватает)
        boolean ok = paymentService.payExactAmountWithMethod(1, PaymentMethod.Бонусы, sum);
        assertTrue(ok, "Оплата бонусами должна пройти при достаточном балансе бонусов.");

        // После списания бонусов должно остаться 150 - 150 = 0
        Customer after = customerService.getCustomer(1);
        assertEquals(0.0, after.getBonusPoints(), 0.001);

        // Корзина при успешной оплате «полным платежом» очищается
        assertTrue(cartService.getCartItems(1).isEmpty(), "Корзина должна очиститься после полной оплаты бонусами.");
    }

    @Test
    void testBonusDeduction_WhenNotEnoughBonus_Fails() {
        // Изначально бонусы = 150
        Customer c = customerService.getCustomer(1);
        assertNotNull(c);

        // Положим в корзину топливо 5 л ID=1 (цена 56*5 = 280) → ошибка при оплате бонусами (150 < 280)
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 1, 5.0));
        double sum = cartService.calculateCartTotal(1);
        assertEquals(280.0, sum, 0.001);

        boolean ok = paymentService.payExactAmountWithMethod(1, PaymentMethod.Бонусы, sum);
        assertFalse(ok, "Оплата бонусами должна провалиться, так как 150 < 280.");

        // Баланс бонусов не меняется (остается 150)
        Customer after = customerService.getCustomer(1);
        assertEquals(150.0, after.getBonusPoints(), 0.001);

        // Корзина не должна очищаться при неудачной оплате
        assertFalse(cartService.getCartItems(1).isEmpty());
    }
}
