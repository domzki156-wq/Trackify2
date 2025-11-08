package App.dao;

import App.Database.MongoDBConnection;
import App.models.Product;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoProductDao implements ProductDao {
    private final MongoCollection<Document> coll;

    public MongoProductDao() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.coll = db.getCollection("products");
        // create basic index on name for quick lookup
        coll.createIndex(Indexes.ascending("name"));
    }

    private Document toDoc(Product p) {
        Document d = new Document()
                .append("name", p.getName())
                .append("sku", p.getSku())
                .append("category", p.getCategory())
                .append("priceUsd", p.getPriceUsd())
                .append("stock", p.getStock())
                .append("createdAt", p.getCreatedAt());
        if (p.getId() != null && !p.getId().isBlank()) {
            try { d.put("_id", new ObjectId(p.getId())); }
            catch (IllegalArgumentException ex) { d.put("_id", p.getId()); }
        }
        return d;
    }

    private Product fromDoc(Document d) {
        if (d == null) return null;
        Product p = new Product();
        Object _id = d.get("_id");
        if (_id instanceof ObjectId) p.setId(((ObjectId)_id).toHexString());
        else if (_id != null) p.setId(_id.toString());
        p.setName(d.getString("name"));
        p.setSku(d.getString("sku"));
        p.setCategory(d.getString("category"));
        Object priceObj = d.get("priceUsd");
        if (priceObj instanceof Number) p.setPriceUsd(((Number) priceObj).doubleValue());
        Object stockObj = d.get("stock");
        if (stockObj instanceof Number) p.setStock(((Number) stockObj).intValue());
        p.setCreatedAt(d.getString("createdAt"));
        return p;
    }

    @Override
    public Product save(Product p) {
        Document d = toDoc(p);
        if (p.getId() == null || p.getId().isBlank()) {
            coll.insertOne(d);
            Object id = d.get("_id");
            if (id instanceof ObjectId) p.setId(((ObjectId)id).toHexString());
            else if (id != null) p.setId(id.toString());
        } else {
            try { coll.replaceOne(Filters.eq("_id", new ObjectId(p.getId())), d); }
            catch (IllegalArgumentException ex) { coll.replaceOne(Filters.eq("_id", p.getId()), d); }
        }
        return p;
    }

    @Override
    public Optional<Product> findById(String id) {
        Document d;
        try { d = coll.find(Filters.eq("_id", new ObjectId(id))).first(); }
        catch (IllegalArgumentException ex) { d = coll.find(Filters.eq("_id", id)).first(); }
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public Optional<Product> findByName(String name) {
        if (name == null) return Optional.empty();
        Document d = coll.find(Filters.eq("name", name)).first();
        if (d == null) {
            // try case-insensitive match
            d = coll.find(Filters.regex("name", "^" + name + "$", "i")).first();
        }
        return Optional.ofNullable(fromDoc(d));
    }

    @Override
    public List<Product> findAll() {
        List<Product> out = new ArrayList<>();
        for (Document d : coll.find()) {
            Product p = fromDoc(d);
            if (p != null) out.add(p);
        }
        return out;
    }

    @Override
    public boolean updateStock(String productId, int newStock) {
        try {
            if (productId == null) return false;
            try {
                coll.updateOne(Filters.eq("_id", new ObjectId(productId)), Updates.set("stock", newStock));
            } catch (IllegalArgumentException ex) {
                coll.updateOne(Filters.eq("_id", productId), Updates.set("stock", newStock));
            }
            return true;
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean adjustStock(String productId, int delta) {
        try {
            if (productId == null) return false;
            try {
                coll.updateOne(Filters.eq("_id", new ObjectId(productId)), Updates.inc("stock", delta));
            } catch (IllegalArgumentException ex) {
                coll.updateOne(Filters.eq("_id", productId), Updates.inc("stock", delta));
            }
            return true;
        } catch (Exception e) { return false; }
    }

    @Override
    public void deleteById(String id) {
        try {
            try { coll.deleteOne(Filters.eq("_id", new ObjectId(id))); }
            catch (IllegalArgumentException ex) { coll.deleteOne(Filters.eq("_id", id)); }
        } catch (Exception ignored) {}
    }
}
