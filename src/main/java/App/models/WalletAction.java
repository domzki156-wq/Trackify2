package App.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Simple model representing a wallet action (Deposit / Withdraw / Buy).
 * Kept lightweight so it can be displayed in a ListView.
 */
public class WalletAction {
    private final LocalDateTime timestamp;
    private final String type;    // "Deposit", "Withdraw", "Buy"
    private final double amount;
    private final String note;

    public WalletAction(String type, double amount, String note) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.note = note == null ? "" : note;
    }

    // formatted for display
    public String getTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public LocalDateTime getTimestampRaw() {
        return timestamp;
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }

    @Override
    public String toString() {
        return String.format("%s %s $%.2f", getTimestamp(), type, amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WalletAction that = (WalletAction) o;
        return Double.compare(that.amount, amount) == 0 &&
                Objects.equals(timestamp, that.timestamp) &&
                Objects.equals(type, that.type) &&
                Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, type, amount, note);
    }
}
