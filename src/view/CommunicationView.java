package view;

import model.NotificationRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CommunicationView extends JPanel {

    private static final Color PRIMARY = new Color(36, 70, 91);
    private static final Color BACKGROUND = Color.WHITE;

    private final JComboBox<String> methodBox;
    private final JTextField recipientField;
    private final JTextArea messageArea;
    private final JLabel recipientLabel;

    private final JButton sendButton;
    private final JButton deleteButton;
    private final JButton refreshButton;

    private final JTable historyTable;
    private final DefaultTableModel tableModel;

    public CommunicationView() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Communication Center");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(PRIMARY);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND);
        titlePanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        titlePanel.add(title, BorderLayout.WEST);

        methodBox = new JComboBox<>(new String[]{
                "Email",
                "SMS"
        });

        methodBox.setPreferredSize(new Dimension(250, 28));

        recipientField = createTextField();

        messageArea = new JTextArea(4, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setFont(new Font("Arial", Font.PLAIN, 13));

        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(250, 90));

        recipientLabel = new JLabel("Recipient Email:");
        recipientLabel.setFont(new Font("Arial", Font.BOLD, 13));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(BACKGROUND);
        formPanel.setBorder(new EmptyBorder(10, 0, 15, 0));

        addFormRow(formPanel, "Notification Method", methodBox, 0);
        addFormRow(formPanel, "Recipient", recipientField, 1);
        addFormRow(formPanel, "Message", messageScroll, 2);

        sendButton = createButton("Send Notification");
        sendButton.setPreferredSize(new Dimension(180, 32));

        JPanel formButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        formButtons.setBackground(BACKGROUND);
        formButtons.add(sendButton);

        GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 1;
        buttonConstraints.gridy = 3;
        buttonConstraints.anchor = GridBagConstraints.WEST;
        buttonConstraints.insets = new Insets(5, 0, 5, 0);

        formPanel.add(formButtons, buttonConstraints);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(BACKGROUND);
        topPanel.add(formPanel, BorderLayout.CENTER);

        JPanel historyHeader = new JPanel(new BorderLayout(10, 0));
        historyHeader.setBackground(BACKGROUND);
        historyHeader.setBorder(new EmptyBorder(5, 0, 10, 0));

        JLabel historyLabel = new JLabel("Notification History");
        historyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        historyLabel.setForeground(PRIMARY);

        refreshButton = createButton("Refresh");
        refreshButton.setPreferredSize(new Dimension(120, 32));

        historyHeader.add(historyLabel, BorderLayout.WEST);
        historyHeader.add(refreshButton, BorderLayout.EAST);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Method",
                        "Recipient",
                        "Message",
                        "Status",
                        "Date / Time"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(26);
        historyTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 13));
        historyTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane tableScrollPane = new JScrollPane(historyTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(220, 220, 220)
        ));

        deleteButton = createButton("Delete Selected Notification");

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(BACKGROUND);
        actionPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        actionPanel.add(deleteButton);

        JPanel recordsPanel = new JPanel(new BorderLayout());
        recordsPanel.setBackground(BACKGROUND);
        recordsPanel.add(historyHeader, BorderLayout.NORTH);
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

        methodBox.addActionListener(e -> {
            if ("Email".equals(getNotificationMethod())) {
                recipientLabel.setText("Recipient Email:");
            } else {
                recipientLabel.setText("Recipient Phone:");
            }
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

    public String getNotificationMethod() {
        return (String) methodBox.getSelectedItem();
    }

    public String getRecipient() {
        return recipientField.getText().trim();
    }

    public String getMessage() {
        return messageArea.getText().trim();
    }

    public JButton getSendButton() {
        return sendButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getRefreshButton() {
        return refreshButton;
    }

    public NotificationRecord getSelectedNotification() {
        int selectedRow = historyTable.getSelectedRow();

        if (selectedRow == -1) {
            return null;
        }

        return new NotificationRecord(
                tableModel.getValueAt(selectedRow, 0).toString(),
                tableModel.getValueAt(selectedRow, 1).toString(),
                tableModel.getValueAt(selectedRow, 2).toString(),
                tableModel.getValueAt(selectedRow, 3).toString(),
                tableModel.getValueAt(selectedRow, 4).toString()
        );
    }

    public void addNotificationToTable(
            String method,
            String recipient,
            String message,
            String status
    ) {
        addNotificationToTable(
                method,
                recipient,
                message,
                status,
                ""
        );
    }

    public void addNotificationToTable(
            String method,
            String recipient,
            String message,
            String status,
            String dateTime
    ) {
        tableModel.addRow(new Object[]{
                method,
                recipient,
                message,
                status,
                dateTime
        });
    }

    public void clearTable() {
        tableModel.setRowCount(0);
    }

    public void clearFields() {
        recipientField.setText("");
        messageArea.setText("");
    }
}
