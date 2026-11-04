package view;

import model.LookupItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class LookupView extends JPanel {

    private static final Color PRIMARY = new Color(36, 70, 91);
    private static final Color BACKGROUND = Color.WHITE;

    private final JComboBox<String> categoryBox;
    private final JTextField valueField;
    private final JTextField searchField;

    private final JButton addButton;
    private final JButton updateButton;
    private final JButton deleteButton;
    private final JButton refreshButton;

    private final JTable lookupTable;
    private final DefaultTableModel tableModel;

    private ArrayList<LookupItem> currentItems = new ArrayList<>();

    public LookupView() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Lookup Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND);
        titlePanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        titlePanel.add(title, BorderLayout.WEST);

        categoryBox = new JComboBox<>(new String[]{
                "Volunteer Skills",
                "Beneficiary Needs",
                "Donation Types",
                "Event Types"
        });

        valueField = createTextField();
        searchField = createTextField();

        addButton = createButton("Add Item");
        updateButton = createButton("Update Item");
        deleteButton = createButton("Delete Item");
        refreshButton = createButton("Refresh");

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND);
        formPanel.setBorder(new EmptyBorder(10, 0, 15, 0));

        addFormRow(formPanel, "Category", categoryBox, 0);
        addFormRow(formPanel, "Lookup Value", valueField, 1);

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        formButtons.setBackground(BACKGROUND);
        formButtons.add(addButton);
        formButtons.add(updateButton);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 2;
        buttonConstraints.gridwidth = 2;
        buttonConstraints.anchor = GridBagConstraints.WEST;
        buttonConstraints.insets = new Insets(5, 0, 5, 0);
        formPanel.add(formButtons, buttonConstraints);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND);
        topPanel.add(formPanel, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(BACKGROUND);
        searchPanel.setBorder(new EmptyBorder(5, 0, 10, 0));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("Arial", Font.BOLD, 13));

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(refreshButton, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Value"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        lookupTable = new JTable(tableModel);
        lookupTable.setRowHeight(26);
        lookupTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lookupTable.getTableHeader().setReorderingAllowed(false);
        lookupTable.setFont(new Font("Arial", Font.PLAIN, 13));
        lookupTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane tableScrollPane = new JScrollPane(lookupTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(220, 220, 220)
        ));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(BACKGROUND);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionPanel.add(deleteButton);

        JPanel recordsPanel = new JPanel(new BorderLayout());
        recordsPanel.setBackground(BACKGROUND);
        recordsPanel.add(searchPanel, BorderLayout.NORTH);
        recordsPanel.add(tableScrollPane, BorderLayout.CENTER);
        recordsPanel.add(actionPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topPanel,
                recordsPanel
        );

        splitPane.setResizeWeight(0.45);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);
        splitPane.setBackground(BACKGROUND);

        add(titlePanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.45));

        lookupTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                int selectedRow = lookupTable.getSelectedRow();

                if (selectedRow >= 0) {
                    valueField.setText(
                            tableModel.getValueAt(selectedRow, 1).toString()
                    );
                }
            }
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                filterItems();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                filterItems();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                filterItems();
            }
        });

        refreshButton.addActionListener(event -> {
            searchField.setText("");
            displayItems(currentItems);
        });
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(250, 28));
        field.setFont(new Font("Arial", Font.PLAIN, 13));
        return field;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(150, 32));
        return button;
    }

    private void addFormRow(
            JPanel panel,
            String label,
            JComponent component,
            int row
    ) {
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(8, 0, 8, 15);

        JLabel fieldLabel = new JLabel(label);
        fieldLabel.setFont(new Font("Arial", Font.BOLD, 13));

        panel.add(fieldLabel, constraints);

        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        panel.add(component, constraints);
    }

    public String getSelectedCategory() {
        return (String) categoryBox.getSelectedItem();
    }

    public String getValueInput() {
        return valueField.getText();
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

    public int getSelectedItemId() {
        int selectedRow = lookupTable.getSelectedRow();

        if (selectedRow == -1) {
            return -1;
        }

        return (int) tableModel.getValueAt(selectedRow, 0);
    }

    public void displayItems(ArrayList<LookupItem> items) {
        currentItems = new ArrayList<>(items);
        filterItems();
    }

    private void filterItems() {
        String search = searchField.getText().trim().toLowerCase();

        tableModel.setRowCount(0);

        for (LookupItem item : currentItems) {
            if (String.valueOf(item.getId()).contains(search)
                    || item.getValue().toLowerCase().contains(search)) {
                tableModel.addRow(new Object[]{
                        item.getId(),
                        item.getValue()
                });
            }
        }
    }

    public void clearInput() {
        valueField.setText("");
        lookupTable.clearSelection();
    }
}