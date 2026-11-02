package controller;

import com.mongodb.MongoWriteException;
import model.Beneficiary;
import repository.BeneficiaryRepository;
import view.BeneficiaryView;

import javax.swing.*;
import java.util.ArrayList;

public class BeneficiaryController {

    private final BeneficiaryRepository beneficiaryRepository;

    public BeneficiaryController() {
        this.beneficiaryRepository = new BeneficiaryRepository();
    }

    public Beneficiary addBeneficiary(String name, String email, String phone, ArrayList<String> needs) {
        if (name == null || name.length() < 2 || name.length() > 100 || !name.matches("\\p{L}[\\p{L} .'-]*")) {

            throw new IllegalArgumentException("Please enter a valid beneficiary name.");
        }

        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Please enter a valid email.");
        }

        if (phone == null || !phone.matches("^01[0125][0-9]{8}$")) {
            throw new IllegalArgumentException("Please enter a valid Egyptian mobile number.");
        }

        if (needs == null || needs.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one beneficiary need.");
        }

        if (beneficiaryRepository.findBeneficiaryByEmail(email) != null) {
            throw new IllegalArgumentException("A beneficiary with this email already exists.");
        }

        try {
            return beneficiaryRepository.addBeneficiary(name, email, phone, needs);
        } catch (MongoWriteException ex) {
            if (ex.getError().getCode() == 11000) {
                throw new IllegalArgumentException("A beneficiary with this email already exists.");
            }

            throw ex;
        }
    }

    public ArrayList<Beneficiary> getBeneficiaries() {
        return beneficiaryRepository.getAllBeneficiaries();
    }

    public Beneficiary findBeneficiary(int id) {
        return beneficiaryRepository.findBeneficiaryById(id);
    }

    public void updateAssistanceStatus(int id, String status) {

        Beneficiary beneficiary = findBeneficiary(id);

        if (beneficiary == null) {
            throw new IllegalArgumentException("Beneficiary not found.");
        }

        if (status == null || (!status.equals("Pending") && !status.equals("In Progress") && !status.equals("Completed"))) {
            throw new IllegalArgumentException("Invalid assistance status.");
        }

        boolean updated = beneficiaryRepository.updateAssistanceStatus(id, status);

        if (!updated) {
            throw new IllegalArgumentException("Beneficiary not found.");
        }
    }

    public int getBeneficiaryCount() {
        return Math.toIntExact(beneficiaryRepository.getBeneficiaryCount());
    }

    public int getPendingCount() {
        return Math.toIntExact(beneficiaryRepository.getPendingCount());
    }

    public int getCompletedCount() {
        return Math.toIntExact(beneficiaryRepository.getCompletedCount());
    }

    public void connectView(BeneficiaryView view) {
        for (Beneficiary beneficiary : getBeneficiaries()) {

            view.addBeneficiaryToTable(
                    beneficiary.getId(),
                    beneficiary.getName(),
                    beneficiary.getEmail(),
                    beneficiary.getPhone(),
                    beneficiary.getNeeds(),
                    beneficiary.getAssistanceStatus()
            );
        }

        view.getRegisterButton().addActionListener(_ -> {

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

                JOptionPane.showMessageDialog(view, "Beneficiary registered and saved successfully!");

            } catch (IllegalArgumentException ex) {
                showError(view, ex.getMessage());
            } catch (RuntimeException ex) {
                showError(view, "Could not register beneficiary: " + ex.getMessage());
            }
        });

        view.getUpdateStatusButton().addActionListener(_ -> {

            try {
                int id = view.getSelectedBeneficiaryId();

                if (id == -1) {
                    throw new IllegalArgumentException("Please select a beneficiary.");
                }

                String status = view.getSelectedStatus();

                updateAssistanceStatus(id, status);

                view.updateBeneficiaryStatus(id, status);

                JOptionPane.showMessageDialog(view, "Assistance status updated and saved!");

            } catch (IllegalArgumentException ex) {
                showError(view, ex.getMessage());
            } catch (RuntimeException ex) {
                showError(view, "Could not update assistance status: " + ex.getMessage());
            }
        });
    }

    private void showError(BeneficiaryView view, String message) {
        JOptionPane.showMessageDialog(view, message, "Beneficiary Error", JOptionPane.ERROR_MESSAGE);
    }
}
