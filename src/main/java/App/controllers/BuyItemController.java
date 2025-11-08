package App.controllers;

import App.Session;
import App.dao.DaoFactory;
import App.dao.ProductDao;
import App.dao.UserDao;
import App.models.Product;
import App.models.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class BuyItemController {
    @FXML private ListView<Product> productList;
    @FXML private TextField nameField, skuField, categoryField, priceField, stockField;
    @FXML private Label messageLabel;

    private final ProductDao productDao = DaoFactory.getProductDao();
    private final UserDao userDao = DaoFactory.getUserDao();

    @FXML
    private void initialize() {
        refreshList();

        productList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Product p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) setText("");
                else setText(p.getName() + "  (stock: " + p.getStock() + ", $" + String.format("%.2f", p.getPriceUsd()) + ")");
            }
        });

        productList.getSelectionModel().selectedItemProperty().addListener((s, o, n) -> {
            if (n != null) {
                nameField.setText(n.getName());
                skuField.setText(n.getSku());
                categoryField.setText(n.getCategory());
                priceField.setText(String.valueOf(n.getPriceUsd()));
                stockField.setText(String.valueOf(n.getStock()));
            }
        });
    }

    private void refreshList() {
        List<Product> all = productDao.findAll();
        productList.setItems(FXCollections.observableArrayList(all));
    }

    @FXML
    private void handleAddOrUpdate() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { showError("Name required"); return; }
            double price = Double.parseDouble(priceField.getText().trim());
            int stock = Integer.parseInt(stockField.getText().trim());

            Optional<Product> existing = productDao.findByName(name);
            Product p = existing.orElseGet(Product::new);
            p.setName(name);
            p.setSku(skuField.getText().trim());
            p.setCategory(categoryField.getText().trim());
            p.setPriceUsd(price);
            p.setStock(stock);

            productDao.save(p);
            showInfo("Product saved.");
            refreshList();
        } catch (NumberFormatException ex) {
            showError("Invalid number in price/stock.");
        } catch (Exception ex) {
            showError("Failed to save product: " + ex.getMessage());
        }
    }

    @FXML
    private void handleBuy() {
        // buy reduces wallet and increases product stock
        Product sel = productList.getSelectionModel().getSelectedItem();
        if (sel == null) { showError("Select a product first."); return; }
        TextInputDialog d = new TextInputDialog("1");
        d.setHeaderText("Buy quantity");
        d.setContentText("Quantity:");
        d.showAndWait().ifPresent(s -> {
            try {
                int q = Integer.parseInt(s);
                if (q <= 0) { showError("Enter positive quantity"); return; }
                double cost = q * sel.getPriceUsd();

                if (!Session.isAuthenticated()) { showError("Not logged in"); return; }
                User u = Session.getCurrentUser();
                if (u.getBalance() < cost) { showError("Insufficient wallet balance ($" + String.format("%.2f", u.getBalance()) + ")"); return; }

                // debit wallet
                double newBal = u.getBalance() - cost;
                u.setBalance(newBal);
                userDao.save(u);
                // increase stock
                productDao.adjustStock(sel.getId(), q);

                showInfo(String.format("Bought %d x %s for $%.2f. Wallet debited.", q, sel.getName(), cost));
                // refresh session & UI on FX thread
                Platform.runLater(() -> {
                    Session.setCurrentUser(u);
                    refreshList();
                });
            } catch (NumberFormatException ex) {
                showError("Invalid quantity.");
            } catch (Exception ex) {
                showError("Buy failed: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void handleClose() {
        Stage st = (Stage) nameField.getScene().getWindow();
        st.close();
    }

    private void showError(String m) { messageLabel.setText(m); }
    private void showInfo(String m) { messageLabel.setText(m); }
}
