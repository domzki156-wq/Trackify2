package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.TransactionDao;
import App.models.Transaction;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * DashboardController — displays transactions and KPIs.
 * Includes handlers referenced by dashboard.fxml (record, delete, export, buy items, currency converter, deposit, withdraw).
 */
public class DashboardController {

    @FXML private Label revenueLabel;
    @FXML private Label costLabel;
    @FXML private Label totalProfitLabel;
    @FXML private Label weeklyProfitLabel;
    @FXML private Label monthlyProfitLabel;
    @FXML private Label usdWalletLabel;
    @FXML private Label phpEquivalentLabel;

    @FXML private TableView<Transaction> transactionTable;
    @FXML private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML private TableColumn<Transaction, String> customerColumn;
    @FXML private TableColumn<Transaction, String> itemColumn;
    @FXML private TableColumn<Transaction, String> paymentColumn;
    @FXML private TableColumn<Transaction, Double> revenueColumn;
    @FXML private TableColumn<Transaction, Double> costColumn;
    @FXML private TableColumn<Transaction, Double> profitColumn;
    @FXML private TableColumn<Transaction, String> notesColumn;

    private final TransactionDao transactionDao = DaoFactory.getTransactionDao();
    private final ObservableList<Transaction> transactionList = FXCollections.observableArrayList();

