package controller;

import model.Donor;
import model.Donation;
import view.DonationView;

import strategy.*;
import observer.*;
import decorator.*;

import repository.DonorRepository;
import repository.DonationRepository;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Locale;

public class DonationController {

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;

    public DonationController() {

        donorRepository = new DonorRepository();
        donationRepository = new DonationRepository();
    }

    public Donation addDonation(
            Donor donor,
            double amount,
            String type,
            String method,
            String itemName,
            int quantity,
            String paymentEmail,
            String paymentReference,
            String bankName,
            String checkNumber,
            String accountHolder,
            boolean taxInfoSelected) {

        if (donor == null) {
            throw new IllegalArgumentException("Donor is required.");
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Invalid donation amount.");
        }

        if (amount == 0 && !method.equals("In-Kind")) {
            throw new IllegalArgumentException("Monetary donations must be greater than zero.");
        }

        return donationRepository.addDonation(
                donor, amount, type, method,
                itemName, quantity,
                paymentEmail, paymentReference,
                bankName, checkNumber, accountHolder,
                taxInfoSelected
        );
    }

    public ArrayList<Donor> getDonors() {
        return donorRepository.getAllDonors();
    }

    public ArrayList<Donation> getDonations() {
        return donationRepository.getAllDonations();
    }

    public double getTotalDonations() {
        return donationRepository.getTotalDonations();
    }

    private void validateAccountHolder(String name) {

        if (name == null || name.length() < 2 || name.length() > 100 || !name.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new IllegalArgumentException("Enter a valid account holder name.");
        }
    }

