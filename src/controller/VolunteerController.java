package controller;

import model.Volunteer;
import repository.VolunteerRepository;
import view.VolunteerView;

import javax.swing.*;
import java.util.ArrayList;

public class VolunteerController {

    private final VolunteerRepository volunteerRepository;

    public VolunteerController() {
        volunteerRepository = new VolunteerRepository();
    }

    public Volunteer addVolunteer(String name, String email, String phone, ArrayList<String> skills, String availability) {
        if (name == null || name.length() < 2 || name.length() > 100 || !name.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new IllegalArgumentException("Please enter a valid volunteer name.");
        }

        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please enter a valid email.");
        }

        if (phone == null || !phone.matches("^01[0125][0-9]{8}$")) {

            throw new IllegalArgumentException("Please enter a valid Egyptian mobile number.");
        }

        if (skills == null || skills.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one skill.");
        }

        if (availability == null || availability.isBlank()) {
            throw new IllegalArgumentException("Please select availability.");
        }

        if (volunteerRepository.findVolunteerByEmail(email) != null) {
            throw new IllegalArgumentException("A volunteer with this email already exists.");
        }

        return volunteerRepository.addVolunteer(name, email, phone, skills, availability);
    }

    public ArrayList<Volunteer> getVolunteers() {
        return volunteerRepository.getAllVolunteers();
    }

    public Volunteer findVolunteer(int id) {
        return volunteerRepository.findVolunteer(id);
    }

    public void addVolunteerHours(int id, double hours) {
        if (!Double.isFinite(hours) || hours <= 0) {
            throw new IllegalArgumentException("Volunteer hours must be greater than zero.");
        }

        Volunteer volunteer = findVolunteer(id);

        if (volunteer == null) {
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

        // Restore volunteers to table
        for (Volunteer volunteer : getVolunteers()) {

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

                JOptionPane.showMessageDialog(view, "Volunteer registered and saved successfully!");

            } catch (RuntimeException ex) {
                showError(view, ex.getMessage());
            }
        });

        // Add volunteer hours
        view.getAddHoursButton().addActionListener(e -> {

            try {
                int id = view.getSelectedVolunteerId();

                if (id == -1) {
                    throw new IllegalArgumentException("Please select a volunteer.");
                }

                double hours;

                try {
                    hours = Double.parseDouble(view.getHoursInput());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Please enter valid numeric hours.");
                }

                addVolunteerHours(id, hours);

                Volunteer volunteer = findVolunteer(id);

                view.updateVolunteerHours(id, volunteer.getVolunteerHours());

                view.clearHoursField();

                JOptionPane.showMessageDialog(view, "Volunteer hours updated and saved!");

            } catch (RuntimeException ex) {
                showError(view, ex.getMessage());
            }
        });
    }

    private void showError(VolunteerView view, String message) {
        JOptionPane.showMessageDialog(view, message, "Volunteer Error", JOptionPane.ERROR_MESSAGE);
    }
}