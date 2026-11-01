
package factory;

public abstract class NotificationFactory {

    // Factory Method
    public abstract Notification createNotification();

    // Uses the notification created by the factory
    public void sendNotification(String recipient, String message) {

        Notification notification = createNotification();

        notification.send(recipient, message);
    }
}
