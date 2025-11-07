package oop.barcelo.trackify27.models;

import java.time.LocalDateTime;

public class Inventory {
    private String id;
    private String productId;
    private String location;
    private int stockQuantity;
    private int reorderLevel;
    private LocalDateTime lastUpdated;

    public Inventory() {}

    public Inventory(String id, String productId, String location, int stockQuantity, int reorderLevel) {
        this.id = id;
        this.productId = productId;
        this.location = location;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.lastUpdated = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
