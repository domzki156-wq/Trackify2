package App.models;

import java.time.LocalDate;
import java.util.Objects;


public class Transaction {

    private String id;
    private LocalDate date;
    private String customer;
    private String item;
    private String paymentMethod;
    private double revenue;   // primitive double (no null)
    private double cost;      // primitive double (no null)
    private String notes;
    private String userId;

    public Transaction() {
    }


    public Transaction(LocalDate date, String customer, String item,
                       String paymentMethod, double revenue, double cost, String notes) {
        this.date = date;
        this.customer = customer;
        this.item = item;
        this.paymentMethod = paymentMethod;
        this.revenue = revenue;
        this.cost = cost;
        this.notes = notes;
    }

    // --- id ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    // --- date ---
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    // --- customer ---
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }

    // --- item ---
    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    // --- paymentMethod ---
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    // --- revenue ---
    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    // --- cost ---
    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    // --- profit ---

    public double getProfit() {
        return this.revenue - this.cost;
    }

    public void setProfit(double ignoredProfit) {
    }

    // --- notes ---
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    // --- userId ---
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", date=" + date +
                ", customer='" + customer + '\'' +
                ", item='" + item + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", revenue=" + revenue +
                ", cost=" + cost +
                ", profit=" + getProfit() +
                ", notes='" + notes + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
