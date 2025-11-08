package App.Database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;


public final class MongoDBConnection {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    // fallback default (use your real URI or env var)
    private static final String DEFAULT_URI = "mongodb+srv://Pantojas23:Mandreza23@trackify.v1ee3tp.mongodb.net";
    private static final String DEFAULT_DB = "trackify";

    private MongoDBConnection() {}


    public static synchronized void init(String uri, String dbName) {
        if (mongoClient != null) return;
        if (uri == null || uri.isBlank()) uri = DEFAULT_URI;
        if (dbName == null || dbName.isBlank()) dbName = DEFAULT_DB;
        ConnectionString conn = new ConnectionString(uri + "/" + dbName + "?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(conn)
                .build();
        mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase(dbName);
        System.out.println("✅ Connected to MongoDB: " + uri + " (DB: " + dbName + ")");
    }

    public static synchronized MongoDatabase getDatabase() {
        if (database == null) {
            init(null, null);
        }
        return database;
    }

    public static synchronized void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
            database = null;
            System.out.println("🧹 MongoDB connection closed.");
        }
    }
}