    private void validatePaymentEmail(String email) {

        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please enter a valid payment email address.");
        }
    }

    private double validateAmount(String input, String method) {

        double amount;

        try {
            amount = Double.parseDouble(input);

        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative or invalid.");
        }

        if (!method.equals("In-Kind") && amount == 0) {
            throw new IllegalArgumentException("Monetary donation must be greater than zero.");
        }

        return amount;
    }

    private DonationStrategy createStrategy(DonationView view, String method) {
        switch (method) {
            case "Online":

                validatePaymentEmail(view.getPaymentEmail());

                if (!view.getPaymentReference().matches("[A-Za-z0-9_-]{3,50}")) {

                    throw new IllegalArgumentException("Enter a valid payment reference.");
                }

                return new OnlineDonation(view.getPaymentEmail(), view.getPaymentReference());

            case "Check":
                if (view.getBankName().isBlank() || view.getBankName().length() > 100) {

                    throw new IllegalArgumentException("Enter a valid bank name.");
                }

                if (!view.getCheckNumber().matches("[A-Za-z0-9-]{3,30}")) {
                    throw new IllegalArgumentException("Enter a valid check number.");
                }

                validateAccountHolder(view.getAccountHolder());

                return new CheckDonation(view.getBankName(), view.getCheckNumber(), view.getAccountHolder());

            case "In-Kind":

                String itemName = view.getItemName();

                if (itemName.isBlank() || itemName.length() > 100) {
                    throw new IllegalArgumentException("Enter a valid item name.");
                }

                int quantity;

                try {
                    quantity = Integer.parseInt(view.getQuantity());

                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("Quantity must be a whole number.");
                }

                if (quantity <= 0) {
                    throw new IllegalArgumentException("Quantity must be greater than zero.");
                }

                return new InKindDonation(itemName, quantity);

            default:

                throw new IllegalArgumentException("Invalid donation method.");
        }
    }

    public void connectView(DonationView view) {

        // Load registered donors and saved donations
        view.loadDonors(getDonors());
        refreshDonationTable(view);

        view.getSubmitButton().addActionListener(e -> {

            try {

                if (view.isEditing()) {
                    throw new IllegalArgumentException("You are editing a donation. Use Save Changes instead.");
                }

                // Retrieve the selected registered donor
                Donor selectedDonor = view.getSelectedDonor();

                if (selectedDonor == null) {
                    throw new IllegalArgumentException("Please select a registered donor.");
                }

                // Retrieve the latest donor information from the database
                Donor donor = donorRepository.findDonorById(selectedDonor.getId());

                if (donor == null) {
                    throw new IllegalArgumentException("Selected donor no longer exists. Please refresh the donor list.");
                }

                String name = donor.getName();
                String email = donor.getEmail();

                String method = view.getDonationMethod();
                String type = view.getDonationType();

                if (method == null || type == null || type.isBlank()) {
                    throw new IllegalArgumentException("Select a valid donation method and type.");
                }

                if ((method.equals("Online") || method.equals("Check")) && !type.equalsIgnoreCase("Money")) {
                    throw new IllegalArgumentException("Monetary donations must use the Money type.");
                }

                if (method.equals("In-Kind") && type.equalsIgnoreCase("Money")) {
                    throw new IllegalArgumentException("Select a physical donation type.");
                }

                double amount = validateAmount(view.getAmount(), method);

                // Strategy Pattern
                DonationStrategy strategy = createStrategy(view, method);

                DonationProcessor processor = new DonationProcessor(strategy);

                if (!processor.processDonation(amount)) {
                    throw new IllegalArgumentException("Donation could not be accepted.");
                }

                String itemName = "";
                int quantity = 0;

                if (method.equals("In-Kind")) {
                    itemName = view.getItemName();
                    quantity = Integer.parseInt(view.getQuantity());
                }

                // Save the donation
                Donation newDonation = addDonation(
                        donor, amount, type, method,
                        itemName, quantity,
                        view.getPaymentEmail(),
                        view.getPaymentReference(),
                        view.getBankName(),
                        view.getCheckNumber(),
                        view.getAccountHolder(),
                        view.isTaxInfoSelected()
                );

                // Observer Pattern
                EventManager manager = new EventManager("donation");

                manager.subscribe("donation", new EmailNotificationListener(email));

                manager.subscribe("donation", new LogNotificationListener("DonationLog"));

                String description = method.equals("In-Kind")
                        ? name + " donated " + quantity + " x " + itemName + "."
                        : name + " donated " + amount + " EGP.";

                // Notification failure must not affect a saved donation
                try {
                    manager.notify("donation", description);

                } catch (RuntimeException notificationError) {
                    System.err.println("Donation saved, but notification failed: " + notificationError.getMessage());
                }

                // Decorator Pattern
                Receipt receipt = new BasicReceipt(newDonation);

                receipt = new ThankYouDecorator(receipt, name);

                if (view.isTaxInfoSelected()) {
                    receipt = new TaxInfoDecorator(receipt);
                }

                // Update the donation table
                view.addDonationToTable(
                        newDonation.getDonor().getId(),
                        newDonation.getDonationId(),
                        name,
                        amount,
                        type,
                        method
                );

                view.showReceipt(receipt.generateReceipt());
                view.clearFields();

                JOptionPane.showMessageDialog(view, "Donation registered and saved successfully!");

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage() == null ? "An unexpected error occurred." : ex.getMessage(),
                        "Donation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Load a donation when a table row is selected
        view.getTable().getSelectionModel().addListSelectionListener(e -> {

            if (e.getValueIsAdjusting()) {
                return;
            }

            int donationId = view.getSelectedDonationId();

            if (donationId == -1) {
                return;
            }

            try {

                Donation donation =
                        donationRepository.findDonationById(donationId);

                if (donation != null) {
                    view.loadDonation(donation);
                    view.setEditingMode(true);
                }

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage() == null ? "Could not load donation." : ex.getMessage(),
                        "Donation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Update the selected donation
        view.getUpdateButton().addActionListener(e -> {

            try {

                int donationId = view.getSelectedDonationId();

                if (donationId == -1) {
                    throw new IllegalArgumentException("Please select a donation first.");
                }

                Donation original = donationRepository.findDonationById(donationId);

                if (original == null) {
                    throw new IllegalArgumentException("Donation could not be found.");
                }

                // Preserve the original donor
                Donation updatedDonation = createDonationFromView(view, donationId, original.getDonor());

                boolean updated = donationRepository.updateDonation(updatedDonation);

                if (!updated) {
                    throw new IllegalStateException("Could not update donation.");
                }

                refreshDonationTable(view);
                view.clearFields();

                JOptionPane.showMessageDialog(view, "Donation updated successfully!"
                );

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage() == null ? "An unexpected error occurred." : ex.getMessage(),
                        "Donation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Delete the selected donation
        view.getDeleteButton().addActionListener(e -> {

            try {

                int donationId = view.getSelectedDonationId();

                if (donationId == -1) {
                    throw new IllegalArgumentException("Please select a donation first.");
                }

                int confirmation = JOptionPane.showConfirmDialog(
                        view,
                        "Are you sure you want to delete this donation?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirmation != JOptionPane.YES_OPTION) {
                    return;
                }

                Donation donation = donationRepository.findDonationById(donationId);

                if (donation == null) {
                    throw new IllegalArgumentException("Donation could not be found.");
                }

                boolean deleted = donationRepository.deleteDonation(donationId);

                if (!deleted) {
                    throw new IllegalStateException("Could not delete donation.");
                }

                refreshDonationTable(view);
                view.clearFields();

                JOptionPane.showMessageDialog(view, "Donation deleted successfully!"
                );

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage() == null ? "An unexpected error occurred." : ex.getMessage(),
                        "Donation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        // Clear the form or cancel editing
        view.getClearButton().addActionListener(e -> {
            view.clearFields();
        });

        // Search donations
        view.getSearchButton().addActionListener(e -> {
            searchDonationTable(view);
        });

        // Refresh donors and donations
        view.getRefreshButton().addActionListener(e -> {

            view.loadDonors(getDonors());
            view.clearSearchField();

            refreshDonationTable(view);
            view.clearFields();
        });
    }

    private void searchDonationTable(DonationView view) {

        String search = view.getSearchText().toLowerCase(Locale.ROOT);

        view.clearTable();

        if (search.isBlank()) {
            refreshDonationTable(view);
            return;
        }

        for (Donation donation : getDonations()) {

            if (donation == null || donation.getDonor() == null) {
                continue;
            }

            Donor donor = donation.getDonor();

            String donorId = String.valueOf(donor.getId());
            String donationId = String.valueOf(donation.getDonationId());

            boolean matches =
                    donor.getName().toLowerCase(Locale.ROOT).contains(search)
                            || donor.getEmail().toLowerCase(Locale.ROOT).contains(search)
                            || donorId.contains(search)
                            || donationId.contains(search);

            if (matches) {
                view.addDonationToTable(
                        donor.getId(),
                        donation.getDonationId(),
                        donor.getName(),
                        donation.getAmount(),
                        donation.getDonationType(),
                        donation.getDonationMethod()
                );
            }
        }
    }

    private void refreshDonationTable(DonationView view) {

        view.clearTable();

        for (Donation donation : getDonations()) {

            if (donation == null || donation.getDonor() == null) {
                continue;
            }

            view.addDonationToTable(
                    donation.getDonor().getId(),
                    donation.getDonationId(),
                    donation.getDonor().getName(),
                    donation.getAmount(),
                    donation.getDonationType(),
                    donation.getDonationMethod()
            );
        }
    }

    private Donation createDonationFromView(DonationView view, int donationId, Donor donor) {

        String method = view.getDonationMethod();
        String type = view.getDonationType();

        if (method == null || type == null || type.isBlank()) {
            throw new IllegalArgumentException("Select a valid donation method and type.");
        }

        if ((method.equals("Online") || method.equals("Check")) && !type.equalsIgnoreCase("Money")) {

            throw new IllegalArgumentException("Monetary donations must use the Money type.");
        }

        if (method.equals("In-Kind") && type.equalsIgnoreCase("Money")) {
            throw new IllegalArgumentException("Select a physical donation type.");
        }

        double amount = validateAmount(view.getAmount(), method);

        DonationStrategy strategy = createStrategy(view, method);

        DonationProcessor processor = new DonationProcessor(strategy);

        if (!processor.processDonation(amount)) {
            throw new IllegalArgumentException("Donation could not be accepted.");
        }

        Donation donation = new Donation(donationId, donor, amount, type, method);

        switch (method) {

            case "Online":

                donation.setOnlineDetails(view.getPaymentEmail(), view.getPaymentReference());

                break;

            case "Check":

                donation.setCheckDetails(view.getBankName(), view.getCheckNumber(), view.getAccountHolder());

                break;

            case "In-Kind":

                donation.setInKindDetails(view.getItemName(), Integer.parseInt(view.getQuantity()));

                break;
        }

        donation.setTaxInfoSelected(view.isTaxInfoSelected());

        return donation;
    }
}
