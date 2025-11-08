package App.dao;

import App.Database.MongoDBConnection;
import App.models.User;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

public class MongoUserDao implements UserDao {
    private final MongoCollection<Document> coll;

    public MongoUserDao() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        coll = db.getCollection("users");
    }

    private Document toDoc(User u) {
        Document d = new Document()
                .append("username", u.getUsername())
                .append("passwordHash", u.getPasswordHash())
                .append("createdAt", u.getCreatedAt() == null ? Instant.now().toString() : u.getCreatedAt().toString())
                .append("balance", u.getBalance());
        if (u.getId() != null && !u.getId().isBlank()) {
            try { d.put("_id", new ObjectId(u.getId())); }
            catch (IllegalArgumentException ex) { d.put("_id", u.getId()); }
        }
        return d;
    }

    private User fromDoc(Document d) {
        if (d == null) return null;
        User u = new User();
        Object id = d.get("_id");
        if (id instanceof ObjectId) u.setId(((ObjectId) id).toHexString());
        else if (id != null) u.setId(id.toString());

        u.setUsername(d.getString("username"));
        u.setPasswordHash(d.getString("passwordHash"));

        Object createdAtObj = d.get("createdAt");
        if (createdAtObj != null) {
            try { u.setCreatedAt(Instant.parse(createdAtObj.toString())); }
            catch (Exception ignored) { /* leave as null if parse fails */ }
        }

        Object bal = d.get("balance");
        if (bal instanceof Number) u.setBalance(((Number) bal).doubleValue());
        else if (bal != null) {
            try { u.setBalance(Double.parseDouble(bal.toString())); }
            catch (Exception ignored) { u.setBalance(0.0); }
        } else u.setBalance(0.0);

        return u;
    }

    @Override
    public Optional<User> findById(String id) {
        Document d;
        try {
            d = coll.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException ex) {
            d = coll.find(eq("_id", id)).first();
        }
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Document d = coll.find(eq("username", username)).first();
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public User save(User u) {
        Document d = toDoc(u);
        if (u.getId() == null || u.getId().isBlank()) {
            coll.insertOne(d);
            Object inserted = d.get("_id");
            if (inserted instanceof ObjectId) u.setId(((ObjectId) inserted).toHexString());
            else if (inserted != null) u.setId(inserted.toString());
        } else {
            try {
                coll.replaceOne(eq("_id", new ObjectId(u.getId())), d);
            } catch (IllegalArgumentException ex) {
                coll.replaceOne(eq("_id", u.getId()), d);
            }
        }
        return u;
    }

    @Override
    public void updateBalance(String userId, double newBalance) {
        try {
            coll.updateOne(eq("_id", new ObjectId(userId)), Updates.set("balance", newBalance));
        } catch (IllegalArgumentException ex) {
            coll.updateOne(eq("_id", userId), Updates.set("balance", newBalance));
        }
    }

    @Override
    public void deleteById(String id) {
        try { coll.deleteOne(eq("_id", new ObjectId(id))); }
        catch (IllegalArgumentException ex) { coll.deleteOne(eq("_id", id)); }
    }
}
