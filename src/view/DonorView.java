package view;

import model.Donor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DonorView extends JPanel {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField searchField;

    private JButton registerButton;
    private JButton cancelButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton searchButton;

    private JTable donorTable;
    private DefaultTableModel tableModel;

    private boolean editingMode = false;

    private final Color PRIMARY = new Color(36, 70, 91);

    public DonorView() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Donor Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        nameField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();

        int row = 0;

        addFormRow(formPanel, "Donor Name:", nameField, row++);
        addFormRow(formPanel, "Email:", emailField, row++);
        addFormRow(formPanel, "Phone Number:", phoneField, row++);

        registerButton = new JButton("Register Donor");
        cancelButton = new JButton("Cancel Edit");

        styleButton(registerButton);
        styleButton(cancelButton);

        cancelButton.setEnabled(false);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        formButtons.setBackground(Color.WHITE);
        formButtons.add(cancelButton);
        formButtons.add(registerButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 5, 5);

        formPanel.add(formButtons, gbc);

        String[] columns = {
                "ID", "Name", "Email", "Phone"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        donorTable = new JTable(tableModel);
        donorTable.setRowHeight(26);
        donorTable.getTableHeader().setReorderingAllowed(false);
        donorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(donorTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Registered Donors")
        );

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);

        searchField = createTextField();
        searchField.setPreferredSize(new Dimension(220, 28));

        searchButton = new JButton("Search");
        refreshButton = new JButton("Refresh");

        styleButton(searchButton);
        styleButton(refreshButton);

        searchPanel.add(new JLabel("Search by ID, name, or email:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        deleteButton = new JButton("Delete Donor");
        styleButton(deleteButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.add(deleteButton);

        JPanel recordsPanel = new JPanel(new BorderLayout(5, 5));
        recordsPanel.setBackground(Color.WHITE);
        recordsPanel.add(searchPanel, BorderLayout.NORTH);
        recordsPanel.add(tableScroll, BorderLayout.CENTER);
        recordsPanel.add(actionPanel, BorderLayout.SOUTH);

        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                formScroll,
                recordsPanel
        );

        splitPane.setResizeWeight(0.35);
        splitPane.setDividerSize(8);

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(
                () -> splitPane.setDividerLocation(0.35)
        );
    }

    private JTextField createTextField() {

        JTextField field = new JTextField(20);
        field.setPreferredSize(new Dimension(250, 28));
        return field;
    }

    private void styleButton(JButton button) {

        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 32));
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

    public String getDonorName() {
        return nameField.getText().trim();
    }

    public String getDonorEmail() {
        return emailField.getText().trim();
    }

    public String getDonorPhone() {
        return phoneField.getText().trim();
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public boolean isEditing() {
        return editingMode;
    }

    public void setEditingMode(boolean editing) {

        editingMode = editing;

        registerButton.setText(
                editing ? "Save Changes" : "Register Donor"
        );

        cancelButton.setEnabled(editing);
    }

    public void populateForm(Donor donor) {

        nameField.setText(donor.getName());
        emailField.setText(donor.getEmail());
        phoneField.setText(donor.getPhone());

        setEditingMode(true);
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JTable getDonorTable() {
        return donorTable;
    }

    public int getSelectedDonorId() {

        int row = donorTable.getSelectedRow();

        if (row == -1) {
            return -1;
        }

        int modelRow = donorTable.convertRowIndexToModel(row);

        return (int) tableModel.getValueAt(modelRow, 0);
    }

    public void addDonorToTable(
            int id,
            String name,
            String email,
            String phone) {

        tableModel.addRow(new Object[]{
                id,
                name,
                email,
                phone
        });
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void clearFields() {

        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        searchField.setText("");

        setEditingMode(false);
        clearTableSelection();
    }

    public void clearTableSelection() {
        donorTable.clearSelection();
    }
}