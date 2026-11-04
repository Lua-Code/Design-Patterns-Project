package repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import database.MongoDBConnection;
import model.LookupItem;
import org.bson.Document;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.descending;

public class LookupRepository {

    private final MongoCollection<Document> lookupItems;
    private final MongoCollection<Document> counters;
    private final MongoCollection<Document> volunteers;
    private final MongoCollection<Document> beneficiaries;
    private final MongoCollection<Document> donations;
    private final MongoCollection<Document> events;

    public LookupRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();

        lookupItems = database.getCollection("lookupitems");
        counters = database.getCollection("_counters");
        volunteers = database.getCollection("volunteers");
        beneficiaries = database.getCollection("beneficiaries");
        donations = database.getCollection("donations");
        events = database.getCollection("events");

        lookupItems.createIndex(
                new Document("id", 1),
                new IndexOptions().unique(true)
        );

        lookupItems.createIndex(
                new Document("category", 1)
                        .append("normalizedValue", 1),
                new IndexOptions().unique(true)
        );

        Document highestItem = lookupItems.find()
                .sort(descending("id"))
                .first();

        int highestExistingId = highestItem == null
                ? 0
                : highestItem.getInteger("id", 0);

        initializeIdCounter(highestExistingId);
    }

    public LookupItem addLookupItem(String category, String value) {
        int id = getNextId();
        String cleanValue = value.trim();

        Document document = new Document("id", id)
                .append("category", category)
                .append("value", cleanValue)
                .append("normalizedValue", normalizeValue(cleanValue));

        lookupItems.insertOne(document);

        return new LookupItem(id, category, cleanValue);
    }

    public ArrayList<LookupItem> getAllLookupItems() {
        ArrayList<LookupItem> result = new ArrayList<>();

        for (Document document : lookupItems.find().sort(ascending("id"))) {
            result.add(toLookupItem(document));
        }

        return result;
    }

    public ArrayList<LookupItem> getItemsByCategory(String category) {
        ArrayList<LookupItem> result = new ArrayList<>();

        for (Document document : lookupItems.find(eq("category", category)).sort(ascending("id"))) {
            result.add(toLookupItem(document));
        }

        return result;
    }

    public LookupItem findLookupItem(int id) {
        Document document = lookupItems.find(eq("id", id)).first();

        return document == null ? null : toLookupItem(document);
    }

    public LookupItem findLookupItem(String category, String value) {
        Document document = lookupItems.find(
                new Document("category", category)
                        .append("normalizedValue", normalizeValue(value))
        ).first();

        return document == null ? null : toLookupItem(document);
    }

    public boolean updateLookupItem(int id, String newValue) {
        String cleanValue = newValue.trim();

        Document updates = new Document("value", cleanValue)
                .append("normalizedValue", normalizeValue(cleanValue));

        return lookupItems.updateOne(
                eq("id", id),
                new Document("$set", updates)
        ).getMatchedCount() > 0;
    }

    public boolean deleteLookupItem(int id) {
        return lookupItems.deleteOne(eq("id", id)).getDeletedCount() > 0;
    }

    public boolean isLookupItemInUse(String category, String value) {
        switch (category) {
            case "Volunteer Skills":
                return volunteers.countDocuments(eq("skills", value)) > 0;

            case "Beneficiary Needs":
                return beneficiaries.countDocuments(eq("needs", value)) > 0;

            case "Donation Types":
                return donations.countDocuments(eq("donationType", value)) > 0;

            case "Event Types":
                return events.countDocuments(eq("eventType", value)) > 0;

            default:
                return false;
        }
    }

    public void ensureDefaultItems() {
        String[][] defaults = {
                {
                        "Volunteer Skills",
                        "Teaching",
                        "Event Organization",
                        "First Aid",
                        "Cooking",
                        "Fundraising",
                        "Driving"
                },
                {
                        "Beneficiary Needs",
                        "Food",
                        "Clothes",
                        "Medical Assistance",
                        "Financial Support",
                        "Education",
                        "Housing"
                },
                {
                        "Donation Types",
                        "Money",
                        "Food",
                        "Clothes",
                        "Medical Supplies",
                        "Electronics",
                        "Other"
                },
                {
                        "Event Types",
                        "Fundraising",
                        "Workshop",
                        "Food Distribution",
                        "Outreach Program"
                }
        };

        for (String[] categoryItems : defaults) {
            String category = categoryItems[0];

            for (int i = 1; i < categoryItems.length; i++) {
                addIfMissing(category, categoryItems[i]);
            }
        }
    }

    private void addIfMissing(String category, String value) {
        if (findLookupItem(category, value) != null) {
            return;
        }

        try {
            addLookupItem(category, value);
        } catch (MongoWriteException exception) {
            if (exception.getError().getCode() != 11000) {
                throw exception;
            }
        }
    }

    public void initializeIdCounter(int highestExistingId) {
        counters.updateOne(
                eq("_id", "lookupItem"),
                new Document(
                        "$max",
                        new Document("sequence", highestExistingId)
                ),
                new UpdateOptions().upsert(true)
        );
    }

    private int getNextId() {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "lookupItem"),
                new Document("$inc", new Document("sequence", 1)),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );

        return counter.getInteger("sequence");
    }

    private LookupItem toLookupItem(Document document) {
        return new LookupItem(
                document.getInteger("id"),
                document.getString("category"),
                document.getString("value")
        );
    }

    private String normalizeValue(String value) {
        return value.trim().toLowerCase();
    }
}