package controller;

import model.Donor;
import model.Donation;
import view.DonationView;

import strategy.*;
import observer.*;
import decorator.*;

import storage.AppData;
import storage.DataStorage;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class DonationController {

    private final AppData appData;

    private final ArrayList<Donor> donors;
    private final ArrayList<Donation> donations;

    private int nextDonorId = 1;
    private int nextDonationId = 101;

    public DonationController(AppData appData) {

        this.appData = appData;

        donors = appData.getDonors();
        donations = appData.getDonations();

        // Continue IDs after previously saved records
        for (Donor donor : donors) {
            nextDonorId = Math.max(
                    nextDonorId,
                    donor.getId() + 1
            );
        }

        for (Donation donation : donations) {
            nextDonationId = Math.max(
                    nextDonationId,
                    donation.getDonationId() + 1
            );
        }
    }

    private void saveData() {

        try {
            DataStorage.save(appData);

        } catch (IOException ex) {

            throw new IllegalStateException(
                    "Could not save donation data: " + ex.getMessage(),
                    ex
            );
        }
    }

    // Find an existing donor using their email address
    public Donor findDonorByEmail(String email) {

        if (email == null) {
            return null;
        }

        for (Donor donor : donors) {

            if (donor.getEmail().equalsIgnoreCase(email.trim())) {
                return donor;
            }
        }

        return null;
    }

    // Create a donor only if they do not already exist
    public Donor addDonor(String name, String email, String phone) {

        validateName(name);
        validateEmail(email);
        validatePhone(phone);

        Donor existingDonor = findDonorByEmail(email);

        if (existingDonor != null) {

            if (!existingDonor.getName().equalsIgnoreCase(name)
                    || !existingDonor.getPhone().equals(phone)) {

                throw new IllegalArgumentException(
                        "A donor with this email already exists.\n"
                                + "Please enter the same donor name and phone number."
                );
            }

            return existingDonor;
        }

        Donor donor = new Donor(
                nextDonorId++, name, email, phone
        );

        donors.add(donor);

        return donor;
    }

    public Donation addDonation(
            Donor donor,
            double amount,
            String type,
            String method) {

        if (donor == null) {
            throw new IllegalArgumentException("Donor is required.");
        }

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException("Invalid donation amount.");
        }

        if (amount == 0 && !method.equals("In-Kind")) {
            throw new IllegalArgumentException(
                    "Monetary donations must be greater than zero."
            );
        }

        Donation donation = new Donation(
                nextDonationId++, donor, amount, type, method
        );

        donations.add(donation);

        return donation;
    }

    public ArrayList<Donor> getDonors() {
        return new ArrayList<>(donors);
    }

    public ArrayList<Donation> getDonations() {
        return new ArrayList<>(donations);
    }

    public double getTotalDonations() {

        double total = 0;

        for (Donation donation : donations) {
            total += donation.getAmount();
        }

        return total;
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

        // Display saved donations
        for (Donation donation : donations) {

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

            Donor donor = null;
            Donor newDonor = null;
            Donation newDonation = null;

            boolean savedSuccessfully = false;

            try {

                String name = view.getDonorName();
                String email = view.getDonorEmail();
                String phone = view.getDonorPhone();

                validateName(name);
                validateEmail(email);
                validatePhone(phone);

                String method = view.getDonationMethod();
                String type = view.getDonationType();

                if (method == null || type == null
                        || type.isBlank()) {

                    throw new IllegalArgumentException(
                            "Select a valid donation method and type."
                    );
                }

                if ((method.equals("Online") || method.equals("Check"))
                        && !type.equalsIgnoreCase("Money")) {

                    throw new IllegalArgumentException(
                            "Monetary donations must use the Money type."
                    );
                }

                if (method.equals("In-Kind")
                        && type.equalsIgnoreCase("Money")) {

                    throw new IllegalArgumentException(
                            "Select a physical donation type."
                    );
                }

                double amount = validateAmount(
                        view.getAmount(), method
                );

                // Validate the selected donation strategy
                DonationStrategy strategy =
                        createStrategy(view, method);

                // Check whether the donor already exists BEFORE
                // processing or creating a new donation
                donor = findDonorByEmail(email);

                if (donor != null) {

                    if (!donor.getName().equalsIgnoreCase(name)
                            || !donor.getPhone().equals(phone)) {

                        throw new IllegalArgumentException(
                                "This email belongs to an existing donor.\n"
                                        + "Please use their registered name and phone number."
                        );
                    }

                }

                DonationProcessor processor =
                        new DonationProcessor(strategy);

                if (!processor.processDonation(amount)) {

                    throw new IllegalArgumentException(
                            "Donation could not be accepted."
                    );
                }

                // Reuse the existing donor or create a new one
                if (donor == null) {

                    donor = addDonor(name, email, phone);
                    newDonor = donor;
                }

                newDonation = addDonation(
                        donor, amount, type, method
                );

                if (method.equals("In-Kind")) {

                    newDonation.setInKindDetails(
                            view.getItemName(),
                            Integer.parseInt(view.getQuantity())
                    );
                }

                // Save donation and donor records
                saveData();
                savedSuccessfully = true;

                // Observer Pattern
                EventManager manager = new EventManager("donation");

                manager.subscribe(
                        "donation",
                        new EmailNotificationListener(email)
                );

                manager.subscribe(
                        "donation",
                        new LogNotificationListener("DonationLog")
                );

                String description = method.equals("In-Kind")
                        ? name + " donated " + view.getQuantity()
                        + " x " + view.getItemName() + "."
                        : name + " donated " + amount + " EGP.";

                // A notification failure must not delete
                // a donation that has already been saved
                try {
                    manager.notify("donation", description);

                } catch (RuntimeException notificationError) {
                    System.err.println(
                            "Donation saved, but notification failed: "
                                    + notificationError.getMessage()
                    );
                }

                // Decorator Pattern
                Receipt receipt = new BasicReceipt(newDonation);

                receipt = new ThankYouDecorator(receipt, name);

                // Add the tax decorator when the checkbox is selected
                if (view.isTaxInfoSelected()) {
                    receipt = new TaxInfoDecorator(receipt);
                }

                // Update GUI
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

                JOptionPane.showMessageDialog(
                        view,
                        "Donation registered and saved successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {

                // Roll back only when the donation was NOT saved
                if (!savedSuccessfully) {

                    if (newDonation != null) {
                        donations.remove(newDonation);
                        nextDonationId--;
                    }

                    // Never delete an existing donor
                    if (newDonor != null) {
                        donors.remove(newDonor);
                        nextDonorId--;
                    }
                }

                JOptionPane.showMessageDialog(
                        view,
                        ex.getMessage(),
                        "Donation Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}