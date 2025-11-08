package App.controllers;

import App.models.WalletAction;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.*;
import java.time.format.DateTimeFormatter;


public class WalletActionCell extends ListCell<WalletAction> {

    private final HBox root = new HBox(10);
    private final Label timeLabel = new Label();
    private final Label typeLabel = new Label();
    private final Label amountLabel = new Label();
    private final Region spacer = new Region();

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public WalletActionCell() {
        super();

        root.setPadding(new Insets(6, 10, 6, 10));
        root.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(245, 245, 245),
                new CornerRadii(8),
                Insets.EMPTY
        )));
        root.setBorder(new Border(new BorderStroke(
                javafx.scene.paint.Color.rgb(220, 220, 220),
                BorderStrokeStyle.SOLID,
                new CornerRadii(8),
                new BorderWidths(1)
        )));

        timeLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 11px;");
        typeLabel.setStyle("-fx-text-fill: #000000; -fx-font-size: 12px; -fx-font-weight: 600;");
        amountLabel.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().addAll(timeLabel, typeLabel, spacer, amountLabel);

        root.setOnMouseEntered(e -> root.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(255, 255, 255),
                new CornerRadii(8),
                Insets.EMPTY
        ))));

        root.setOnMouseExited(e -> root.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(245, 245, 245),
                new CornerRadii(8),
                Insets.EMPTY
        ))));
    }

    @Override
    protected void updateItem(WalletAction action, boolean empty) {
        super.updateItem(action, empty);

        if (empty || action == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        String timestampText = formatTimestamp(action.getTimestamp());
        timeLabel.setText(timestampText);

        String type = action.getType() == null ? "" : action.getType();
        typeLabel.setText(type);

        double amount = action.getAmount();
        amountLabel.setText(String.format("$%.2f", amount));

        if ("Deposit".equalsIgnoreCase(type)) {
            amountLabel.setStyle("-fx-text-fill: #008000; -fx-font-weight: bold;"); // dark green
        } else if ("Withdraw".equalsIgnoreCase(type)) {
            amountLabel.setStyle("-fx-text-fill: #b22222; -fx-font-weight: bold;"); // dark red
        } else if ("Buy".equalsIgnoreCase(type)) {
            amountLabel.setStyle("-fx-text-fill: #003366; -fx-font-weight: bold;"); // navy blue
        } else {
            amountLabel.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
        }

        setGraphic(root);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.playFromStart();
    }


    private String formatTimestamp(Object ts) {
        if (ts == null) return "";

        try {
            if (ts instanceof LocalDateTime) {
                return ((LocalDateTime) ts).format(TIME_FORMATTER);
            } else if (ts instanceof LocalDate) {
                return ((LocalDate) ts).atStartOfDay().format(TIME_FORMATTER);
            } else if (ts instanceof Instant) {
                return LocalDateTime.ofInstant((Instant) ts, ZoneId.systemDefault()).format(TIME_FORMATTER);
            } else if (ts instanceof Long) {
                Instant instant = Instant.ofEpochMilli((Long) ts);
                return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(TIME_FORMATTER);
            } else if (ts instanceof String) {
                String s = ((String) ts).trim();
                try {
                    return LocalDateTime.parse(s, DateTimeFormatter.ISO_DATE_TIME).format(TIME_FORMATTER);
                } catch (Exception ignored) {
                    return s;
                }
            }
        } catch (Exception e) {
            return ts.toString();
        }
        return ts.toString();
    }
}
