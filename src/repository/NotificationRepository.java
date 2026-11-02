package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import model.NotificationRecord;
import org.bson.Document;
import database.MongoDBConnection;

import java.util.ArrayList;

public class NotificationRepository {

    private final MongoCollection<Document> notifications;

    public NotificationRepository() {
        this.notifications = MongoDBConnection.getDatabase().getCollection("notifications");
    }

    public void addNotification(NotificationRecord record) {

        Document document = new Document("method", record.getMethod())
                .append("recipient", record.getRecipient())
                .append("message", record.getMessage())
                .append("status", record.getStatus())
                .append("dateTime", record.getDateTime());

        notifications.insertOne(document);
    }

    // Get all notifications in chronological order
    public ArrayList<NotificationRecord> getAllNotifications() {

        ArrayList<NotificationRecord> result = new ArrayList<>();

        for (Document document : notifications.find().sort(Sorts.ascending("dateTime", "_id"))) {
            result.add(new NotificationRecord(
                    document.getString("method"),
                    document.getString("recipient"),
                    document.getString("message"),
                    document.getString("status"),
                    document.getString("dateTime")
            ));
        }

        return result;
    }

    public long getNotificationCount() {
        return notifications.countDocuments();
    }
}
