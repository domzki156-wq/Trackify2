package oop.barcelo.trackify27.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class DBSeed {
    public static void seed() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        MongoCollection<Document> pm = db.getCollection("payment_methods");
        if (pm.countDocuments() == 0) {
            pm.insertOne(new Document("method_name", "PAYPAL"));
            pm.insertOne(new Document("method_name", "CASH"));
            pm.insertOne(new Document("method_name", "CARD"));
        }
        MongoCollection<Document> products = db.getCollection("products");
        if (products.countDocuments() == 0) {
            products.insertOne(new Document("name","Sample Product A").append("category","Digital").append("price",19.99).append("cost",5.0).append("createdAt",System.currentTimeMillis()));
            products.insertOne(new Document("name","Sample Product B").append("category","Digital").append("price",9.99).append("cost",2.0).append("createdAt",System.currentTimeMillis()));
        }
        MongoCollection<Document> inventory = db.getCollection("inventory");
        if (inventory.countDocuments() == 0) {
            // map first product to inventory
            for (Document p : products.find()) {
                inventory.insertOne(new Document("product_id", p.getObjectId("_id")).append("stock_quantity", 100).append("last_updated", System.currentTimeMillis()));
            }
        }
    }

    public static void main(String[] args) {
        seed();
        System.out.println("Seed done.");
    }
}
