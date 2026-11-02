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
import java.io.IOException;
import java.util.ArrayList;

public class DonationController {

    private final DonorRepository donorRepository;
    private final DonationRepository donationRepository;

    public DonationController() {
        donorRepository = new DonorRepository();
        donationRepository = new DonationRepository();
    }

    // Find an existing donor using their email address
    public Donor findDonorByEmail(String email) {
        return donorRepository.findDonorByEmail(email);
    }

    // Create a donor only if they do not already exist
    public Donor addDonor(String name, String email, String phone) {

        validateName(name);
        validateEmail(email);
        validatePhone(phone);

        Donor existingDonor = findDonorByEmail(email);

        if (existingDonor != null) {

            if (!existingDonor.getName().equalsIgnoreCase(name) || !existingDonor.getPhone().equals(phone)) {
                throw new IllegalArgumentException("A donor with this email already exists.\n" + "Please enter the same donor name and phone number.");
            }

            return existingDonor;
        }

        return donorRepository.addDonor(name, email, phone);
    }

    public Donation addDonation(Donor donor, double amount, String type, String method, String itemName, int quantity) {

        if (donor == null) {
            throw new IllegalArgumentException("Donor is required.");
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Invalid donation amount.");
        }

        if (amount == 0 && !method.equals("In-Kind")) {
            throw new IllegalArgumentException("Monetary donations must be greater than zero.");
        }

        return donationRepository.addDonation(donor, amount, type, method, itemName, quantity);
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

    private void validateName(String name) {

        if (name == null || name.length() < 2
                || name.length() > 100
                || !name.matches("[\\p{L}][\\p{L} .'-]*")) {

            throw new IllegalArgumentException(
                    "Enter a valid donor name."
            );
        }
    }

    private void validateEmail(String email) {

        if (email == null ||
                !email.matches(
                        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
                )) {

            throw new IllegalArgumentException(
                    "Please enter a valid email address."
            );
        }
    }

    private void validatePhone(String phone) {

        if (phone == null ||
                !phone.matches("^01[0125][0-9]{8}$")) {

            throw new IllegalArgumentException(
                    "Phone must be a valid Egyptian mobile number."
            );
        }
    }

    private double validateAmount(String input, String method) {

        double amount;

        try {
            amount = Double.parseDouble(input);

        } catch (NumberFormatException | NullPointerException ex) {

            throw new IllegalArgumentException(
                    "Amount must be a valid number."
            );
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                    "Amount cannot be negative or invalid."
            );
        }

        if (!method.equals("In-Kind") && amount == 0) {
            throw new IllegalArgumentException(
                    "Monetary donation must be greater than zero."
            );
        }

        return amount;
    }

    private DonationStrategy createStrategy(
            DonationView view, String method) {

        switch (method) {

            case "Online":

                validateEmail(view.getPaymentEmail());

                if (!view.getPaymentReference()
                        .matches("[A-Za-z0-9_-]{3,50}")) {

                    throw new IllegalArgumentException(
                            "Enter a valid payment reference."
                    );
                }

                return new OnlineDonation(
                        view.getPaymentEmail(),
                        view.getPaymentReference()
                );

            case "Check":

                if (view.getBankName().isBlank()
                        || view.getBankName().length() > 100) {

                    throw new IllegalArgumentException(
                            "Enter a valid bank name."
                    );
                }

                if (!view.getCheckNumber()
                        .matches("[A-Za-z0-9-]{3,30}")) {

                    throw new IllegalArgumentException(
                            "Enter a valid check number."
                    );
                }

                validateName(view.getAccountHolder());

                return new CheckDonation(
                        view.getBankName(),
                        view.getCheckNumber(),
                        view.getAccountHolder()
                );

            case "In-Kind":

                String itemName = view.getItemName();

                if (itemName.isBlank() || itemName.length() > 100) {
                    throw new IllegalArgumentException(
                            "Enter a valid item name."
                    );
                }

                int quantity;

                try {
                    quantity = Integer.parseInt(
                            view.getQuantity()
                    );

                } catch (NumberFormatException ex) {

                    throw new IllegalArgumentException(
                            "Quantity must be a whole number."
                    );
                }

                if (quantity <= 0) {
                    throw new IllegalArgumentException(
                            "Quantity must be greater than zero."
                    );
                }

                return new InKindDonation(itemName, quantity);

            default:

                throw new IllegalArgumentException(
                        "Invalid donation method."
                );
        }
    }

    public void connectView(DonationView view) {

        // Display saved donations from MongoDB
        for (Donation donation : getDonations()) {

            view.addDonationToTable(
                    donation.getDonor().getId(),
                    donation.getDonationId(),
                    donation.getDonor().getName(),
                    donation.getAmount(),
                    donation.getDonationType(),
                    donation.getDonationMethod()
            );
        }

        view.getSubmitButton().addActionListener(e -> {

            try {

                String name = view.getDonorName();
                String email = view.getDonorEmail();
                String phone = view.getDonorPhone();

                validateName(name);
                validateEmail(email);
                validatePhone(phone);

                String method = view.getDonationMethod();
                String type = view.getDonationType();

                if (method == null || type == null || type.isBlank()) {

                    throw new IllegalArgumentException("Select a valid donation method and type.");
                }

                if ((method.equals("Online") || method.equals("Check")) && !type.equalsIgnoreCase("Money")) {
                    throw new IllegalArgumentException(
                            "Monetary donations must use the Money type."
                    );
                }

                if (method.equals("In-Kind") && type.equalsIgnoreCase("Money")) {
                    throw new IllegalArgumentException("Select a physical donation type.");
                }

                double amount = validateAmount(
                        view.getAmount(), method
                );

                // Validate the selected donation strategy
                DonationStrategy strategy = createStrategy(view, method);

                // Check whether the donor already exists
                Donor donor = findDonorByEmail(email);

                if (donor != null) {

                    if (!donor.getName().equalsIgnoreCase(name)
                            || !donor.getPhone().equals(phone)) {

                        throw new IllegalArgumentException("This email belongs to an existing donor.\n" + "Please use their registered name and phone number.");
                    }
                }

                DonationProcessor processor = new DonationProcessor(strategy);

                if (!processor.processDonation(amount)) {
                    throw new IllegalArgumentException("Donation could not be accepted.");
                }

                // Reuse the existing donor or create a new one
                if (donor == null) {
                    donor = addDonor(name, email, phone);
                }

                // Prepare in-kind details
                String itemName = "";
                int quantity = 0;

                if (method.equals("In-Kind")) {
                    itemName = view.getItemName();
                    quantity = Integer.parseInt(view.getQuantity());
                }

                // Save the donation directly to MongoDB
                Donation newDonation = addDonation(donor,amount, type, method, itemName, quantity);

                // Observer Pattern
                EventManager manager = new EventManager("donation");

                manager.subscribe("donation", new EmailNotificationListener(email));

                manager.subscribe("donation", new LogNotificationListener("DonationLog")
                );

                String description = method.equals("In-Kind")
                        ? name + " donated " + quantity
                        + " x " + itemName + "."
                        : name + " donated " + amount + " EGP.";

                // A notification failure must not affect a saved donation
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

                // Update GUI
                view.addDonationToTable(newDonation.getDonor().getId(), newDonation.getDonationId(), name, amount, type, method);

                view.showReceipt(receipt.generateReceipt());
                view.clearFields();

                JOptionPane.showMessageDialog(view, "Donation registered and saved successfully!");

            } catch (RuntimeException ex) {

                JOptionPane.showMessageDialog(view, ex.getMessage() == null ? "An unexpected error occurred." : ex.getMessage(), "Donation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}