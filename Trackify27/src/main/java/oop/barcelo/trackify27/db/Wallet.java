package oop.barcelo.trackify27.db;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

/**
 * Wallet model + service: handles all MongoDB wallet operations.
 */
public class Wallet {

    private static final String COLLECTION = "wallet";

    private static MongoCollection<Document> coll() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        return db.getCollection(COLLECTION);
    }

    /** Returns the current USD balance (0.0 if none). */
    public static double getBalanceUsd() {
        try {
            Document doc = coll().find().first();
            if (doc == null) return 0.0;
            Object o = doc.get("usd");
            if (o instanceof Number) return ((Number) o).doubleValue();
            try { return Double.parseDouble(o.toString()); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Atomically adds/subtracts balance.
     * Positive delta = deposit, negative delta = withdraw.
     * Returns the new balance after operation.
     */
    public static double changeBalanceUsd(double delta, String note) {
        try {
            Document update = new Document("$inc", new Document("usd", delta))
                    .append("$set", new Document("updatedAt", System.currentTimeMillis()))
                    .append("$push", new Document("history", new Document()
                            .append("change", delta)
                            .append("note", note == null ? "" : note)
                            .append("ts", System.currentTimeMillis())));
            FindOneAndUpdateOptions opts = new FindOneAndUpdateOptions()
                    .returnDocument(ReturnDocument.AFTER)
                    .upsert(true);

            Document after = coll().findOneAndUpdate(new Document(), update, opts);
            if (after != null && after.containsKey("usd")) {
                Object o = after.get("usd");
                if (o instanceof Number) return ((Number) o).doubleValue();
                try { return Double.parseDouble(o.toString()); } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBalanceUsd();
    }
}
