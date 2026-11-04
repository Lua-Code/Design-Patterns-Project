package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import database.MongoDBConnection;
import model.Beneficiary;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Updates.inc;

public class BeneficiaryRepository {

    private final MongoCollection<Document> beneficiaries;
    private final MongoCollection<Document> counters;

    public BeneficiaryRepository() {
        this.beneficiaries = MongoDBConnection.getDatabase().getCollection("beneficiaries");

        this.counters = MongoDBConnection.getDatabase().getCollection("_counters");

        createIndexes();
        initializeIdCounter(400);
    }

    private void createIndexes() {
        beneficiaries.createIndex(new Document("id", 1), new IndexOptions().unique(true));

        beneficiaries.createIndex(new Document("normalizedEmail", 1), new IndexOptions().unique(true));
    }

    public Beneficiary addBeneficiary(String name, String email, String phone, ArrayList<String> needs) {
        int id = getNextBeneficiaryId();

        Beneficiary beneficiary = new Beneficiary(id, name, email, phone, needs);

        Document document = new Document("id", id)
                .append("name", name)
                .append("email", email)
                .append("normalizedEmail", normalizeEmail(email))
                .append("phone", phone)
                .append("needs", needs)
                .append("assistanceStatus", beneficiary.getAssistanceStatus());

        beneficiaries.insertOne(document);

        return beneficiary;
    }

    public ArrayList<Beneficiary> getAllBeneficiaries() {
        ArrayList<Beneficiary> result = new ArrayList<>();

        for (Document document : beneficiaries.find().sort(ascending("id"))) {
            result.add(toBeneficiary(document));
        }

        return result;
    }

    public Beneficiary findBeneficiaryById(int id) {
        Document document = beneficiaries.find(eq("id", id)).first();

        return document == null ? null : toBeneficiary(document);
    }

    public Beneficiary findBeneficiaryByEmail(String email) {
        if (email == null) {
            return null;
        }

        Document document = beneficiaries.find(eq("normalizedEmail", normalizeEmail(email))).first();

        return document == null ? null : toBeneficiary(document);
    }

    public boolean updateAssistanceStatus(int id, String status) {
        Document update = new Document("$set", new Document("assistanceStatus", status));

        return beneficiaries.updateOne(eq("id", id), update).getMatchedCount() > 0;
    }

    public long getBeneficiaryCount() {
        return beneficiaries.countDocuments();
    }

    public long getPendingCount() {
        return beneficiaries.countDocuments(eq("assistanceStatus", "Pending"));
    }

    public long getCompletedCount() {
        return beneficiaries.countDocuments(eq("assistanceStatus", "Completed"));
    }

    public boolean deleteBeneficiary(int id) {
        return beneficiaries.deleteOne(eq("id", id)).getDeletedCount() > 0;
    }

    public void initializeIdCounter(int highestExistingId) {
        Document filter = new Document("_id", "beneficiaryId");

        Document update = new Document("$max", new Document("sequence", highestExistingId));

        counters.updateOne(filter, update, new com.mongodb.client.model.UpdateOptions().upsert(true));
    }

    private int getNextBeneficiaryId() {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "beneficiaryId"),
                inc("sequence", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );

        assert counter != null;
        return counter.getInteger("sequence");
    }

    private Beneficiary toBeneficiary(Document document) {
        List<String> storedNeeds = document.getList("needs", String.class, new ArrayList<>());

        Beneficiary beneficiary = new Beneficiary(
                document.getInteger("id"),
                document.getString("name"),
                document.getString("email"),
                document.getString("phone"),
                new ArrayList<>(storedNeeds)
        );

        String status = document.getString("assistanceStatus");

        if (status != null) {
            beneficiary.updateAssistanceStatus(status);
        }

        return beneficiary;
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public boolean updateBeneficiary(
            int id,
            String name,
            String email,
            String phone,
            ArrayList<String> needs) {

        Document update = new Document("$set",
                new Document("name", name)
                        .append("email", email)
                        .append("normalizedEmail", normalizeEmail(email))
                        .append("phone", phone)
                        .append("needs", needs));

        return beneficiaries.updateOne(eq("id", id), update)
                .getMatchedCount() > 0;
    }
}
