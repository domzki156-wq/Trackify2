package App.Database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String currentUri;
    private static String currentDbName;

    // fallback (dev only) - remove credentials or use env var in production
    private static final String DEFAULT_URI = "mongodb+srv://Pantojas23:Mandreza23@trackify.v1ee3tp.mongodb.net/";
    private static final String DEFAULT_DB = "trackify";

    public static synchronized void init(String uri, String dbName) {
        if (mongoClient != null) return; // already initialized
        if (uri == null || uri.isBlank()) uri = DEFAULT_URI;
        if (dbName == null || dbName.isBlank()) dbName = DEFAULT_DB;
        currentUri = uri;
        currentDbName = dbName;
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase(dbName);
        System.out.println("✅ Connected to MongoDB: " + uri + " (DB: " + dbName + ")");
    }

    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            // lazy init with env var if not init() called
            String uri = System.getenv("MONGODB_URI");
            String dbName = System.getenv("TRACKIFY_DB");
            if (uri == null || uri.isBlank()) uri = DEFAULT_URI;
            if (dbName == null || dbName.isBlank()) dbName = DEFAULT_DB;
            init(uri, dbName);
        }
        return database;
    }

    public static synchronized void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            currentUri = null;
            currentDbName = null;
            System.out.println("MongoDB connection closed.");
        }
    }
}
