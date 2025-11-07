package oop.barcelo.trackify27.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Optional;

public class MongoDBConnection {
    private static MongoClient client;
    private static MongoDatabase database;

    // Provided connection strings (Atlas + local)
    private static final String ATLAS = "mongodb+srv://trackify:KBARZELO2O$$@cluster0.vgm8xr4.mongodb.net/";
    private static final String LOCAL = "mongodb://localhost:27017/trackify2";

    public static synchronized MongoDatabase getDatabase() {
        if (database != null) return database;

        // Environment variable has highest priority
        String uri = Optional.ofNullable(System.getenv("MONGO_URI")).orElse(null);
        if (uri == null || uri.isBlank()) {
            // default to Atlas (you can swap to LOCAL if you prefer local first)
            uri = ATLAS;
        }

        client = MongoClients.create(uri);

        // Try to determine DB name from connection string, otherwise fallback to 'trackify'
        String dbName = "trackify";
        try {
            // If connection string contains a path like /dbname, use it
            int idx = uri.indexOf('/', uri.indexOf("://") + 3);
            if (idx != -1 && idx < uri.length() - 1) {
                String maybe = uri.substring(idx + 1).trim();
                if (!maybe.isBlank()) dbName = maybe;
            }
        } catch (Exception ignored) {}

        database = client.getDatabase(dbName);
        return database;
    }

    public static synchronized void close() {
        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }
}
