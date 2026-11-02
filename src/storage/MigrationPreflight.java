package storage;

import com.mongodb.client.MongoDatabase;
import database.MongoDBConnection;
import model.*;
import org.bson.Document;

public class MigrationPreflight {

    public static void main(String[] args) {

        try {
            AppData data = DataStorage.load();
            MongoDatabase database = MongoDBConnection.getDatabase();

            System.out.println("========== LEGACY RECORDS ==========");

            System.out.println("\nDONORS");
            for (Donor donor : data.getDonors()) {
                System.out.println(
                        donor.getId() + " | " +
                                donor.getName() + " | " +
                                donor.getEmail()
                );
            }

            System.out.println("\nDONATIONS");
            for (Donation donation : data.getDonations()) {
                System.out.println(
                        donation.getDonationId() + " | Donor ID: " +
                                donation.getDonor().getId()
                );
            }

            System.out.println("\nVOLUNTEERS");
            for (Volunteer volunteer : data.getVolunteers()) {
                System.out.println(
                        volunteer.getId() + " | " +
                                volunteer.getName() + " | " +
                                volunteer.getEmail()
                );
            }

            System.out.println("\nVOLUNTEER SKILLS");
            for (Volunteer volunteer : data.getVolunteers()) {
                System.out.println(
                        volunteer.getName() + ": " + volunteer.getSkills()
                );
            }

            System.out.println("\nEVENTS");
            for (Event event : data.getEvents()) {
                System.out.println(
                        event.getEventId() + " | " +
                                event.getEventName()
                );
            }

            System.out.println("\nBENEFICIARIES");
            for (Beneficiary beneficiary : data.getBeneficiaries()) {
                System.out.println(
                        beneficiary.getId() + " | " +
                                beneficiary.getName() + " | " +
                                beneficiary.getEmail()
                );
            }

            System.out.println("\nBENEFICIARY NEEDS");
            for (Beneficiary beneficiary : data.getBeneficiaries()) {
                System.out.println(
                        beneficiary.getName() + ": " + beneficiary.getNeeds()
                );
            }

            System.out.println("\nLOOKUP ITEMS");
            for (LookupItem item : data.getLookupItems()) {
                System.out.println(
                        item.getId() + " | " +
                                item.getCategory() + " | " +
                                item.getValue()
                );
            }

            System.out.println("\nNOTIFICATIONS");
            for (NotificationRecord notification : data.getNotifications()) {
                System.out.println(
                        notification.getMethod() + " | " +
                                notification.getRecipient() + " | " +
                                notification.getStatus() + " | " +
                                notification.getDateTime()
                );
            }

            System.out.println("\n========== EXISTING MONGODB RECORDS ==========");

            String[] collections = {
                    "donors",
                    "donations",
                    "volunteers",
                    "events",
                    "beneficiaries",
                    "lookupitems",
                    "notifications"
            };

            for (String collection : collections) {
                System.out.println("\n" + collection.toUpperCase());

                for (Document document :
                        database.getCollection(collection).find()) {
                    System.out.println(document.toJson());
                }
            }

            System.out.println("\n========== COUNTERS ==========");

            for (Document counter :
                    database.getCollection("_counters").find()) {
                System.out.println(counter.toJson());
            }

            System.out.println("\nInspection completed. No data was modified.");

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            MongoDBConnection.close();
        }
    }
}