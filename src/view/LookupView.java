
package view;

import model.LookupItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class LookupView extends JPanel {

    private JComboBox<String> categoryBox;
    private JTextField valueField;

    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;

    private JTable lookupTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY = new Color(36, 70, 91);

    public LookupView() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Page title
        JLabel title = new JLabel("Lookup Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));


        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        categoryBox = new JComboBox<>(new String[]{
                "Volunteer Skills",
                "Beneficiary Needs",
                "Donation Types",
                "Event Types"
        });

        categoryBox.setPreferredSize(new Dimension(250, 28));

        valueField = new JTextField(20);
        valueField.setPreferredSize(new Dimension(250, 28));

        // Add form fields
        addFormRow(
                formPanel,
                "Lookup Category:",
                categoryBox,
                0
        );

        addFormRow(
                formPanel,
                "Value:",
                valueField,
                1
        );

        // Buttons
        addButton = new JButton("Add");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");

        styleButton(addButton);
        styleButton(updateButton);
        styleButton(deleteButton);

        deleteButton.setBackground(new Color(170, 50, 50));

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );

        buttonPanel.setBackground(Color.WHITE);

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 5, 5, 5);

        formPanel.add(buttonPanel, gbc);

        JPanel topPanel = new JPanel(new BorderLayout(5, 10));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(formPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Lookup table
        String[] columns = {"ID", "Value"};

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        lookupTable = new JTable(tableModel);
        lookupTable.setRowHeight(28);

        lookupTable.getTableHeader().setReorderingAllowed(false);
        lookupTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane =
                new JScrollPane(lookupTable);

        scrollPane.setBorder(
                BorderFactory.createTitledBorder("Lookup Values")
        );

        add(scrollPane, BorderLayout.CENTER);

        // When a row is selected, display its value
        lookupTable.getSelectionModel().addListSelectionListener(e -> {

            if (!e.getValueIsAdjusting()) {

                int selectedRow = lookupTable.getSelectedRow();

                if (selectedRow != -1) {

                    int modelRow =
                            lookupTable.convertRowIndexToModel(selectedRow);

                    valueField.setText(
                            tableModel.getValueAt(modelRow, 1).toString()
                    );
                }
            }
        });
    }

    private void styleButton(JButton button) {

        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(90, 32));
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
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(component, gbc);
    }

    // Get selected category
    public String getSelectedCategory() {
        return (String) categoryBox.getSelectedItem();
    }

    // Get entered value
    public String getValueInput() {
        return valueField.getText().trim();
    }

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JComboBox<String> getCategoryBox() {
        return categoryBox;
    }

    // Get selected item ID
    public int getSelectedItemId() {

        int selectedRow = lookupTable.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        int modelRow =
                lookupTable.convertRowIndexToModel(selectedRow);

        return (int) tableModel.getValueAt(modelRow, 0);
    }

    // Display lookup items in the table
    public void displayItems(ArrayList<LookupItem> items) {

        tableModel.setRowCount(0);

        for (LookupItem item : items) {

            tableModel.addRow(new Object[]{
                    item.getId(),
                    item.getValue()
            });
        }
    }

    // Clear input and selection
    public void clearInput() {

        lookupTable.clearSelection();
        valueField.setText("");
    }
}
