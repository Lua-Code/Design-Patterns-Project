package controller;

import model.LookupItem;
import storage.AppData;
import storage.DataStorage;
import view.LookupView;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class LookupController {

    private final AppData appData;
    private final ArrayList<LookupItem> lookupItems;

    private int nextId = 1;

    public LookupController(AppData appData) {

        this.appData = appData;
        this.lookupItems = appData.getLookupItems();

        // Initialize default values only once
        if (!appData.isLookupInitialized()) {

            addDefault("Volunteer Skills", "Teaching");
            addDefault("Volunteer Skills", "Event Organization");
            addDefault("Volunteer Skills", "First Aid");
            addDefault("Volunteer Skills", "Cooking");
            addDefault("Volunteer Skills", "Fundraising");
            addDefault("Volunteer Skills", "Driving");

            addDefault("Beneficiary Needs", "Food");
            addDefault("Beneficiary Needs", "Clothes");
            addDefault("Beneficiary Needs", "Medical Assistance");
            addDefault("Beneficiary Needs", "Financial Support");
            addDefault("Beneficiary Needs", "Education");
            addDefault("Beneficiary Needs", "Housing");

            addDefault("Donation Types", "Money");
            addDefault("Donation Types", "Food");
            addDefault("Donation Types", "Clothes");
            addDefault("Donation Types", "Medical Supplies");
            addDefault("Donation Types", "Electronics");
            addDefault("Donation Types", "Other");

            addDefault("Event Types", "Fundraising");
            addDefault("Event Types", "Workshop");
            addDefault("Event Types", "Food Distribution");
            addDefault("Event Types", "Outreach Program");

            appData.setLookupInitialized(true);
        }

        // Continue IDs after previously saved records
        for (LookupItem item : lookupItems) {
            nextId = Math.max(nextId, item.getId() + 1);
        }

        // Ensure the essential Money type exists.
        // This also repairs old saved data if Money was deleted.
        ensureEssentialDonationType();
    }

    private void addDefault(String category, String value) {
        lookupItems.add(new LookupItem(nextId++, category, value));
    }

    private void ensureEssentialDonationType() {

        for (LookupItem item : lookupItems) {

            if (item.getCategory().equals("Donation Types")
                    && item.getValue().equalsIgnoreCase("Money")) {

                return;
            }
        }

        addDefault("Donation Types", "Money");

        // Persist the restored value
        saveChanges();
    }

    // Identify values required for core system functionality
    private boolean isProtectedItem(LookupItem item) {

        return item.getCategory().equals("Donation Types")
                && item.getValue().equalsIgnoreCase("Money");
    }

    private void saveChanges() {

        try {
            DataStorage.save(appData);

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Could not save lookup changes: "
                            + ex.getMessage(),
                    ex
            );
        }
    }

    // CREATE
    public LookupItem addLookupItem(
            String category,
            String value) {

        validateCategory(category);
        validateValue(value);

        value = value.trim();

        for (LookupItem item : lookupItems) {

            if (item.getCategory().equals(category)
                    && item.getValue().equalsIgnoreCase(value)) {

                throw new IllegalArgumentException(
                        "This value already exists."
                );
            }
        }

        LookupItem item = new LookupItem(
                nextId++, category, value
        );

        lookupItems.add(item);

        try {
            saveChanges();

        } catch (IllegalStateException ex) {

            lookupItems.remove(item);
            nextId--;
            throw ex;
        }

        return item;
    }

    // READ
    public ArrayList<LookupItem> getAllLookupItems() {
        return new ArrayList<>(lookupItems);
    }

    public ArrayList<LookupItem> getItemsByCategory(
            String category) {

        validateCategory(category);

        ArrayList<LookupItem> result = new ArrayList<>();

        for (LookupItem item : lookupItems) {

            if (item.getCategory().equals(category)) {
                result.add(item);
            }
        }

        return result;
    }

    public LookupItem findLookupItem(int id) {

        for (LookupItem item : lookupItems) {

            if (item.getId() == id) {
                return item;
            }
        }

        return null;
    }

    // UPDATE
    public void updateLookupItem(int id, String newValue) {

        validateValue(newValue);

        LookupItem item = findLookupItem(id);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Lookup item not found."
            );
        }

        // Prevent changing essential system values
        if (isProtectedItem(item)) {

            throw new IllegalArgumentException(
                    "The Money donation type is required by the system.\n"
                            + "It cannot be renamed."
            );
        }

        newValue = newValue.trim();

        for (LookupItem existing : lookupItems) {

            if (existing.getId() != id
                    && existing.getCategory().equals(item.getCategory())
                    && existing.getValue().equalsIgnoreCase(newValue)) {

                throw new IllegalArgumentException(
                        "This value already exists."
                );
            }
        }

        String previousValue = item.getValue();

        item.setValue(newValue);

        try {
            saveChanges();

        } catch (IllegalStateException ex) {

            item.setValue(previousValue);
            throw ex;
        }
    }

    // DELETE
    public void deleteLookupItem(int id) {

        LookupItem item = findLookupItem(id);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Lookup item not found."
            );
        }

        // Prevent deleting essential system values
        if (isProtectedItem(item)) {

            throw new IllegalArgumentException(
                    "The Money donation type is required by the system.\n"
                            + "It cannot be deleted."
            );
        }

        int index = lookupItems.indexOf(item);

        lookupItems.remove(index);

        try {
            saveChanges();

        } catch (IllegalStateException ex) {

            lookupItems.add(index, item);
            throw ex;
        }
    }

    private void validateCategory(String category) {

        if (category == null ||
                (!category.equals("Volunteer Skills")
                        && !category.equals("Beneficiary Needs")
                        && !category.equals("Donation Types")
                        && !category.equals("Event Types"))) {

            throw new IllegalArgumentException(
                    "Invalid lookup category."
            );
        }
    }

    private void validateValue(String value) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    "Lookup value cannot be empty."
            );
        }

        if (value.trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Lookup value cannot exceed 100 characters."
            );
        }
    }

    public void connectView(LookupView view) {

        Runnable refresh = () -> {

            view.displayItems(
                    getItemsByCategory(view.getSelectedCategory())
            );

            view.clearInput();
        };

        refresh.run();

        view.getCategoryBox().addActionListener(
                e -> refresh.run()
        );

        // Add lookup value
        view.getAddButton().addActionListener(e -> {

            try {

                addLookupItem(
                        view.getSelectedCategory(),
                        view.getValueInput()
                );

                refresh.run();

                showSuccess(
                        view,
                        "Lookup value added successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {

                showError(view, ex.getMessage());
            }
        });

        // Update lookup value
        view.getUpdateButton().addActionListener(e -> {

            try {

                int id = view.getSelectedItemId();

                if (id == -1) {

                    throw new IllegalArgumentException(
                            "Please select an item to update."
                    );
                }

                updateLookupItem(
                        id,
                        view.getValueInput()
                );

                refresh.run();

                showSuccess(
                        view,
                        "Lookup value updated successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {

                showError(view, ex.getMessage());
            }
        });

        // Delete lookup value
        view.getDeleteButton().addActionListener(e -> {

            try {

                int id = view.getSelectedItemId();

                if (id == -1) {

                    throw new IllegalArgumentException(
                            "Please select an item to delete."
                    );
                }

                // Check protection before asking for confirmation
                LookupItem selectedItem = findLookupItem(id);

                if (selectedItem != null
                        && isProtectedItem(selectedItem)) {

                    throw new IllegalArgumentException(
                            "The Money donation type cannot be deleted."
                    );
                }

                int confirmation = JOptionPane.showConfirmDialog(
                        view,
                        "Delete this lookup value?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmation != JOptionPane.YES_OPTION) {
                    return;
                }

                deleteLookupItem(id);

                refresh.run();

                showSuccess(
                        view,
                        "Lookup value deleted successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {

                showError(view, ex.getMessage());
            }
        });
    }

    private void showSuccess(
            LookupView view,
            String message) {

        JOptionPane.showMessageDialog(
                view,
                message,
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(
            LookupView view,
            String message) {

        JOptionPane.showMessageDialog(
                view,
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}