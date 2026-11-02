package repository;

import model.NotificationRecord;
import repository.NotificationRepository;
import database.MongoDBConnection;

public class Test {

    public static void main(String[] args) {

        NotificationRepository repository =
                new NotificationRepository();

        try {
            NotificationRecord record = new NotificationRecord(
                    "Email",
                    "test@example.com",
                    "This is a test notification",
                    "Simulated"
            );

            repository.addNotification(record);

            System.out.println("Total notifications: "
                    + repository.getNotificationCount());

            boolean found = repository.getAllNotifications()
                    .stream()
                    .anyMatch(n ->
                            n.getRecipient().equals("test@example.com")
                                    && n.getMessage().equals(
                                    "This is a test notification"
                            )
                    );

            System.out.println("Notification found: " + found);

        } finally {
            MongoDBConnection.close();
        }
    }
}