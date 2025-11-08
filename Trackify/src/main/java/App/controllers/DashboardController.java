package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.TransactionDao;
import App.dao.UserDao;
import App.models.Transaction;
import App.models.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * DashboardController - shows transactions and wallet.
 * This version uses reflection when interacting with the record-transaction controller
 * so it compiles even if that controller does not expose helper setters.
 */
public class DashboardController {

    // KPI labels (from your dashboard.fxml)
    @FXML private Label revenueLabel;
    @FXML private Label costLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label weeklyProfitLabel;
    @FXML private Label monthlyProfitLabel;
    @FXML private Label usdWalletLabel;
    @FXML private Label phpEquivalentLabel;

    // Table and columns (must match fx:id)
    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> customerColumn;
    @FXML private TableColumn<Transaction, String> itemColumn;
    @FXML private TableColumn<Transaction, String> paymentColumn;
    @FXML private TableColumn<Transaction, Double> revenueColumn;
    @FXML private TableColumn<Transaction, Double> costColumn;
    @FXML private TableColumn<Transaction, Double> profitColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;

    // DAOs
    private final UserDao userDao = DaoFactory.getUserDao();
    private final TransactionDao txDao = DaoFactory.getTransactionDao();

    // in-memory wallet representation (keeps UI in sync)
    private double usdBalance = 0.0;

