package App.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Minimal currency converter — keeps UI behavior separate.
 * If you have a more advanced converter (fetch rates) plug that in.
 */
public class CurrencyConverterController {
    @FXML private TextField amountField;
    @FXML private Label resultLabel;
    @FXML private Button convertBtn;

    @FXML
    private void initialize() {
        resultLabel.setText("");
    }

    @FXML
    private void handleConvert() {
        String s = amountField.getText() == null ? "" : amountField.getText().trim();
        try {
            double amt = Double.parseDouble(s);
            // static conversion for UX (example 1 USD -> 59.0 PHP)
            double php = amt * 59.0;
            resultLabel.setText(String.format("₱%.2f", php));
        } catch (Exception ex) {
            resultLabel.setText("Invalid");
        }
    }
}
