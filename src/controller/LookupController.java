package controller;

import model.LookupItem;
import repository.LookupRepository;
import view.LookupView;

import javax.swing.*;
import java.util.ArrayList;

public class LookupController {

    private final LookupRepository lookupRepository;

    public LookupController() {
        lookupRepository = new LookupRepository();
        lookupRepository.ensureDefaultItems();
    }

    public boolean isProtectedItem(LookupItem item) {
        return item != null && "Donation Types".equals(item.getCategory()) && "Money".equalsIgnoreCase(item.getValue());
    }

    public void addLookupItem(String category, String value) {
        validateCategory(category);
        validateValue(value);

        String cleanValue = value.trim();

        if (lookupRepository.findLookupItem(category, cleanValue) != null) {
            throw new IllegalArgumentException("This lookup item already exists in this category");
        }

        lookupRepository.addLookupItem(category, cleanValue);
    }

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

    public void updateLookupItem(int id, String newValue) {
        validateValue(newValue);

        LookupItem existingItem = lookupRepository.findLookupItem(id);

        if (existingItem == null) {
            throw new IllegalArgumentException("Lookup item was not found");
        }

        if (isProtectedItem(existingItem)) {
            throw new IllegalArgumentException("The Money donation type cannot be modified");
        }

        String cleanValue = newValue.trim();

        if (!existingItem.getValue().equalsIgnoreCase(cleanValue) && lookupRepository.isLookupItemInUse(existingItem.getCategory(), existingItem.getValue())) {
            throw new IllegalArgumentException("This lookup item is currently used by existing records and cannot be renamed");
        }

        LookupItem duplicate = lookupRepository.findLookupItem(existingItem.getCategory(), cleanValue);

        if (duplicate != null && duplicate.getId() != id) {
            throw new IllegalArgumentException("This lookup item already exists in this category");
        }

        lookupRepository.updateLookupItem(id, cleanValue);
    }

    public void deleteLookupItem(int id) {
        LookupItem existingItem = lookupRepository.findLookupItem(id);

        if (existingItem == null) {
            throw new IllegalArgumentException("Lookup item was not found");
        }

        if (isProtectedItem(existingItem)) {
            throw new IllegalArgumentException("The Money donation type cannot be deleted");
        }

        if (lookupRepository.isLookupItemInUse(existingItem.getCategory(), existingItem.getValue()
        )) {
            throw new IllegalArgumentException("This lookup item is currently used by existing records and cannot be deleted");
        }

        lookupRepository.deleteLookupItem(id);
    }

    private void validateCategory(String category) {
        if (category == null
                || !(category.equals("Volunteer Skills")
                || category.equals("Beneficiary Needs")
                || category.equals("Donation Types")
                || category.equals("Event Types"))) {
            throw new IllegalArgumentException("Please select a valid lookup category");
        }
    }

    private void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Lookup value cannot be empty");
        }

        if (value.trim().length() > 100) {
            throw new IllegalArgumentException("Lookup value cannot exceed 100 characters");
        }
    }

    public void connectView(LookupView view) {
        Runnable refresh = () -> {
            view.displayItems(getItemsByCategory(view.getSelectedCategory()));
            view.clearInput();
        };

        refresh.run();

        view.getCategoryBox().addActionListener(event -> refresh.run());

        view.getAddButton().addActionListener(event -> {
            try {
                addLookupItem(view.getSelectedCategory(), view.getValueInput()
                );

                refresh.run();

                JOptionPane.showMessageDialog(view, "Lookup item added successfully");
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(view, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        view.getUpdateButton().addActionListener(event -> {
            try {
                int selectedId = view.getSelectedItemId();

                if (selectedId == -1) {
                    throw new IllegalArgumentException("Please select a lookup item to update");
                }

                updateLookupItem(selectedId, view.getValueInput());

                refresh.run();

                JOptionPane.showMessageDialog(view, "Lookup item updated successfully");
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(view, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        view.getDeleteButton().addActionListener(event -> {
            try {
                int selectedId = view.getSelectedItemId();

                if (selectedId == -1) {
                    throw new IllegalArgumentException("Please select a lookup item to delete");
                }

                LookupItem selectedItem = findLookupItem(selectedId);

                if (isProtectedItem(selectedItem)) {
                    throw new IllegalArgumentException("The Money donation type cannot be deleted");
                }

                int confirmation = JOptionPane.showConfirmDialog(view,
                        "Are you sure you want to delete this lookup item?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmation != JOptionPane.YES_OPTION) {
                    return;
                }

                deleteLookupItem(selectedId);

                refresh.run();

                JOptionPane.showMessageDialog(view, "Lookup item deleted successfully");
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(view, exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}