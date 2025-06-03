package com.example.fuel.view;

import com.example.fuel.model.CartItem;
import com.example.fuel.model.FuelProduct;
import com.example.fuel.model.PaymentMethod;
import com.example.fuel.model.ServiceProduct;
import com.example.fuel.presenter.SmartFuelPresenter;
import com.example.fuel.db.DatabaseInitializer;
import com.example.fuel.model.Customer;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * JavaFX‐приложение SmartFuel (без FXML), с учётом того, что если при частичной оплате
 * остаток достигает нуля, корзина очищается и показывается успешное сообщение.
 */
public class MainApp extends Application {

    private SmartFuelPresenter presenter;
    private final int CUSTOMER_ID = 1;

    // Панель балансов
    private Label lblCash;
    private Label lblCard;
    private Label lblBonus;

    // Вкладка Топливо
    private TableView<FuelProduct> fuelTable;

    // Вкладка Услуги
    private TableView<ServiceProduct> serviceTable;

    // Вкладка Корзина
    private TableView<CartRow> cartTable;
    private Label lblTotal; // показывает остаток (из полной суммы)

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        presenter = new SmartFuelPresenter();

        // 1) Панель балансов
        HBox balanceBox = createBalanceBox();
        updateBalances();

        // 2) Вкладки: Топливо, Услуги, Корзина
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        Tab fuelTab = new Tab("Топливо", createFuelPane());
        Tab serviceTab = new Tab("Услуги", createServicePane());
        Tab cartTab = new Tab("Корзина", createCartPane());
        tabPane.getTabs().addAll(fuelTab, serviceTab, cartTab);

        // 3) Главная сцена
        BorderPane root = new BorderPane();
        root.setTop(balanceBox);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SmartFuel — Умная автозаправка");
        primaryStage.setResizable(false);
        primaryStage.show();

