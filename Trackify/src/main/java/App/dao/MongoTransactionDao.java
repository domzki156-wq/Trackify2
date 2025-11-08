package App.dao;

import App.Database.MongoDBConnection;
import App.models.Transaction;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.Filters.eq;

/**
 * Mongo-backed Transaction DAO.
 * - stores transactions in "transactions" collection
 * - expects a "userId" field on each document to isolate user data
 */
public class MongoTransactionDao implements TransactionDao {
    private final MongoCollection<Document> coll;

    public MongoTransactionDao() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        this.coll = db.getCollection("transactions");
        // index by date for sort and by userId for per-user queries
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
                .append("notes", t.getNotes());

        // attach userId if present
        if (t.getUserId() != null && !t.getUserId().isBlank()) {
            d.append("userId", t.getUserId());
        }

        if (t.getId() != null && !t.getId().isBlank()) {
            try {
                d.put("_id", new ObjectId(t.getId()));
            } catch (IllegalArgumentException ex) {
                // if it's not a valid ObjectId, store as-is (string)
                d.put("_id", t.getId());
            }
        }
        return d;
    }

    private Transaction fromDocument(Document d) {
        if (d == null) return null;
        Transaction t = new Transaction();

        Object _id = d.get("_id");
        if (_id instanceof ObjectId) {
            t.setId(((ObjectId) _id).toHexString());
        } else if (_id != null) {
            t.setId(_id.toString());
        }

        // userId (may be null)
        t.setUserId(d.getString("userId"));

        // parse date if present
        String dateStr = d.getString("date");
        if (dateStr != null && !dateStr.isBlank()) {
            try {
                t.setDate(LocalDate.parse(dateStr));
            } catch (Exception ignored) { /* leave null if parsing fails */ }
        }

        t.setCustomer(d.getString("customer"));
        t.setItem(d.getString("item"));
        t.setPaymentMethod(d.getString("payment"));

        // revenue/cost might be Integer or Double or String in some documents -> handle generically
        Object revObj = d.get("revenue");
        if (revObj instanceof Number) {
            t.setRevenue(((Number) revObj).doubleValue());
        } else if (revObj != null) {
            try { t.setRevenue(Double.parseDouble(revObj.toString())); } catch (Exception ignored) {}
        }

        Object costObj = d.get("cost");
        if (costObj instanceof Number) {
            t.setCost(((Number) costObj).doubleValue());
        } else if (costObj != null) {
            try { t.setCost(Double.parseDouble(costObj.toString())); } catch (Exception ignored) {}
        }

        // profit may be stored, but if not present, recalc from revenue/cost
        Object profitObj = d.get("profit");
        if (profitObj instanceof Number) {
            t.setProfit(((Number) profitObj).doubleValue());
        } else if (profitObj != null) {
            try { t.setProfit(Double.parseDouble(profitObj.toString())); } catch (Exception ignored) {}
        } else {
            // ensure profit consistent
            t.setProfit(t.getRevenue() - t.getCost());
        }

        t.setNotes(d.getString("notes"));
        return t;
    }

    @Override
    public Transaction save(Transaction transaction) {
        // Ensure userId present: fallback to Session (if available) would be done by caller or service.
        // But we still keep as-is here.
        Document doc = toDocument(transaction);

        if (transaction.getId() == null || transaction.getId().isBlank()) {
            // Insert and obtain inserted id
            InsertOneResult res = coll.insertOne(doc);
            if (res.getInsertedId() != null) {
                ObjectId oid = null;
                try {
                    // inserted id may be an ObjectId
                    oid = res.getInsertedId().asObjectId().getValue();
                } catch (Exception ignored) {}
                if (oid != null) transaction.setId(oid.toHexString());
                else transaction.setId(res.getInsertedId().toString());
            } else {
                // backup: if driver didn't provide inserted id, try reading _id from doc
                Object insertedId = doc.get("_id");
                if (insertedId instanceof ObjectId) {
                    transaction.setId(((ObjectId) insertedId).toHexString());
                } else if (insertedId != null) {
                    transaction.setId(insertedId.toString());
                }
            }
        } else {
            // Replace existing document; attempt ObjectId first
            try {
                coll.replaceOne(eq("_id", new ObjectId(transaction.getId())), doc);
            } catch (IllegalArgumentException ex) {
                coll.replaceOne(eq("_id", transaction.getId()), doc);
            }
        }

        return transaction;
    }

    @Override
    public Optional<Transaction> findById(String id) {
        Document d;
        try {
            d = coll.find(eq("_id", new ObjectId(id))).first();
        } catch (IllegalArgumentException ex) {
            d = coll.find(eq("_id", id)).first();
        }
        return Optional.ofNullable(fromDocument(d));
    }

    @Override
    public List<Transaction> findAllForUser(String userId) {
        List<Transaction> out = new ArrayList<>();
        if (userId == null) return out;
        for (Document d : coll.find(eq("userId", userId)).sort(Sorts.descending("date"))) {
            Transaction t = fromDocument(d);
            if (t != null) out.add(t);
        }
        return out;
    }

    @Override
    public boolean deleteById(String id) {
        long deleted;
        try {
            deleted = coll.deleteOne(eq("_id", new ObjectId(id))).getDeletedCount();
        } catch (IllegalArgumentException ex) {
            deleted = coll.deleteOne(eq("_id", id)).getDeletedCount();
        }
        return deleted > 0;
    }

    @Override
    public void exportCsv(java.nio.file.Path outputPath) throws Exception {
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(outputPath)) {
            writer.write("Date,Customer,Item,Payment Method,Revenue,Cost,Profit,Notes");
            writer.newLine();
            List<Transaction> all = findAll(); // interface method: all users
            for (Transaction t : all) {
                String line = String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%s",
                        t.getDate() == null ? "" : t.getDate().toString(),
                        escapeCsv(t.getCustomer()),
                        escapeCsv(t.getItem()),
                        escapeCsv(t.getPaymentMethod()),
                        t.getRevenue(),
                        t.getCost(),
                        t.getProfit(),
                        escapeCsv(t.getNotes()));
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }

    @Override
    public void deleteAll() {
        coll.deleteMany(new Document());
    }

    @Override
    public int count() {
        return (int) coll.countDocuments();
    }

    @Override
    public List<Transaction> findAll() {
        List<Transaction> out = new ArrayList<>();
        for (Document d : coll.find().sort(Sorts.descending("date"))) {
            Transaction t = fromDocument(d);
            if (t != null) out.add(t);
        }
        return out;
    }
}
