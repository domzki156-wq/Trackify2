package oop.barcelo.trackify27.models;

import javafx.beans.property.*;

public class Product {
    private final StringProperty id;
    private final StringProperty name;
    private final StringProperty category;
    private final DoubleProperty price;
    private final DoubleProperty cost;
    private final StringProperty description;

    public Product(String id, String name, String category, double price, double cost, String description) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.category = new SimpleStringProperty(category);
        this.price = new SimpleDoubleProperty(price);
        this.cost = new SimpleDoubleProperty(cost);
        this.description = new SimpleStringProperty(description);
    }

    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getCategory() { return category.get(); }
    public double getPrice() { return price.get(); }
    public double getCost() { return cost.get(); }
    public String getDescription() { return description.get(); }

    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty categoryProperty() { return category; }
    public DoubleProperty priceProperty() { return price; }
    public DoubleProperty costProperty() { return cost; }
    public StringProperty descriptionProperty() { return description; }
}
