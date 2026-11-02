package storage;

import java.nio.file.Files;
import java.nio.file.Path;

public class LegacyDataTest {

    public static void main(String[] args) {

        Path dataFile = Path.of("data", "organization-data.dat");

        if (!Files.exists(dataFile)) {
            System.out.println("Legacy data file not found");
            return;
        }

        try {
            AppData data = DataStorage.load();

            System.out.println("Successfully loaded legacy data!");
            System.out.println("Donors: " + data.getDonors().size());
            System.out.println("Donations: " + data.getDonations().size());
            System.out.println("Volunteers: " + data.getVolunteers().size());
            System.out.println("Events: " + data.getEvents().size());
            System.out.println("Beneficiaries: " + data.getBeneficiaries().size());
            System.out.println("Lookup items: " + data.getLookupItems().size());
            System.out.println("Notifications: " + data.getNotifications().size());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}