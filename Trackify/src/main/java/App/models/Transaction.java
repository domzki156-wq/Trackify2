package App.models;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Transaction model used by the UI and DAOs.
 * Each transaction belongs to a specific user (userId).
 */
public class Transaction {
    private String id;
    private String userId;          // <-- NEW: link to User._id in MongoDB
    private LocalDate date;
    private String customer;
    private String item;
    private String paymentMethod;
    private double revenue;
    private double cost;
    private double profit;
    private String notes;

    public Transaction() { }

    // Constructor used by UI (no id, profit auto-calculated)
    public Transaction(LocalDate date, String customer, String item,
                       String paymentMethod, double revenue, double cost, String notes) {
        this.id = null;
        this.date = date;
        this.customer = customer;
        this.item = item;
        this.paymentMethod = paymentMethod;
        this.revenue = revenue;
        this.cost = cost;
        this.profit = revenue - cost;
        this.notes = notes;
    }

    // Full constructor (if you need to set id)
    public Transaction(String id, String userId, LocalDate date, String customer,
                       String item, String paymentMethod, double revenue,
                       double cost, String notes) {
        this.id = id;
        this.userId = userId;
        this.date = date;
        this.customer = customer;
        this.item = item;
        this.paymentMethod = paymentMethod;
        this.revenue = revenue;
        this.cost = cost;
        this.profit = revenue - cost;
        this.notes = notes;
    }

    // --- getters & setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) {
        this.revenue = revenue;
        recalcProfit();
    }

    public double getCost() { return cost; }
    public void setCost(double cost) {
        this.cost = cost;
        recalcProfit();
    }

    public double getProfit() { return profit; }
    // no setProfit: profit derived from revenue - cost

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    private void recalcProfit() {
        this.profit = this.revenue - this.cost;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", date=" + date +
                ", customer='" + customer + '\'' +
                ", item='" + item + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", revenue=" + revenue +
                ", cost=" + cost +
                ", profit=" + profit +
                ", notes='" + notes + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
