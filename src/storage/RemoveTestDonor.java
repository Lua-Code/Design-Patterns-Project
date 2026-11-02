package storage;

import com.mongodb.client.result.DeleteResult;
import database.MongoDBConnection;
import org.bson.Document;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class RemoveTestDonor {

    public static void main(String[] args) {

        try {
            DeleteResult result = MongoDBConnection.getDatabase()
                    .getCollection("donors")
                    .deleteOne(and(
                            eq("id", 2),
                            eq("normalizedEmail", "testdonor@example.com")
                    ));

            System.out.println(
                    "Deleted test donors: " + result.getDeletedCount()
            );

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            MongoDBConnection.close();
        }
    }
}