package database;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import config.DatabaseConfig;

public class MongoDBConnection {

    private static MongoClient mongoClient;

    private MongoDBConnection() {
    }

    public static synchronized MongoClient getClient() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(DatabaseConfig.getConnectionString());
        }

        return mongoClient;
    }

    public static MongoDatabase getDatabase() {
        return getClient().getDatabase(DatabaseConfig.getDatabaseName());
    }

    public static boolean testConnection() {
        try {
            getDatabase().runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            System.err.println("MongoDB connection failed: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void close() {
        if (mongoClient != null) {
            mongoClient.close();
            mongoClient = null;
        }
    }
}
