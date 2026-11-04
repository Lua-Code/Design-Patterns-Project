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
import static com.mongodb.client.model.Sorts.descending;

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

        Document highestDonation = donations.find()
                .sort(descending("donationId"))
                .first();

        int highestExistingId = highestDonation == null
                ? 0
                : highestDonation.getInteger("donationId", 0);

        initializeIdCounter(highestExistingId);
    }

    public Donation addDonation(
            Donor donor,
            double amount,
            String type,
            String method,
            String itemName,
            int quantity) {

        return addDonation(
                donor, amount, type, method,
                itemName, quantity,
                "", "", "", "", "", false
        );
    }

    public Donation addDonation(
            Donor donor,
            double amount,
            String type,
            String method,
            String itemName,
            int quantity,
            String paymentEmail,
            String paymentReference,
            String bankName,
            String checkNumber,
            String accountHolder,
            boolean taxInfoSelected) {

        if (donor == null) {
            throw new IllegalArgumentException("Donor is required.");
        }

        if (donorRepository.findDonorById(donor.getId()) == null) {
            throw new IllegalArgumentException("Donor does not exist in the database.");
        }

        int donationId = getNextDonationId();

        Donation donation = new Donation(donationId, donor, amount, type, method);

        if ("In-Kind".equalsIgnoreCase(method)) {
            donation.setInKindDetails(itemName, quantity);
        }

        if ("Online".equalsIgnoreCase(method)) {
            donation.setOnlineDetails(paymentEmail, paymentReference);
        }

        if ("Check".equalsIgnoreCase(method)) {
            donation.setCheckDetails(bankName, checkNumber, accountHolder);
        }

        donation.setTaxInfoSelected(taxInfoSelected);

        Document document = new Document("donationId", donationId)
                .append("donorId", donor.getId())
                .append("amount", amount)
                .append("donationType", type)
                .append("donationMethod", method)
                .append("itemName", donation.getItemName())
                .append("quantity", donation.getQuantity())
                .append("paymentEmail", donation.getPaymentEmail())
                .append("paymentReference", donation.getPaymentReference())
                .append("bankName", donation.getBankName())
                .append("checkNumber", donation.getCheckNumber())
                .append("accountHolder", donation.getAccountHolder())
                .append("taxInfoSelected", donation.isTaxInfoSelected());

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

    public boolean updateDonation(Donation donation) {

        if (donation == null || donation.getDonor() == null) {
            throw new IllegalArgumentException("Donation and donor are required.");
        }

        if (donorRepository.findDonorById(donation.getDonor().getId()) == null) {
            throw new IllegalArgumentException("Donor does not exist in the database.");
        }

        Document updatedDonation = new Document("donorId", donation.getDonor().getId())
                .append("amount", donation.getAmount())
                .append("donationType", donation.getDonationType())
                .append("donationMethod", donation.getDonationMethod())
                .append("itemName", donation.getItemName())
                .append("quantity", donation.getQuantity())
                .append("paymentEmail", donation.getPaymentEmail())
                .append("paymentReference", donation.getPaymentReference())
                .append("bankName", donation.getBankName())
                .append("checkNumber", donation.getCheckNumber())
                .append("accountHolder", donation.getAccountHolder())
                .append("taxInfoSelected", donation.isTaxInfoSelected());

        return donations.updateOne(
                eq("donationId", donation.getDonationId()),
                new Document("$set", updatedDonation)
        ).getMatchedCount() > 0;
    }

    public boolean deleteDonation(int donationId) {

        return donations.deleteOne(eq("donationId", donationId)).getDeletedCount() > 0;
    }

    public void initializeIdCounter(int highestExistingId) {

        counters.updateOne(
                eq("_id", "donation"),
                new Document("$max",
                        new Document("sequence", highestExistingId)
                ),
                new UpdateOptions().upsert(true)
        );
    }

    public double getTotalDonations() {

        double total = 0;

        for (Document document : donations.find()) {

            Number amount = document.get("amount", Number.class);

            if (amount != null) {
                total += amount.doubleValue();
            }
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

        Number amount = document.get("amount", Number.class);

        Donation donation = new Donation(
                document.getInteger("donationId"),
                donor,
                amount == null ? 0 : amount.doubleValue(),
                document.getString("donationType"),
                document.getString("donationMethod")
        );

        String method = document.getString("donationMethod");

        if ("In-Kind".equalsIgnoreCase(method)) {

            String itemName = document.getString("itemName");
            Number quantity = document.get("quantity", Number.class);

            donation.setInKindDetails(itemName == null ? "" : itemName, quantity == null ? 0 : quantity.intValue());
        }

        if ("Online".equalsIgnoreCase(method)) {

            donation.setOnlineDetails(getStringOrEmpty(document, "paymentEmail"), getStringOrEmpty(document, "paymentReference"));
        }

        if ("Check".equalsIgnoreCase(method)) {

            donation.setCheckDetails(
                    getStringOrEmpty(document, "bankName"),
                    getStringOrEmpty(document, "checkNumber"),
                    getStringOrEmpty(document, "accountHolder")
            );
        }

        donation.setTaxInfoSelected(
                document.getBoolean("taxInfoSelected", false)
        );

        return donation;
    }

    private String getStringOrEmpty(Document document, String field) {

        String value = document.getString(field);

        return value == null ? "" : value;
    }
}
