package controller;

import model.LookupItem;
import repository.LookupRepository;
import view.LookupView;

import javax.swing.*;
import java.util.ArrayList;

public class LookupController {

    private final LookupRepository lookupRepository;

    private int nextId = 1;

    public LookupController() {

        lookupRepository = new LookupRepository();
        lookupRepository.ensureDefaultItems();
    }

    // Identify values required for core system functionality
    private boolean isProtectedItem(LookupItem item) {

        return item.getCategory().equals("Donation Types")
                && item.getValue().equalsIgnoreCase("Money");
    }


    // CREATE
    public void addLookupItem(String category, String value) {
        validateCategory(category);
        validateValue(value);

        value = value.trim();

        if (lookupRepository.findLookupItem(category, value) != null) {
            throw new IllegalArgumentException("This item already exists in the selected category.");
        }

        lookupRepository.addLookupItem(category, value);
    }

    // READ
    public ArrayList<LookupItem> getAllLookupItems() {
        return lookupRepository.getAllLookupItems();
    }

    public ArrayList<LookupItem> getItemsByCategory(String category) {
        validateCategory(category);
        return lookupRepository.getItemsByCategory(category);
    }

    public LookupItem findLookupItem(int id) {
        return lookupRepository.findLookupItem(id);
    }

    // UPDATE
    public void updateLookupItem(int id, String newValue) {
        validateValue(newValue);

        LookupItem existingItem = findLookupItem(id);

        if (existingItem == null) {
            throw new IllegalArgumentException("Lookup item not found.");
        }

        if (isProtectedItem(existingItem)) {
            throw new IllegalArgumentException("The Money donation type cannot be modified.");
        }

        newValue = newValue.trim();

        LookupItem duplicate = lookupRepository.findLookupItem(existingItem.getCategory(), newValue);

        if (duplicate != null && duplicate.getId() != id) {
            throw new IllegalArgumentException("This item already exists in the selected category.");
        }

        lookupRepository.updateLookupItem(id, newValue);
    }

    // DELETE
    public void deleteLookupItem(int id) {
        LookupItem existingItem = findLookupItem(id);

        if (existingItem == null) {
            throw new IllegalArgumentException("Lookup item not found.");
        }

        if (isProtectedItem(existingItem)) {
            throw new IllegalArgumentException("The Money donation type cannot be deleted.");
        }

        lookupRepository.deleteLookupItem(id);
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