
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class CommunicationView extends JPanel {

    private JComboBox<String> methodBox;
    private JTextField recipientField;
    private JTextArea messageArea;

    private JLabel recipientLabel;

    private JButton sendButton;

    private JTable historyTable;
    private DefaultTableModel tableModel;

    private final Color PRIMARY = new Color(36, 70, 91);

    public CommunicationView() {

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Page title
        JLabel title = new JLabel("Communication Center");
        title.setFont(new Font("Arial", Font.BOLD, 24));

        add(title, BorderLayout.NORTH);

        // Communication form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);

        methodBox = new JComboBox<>(new String[]{
                "Email",
                "SMS"
        });

        methodBox.setPreferredSize(new Dimension(250, 28));

        recipientField = new JTextField(20);
        recipientField.setPreferredSize(new Dimension(250, 28));

        messageArea = new JTextArea(4, 20);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setPreferredSize(new Dimension(250, 90));

        recipientLabel = new JLabel("Recipient Email:");

        // Add form fields
        int row = 0;

        addFormRow(
                formPanel,
                new JLabel("Notification Method:"),
                methodBox,
                row++
        );

        addFormRow(
                formPanel,
                recipientLabel,
                recipientField,
                row++
        );

        addFormRow(
                formPanel,
                new JLabel("Message:"),
                messageScroll,
                row++
        );

        // Send button
        sendButton = new JButton("Send Notification");
        sendButton.setBackground(PRIMARY);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setPreferredSize(new Dimension(180, 32));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(10, 5, 5, 5);

        formPanel.add(sendButton, gbc);

        // Update recipient label when method changes
        methodBox.addActionListener(e -> {

            String method = getNotificationMethod();

            if (method.equals("Email")) {
                recipientLabel.setText("Recipient Email:");
            } else {
                recipientLabel.setText("Recipient Phone:");
            }
        });

        // Notification history table
        String[] columns = {
                "Method",
                "Recipient",
                "Message",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(26);

        historyTable.getTableHeader().setReorderingAllowed(false);
        historyTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane tableScrollPane =
                new JScrollPane(historyTable);

        tableScrollPane.setBorder(
                BorderFactory.createTitledBorder(
                        "Notification History"
                )
        );

        // Split form and history
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                formScrollPane,
                tableScrollPane
        );

        splitPane.setResizeWeight(0.55);
        splitPane.setDividerSize(8);
        splitPane.setOneTouchExpandable(true);

        add(splitPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() ->
                splitPane.setDividerLocation(0.55)
        );
    }

    // Add labeled form row
    private void addFormRow(
            JPanel panel,
            JLabel label,
            JComponent component,
            int row) {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridy = row;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(component, gbc);
    }

    // Get user input
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

    // Add notification to history
    public void addNotificationToTable(
            String method,
            String recipient,
            String message,
            String status) {

        tableModel.addRow(new Object[]{
                method,
                recipient,
                message,
                status
        });
    }

    // Clear form
    public void clearFields() {
        recipientField.setText("");
        messageArea.setText("");
    }
}
