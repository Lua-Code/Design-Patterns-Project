
package view;

import model.LookupItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class VolunteerView extends JPanel {

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField hoursField;

    private JComboBox<String> availabilityBox;
    private JPanel skillsPanel;

    private final ArrayList<JCheckBox> skillCheckboxes =
            new ArrayList<>();

    private JButton registerButton;
    private JButton addHoursButton;

    private JTable volunteerTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY = new Color(36, 70, 91);

    public VolunteerView() {

        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Volunteer Management");
        title.setFont(new Font("Arial", Font.BOLD, 23));
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

        availabilityBox.setPreferredSize(new Dimension(220, 26));

        // Skills loaded from Lookup Management
        skillsPanel = new JPanel(new GridLayout(0, 2, 8, 3));
        skillsPanel.setBackground(Color.WHITE);

        int row = 0;

        addFormRow(formPanel, "Volunteer Name:", nameField, row++);
        addFormRow(formPanel, "Email:", emailField, row++);
        addFormRow(formPanel, "Phone Number:", phoneField, row++);
        addFormRow(formPanel, "Skills:", skillsPanel, row++);
        addFormRow(formPanel, "Availability:", availabilityBox, row++);

        // Register button remains visible
        registerButton = new JButton("Register Volunteer");
        styleButton(registerButton, 170);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(registerButton);

        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(formScroll, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Volunteer table
        String[] columns = {
                "ID", "Name", "Email", "Phone",
                "Skills", "Availability", "Hours"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        volunteerTable = new JTable(tableModel);
        volunteerTable.setRowHeight(24);
        volunteerTable.getTableHeader().setReorderingAllowed(false);
        volunteerTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane tableScroll = new JScrollPane(volunteerTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Registered Volunteers")
        );

        // Add hours controls
        hoursField = createTextField();
        hoursField.setPreferredSize(new Dimension(90, 26));

        addHoursButton = new JButton("Add Hours");
        styleButton(addHoursButton, 120);

        JPanel hoursPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        hoursPanel.setBackground(Color.WHITE);

        hoursPanel.add(new JLabel("Select Volunteer, Hours:"));
        hoursPanel.add(hoursField);
        hoursPanel.add(addHoursButton);

        JPanel recordsPanel = new JPanel(new BorderLayout(4, 4));
        recordsPanel.setBackground(Color.WHITE);

        recordsPanel.add(tableScroll, BorderLayout.CENTER);
        recordsPanel.add(hoursPanel, BorderLayout.SOUTH);

        // Compact records section
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topPanel,
                recordsPanel
        );

        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(true);

        topPanel.setMinimumSize(new Dimension(0, 160));
        recordsPanel.setMinimumSize(new Dimension(0, 125));

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(
                () -> splitPane.setDividerLocation(0.65)
        );
    }

    private JTextField createTextField() {
        JTextField field = new JTextField(18);
        field.setPreferredSize(new Dimension(220, 26));
        return field;
    }

    private void styleButton(JButton button, int width) {
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, 30));
    }

    private void addFormRow(JPanel panel, String label,
                            JComponent component, int row) {

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = row;
        gbc.insets = new Insets(3, 5, 3, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
    }

    // Dynamic skills from Lookup Management
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

    public JButton getRegisterButton() {
        return registerButton;
    }

    public JButton getAddHoursButton() {
        return addHoursButton;
    }

    public String getHoursInput() {
        return hoursField.getText().trim();
    }

    public int getSelectedVolunteerId() {

        int row = volunteerTable.getSelectedRow();
        if (row == -1) return -1;

        int modelRow = volunteerTable.convertRowIndexToModel(row);
        return (int) tableModel.getValueAt(modelRow, 0);
    }

    public void addVolunteerToTable(
            int id, String name, String email, String phone,
            ArrayList<String> skills, String availability,
            double hours) {

        tableModel.addRow(new Object[]{
                id, name, email, phone,
                String.join(", ", skills), availability, hours
        });
    }

    public void updateVolunteerHours(int id, double hours) {

        for (int row = 0; row < tableModel.getRowCount(); row++) {

            if ((int) tableModel.getValueAt(row, 0) == id) {
                tableModel.setValueAt(hours, row, 6);
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
    }

    public void clearHoursField() {
        hoursField.setText("");
    }
}