        // 4) Загрузка данных
        loadFuelData();
        loadServiceData();
        loadCartData();
    }

    // -----------------------------
    // Панель "Баланс клиента"
    // -----------------------------
    private HBox createBalanceBox() {
        HBox box = new HBox(20);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #eef;");

        lblCash = new Label();
        lblCard = new Label();
        lblBonus = new Label();

        lblCash.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        lblCard.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        lblBonus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        box.getChildren().addAll(
                new Label("Наличные:"), lblCash,
                new Label("На карте:"), lblCard,
                new Label("Бонусы:"), lblBonus
        );
        return box;
    }

    private void updateBalances() {
        Customer c = presenter.getCustomer(CUSTOMER_ID);
        if (c != null) {
            lblCash.setText(String.format("%.2f", c.getWalletBalance()));
            lblCard.setText(String.format("%.2f", c.getCardBalance()));
            lblBonus.setText(String.format("%.2f", c.getBonusPoints()));
        } else {
            lblCash.setText("—");
            lblCard.setText("—");
            lblBonus.setText("—");
        }
    }

    // -----------------------------
    // Панель "Топливо"
    // -----------------------------
    private BorderPane createFuelPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label lbl = new Label("Доступное топливо:");
        lbl.setStyle("-fx-font-size: 16px; -fx-padding: 0 0 10 0;");
        root.setTop(lbl);

        fuelTable = new TableView<>();
        fuelTable.setPrefHeight(400);

        TableColumn<FuelProduct, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<FuelProduct, String> colName = new TableColumn<>("Название");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colName.setPrefWidth(200);

        TableColumn<FuelProduct, Double> colPrice = new TableColumn<>("Цена/л");
        colPrice.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getPrice()).asObject());
        colPrice.setPrefWidth(100);

        TableColumn<FuelProduct, Double> colStock = new TableColumn<>("В наличии");
        colStock.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getStockQty()).asObject());
        colStock.setPrefWidth(100);

        // --- ИЗМЕНЕНО: добавляем колонку "В корзине (л)" ---
        TableColumn<FuelProduct, String> colInCart = new TableColumn<>("В корзине (л)");
        colInCart.setCellValueFactory(cellData -> {
            FuelProduct fuel = cellData.getValue();
            List<CartItem> cartItems = presenter.getCartItems(CUSTOMER_ID);
            Optional<CartItem> match = cartItems.stream()
                    .filter(ci -> ci.getItemType().equals("PRODUCT") && ci.getItemId() == fuel.getId())
                    .findFirst();
            return new SimpleStringProperty(match
                    .map(ci -> String.format("%.2f", ci.getQuantity()))
                    .orElse(""));
        });
        colInCart.setPrefWidth(100);

        fuelTable.getColumns().addAll(colId, colName, colPrice, colStock, colInCart);
        root.setCenter(fuelTable);

        Button btnAdd = new Button("Добавить в корзину");
        Button btnRemove = new Button("Выложить из корзины");

        btnAdd.setDisable(true);
        btnRemove.setDisable(true);

        // Активируем кнопки при выборе топлива
        fuelTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean disable = newSel == null;
            btnAdd.setDisable(disable);
            btnRemove.setDisable(disable);
        });

        btnAdd.setOnAction(e -> onAddFuel());
        btnRemove.setOnAction(e -> onRemoveFuelFromCart());

        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.getChildren().addAll(btnAdd, btnRemove);

        root.setBottom(bottomBox);

        return root;
    }

    /**
     * Теперь при добавлении топлива:
     * - Если введено некорректное число или пустая строка → qty=0
     * - Сам элемент добавляется в корзину вне зависимости от qty
     */
    private void onAddFuel() {
        FuelProduct selected = fuelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите топливо из таблицы.");
            return;
        }
        // Запросить количество через диалог (может быть пусто или нечисловое)
        TextInputDialog qtyDialog = new TextInputDialog();
        qtyDialog.setTitle("Количество литров");
        qtyDialog.setHeaderText("Введите количество литров для \"" + selected.getName() +
                "\" (макс " + selected.getStockQty() + "):");
        Optional<String> res = qtyDialog.showAndWait();
        double qty;
        if (res.isEmpty()) {
            qty = 0.0;
        } else {
            try {
                qty = Double.parseDouble(res.get().trim());
            } catch (NumberFormatException ex) {
                qty = 0.0;
            }
        }
        // Добавляем в корзину (даже если qty == 0)
        presenter.addToCart(CUSTOMER_ID, "PRODUCT", selected.getId(), qty);
        showAlert("Успех", "Топливо добавлено в корзину (количество: " + String.format("%.2f", qty) + ").");

        loadFuelData();
        loadCartData();
        updateBalances();
    }

    private void onRemoveFuelFromCart() {
        FuelProduct selected = fuelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите топливо из таблицы.");
            return;
        }
        List<CartItem> cartItems = presenter.getCartItems(CUSTOMER_ID);
        Optional<CartItem> toRemove = cartItems.stream()
                .filter(ci -> ci.getItemType().equals("PRODUCT") && ci.getItemId() == selected.getId())
                .findFirst();

        if (toRemove.isEmpty()) {
            showAlert("Информация", "Данное топливо отсутствует в корзине.");
            return;
        }

        presenter.removeCartItemById(toRemove.get().getId(), CUSTOMER_ID);
        showAlert("Успех", "Топливо удалено из корзины.");
        loadCartData();
        loadFuelData();
        updateBalances();
    }

    private void loadFuelData() {
        List<FuelProduct> list = presenter.getAllFuel();
        ObservableList<FuelProduct> obs = FXCollections.observableArrayList(list);
        fuelTable.setItems(obs);
        fuelTable.refresh();  // обновить колонку "В корзине"
    }


    // -----------------------------
    // Панель "Услуги"
    // -----------------------------
    private BorderPane createServicePane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label lbl = new Label("Доступные услуги:");
        lbl.setStyle("-fx-font-size: 16px; -fx-padding: 0 0 10 0;");
        root.setTop(lbl);

        serviceTable = new TableView<>();
        serviceTable.setPrefHeight(400);

        TableColumn<ServiceProduct, Integer> colSid = new TableColumn<>("ID");
        colSid.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSid.setPrefWidth(50);

        TableColumn<ServiceProduct, String> colSname = new TableColumn<>("Название");
        colSname.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
        colSname.setPrefWidth(200);

        TableColumn<ServiceProduct, Double> colSprice = new TableColumn<>("Цена");
        colSprice.setCellValueFactory(cell ->
                new SimpleDoubleProperty(cell.getValue().getPrice()).asObject());
        colSprice.setPrefWidth(100);

        // --- ИЗМЕНЕНО: добавляем колонку "В корзине (шт)" ---
        TableColumn<ServiceProduct, String> colInCart = new TableColumn<>("В корзине (шт)");
        colInCart.setCellValueFactory(cellData -> {
            ServiceProduct service = cellData.getValue();
            List<CartItem> cartItems = presenter.getCartItems(CUSTOMER_ID);
            Optional<CartItem> match = cartItems.stream()
                    .filter(ci -> ci.getItemType().equals("SERVICE") && ci.getItemId() == service.getId())
                    .findFirst();
            return new SimpleStringProperty(match
                    .map(ci -> String.format("%.0f", ci.getQuantity()))
                    .orElse(""));
        });
        colInCart.setPrefWidth(100);

        serviceTable.getColumns().addAll(colSid, colSname, colSprice, colInCart);
        root.setCenter(serviceTable);

        Button btnAdd = new Button("Добавить в корзину");
        Button btnRemove = new Button("Выложить из корзины");

        btnAdd.setDisable(true);
        btnRemove.setDisable(true);

        // Активируем кнопки при выборе услуги
        serviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean disable = newSel == null;
            btnAdd.setDisable(disable);
            btnRemove.setDisable(disable);
        });

        btnAdd.setOnAction(e -> onAddService());
        btnRemove.setOnAction(e -> onRemoveServiceFromCart());

        HBox bottomBox = new HBox(10);
        bottomBox.setPadding(new Insets(10, 0, 0, 0));
        bottomBox.getChildren().addAll(btnAdd, btnRemove);

        root.setBottom(bottomBox);

        return root;
    }

    private void onAddService() {
        ServiceProduct selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите услугу из таблицы.");
            return;
        }
        // Для услуги количество всегда = 1
        presenter.addToCart(CUSTOMER_ID, "SERVICE", selected.getId(), 1.0);
        showAlert("Успех", "Услуга добавлена в корзину.");
        loadServiceData();
        loadCartData();
        updateBalances();
    }

    private void onRemoveServiceFromCart() {
        ServiceProduct selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите услугу из таблицы.");
            return;
        }
        List<CartItem> cartItems = presenter.getCartItems(CUSTOMER_ID);
        Optional<CartItem> toRemove = cartItems.stream()
                .filter(ci -> ci.getItemType().equals("SERVICE") && ci.getItemId() == selected.getId())
                .findFirst();

        if (toRemove.isEmpty()) {
            showAlert("Информация", "Данная услуга отсутствует в корзине.");
            return;
        }

        presenter.removeCartItemById(toRemove.get().getId(), CUSTOMER_ID);
        showAlert("Успех", "Услуга удалена из корзины.");
        loadCartData();
        loadServiceData();
        updateBalances();
    }

    private void loadServiceData() {
        List<ServiceProduct> list = presenter.getAllServices();
        ObservableList<ServiceProduct> obs = FXCollections.observableArrayList(list);
        serviceTable.setItems(obs);
        serviceTable.refresh();  // обновить колонку "В корзине"
    }


    // -----------------------------
    // Панель "Корзина"
    // -----------------------------
    private BorderPane createCartPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        Label lbl = new Label("Содержимое корзины:");
        lbl.setStyle("-fx-font-size: 16px; -fx-padding: 0 0 10 0;");
        root.setTop(lbl);

        cartTable = new TableView<>();
        cartTable.setPrefHeight(300);

        TableColumn<CartRow, String> colType = new TableColumn<>("Тип");
        colType.setCellValueFactory(cell -> cell.getValue().typeProperty());
        colType.setPrefWidth(100);

        TableColumn<CartRow, Integer> colCartItemId = new TableColumn<>("CartItem ID");
        colCartItemId.setCellValueFactory(cell -> cell.getValue().cartItemIdProperty().asObject());
        colCartItemId.setPrefWidth(100);

        TableColumn<CartRow, Integer> colItemId = new TableColumn<>("Item ID");
        colItemId.setCellValueFactory(cell -> cell.getValue().itemIdProperty().asObject());
        colItemId.setPrefWidth(70);

        TableColumn<CartRow, String> colName = new TableColumn<>("Название");
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colName.setPrefWidth(200);

        TableColumn<CartRow, Double> colQty = new TableColumn<>("Кол-во");
        colQty.setCellValueFactory(cell -> cell.getValue().quantityProperty().asObject());
        colQty.setPrefWidth(80);

        TableColumn<CartRow, Double> colPrice = new TableColumn<>("Цена");
        colPrice.setCellValueFactory(cell -> cell.getValue().priceProperty().asObject());
        colPrice.setPrefWidth(100);

        cartTable.getColumns().addAll(colType, colCartItemId, colItemId, colName, colQty, colPrice);
        root.setCenter(cartTable);

        // Кнопка удаления выбранного элемента
        Button btnDelete = new Button("Удалить выбранный элемент");
        btnDelete.setOnAction(e -> onDeleteSelectedItem());
        btnDelete.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 0, 0, 0));

        HBox hbTotal = new HBox(10);
        Label lblSum = new Label("Остаток к оплате (из полной суммы):");
        lblTotal = new Label("0.00 (из 0.00)");
        lblTotal.setStyle("-fx-font-weight: bold;");
        hbTotal.getChildren().addAll(lblSum, lblTotal);

        HBox hbButtons = new HBox(10);
        Button btnFull = new Button("Полная оплата");
        btnFull.setOnAction(e -> onFullCheckout());
        Button btnPartial = new Button("Частичная оплата");
        btnPartial.setOnAction(e -> onPartialCheckout());
        hbButtons.getChildren().addAll(btnFull, btnPartial);

        vbox.getChildren().addAll(btnDelete, hbTotal, hbButtons);
        root.setBottom(vbox);

        return root;
    }

    private void loadCartData() {
        List<CartItem> items = presenter.getCartItems(CUSTOMER_ID);
        ObservableList<CartRow> rows = FXCollections.observableArrayList();
        double total = presenter.getCartTotal(CUSTOMER_ID);
        double paid = presenter.getPaid(CUSTOMER_ID);
        double remaining = presenter.getRemaining(CUSTOMER_ID);

        for (CartItem ci : items) {
            String type = ci.getItemType();
            int cartItemId = ci.getId();
            int id = ci.getItemId();
            double qty = ci.getQuantity();
            String name;
            double unitPrice;

            if ("PRODUCT".equals(type)) {
                FuelProduct p = presenter.getAllFuel().stream()
                        .filter(fp -> fp.getId() == id).findFirst().orElse(null);
                name = (p != null ? p.getName() : "неизвестно");
                unitPrice = (p != null ? p.getPrice() : 0.0);
            } else {
                ServiceProduct s = presenter.getAllServices().stream()
                        .filter(sp -> sp.getId() == id).findFirst().orElse(null);
                name = (s != null ? s.getName() : "неизвестно");
                unitPrice = (s != null ? s.getPrice() : 0.0);
            }

            double price = unitPrice * qty;
            rows.add(new CartRow(type, cartItemId, id, name, qty, price));
        }
        cartTable.setItems(rows);
        lblTotal.setText(
                String.format("%.2f", remaining) + " (из " + String.format("%.2f", total) + ")"
        );
        cartTable.refresh();
    }

    private void onDeleteSelectedItem() {
        CartRow row = cartTable.getSelectionModel().getSelectedItem();
        if (row == null) {
            showAlert("Внимание", "Пожалуйста, выберите элемент для удаления.");
            return;
        }
        presenter.removeCartItemById(row.cartItemIdProperty().get(), CUSTOMER_ID);
        showAlert("Успех", "Элемент удалён из корзины.");
        loadCartData();
        loadFuelData();
        loadServiceData();
        updateBalances();
    }

    /**
     * Проверяет содержимое корзины:
     * 1) У каждого топлива (itemType = "PRODUCT") quantity > 0.
     * 2) Общая стоимость не превышает общий баланс (наличные + карта + бонусы).
     * Если что-то не так, возвращает строку с текстом ошибки, иначе — null.
     */
    private String validateCart() {
        List<CartItem> items = presenter.getCartItems(CUSTOMER_ID);

        // 1) Проверка, что для топлива указано количество > 0
        for (CartItem ci : items) {
            if ("PRODUCT".equals(ci.getItemType())) {
                if (ci.getQuantity() <= 0) {
                    // Найдём имя топлива
                    FuelProduct p = presenter.getAllFuel().stream()
                            .filter(fp -> fp.getId() == ci.getItemId())
                            .findFirst().orElse(null);
                    String name = (p != null ? p.getName() : ("ID=" + ci.getItemId()));
                    return "Топливо \"" + name + "\" не взвешено. Укажите количество литров.";
                }
            }
        }

        // 2) Проверка средств: сумма цен всех позиций vs суммарный баланс клиента
        double totalCost = presenter.getCartTotal(CUSTOMER_ID);
        Customer c = presenter.getCustomer(CUSTOMER_ID);
        if (c == null) {
            return "Ошибка: клиент не найден.";
        }
        double combinedBalance = c.getWalletBalance() + c.getCardBalance() + c.getBonusPoints();
        if (totalCost > combinedBalance) {
            return String.format(
                    "Недостаточно средств. Стоимость корзины: %.2f, доступно: %.2f.",
                    totalCost, combinedBalance
            );
        }

        return null; // всё верно
    }

    private void onFullCheckout() {
        // Сначала проверим корзину
        String error = validateCart();
        if (error != null) {
            showAlert("Ошибка", error);
            return;
        }

        double remaining = presenter.getRemaining(CUSTOMER_ID);
        if (remaining <= 0) {
            showAlert("Информация", "Корзина пуста или уже всё оплачено.");
            return;
        }

        ChoiceDialog<PaymentMethod> choiceDialog =
                new ChoiceDialog<>(PaymentMethod.Наличные, PaymentMethod.values());
        choiceDialog.setTitle("Полная оплата");
        choiceDialog.setHeaderText(
                "Сумма к оплате: " + String.format("%.2f", remaining) +
                        "\nВыберите метод полной оплаты:"
        );
        Optional<PaymentMethod> methodResult = choiceDialog.showAndWait();
        if (methodResult.isEmpty()) return;
        PaymentMethod method = methodResult.get();

        boolean ok = presenter.checkoutFullWithMethod(CUSTOMER_ID, method);
        if (ok) {
            showAlert("Успех", "Полная оплата (" + method + ") прошла успешно.");
            loadCartData();
            loadFuelData();
            loadServiceData();
            updateBalances();
        } else {
            showAlert("Ошибка", "Недостаточно средств (" + method + ") для полной оплаты.");
        }
    }

    private void onPartialCheckout() {
        // Сначала проверим корзину (в том числе взвешенность топлива)
        String error = validateCart();
        if (error != null) {
            showAlert("Ошибка", error);
            return;
        }

        double remaining = presenter.getRemaining(CUSTOMER_ID);
        if (remaining <= 0) {
            showAlert("Информация", "Корзина пуста или уже всё оплачено.");
            return;
        }

        // Ввод суммы
        TextInputDialog inputDialog = new TextInputDialog();
        inputDialog.setTitle("Частичная оплата");
        inputDialog.setHeaderText(
                "Сумма к оплате (из оставшихся " + String.format("%.2f", remaining) + "):"
        );
        Optional<String> res = inputDialog.showAndWait();
        if (res.isEmpty()) return;

        double amount;
        try {
            amount = Double.parseDouble(res.get().trim());
        } catch (NumberFormatException ex) {
            showAlert("Ошибка", "Некорректная сумма.");
            return;
        }
        if (amount > remaining) {
            showAlert("Ошибка", "Сумма превышает остаток (" + String.format("%.2f", remaining) + ").");
            return;
        }

        // Выбор метода оплаты
        ChoiceDialog<PaymentMethod> choiceDialog =
                new ChoiceDialog<>(PaymentMethod.Наличные, PaymentMethod.values());
        choiceDialog.setTitle("Частичная оплата");
        choiceDialog.setHeaderText("Выберите способ оплаты для суммы " + String.format("%.2f", amount) + ":");
        Optional<PaymentMethod> methodResult = choiceDialog.showAndWait();
        if (methodResult.isEmpty()) return;
        PaymentMethod method = methodResult.get();

        boolean ok = presenter.checkoutPartial(CUSTOMER_ID, method, amount);
        if (!ok) {
            showAlert("Ошибка", "Недостаточно средств по методу " + method + ".");
            return;
        }

        double newRemaining = presenter.getRemaining(CUSTOMER_ID);
        if (newRemaining <= 0.000001) {
            presenter.clearCart(CUSTOMER_ID);
            showAlert("Успех", "Вы полностью оплатили чек. Спасибо за покупку!");
        } else {
            showAlert("Успех", "Списано " + String.format("%.2f", amount)
                    + " (" + method + "). Осталось к оплате: " + String.format("%.2f", newRemaining));
        }

        loadCartData();
        loadFuelData();
        loadServiceData();
        updateBalances();
    }


    // -----------------------------
    // Вспомогательные методы
    // -----------------------------
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    // -----------------------------
    // Вспомогательный класс для TableView корзины
    // -----------------------------
    public static class CartRow {
        private final SimpleStringProperty type;
        private final SimpleIntegerProperty cartItemId;
        private final SimpleIntegerProperty itemId;
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty quantity;
        private final SimpleDoubleProperty price;

        public CartRow(String type, int cartItemId, int itemId, String name, double quantity, double price) {
            this.type = new SimpleStringProperty(type);
            this.cartItemId = new SimpleIntegerProperty(cartItemId);
            this.itemId = new SimpleIntegerProperty(itemId);
            this.name = new SimpleStringProperty(name);
            this.quantity = new SimpleDoubleProperty(quantity);
            this.price = new SimpleDoubleProperty(price);
        }

        public SimpleStringProperty typeProperty() { return type; }
        public SimpleIntegerProperty cartItemIdProperty() { return cartItemId; }
        public SimpleIntegerProperty itemIdProperty() { return itemId; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleDoubleProperty quantityProperty() { return quantity; }
        public SimpleDoubleProperty priceProperty() { return price; }
    }
}
