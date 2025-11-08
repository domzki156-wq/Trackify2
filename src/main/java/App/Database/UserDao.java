package App.Database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import com.mongodb.client.model.Updates;

public class UserDao {
    private final MongoCollection<Document> collection;

    public UserDao() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db.getCollection("users");
    }

    public void createUser(String id, String name, String email) {
        Document d = new Document("_id", id)
                .append("name", name)
                .append("email", email);
        collection.insertOne(d);
    }

    public Document getUserById(String id) {
        return collection.find(eq("_id", id)).first();
    }

    public List<Document> getAllUsers() {
        List<Document> out = new ArrayList<>();
        collection.find().into(out);
        return out;
    }

    public void updateUserEmail(String id, String newEmail) {
        collection.updateOne(eq("_id", id), Updates.set("email", newEmail));
    }

    public void deleteUser(String id) {
        collection.deleteOne(eq("_id", id));
    }
}
