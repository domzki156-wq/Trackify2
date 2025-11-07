package oop.barcelo.trackify27.controllers;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import oop.barcelo.trackify27.db.MongoDBConnection;
import oop.barcelo.trackify27.models.Product;
import org.bson.Document;
import org.bson.types.ObjectId;

public class ProductsController {
    @FXML public TextField nameField, categoryField, priceField, costField;
    @FXML public TableView<Product> productsTable;
    @FXML public TableColumn<Product,String> colName, colCategory;
    @FXML public TableColumn<Product,Number> colPrice, colCost;

    private MongoCollection<Document> productsColl;
    private final ObservableList<Product> products = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        productsColl = db.getCollection("products");
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()));
        colCost.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getCost()));
        productsTable.setItems(products);
        reload();
    }

    private void reload() {
        products.clear();
        for (Document d : productsColl.find()) {
            String id = d.getObjectId("_id").toHexString();
            products.add(new Product(id, d.getString("name"), d.getString("category"), d.containsKey("price") ? d.getDouble("price") : 0.0, d.containsKey("cost") ? d.getDouble("cost") : 0.0, d.getString("description")));
        }
    }

    @FXML
    private void onAddProduct() {
        try {
            String name = nameField.getText().trim();
            if (name.isEmpty()) { new Alert(Alert.AlertType.ERROR, "Name required").showAndWait(); return; }
            Document d = new Document().append("name", name)
                    .append("category", categoryField.getText())
                    .append("price", Double.parseDouble(priceField.getText()))
                    .append("cost", Double.parseDouble(costField.getText()))
                    .append("createdAt", System.currentTimeMillis());
            productsColl.insertOne(d);
            nameField.clear(); categoryField.clear(); priceField.clear(); costField.clear();
            reload();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Add failed: " + e.getMessage()).showAndWait();
        }
    }
}
