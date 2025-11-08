package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.TransactionDao;
import App.dao.UserDao;
import App.models.Transaction;
import App.models.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for record-transaction.fxml
 * - exposes setOwner(Stage) and setOnSaved(Runnable)
 * - validates input, saves transaction via DAO and updates user balance
 */
public class RecordTransactionController {

    @FXML private DatePicker datePicker;
    @FXML private TextField customerField;
    @FXML private TextField itemField;
    @FXML private ComboBox<String> paymentCombo;
    @FXML private TextField revenueField;
    @FXML private TextField costField;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;

    // owner stage (modal) and onSaved callback
    private Stage owner;
    private Runnable onSaved;

    private final TransactionDao txDao = DaoFactory.getTransactionDao();
    private final UserDao userDao = DaoFactory.getUserDao();

    @FXML
    private void initialize() {
        // default values
        if (datePicker != null) datePicker.setValue(LocalDate.now());
        if (paymentCombo != null) {
            paymentCombo.getItems().clear();
            paymentCombo.getItems().addAll("CASH", "GCASH", "PAYPAL", "CARD");
            paymentCombo.getSelectionModel().selectFirst();
        }
        if (messageLabel != null) messageLabel.setText("");
    }

    // called by DashboardController via reflection or directly
    public void setOwner(Stage stage) {
        this.owner = stage;
    }

    // called by DashboardController to be notified when saved
    public void setOnSaved(Runnable r) {
        this.onSaved = r;
    }

    @FXML
    private void handleCancel() {
        if (owner != null) owner.close(); else {
            // fallback: try to hide any control's scene window
            try { customerField.getScene().getWindow().hide(); } catch (Exception ignored) {}
        }
    }

    @FXML
    private void handleSave() {
        try {
            // basic validation
            LocalDate date = datePicker != null ? datePicker.getValue() : LocalDate.now();
            String customer = customerField == null ? "" : customerField.getText().trim();
            String item = itemField == null ? "" : itemField.getText().trim();
            String payment = paymentCombo == null ? "CASH" : paymentCombo.getValue();
            double revenue = parseDoubleSafe(revenueField, 0.0);
            double cost = parseDoubleSafe(costField, 0.0);
            String notes = notesArea == null ? "" : notesArea.getText().trim();

            if (revenue < 0 || cost < 0) {
                showMessage("Revenue and cost must be non-negative");
                return;
            }

            // Build transaction and set owner userId
            Transaction tx = new Transaction();
            tx.setDate(date);
            tx.setCustomer(customer);
            tx.setItem(item);
            tx.setPaymentMethod(payment);
            tx.setRevenue(revenue);
            tx.setCost(cost);
            tx.setNotes(notes);
            // compute profit in model (if model calculates profit by revenue - cost, ensure set)
            tx.setProfit(revenue - cost);

            if (!Session.isAuthenticated()) {
                showMessage("Not authenticated - cannot save");
                return;
            }
            User current = Session.getCurrentUser();
            tx.setUserId(current.getId());

            // Save transaction
            Transaction saved = txDao.save(tx);

            // Update user's wallet (add profit)
            double newBalance = current.getBalance() + (saved.getProfit());
            // If your UserDao provides updateBalance, call it; otherwise save the whole user
            try {
                userDao.updateBalance(current.getId(), newBalance);
            } catch (Exception ex) {
                // fallback: update the user object and save
                current.setBalance(newBalance);
                userDao.save(current);
            }
            // update session user so UI will reflect new balance
            current.setBalance(newBalance);
            Session.setCurrentUser(current);

            // notify caller and close
            if (onSaved != null) {
                try { onSaved.run(); } catch (Exception ignored) {}
            }

            if (owner != null) owner.close(); else {
                try { customerField.getScene().getWindow().hide(); } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showMessage("Failed to save transaction: " + ex.getMessage());
        }
    }

    // helper to parse double from textfield
    private double parseDoubleSafe(TextField f, double defaultVal) {
        if (f == null) return defaultVal;
        String s = f.getText();
        if (s == null || s.isBlank()) return defaultVal;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException ex) { return defaultVal; }
    }

    private void showMessage(String m) {
        if (messageLabel != null) messageLabel.setText(m);
        else System.out.println("RecordTransactionController: " + m);
    }
}
