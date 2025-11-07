package App.Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoTest {
    public static void main(String[] args) {
        String uri = System.getenv("MONGODB_URI");
        if (uri == null || uri.isBlank()) {
            System.out.println("MONGODB_URI not set - using default in code");
        } else {
            System.out.println("Using MONGODB_URI from env");
        }
        try (MongoClient client = MongoClients.create(uri == null ? "mongodb://localhost:27017" : uri)) {
            MongoDatabase db = client.getDatabase("trackify");
            var col = db.getCollection("transactions");
            System.out.println("Connected. transactions count: " + col.countDocuments());
            for (Document d : col.find().limit(5)) {
                System.out.println(d.toJson());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
