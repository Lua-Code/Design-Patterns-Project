package model;

import java.io.Serializable;

public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private int eventId;
    private String eventName;
    private String eventType;
    private String date;
    private String location;
    private String description;
    private int capacity;
    private int registeredAttendees;

    public Event(int eventId, String eventName, String eventType,
                 String date, String location,
                 String description, int capacity) {

        this.eventId = eventId;
        this.eventName = eventName;
        this.eventType = eventType;
        this.date = date;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.registeredAttendees = 0;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getRegisteredAttendees() {
        return registeredAttendees;
    }

    public void registerAttendee() {
        if (registeredAttendees >= capacity) {
            throw new IllegalStateException(
                    "Event is fully booked."
            );
        }

        registeredAttendees++;
    }

    public void setRegisteredAttendees(int count) {
        if (count < 0 || count > capacity) {
            throw new IllegalArgumentException(
                    "Invalid registered attendee count."
            );
        }

        registeredAttendees = count;
    }

    public void displayInfo() {
        System.out.println("Event ID: " + eventId);
        System.out.println("Event: " + eventName);
        System.out.println("Type: " + eventType);
        System.out.println("Date: " + date);
        System.out.println("Location: " + location);
        System.out.println("Description: " + description);
        System.out.println("Capacity: " + capacity);
        System.out.println("Registered: " + registeredAttendees);
    }
}
