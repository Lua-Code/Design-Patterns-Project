package view;

import model.Donation;
import model.Donor;
import model.LookupItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DonationView extends JPanel {

    private JComboBox<Donor> donorBox;
    private JLabel donorDetailsLabel;

    private JTextField amountField;
    private JTextField paymentEmailField, paymentReferenceField;
    private JTextField bankNameField, checkNumberField, accountHolderField;
    private JTextField itemNameField, quantityField;
    private JTextField searchField;

    private JComboBox<String> methodBox, typeBox;
    private JCheckBox taxInfoCheckBox;

    private JPanel methodDetailsPanel;
    private CardLayout methodCardLayout;

    private JButton submitButton, updateButton;
    private JButton deleteButton, clearButton;
    private JButton searchButton, refreshButton;

    private JTable table;
    private DefaultTableModel tableModel;

    private final ArrayList<String> donationTypes = new ArrayList<>();

    private boolean editingMode = false;

    private final Color PRIMARY = new Color(36, 70, 91);
    private final Color TEAL = new Color(24, 169, 153);
    private final Color DANGER = new Color(190, 65, 65);
    private final Color SECONDARY = new Color(110, 120, 130);

    public DonationView() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Donation Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        donorBox = new JComboBox<>();
        donorBox.setPreferredSize(new Dimension(250, 28));

        donorDetailsLabel = new JLabel("No donor selected");
        donorDetailsLabel.setForeground(SECONDARY);

        donorBox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus
                );

                if (value instanceof Donor donor) {
                    setText(donor.getName() + " - " + donor.getEmail());
                }

                return this;
            }
        });

        amountField = createTextField();

        methodBox = new JComboBox<>(new String[]{
                "Online", "Check", "In-Kind"
        });

        typeBox = new JComboBox<>();

        methodBox.setPreferredSize(new Dimension(250, 28));
        typeBox.setPreferredSize(new Dimension(250, 28));

        paymentEmailField = createTextField();
        paymentReferenceField = createTextField();

        bankNameField = createTextField();
        checkNumberField = createTextField();
        accountHolderField = createTextField();

        itemNameField = createTextField();
        quantityField = createTextField();

        methodCardLayout = new CardLayout();

        methodDetailsPanel = new JPanel(methodCardLayout);
        methodDetailsPanel.setBackground(Color.WHITE);

        JPanel onlinePanel = createDetailsPanel();

        addFormRow(onlinePanel, "Payment Email:", paymentEmailField, 0);
        addFormRow(onlinePanel, "Payment Reference:", paymentReferenceField, 1);

        JPanel checkPanel = createDetailsPanel();

        addFormRow(checkPanel, "Bank Name:", bankNameField, 0);
        addFormRow(checkPanel, "Check Number:", checkNumberField, 1);
        addFormRow(checkPanel, "Account Holder:", accountHolderField, 2);

        JPanel inKindPanel = createDetailsPanel();

        addFormRow(inKindPanel, "Item Name:", itemNameField, 0);
        addFormRow(inKindPanel, "Quantity:", quantityField, 1);

        methodDetailsPanel.add(onlinePanel, "Online");
        methodDetailsPanel.add(checkPanel, "Check");
        methodDetailsPanel.add(inKindPanel, "In-Kind");

        taxInfoCheckBox = new JCheckBox("Include tax information on receipt");
        taxInfoCheckBox.setBackground(Color.WHITE);

        int row = 0;

        addFormRow(formPanel, "Select Donor:", donorBox, row++);
        addFormRow(formPanel, "Donor Details:", donorDetailsLabel, row++);
        addFormRow(formPanel, "Donation Method:", methodBox, row++);
        addFormRow(formPanel, "Donation Type:", typeBox, row++);
        addFormRow(formPanel, "Amount / Estimated Value:", amountField, row++);
        addFormRow(formPanel, "Method Details:", methodDetailsPanel, row++);
        addFormRow(formPanel, "Receipt Options:", taxInfoCheckBox, row++);

        donorBox.addActionListener(e -> updateDonorDetails());
        methodBox.addActionListener(e -> updateDonationMethod());

        submitButton = createButton("Submit Donation", PRIMARY);
        updateButton = createButton("Save Changes", TEAL);
        clearButton = createButton("Clear Form", SECONDARY);

        updateButton.setVisible(false);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        formButtons.setBackground(Color.WHITE);

        formButtons.add(clearButton);
        formButtons.add(updateButton);
        formButtons.add(submitButton);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 5, 5);

        formPanel.add(formButtons, gbc);

        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        String[] columns = {
                "Donor ID",
                "Donation ID",
                "Donor",
                "Amount / Value",
                "Type",
                "Method"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFont(new Font("Arial", Font.PLAIN, 13));
        table.setRowHeight(26);
        table.setSelectionBackground(new Color(207, 239, 233));
        table.setSelectionForeground(PRIMARY);
        table.setGridColor(new Color(230, 235, 240));
        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Donation Records"));
        tableScroll.getViewport().setBackground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        searchField = createTextField();
        searchField.setPreferredSize(new Dimension(220, 28));

        searchButton = createButton("Search", PRIMARY);
        refreshButton = createButton("Refresh", PRIMARY);

        searchPanel.add(new JLabel("Search by donor, email, or ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        deleteButton = createButton("Delete Selected", DANGER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(Color.WHITE);

        actionPanel.add(deleteButton);

        JPanel recordsPanel = new JPanel(new BorderLayout(5, 5));
        recordsPanel.setBackground(Color.WHITE);

        recordsPanel.add(searchPanel, BorderLayout.NORTH);
        recordsPanel.add(tableScroll, BorderLayout.CENTER);
        recordsPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                formScroll,
                recordsPanel
        );

        splitPane.setResizeWeight(0.45);
        splitPane.setDividerSize(8);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.45));
    }

    private JTextField createTextField() {

        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(250, 28));

        return field;
    }

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 32));

        return button;
    }

    private JPanel createDetailsPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        return panel;
    }

    private void addFormRow(
            JPanel panel,
            String label,
            JComponent component,
            int row) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = row;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(component, gbc);
    }

    public void loadDonors(ArrayList<Donor> donors) {

        int selectedId = getSelectedDonorId();

        donorBox.removeAllItems();

        for (Donor donor : donors) {
            donorBox.addItem(donor);
        }

        selectDonorById(selectedId);
        updateDonorDetails();
    }

    public void selectDonorById(int donorId) {

        for (int i = 0; i < donorBox.getItemCount(); i++) {

            Donor donor = donorBox.getItemAt(i);

            if (donor.getId() == donorId) {
                donorBox.setSelectedIndex(i);
                return;
            }
        }
    }

    private void updateDonorDetails() {

        Donor donor = getSelectedDonor();

        if (donor == null) {
            donorDetailsLabel.setText("No donor selected");
            return;
        }

        donorDetailsLabel.setText(
                "ID: " + donor.getId() +
                        " | Phone: " + donor.getPhone()
        );
    }

    public void loadDonationTypes(ArrayList<LookupItem> items) {

        donationTypes.clear();

        for (LookupItem item : items) {
            donationTypes.add(item.getValue());
        }

        updateDonationMethod();
    }

    private void updateDonationMethod() {

        String method = getDonationMethod();

        if (method == null) {
            return;
        }

        methodCardLayout.show(methodDetailsPanel, method);

        String previousType = (String) typeBox.getSelectedItem();

        typeBox.removeAllItems();

        for (String type : donationTypes) {

            if (method.equals("In-Kind")) {

                if (!type.equalsIgnoreCase("Money")) {
                    typeBox.addItem(type);
                }

            } else {

                if (type.equalsIgnoreCase("Money")) {
                    typeBox.addItem(type);
                }
            }
        }

        if (previousType != null) {

            for (int i = 0; i < typeBox.getItemCount(); i++) {

                if (previousType.equals(typeBox.getItemAt(i))) {
                    typeBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        methodDetailsPanel.revalidate();
        methodDetailsPanel.repaint();
    }

    public Donor getSelectedDonor() {
        return (Donor) donorBox.getSelectedItem();
    }

    public int getSelectedDonorId() {

        Donor donor = getSelectedDonor();

        return donor == null ? -1 : donor.getId();
    }

    public String getDonationMethod() {
        return (String) methodBox.getSelectedItem();
    }

    public String getDonationType() {
        return (String) typeBox.getSelectedItem();
    }

    public String getAmount() {
        return amountField.getText().trim();
    }

    public String getPaymentEmail() {
        return paymentEmailField.getText().trim();
    }

    public String getPaymentReference() {
        return paymentReferenceField.getText().trim();
    }

    public String getBankName() {
        return bankNameField.getText().trim();
    }

    public String getCheckNumber() {
        return checkNumberField.getText().trim();
    }

    public String getAccountHolder() {
        return accountHolderField.getText().trim();
    }

    public String getItemName() {
        return itemNameField.getText().trim();
    }

    public String getQuantity() {
        return quantityField.getText().trim();
    }

    public boolean isTaxInfoSelected() {
        return taxInfoCheckBox.isSelected();
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public void clearSearchField() {
        searchField.setText("");
    }

    public JButton getSubmitButton() {
        return submitButton;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getClearButton() {
        return clearButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JTable getTable() {
        return table;
    }

    public int getSelectedDonationId() {

        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);

        return (int) tableModel.getValueAt(modelRow, 1);
    }

    public void addDonationToTable(
            int donorId,
            int donationId,
            String donor,
            double amount,
            String type,
            String method) {

        tableModel.addRow(new Object[]{
                donorId,
                donationId,
                donor,
                amount,
                type,
                method
        });
    }

    public void updateDonationInTable(
            int donorId,
            int donationId,
            String donor,
            double amount,
            String type,
            String method) {

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            int currentId = (int) tableModel.getValueAt(i, 1);

            if (currentId == donationId) {

                tableModel.setValueAt(donorId, i, 0);
                tableModel.setValueAt(donationId, i, 1);
                tableModel.setValueAt(donor, i, 2);
                tableModel.setValueAt(amount, i, 3);
                tableModel.setValueAt(type, i, 4);
                tableModel.setValueAt(method, i, 5);

                return;
            }
        }
    }

    public void removeDonationFromTable(int donationId) {

        for (int i = 0; i < tableModel.getRowCount(); i++) {

            int currentId = (int) tableModel.getValueAt(i, 1);

            if (currentId == donationId) {
                tableModel.removeRow(i);
                return;
            }
        }
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void loadDonation(Donation donation) {

        if (donation == null) {
            return;
        }

        selectDonorById(donation.getDonor().getId());

        amountField.setText(String.valueOf(donation.getAmount()));

        methodBox.setSelectedItem(donation.getDonationMethod());
        updateDonationMethod();

        typeBox.setSelectedItem(donation.getDonationType());

        paymentEmailField.setText(donation.getPaymentEmail());
        paymentReferenceField.setText(donation.getPaymentReference());

        bankNameField.setText(donation.getBankName());
        checkNumberField.setText(donation.getCheckNumber());
        accountHolderField.setText(donation.getAccountHolder());

        itemNameField.setText(donation.getItemName());
        quantityField.setText(String.valueOf(donation.getQuantity()));

        taxInfoCheckBox.setSelected(donation.isTaxInfoSelected());

        setEditingMode(true);
    }

    public void setEditingMode(boolean editing) {

        editingMode = editing;

        submitButton.setVisible(!editing);
        updateButton.setVisible(editing);

        donorBox.setEnabled(!editing);
        deleteButton.setEnabled(true);

        clearButton.setText(editing ? "Cancel Edit" : "Clear Form");
    }

    public boolean isEditing() {
        return editingMode;
    }

    public void clearFields() {

        amountField.setText("");

        paymentEmailField.setText("");
        paymentReferenceField.setText("");

        bankNameField.setText("");
        checkNumberField.setText("");
        accountHolderField.setText("");

        itemNameField.setText("");
        quantityField.setText("");

        taxInfoCheckBox.setSelected(false);

        methodBox.setSelectedIndex(0);
        updateDonationMethod();

        searchField.setText("");

        table.clearSelection();

        setEditingMode(false);
    }

    public void showReceipt(String receipt) {

        JTextArea area = new JTextArea(receipt);

        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Donation Receipt",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
