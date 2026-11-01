
package view;

import model.LookupItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class EventView extends JPanel {

    private JTextField nameField;
    private JTextField dateField;
    private JTextField locationField;
    private JTextField capacityField;

    private JTextField attendeeCountField;

    private JTextArea descriptionArea;
    private JComboBox<String> eventTypeBox;

    private JButton createButton;
    private JButton registerAttendeeButton;

    private JTable eventTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY = new Color(36, 70, 91);

    public EventView() {

        setLayout(new BorderLayout(8, 8));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Event Management");
        title.setFont(new Font("Arial", Font.BOLD, 23));
        add(title, BorderLayout.NORTH);

        // Event creation form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        nameField = createTextField();
        dateField = createTextField();
        locationField = createTextField();
        capacityField = createTextField();

        dateField.setToolTipText("YYYY-MM-DD");

        eventTypeBox = new JComboBox<>();
        eventTypeBox.setPreferredSize(new Dimension(220, 26));

        descriptionArea = new JTextArea(2, 18);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descriptionScroll =
                new JScrollPane(descriptionArea);

        descriptionScroll.setPreferredSize(
                new Dimension(220, 50)
        );

        int row = 0;

        addFormRow(formPanel, "Event Name:", nameField, row++);
        addFormRow(formPanel, "Event Type:", eventTypeBox, row++);
        addFormRow(formPanel, "Date (YYYY-MM-DD):", dateField, row++);
        addFormRow(formPanel, "Location:", locationField, row++);
        addFormRow(formPanel, "Capacity:", capacityField, row++);
        addFormRow(formPanel, "Description:", descriptionScroll, row++);

        // Create Event button
        createButton = new JButton("Create Event");
        styleButton(createButton, 160);

        JPanel buttonPanel =
                new JPanel(new FlowLayout(FlowLayout.RIGHT));

        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(createButton);

        JScrollPane formScroll = new JScrollPane(formPanel);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBackground(Color.WHITE);

        topPanel.add(formScroll, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Event records table
        String[] columns = {
                "ID", "Event Name", "Type", "Date",
                "Location", "Capacity", "Registered"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(24);

        eventTable.getTableHeader().setReorderingAllowed(false);
        eventTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane tableScroll = new JScrollPane(eventTable);

        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Events")
        );

        // Multiple attendee registration
        attendeeCountField = new JTextField("1", 6);
        attendeeCountField.setPreferredSize(
                new Dimension(70, 26)
        );

        registerAttendeeButton =
                new JButton("Register Attendees");

        styleButton(registerAttendeeButton, 170);

        JPanel registrationPanel =
                new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));

        registrationPanel.setBackground(Color.WHITE);

        registrationPanel.add(new JLabel("Select Event"));

        registrationPanel.add(
                new JLabel("Number of Attendees:")
        );

        registrationPanel.add(attendeeCountField);
        registrationPanel.add(registerAttendeeButton);

        JPanel recordsPanel =
                new JPanel(new BorderLayout(4, 4));

        recordsPanel.setBackground(Color.WHITE);

        recordsPanel.add(tableScroll, BorderLayout.CENTER);
        recordsPanel.add(registrationPanel, BorderLayout.SOUTH);

        // Compact layout
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                topPanel,
                recordsPanel
        );

        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(6);
        splitPane.setOneTouchExpandable(true);

        topPanel.setMinimumSize(new Dimension(0, 170));
        recordsPanel.setMinimumSize(new Dimension(0, 120));

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

    // Dynamic event types
    public void loadEventTypes(ArrayList<LookupItem> items) {

        String previous = getEventType();

        eventTypeBox.removeAllItems();

        for (LookupItem item : items) {
            eventTypeBox.addItem(item.getValue());
        }

        if (previous != null) {
            eventTypeBox.setSelectedItem(previous);
        }
    }

    public String getEventName() {
        return nameField.getText().trim();
    }

    public String getEventType() {
        return (String) eventTypeBox.getSelectedItem();
    }

    public String getEventDate() {
        return dateField.getText().trim();
    }

    public String getEventLocation() {
        return locationField.getText().trim();
    }

    public String getEventDescription() {
        return descriptionArea.getText().trim();
    }

    public String getCapacityInput() {
        return capacityField.getText().trim();
    }

    // Number of attendees entered by user
    public String getAttendeeCountInput() {
        return attendeeCountField.getText().trim();
    }

    public JButton getCreateButton() {
        return createButton;
    }

    public JButton getRegisterAttendeeButton() {
        return registerAttendeeButton;
    }

    public int getSelectedEventId() {

        int row = eventTable.getSelectedRow();

        if (row == -1) return -1;

        int modelRow = eventTable.convertRowIndexToModel(row);

        return (int) tableModel.getValueAt(modelRow, 0);
    }

    public void addEventToTable(
            int id, String name, String type, String date,
            String location, int capacity, int registered) {

        tableModel.addRow(new Object[]{
                id, name, type, date,
                location, capacity, registered
        });
    }

    public void updateAttendeeCount(int id, int count) {

        for (int row = 0; row < tableModel.getRowCount(); row++) {

            if ((int) tableModel.getValueAt(row, 0) == id) {

                tableModel.setValueAt(count, row, 6);
                break;
            }
        }
    }

    public void clearAttendeeCount() {
        attendeeCountField.setText("1");
    }

    public void clearFields() {

        nameField.setText("");
        dateField.setText("");
        locationField.setText("");
        capacityField.setText("");
        descriptionArea.setText("");

        if (eventTypeBox.getItemCount() > 0) {
            eventTypeBox.setSelectedIndex(0);
        }
    }
}
