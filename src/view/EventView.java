package view;

import model.Event;
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
    private JTextField searchField;

    private JTextArea descriptionArea;
    private JComboBox<String> eventTypeBox;

    private JButton createButton;
    private JButton updateButton;
    private JButton cancelButton;
    private JButton registerAttendeeButton;
    private JButton deleteButton;
    private JButton searchButton;
    private JButton refreshButton;

    private JTable eventTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY = new Color(36, 70, 91);

    public EventView() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Event Management");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        nameField = createTextField();
        dateField = createTextField();
        locationField = createTextField();
        capacityField = createTextField();

        dateField.setToolTipText("YYYY-MM-DD");

        eventTypeBox = new JComboBox<>();
        eventTypeBox.setPreferredSize(new Dimension(250, 28));

        descriptionArea = new JTextArea(3, 18);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setPreferredSize(new Dimension(250, 65));

        int row = 0;

        addFormRow(formPanel, "Event Name:", nameField, row++);
        addFormRow(formPanel, "Event Type:", eventTypeBox, row++);
        addFormRow(formPanel, "Date (YYYY-MM-DD):", dateField, row++);
        addFormRow(formPanel, "Location:", locationField, row++);
        addFormRow(formPanel, "Capacity:", capacityField, row++);
        addFormRow(formPanel, "Description:", descriptionScroll, row++);

        createButton = new JButton("Create Event");
        updateButton = new JButton("Save Changes");
        cancelButton = new JButton("Cancel Edit");

        styleButton(createButton, 150);
        styleButton(updateButton, 150);
        styleButton(cancelButton, 130);

        updateButton.setVisible(false);
        cancelButton.setVisible(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(createButton);
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
        searchPanel.add(new JLabel("Search by ID, Name, Type or Location:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        String[] columns = {
                "ID", "Event Name", "Type", "Date",
                "Location", "Capacity", "Registered"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        eventTable = new JTable(tableModel);
        eventTable.setRowHeight(26);
        eventTable.getTableHeader().setReorderingAllowed(false);
        eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane tableScroll = new JScrollPane(eventTable);
        tableScroll.setBorder(
                BorderFactory.createTitledBorder("Events")
        );

        attendeeCountField = new JTextField("1", 6);
        attendeeCountField.setPreferredSize(new Dimension(90, 28));

        registerAttendeeButton = new JButton("Register Attendees");
        deleteButton = new JButton("Delete Event");

        styleButton(registerAttendeeButton, 160);
        styleButton(deleteButton, 150);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionsPanel.setBackground(Color.WHITE);
        actionsPanel.add(new JLabel("Number of Attendees:"));
        actionsPanel.add(attendeeCountField);
        actionsPanel.add(registerAttendeeButton);
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

    public void loadEvent(Event event) {
        nameField.setText(event.getEventName());
        eventTypeBox.setSelectedItem(event.getEventType());
        dateField.setText(event.getDate());
        locationField.setText(event.getLocation());
        capacityField.setText(String.valueOf(event.getCapacity()));
        descriptionArea.setText(event.getDescription());

        setEditingMode(true);
    }

    public void setEditingMode(boolean editing) {
        createButton.setVisible(!editing);
        updateButton.setVisible(editing);
        cancelButton.setVisible(editing);
    }

    public void displayEvents(ArrayList<Event> events) {
        tableModel.setRowCount(0);

        for (Event event : events) {
            addEventToTable(
                    event.getEventId(),
                    event.getEventName(),
                    event.getEventType(),
                    event.getDate(),
                    event.getLocation(),
                    event.getCapacity(),
                    event.getRegisteredAttendees()
            );
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

    public String getAttendeeCountInput() {
        return attendeeCountField.getText().trim();
    }

    public String getSearchInput() {
        return searchField.getText().trim();
    }

    public JButton getCreateButton() {
        return createButton;
    }

    public JButton getUpdateButton() {
        return updateButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public JButton getRegisterAttendeeButton() {
        return registerAttendeeButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public JTable getEventTable() {
        return eventTable;
    }

    public int getSelectedEventId() {
        int row = eventTable.getSelectedRow();

        if (row == -1) {
            return -1;
        }

        int modelRow = eventTable.convertRowIndexToModel(row);

        return (int) tableModel.getValueAt(modelRow, 0);
    }

    public void addEventToTable(
            int id,
            String name,
            String type,
            String date,
            String location,
            int capacity,
            int registered
    ) {
        tableModel.addRow(new Object[]{
                id, name, type, date, location, capacity, registered
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

        setEditingMode(false);
    }
}
