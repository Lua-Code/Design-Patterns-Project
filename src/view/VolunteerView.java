package view;

import model.LookupItem;
import model.Volunteer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class VolunteerView extends JPanel {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField hoursField;
    private JTextField searchField;

    private JComboBox<String> availabilityBox;
    private JPanel skillsPanel;

    private final ArrayList<JCheckBox> skillCheckboxes = new ArrayList<>();

    private JButton registerButton;
    private JButton updateButton;
    private JButton cancelButton;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton deleteButton;
    private JButton addHoursButton;

    private JTable volunteerTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY = new Color(36, 70, 91);

    public VolunteerView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Volunteer Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        nameField = createTextField();
        emailField = createTextField();
        phoneField = createTextField();

        availabilityBox = new JComboBox<>(new String[]{
                "Weekdays", "Weekends", "Any Day",
                "Mornings", "Evenings"
        });
        availabilityBox.setPreferredSize(new Dimension(250, 28));

        skillsPanel = new JPanel(new GridLayout(0, 2, 8, 3));
        skillsPanel.setBackground(Color.WHITE);

        JScrollPane skillsScroll = new JScrollPane(skillsPanel);
        skillsScroll.setPreferredSize(new Dimension(250, 90));
        skillsScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        int row = 0;

        addFormRow(formPanel, "Volunteer Name:", nameField, row++);
        addFormRow(formPanel, "Email:", emailField, row++);
        addFormRow(formPanel, "Phone Number:", phoneField, row++);
        addFormRow(formPanel, "Skills:", skillsScroll, row++);
        addFormRow(formPanel, "Availability:", availabilityBox, row++);

        registerButton = new JButton("Register Volunteer");
        updateButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel Edit");

        styleButton(registerButton, 180);
        styleButton(updateButton, 180);
        styleButton(cancelButton, 140);

        updateButton.setVisible(false);
        cancelButton.setVisible(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        searchField = createTextField();
        searchField.setPreferredSize(new Dimension(250, 28));

        searchButton = new JButton("Search");
        refreshButton = new JButton("Refresh");

        styleButton(searchButton, 100);
        styleButton(refreshButton, 100);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(new JLabel("Search by ID, Name or Email:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        String[] columns = {
                "ID", "Name", "Email", "Phone",
                "Skills", "Availability", "Hours"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        volunteerTable = new JTable(tableModel);
        volunteerTable.setRowHeight(26);
        volunteerTable.getTableHeader().setReorderingAllowed(false);
        volunteerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(volunteerTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Registered Volunteers")
        );

        hoursField = createTextField();
        hoursField.setPreferredSize(new Dimension(100, 28));

        addHoursButton = new JButton("Add Hours");
        deleteButton = new JButton("Delete Volunteer");

        styleButton(addHoursButton, 130);
        styleButton(deleteButton, 170);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.add(new JLabel("Hours:"));
        actionsPanel.add(hoursField);
        actionsPanel.add(addHoursButton);
        actionsPanel.add(deleteButton);

        JPanel recordsPanel = new JPanel(new BorderLayout(5, 5));
        recordsPanel.setBackground(Color.WHITE);
        recordsPanel.add(searchPanel, BorderLayout.NORTH);
        recordsPanel.add(tableScroll, BorderLayout.CENTER);
        recordsPanel.add(actionsPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topPanel,
                recordsPanel
        );

        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.55));
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(18);
        field.setPreferredSize(new Dimension(250, 28));
        return field;
    }

    private void styleButton(JButton button, int width) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 32));
    }

    private void addFormRow(
            JPanel panel,
            String label,
            JComponent component,
            int row
    ) {
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

    public void loadSkills(ArrayList<LookupItem> items) {
        ArrayList<String> selected = getSelectedSkills();

        skillsPanel.removeAll();
        skillCheckboxes.clear();

        for (LookupItem item : items) {
            JCheckBox box = new JCheckBox(item.getValue());
            box.setBackground(Color.WHITE);
            box.setSelected(selected.contains(item.getValue()));

            skillCheckboxes.add(box);
            skillsPanel.add(box);
        }

        skillsPanel.revalidate();
        skillsPanel.repaint();
    }

    public ArrayList<String> getSelectedSkills() {
        ArrayList<String> skills = new ArrayList<>();

        for (JCheckBox box : skillCheckboxes) {
            if (box.isSelected()) {
                skills.add(box.getText());
            }
        }

        return skills;
    }

    public void loadVolunteer(Volunteer volunteer) {
        nameField.setText(volunteer.getName());
        emailField.setText(volunteer.getEmail());
        phoneField.setText(volunteer.getPhone());
        availabilityBox.setSelectedItem(volunteer.getAvailability());

        ArrayList<String> selectedSkills = volunteer.getSkills();

        for (JCheckBox box : skillCheckboxes) {
            box.setSelected(selectedSkills.contains(box.getText()));
        }

        setEditingMode(true);
    }

    public void setEditingMode(boolean editing) {
        registerButton.setVisible(!editing);
        updateButton.setVisible(editing);
        cancelButton.setVisible(editing);
    }

    public void displayVolunteers(ArrayList<Volunteer> volunteers) {
        tableModel.setRowCount(0);

        for (Volunteer volunteer : volunteers) {
            addVolunteerToTable(
                    volunteer.getId(),
                    volunteer.getName(),
                    volunteer.getEmail(),
                    volunteer.getPhone(),
                    volunteer.getSkills(),
                    volunteer.getAvailability(),
                    volunteer.getVolunteerHours()
            );
        }
    }

    public void addVolunteerToTable(
            int id,
            String name,
            String email,
            String phone,
            ArrayList<String> skills,
            String availability,
            double hours
    ) {
        tableModel.addRow(new Object[]{
                id,
                name,
                email,
                phone,
                String.join(", ", skills),
                availability,
                hours
        });
    }

    public void updateVolunteerInTable(Volunteer volunteer) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if ((int) tableModel.getValueAt(row, 0) == volunteer.getId()) {
                tableModel.setValueAt(volunteer.getName(), row, 1);
                tableModel.setValueAt(volunteer.getEmail(), row, 2);
                tableModel.setValueAt(volunteer.getPhone(), row, 3);
                tableModel.setValueAt(
                        String.join(", ", volunteer.getSkills()), row, 4
                );
                tableModel.setValueAt(volunteer.getAvailability(), row, 5);
                tableModel.setValueAt(volunteer.getVolunteerHours(), row, 6);
                break;
            }
        }
    }

    public void updateVolunteerHours(int id, double hours) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if ((int) tableModel.getValueAt(row, 0) == id) {
                tableModel.setValueAt(hours, row, 6);
                break;
            }
        }
    }

    public void removeVolunteerFromTable(int id) {
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if ((int) tableModel.getValueAt(row, 0) == id) {
                tableModel.removeRow(row);
                break;
            }
        }
    }

    public void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        availabilityBox.setSelectedIndex(0);

        for (JCheckBox box : skillCheckboxes) {
            box.setSelected(false);
        }

        setEditingMode(false);
    }

    public void clearHoursField() {
        hoursField.setText("");
    }

    public String getVolunteerName() {
        return nameField.getText().trim();
    }

    public String getVolunteerEmail() {
        return emailField.getText().trim();
    }

    public String getVolunteerPhone() {
        return phoneField.getText().trim();
    }

    public String getAvailability() {
        return (String) availabilityBox.getSelectedItem();
    }

    public String getHoursInput() {
        return hoursField.getText().trim();
    }

    public String getSearchInput() {
        return searchField.getText().trim();
    }

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getAddHoursButton() {
        return addHoursButton;
    }

    public JTable getVolunteerTable() {
        return volunteerTable;
    }

    public int getSelectedVolunteerId() {
        int row = volunteerTable.getSelectedRow();

        if (row == -1) {
            return -1;
        }

        int modelRow = volunteerTable.convertRowIndexToModel(row);
        return (int) tableModel.getValueAt(modelRow, 0);
    }
}