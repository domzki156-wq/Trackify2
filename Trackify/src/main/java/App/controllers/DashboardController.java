package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.TransactionDao;
import App.dao.UserDao;
import App.models.Transaction;
import App.models.User;
import App.models.WalletAction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;


public class DashboardController {

    // KPI labels
    @FXML private Label usdWalletLabel;
    @FXML private Label phpEquivalentLabel;
    @FXML private Label revenueLabel;
    @FXML private Label costLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label weeklyProfitLabel;
    @FXML private Label monthlyProfitLabel;

    // Transaction table + columns
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, ?> dateColumn;
    @FXML private TableColumn<Transaction, String> customerColumn;
    @FXML private TableColumn<Transaction, String> itemColumn;
    @FXML private TableColumn<Transaction, String> paymentColumn;
    @FXML private TableColumn<Transaction, Double> revenueColumn;
    @FXML private TableColumn<Transaction, Double> costColumn;
    @FXML private TableColumn<Transaction, Double> profitColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;

    // Wallet list view
    @FXML private ListView<WalletAction> walletListView;

    // buttons (optional fx:ids)
    @FXML private Button recordTransactionBtn;
    @FXML private Button deleteSelectedBtn;
    @FXML private Button exportCsvBtn;
    @FXML private Button buyItemsBtn;
    @FXML private Button currencyConverterBtn;
    @FXML private Button depositBtn;
    @FXML private Button withdrawBtn;

    private final TransactionDao txDao = DaoFactory.getTransactionDao();
    private final UserDao userDao = DaoFactory.getUserDao();

    private final ObservableList<WalletAction> walletActions = FXCollections.observableArrayList();
    private double usdBalance = 0.0;

