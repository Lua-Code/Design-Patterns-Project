package view;

import model.LookupItem;
import model.Beneficiary;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class BeneficiaryView extends JPanel {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField searchField;

    private JPanel needsPanel;
    private final ArrayList<JCheckBox> needCheckboxes = new ArrayList<>();

    private JComboBox<String> statusBox;

    private JButton registerButton;
    private JButton cancelButton;
    private JButton updateStatusButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton searchButton;

    private JTable beneficiaryTable;
    private DefaultTableModel tableModel;

    private boolean editingMode = false;

    private final Color PRIMARY = new Color(36, 70, 91);

    public BeneficiaryView() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Beneficiary Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        nameField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();

        needsPanel = new JPanel(new GridLayout(0, 2, 10, 5));
        needsPanel.setBackground(Color.WHITE);

        int row = 0;

        addFormRow(formPanel, "Beneficiary Name:", nameField, row++);
        addFormRow(formPanel, "Email:", emailField, row++);
        addFormRow(formPanel, "Phone Number:", phoneField, row++);

        JScrollPane needsScroll = new JScrollPane(needsPanel);
        needsScroll.setPreferredSize(new Dimension(350, 100));
        needsScroll.setBorder(BorderFactory.createEmptyBorder());

        addFormRow(formPanel, "Needs:", needsScroll, row++);

        registerButton = new JButton("Register Beneficiary");
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
                "ID", "Name", "Email", "Phone", "Needs", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        beneficiaryTable = new JTable(tableModel);
        beneficiaryTable.setRowHeight(26);
        beneficiaryTable.getTableHeader().setReorderingAllowed(false);
        beneficiaryTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane tableScroll = new JScrollPane(beneficiaryTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Registered Beneficiaries")
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

        statusBox = new JComboBox<>(new String[]{
                "Pending", "In Progress", "Completed"
        });

        statusBox.setPreferredSize(new Dimension(150, 28));

        updateStatusButton = new JButton("Update Status");
        deleteButton = new JButton("Delete Beneficiary");

        styleButton(updateStatusButton);
        styleButton(deleteButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setBackground(Color.WHITE);

        actionPanel.add(new JLabel("Selected Beneficiary Status:"));
        actionPanel.add(statusBox);
        actionPanel.add(updateStatusButton);
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

        splitPane.setResizeWeight(0.45);
        splitPane.setDividerSize(8);

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(
                () -> splitPane.setDividerLocation(0.45)
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

    public void loadNeeds(ArrayList<LookupItem> items) {

        ArrayList<String> selected = getSelectedNeeds();

        needsPanel.removeAll();
        needCheckboxes.clear();

        for (LookupItem item : items) {

            JCheckBox box = new JCheckBox(item.getValue());
            box.setBackground(Color.WHITE);
            box.setSelected(selected.contains(item.getValue()));

            needCheckboxes.add(box);
            needsPanel.add(box);
        }

        needsPanel.revalidate();
        needsPanel.repaint();
    }

    public ArrayList<String> getSelectedNeeds() {

        ArrayList<String> needs = new ArrayList<>();

        for (JCheckBox box : needCheckboxes) {
            if (box.isSelected()) {
                needs.add(box.getText());
            }
        }

        return needs;
    }

    public String getBeneficiaryName() {
        return nameField.getText().trim();
    }

    public String getBeneficiaryEmail() {
        return emailField.getText().trim();
    }

    public String getBeneficiaryPhone() {
        return phoneField.getText().trim();
    }

    public String getSelectedStatus() {
        return (String) statusBox.getSelectedItem();
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
                editing ? "Save Changes" : "Register Beneficiary"
        );

        cancelButton.setEnabled(editing);
    }

    public void populateForm(Beneficiary beneficiary) {

        nameField.setText(beneficiary.getName());
        emailField.setText(beneficiary.getEmail());
        phoneField.setText(beneficiary.getPhone());

        for (JCheckBox box : needCheckboxes) {
            box.setSelected(
                    beneficiary.getNeeds().contains(box.getText())
            );
        }

        statusBox.setSelectedItem(beneficiary.getAssistanceStatus());

        setEditingMode(true);
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getUpdateStatusButton() {
        return updateStatusButton;
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

    public JTable getBeneficiaryTable() {
        return beneficiaryTable;
    }

    public int getSelectedBeneficiaryId() {

        int row = beneficiaryTable.getSelectedRow();

        if (row == -1) {
            return -1;
        }

        int modelRow = beneficiaryTable.convertRowIndexToModel(row);

        return (int) tableModel.getValueAt(modelRow, 0);
    }

    public void addBeneficiaryToTable(
            int id,
            String name,
            String email,
            String phone,
            ArrayList<String> needs,
            String status) {

        tableModel.addRow(new Object[]{
                id,
                name,
                email,
                phone,
                String.join(", ", needs),
                status
        });
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void updateBeneficiaryStatus(int id, String status) {

        for (int row = 0; row < tableModel.getRowCount(); row++) {

            if ((int) tableModel.getValueAt(row, 0) == id) {
                tableModel.setValueAt(status, row, 5);
                break;
            }
        }
    }

    public void clearFields() {

        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        searchField.setText("");

        for (JCheckBox box : needCheckboxes) {
            box.setSelected(false);
        }

        setEditingMode(false);
    }

    public void clearTableSelection() {
        beneficiaryTable.clearSelection();
    }
}
