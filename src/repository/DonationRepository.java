package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import database.MongoDBConnection;
import model.Donation;
import model.Donor;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

public class DonationRepository {

    private final MongoCollection<Document> donations;
    private final MongoCollection<Document> counters;

    private final DonorRepository donorRepository;

    public DonationRepository() {

        MongoDatabase database = MongoDBConnection.getDatabase();

        donations = database.getCollection("donations");
        counters = database.getCollection("_counters");

        donorRepository = new DonorRepository();

        donations.createIndex(
                new Document("donationId", 1),
                new IndexOptions().unique(true)
        );

        donations.createIndex(
                new Document("donorId", 1)
        );
    }

    public Donation addDonation(
            Donor donor,
            double amount,
            String type,
            String method,
            String itemName,
            int quantity) {

        if (donor == null) {
            throw new IllegalArgumentException("Donor is required.");
        }

        if (donorRepository.findDonorById(donor.getId()) == null) {
            throw new IllegalArgumentException("Donor does not exist in the database.");
        }

        int donationId = getNextDonationId();

        Donation donation = new Donation(donationId, donor, amount, type, method);

        if (method.equalsIgnoreCase("In-Kind")) {
            donation.setInKindDetails(itemName, quantity);
        }

        Document document = new Document("donationId", donationId)
                .append("donorId", donor.getId())
                .append("amount", amount)
                .append("donationType", type)
                .append("donationMethod", method)
                .append("itemName", donation.getItemName())
                .append("quantity", donation.getQuantity());

        donations.insertOne(document);

        return donation;
    }

    public ArrayList<Donation> getAllDonations() {

        ArrayList<Donation> result = new ArrayList<>();

        for (Document document : donations.find().sort(ascending("donationId"))) {

            Donation donation = toDonation(document);

            if (donation != null) {
                result.add(donation);
            }
        }

        return result;
    }

    public Donation findDonationById(int donationId) {

        Document document = donations.find(
                eq("donationId", donationId)
        ).first();

        return document == null ? null : toDonation(document);
    }

    public ArrayList<Donation> findDonationsByDonorId(int donorId) {

        ArrayList<Donation> result = new ArrayList<>();

        for (Document document : donations.find(eq("donorId", donorId)).sort(ascending("donationId"))) {

            Donation donation = toDonation(document);

            if (donation != null) {
                result.add(donation);
            }
        }

        return result;
    }

    public boolean deleteDonation(int donationId) {

        return donations.deleteOne(eq("donationId", donationId)).getDeletedCount() > 0;
    }

    public void initializeIdCounter(int highestExistingId) {

        counters.updateOne(
                eq("_id", "donation"),
                new Document(
                        "$max",
                        new Document("sequence", highestExistingId)
                ),
                new UpdateOptions().upsert(true)
        );
    }

    public double getTotalDonations() {

        double total = 0;

        for (Document document : donations.find()) {
            total += document.getDouble("amount");
        }

        return total;
    }

    private int getNextDonationId() {

        Document counter = counters.findOneAndUpdate(
                eq("_id", "donation"),
                new Document("$inc", new Document("sequence", 1)),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );

        assert counter != null;
        return counter.getInteger("sequence");
    }

    private Donation toDonation(Document document) {

        int donorId = document.getInteger("donorId");

        Donor donor = donorRepository.findDonorById(donorId);

        if (donor == null) {
            System.err.println("Could not find donor " + donorId + " for donation " + document.getInteger("donationId"));

            return null;
        }

        Donation donation = new Donation(
                document.getInteger("donationId"),
                donor,
                document.getDouble("amount"),
                document.getString("donationType"),
                document.getString("donationMethod")
        );

        String method = document.getString("donationMethod");

        if (method != null && method.equalsIgnoreCase("In-Kind")) {
            donation.setInKindDetails(document.getString("itemName"), document.getInteger("quantity"));
        }

        return donation;
    }
}
