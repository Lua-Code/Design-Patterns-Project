
package controller;

import model.Event;
import repository.EventRepository;
import view.EventView;

import javax.swing.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class EventController {

    private final EventRepository eventRepository;

    public EventController() {
        this.eventRepository = new EventRepository();
    }

    public Event addEvent(String name, String type, String date, String location, String description, int capacity) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new IllegalArgumentException("Event name must contain 1 to 100 characters.");
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Please select an event type.");
        }

        LocalDate eventDate;

        try {
            eventDate = LocalDate.parse(date);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException("Date must use YYYY-MM-DD format.");
        }

        if (eventDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date cannot be in the past.");
        }

        if (location == null || location.isBlank() || location.length() > 150) {
            throw new IllegalArgumentException("Location must contain 1 to 150 characters.");
        }

        if (description == null || description.isBlank() || description.length() > 500) {
            throw new IllegalArgumentException("Description must contain 1 to 500 characters.");
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero.");
        }

        return eventRepository.addEvent(name, type, date, location, description, capacity);
    }

    public ArrayList<Event> getEvents() {
        return new ArrayList<>(eventRepository.getAllEvents());
    }

    public Event findEvent(int id) {
        return eventRepository.findEvent(id);
    }

    public void registerAttendees(int eventId, int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException("Number of attendees must exceed zero.");
        }

        Event event = findEvent(eventId);

        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        boolean registered = eventRepository.registerAttendees(eventId, quantity);

        if (!registered) {
            Event updatedEvent = findEvent(eventId);

            if (updatedEvent == null) {
                throw new IllegalArgumentException("Event not found.");
            }

            int available = updatedEvent.getCapacity() - updatedEvent.getRegisteredAttendees();

            throw new IllegalArgumentException("Not enough places!\n" + "Available: " + available + "\nRequested: " + quantity);
        }
    }

    public void registerAttendee(int eventId) {
        registerAttendees(eventId, 1);
    }

    public int getEventCount() {
        return Math.toIntExact(eventRepository.getEventCount());
    }

    public int getTotalAttendees() {
        return Math.toIntExact(eventRepository.getTotalAttendees());
    }

    public void connectView(EventView view) {

        for (Event event : getEvents()) {
            view.addEventToTable(
                    event.getEventId(),
                    event.getEventName(),
                    event.getEventType(),
                    event.getDate(),
                    event.getLocation(),
                    event.getCapacity(),
                    event.getRegisteredAttendees()
            );
        }

        view.getCreateButton().addActionListener(e -> {

            try {
                int capacity;

                try {
                    capacity = Integer.parseInt(view.getCapacityInput());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Capacity must be a whole number.");
                }

                Event event = addEvent(
                        view.getEventName(),
                        view.getEventType(),
                        view.getEventDate(),
                        view.getEventLocation(),
                        view.getEventDescription(),
                        capacity
                );

                view.addEventToTable(
                        event.getEventId(),
                        event.getEventName(),
                        event.getEventType(),
                        event.getDate(),
                        event.getLocation(),
                        event.getCapacity(),
                        event.getRegisteredAttendees()
                );

                view.clearFields();

                JOptionPane.showMessageDialog(view, "Event created and saved successfully!");

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        // Bulk attendee registration
        view.getRegisterAttendeeButton().addActionListener(e -> {

            try {
                int eventId = view.getSelectedEventId();

                if (eventId == -1) {
                    throw new IllegalArgumentException("Please select an event.");
                }

                int quantity;

                try {
                    quantity = Integer.parseInt(view.getAttendeeCountInput());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Attendee quantity must be a whole number.");
                }

                registerAttendees(eventId, quantity);

                Event event = findEvent(eventId);

                view.updateAttendeeCount(eventId, event.getRegisteredAttendees());

                view.clearAttendeeCount();

                JOptionPane.showMessageDialog(view, quantity + " attendee(s) registered and saved!");

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });
    }

    private void showError(EventView view, String message) {
        JOptionPane.showMessageDialog(view, message, "Event Error", JOptionPane.ERROR_MESSAGE);
    }
}
