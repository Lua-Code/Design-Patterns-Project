
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
    private int editingEventId = -1;

    public EventController() {
        this.eventRepository = new EventRepository();
    }

    public Event addEvent(
            String name,
            String type,
            String date,
            String location,
            String description,
            int capacity
    ) {
        validateEvent(name, type, date, location, description, capacity);

        return eventRepository.addEvent(
                name, type, date, location, description, capacity
        );
    }

    public void updateEvent(
            int eventId,
            String name,
            String type,
            String date,
            String location,
            String description,
            int capacity
    ) {
        validateEvent(name, type, date, location, description, capacity);

        Event existingEvent = findEvent(eventId);

        if (existingEvent == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        if (capacity < existingEvent.getRegisteredAttendees()) {
            throw new IllegalArgumentException(
                    "Capacity cannot be lower than the number of registered attendees."
            );
        }

        eventRepository.updateEvent(
                eventId, name, type, date, location, description, capacity
        );
    }

    private void validateEvent(
            String name,
            String type,
            String date,
            String location,
            String description,
            int capacity
    ) {
        if (name == null || name.isBlank() || name.length() > 100) {
            throw new IllegalArgumentException(
                    "Event name must contain 1 to 100 characters."
            );
        }

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Please select an event type."
            );
        }

        LocalDate eventDate;

        try {
            eventDate = LocalDate.parse(date);
        } catch (DateTimeParseException | NullPointerException ex) {
            throw new IllegalArgumentException(
                    "Date must use YYYY-MM-DD format."
            );
        }

        if (eventDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Event date cannot be in the past."
            );
        }

        if (location == null || location.isBlank()
                || location.length() > 150) {
            throw new IllegalArgumentException(
                    "Location must contain 1 to 150 characters."
            );
        }

        if (description == null || description.isBlank()
                || description.length() > 500) {
            throw new IllegalArgumentException(
                    "Description must contain 1 to 500 characters."
            );
        }

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero."
            );
        }
    }

    public ArrayList<Event> getEvents() {
        return new ArrayList<>(eventRepository.getAllEvents());
    }

    public Event findEvent(int id) {
        return eventRepository.findEvent(id);
    }

    public void registerAttendees(int eventId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Number of attendees must exceed zero."
            );
        }

        Event event = findEvent(eventId);

        if (event == null) {
            throw new IllegalArgumentException("Event not found.");
        }

        boolean registered = eventRepository.registerAttendees(
                eventId, quantity
        );

        if (!registered) {
            Event updatedEvent = findEvent(eventId);

            if (updatedEvent == null) {
                throw new IllegalArgumentException("Event not found.");
            }

            int available = updatedEvent.getCapacity()
                    - updatedEvent.getRegisteredAttendees();

            throw new IllegalArgumentException(
                    "Not enough places!\n"
                            + "Available: " + available
                            + "\nRequested: " + quantity
            );
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
        refreshEvents(view);

        view.getEventTable().getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        loadSelectedEvent(view);
                    }
                });

        view.getCreateButton().addActionListener(e -> {
            try {
                int capacity = parseCapacity(view.getCapacityInput());

                Event event = addEvent(
                        view.getEventName(),
                        view.getEventType(),
                        view.getEventDate(),
                        view.getEventLocation(),
                        view.getEventDescription(),
                        capacity
                );

                refreshEvents(view);
                view.clearFields();

                JOptionPane.showMessageDialog(
                        view,
                        "Event created and saved successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getUpdateButton().addActionListener(e -> {
            try {
                if (editingEventId == -1) {
                    throw new IllegalArgumentException(
                            "Please select an event to update."
                    );
                }

                int capacity = parseCapacity(view.getCapacityInput());

                updateEvent(
                        editingEventId,
                        view.getEventName(),
                        view.getEventType(),
                        view.getEventDate(),
                        view.getEventLocation(),
                        view.getEventDescription(),
                        capacity
                );

                editingEventId = -1;
                refreshEvents(view);
                view.clearFields();

                JOptionPane.showMessageDialog(
                        view,
                        "Event updated successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getCancelButton().addActionListener(e -> {
            editingEventId = -1;
            view.clearFields();
            view.getEventTable().clearSelection();
        });

        view.getDeleteButton().addActionListener(e -> {
            try {
                int eventId = view.getSelectedEventId();

                if (eventId == -1) {
                    throw new IllegalArgumentException(
                            "Please select an event to delete."
                    );
                }

                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "Are you sure you want to delete this event?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }

                if (!eventRepository.deleteEvent(eventId)) {
                    throw new IllegalArgumentException(
                            "Event could not be deleted."
                    );
                }

                editingEventId = -1;
                view.clearFields();
                refreshEvents(view);

                JOptionPane.showMessageDialog(
                        view,
                        "Event deleted successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getRegisterAttendeeButton().addActionListener(e -> {
            try {
                int eventId = view.getSelectedEventId();

                if (eventId == -1) {
                    throw new IllegalArgumentException(
                            "Please select an event."
                    );
                }

                int quantity;

                try {
                    quantity = Integer.parseInt(
                            view.getAttendeeCountInput()
                    );
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Attendee quantity must be a whole number."
                    );
                }

                registerAttendees(eventId, quantity);

                Event event = findEvent(eventId);

                view.updateAttendeeCount(
                        eventId, event.getRegisteredAttendees()
                );

                view.clearAttendeeCount();

                JOptionPane.showMessageDialog(
                        view,
                        quantity + " attendee(s) registered and saved!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getRefreshButton().addActionListener(e -> {
            editingEventId = -1;
            view.clearFields();
            view.getEventTable().clearSelection();
            refreshEvents(view);
        });

        view.getSearchButton().addActionListener(e -> searchEvents(view));
    }

    private int parseCapacity(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "Capacity must be a whole number."
            );
        }
    }

    private void loadSelectedEvent(EventView view) {
        int eventId = view.getSelectedEventId();

        if (eventId == -1) {
            return;
        }

        Event event = findEvent(eventId);

        if (event != null) {
            editingEventId = eventId;
            view.loadEvent(event);
        }
    }

    private void refreshEvents(EventView view) {
        view.displayEvents(getEvents());
    }

    private void searchEvents(EventView view) {
        String search = view.getSearchInput().toLowerCase();

        if (search.isBlank()) {
            refreshEvents(view);
            return;
        }

        ArrayList<Event> results = new ArrayList<>();

        for (Event event : getEvents()) {
            if (String.valueOf(event.getEventId()).contains(search)
                    || event.getEventName().toLowerCase().contains(search)
                    || event.getEventType().toLowerCase().contains(search)
                    || event.getLocation().toLowerCase().contains(search)
                    || event.getDate().contains(search)) {
                results.add(event);
            }
        }

        view.displayEvents(results);
    }

    private void showError(EventView view, String message) {
        JOptionPane.showMessageDialog(
                view,
                message,
                "Event Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
