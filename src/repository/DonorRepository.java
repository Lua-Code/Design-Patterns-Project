package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.Document;

import model.Donor;
import database.MongoDBConnection;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

public class DonorRepository {

    private final MongoCollection<Document> donors;
    private final MongoCollection<Document> counters;

    public DonorRepository() {

        MongoDatabase database = MongoDBConnection.getDatabase();

        donors = database.getCollection("donors");
        counters = database.getCollection("_counters");

        donors.createIndex(new Document("id", 1), new IndexOptions().unique(true));

        donors.createIndex(new Document("normalizedEmail", 1), new IndexOptions().unique(true));
    }

    public Donor addDonor(String name, String email, String phone) {

        int id = getNextDonorId();

        Donor donor = new Donor(id, name, email, phone);

        Document document = new Document("id", donor.getId())
                .append("name", donor.getName())
                .append("email", donor.getEmail())
                .append("normalizedEmail", normalizeEmail(donor.getEmail()))
                .append("phone", donor.getPhone());

        donors.insertOne(document);

        return donor;
    }

    public ArrayList<Donor> getAllDonors() {

        ArrayList<Donor> result = new ArrayList<>();

        for (Document document : donors.find().sort(ascending("id"))) {
            result.add(toDonor(document));
        }

        return result;
    }

    public Donor findDonorById(int id) {

        Document document = donors.find(eq("id", id)).first();

        return document == null ? null : toDonor(document);
    }

    public Donor findDonorByEmail(String email) {

        if (email == null) {
            return null;
        }

        Document document = donors.find(
                eq("normalizedEmail", normalizeEmail(email))
        ).first();

        return document == null ? null : toDonor(document);
    }

    public boolean updateDonor(
            int id, String name, String email, String phone) {

        Document updates = new Document("name", name)
                .append("email", email)
                .append("normalizedEmail", normalizeEmail(email))
                .append("phone", phone);

        return donors.updateOne(
                eq("id", id),
                new Document("$set", updates)
        ).getModifiedCount() > 0;
    }

    public boolean deleteDonor(int id) {

        return donors.deleteOne(eq("id", id)).getDeletedCount() > 0;
    }

    public void initializeIdCounter(int highestExistingId) {

        counters.updateOne(
                eq("_id", "donor"),
                new Document("$max",
                        new Document("sequence", highestExistingId)),
                        new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    private int getNextDonorId() {

        Document counter = counters.findOneAndUpdate(
                eq("_id", "donor"),
                new Document("$inc", new Document("sequence", 1)),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );

        assert counter != null;
        return counter.getInteger("sequence");
    }

    private Donor toDonor(Document document) {

        return new Donor(
                document.getInteger("id"),
                document.getString("name"),
                document.getString("email"),
                document.getString("phone")
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
