package controller;

import model.Donor;
import model.Donation;
import view.DonorView;

import repository.DonorRepository;
import repository.DonationRepository;

import javax.swing.*;
import java.util.ArrayList;

public class DonorController {

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;

    public DonorController() {

        donorRepository = new DonorRepository();
        donationRepository = new DonationRepository();
    }

    public void connectView(DonorView view) {

        refreshDonorTable(view);

        view.getRegisterButton().addActionListener(e -> {

            try {

                String name = view.getDonorName();
                String email = view.getDonorEmail();
                String phone = view.getDonorPhone();

                validateDonor(name, email, phone);

                Donor existing = donorRepository.findDonorByEmail(email);

                if (view.isEditing()) {

                    int id = view.getSelectedDonorId();

                    if (id == -1) {
                        throw new IllegalArgumentException(
                                "Please select a donor to update"
                        );
                    }

                    if (existing != null && existing.getId() != id) {
                        throw new IllegalArgumentException(
                                "Another donor is already registered with this email"
                        );
                    }

                    boolean updated = donorRepository.updateDonor(
                            id,
                            name,
                            email,
                            phone
                    );

                    if (!updated) {
                        throw new IllegalStateException(
                                "Could not update donor"
                        );
                    }

                    JOptionPane.showMessageDialog(
                            view,
                            "Donor updated successfully"
                    );

                } else {

                    if (existing != null) {
                        throw new IllegalArgumentException(
                                "A donor with this email already exists"
                        );
                    }

                    donorRepository.addDonor(name, email, phone);

                    JOptionPane.showMessageDialog(
                            view,
                            "Donor registered successfully"
                    );
                }

                refreshDonorTable(view);
                view.clearFields();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage(),
                        "Donor Management",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        view.getCancelButton().addActionListener(e -> {
            view.clearFields();
        });

        view.getDonorTable().getSelectionModel().addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int id = view.getSelectedDonorId();

            if (id == -1) {
                return;
            }

            Donor donor = donorRepository.findDonorById(id);

            if (donor != null) {
                view.populateForm(donor);
            }
        });

        view.getDeleteButton().addActionListener(e -> {

            try {

                int id = view.getSelectedDonorId();

                if (id == -1) {
                    throw new IllegalArgumentException(
                            "Please select a donor to delete"
                    );
                }

                ArrayList<Donation> donations =
                        donationRepository.findDonationsByDonorId(id);

                if (!donations.isEmpty()) {
                    throw new IllegalArgumentException(
                            "This donor has existing donations and cannot be deleted"
                    );
                }

                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "Are you sure you want to delete this donor?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }

                boolean deleted = donorRepository.deleteDonor(id);

                if (!deleted) {
                    throw new IllegalStateException(
                            "Could not delete donor"
                    );
                }

                JOptionPane.showMessageDialog(
                        view,
                        "Donor deleted successfully"
                );

                refreshDonorTable(view);
                view.clearFields();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage(),
                        "Donor Management",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        view.getSearchButton().addActionListener(e -> {
            searchDonorTable(view);
        });

        view.getRefreshButton().addActionListener(e -> {
            refreshDonorTable(view);
            view.clearFields();
        });
    }

    private void validateDonor(
            String name,
            String email,
            String phone) {

        if (name == null || name.trim().length() < 2 || name.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Name must be between 2 and 100 characters"
            );
        }

        if (email == null ||
                !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address"
            );
        }

        if (phone == null || !phone.matches("[0-9+() -]{7,20}")) {
            throw new IllegalArgumentException(
                    "Please enter a valid phone number"
            );
        }
    }

    private void searchDonorTable(DonorView view) {

        String search = view.getSearchText().toLowerCase();

        view.clearTable();

        for (Donor donor : donorRepository.getAllDonors()) {

            boolean matches =
                    String.valueOf(donor.getId()).contains(search) ||
                            donor.getName().toLowerCase().contains(search) ||
                            donor.getEmail().toLowerCase().contains(search);

            if (matches) {
                view.addDonorToTable(
                        donor.getId(),
                        donor.getName(),
                        donor.getEmail(),
                        donor.getPhone()
                );
            }
        }
    }

    private void refreshDonorTable(DonorView view) {

        view.clearTable();

        for (Donor donor : donorRepository.getAllDonors()) {

            view.addDonorToTable(
                    donor.getId(),
                    donor.getName(),
                    donor.getEmail(),
                    donor.getPhone()
            );
        }
    }
}