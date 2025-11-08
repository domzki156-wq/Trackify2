package App.models;

import java.time.Instant;

public class Product {
    private String id;
    private String sku;
    private String name;
    private String category;
    private double priceUsd;
    private int stock;
    private String createdAt;

    public Product() {
        this.createdAt = Instant.now().toString();
    }

    public Product(String name, double priceUsd, int stock) {
        this();
        this.name = name;
        this.priceUsd = priceUsd;
        this.stock = stock;
    }

    // getters / setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPriceUsd() { return priceUsd; }
    public void setPriceUsd(double priceUsd) { this.priceUsd = priceUsd; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
