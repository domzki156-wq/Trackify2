package App.controllers;

import App.service.CurrencyService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class CurrencyConverterController {

    @FXML private TextField usdField;
    @FXML private TextField phpField;
    @FXML private Label statusLabel;

    private Stage owner;

    public void setOwner(Stage s) { this.owner = s; }

    @FXML
    private void handleConvert() {
        String raw = (usdField == null ? "" : usdField.getText());
        double usd;
        try {
            usd = Double.parseDouble(raw.trim());
        } catch (Exception ex) {
            if (statusLabel != null) statusLabel.setText("Enter a valid USD number.");
            return;
        }

        if (statusLabel != null) statusLabel.setText("Converting...");
        if (phpField != null) phpField.setText("");


        CompletableFuture.supplyAsync(() -> {
            try {
                return CurrencyService.convertUsdToPhp(usd);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }).handle((result, throwable) -> {
            Platform.runLater(() -> {
                if (throwable != null) {
                    Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
                    String msg = cause.getMessage();
                    if (statusLabel != null) statusLabel.setText("Conversion failed (network). " + (msg == null ? "" : msg));
                    double fallback = CurrencyService.convertUsingFallback(usd);
                    if (phpField != null) phpField.setText(String.format("₱%.2f (fallback)", fallback));
                } else {
                    if (result == null) {
                        if (statusLabel != null) statusLabel.setText("Conversion returned no value.");
                        double fallback = CurrencyService.convertUsingFallback(usd);
                        if (phpField != null) phpField.setText(String.format("₱%.2f (fallback)", fallback));
                    } else {
                        if (statusLabel != null) statusLabel.setText("");
                        if (phpField != null) phpField.setText(String.format("₱%.2f", result));
                    }
                }
            });
            return null;
        });
    }

    @FXML
    private void handleClose() {
        if (owner != null) owner.close();
        else {
            Stage s = (Stage) (usdField == null ? null : usdField.getScene().getWindow());
            if (s != null) s.close();
        }
    }
}
