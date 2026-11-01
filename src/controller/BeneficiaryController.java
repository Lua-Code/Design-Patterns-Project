
package controller;

import model.Beneficiary;
import storage.AppData;
import storage.DataStorage;
import view.BeneficiaryView;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;

public class BeneficiaryController {

    private final AppData appData;
    private final ArrayList<Beneficiary> beneficiaries;

    private int nextBeneficiaryId = 401;

    public BeneficiaryController(AppData appData) {

        this.appData = appData;
        this.beneficiaries = appData.getBeneficiaries();

        // Continue IDs after saved records
        for (Beneficiary beneficiary : beneficiaries) {
            nextBeneficiaryId = Math.max(
                    nextBeneficiaryId,
                    beneficiary.getId() + 1
            );
        }
    }

    private void saveData() {
        try {
            DataStorage.save(appData);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not save beneficiary data: " + ex.getMessage(),
                    ex
            );
        }
    }

    public Beneficiary addBeneficiary(
            String name,
            String email,
            String phone,
            ArrayList<String> needs) {

        if (name == null || name.length() < 2
                || name.length() > 100
                || !name.matches("[\\p{L}][\\p{L} .'-]*")) {

            throw new IllegalArgumentException(
                    "Please enter a valid beneficiary name."
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

        if (needs == null || needs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select at least one beneficiary need."
            );
        }

        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException(
                        "A beneficiary with this email already exists."
                );
            }
        }

        Beneficiary beneficiary = new Beneficiary(
                nextBeneficiaryId++,
                name, email, phone, needs
        );

        beneficiaries.add(beneficiary);

        try {
            saveData();
        } catch (IllegalStateException ex) {
            beneficiaries.remove(beneficiary);
            nextBeneficiaryId--;
            throw ex;
        }

        return beneficiary;
    }

    public ArrayList<Beneficiary> getBeneficiaries() {
        return new ArrayList<>(beneficiaries);
    }

    public Beneficiary findBeneficiary(int id) {

        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary.getId() == id) {
                return beneficiary;
            }
        }

        return null;
    }

    public void updateAssistanceStatus(int id, String status) {

        Beneficiary beneficiary = findBeneficiary(id);

        if (beneficiary == null) {
            throw new IllegalArgumentException(
                    "Beneficiary not found."
            );
        }

        if (status == null ||
                (!status.equals("Pending")
                        && !status.equals("In Progress")
                        && !status.equals("Completed"))) {

            throw new IllegalArgumentException(
                    "Invalid assistance status."
            );
        }

        String previousStatus =
                beneficiary.getAssistanceStatus();

        beneficiary.updateAssistanceStatus(status);

        try {
            saveData();
        } catch (IllegalStateException ex) {
            beneficiary.updateAssistanceStatus(previousStatus);
            throw ex;
        }
    }

    public int getBeneficiaryCount() {
        return beneficiaries.size();
    }

    public int getPendingCount() {

        int count = 0;

        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary.getAssistanceStatus().equals("Pending")) {
                count++;
            }
        }

        return count;
    }

    public int getCompletedCount() {

        int count = 0;

        for (Beneficiary beneficiary : beneficiaries) {
            if (beneficiary.getAssistanceStatus().equals("Completed")) {
                count++;
            }
        }

        return count;
    }

    public void connectView(BeneficiaryView view) {

        // Restore saved beneficiaries
        for (Beneficiary beneficiary : beneficiaries) {

            view.addBeneficiaryToTable(
                    beneficiary.getId(),
                    beneficiary.getName(),
                    beneficiary.getEmail(),
                    beneficiary.getPhone(),
                    beneficiary.getNeeds(),
                    beneficiary.getAssistanceStatus()
            );
        }

        // Register beneficiary
        view.getRegisterButton().addActionListener(e -> {

            try {
                Beneficiary beneficiary = addBeneficiary(
                        view.getBeneficiaryName(),
                        view.getBeneficiaryEmail(),
                        view.getBeneficiaryPhone(),
                        view.getSelectedNeeds()
                );

                view.addBeneficiaryToTable(
                        beneficiary.getId(),
                        beneficiary.getName(),
                        beneficiary.getEmail(),
                        beneficiary.getPhone(),
                        beneficiary.getNeeds(),
                        beneficiary.getAssistanceStatus()
                );

                view.clearFields();

                JOptionPane.showMessageDialog(
                        view,
                        "Beneficiary registered and saved successfully!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });

        // Update assistance status
        view.getUpdateStatusButton().addActionListener(e -> {

            try {
                int id = view.getSelectedBeneficiaryId();

                if (id == -1) {
                    throw new IllegalArgumentException(
                            "Please select a beneficiary."
                    );
                }

                String status = view.getSelectedStatus();

                updateAssistanceStatus(id, status);

                view.updateBeneficiaryStatus(id, status);

                JOptionPane.showMessageDialog(
                        view,
                        "Assistance status updated and saved!"
                );

            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(view, ex.getMessage());
            }
        });
    }

    private void showError(BeneficiaryView view, String message) {
        JOptionPane.showMessageDialog(
                view,
                message,
                "Beneficiary Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
