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


public class RecordTransactionController {

    @FXML private DatePicker datePicker;
    @FXML private TextField customerField;
    @FXML private TextField itemField;
    @FXML private ComboBox<String> paymentCombo;
    @FXML private TextField revenueField;
    @FXML private TextField costField;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final TransactionDao txDao = DaoFactory.getTransactionDao();
    private final UserDao userDao = DaoFactory.getUserDao();


    private Stage owner;
    private Runnable onSaved;

    @FXML
    public void initialize() {
        if (paymentCombo != null) {
            paymentCombo.getItems().clear();
            paymentCombo.getItems().addAll("CASH", "GCASH", "PAYPAL", "CARD", "OTHER");
            paymentCombo.getSelectionModel().selectFirst();
        }
        if (datePicker != null) datePicker.setValue(LocalDate.now());

        if (saveButton != null) saveButton.setOnAction(e -> {
            try { handleSave(); } catch (Exception ex) { ex.printStackTrace(); }
        });
        if (cancelButton != null) cancelButton.setOnAction(e -> {
            try { handleCancel(); } catch (Exception ex) { ex.printStackTrace(); }
        });
    }

    public void setOwner(Stage owner) { this.owner = owner; }

    public void setOnSaved(Runnable onSaved) { this.onSaved = onSaved; }


    @FXML
    public void handleSave() {
        LocalDate date = datePicker != null ? datePicker.getValue() : LocalDate.now();
        String customer = textOf(customerField);
        String item = textOf(itemField);
        String payment = paymentCombo != null ? paymentCombo.getSelectionModel().getSelectedItem() : "CASH";
        double revenue = parseDouble(revenueField);
        double cost = parseDouble(costField);
        String notes = textOf(notesArea);

        if ((customer.isEmpty() && item.isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Please enter at least a customer or item.");
            return;
        }

        Transaction tx = new Transaction();
        tx.setDate(date);
        tx.setCustomer(customer);
        tx.setItem(item);
        tx.setPaymentMethod(payment);
        tx.setRevenue(revenue);
        tx.setCost(cost);
        tx.setProfit(revenue - cost);
        tx.setNotes(notes);
        if (Session.isAuthenticated() && Session.getCurrentUser() != null) {
            tx.setUserId(Session.getCurrentUser().getId());
        }

        boolean saved = false;
        try {

            try {
                Object result = txDao.getClass().getMethod("save", Transaction.class).invoke(txDao, tx);
                saved = interpretResult(result);
            } catch (NoSuchMethodException nsme) {
                String[] alt = new String[]{"insert","add","create","persist"};
                for (String name : alt) {
                    try {
                        Object res = txDao.getClass().getMethod(name, Transaction.class).invoke(txDao, tx);
                        saved = interpretResult(res);
                        break;
                    } catch (NoSuchMethodException ignored) { /* try next */ }
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            saved = false;
        }

        if (!saved) {
            showAlert(Alert.AlertType.ERROR, "Save failed", "Could not save transaction (DAO returned failure).");
            return;
        }

        try {
            if (Session.isAuthenticated() && Session.getCurrentUser() != null) {
                User u = Session.getCurrentUser();
                double bal = u.getBalance();
                bal += revenue;
                bal -= cost;
                u.setBalance(bal);
                try { userDao.updateBalance(u.getId(), bal); } catch (AbstractMethodError | NoSuchMethodError e) { userDao.save(u); }
                Session.setCurrentUser(u);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        if (onSaved != null) Platform.runLater(onSaved);

        if (owner != null) owner.close();
        else if (saveButton != null && saveButton.getScene() != null) {
            Stage st = (Stage) saveButton.getScene().getWindow();
            st.close();
        }
    }


    private boolean interpretResult(Object res) {
        if (res == null) return true;
        if (res instanceof Boolean) return (Boolean) res;
        return true;
    }

    @FXML
    public void handleCancel() {
        if (owner != null) owner.close();
        else if (cancelButton != null && cancelButton.getScene() != null) {
            ((Stage) cancelButton.getScene().getWindow()).close();
        }
    }

    private static String textOf(TextInputControl t) {
        if (t == null) return "";
        String s = t.getText();
        return s == null ? "" : s.trim();
    }

    private static double parseDouble(TextField f) {
        if (f == null) return 0.0;
        String s = f.getText();
        if (s == null || s.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException ex) { return 0.0; }
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        if (owner != null) a.initOwner(owner);
        a.showAndWait();
    }
}
