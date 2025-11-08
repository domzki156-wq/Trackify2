package App.ui;

import javafx.scene.control.Dialog;

/**
 * Small helper to apply the app stylesheet to JavaFX Dialogs (Alert, TextInputDialog, custom Dialogs).
 */
public final class UiUtils {
    private UiUtils() {}

    /**
     * Applies the main app stylesheet to the dialog's DialogPane and adds a custom style class.
     * Safe to call repeatedly.
     */
    public static void applyAppCssToDialog(Dialog<?> dialog) {
        if (dialog == null) return;
        try {
            String css = UiUtils.class.getResource("/styles/app.css").toExternalForm();
            if (css != null && !dialog.getDialogPane().getStylesheets().contains(css)) {
                dialog.getDialogPane().getStylesheets().add(css);
            }
            if (!dialog.getDialogPane().getStyleClass().contains("custom-dialog-pane")) {
                dialog.getDialogPane().getStyleClass().add("custom-dialog-pane");
            }
        } catch (Exception e) {
            // fail safe: print stack (so dev can see missing resource) but keep running
            e.printStackTrace();
        }
    }
}
