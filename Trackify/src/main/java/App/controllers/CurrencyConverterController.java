package App.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controller for the Currency Converter dialog.
 * Converts between USD and PHP using a user-specified exchange rate.
 *
 * @author Trackify Team
 * @version 1.0
 */
public class CurrencyConverterController {

    @FXML private TextField exchangeRateField;
    @FXML private TextField usdAmountField;
    @FXML private TextField phpAmountField;
    @FXML private Button usdToPhpButton;
    @FXML private Button phpToUsdButton;
    @FXML private Button closeButton;

    private static final double DEFAULT_EXCHANGE_RATE = 59.07;

    /**
     * Initializes the converter with default exchange rate.
     */
    @FXML
    public void initialize() {
        exchangeRateField.setText(String.valueOf(DEFAULT_EXCHANGE_RATE));
        usdAmountField.setText("0.00");
        phpAmountField.setText("0.00");
    }

    /**
     * Converts USD to PHP.
     */
    @FXML
    private void handleUsdToPhp() {
        try {
            double usd = Double.parseDouble(usdAmountField.getText());
            double rate = Double.parseDouble(exchangeRateField.getText());

            if (rate <= 0) {
                showError("Invalid Rate", "Exchange rate must be positive.");
                return;
            }

            double php = usd * rate;
            phpAmountField.setText(String.format("%.2f", php));
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers.");
        }
    }

    /**
     * Converts PHP to USD.
     */
    @FXML
    private void handlePhpToUsd() {
        try {
            double php = Double.parseDouble(phpAmountField.getText());
            double rate = Double.parseDouble(exchangeRateField.getText());

            if (rate <= 0) {
                showError("Invalid Rate", "Exchange rate must be positive.");
                return;
            }

            double usd = php / rate;
            usdAmountField.setText(String.format("%.2f", usd));
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter valid numbers.");
        }
    }

    /**
     * Closes the converter dialog.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows an error alert.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}