package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.TransactionDao;
import App.dao.UserDao;
import App.models.Transaction;
import App.models.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;

public class SidebarController {

    @FXML private TextField depositAmountField;
    @FXML private TextField withdrawAmountField;

    @FXML private TextField buyCustomerField;
    @FXML private TextField buyItemField;
    @FXML private ComboBox<String> buyPaymentCombo;
    @FXML private TextField buyRevenueField;
    @FXML private TextField buyCostField;

    @FXML private Label messageLabel;


    private final UserDao userDao = DaoFactory.getUserDao();
    private final TransactionDao txDao = DaoFactory.getTransactionDao();


    private Stage owner;


    private Runnable refreshCallback;

    @FXML
    private void initialize() {
        if (buyPaymentCombo != null) {
            buyPaymentCombo.getItems().addAll("CASH", "PAYPAL", "GCASH", "CARD");
            buyPaymentCombo.getSelectionModel().selectFirst();
        }
    }

    public void setOwner(Stage s) { this.owner = s; }

    public void setOnSaved(Runnable r) { this.refreshCallback = r; }


    @FXML
    private void handleDeposit() {
        if (!Session.isAuthenticated()) {
            showMsg("Please login first.", true);
            return;
        }
        String s = depositAmountField.getText();
        double amount;
        try {
            amount = Double.parseDouble(s);
            if (amount <= 0) { showMsg("Enter a positive amount.", true); return; }
        } catch (NumberFormatException ex) { showMsg("Invalid amount.", true); return; }

        User u = Session.getCurrentUser();
        double newBalance = u.getBalance() + amount;
        u.setBalance(newBalance);

        try {
            try { userDao.updateBalance(u.getId(), newBalance); }
            catch (AbstractMethodError | NoSuchMethodError e) { userDao.save(u); }

            Transaction tx = new Transaction();
            tx.setDate(LocalDate.now());
            tx.setCustomer("Deposit");
            tx.setItem("Deposit");
            tx.setPaymentMethod("DEPOSIT");
            tx.setRevenue(amount);
            tx.setCost(0.0);
            tx.setNotes("Deposit via sidebar");
            tx.setUserId(u.getId());
            txDao.save(tx);

            Session.setCurrentUser(u);
            showMsg(String.format("Deposited $%.2f", amount), false);
            runRefresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Failed to persist deposit: " + ex.getMessage(), true);
        }
    }

    @FXML
    private void handleWithdraw() {
        if (!Session.isAuthenticated()) {
            showMsg("Please login first.", true);
            return;
        }
        String s = withdrawAmountField.getText();
        double amount;
        try {
            amount = Double.parseDouble(s);
            if (amount <= 0) { showMsg("Enter a positive amount.", true); return; }
        } catch (NumberFormatException ex) { showMsg("Invalid amount.", true); return; }

        User u = Session.getCurrentUser();
        if (amount > u.getBalance()) { showMsg("Insufficient funds.", true); return; }

        double newBalance = u.getBalance() - amount;
        u.setBalance(newBalance);

        try {
            try { userDao.updateBalance(u.getId(), newBalance); }
            catch (AbstractMethodError | NoSuchMethodError e) { userDao.save(u); }

            Transaction tx = new Transaction();
            tx.setDate(LocalDate.now());
            tx.setCustomer("Withdraw");
            tx.setItem("Withdraw");
            tx.setPaymentMethod("WITHDRAW");
            tx.setRevenue(0.0);
            tx.setCost(amount);
            tx.setNotes("Withdrawal via sidebar");
            tx.setUserId(u.getId());
            txDao.save(tx);

            Session.setCurrentUser(u);
            showMsg(String.format("Withdrew $%.2f", amount), false);
            runRefresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Failed to persist withdrawal: " + ex.getMessage(), true);
        }
    }

    @FXML
    private void handleBuyItem() {
        if (!Session.isAuthenticated()) { showMsg("Please login first.", true); return; }
        String customer = buyCustomerField.getText();
        String item = buyItemField.getText();
        String pay = buyPaymentCombo.getSelectionModel().getSelectedItem();
        double revenue, cost;
        try {
            revenue = Double.parseDouble(buyRevenueField.getText());
            cost = Double.parseDouble(buyCostField.getText());
        } catch (Exception ex) { showMsg("Enter numeric revenue & cost.", true); return; }

        Transaction tx = new Transaction();
        tx.setDate(LocalDate.now());
        tx.setCustomer(customer == null ? "" : customer);
        tx.setItem(item == null ? "" : item);
        tx.setPaymentMethod(pay);
        tx.setRevenue(revenue);
        tx.setCost(cost);
        tx.setNotes("Bought via sidebar");
        tx.setUserId(Session.getCurrentUser().getId());
        tx.setProfit(revenue - cost);

        try {
            txDao.save(tx);
            User u = Session.getCurrentUser();
            double newBalance = u.getBalance() + revenue - 0.0; // if you want cost to be subtracted then: u.getBalance() + (revenue - cost)
            u.setBalance(newBalance);
            try { userDao.updateBalance(u.getId(), newBalance); } catch (AbstractMethodError | NoSuchMethodError e) { userDao.save(u); }
            Session.setCurrentUser(u);

            showMsg("Recorded purchase.", false);
            runRefresh();
        } catch (Exception ex) {
            ex.printStackTrace();
            showMsg("Failed to save transaction: " + ex.getMessage(), true);
        }
    }

    // small helper to show messages (error or success)
    private void showMsg(String text, boolean error) {
        Platform.runLater(() -> {
            if (messageLabel != null) {
                messageLabel.setText(text);
                messageLabel.setStyle(error ? "-fx-text-fill: #ff7b7b;" : "-fx-text-fill: #aaf0aa;");
            } else {
                // fallback
                Alert a = new Alert(error ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION);
                a.setContentText(text);
                if (owner != null) a.initOwner(owner);
                a.showAndWait();
            }
        });
    }

    private void runRefresh() {
        if (refreshCallback != null) {
            try { Platform.runLater(refreshCallback); } catch (Exception ignored) {}
        }
    }
}
