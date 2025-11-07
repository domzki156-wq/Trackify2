package App.dao;

import App.Database.MongoDBConnection;
import App.models.Transaction;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

public class MongoTransactionDao implements TransactionDao {
    private final MongoCollection<Document> coll;

    public MongoTransactionDao() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.coll = db.getCollection("transactions");
        // indexes
        coll.createIndex(new Document("date", -1));
        coll.createIndex(new Document("userId", 1));
    }

    private Document toDocument(Transaction t) {
        Document d = new Document()
                .append("date", t.getDate() == null ? null : t.getDate().toString())
                .append("customer", t.getCustomer())
                .append("item", t.getItem())
                .append("payment", t.getPaymentMethod())
                .append("revenue", t.getRevenue())
                .append("cost", t.getCost())
                .append("profit", t.getProfit())
                .append("notes", t.getNotes())
                .append("userId", t.getUserId());
        if (t.getId() != null && !t.getId().isBlank()) {
            try { d.put("_id", new ObjectId(t.getId())); }
            catch (IllegalArgumentException ex) { d.put("_id", t.getId()); }
        }
        return d;
    }

    private Transaction fromDocument(Document d) {
        if (d == null) return null;
        Transaction t = new Transaction();
        Object _id = d.get("_id");
        if (_id instanceof ObjectId) t.setId(((ObjectId) _id).toHexString());
        else if (_id != null) t.setId(_id.toString());

        t.setUserId(d.getString("userId"));
        String dateStr = d.getString("date");
        if (dateStr != null) t.setDate(LocalDate.parse(dateStr));
        t.setCustomer(d.getString("customer"));
        t.setItem(d.getString("item"));
        t.setPaymentMethod(d.getString("payment"));
        t.setRevenue(d.containsKey("revenue") ? d.getDouble("revenue") : 0.0);
        t.setCost(d.containsKey("cost") ? d.getDouble("cost") : 0.0);
        t.setNotes(d.getString("notes"));

        // recalc profit
        t.setRevenue(t.getRevenue());
        t.setCost(t.getCost());
        return t;
    }

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction == null) throw new IllegalArgumentException("transaction is null");
        if (transaction.getUserId() == null || transaction.getUserId().isBlank())
            throw new IllegalArgumentException("transaction.userId is required");

        Document doc = toDocument(transaction);

        if (transaction.getId() == null || transaction.getId().isBlank()) {
            coll.insertOne(doc);
            Object insertedId = doc.get("_id");
            if (insertedId instanceof ObjectId) transaction.setId(((ObjectId) insertedId).toHexString());
            else if (insertedId != null) transaction.setId(insertedId.toString());
        } else {
            try { coll.replaceOne(eq("_id", new ObjectId(transaction.getId())), doc); }
            catch (IllegalArgumentException ex) { coll.replaceOne(eq("_id", transaction.getId()), doc); }
        }
        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        Document d;
        try { d = coll.find(eq("_id", new ObjectId(id))).first(); }
        catch (IllegalArgumentException ex) { d = coll.find(eq("_id", id)).first(); }
        return Optional.ofNullable(fromDocument(d));
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> out = new ArrayList<>();
        for (Document d : coll.find().sort(descending("date"))) out.add(fromDocument(d));
        return out;
    }

    @Override
    public List<Transaction> findAllForUser(String userId) {
        List<Transaction> out = new ArrayList<>();
        if (userId == null) return out;
        for (Document d : coll.find(eq("userId", userId)).sort(descending("date"))) out.add(fromDocument(d));
        return out;
    }

    @Override
    public boolean deleteById(String id) {
        long deleted;
        try { deleted = coll.deleteOne(eq("_id", new ObjectId(id))).getDeletedCount(); }
        catch (IllegalArgumentException ex) { deleted = coll.deleteOne(eq("_id", id)).getDeletedCount(); }
        return deleted > 0;
    }

    @Override
    public void exportCsv(java.nio.file.Path outputPath) throws Exception {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            writer.write("Date,Customer,Item,Payment Method,Revenue,Cost,Profit,Notes,UserId");
            writer.newLine();
            for (Transaction t : findAll()) {
                String line = String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%s,%s",
                        t.getDate() == null ? "" : t.getDate().toString(),
                        escapeCsv(t.getCustomer()), escapeCsv(t.getItem()),
                        escapeCsv(t.getPaymentMethod()), t.getRevenue(), t.getCost(), t.getProfit(),
                        escapeCsv(t.getNotes()), escapeCsv(t.getUserId()));
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }

    @Override
    public void deleteAll() { coll.deleteMany(new Document()); }

    @Override
    public int count() { return (int) coll.countDocuments(); }
}