    @FXML
    public void initialize() {
        // Configure cell value factories (in case FXML didn't set them)
        try {
            if (dateColumn != null) dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            if (customerColumn != null) customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
            if (itemColumn != null) itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
            if (paymentColumn != null) paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
            if (revenueColumn != null) revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));
            if (costColumn != null) costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
            if (profitColumn != null) profitColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));
            if (notesColumn != null) notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));

            // formatting cells for readability
            if (dateColumn != null) dateColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.toString());
                }
            });
            if (revenueColumn != null) revenueColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : String.format("$%.2f", item));
                }
            });
            if (costColumn != null) costColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : String.format("$%.2f", item));
                }
            });
            if (profitColumn != null) profitColumn.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : String.format("$%.2f", item));
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Load user & transaction data once JavaFX has finished building the scene
        Platform.runLater(this::loadUserData);
    }

    // Refresh user info and transactions from DB
    private void loadUserData() {
        if (!Session.isAuthenticated()) return;

        User sessionUser = Session.getCurrentUser();
        Optional<User> fresh = userDao.findById(sessionUser.getId());
        fresh.ifPresent(user -> {
            Session.setCurrentUser(user);
            usdBalance = user.getBalance();
            updateWalletLabel();
        });

        loadTransactions();
        updateKpis(); // recompute KPI labels (basic)
    }

    private void loadTransactions() {
        if (transactionTable != null) transactionTable.getItems().clear();
        if (!Session.isAuthenticated()) {
            System.out.println("loadTransactions: no authenticated session.");
            return;
        }

        String userId = Session.getCurrentUser().getId();
        System.out.println("loadTransactions: fetching for userId=" + userId);

        List<Transaction> txs = txDao.findAllForUser(userId);
        System.out.println("loadTransactions: dao returned " + (txs == null ? 0 : txs.size()) + " transactions.");
        if (txs != null && !txs.isEmpty()) {
            for (Transaction tx : txs) {
                System.out.println("  tx id=" + tx.getId() + " date=" + tx.getDate() + " item=" + tx.getItem());
            }
            if (transactionTable != null) transactionTable.getItems().addAll(txs);
        }
        updateKpis();
    }

    // recompute simple KPI totals to reflect table contents
    private void updateKpis() {
        double revenue = 0.0, cost = 0.0, weeklyProfit = 0.0, monthlyProfit = 0.0;
        LocalDate now = LocalDate.now();

        if (transactionTable != null) {
            for (Transaction t : transactionTable.getItems()) {
                revenue += t.getRevenue();
                cost += t.getCost();

                // weekly profit
                if (t.getDate() != null && !t.getDate().isAfter(now) &&
                        !t.getDate().isBefore(now.minusDays(7))) {
                    weeklyProfit += t.getProfit();
                }

                // monthly profit
                if (t.getDate() != null &&
                        t.getDate().getMonthValue() == now.getMonthValue() &&
                        t.getDate().getYear() == now.getYear()) {
                    monthlyProfit += t.getProfit();
                }
            }
        }

        double totalProfit = revenue - cost;

        if (revenueLabel != null) revenueLabel.setText(String.format("%.2f", revenue));
        if (costLabel != null) costLabel.setText(String.format("%.2f", cost));
        if (totalProfitLabel != null) totalProfitLabel.setText(String.format("%.2f", totalProfit));
        if (weeklyProfitLabel != null) weeklyProfitLabel.setText(String.format("%.2f", weeklyProfit));
        if (monthlyProfitLabel != null) monthlyProfitLabel.setText(String.format("%.2f", monthlyProfit));
    }



    private void updateWalletLabel() {
        if (usdWalletLabel != null)
            usdWalletLabel.setText(String.format("$%.2f", usdBalance));
        updatePhpEquivalent();
    }


    private void updatePhpEquivalent() {
        double rate = 56.5; // 1 USD = ₱56.50 (you can update manually or connect API)
        double phpValue = usdBalance * rate;
        if (phpEquivalentLabel != null)
            phpEquivalentLabel.setText(String.format("₱%.2f", phpValue));
    }

   

    @FXML
    public void handleRecordTransaction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/record-transaction.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController(); // generic reference

            // create modal stage
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (usdWalletLabel != null && usdWalletLabel.getScene() != null) {
                dialog.initOwner(usdWalletLabel.getScene().getWindow());
            }
            dialog.setTitle("Record Transaction");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);

            // Try to call setOwner(Stage) on the controller if that method exists (reflection)
            try {
                Method m = controller.getClass().getMethod("setOwner", Stage.class);
                if (m != null) m.invoke(controller, dialog);
            } catch (NoSuchMethodException ignored) {
                // controller doesn't expose setOwner - that's okay
            }

            // Try to register a saved callback: controller.setOnSaved(Runnable)
            try {
                Method onSaved = controller.getClass().getMethod("setOnSaved", Runnable.class);
                if (onSaved != null) {
                    Runnable callback = () -> Platform.runLater(() -> {
                        loadUserData();
                        loadTransactions();
                    });
                    onSaved.invoke(controller, callback);
                }
            } catch (NoSuchMethodException ignored) {
                // not provided; we'll refresh after dialog closes anyway
            }

            // Show modal and wait
            dialog.showAndWait();

            // Always refresh after dialog closes
            loadUserData();
            loadTransactions();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open record transaction dialog:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleDeleteSelected() {
        Transaction sel = transactionTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No selection", "Please select a transaction to delete.", Alert.AlertType.WARNING);
            return;
        }
        boolean ok = confirm("Delete", "Delete selected transaction?");
        if (!ok) return;

        boolean removed = txDao.deleteById(sel.getId());
        if (removed) {
            transactionTable.getItems().remove(sel);
            updateKpis();
            showAlert("Deleted", "Transaction deleted.", Alert.AlertType.INFORMATION);
        } else {
            showAlert("Failed", "Could not delete transaction.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    public void handleExportCsv() {
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
    public void handleDeposit() {
        if (!Session.isAuthenticated()) { showAlert("Not logged in", "Please login to deposit.", Alert.AlertType.WARNING); return; }
        TextInputDialog d = new TextInputDialog("0.00");
        d.setTitle("Deposit");
        d.setHeaderText("Deposit to USD Wallet");
        d.setContentText("Amount:");
        d.showAndWait().ifPresent(s -> {
            try {
                double amount = Double.parseDouble(s);
                if (amount <= 0) { showAlert("Invalid amount","Enter a positive number", Alert.AlertType.ERROR); return; }
                usdBalance += amount;
                User u = Session.getCurrentUser();
                u.setBalance(usdBalance);
                userDao.save(u);
                updateWalletLabel();
                showAlert("Deposit successful", String.format("Deposited $%.2f", amount), Alert.AlertType.INFORMATION);
            } catch (NumberFormatException ex) { showAlert("Invalid input","Please enter a valid number.", Alert.AlertType.ERROR); }
        });
    }

    @FXML
    public void handleWithdraw() {
        if (!Session.isAuthenticated()) { showAlert("Not logged in", "Please login to withdraw.", Alert.AlertType.WARNING); return; }
        TextInputDialog d = new TextInputDialog("0.00");
        d.setTitle("Withdraw");
        d.setHeaderText("Withdraw from USD Wallet");
        d.setContentText("Amount:");
        d.showAndWait().ifPresent(s -> {
            try {
                double amount = Double.parseDouble(s);
                if (amount <= 0) { showAlert("Invalid amount","Enter a positive number", Alert.AlertType.ERROR); return; }
                if (amount > usdBalance) { showAlert("Insufficient funds","You don't have enough balance.", Alert.AlertType.ERROR); return; }
                usdBalance -= amount;
                User u = Session.getCurrentUser();
                u.setBalance(usdBalance);
                userDao.save(u);
                updateWalletLabel();
                showAlert("Withdraw successful", String.format("Withdrew $%.2f", amount), Alert.AlertType.INFORMATION);
            } catch (NumberFormatException ex) { showAlert("Invalid input","Please enter a valid number.", Alert.AlertType.ERROR); }
        });
    }

    @FXML
    public void handleBuyItems() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/buy-item.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            if (usdWalletLabel != null && usdWalletLabel.getScene() != null) dialog.initOwner(usdWalletLabel.getScene().getWindow());
            dialog.setTitle("Buy Items");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();

            // Refresh after dialog closes
            loadUserData();
            loadTransactions();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open buy dialog:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Currency converter handler MUST be public @FXML to satisfy FXMLLoader requirements.
     */
    @FXML
    public void handleCurrencyConverter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/currency-converter.fxml"));
            Parent root = loader.load();

            Stage dialog = new Stage();
            if (usdWalletLabel != null && usdWalletLabel.getScene() != null) dialog.initOwner(usdWalletLabel.getScene().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Currency Converter");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialog.setScene(scene);
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "Failed to open currency converter:\n" + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

  
    private void showAlert(String title, String message, Alert.AlertType t) {
        try {
            Alert a = new Alert(t);
            a.setTitle(title);
            a.setHeaderText(null);
            a.setContentText(message);
            if (usdWalletLabel != null && usdWalletLabel.getScene() != null) a.initOwner(usdWalletLabel.getScene().getWindow());
            a.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean confirm(String title, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        if (usdWalletLabel != null && usdWalletLabel.getScene() != null) a.initOwner(usdWalletLabel.getScene().getWindow());
        Optional<ButtonType> res = a.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }
}
