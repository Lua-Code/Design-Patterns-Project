package view;

import model.LookupItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class DonationView extends JPanel {

    private JTextField nameField, emailField, phoneField, amountField;
    private JTextField paymentEmailField, paymentReferenceField;
    private JTextField bankNameField, checkNumberField, accountHolderField;
    private JTextField itemNameField, quantityField;

    private JComboBox<String> methodBox, typeBox;
    private JCheckBox taxInfoCheckBox;

    private JPanel methodDetailsPanel;
    private CardLayout methodCardLayout;

    private JButton submitButton;
    private DefaultTableModel tableModel;

    private final ArrayList<String> donationTypes = new ArrayList<>();

    private final Color PRIMARY = new Color(24, 59, 86);
    private final Color TEAL = new Color(24, 169, 153);
    private final Color BACKGROUND = new Color(244, 247, 251);

    public DonationView() {

        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Donation Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 23));
        title.setForeground(PRIMARY);

        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        nameField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();
        amountField = createTextField();

        methodBox = new JComboBox<>(new String[]{
                "Online", "Check", "In-Kind"
        });

        typeBox = new JComboBox<>();

        methodBox.setPreferredSize(new Dimension(220, 28));
        typeBox.setPreferredSize(new Dimension(220, 28));

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

        taxInfoCheckBox = new JCheckBox(
                "Include tax information on receipt"
        );

        taxInfoCheckBox.setBackground(Color.WHITE);
        taxInfoCheckBox.setSelected(false);

        int row = 0;

        addFormRow(formPanel, "Donor Name:", nameField, row++);
        addFormRow(formPanel, "Donor Email:", emailField, row++);
        addFormRow(formPanel, "Phone Number:", phoneField, row++);
        addFormRow(formPanel, "Donation Method:", methodBox, row++);
        addFormRow(formPanel, "Donation Type:", typeBox, row++);

        addFormRow(
                formPanel,
                "Amount / Estimated Value:",
                amountField,
                row++
        );

        addFormRow(
                formPanel,
                "Method Details:",
                methodDetailsPanel,
                row++
        );

        addFormRow(
                formPanel,
                "Receipt Options:",
                taxInfoCheckBox,
                row++
        );

        methodBox.addActionListener(e -> updateDonationMethod());

        submitButton = new JButton("Submit Donation");

        submitButton.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        submitButton.setBackground(TEAL);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFocusPainted(false);

        submitButton.setPreferredSize(new Dimension(170, 33));

        JPanel buttonPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(submitButton);

        JScrollPane formScroll = new JScrollPane(formPanel);

        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(formScroll, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Donor ID is now a separate column
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
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);

        table.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        table.setRowHeight(28);

        table.setSelectionBackground(
                new Color(207, 239, 233)
        );

        table.setSelectionForeground(PRIMARY);

        table.setGridColor(
                new Color(230, 235, 240)
        );

        table.setShowVerticalLines(false);

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScroll = new JScrollPane(table);

        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Donation Records")
        );

        tableScroll.setPreferredSize(
                new Dimension(450, 155)
        );

        tableScroll.getViewport().setBackground(Color.WHITE);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topPanel,
                tableScroll
        );

        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(true);

        topPanel.setMinimumSize(new Dimension(0, 170));
        tableScroll.setMinimumSize(new Dimension(0, 90));

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(
                () -> splitPane.setDividerLocation(0.72)
        );
    }

    private JTextField createTextField() {

        JTextField field = new JTextField(18);

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        field.setPreferredSize(new Dimension(220, 28));

        return field;
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
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;

        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        fieldLabel.setForeground(PRIMARY);

        panel.add(fieldLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(component, gbc);
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

        String previousType =
                (String) typeBox.getSelectedItem();

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

    public String getDonorName() {
        return nameField.getText().trim();
    }

    public String getDonorEmail() {
        return emailField.getText().trim();
    }

    public String getDonorPhone() {
        return phoneField.getText().trim();
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

    public JButton getSubmitButton() {
        return submitButton;
    }

    // Updated: accepts BOTH donor ID and donation ID
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

    public void clearFields() {

        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
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
    }

    public void showReceipt(String receipt) {

        JTextArea area = new JTextArea(receipt);

        area.setEditable(false);
        area.setFont(
                new Font("Monospaced", Font.PLAIN, 13)
        );

        JOptionPane.showMessageDialog(
                this,
                new JScrollPane(area),
                "Donation Receipt",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}