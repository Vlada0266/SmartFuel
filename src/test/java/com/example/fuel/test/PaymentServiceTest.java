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
        // Удаляем старый файл
        File dbFile = new File("smart_fuel.db");
        if (dbFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dbFile.delete();
        }
        // Инициализируем новую БД
        DatabaseInitializer.initialize();

        paymentService = new PaymentService();
        customerService = new CustomerService();
        cartService = new CartService();
        productService = new ProductService();
    }

    @Test
    void testPayPartialWithSufficientCash() {
        // Изначальный баланс клиента (ID=1): наличные=1000, карта=2000, бонусы=150
        // Попробуем списать 500.0 наличными
        boolean result = paymentService.payPartial(1, PaymentMethod.Наличные, 500.0);
        assertTrue(result, "Должна пройти частичная оплата наличными при достаточном балансе.");

        // Баланс наличных теперь 1000 - 500 = 500
        Customer c = customerService.getCustomer(1);
        assertNotNull(c);
        assertEquals(500.0, c.getWalletBalance(), 0.001);
        assertEquals(2000.0, c.getCardBalance(), 0.001);
        assertEquals(150.0, c.getBonusPoints(), 0.001);
    }

    @Test
    void testPayPartialWithInsufficientCash() {
        // Баланс наличных = 1000. Попытаемся списать 1500 наличными.
        boolean result = paymentService.payPartial(1, PaymentMethod.Наличные, 1500.0);
        assertFalse(result, "Частичная оплата наличными должна провалиться при недостаточном балансе.");
        // Баланс при этом не меняется
        Customer c = customerService.getCustomer(1);
        assertNotNull(c);
        assertEquals(1000.0, c.getWalletBalance(), 0.001);
    }

    @Test
    void testPayExactAmountWithMethod_SuccessAndFail() {
        // Чтобы протестировать полную оплату, заранее добавим что-то в корзину:
        // Добавим 2 литра топлива ID=1 (56*2 = 112)
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 1, 2.0));

        // Остаток к оплате = 112.0
        double sum = cartService.calculateCartTotal(1, productService);
        assertEquals(112.0, sum, 0.001);

        // Полная оплата наличными (1000 наличных) – должно пройти, и корзина очиститься
        boolean ok = paymentService.payExactAmountWithMethod(1, PaymentMethod.Наличные, sum);
        assertTrue(ok, "Полная оплата наличными должна пройти при достаточном балансе.");

        // После этого корзина пуста
        assertTrue(cartService.getCartItems(1).isEmpty(), "Корзина должна очиститься после успешной полной оплаты.");

        // Баланс наличных стал 1000 - 112 = 888
        Customer c = customerService.getCustomer(1);
        assertEquals(888.0, c.getWalletBalance(), 0.001);

        // Теперь протестируем неуспешную оплату: заново положим в корзину, но запросим сумму больше, чем у клиента
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 2, 1000.0));
        // 1000 л * 50 (цена дизеля) = 50000 > 888 (наличные)
        double largeSum = cartService.calculateCartTotal(1, productService);
        assertTrue(largeSum > 888.0);

        // Попытка полной оплаты наличными провалится
        boolean fail = paymentService.payExactAmountWithMethod(1, PaymentMethod.Карта, largeSum);
        assertFalse(fail, "Полная оплата наличными должна провалиться при недостаточном балансе.");
        // Все ещё должны остаться записи в корзине
        assertFalse(cartService.getCartItems(1).isEmpty(), "Корзина не должна очищаться при неудачной оплате.");
    }

    @Test
    void testPayExactAmountCombined_SucceedsWhenCombinedFundsSufficient() {
        // Положим в корзину 10 литров дизеля (ID=2, цена=50 → 10*50 = 500)
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 2, 10.0));
        double sum = cartService.calculateCartTotal(1, productService);
        assertEquals(500.0, sum, 0.001);

        // У клиента: наличные = 1000, карта = 2000, бонусы = 150
        // Комбинированная оплата сначала снимет 500 из наличных:
        boolean ok = paymentService.payExactAmountCombined(1, sum);
        assertTrue(ok, "Комбинированная оплата должна пройти при достаточных комбинированных средствах.");

        // После списания корзина должна очиститься
        assertTrue(cartService.getCartItems(1).isEmpty());

        // Баланс наличных станет 1000 - 500 = 500, карта и бонусы не тронуты
        Customer c = customerService.getCustomer(1);
        assertEquals(500.0, c.getWalletBalance(), 0.001);
        assertEquals(2000.0, c.getCardBalance(), 0.001);
        assertEquals(150.0, c.getBonusPoints(), 0.001);
    }

    @Test
    void testPayExactAmountCombined_FailsWhenNotEnoughTotalFunds() {
        // Добавим в корзину нечто дорогое: 1000 л бензина (ID=1, цена=56) → 56000
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 1, 1000.0));
        double sum = cartService.calculateCartTotal(1, productService);
        assertTrue(sum > 1000 + 2000 + 150);

        boolean ok = paymentService.payExactAmountCombined(1, sum);
        assertFalse(ok, "Комбинированная оплата должна провалиться, поскольку суммарные средства < 56000.");

        // Корзина остаётся нетронутой
        assertFalse(cartService.getCartItems(1).isEmpty());
    }

    @Test
    void testPayPartialWithBonusAndCardMix() {
        // Добавим услугу ID=1 (300) и 2 л дизеля (ID=2, цена=50 → 100)
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "PRODUCT", 2, 2.0)); // 100₽
        cartService.addToCart(new com.example.fuel.model.CartItem(1, "SERVICE", 1, 1.0)); // 300₽
        double sum = cartService.calculateCartTotal(1, productService);
        assertEquals(400.0, sum, 0.001);

        // Попытаемся частично оплатить 150 бонусами (у клиента бонусов 150)
        boolean okBonus = paymentService.payPartial(1, PaymentMethod.Бонусы, 150.0);
        assertTrue(okBonus, "Частичная оплата бонусами должна пройти при достаточном бонусном балансе.");

        // Баланс бонусов теперь 0
        Customer afterBonus = customerService.getCustomer(1);
        assertEquals(0.0, afterBonus.getBonusPoints(), 0.001);

        // Остаток к оплате (в логике презентера) будет sum – 150 = 250 (но здесь лишь проверяем списание)
        // Теперь снимем 250 с карты
        boolean okCard = paymentService.payPartial(1, PaymentMethod.Карта, 250.0);
        assertTrue(okCard, "Частичная оплата с карты должна пройти при достаточном остатке на карте.");

        Customer finalCustomer = customerService.getCustomer(1);
        assertEquals(2000 - 250, finalCustomer.getCardBalance(), 0.001);
        assertEquals(1000, finalCustomer.getWalletBalance(), 0.001); // наличные не тронуты

        // Элемент в корзине при частичной оплате не удаляется (это логика презентера),
        // но здесь мы тестируем лишь списание средств, поэтому корзина всё ещё должна содержать 2 элемента.
        assertEquals(2, cartService.getCartItems(1).size());
    }
}
