package database;

public class MongoDBConnectionTest {

    public static void main(String[] args) {

        boolean connected = MongoDBConnection.testConnection();

        if (connected) {
            System.out.println("Successfully connected to MongoDB Atlas!");
            System.out.println("Database: " + MongoDBConnection.getDatabase().getName());
        } else {
            System.out.println("Could not connect to MongoDB Atlas.");
        }

        MongoDBConnection.close();
    }
}