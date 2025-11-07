package oop.barcelo.trackify27.models;

import javafx.beans.property.*;

public class Transaction {
    private final StringProperty id;
    private final StringProperty date;
    private final StringProperty customer;
    private final StringProperty item;
    private final StringProperty payment;
    private final DoubleProperty revenue;
    private final DoubleProperty cost;
    private final DoubleProperty profit;
    private final StringProperty notes;

    public Transaction(String id, String date, String customer, String item, String payment,
                       double revenue, double cost, double profit, String notes) {
        this.id = new SimpleStringProperty(id);
        this.date = new SimpleStringProperty(date);
        this.customer = new SimpleStringProperty(customer);
        this.item = new SimpleStringProperty(item);
        this.payment = new SimpleStringProperty(payment);
        this.revenue = new SimpleDoubleProperty(revenue);
        this.cost = new SimpleDoubleProperty(cost);
        this.profit = new SimpleDoubleProperty(profit);
        this.notes = new SimpleStringProperty(notes);
    }

    // Getters for properties (for TableView)
    public StringProperty idProperty() { return id; }
    public StringProperty dateProperty() { return date; }
    public StringProperty customerProperty() { return customer; }
    public StringProperty itemProperty() { return item; }
    public StringProperty paymentProperty() { return payment; }
    public DoubleProperty revenueProperty() { return revenue; }
    public DoubleProperty costProperty() { return cost; }
    public DoubleProperty profitProperty() { return profit; }
    public StringProperty notesProperty() { return notes; }

    // convenience getters
    public String getId() { return id.get(); }
    public String getDate() { return date.get(); }
    public String getCustomer() { return customer.get(); }
    public String getItem() { return item.get(); }
    public String getPayment() { return payment.get(); }
    public double getRevenue() { return revenue.get(); }
    public double getCost() { return cost.get(); }
    public double getProfit() { return profit.get(); }
    public String getNotes() { return notes.get(); }
}
