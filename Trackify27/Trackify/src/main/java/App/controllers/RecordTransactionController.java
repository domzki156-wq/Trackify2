package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.TransactionDao;
import App.models.Transaction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;

public class RecordTransactionController {

    @FXML private DatePicker datePicker;
    @FXML private TextField customerField;
    @FXML private TextField itemField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField revenueField;
    @FXML private TextField costField;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final TransactionDao transactionDao = DaoFactory.getTransactionDao();
    private Transaction result;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        paymentMethodCombo.getItems().addAll("Cash","PAYPAL","PAYPAL FNF","Paypal FNF","GCash","Credit Card","Bank Transfer");
        paymentMethodCombo.setValue("Cash");
        revenueField.setText("0.00");
        costField.setText("0.00");
    }

    @FXML
    private void handleSave() {
        if (!validateInputs()) return;
        try {
            LocalDate date = datePicker.getValue();
            String customer = customerField.getText().trim();
            String item = itemField.getText().trim();
            String paymentMethod = paymentMethodCombo.getValue();
            double revenue = Double.parseDouble(revenueField.getText());
            double cost = Double.parseDouble(costField.getText());
            String notes = notesArea.getText().trim();

            Transaction tx = new Transaction(date, customer, item, paymentMethod, revenue, cost, notes);

            if (!Session.isAuthenticated()) {
                showError("Not authenticated", "You must be logged in to record transactions.");
                return;
            }
            tx.setUserId(Session.getCurrentUser().getId());

            // Save off UI thread
            final Transaction toSave = tx;
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    transactionDao.save(toSave);
                    Platform.runLater(() -> {
                        result = toSave;
                        closeDialog();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showError("Save failed", e.getMessage()));
                }
            });
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Revenue and Cost must be valid numbers.");
        }
    }

    @FXML private void handleCancel() { closeDialog(); }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        if (datePicker.getValue() == null) { showError("Validation", "Please select a date."); return false; }
        if (customerField.getText().trim().isEmpty()) { showError("Validation", "Please enter a customer name."); return false; }
        if (itemField.getText().trim().isEmpty()) { showError("Validation", "Please enter an item."); return false; }
        try {
            double r = Double.parseDouble(revenueField.getText());
            double c = Double.parseDouble(costField.getText());
            if (r < 0 || c < 0) { showError("Validation", "Revenue and cost cannot be negative."); return false; }
        } catch (NumberFormatException e) { showError("Validation", "Revenue and cost must be numbers."); return false; }
        return true;
    }

    private void showError(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    public Optional<Transaction> getResult() { return Optional.ofNullable(result); }
}
