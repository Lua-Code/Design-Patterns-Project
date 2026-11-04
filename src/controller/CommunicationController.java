package controller;

import model.NotificationRecord;
import repository.NotificationRepository;
import view.CommunicationView;

import factory.NotificationFactory;
import factory.EmailNotificationFactory;
import factory.SMSNotificationFactory;

import javax.swing.*;
import java.util.ArrayList;

public class CommunicationController {

    private final NotificationRepository notificationRepository;

    public CommunicationController() {
        this.notificationRepository = new NotificationRepository();
    }

    public ArrayList<NotificationRecord> getNotifications() {
        return notificationRepository.getAllNotifications();
    }

    private void validateEmail(String email) {
        if (email == null
                || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("^01[0125][0-9]{8}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid Egyptian mobile number."
            );
        }
    }

    private void refreshHistory(CommunicationView view) {
        view.clearTable();

        for (NotificationRecord record : getNotifications()) {
            view.addNotificationToTable(
                    record.getMethod(),
                    record.getRecipient(),
                    record.getMessage(),
                    record.getStatus(),
                    record.getDateTime()
            );
        }
    }

    public void connectView(CommunicationView view) {

        refreshHistory(view);

        view.getSendButton().addActionListener(e -> {
            try {
                String method = view.getNotificationMethod();
                String recipient = view.getRecipient();
                String message = view.getMessage();

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

                if ("Email".equals(method)) {
                    validateEmail(recipient);
                    factory = new EmailNotificationFactory();

                } else if ("SMS".equals(method)) {
                    validatePhone(recipient);
                    factory = new SMSNotificationFactory();

                } else {
                    throw new IllegalArgumentException(
                            "Invalid notification method."
                    );
                }

                NotificationRecord newRecord = new NotificationRecord(
                        method,
                        recipient,
                        message,
                        "Simulated"
                );

                notificationRepository.addNotification(newRecord);

                factory.sendNotification(recipient, message);

                refreshHistory(view);

                JOptionPane.showMessageDialog(
                        view,
                        method + " notification simulated and saved!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                view.clearFields();

            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage(),
                        "Notification Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        view.getDeleteButton().addActionListener(e -> {
            try {
                NotificationRecord selectedRecord =
                        view.getSelectedNotification();

                if (selectedRecord == null) {
                    throw new IllegalArgumentException(
                            "Please select a notification to delete"
                    );
                }

                int confirmation = JOptionPane.showConfirmDialog(
                        view,
                        "Are you sure you want to delete this notification from history?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmation != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean deleted = notificationRepository.deleteNotification(
                        selectedRecord
                );

                if (!deleted) {
                    throw new IllegalStateException(
                            "The notification could not be found or deleted"
                    );
                }

                refreshHistory(view);

                JOptionPane.showMessageDialog(
                        view,
                        "Notification deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage(),
                        "Notification Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        view.getRefreshButton().addActionListener(e -> refreshHistory(view));
    }
}
