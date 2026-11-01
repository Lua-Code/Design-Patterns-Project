
package controller;

import model.Volunteer;
import storage.AppData;
import storage.DataStorage;
import view.VolunteerView;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class VolunteerController {

    private final AppData appData;
    private final ArrayList<Volunteer> volunteers;

    private int nextVolunteerId = 201;

    public VolunteerController(AppData appData) {

        this.appData = appData;
        this.volunteers = appData.getVolunteers();

        // Continue IDs after saved volunteers
        for (Volunteer volunteer : volunteers) {
            nextVolunteerId = Math.max(
                    nextVolunteerId,
                    volunteer.getId() + 1
            );
        }
    }

    private void saveData() {
        try {
            DataStorage.save(appData);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not save volunteer data: " + ex.getMessage(),
                    ex
            );
        }
    }

    public Volunteer addVolunteer(
            String name,
            String email,
            String phone,
            ArrayList<String> skills,
            String availability) {

        if (name == null || name.length() < 2
                || name.length() > 100
                || !name.matches("[\\p{L}][\\p{L} .'-]*")) {

            throw new IllegalArgumentException(
                    "Please enter a valid volunteer name."
            );
        }

        if (email == null ||
                !email.matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                )) {

            throw new IllegalArgumentException(
                    "Please enter a valid email."
            );
        }

        if (phone == null ||
                !phone.matches("^01[0125][0-9]{8}$")) {

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

        for (Volunteer volunteer : volunteers) {
            if (volunteer.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException(
                        "A volunteer with this email already exists."
                );
            }
        }

        Volunteer volunteer = new Volunteer(
                nextVolunteerId++,
                name, email, phone, skills, availability
        );

        volunteers.add(volunteer);

        try {
            saveData();
        } catch (IllegalStateException ex) {
            volunteers.remove(volunteer);
            nextVolunteerId--;
            throw ex;
        }

        return volunteer;
    }

    public ArrayList<Volunteer> getVolunteers() {
        return new ArrayList<>(volunteers);
    }

    public Volunteer findVolunteer(int id) {

        for (Volunteer volunteer : volunteers) {
            if (volunteer.getId() == id) {
                return volunteer;
            }
        }

        return null;
    }

    public void addVolunteerHours(int id, double hours) {

        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException(
                    "Volunteer hours must be greater than zero."
            );
        }

        Volunteer volunteer = findVolunteer(id);

        if (volunteer == null) {
            throw new IllegalArgumentException(
                    "Volunteer not found."
            );
        }

        double previousHours = volunteer.getVolunteerHours();

        volunteer.addHours(hours);

        try {
            saveData();
        } catch (IllegalStateException ex) {
            volunteer.setVolunteerHours(previousHours);
            throw ex;
        }
    }

    public double getTotalVolunteerHours() {

        double total = 0;

        for (Volunteer volunteer : volunteers) {
            total += volunteer.getVolunteerHours();
        }

        return total;
    }

    public int getVolunteerCount() {
        return volunteers.size();
    }

    public void connectView(VolunteerView view) {

        // Restore saved volunteers to table
        for (Volunteer volunteer : volunteers) {

            view.addVolunteerToTable(
                    volunteer.getId(),
                    volunteer.getName(),
                    volunteer.getEmail(),
                    volunteer.getPhone(),
                    volunteer.getSkills(),
                    volunteer.getAvailability(),
                    volunteer.getVolunteerHours()
            );
        }

        // Register Volunteer
        view.getRegisterButton().addActionListener(e -> {

            try {
                Volunteer volunteer = addVolunteer(
                        view.getVolunteerName(),
                        view.getVolunteerEmail(),
                        view.getVolunteerPhone(),
                        view.getSelectedSkills(),
                        view.getAvailability()
                );

                view.addVolunteerToTable(
                        volunteer.getId(),
                        volunteer.getName(),
                        volunteer.getEmail(),
                        volunteer.getPhone(),
                        volunteer.getSkills(),
                        volunteer.getAvailability(),
                        volunteer.getVolunteerHours()
                );

                view.clearFields();

                JOptionPane.showMessageDialog(
                        view,
                        "Volunteer registered and saved successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        // Add volunteer hours
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
                    hours = Double.parseDouble(
                            view.getHoursInput()
                    );
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(
                            "Please enter valid numeric hours."
                    );
                }

                addVolunteerHours(id, hours);

                Volunteer volunteer = findVolunteer(id);

                view.updateVolunteerHours(
                        id,
                        volunteer.getVolunteerHours()
                );

                view.clearHoursField();

                JOptionPane.showMessageDialog(
                        view,
                        "Volunteer hours updated and saved!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });
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
