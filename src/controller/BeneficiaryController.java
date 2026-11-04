package controller;

import com.mongodb.MongoWriteException;
import model.Beneficiary;
import repository.BeneficiaryRepository;
import repository.LookupRepository;
import view.BeneficiaryView;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Locale;

public class BeneficiaryController {

    private final BeneficiaryRepository beneficiaryRepository;
    private final LookupRepository lookupRepository;

    public BeneficiaryController()
    {
        this.beneficiaryRepository = new BeneficiaryRepository();
        this.lookupRepository = new LookupRepository();
    }

    public Beneficiary addBeneficiary(String name, String email, String phone, ArrayList<String> needs)
    {
        validateBeneficiary(name, email, phone, needs);

        if (beneficiaryRepository.findBeneficiaryByEmail(email) != null)
        {
            throw new IllegalArgumentException("A beneficiary with this email already exists.");
        }

        try
        {
            return beneficiaryRepository.addBeneficiary(name, email, phone, needs);
        }
        catch (MongoWriteException ex)
        {
            if (ex.getError().getCode() == 11000)
            {
                throw new IllegalArgumentException("A beneficiary with this email already exists.");
            }

            throw ex;
        }
    }

    public void updateBeneficiary(int id, String name, String email, String phone, ArrayList<String> needs)
    {
        Beneficiary existing = beneficiaryRepository.findBeneficiaryById(id);

        if (existing == null)
        {
            throw new IllegalArgumentException("Beneficiary not found.");
        }

        validateBeneficiary(name, email, phone, needs);

        Beneficiary emailOwner = beneficiaryRepository.findBeneficiaryByEmail(email);

        if (emailOwner != null && emailOwner.getId() != id)
        {
            throw new IllegalArgumentException("Another beneficiary already uses this email.");
        }

        try
        {
            boolean updated = beneficiaryRepository.updateBeneficiary(id, name, email, phone, needs);

            if (!updated)
            {
                throw new IllegalArgumentException("Beneficiary not found.");
            }
        }
        catch (MongoWriteException ex)
        {
            if (ex.getError().getCode() == 11000)
            {
                throw new IllegalArgumentException("Another beneficiary already uses this email.");
            }

            throw ex;
        }
    }

    private void validateBeneficiary(String name, String email, String phone, ArrayList<String> needs)
    {
        if (name == null || name.length() < 2 || name.length() > 100 || !name.matches("\\p{L}[\\p{L} .'-]*"))
        {
            throw new IllegalArgumentException("Please enter a valid beneficiary name.");
        }

        if (email == null || !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Please enter a valid email.");
        }

        if (phone == null || !phone.matches("^01[0125][0-9]{8}$"))
        {
            throw new IllegalArgumentException("Please enter a valid Egyptian mobile number.");
        }

        if (needs == null || needs.isEmpty())
        {
            throw new IllegalArgumentException("Please select at least one beneficiary need.");
        }
    }

    public ArrayList<Beneficiary> getBeneficiaries()
    {
        return beneficiaryRepository.getAllBeneficiaries();
    }

    public Beneficiary findBeneficiary(int id)
    {
        return beneficiaryRepository.findBeneficiaryById(id);
    }

    public void updateAssistanceStatus(int id, String status)
    {
        Beneficiary beneficiary = findBeneficiary(id);

        if (beneficiary == null)
        {
            throw new IllegalArgumentException("Beneficiary not found.");
        }

        if (status == null || (!status.equals("Pending") && !status.equals("In Progress") && !status.equals("Completed")))
        {
            throw new IllegalArgumentException("Invalid assistance status.");
        }

        boolean updated = beneficiaryRepository.updateAssistanceStatus(id, status);

        if (!updated)
        {
            throw new IllegalArgumentException("Beneficiary not found.");
        }
    }

    public void deleteBeneficiary(int id)
    {
        if (findBeneficiary(id) == null)
        {
            throw new IllegalArgumentException("Beneficiary not found.");
        }

        if (!beneficiaryRepository.deleteBeneficiary(id))
        {
            throw new IllegalArgumentException("Could not delete beneficiary.");
        }
    }

    public int getBeneficiaryCount()
    {
        return Math.toIntExact(beneficiaryRepository.getBeneficiaryCount());
    }

    public int getPendingCount()
    {
        return Math.toIntExact(beneficiaryRepository.getPendingCount());
    }

    public int getCompletedCount()
    {
        return Math.toIntExact(beneficiaryRepository.getCompletedCount());
    }

