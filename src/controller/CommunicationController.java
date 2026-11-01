
package controller;

import model.NotificationRecord;

import storage.AppData;
import storage.DataStorage;

import view.CommunicationView;

import factory.NotificationFactory;
import factory.EmailNotificationFactory;
import factory.SMSNotificationFactory;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class CommunicationController {

    private final AppData appData;

    private final ArrayList<NotificationRecord> notifications;

    public CommunicationController(AppData appData) {

        this.appData = appData;
        this.notifications = appData.getNotifications();
    }

    // Save notification history
    private void saveData() {

        try {
            DataStorage.save(appData);

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Could not save notification history: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    // Get notification history
    public ArrayList<NotificationRecord> getNotifications() {
        return new ArrayList<>(notifications);
    }

    // Validate email
    private void validateEmail(String email) {

        if (email == null ||
                !email.matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                )) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }
    }

    // Validate Egyptian mobile number
    private void validatePhone(String phone) {

        if (phone == null ||
                !phone.matches("^01[0125][0-9]{8}$")) {

            throw new IllegalArgumentException(
                    "Please enter a valid Egyptian mobile number."
            );
        }
    }

    // Connect controller to GUI
    public void connectView(CommunicationView view) {

        // Restore previously saved notifications
        for (NotificationRecord record : notifications) {

            view.addNotificationToTable(
                    record.getMethod(),
                    record.getRecipient(),
                    record.getMessage(),
                    record.getStatus()
            );
        }

        // Send Notification button
        view.getSendButton().addActionListener(e -> {

            NotificationRecord newRecord = null;

            try {

                String method = view.getNotificationMethod();
                String recipient = view.getRecipient();
                String message = view.getMessage();

                // Validate message
                if (message == null || message.isBlank()) {
                    throw new IllegalArgumentException(
                            "Please enter a message."
                    );
                }

                if (message.length() > 1000) {
                    throw new IllegalArgumentException(
                            "Message cannot exceed 1000 characters."
                    );
                }

                // Factory Method Pattern
                NotificationFactory factory;

                switch (method) {

                    case "Email":

                        validateEmail(recipient);
                        factory = new EmailNotificationFactory();
                        break;

                    case "SMS":

                        validatePhone(recipient);
                        factory = new SMSNotificationFactory();
                        break;

                    default:

                        throw new IllegalArgumentException(
                                "Invalid notification method."
                        );
                }

                // Create notification record
                newRecord = new NotificationRecord(
                        method,
                        recipient,
                        message,
                        "Simulated"
                );

                notifications.add(newRecord);

                // Save record before confirming success
                saveData();

                // Simulate sending using Factory Method
                factory.sendNotification(recipient, message);

                // Update history table
                view.addNotificationToTable(
                        newRecord.getMethod(),
                        newRecord.getRecipient(),
                        newRecord.getMessage(),
                        newRecord.getStatus()
                );

                JOptionPane.showMessageDialog(
                        view,
                        method + " notification simulated and saved!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                view.clearFields();

            } catch (IllegalArgumentException | IllegalStateException ex) {

                // Roll back the new record when saving fails
                if (newRecord != null) {
                    notifications.remove(newRecord);
                }

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage(),
                        "Notification Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
