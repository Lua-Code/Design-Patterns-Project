
package storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import database.MongoDBConnection;
import org.bson.Document;

import java.util.List;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseSeeder {

    private static final MongoDatabase database =
            MongoDBConnection.getDatabase();

    private static final String RUN_ID = "initial-demo-seed-v1";

    private static int donorBase;
    private static int donationBase;
    private static int volunteerBase;
    private static int eventBase;
    private static int beneficiaryBase;
    private static int lookupBase;

    public static void main(String[] args) {

        try {
            initializeSeed();

            seedDonors();
            seedDonations();
            seedVolunteers();
            seedEvents();
            seedBeneficiaries();
            seedLookupItems();
            seedNotifications();

            updateCounters();

            System.out.println("\n========== SEEDING COMPLETE ==========");
            printCounts();

        } catch (Exception ex) {
            System.out.println("Seeding failed:");
            ex.printStackTrace();
        } finally {
            MongoDBConnection.close();
        }
    }

    // Saves the ID ranges so repeated executions reuse the same IDs.
    private static void initializeSeed() {

        MongoCollection<Document> runs =
                database.getCollection("demoSeedRuns");

        Document existing = runs.find(eq("_id", RUN_ID)).first();

        if (existing == null) {

            Document marker = new Document("_id", RUN_ID)
                    .append("donorBase", getNextBase("donor", 0))
                    .append("donationBase", getNextBase("donation", 1))
                    .append("volunteerBase", getNextBase("volunteer", 200))
                    .append("eventBase", getNextBase("event", 300))
                    .append("beneficiaryBase", getNextBase("beneficiaryId", 400))
                    .append("lookupBase", getNextBase("lookupItem", 0));

            runs.updateOne(
                    eq("_id", RUN_ID),
                    new Document("$setOnInsert", marker),
                    new UpdateOptions().upsert(true)
            );

            existing = runs.find(eq("_id", RUN_ID)).first();
        }

        donorBase = existing.getInteger("donorBase");
        donationBase = existing.getInteger("donationBase");
        volunteerBase = existing.getInteger("volunteerBase");
        eventBase = existing.getInteger("eventBase");
        beneficiaryBase = existing.getInteger("beneficiaryBase");
        lookupBase = existing.getInteger("lookupBase");

        System.out.println("Demo seed initialized");
    }

    private static int getNextBase(String counterId, int defaultValue) {

        Document counter = database.getCollection("_counters")
                .find(eq("_id", counterId))
                .first();

        int current = counter == null
                ? defaultValue
                : ((Number) counter.get("sequence")).intValue();

        return current + 1;
    }

    private static void seedDonors() {

        MongoCollection<Document> collection =
                database.getCollection("donors");

        String[][] donors = {
                {"Ahmed Hassan", "01012345671", "ahmed@example.com"},
                {"Mariam Adel", "01112345672", "mariam@example.com"},
                {"Omar Khaled", "01212345673", "omar@example.com"},
                {"Salma Mostafa", "01512345674", "salma@example.com"},
                {"Youssef Ali", "01012345675", "youssef@example.com"},
                {"Nour Ibrahim", "01112345676", "nour@example.com"},
                {"Karim Samir", "01212345677", "karim@example.com"},
                {"Farida Mahmoud", "01512345678", "farida@example.com"},
                {"Amr Tarek", "01012345679", "amr@example.com"},
                {"Hana Mohamed", "01112345670", "hana@example.com"},
                {"Mostafa Nabil", "01212345671", "mostafa@example.com"},
                {"Laila Ashraf", "01512345672", "laila@example.com"}
        };

        for (int i = 0; i < donors.length; i++) {

            int id = donorBase + i;
            String email = donors[i][2];

            Document document = new Document("id", id)
                    .append("name", donors[i][0])
                    .append("phone", donors[i][1])
                    .append("email", email)
                    .append("normalizedEmail", normalize(email));

            upsert(collection, "id", id, document);
        }

        System.out.println("Donors seeded");
    }

    private static void seedDonations() {

        MongoCollection<Document> collection =
                database.getCollection("donations");

        String[] types = {
                "Money", "Food", "Clothes", "Medical Supplies",
                "Money", "Electronics", "Food", "Money",
                "Clothes", "Medical Supplies"
        };

        String[] methods = {
                "Cash", "In-Kind", "In-Kind", "In-Kind",
                "Bank Transfer", "In-Kind", "In-Kind", "Cash",
                "In-Kind", "In-Kind"
        };

        String[] itemNames = {
                "", "Canned Food Boxes", "Winter Clothes",
                "First Aid Kits", "", "Laptops",
                "Rice Bags", "", "Blankets", "Medical Supplies"
        };

        int[] quantities = {
                0, 20, 15, 8, 0, 3, 30, 0, 12, 10
        };

        double[] amounts = {
                5000, 0, 0, 0, 2500, 0, 0, 1000, 0, 0
        };

        for (int i = 0; i < 25; i++) {

            int id = donationBase + i;
            int donorId = donorBase + (i % 12);
            int typeIndex = i % types.length;

            Document document = new Document("donationId", id)
                    .append("donorId", donorId)
                    .append("amount", amounts[typeIndex])
                    .append("donationType", types[typeIndex])
                    .append("donationMethod", methods[typeIndex])
                    .append("itemName", itemNames[typeIndex])
                    .append("quantity", quantities[typeIndex]);

            upsert(collection, "donationId", id, document);
        }

        System.out.println("Donations seeded");
    }

    private static void seedVolunteers() {

        MongoCollection<Document> collection =
                database.getCollection("volunteers");

        String[][] volunteers = {
                {"Omar Hassan", "omar.volunteer@example.com", "01022334451"},
                {"Mariam Ali", "mariam.volunteer@example.com", "01122334452"},
                {"Youssef Adel", "youssef.volunteer@example.com", "01222334453"},
                {"Nour Samir", "nour.volunteer@example.com", "01522334454"},
                {"Karim Ahmed", "karim.volunteer@example.com", "01022334455"},
                {"Salma Tarek", "salma.volunteer@example.com", "01122334456"},
                {"Hana Khaled", "hana.volunteer@example.com", "01222334457"},
                {"Amr Mostafa", "amr.volunteer@example.com", "01522334458"},
                {"Laila Mahmoud", "laila.volunteer@example.com", "01022334459"},
                {"Farida Nabil", "farida.volunteer@example.com", "01122334460"}
        };

        String[][] skills = {
                {"First Aid", "Teaching"},
                {"Fundraising", "Event Organization"},
                {"Driving", "First Aid"},
                {"Cooking", "Teaching"},
                {"Fundraising", "Driving"},
                {"Event Organization", "Cooking"},
                {"First Aid", "Cooking"},
                {"Teaching", "Fundraising"},
                {"Driving", "Event Organization"},
                {"First Aid", "Teaching"}
        };

        String[] availability = {
                "Weekends", "Weekdays", "Flexible", "Weekends",
                "Weekdays", "Flexible", "Weekends", "Weekdays",
                "Flexible", "Weekends"
        };

        for (int i = 0; i < volunteers.length; i++) {

            int id = volunteerBase + i;

            Document document = new Document("id", id)
                    .append("name", volunteers[i][0])
                    .append("email", volunteers[i][1])
                    .append("normalizedEmail", normalize(volunteers[i][1]))
                    .append("phone", volunteers[i][2])
                    .append("skills", List.of(skills[i]))
                    .append("availability", availability[i])
                    .append("volunteerHours", 5.0 + (i * 3));

            upsert(collection, "id", id, document);
        }

        System.out.println("Volunteers seeded");
    }

    private static void seedEvents() {

        MongoCollection<Document> collection =
                database.getCollection("events");

        String[][] events = {
                {"Winter Food Distribution", "Food Distribution",
                        "2026-11-15", "Nasr City"},
                {"Community Fundraiser", "Fundraising",
                        "2026-11-20", "Cairo"},
                {"First Aid Workshop", "Workshop",
                        "2026-11-25", "Heliopolis"},
                {"Community Outreach", "Outreach Program",
                        "2026-12-01", "Maadi"},
                {"School Supplies Drive", "Fundraising",
                        "2026-12-05", "New Cairo"},
                {"Food and Clothing Drive", "Food Distribution",
                        "2026-12-10", "Giza"}
        };

        for (int i = 0; i < events.length; i++) {

            int id = eventBase + i;
            int capacity = 50 + (i * 10);
            int attendees = 15 + (i * 5);

            Document document = new Document("eventId", id)
                    .append("eventName", events[i][0])
                    .append("eventType", events[i][1])
                    .append("date", events[i][2])
                    .append("location", events[i][3])
                    .append("description",
                            "Community support event organized by the nonprofit")
                    .append("capacity", capacity)
                    .append("registeredAttendees", attendees);

            upsert(collection, "eventId", id, document);
        }

        System.out.println("Events seeded");
    }

    private static void seedBeneficiaries() {

        MongoCollection<Document> collection =
                database.getCollection("beneficiaries");

        String[][] beneficiaries = {
                {"Mahmoud Hassan", "mahmoud.beneficiary@example.com", "01032334451"},
                {"Amina Adel", "amina.beneficiary@example.com", "01132334452"},
                {"Khaled Ibrahim", "khaled.beneficiary@example.com", "01232334453"},
                {"Sara Mostafa", "sara.beneficiary@example.com", "01532334454"},
                {"Hassan Ali", "hassan.beneficiary@example.com", "01032334455"},
                {"Mona Samir", "mona.beneficiary@example.com", "01132334456"},
                {"Tarek Mohamed", "tarek.beneficiary@example.com", "01232334457"},
                {"Dina Ashraf", "dina.beneficiary@example.com", "01532334458"},
                {"Adel Nabil", "adel.beneficiary@example.com", "01032334459"},
                {"Rana Khaled", "rana.beneficiary@example.com", "01132334460"},
                {"Ibrahim Tarek", "ibrahim.beneficiary@example.com", "01232334461"},
                {"Mariam Nabil", "mariam.beneficiary2@example.com", "01532334462"}
        };

        String[][] needs = {
                {"Food"},
                {"Clothes", "Food"},
                {"Medical Assistance"},
                {"Education"},
                {"Food", "Clothes"},
                {"Housing"},
                {"Financial Support"},
                {"Medical Assistance", "Food"},
                {"Clothes"},
                {"Education", "Financial Support"},
                {"Food"},
                {"Housing", "Medical Assistance"}
        };

        for (int i = 0; i < beneficiaries.length; i++) {

            int id = beneficiaryBase + i;
            String status = i % 3 == 0 ? "Completed" : "Pending";

            Document document = new Document("id", id)
                    .append("name", beneficiaries[i][0])
                    .append("email", beneficiaries[i][1])
                    .append("normalizedEmail",
                            normalize(beneficiaries[i][1]))
                    .append("phone", beneficiaries[i][2])
                    .append("needs", List.of(needs[i]))
                    .append("assistanceStatus", status);

            upsert(collection, "id", id, document);
        }

        System.out.println("Beneficiaries seeded");
    }

    private static void seedLookupItems() {

        MongoCollection<Document> collection =
                database.getCollection("lookupitems");

        String[][] items = {
                {"Volunteer Skills", "Logistics"},
                {"Donation Types", "Hygiene Kits"},
                {"Event Types", "Community Training"}
        };

        for (int i = 0; i < items.length; i++) {

            int id = lookupBase + i;
            String category = items[i][0];
            String value = items[i][1];

            Document existing = collection.find(
                    new Document("category", category)
                            .append("normalizedValue", normalize(value))
            ).first();

            if (existing != null) {
                continue;
            }

            Document document = new Document("id", id)
                    .append("category", category)
                    .append("value", value)
                    .append("normalizedValue", normalize(value));

            upsert(collection, "id", id, document);
        }

        System.out.println("Lookup items seeded");
    }

    private static void seedNotifications() {

        MongoCollection<Document> collection =
                database.getCollection("notifications");

        String[] recipients = {
                "ahmed@example.com",
                "mariam@example.com",
                "omar@example.com",
                "salma@example.com",
                "youssef@example.com",
                "nour@example.com",
                "karim@example.com",
                "farida@example.com",
                "amr@example.com",
                "hana@example.com",
                "mostafa@example.com",
                "laila@example.com",
                "omar.volunteer@example.com",
                "mahmoud.beneficiary@example.com",
                "mariam.volunteer@example.com"
        };

        for (int i = 0; i < recipients.length; i++) {

            String id = "demo-notification-" + (i + 1);

            Document document = new Document("_id", id)
                    .append("method", i % 2 == 0 ? "Email" : "SMS")
                    .append("recipient", recipients[i])
                    .append("message",
                            "Thank you for being part of our community support program")
                    .append("status", "Simulated")
                    .append("dateTime",
                            String.format(
                                    Locale.ROOT,
                                    "2026-10-%02d %02d:%02d:%02d",
                                    15 + (i / 3),
                                    9 + (i % 8),
                                    (i * 7) % 60,
                                    0
                            ));

            collection.updateOne(
                    eq("_id", id),
                    new Document("$setOnInsert", document),
                    new UpdateOptions().upsert(true)
            );
        }

        System.out.println("Notifications seeded");
    }

    private static void updateCounters() {

        updateCounter("donor", donorBase + 11);
        updateCounter("donation", donationBase + 24);
        updateCounter("volunteer", volunteerBase + 9);
        updateCounter("event", eventBase + 5);
        updateCounter("beneficiaryId", beneficiaryBase + 11);
        updateCounter("lookupItem", lookupBase + 2);

        System.out.println("Counters updated");
    }

    private static void updateCounter(String id, int highestId) {

        database.getCollection("_counters").updateOne(
                eq("_id", id),
                new Document("$max",
                        new Document("sequence", highestId)),
                new UpdateOptions().upsert(true)
        );
    }

    private static void upsert(
            MongoCollection<Document> collection,
            String field,
            int id,
            Document document) {

        collection.updateOne(
                eq(field, id),
                new Document("$setOnInsert", document),
                new UpdateOptions().upsert(true)
        );
    }

    private static String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static void printCounts() {

        String[] collections = {
                "donors",
                "donations",
                "volunteers",
                "events",
                "beneficiaries",
                "lookupitems",
                "notifications"
        };

        for (String name : collections) {

            long count = database.getCollection(name).countDocuments();

            System.out.println(name + ": " + count);
        }
    }
}
