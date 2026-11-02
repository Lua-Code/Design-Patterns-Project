package repository;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import database.MongoDBConnection;
import model.LookupItem;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Sorts.ascending;


public class LookupRepository {
    private final MongoCollection<Document> lookupItems;
    private final MongoCollection<Document> counters;

    public LookupRepository(){
        MongoDatabase database = MongoDBConnection.getDatabase();

        lookupItems = database.getCollection("lookupitems");
        counters = database.getCollection("_counters");

        lookupItems.createIndex(new Document("id",1),new IndexOptions().unique(true));
        lookupItems.createIndex(new Document("category",1).append("normalizedValue",1),new IndexOptions().unique(true));
    }

    public LookupItem addLookupItem(String Category,String Value){
        int id = getNextId();

        LookupItem lookupItem = new LookupItem(id,Category,Value.trim());
        Document document = new Document("id", lookupItem.getId())
                .append("category",lookupItem.getCategory())
                .append("value",lookupItem.getValue())
                .append("normalizedValue",normalizeValue(lookupItem.getValue()));

        lookupItems.insertOne(document);
        return lookupItem;
    }

    public ArrayList<LookupItem> getAllLookupItems(){
        ArrayList<LookupItem> allItems = new ArrayList<LookupItem>();

        for (Document document : lookupItems.find().sort(ascending("id"))){
           allItems.add(toLookupItem(document));
        }
        return allItems;
    }

    public ArrayList<LookupItem> getItemsByCategory(String category){
        ArrayList<LookupItem> result = new ArrayList<LookupItem>();

        for (Document document : lookupItems.find(eq("category",category)).sort(ascending("id"))){
            result.add(toLookupItem(document));
        }
        return result;
    }

    public LookupItem findLookupItem(int id){
        Document document = lookupItems.find(eq("id",id)).first();

        return document == null ? null : toLookupItem(document);
    }

    public LookupItem findLookupItem(String category, String value) {

        Document document = lookupItems.find(new Document("category", category).append("normalizedValue", normalizeValue(value))).first();

        return document == null ? null : toLookupItem(document);
    }

    public boolean updateLookupItem(int id, String newValue) {

        return lookupItems.updateOne(eq("id", id),
                new Document("$set", new Document("value", newValue.trim())
                        .append("normalizedValue", normalizeValue(newValue))))
                .getMatchedCount() > 0;
    }

    public boolean deleteLookupItem(int id) {

        return lookupItems.deleteOne(eq("id", id))
                .getDeletedCount() > 0;
    }

    public void ensureDefaultItems() {

        addIfMissing("Volunteer Skills", "Teaching");
        addIfMissing("Volunteer Skills", "Event Organization");
        addIfMissing("Volunteer Skills", "First Aid");
        addIfMissing("Volunteer Skills", "Cooking");
        addIfMissing("Volunteer Skills", "Fundraising");
        addIfMissing("Volunteer Skills", "Driving");

        addIfMissing("Beneficiary Needs", "Food");
        addIfMissing("Beneficiary Needs", "Clothes");
        addIfMissing("Beneficiary Needs", "Medical Assistance");
        addIfMissing("Beneficiary Needs", "Financial Support");
        addIfMissing("Beneficiary Needs", "Education");
        addIfMissing("Beneficiary Needs", "Housing");

        addIfMissing("Donation Types", "Money");
        addIfMissing("Donation Types", "Food");
        addIfMissing("Donation Types", "Clothes");
        addIfMissing("Donation Types", "Medical Supplies");
        addIfMissing("Donation Types", "Electronics");
        addIfMissing("Donation Types", "Other");

        addIfMissing("Event Types", "Fundraising");
        addIfMissing("Event Types", "Workshop");
        addIfMissing("Event Types", "Food Distribution");
        addIfMissing("Event Types", "Outreach Program");
    }

    private void addIfMissing(String category, String value) {
        if (findLookupItem(category, value) != null) {
            return;
        }

        try {
            addLookupItem(category, value);

        } catch (MongoWriteException ex) {

            if (ex.getError().getCode() != 11000) {
                throw ex;
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

        return new LookupItem(document.getInteger("id"), document.getString("category"), document.getString("value"));
    }

    private String normalizeValue(String value) {
        return value.trim().toLowerCase();
    }
}
