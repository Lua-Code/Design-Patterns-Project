package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import database.MongoDBConnection;
import model.Volunteer;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

public class VolunteerRepository {

    private final MongoCollection<Document> volunteers;
    private final MongoCollection<Document> counters;

    public VolunteerRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();

        volunteers = database.getCollection("volunteers");
        counters = database.getCollection("_counters");

        volunteers.createIndex(new Document("id", 1), new IndexOptions().unique(true));

        volunteers.createIndex(new Document("normalizedEmail", 1), new IndexOptions().unique(true));

        initializeIdCounter(200);
    }

    public Volunteer addVolunteer(String name, String email, String phone,ArrayList<String> skills, String availability) {
        int id = getNextVolunteerId();

        Volunteer volunteer = new Volunteer(id, name, email, phone, skills, availability);

        Document document = new Document("id", volunteer.getId())
                .append("name", volunteer.getName())
                .append("email", volunteer.getEmail())
                .append("normalizedEmail", normalizeEmail(volunteer.getEmail()))
                .append("phone", volunteer.getPhone())
                .append("skills", volunteer.getSkills())
                .append("availability", volunteer.getAvailability())
                .append("volunteerHours", volunteer.getVolunteerHours());

        volunteers.insertOne(document);

        return volunteer;
    }

    public ArrayList<Volunteer> getAllVolunteers() {
        ArrayList<Volunteer> result = new ArrayList<>();

        for (Document document : volunteers.find().sort(ascending("id"))) {
            result.add(toVolunteer(document));
        }

        return result;
    }

    public Volunteer findVolunteer(int id) {
        Document document = volunteers.find(eq("id", id)).first();

        return document == null ? null : toVolunteer(document);
    }

    public Volunteer findVolunteerByEmail(String email) {
        Document document = volunteers.find(eq("normalizedEmail", normalizeEmail(email))).first();

        return document == null ? null : toVolunteer(document);
    }

    public boolean addVolunteerHours(int id, double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException("Volunteer hours must be greater than zero.");
        }

        return volunteers.updateOne(eq("id", id), new Document("$inc", new Document("volunteerHours", hours)))
                .getMatchedCount() > 0;
    }

    public double getTotalVolunteerHours() {
        double total = 0;

        for (Document document : volunteers.find()) {
            Number hours = document.get("volunteerHours", Number.class);

            if (hours != null) {
                total += hours.doubleValue();
            }
        }

        return total;
    }

    public long getVolunteerCount() {
        return volunteers.countDocuments();
    }

    public boolean deleteVolunteer(int id) {
        return volunteers.deleteOne(eq("id", id)).getDeletedCount() > 0;
    }

    public void initializeIdCounter(int highestExistingId) {
        counters.updateOne(
                eq("_id", "volunteer"),
                new Document("$max", new Document("sequence", highestExistingId)),
                new UpdateOptions().upsert(true)
        );
    }

    private int getNextVolunteerId() {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "volunteer"),
                new Document("$inc", new Document("sequence", 1)),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );

        return counter.getInteger("sequence");
    }

    private Volunteer toVolunteer(Document document) {
        ArrayList<String> skills = new ArrayList<>(document.getList("skills", String.class));

        Volunteer volunteer = new Volunteer(
                document.getInteger("id"),
                document.getString("name"),
                document.getString("email"),
                document.getString("phone"),
                skills,
                document.getString("availability")
        );

        Number hours = document.get("volunteerHours", Number.class);

        if (hours != null) {
            volunteer.setVolunteerHours(hours.doubleValue());
        }

        return volunteer;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}