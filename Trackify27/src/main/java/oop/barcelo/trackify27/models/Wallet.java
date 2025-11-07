package oop.barcelo.trackify27.models;

import java.time.LocalDateTime;

public class Wallet {
    private String id;
    private String userId;
    private String name;
    private double balance;
    private String currency;
    private LocalDateTime lastUpdated;

    public Wallet() {}

    public Wallet(String id, String userId, String name, double balance, String currency) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
