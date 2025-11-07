package oop.barcelo.trackify27.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Controller for wallet.fxml dialog — used for deposit and withdraw actions.
 */
public class WalletController {

    @FXML public Label titleLabel;
    @FXML public TextField amountField;
    @FXML public TextField noteField;

    public void setTitle(String title) {
        if (titleLabel != null && title != null) {
            titleLabel.setText(title);
        }
    }

    public double getAmount() throws NumberFormatException {
        String t = amountField.getText();
        if (t == null || t.isBlank()) return 0.0;
        return Double.parseDouble(t.trim());
    }

    public String getNote() {
        return noteField.getText() == null ? "" : noteField.getText().trim();
    }
}
