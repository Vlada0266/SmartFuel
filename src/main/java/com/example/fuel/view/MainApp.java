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
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * JavaFX‐приложение SmartFuel (без FXML):
 * при частичной оплате, если остаток достигает нуля, корзина очищается и показывается сообщение.
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
    private Label lblTotal;  // показывает остаток и общую сумму

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
        tabPane.setStyle("-fx-background-color: #f9f9f9;");

        Tab fuelTab = new Tab("Топливо", createFuelPane());
        Tab serviceTab = new Tab("Услуги", createServicePane());
        Tab cartTab = new Tab("Корзина", createCartPane());
        tabPane.getTabs().addAll(fuelTab, serviceTab, cartTab);

        // 3) Главная сцена
        BorderPane root = new BorderPane();
        root.setTop(balanceBox);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 820, 620);
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
        HBox box = new HBox(30);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-background-color: #eef3f7; -fx-border-color: #ccd6e0; -fx-border-width: 0 0 1 0;");

        lblCash = new Label();
        lblCard = new Label();
        lblBonus = new Label();

        lblCash.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblCard.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        lblBonus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        box.getChildren().addAll(
                createBalancePair("Наличные:", lblCash),
                createBalancePair("На карте:", lblCard),
                createBalancePair("Бонусы:", lblBonus)
        );
        return box;
    }

    // Вспомогательный метод для пары заголовок + значение
    private HBox createBalancePair(String title, Label valueLabel) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");
        HBox pair = new HBox(5, titleLabel, valueLabel);
        pair.setAlignment(Pos.CENTER_LEFT);
        return pair;
    }

    private void updateBalances() {
        Customer c = presenter.getCustomer(CUSTOMER_ID);
        if (c != null) {
            lblCash.setText(String.format("%.2f ₽", c.getWalletBalance()));
            lblCard.setText(String.format("%.2f ₽", c.getCardBalance()));
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
        root.setPadding(new Insets(10, 12, 10, 12));

        Label lbl = new Label("Доступное топливо:");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        root.setTop(lbl);

        fuelTable = new TableView<>();
        fuelTable.setPrefHeight(420);
        fuelTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        // Колонка "В корзине (л)"
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
        fuelTable.setStyle("-fx-selection-bar: #3498db; -fx-selection-bar-non-focused: #a5cde5;");

        root.setCenter(fuelTable);

        Button btnAdd = new Button("Добавить в корзину");
        Button btnRemove = new Button("Убрать из корзины");

        btnAdd.setPrefWidth(140);
        btnRemove.setPrefWidth(140);

        btnAdd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        btnAdd.setDisable(true);
        btnRemove.setDisable(true);

        fuelTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean disable = (newSel == null);
            btnAdd.setDisable(disable);
            btnRemove.setDisable(disable);
        });

        btnAdd.setOnAction(e -> onAddFuel());
        btnRemove.setOnAction(e -> onRemoveFuelFromCart());

        HBox bottomBox = new HBox(15, btnAdd, btnRemove);
        bottomBox.setPadding(new Insets(12, 0, 0, 0));
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        root.setBottom(bottomBox);
        return root;
    }

    private void onAddFuel() {
        FuelProduct selected = fuelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Пожалуйста, выберите топливо.");
            return;
        }

        boolean alreadyInCart = presenter.isItemInCart(CUSTOMER_ID, "PRODUCT", selected.getId());
        if (alreadyInCart) {
            showAlert("Корзина", "Данное топливо уже есть в корзине.\nУдалите его для изменения количества.");
            return;
        }

        TextInputDialog qtyDialog = new TextInputDialog();
        qtyDialog.setTitle("Количество литров");
        qtyDialog.setHeaderText("Укажите количество литров для «" + selected.getName() +
                "» (макс. " + selected.getStockQty() + "):");

        Optional<String> res = qtyDialog.showAndWait();
        if (res.isEmpty()) {
            return;
        }

        String input = res.get().trim();
        double qty;

        if (input.isEmpty()) {
            qty = 0.0;
        } else {
            try {
                qty = Double.parseDouble(input);
            } catch (NumberFormatException ex) {
                showAlert("Ошибка ввода", "Введите корректное число, например 5.5.");
                return;
            }
            if (qty < 0.0) {
                showAlert("Ошибка ввода", "Количество не может быть отрицательным.");
                return;
            }
        }

        presenter.addToCart(CUSTOMER_ID, "PRODUCT", selected.getId(), qty);
        showAlert("Успешно добавлено", "Топливо «" + selected.getName() + "» добавлено в корзину.\nКоличество: "
                + String.format("%.2f", qty) + " л.");

        loadFuelData();
        loadCartData();
        updateBalances();
    }

    private void onRemoveFuelFromCart() {
        FuelProduct selected = fuelTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Пожалуйста, выберите топливо.");
            return;
        }
        List<CartItem> cartItems = presenter.getCartItems(CUSTOMER_ID);
        Optional<CartItem> toRemove = cartItems.stream()
                .filter(ci -> ci.getItemType().equals("PRODUCT") && ci.getItemId() == selected.getId())
                .findFirst();

        if (toRemove.isEmpty()) {
            showAlert("Информация", "Это топливо отсутствует в корзине.");
            return;
        }

        presenter.removeCartItemById(toRemove.get().getId(), CUSTOMER_ID);
        showAlert("Успешно удалено", "Топливо «" + selected.getName() + "» удалено из корзины.");
        loadCartData();
        loadFuelData();
        updateBalances();
    }

    private void loadFuelData() {
        List<FuelProduct> list = presenter.getAllFuel();
        ObservableList<FuelProduct> obs = FXCollections.observableArrayList(list);
        fuelTable.setItems(obs);
        fuelTable.refresh();
    }

    // -----------------------------
    // Панель "Услуги"
    // -----------------------------
    private BorderPane createServicePane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 12, 10, 12));

        Label lbl = new Label("Доступные услуги:");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        root.setTop(lbl);

        serviceTable = new TableView<>();
        serviceTable.setPrefHeight(420);
        serviceTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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

        // Колонка "В корзине (шт)"
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
        serviceTable.setStyle("-fx-selection-bar: #3498db; -fx-selection-bar-non-focused: #a5cde5;");

        root.setCenter(serviceTable);

        Button btnAdd = new Button("Добавить в корзину");
        Button btnRemove = new Button("Убрать из корзины");

        btnAdd.setPrefWidth(140);
        btnRemove.setPrefWidth(140);

        btnAdd.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnRemove.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        btnAdd.setDisable(true);
        btnRemove.setDisable(true);

        serviceTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean disable = (newSel == null);
            btnAdd.setDisable(disable);
            btnRemove.setDisable(disable);
        });

        btnAdd.setOnAction(e -> onAddService());
        btnRemove.setOnAction(e -> onRemoveServiceFromCart());

        HBox bottomBox = new HBox(15, btnAdd, btnRemove);
        bottomBox.setPadding(new Insets(12, 0, 0, 0));
        bottomBox.setAlignment(Pos.CENTER_LEFT);

        root.setBottom(bottomBox);
        return root;
    }

    private void onAddService() {
        ServiceProduct selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Пожалуйста, выберите услугу.");
            return;
        }
        boolean alreadyInCart = presenter.isItemInCart(CUSTOMER_ID, "SERVICE", selected.getId());
        if (alreadyInCart) {
            showAlert("Корзина", "Эта услуга уже есть в корзине.\nУдалите её для изменения.");
            return;
        }

        presenter.addToCart(CUSTOMER_ID, "SERVICE", selected.getId(), 1.0);
        showAlert("Успешно добавлено", "Услуга «" + selected.getName() + "» добавлена в корзину.");
        loadServiceData();
        loadCartData();
        updateBalances();
    }

    private void onRemoveServiceFromCart() {
        ServiceProduct selected = serviceTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Пожалуйста, выберите услугу.");
            return;
        }
        List<CartItem> cartItems = presenter.getCartItems(CUSTOMER_ID);
        Optional<CartItem> toRemove = cartItems.stream()
                .filter(ci -> ci.getItemType().equals("SERVICE") && ci.getItemId() == selected.getId())
                .findFirst();

        if (toRemove.isEmpty()) {
            showAlert("Информация", "Эта услуга отсутствует в корзине.");
            return;
        }

        presenter.removeCartItemById(toRemove.get().getId(), CUSTOMER_ID);
        showAlert("Успешно удалено", "Услуга «" + selected.getName() + "» удалена из корзины.");
        loadCartData();
        loadServiceData();
        updateBalances();
    }

    private void loadServiceData() {
        List<ServiceProduct> list = presenter.getAllServices();
        ObservableList<ServiceProduct> obs = FXCollections.observableArrayList(list);
        serviceTable.setItems(obs);
        serviceTable.refresh();
    }

    // -----------------------------
    // Панель "Корзина"
    // -----------------------------
    private BorderPane createCartPane() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10, 12, 10, 12));

        Label lbl = new Label("Содержимое корзины:");
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        root.setTop(lbl);

        cartTable = new TableView<>();
        cartTable.setPrefHeight(330);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<CartRow, String> colType = new TableColumn<>("Тип");
        colType.setCellValueFactory(cell -> cell.getValue().typeProperty());
        colType.setPrefWidth(80);

        TableColumn<CartRow, Integer> colCartItemId = new TableColumn<>("CartItem ID");
        colCartItemId.setCellValueFactory(cell -> cell.getValue().cartItemIdProperty().asObject());
        colCartItemId.setPrefWidth(90);

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
        cartTable.setStyle("-fx-selection-bar: #3498db; -fx-selection-bar-non-focused: #a5cde5;");

        root.setCenter(cartTable);

        Button btnDelete = new Button("Удалить выбранный");
        btnDelete.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");

        HBox.setHgrow(btnDelete, Priority.ALWAYS);

        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(12, 0, 0, 0));

        HBox hbTotal = new HBox(10);
        Label lblSum = new Label("Остаток к оплате:");
        lblSum.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        lblTotal = new Label("0.00 (из 0.00)");
        lblTotal.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e67e22;");
        hbTotal.getChildren().addAll(lblSum, lblTotal);
        hbTotal.setAlignment(Pos.CENTER_LEFT);

        HBox hbButtons = new HBox(15);
        Button btnFull = new Button("Полная оплата");
        Button btnPartial = new Button("Частичная оплата");
        btnFull.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnPartial.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        hbButtons.getChildren().addAll(btnFull, btnPartial);

        btnFull.setOnAction(e -> onFullCheckout());
        btnPartial.setOnAction(e -> onPartialCheckout());

        vbox.getChildren().addAll(btnDelete, hbTotal, hbButtons);
        root.setBottom(vbox);

        btnDelete.setOnAction(e -> onDeleteSelectedItem());

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
                String.format("%.2f ₽", remaining) + " (из " + String.format("%.2f ₽", total) + ")"
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
        showAlert("Успешно удалено", "Элемент удалён из корзины.");
        loadCartData();
        loadFuelData();
        loadServiceData();
        updateBalances();
    }

    /**
     * Проверяет содержимое корзины:
     * 1) У каждого топлива (itemType = "PRODUCT") quantity > 0.
     * 2) Общая стоимость не превышает общий баланс (наличные + карта + бонусы).
     * Если что-то не так, возвращает текст ошибки, иначе — null.
     */
    private String validateCart() {
        List<CartItem> items = presenter.getCartItems(CUSTOMER_ID);

        // 1) Проверка, что для топлива указано количество > 0
        for (CartItem ci : items) {
            if ("PRODUCT".equals(ci.getItemType())) {
                if (ci.getQuantity() <= 0) {
                    FuelProduct p = presenter.getAllFuel().stream()
                            .filter(fp -> fp.getId() == ci.getItemId())
                            .findFirst().orElse(null);
                    String name = (p != null ? p.getName() : ("ID=" + ci.getItemId()));
                    return "Топливо «" + name + "» не взвешено. Укажите количество литров.";
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
                    "Недостаточно средств. Стоимость корзины: %.2f ₽, доступно: %.2f ₽.",
                    totalCost, combinedBalance
            );
        }
        return null;
    }
    // -----------------------------
// Методы оплаты
// -----------------------------
    private void onFullCheckout() {
        String error = validateCart();
        if (error != null) {
            showAlert("Ошибка", error);
            return;
        }

        double remaining = presenter.getRemaining(CUSTOMER_ID);
        if (remaining <= 0) {
            showAlert("Информация", "Корзина пуста или все уже оплачено.");
            return;
        }

        // Сразу предложим выбрать способ оплаты, в заголовке укажем сумму
        ChoiceDialog<PaymentMethod> choiceDialog =
                new ChoiceDialog<>(PaymentMethod.Наличные, PaymentMethod.values());
        choiceDialog.setTitle("Полная оплата");
        choiceDialog.setHeaderText("К оплате: " + String.format("%.2f ₽", remaining));
        choiceDialog.setContentText("Выберите способ:");
        // Можно задать CSS-стиль, но для простоты – только текст
        Optional<PaymentMethod> methodResult = choiceDialog.showAndWait();
        if (methodResult.isEmpty()) return;
        PaymentMethod method = methodResult.get();

        boolean ok = presenter.checkoutFullWithMethod(CUSTOMER_ID, method);
        if (ok) {
            showAlert("Оплата прошла", "Полная оплата (" + method + ") выполнена успешно.");
            loadCartData();
            loadFuelData();
            loadServiceData();
            updateBalances();
        } else {
            showAlert("Ошибка оплаты", "Недостаточно средств по способу: " + method);
        }
    }

    private void onPartialCheckout() {
        String error = validateCart();
        if (error != null) {
            showAlert("Ошибка", error);
            return;
        }

        double remaining = presenter.getRemaining(CUSTOMER_ID);
        if (remaining <= 0) {
            showAlert("Информация", "Корзина пуста или все уже оплачено.");
            return;
        }

        // Сначала выбираем способ оплаты (в заголовке показываем остаток)
        ChoiceDialog<PaymentMethod> choiceDialog =
                new ChoiceDialog<>(PaymentMethod.Наличные, PaymentMethod.values());
        choiceDialog.setTitle("Частичная оплата");
        choiceDialog.setHeaderText("Остаток к оплате: " + String.format("%.2f ₽", remaining));
        choiceDialog.setContentText("Выберите способ оплаты:");
        Optional<PaymentMethod> methodResult = choiceDialog.showAndWait();
        if (methodResult.isEmpty()) return;
        PaymentMethod method = methodResult.get();

        // Теперь вводим сумму, указывая допустимый диапазон
        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Сумма частичной оплаты");
        amountDialog.setHeaderText("Способ: " + method);
        amountDialog.setContentText("Введите сумму (до " + String.format("%.2f ₽", remaining) + "):");
        Optional<String> res = amountDialog.showAndWait();
        if (res.isEmpty()) return;

        double amount;
        try {
            amount = Double.parseDouble(res.get().trim());
        } catch (NumberFormatException ex) {
            showAlert("Ошибка ввода", "Введите корректную сумму, например 123.45.");
            return;
        }
        if (amount <= 0) {
            showAlert("Ошибка ввода", "Сумма должна быть больше нуля.");
            return;
        }
        if (amount > remaining) {
            showAlert("Ошибка", "Сумма превышает остаток (" + String.format("%.2f ₽", remaining) + ").");
            return;
        }

        boolean ok = presenter.checkoutPartial(CUSTOMER_ID, method, amount);
        if (!ok) {
            showAlert("Ошибка оплаты", "Недостаточно средств по способу: " + method);
            return;
        }

        double newRemaining = presenter.getRemaining(CUSTOMER_ID);
        if (newRemaining < 0.000001) {
            presenter.clearCart(CUSTOMER_ID);
            showAlert("Оплата завершена", "Вы полностью оплатили чек. Спасибо за покупку!");
        } else {
            showAlert("Частичная оплата",
                    "Сумма " + String.format("%.2f ₽", amount) +
                            " списана (" + method + "), остаток: " + String.format("%.2f ₽", newRemaining));
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