    @FXML
    private void initialize() {
        try {
            if (transactionTable != null) {
                transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                transactionTable.setPlaceholder(new Label("No transactions recorded yet"));
            }

            if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            if (customerColumn != null) customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
            if (itemColumn != null) itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
            if (paymentColumn != null) paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
            if (revenueColumn != null) revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));
            if (costColumn != null) costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
            if (profitColumn != null) profitColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));
            if (notesColumn != null) notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));


            if (revenueColumn != null) revenueColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : String.format("$%.2f", item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            });
            if (costColumn != null) costColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : String.format("$%.2f", item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            });
            if (profitColumn != null) profitColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : String.format("$%.2f", item));
                    setStyle("-fx-alignment: CENTER-RIGHT;");
                }
            });

            // Wallet list
            if (walletListView != null) {
                walletListView.setItems(walletActions);
                walletListView.setCellFactory(lv -> new WalletActionCell());
            }

            // small UI niceties: highlight row on selection (makes selection color consistent)
            if (transactionTable != null) transactionTable.setStyle("-fx-selection-bar: derive(#3a7bd5,30%);");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // load once UI is shown
        Platform.runLater(this::loadUserData);
    }

    // Load user session and transactions
    private void loadUserData() {
        if (!Session.isAuthenticated()) return;
        Optional<User> fresh = userDao.findById(Session.getCurrentUser().getId());
        fresh.ifPresent(user -> {
            Session.setCurrentUser(user);
            usdBalance = user.getBalance();
            updateWalletLabel();
        });
        loadTransactions();
    }

    private void loadTransactions() {
        if (transactionTable != null) transactionTable.getItems().clear();
        if (!Session.isAuthenticated()) return;

        String userId = Session.getCurrentUser().getId();
        List<Transaction> txs = txDao.findAllForUser(userId);
        if (txs != null && !txs.isEmpty()) transactionTable.getItems().addAll(txs);
        updateKpis();
    }

    // Update USD label and start async PHP equivalent update
    private void updateWalletLabel() {
        if (usdWalletLabel != null) usdWalletLabel.setText(String.format("$%.2f", usdBalance));
        updatePhpEquivalentAsync();
    }

    private void updatePhpEquivalentAsync() {
        if (phpEquivalentLabel == null) return;
        double usd = usdBalance;
        if (usd <= 0) { phpEquivalentLabel.setText("₱0.00"); return; }


        java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return App.service.CurrencyService.convertUsdToPhp(usd);
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }).thenAccept(result -> Platform.runLater(() -> {
            if (result == null) phpEquivalentLabel.setText("₱0.00"); else phpEquivalentLabel.setText(String.format("₱%.2f", result));
        }));
    }


    private void updateKpis() {
        double revenue = 0.0, cost = 0.0;
        if (transactionTable != null) {
            for (Transaction t : transactionTable.getItems()) {
                if (t == null) continue;
                Double r = t.getRevenue();
                Double c = t.getCost();
                revenue += (r == null ? 0.0 : r);
                cost += (c == null ? 0.0 : c);
            }
        }
        double profit = revenue - cost;

        if (revenueLabel != null) revenueLabel.setText(String.format("%.2f", revenue));
        if (costLabel != null) costLabel.setText(String.format("%.2f", cost));
        if (totalProfitLabel != null) totalProfitLabel.setText(String.format("%.2f", profit));
        if (weeklyProfitLabel != null) weeklyProfitLabel.setText("0.00");
        if (monthlyProfitLabel != null) monthlyProfitLabel.setText("0.00");
    }



    @FXML
    private void handleRecordTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/record-transaction.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();

            Stage dialog = new Stage();
            dialog.initOwner(usdWalletLabel.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Record Transaction");


            try {
                controller.getClass().getMethod("setOwner", Stage.class).invoke(controller, dialog);
            } catch (NoSuchMethodException ignored) {}
            try {
                controller.getClass().getMethod("setOnSaved", Runnable.class).invoke(controller, (Runnable) () -> {
                    Platform.runLater(() -> {
                        loadUserData();
                        loadTransactions();
                    });
                });
            } catch (NoSuchMethodException ignored) {}

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();


            loadUserData();
            loadTransactions();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open record transaction dialog:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteSelected() {
        Transaction sel = transactionTable.getSelectionModel().getSelectedItem();
        if (sel == null) { showAlert("No selection", "Please select a transaction to delete.", Alert.AlertType.WARNING); return; }
        boolean ok = confirm("Delete", "Delete selected transaction?");
        if (!ok) return;

        boolean removed = txDao.deleteById(sel.getId());
        if (removed) {
            transactionTable.getItems().remove(sel);
            showAlert("Deleted", "Transaction deleted.", Alert.AlertType.INFORMATION);
            updateKpis();
        } else {
            showAlert("Failed", "Could not delete transaction.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExportCsv() {
        try {
            Path out = Path.of(System.getProperty("user.home"), "trackify_transactions.csv");
            txDao.exportCsv(out);
            showAlert("Export complete", "CSV exported to: " + out.toString(), Alert.AlertType.INFORMATION);
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Export failed", ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeposit() {
        if (!Session.isAuthenticated()) { showAlert("Not logged in", "Please login to deposit.", Alert.AlertType.WARNING); return; }
        TextInputDialog d = new TextInputDialog("0.00");
        d.setTitle("Deposit");
        d.setHeaderText("Deposit to USD Wallet");
        d.setContentText("Amount:");
        applyDialogCss(d);
        d.showAndWait().ifPresent(s -> {
            try {
                double amount = Double.parseDouble(s);
                if (amount <= 0) { showAlert("Invalid amount","Enter a positive number", Alert.AlertType.ERROR); return; }
                usdBalance += amount;
                User u = Session.getCurrentUser();
                u.setBalance(usdBalance);
                try { userDao.updateBalance(u.getId(), usdBalance); } catch (AbstractMethodError | NoSuchMethodError e) { userDao.save(u); }
                Session.setCurrentUser(u);
                updateWalletLabel();

                addWalletAction("Deposit", amount, "Deposit via UI");

                showAlert("Deposit successful", String.format("Deposited $%.2f", amount), Alert.AlertType.INFORMATION);
            } catch (NumberFormatException ex) { showAlert("Invalid input","Please enter a valid number.", Alert.AlertType.ERROR); }
        });
    }

    @FXML
    private void handleWithdraw() {
        if (!Session.isAuthenticated()) { showAlert("Not logged in", "Please login to withdraw.", Alert.AlertType.WARNING); return; }
        TextInputDialog d = new TextInputDialog("0.00");
        d.setTitle("Withdraw");
        d.setHeaderText("Withdraw from USD Wallet");
        d.setContentText("Amount:");
        applyDialogCss(d);
        d.showAndWait().ifPresent(s -> {
            try {
                double amount = Double.parseDouble(s);
                if (amount <= 0) { showAlert("Invalid amount","Enter a positive number", Alert.AlertType.ERROR); return; }
                if (amount > usdBalance) { showAlert("Insufficient funds","You don't have enough balance.", Alert.AlertType.ERROR); return; }
                usdBalance -= amount;
                User u = Session.getCurrentUser();
                u.setBalance(usdBalance);
                try { userDao.updateBalance(u.getId(), usdBalance); } catch (AbstractMethodError | NoSuchMethodError e) { userDao.save(u); }
                Session.setCurrentUser(u);
                updateWalletLabel();

                addWalletAction("Withdraw", amount, "Withdraw via UI");

                showAlert("Withdraw successful", String.format("Withdrew $%.2f", amount), Alert.AlertType.INFORMATION);
            } catch (NumberFormatException ex) { showAlert("Invalid input","Please enter a valid number.", Alert.AlertType.ERROR); }
        });
    }

    @FXML
    private void handleBuyItems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/buy-item.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(usdWalletLabel.getScene().getWindow());
            dialog.setTitle("Buy Items");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();


            loadUserData();
            loadTransactions();

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open buy dialog:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCurrencyConverter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/currency-converter.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initOwner(usdWalletLabel.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Currency Converter");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
            updatePhpEquivalentAsync();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open currency converter:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }


    private void addWalletAction(String type, double amount, String note) {
        WalletAction a = new WalletAction(type, amount, note);
        Platform.runLater(() -> walletActions.add(0, a)); // newest first
    }


    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        applyDialogCss(a);
        if (usdWalletLabel != null && usdWalletLabel.getScene() != null) a.initOwner(usdWalletLabel.getScene().getWindow());
        a.showAndWait();
    }

    private boolean confirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        applyDialogCss(a);
        if (usdWalletLabel != null && usdWalletLabel.getScene() != null) a.initOwner(usdWalletLabel.getScene().getWindow());
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }


    private void applyDialogCss(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("dialog-container");
        } catch (Exception ignored) {}
    }

    private void applyDialogCss(TextInputDialog dialog) { applyDialogCss((Dialog<?>) dialog); }
    private void applyDialogCss(Alert alert) { applyDialogCss((Dialog<?>) alert); }


    private static class WalletActionCell extends ListCell<WalletAction> {
        private final HBox container = new HBox(8);
        private final Label left = new Label();
        private final Region spacer = new Region();
        private final Label amount = new Label();

        WalletActionCell() {
            container.setStyle("-fx-padding: 8;");
            left.getStyleClass().add("wallet-list-left");    // optionally style via css
            amount.getStyleClass().add("wallet-list-amount");
            HBox.setHgrow(spacer, Priority.ALWAYS);
            container.getChildren().addAll(left, spacer, amount);
        }

        @Override
        protected void updateItem(WalletAction item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {

                String when = item.getTimestamp() == null ? "" : item.getTimestamp();
                left.setText(when + "  " + item.getType());
                amount.setText(String.format("$%.2f", item.getAmount()));


                left.setStyle("-fx-text-fill: #000000; -fx-font-size: 11px;");


                if ("Deposit".equalsIgnoreCase(item.getType())) {
                    amount.setStyle("-fx-text-fill: #006400; -fx-font-weight: bold;");
                } else if ("Withdraw".equalsIgnoreCase(item.getType())) {
                    amount.setStyle("-fx-text-fill: #8b0000; -fx-font-weight: bold;");
                } else {
                    amount.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
                }

                setGraphic(container);
            }
        }
    }
}