    public void connectView(BeneficiaryView view)
    {
        view.loadNeeds(lookupRepository.getItemsByCategory("Beneficiary Needs"));

        refreshTable(view);

        view.getRegisterButton().addActionListener(_ ->
        {
            try
            {
                if (view.isEditing())
                {
                    int id = view.getSelectedBeneficiaryId();

                    if (id == -1)
                    {
                        throw new IllegalArgumentException("Please select a beneficiary to edit.");
                    }

                    updateBeneficiary(
                            id,
                            view.getBeneficiaryName(),
                            view.getBeneficiaryEmail(),
                            view.getBeneficiaryPhone(),
                            view.getSelectedNeeds()
                    );

                    refreshTable(view);
                    view.clearFields();
                    view.clearTableSelection();

                    JOptionPane.showMessageDialog(view, "Beneficiary details updated successfully!");
                }
                else
                {
                    addBeneficiary(
                            view.getBeneficiaryName(),
                            view.getBeneficiaryEmail(),
                            view.getBeneficiaryPhone(),
                            view.getSelectedNeeds()
                    );

                    refreshTable(view);
                    view.clearFields();
                    view.clearTableSelection();

                    JOptionPane.showMessageDialog(view, "Beneficiary registered successfully!");
                }
            }
            catch (IllegalArgumentException ex)
            {
                showError(view, ex.getMessage());
            }
            catch (RuntimeException ex)
            {
                showError(view, "Could not save beneficiary: " + ex.getMessage());
            }
        });

        view.getCancelButton().addActionListener(_ ->
        {
            view.clearFields();
            view.clearTableSelection();
        });

        view.getUpdateStatusButton().addActionListener(_ ->
        {
            try
            {
                int id = view.getSelectedBeneficiaryId();

                if (id == -1)
                {
                    throw new IllegalArgumentException("Please select a beneficiary.");
                }

                String status = view.getSelectedStatus();

                updateAssistanceStatus(id, status);
                view.updateBeneficiaryStatus(id, status);

                JOptionPane.showMessageDialog(view, "Assistance status updated successfully!");
            }
            catch (IllegalArgumentException ex)
            {
                showError(view, ex.getMessage());
            }
            catch (RuntimeException ex)
            {
                showError(view, "Could not update assistance status: " + ex.getMessage());
            }
        });

        view.getDeleteButton().addActionListener(_ ->
        {
            try
            {
                int id = view.getSelectedBeneficiaryId();

                if (id == -1)
                {
                    throw new IllegalArgumentException("Please select a beneficiary to delete.");
                }

                int choice = JOptionPane.showConfirmDialog(
                        view,
                        "Are you sure you want to delete this beneficiary?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (choice != JOptionPane.YES_OPTION)
                {
                    return;
                }

                deleteBeneficiary(id);

                refreshTable(view);
                view.clearFields();
                view.clearTableSelection();

                JOptionPane.showMessageDialog(view, "Beneficiary deleted successfully!");
            }
            catch (IllegalArgumentException ex)
            {
                showError(view, ex.getMessage());
            }
            catch (RuntimeException ex)
            {
                showError(view, "Could not delete beneficiary: " + ex.getMessage());
            }
        });

        view.getRefreshButton().addActionListener(_ ->
        {
            try
            {
                view.clearFields();
                view.clearTableSelection();
                refreshTable(view);
            }
            catch (RuntimeException ex)
            {
                showError(view, "Could not refresh beneficiaries: " + ex.getMessage());
            }
        });

        view.getSearchButton().addActionListener(_ ->
        {
            try
            {
                searchBeneficiaries(view);
            }
            catch (RuntimeException ex)
            {
                showError(view, "Could not search beneficiaries: " + ex.getMessage());
            }
        });

        view.getBeneficiaryTable().getSelectionModel().addListSelectionListener(e ->
        {
            if (e.getValueIsAdjusting())
            {
                return;
            }

            int id = view.getSelectedBeneficiaryId();

            if (id == -1)
            {
                return;
            }

            Beneficiary beneficiary = findBeneficiary(id);

            if (beneficiary != null)
            {
                view.populateForm(beneficiary);
            }
        });
    }

    private void refreshTable(BeneficiaryView view)
    {
        view.clearTable();

        for (Beneficiary beneficiary : getBeneficiaries())
        {
            view.addBeneficiaryToTable(
                    beneficiary.getId(),
                    beneficiary.getName(),
                    beneficiary.getEmail(),
                    beneficiary.getPhone(),
                    beneficiary.getNeeds(),
                    beneficiary.getAssistanceStatus()
            );
        }
    }

    private void searchBeneficiaries(BeneficiaryView view)
    {
        String query = view.getSearchText().toLowerCase(Locale.ROOT);

        view.clearTable();
        view.clearTableSelection();

        for (Beneficiary beneficiary : getBeneficiaries())
        {
            boolean matches =
                    String.valueOf(beneficiary.getId()).contains(query)
                            || beneficiary.getName().toLowerCase(Locale.ROOT).contains(query)
                            || beneficiary.getEmail().toLowerCase(Locale.ROOT).contains(query);

            if (matches)
            {
                view.addBeneficiaryToTable(
                        beneficiary.getId(),
                        beneficiary.getName(),
                        beneficiary.getEmail(),
                        beneficiary.getPhone(),
                        beneficiary.getNeeds(),
                        beneficiary.getAssistanceStatus()
                );
            }
        }
    }

    private void showError(BeneficiaryView view, String message)
    {
        JOptionPane.showMessageDialog(view, message, "Beneficiary Error", JOptionPane.ERROR_MESSAGE);
    }
}
