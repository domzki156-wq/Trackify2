package App.dao;

import App.Database.MongoDBConnection;
import App.models.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class MongoUserDao implements UserDao {
    private final MongoCollection<Document> coll;

    public MongoUserDao() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.coll = db.getCollection("users");
        // ensure unique index on username (idempotent)
        coll.createIndex(new Document("username", 1));
    }

    private Document toDocument(User u) {
        Document d = new Document()
                .append("username", u.getUsername())
                .append("passwordHash", u.getPasswordHash())
                .append("createdAt", u.getCreatedAt() == null ? System.currentTimeMillis() : u.getCreatedAt().toEpochMilli());
        if (u.getId() != null) {
            try { d.put("_id", new ObjectId(u.getId())); } catch (IllegalArgumentException ex) { d.put("_id", u.getId()); }
        }
        return d;
    }

    private User fromDocument(Document d) {
        if (d == null) return null;
        User u = new User();
        Object _id = d.get("_id");
        if (_id instanceof ObjectId) u.setId(((ObjectId) _id).toHexString());
        else if (_id != null) u.setId(_id.toString());
        u.setUsername(d.getString("username"));
        u.setPasswordHash(d.getString("passwordHash"));
        if (d.containsKey("createdAt")) {
            long millis = d.getLong("createdAt");
            u.setCreatedAt(java.time.Instant.ofEpochMilli(millis));
        }
        return u;
    }

    @Override
    public User save(User user) {
        Document doc = toDocument(user);
        if (user.getId() == null || user.getId().isBlank()) {
            coll.insertOne(doc);
            Object id = doc.get("_id");
            if (id instanceof ObjectId) user.setId(((ObjectId) id).toHexString());
            else if (id != null) user.setId(id.toString());
        } else {
            try { coll.replaceOne(eq("_id", new ObjectId(user.getId())), doc); }
            catch (IllegalArgumentException ex) { coll.replaceOne(eq("_id", user.getId()), doc); }
        }
        return user;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Document d = coll.find(eq("username", username)).first();
        return Optional.ofNullable(fromDocument(d));
    }

    @Override
    public Optional<User> findById(String id) {
        Document d;
        try { d = coll.find(eq("_id", new ObjectId(id))).first(); }
        catch (IllegalArgumentException ex) { d = coll.find(eq("_id", id)).first(); }
        return Optional.ofNullable(fromDocument(d));
    }

    @Override
    public void deleteAll() {
        coll.deleteMany(new Document());
    }
}
