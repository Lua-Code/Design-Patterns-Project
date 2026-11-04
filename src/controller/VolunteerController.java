package controller;

import model.Volunteer;
import repository.VolunteerRepository;
import view.VolunteerView;

import javax.swing.*;
import java.util.ArrayList;

public class VolunteerController {

    private final VolunteerRepository volunteerRepository;
    private int editingVolunteerId = -1;

    public VolunteerController() {
        volunteerRepository = new VolunteerRepository();
    }

    public Volunteer addVolunteer(
            String name,
            String email,
            String phone,
            ArrayList<String> skills,
            String availability
    ) {
        validateVolunteer(name, email, phone, skills, availability);

        if (volunteerRepository.findVolunteerByEmail(email) != null) {
            throw new IllegalArgumentException(
                    "A volunteer with this email already exists."
            );
        }

        return volunteerRepository.addVolunteer(
                name, email, phone, skills, availability
        );
    }

    public void updateVolunteer(
            int id,
            String name,
            String email,
            String phone,
            ArrayList<String> skills,
            String availability
    ) {
        validateVolunteer(name, email, phone, skills, availability);

        Volunteer existing = findVolunteer(id);

        if (existing == null) {
            throw new IllegalArgumentException("Volunteer not found.");
        }

        Volunteer emailMatch = volunteerRepository.findVolunteerByEmail(email);

        if (emailMatch != null && emailMatch.getId() != id) {
            throw new IllegalArgumentException(
                    "A volunteer with this email already exists."
            );
        }

        volunteerRepository.updateVolunteer(
                id, name, email, phone, skills, availability
        );
    }

    private void validateVolunteer(
            String name,
            String email,
            String phone,
            ArrayList<String> skills,
            String availability
    ) {
        if (name == null || name.length() < 2 || name.length() > 100
                || !name.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new IllegalArgumentException(
                    "Please enter a valid volunteer name."
            );
        }

        if (email == null || !email.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )) {
            throw new IllegalArgumentException("Please enter a valid email.");
        }

        if (phone == null || !phone.matches("^01[0125][0-9]{8}$")) {
            throw new IllegalArgumentException(
                    "Please enter a valid Egyptian mobile number."
            );
        }

        if (skills == null || skills.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select at least one skill."
            );
        }

        if (availability == null || availability.isBlank()) {
            throw new IllegalArgumentException(
                    "Please select availability."
            );
        }
    }

    public ArrayList<Volunteer> getVolunteers() {
        return volunteerRepository.getAllVolunteers();
    }

    public Volunteer findVolunteer(int id) {
        return volunteerRepository.findVolunteer(id);
    }

    public void addVolunteerHours(int id, double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException(
                    "Volunteer hours must be greater than zero."
            );
        }

        if (findVolunteer(id) == null) {
            throw new IllegalArgumentException("Volunteer not found.");
        }

        volunteerRepository.addVolunteerHours(id, hours);
    }

    public double getTotalVolunteerHours() {
        return volunteerRepository.getTotalVolunteerHours();
    }

    public int getVolunteerCount() {
        return Math.toIntExact(volunteerRepository.getVolunteerCount());
    }

    public void connectView(VolunteerView view) {
        refreshVolunteers(view);

        view.getVolunteerTable().getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        loadSelectedVolunteer(view);
                    }
                });

        view.getRegisterButton().addActionListener(e -> {
            try {
                Volunteer volunteer = addVolunteer(
                        view.getVolunteerName(),
                        view.getVolunteerEmail(),
                        view.getVolunteerPhone(),
                        view.getSelectedSkills(),
                        view.getAvailability()
                );

                refreshVolunteers(view);
                view.clearFields();

                JOptionPane.showMessageDialog(
                        view,
                        "Volunteer registered and saved successfully!"
                );

            } catch (RuntimeException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getUpdateButton().addActionListener(e -> {
            try {
                if (editingVolunteerId == -1) {
                    throw new IllegalArgumentException(
                            "Please select a volunteer to update."
                    );
                }

                updateVolunteer(
                        editingVolunteerId,
                        view.getVolunteerName(),
                        view.getVolunteerEmail(),
                        view.getVolunteerPhone(),
                        view.getSelectedSkills(),
                        view.getAvailability()
                );

                refreshVolunteers(view);
                view.clearFields();
                editingVolunteerId = -1;

                JOptionPane.showMessageDialog(
                        view,
                        "Volunteer updated successfully!"
                );

            } catch (RuntimeException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getCancelButton().addActionListener(e -> {
            editingVolunteerId = -1;
            view.clearFields();
            view.getVolunteerTable().clearSelection();
        });

        view.getDeleteButton().addActionListener(e -> {
            try {
                int id = view.getSelectedVolunteerId();

                if (id == -1) {
                    throw new IllegalArgumentException(
                            "Please select a volunteer to delete."
                    );
                }

                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "Are you sure you want to delete this volunteer?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }

                if (!volunteerRepository.deleteVolunteer(id)) {
                    throw new IllegalArgumentException(
                            "Volunteer could not be deleted."
                    );
                }

                editingVolunteerId = -1;
                view.clearFields();
                refreshVolunteers(view);

                JOptionPane.showMessageDialog(
                        view,
                        "Volunteer deleted successfully!"
                );

            } catch (RuntimeException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getAddHoursButton().addActionListener(e -> {
            try {
                int id = view.getSelectedVolunteerId();

                if (id == -1) {
                    throw new IllegalArgumentException(
                            "Please select a volunteer."
                    );
                }

                double hours;

                try {
                    hours = Double.parseDouble(view.getHoursInput());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Please enter valid numeric hours."
                    );
                }

                addVolunteerHours(id, hours);

                refreshVolunteers(view);
                view.clearHoursField();

                JOptionPane.showMessageDialog(
                        view,
                        "Volunteer hours updated and saved!"
                );

            } catch (RuntimeException ex) {
                showError(view, ex.getMessage());
            }
        });

        view.getRefreshButton().addActionListener(e -> {
            view.getVolunteerTable().clearSelection();
            editingVolunteerId = -1;
            view.clearFields();
            refreshVolunteers(view);
        });

        view.getSearchButton().addActionListener(e -> {
            searchVolunteers(view);
        });
    }

    private void loadSelectedVolunteer(VolunteerView view) {
        int id = view.getSelectedVolunteerId();

        if (id == -1) {
            return;
        }

        Volunteer volunteer = findVolunteer(id);

        if (volunteer != null) {
            editingVolunteerId = id;
            view.loadVolunteer(volunteer);
        }
    }

    private void refreshVolunteers(VolunteerView view) {
        view.displayVolunteers(getVolunteers());
    }

    private void searchVolunteers(VolunteerView view) {
        String search = view.getSearchInput().toLowerCase();

        if (search.isBlank()) {
            refreshVolunteers(view);
            return;
        }

        ArrayList<Volunteer> results = new ArrayList<>();

        for (Volunteer volunteer : getVolunteers()) {
            if (String.valueOf(volunteer.getId()).contains(search)
                    || volunteer.getName().toLowerCase().contains(search)
                    || volunteer.getEmail().toLowerCase().contains(search)) {
                results.add(volunteer);
            }
        }

        view.displayVolunteers(results);
    }

    private void showError(VolunteerView view, String message) {
        JOptionPane.showMessageDialog(
                view,
                message,
                "Volunteer Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}