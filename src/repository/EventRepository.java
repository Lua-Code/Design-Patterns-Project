package repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import database.MongoDBConnection;
import model.Event;

import java.util.ArrayList;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;

public class EventRepository {

    private final MongoCollection<Document> events;
    private final MongoCollection<Document> counters;

    public EventRepository() {
        MongoDatabase database = MongoDBConnection.getDatabase();

        events = database.getCollection("events");
        counters = database.getCollection("_counters");

        events.createIndex(
                new Document("eventId", 1),
                new IndexOptions().unique(true)
        );

        initializeIdCounter(300);
    }

    public Event addEvent(
            String name,
            String type,
            String date,
            String location,
            String description,
            int capacity
    ) {
        int eventId = getNextEventId();

        Event event = new Event(
                eventId, name, type, date, location, description, capacity
        );

        Document document = new Document("eventId", event.getEventId())
                .append("eventName", event.getEventName())
                .append("eventType", event.getEventType())
                .append("date", event.getDate())
                .append("location", event.getLocation())
                .append("description", event.getDescription())
                .append("capacity", event.getCapacity())
                .append("registeredAttendees", event.getRegisteredAttendees());

        events.insertOne(document);

        return event;
    }

    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> result = new ArrayList<>();

        for (Document document : events.find().sort(ascending("eventId"))) {
            result.add(toEvent(document));
        }

        return result;
    }

    public Event findEvent(int eventId) {
        Document document = events.find(eq("eventId", eventId)).first();

        return document == null ? null : toEvent(document);
    }

    public boolean updateEvent(
            int eventId,
            String name,
            String type,
            String date,
            String location,
            String description,
            int capacity
    ) {
        Document updates = new Document("eventName", name)
                .append("eventType", type)
                .append("date", date)
                .append("location", location)
                .append("description", description)
                .append("capacity", capacity);

        return events.updateOne(
                eq("eventId", eventId),
                new Document("$set", updates)
        ).getMatchedCount() > 0;
    }

    public boolean registerAttendees(int eventId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Number of attendees must exceed zero."
            );
        }

        Document condition = new Document(
                "$lte",
                java.util.Arrays.asList(
                        new Document(
                                "$add",
                                java.util.Arrays.asList(
                                        "$registeredAttendees",
                                        quantity
                                )
                        ),
                        "$capacity"
                )
        );

        Document filter = new Document("eventId", eventId)
                .append("$expr", condition);

        return events.updateOne(
                filter,
                new Document(
                        "$inc",
                        new Document("registeredAttendees", quantity)
                )
        ).getModifiedCount() > 0;
    }

    public long getEventCount() {
        return events.countDocuments();
    }

    public int getTotalAttendees() {
        int total = 0;

        for (Document document : events.find()) {
            Number attendees = document.get(
                    "registeredAttendees", Number.class
            );

            if (attendees != null) {
                total += attendees.intValue();
            }
        }

        return total;
    }

    public boolean deleteEvent(int eventId) {
        return events.deleteOne(eq("eventId", eventId))
                .getDeletedCount() > 0;
    }

    public void initializeIdCounter(int highestExistingId) {
        counters.updateOne(
                eq("_id", "event"),
                new Document(
                        "$max",
                        new Document("sequence", highestExistingId)
                ),
                new UpdateOptions().upsert(true)
        );
    }

    private int getNextEventId() {
        Document counter = counters.findOneAndUpdate(
                eq("_id", "event"),
                new Document("$inc", new Document("sequence", 1)),
                new FindOneAndUpdateOptions()
                        .upsert(true)
                        .returnDocument(ReturnDocument.AFTER)
        );

        return counter.getInteger("sequence");
    }

    private Event toEvent(Document document) {
        Event event = new Event(
                document.getInteger("eventId"),
                document.getString("eventName"),
                document.getString("eventType"),
                document.getString("date"),
                document.getString("location"),
                document.getString("description"),
                document.getInteger("capacity")
        );

        Number attendees = document.get(
                "registeredAttendees", Number.class
        );

        if (attendees != null) {
            event.setRegisteredAttendees(attendees.intValue());
        }

        return event;
    }
}