    private double usdBalance = 22.39;
    private static final double EXCHANGE_RATE = 59.07;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadAllTransactionsAsync();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        itemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        revenueColumn.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        profitColumn.setCellValueFactory(new PropertyValueFactory<>("profit"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
    }

    private void loadAllTransactionsAsync() {
        String userId = Session.getCurrentUser() == null ? null : Session.getCurrentUser().getId();
        if (userId == null) {
            transactionList.clear();
            transactionTable.setItems(transactionList);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                List<Transaction> transactions = transactionDao.findAllForUser(userId);
                Platform.runLater(() -> {
                    transactionList.setAll(transactions);
                    transactionTable.setItems(transactionList);
                    updateKPIs();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Load Error", e.getMessage()));
            }
        });
    }

    private void updateKPIs() {
        double totalRevenue = transactionList.stream().mapToDouble(Transaction::getRevenue).sum();
        double totalCost = transactionList.stream().mapToDouble(Transaction::getCost).sum();
        double totalProfit = totalRevenue - totalCost;

        LocalDate now = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int currentWeek = now.get(wf.weekOfWeekBasedYear());
        int currentYear = now.getYear();

        double weeklyProfit = transactionList.stream()
                .filter(t -> t.getDate() != null &&
                        t.getDate().getYear() == currentYear &&
                        t.getDate().get(wf.weekOfWeekBasedYear()) == currentWeek)
                .mapToDouble(Transaction::getProfit).sum();

        double monthlyProfit = transactionList.stream()
                .filter(t -> t.getDate() != null &&
                        t.getDate().getYear() == currentYear &&
                        t.getDate().getMonthValue() == now.getMonthValue())
                .mapToDouble(Transaction::getProfit).sum();

        revenueLabel.setText(String.format("$%.2f", totalRevenue));
        costLabel.setText(String.format("$%.2f", totalCost));
        totalProfitLabel.setText(String.format("$%.2f", totalProfit));
        weeklyProfitLabel.setText(String.format("$%.2f", weeklyProfit));
        monthlyProfitLabel.setText(String.format("$%.2f", monthlyProfit));
        usdWalletLabel.setText(String.format("$%.2f", usdBalance));
        phpEquivalentLabel.setText(String.format("₱%.2f", usdBalance * EXCHANGE_RATE));
    }

    // ------------------ FXML Handlers ------------------

    @FXML
    private void handleRecordTransaction() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/record-transaction.fxml"));
            javafx.scene.Parent root = loader.load();
            RecordTransactionController controller = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("Record Transaction");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.showAndWait();

            controller.getResult().ifPresent(tx -> {
                // record was saved in controller — just refresh view
                loadAllTransactionsAsync();
                usdBalance += tx.getProfit();
                updateKPIs();
            });
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to open record transaction dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteSelected() {
        Transaction selected = transactionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showWarning("No Selection", "Please select a transaction to delete."); return; }

        String currentUserId = Session.getCurrentUser() == null ? null : Session.getCurrentUser().getId();
        if (currentUserId == null || !currentUserId.equals(selected.getUserId())) {
            showError("Permission denied", "You can only delete your own transactions.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete this transaction?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setHeaderText("Delete Transaction");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                CompletableFuture.runAsync(() -> {
                    boolean ok = transactionDao.deleteById(selected.getId());
                    Platform.runLater(() -> {
                        if (ok) {
                            transactionList.remove(selected);
                            updateKPIs();
                            showInfo("Success", "Transaction deleted.");
                        } else showError("Error", "Failed to delete transaction.");
                    });
                });
            }
        });
    }

    @FXML
    private void handleExportCsv() {
        String userId = Session.getCurrentUser() == null ? null : Session.getCurrentUser().getId();
        if (userId == null) { showError("Not authenticated", "Please login."); return; }

        CompletableFuture.runAsync(() -> {
            try {
                List<Transaction> toExport = transactionDao.findAllForUser(userId);
                java.nio.file.Path path = java.nio.file.Paths.get("exports", "transactions-" + userId + ".csv");
                java.nio.file.Files.createDirectories(path.getParent());
                try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(path)) {
                    writer.write("Date,Customer,Item,Payment Method,Revenue,Cost,Profit,Notes");
                    writer.newLine();
                    for (Transaction t : toExport) {
                        String line = String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%s",
                                t.getDate() == null ? "" : t.getDate().toString(),
                                escapeCsv(t.getCustomer()), escapeCsv(t.getItem()),
                                escapeCsv(t.getPaymentMethod()), t.getRevenue(), t.getCost(),
                                t.getProfit(), escapeCsv(t.getNotes()));
                        writer.write(line); writer.newLine();
                    }
                }
                Platform.runLater(() -> showInfo("Exported", "CSV exported to exports folder."));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showError("Export Failed", e.getMessage()));
            }
        });
    }

    // This method was missing and caused FXMLLoader to fail.
    @FXML
    private void handleBuyItems() {
        // placeholder — show the buy items dialog when you implement inventory
        showInfo("Buy Items", "Buy Items feature will be implemented soon.");
    }

    @FXML
    private void handleCurrencyConverter() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/currency-converter.fxml"));
            javafx.scene.Parent root = loader.load();
            Stage st = new Stage();
            st.setTitle("Currency Converter");
            st.initModality(Modality.APPLICATION_MODAL);
            st.setScene(new Scene(root));
            st.setResizable(false);
            st.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to open currency converter: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeposit() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Deposit USD to wallet");
        dialog.setContentText("Amount:");
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) { showWarning("Invalid Amount", "Enter a positive amount."); return; }
                usdBalance += amount;
                updateKPIs();
                showInfo("Deposit", String.format("Deposited $%.2f", amount));
            } catch (NumberFormatException e) { showError("Invalid Input", "Please enter a valid number."); }
        });
    }

    @FXML
    private void handleWithdraw() {
        TextInputDialog dialog = new TextInputDialog("0.00");
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Withdraw USD from wallet");
        dialog.setContentText("Amount:");
        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) { showWarning("Invalid Amount", "Enter a positive amount."); return; }
                if (amount > usdBalance) { showWarning("Insufficient Funds", "Not enough balance."); return; }
                usdBalance -= amount;
                updateKPIs();
                showInfo("Withdraw", String.format("Withdrew $%.2f", amount));
            } catch (NumberFormatException e) { showError("Invalid Input", "Please enter a valid number."); }
        });
    }

    // ------------------ Helpers ------------------

    private String escapeCsv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }

    private void showInfo(String t, String m) { showAlert(Alert.AlertType.INFORMATION, t, m); }
    private void showWarning(String t, String m) { showAlert(Alert.AlertType.WARNING, t, m); }
    private void showError(String t, String m) { showAlert(Alert.AlertType.ERROR, t, m); }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(message); a.showAndWait();
    }
}
