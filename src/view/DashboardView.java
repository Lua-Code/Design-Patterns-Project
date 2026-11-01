package view;

import controller.*;
import singleton.OrganizationManager;
import storage.AppData;
import storage.DataStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardView extends JFrame {

    private AppData appData;
    private JPanel mainPanel;

    private DonationController donationController;
    private DonationView donationView;

    private VolunteerController volunteerController;
    private VolunteerView volunteerView;

    private EventController eventController;
    private EventView eventView;

    private BeneficiaryController beneficiaryController;
    private BeneficiaryView beneficiaryView;

    private CommunicationController communicationController;
    private CommunicationView communicationView;

    private LookupController lookupController;
    private LookupView lookupView;

    private final OrganizationManager organization =
            OrganizationManager.getInstance();

    private final Map<String, NavigationButton> navigationButtons =
            new LinkedHashMap<>();

    public DashboardView() {

        setTitle(
                organization.getOrganizationName()
                        + " - Management System"
        );

        setSize(1200, 800);
        setMinimumSize(new Dimension(950, 650));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(ModernTheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(createHeader(), BorderLayout.NORTH);
        add(createSidebar(), BorderLayout.WEST);

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.BACKGROUND);

        mainPanel.setBorder(
                new EmptyBorder(20, 22, 20, 22)
        );

        add(mainPanel, BorderLayout.CENTER);

        try {

            appData = DataStorage.load();

        } catch (IOException | ClassNotFoundException ex) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load saved application data.\n"
                            + "The existing file has not been overwritten.",
                    "Loading Error",
                    JOptionPane.ERROR_MESSAGE
            );

            throw new IllegalStateException(
                    "Failed to load saved application data.", ex
            );
        }

        initializeModules();

        showPage("Dashboard");

        setVisible(true);
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(ModernTheme.NAVY);
        header.setPreferredSize(new Dimension(1200, 108));

        header.setBorder(
                new EmptyBorder(16, 35, 16, 35)
        );

        // Two separate rows prevent subtitle clipping
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 5));
        titles.setOpaque(false);

        JLabel title = new JLabel("Hope Foundation");

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 28)
        );

        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(
                "Non-Profit Management System"
        );

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 15)
        );

        subtitle.setForeground(
                new Color(201, 223, 234)
        );

        titles.add(title);
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);

        JLabel portalLabel = new JLabel("MANAGEMENT PORTAL");

        portalLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        portalLabel.setForeground(
                new Color(184, 235, 226)
        );

        header.add(portalLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());

        sidebar.setBackground(ModernTheme.DARK_NAVY);
        sidebar.setPreferredSize(new Dimension(240, 0));

        sidebar.setBorder(
                new EmptyBorder(27, 16, 20, 16)
        );

        JPanel navigation = new JPanel();

        navigation.setOpaque(false);

        navigation.setLayout(
                new BoxLayout(navigation, BoxLayout.Y_AXIS)
        );

        JLabel heading = new JLabel("NAVIGATION");

        heading.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        heading.setForeground(
                new Color(153, 185, 203)
        );

        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        heading.setBorder(
                new EmptyBorder(0, 12, 18, 0)
        );

        navigation.add(heading);

        String[] pages = {
                "Dashboard",
                "Donations",
                "Volunteers",
                "Events",
                "Beneficiaries",
                "Notifications",
                "Lookup Management"
        };

        for (String page : pages) {

            NavigationButton button =
                    new NavigationButton(page);

            button.setAlignmentX(Component.LEFT_ALIGNMENT);

            button.setMaximumSize(
                    new Dimension(Integer.MAX_VALUE, 48)
            );

            button.setPreferredSize(
                    new Dimension(205, 48)
            );

            button.addActionListener(
                    e -> showPage(page)
            );

            navigation.add(button);
            navigation.add(Box.createVerticalStrut(10));

            navigationButtons.put(page, button);
        }

        sidebar.add(navigation, BorderLayout.NORTH);

        // No footer at the bottom of the sidebar

        return sidebar;
    }

    private static class NavigationButton extends JButton {

        private boolean active = false;

        NavigationButton(String text) {

            super(text);

            setFont(
                    new Font("Segoe UI", Font.BOLD, 14)
            );

            setForeground(Color.WHITE);

            setHorizontalAlignment(SwingConstants.LEFT);

            setBorder(
                    new EmptyBorder(0, 20, 0, 10)
            );

            setFocusPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);

            setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );

            setRolloverEnabled(true);
        }

        void setActive(boolean active) {

            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color background;

            if (active) {

                background = ModernTheme.TEAL;

            } else if (getModel().isRollover()) {

                background = new Color(43, 84, 110);

            } else {

                background = new Color(27, 63, 89);
            }

            g.setColor(background);

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    20,
                    20
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }

    private void initializeModules() {

        lookupController = new LookupController(appData);
        lookupView = new LookupView();
        lookupController.connectView(lookupView);

        donationController = new DonationController(appData);
        donationView = new DonationView();
        donationController.connectView(donationView);

        volunteerController = new VolunteerController(appData);
        volunteerView = new VolunteerView();
        volunteerController.connectView(volunteerView);

        eventController = new EventController(appData);
        eventView = new EventView();
        eventController.connectView(eventView);

        beneficiaryController =
                new BeneficiaryController(appData);

        beneficiaryView = new BeneficiaryView();
        beneficiaryController.connectView(beneficiaryView);

        communicationController =
                new CommunicationController(appData);

        communicationView = new CommunicationView();
        communicationController.connectView(communicationView);

        // Apply the modern design to every module
        ModernTheme.apply(donationView);
        ModernTheme.apply(volunteerView);
        ModernTheme.apply(eventView);
        ModernTheme.apply(beneficiaryView);
        ModernTheme.apply(communicationView);
        ModernTheme.apply(lookupView);
    }

    private JPanel createStatCard(
            String label,
            String value,
            Color accent) {

        JPanel card = new RoundedPanel();

        card.setBackground(Color.WHITE);

        card.setLayout(new BorderLayout(5, 10));

        card.setBorder(
                new EmptyBorder(20, 24, 20, 24)
        );

        JLabel title = new JLabel(label);

        title.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        title.setForeground(ModernTheme.MUTED);

        JLabel number = new JLabel(value);

        number.setFont(
                new Font("Segoe UI", Font.BOLD, 34)
        );

        number.setForeground(accent);

        card.add(title, BorderLayout.NORTH);
        card.add(number, BorderLayout.CENTER);

        return card;
    }

    private static class RoundedPanel extends JPanel {

        RoundedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(getBackground());

            g.fillRoundRect(
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    20,
                    20
            );

            g.dispose();

            super.paintComponent(graphics);
        }
    }

    private JPanel createDashboard() {

        JPanel dashboard = new JPanel(
                new BorderLayout(18, 20)
        );

        dashboard.setBackground(ModernTheme.BACKGROUND);

        JPanel welcomePanel = new RoundedPanel();

        welcomePanel.setBackground(Color.WHITE);

        welcomePanel.setLayout(
                new BoxLayout(
                        welcomePanel,
                        BoxLayout.Y_AXIS
                )
        );

        welcomePanel.setBorder(
                new EmptyBorder(22, 25, 22, 25)
        );

        JLabel welcome = new JLabel(
                "Welcome to Hope Foundation"
        );

        welcome.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );

        welcome.setForeground(ModernTheme.NAVY);

        JLabel description = new JLabel(
                "Manage donations, volunteers, events and beneficiaries."
        );

        description.setFont(
                new Font("Segoe UI", Font.PLAIN, 14)
        );

        description.setForeground(ModernTheme.MUTED);

        welcomePanel.add(welcome);
        welcomePanel.add(Box.createVerticalStrut(8));
        welcomePanel.add(description);

        dashboard.add(welcomePanel, BorderLayout.NORTH);

        JPanel cards = new JPanel(
                new GridLayout(2, 2, 16, 16)
        );

        cards.setOpaque(false);

        cards.add(createStatCard(
                "Total Donations",
                String.valueOf(
                        donationController.getDonations().size()
                ),
                ModernTheme.TEAL
        ));

        cards.add(createStatCard(
                "Registered Volunteers",
                String.valueOf(
                        volunteerController.getVolunteerCount()
                ),
                new Color(62, 119, 194)
        ));

        cards.add(createStatCard(
                "Created Events",
                String.valueOf(
                        eventController.getEventCount()
                ),
                new Color(228, 149, 61)
        ));

        cards.add(createStatCard(
                "Registered Beneficiaries",
                String.valueOf(
                        beneficiaryController.getBeneficiaryCount()
                ),
                new Color(175, 95, 170)
        ));

        dashboard.add(cards, BorderLayout.CENTER);

        JPanel statistics = new RoundedPanel();

        statistics.setBackground(Color.WHITE);

        statistics.setLayout(
                new GridLayout(5, 2, 12, 10)
        );

        statistics.setBorder(
                new EmptyBorder(18, 25, 18, 25)
        );

        statistics.add(
                createDetailLabel("ADDITIONAL STATISTICS", true)
        );

        statistics.add(new JLabel(""));

        statistics.add(
                createDetailLabel("Total Donation Value", false)
        );

        statistics.add(
                createValueLabel(
                        String.format(
                                "%.2f EGP",
                                donationController.getTotalDonations()
                        )
                )
        );

        statistics.add(
                createDetailLabel("Total Volunteer Hours", false)
        );

        statistics.add(
                createValueLabel(
                        String.format(
                                "%.1f hours",
                                volunteerController.getTotalVolunteerHours()
                        )
                )
        );

        statistics.add(
                createDetailLabel("Registered Event Attendees", false)
        );

        statistics.add(
                createValueLabel(
                        String.valueOf(
                                eventController.getTotalAttendees()
                        )
                )
        );

        statistics.add(
                createDetailLabel("Pending Assistance Cases", false)
        );

        statistics.add(
                createValueLabel(
                        String.valueOf(
                                beneficiaryController.getPendingCount()
                        )
                )
        );

        dashboard.add(statistics, BorderLayout.SOUTH);

        return dashboard;
    }

    private JLabel createDetailLabel(
            String text,
            boolean heading) {

        JLabel label = new JLabel(text);

        label.setFont(
                new Font(
                        "Segoe UI",
                        heading ? Font.BOLD : Font.PLAIN,
                        heading ? 12 : 14
                )
        );

        label.setForeground(
                heading
                        ? ModernTheme.NAVY
                        : ModernTheme.MUTED
        );

        return label;
    }

    private JLabel createValueLabel(String value) {

        JLabel label = new JLabel(value);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 15)
        );

        label.setForeground(ModernTheme.TEXT);

        return label;
    }

    private void showPage(String pageName) {

        for (Map.Entry<String, NavigationButton> entry
                : navigationButtons.entrySet()) {

            entry.getValue().setActive(
                    entry.getKey().equals(pageName)
            );
        }

        mainPanel.removeAll();

        switch (pageName) {

            case "Dashboard":

                mainPanel.add(
                        createDashboard(),
                        BorderLayout.CENTER
                );

                break;

            case "Donations":

                donationView.loadDonationTypes(
                        lookupController.getItemsByCategory(
                                "Donation Types"
                        )
                );

                mainPanel.add(
                        donationView,
                        BorderLayout.CENTER
                );

                break;

            case "Volunteers":

                volunteerView.loadSkills(
                        lookupController.getItemsByCategory(
                                "Volunteer Skills"
                        )
                );

                mainPanel.add(
                        volunteerView,
                        BorderLayout.CENTER
                );

                break;

            case "Events":

                eventView.loadEventTypes(
                        lookupController.getItemsByCategory(
                                "Event Types"
                        )
                );

                mainPanel.add(
                        eventView,
                        BorderLayout.CENTER
                );

                break;

            case "Beneficiaries":

                beneficiaryView.loadNeeds(
                        lookupController.getItemsByCategory(
                                "Beneficiary Needs"
                        )
                );

                mainPanel.add(
                        beneficiaryView,
                        BorderLayout.CENTER
                );

                break;

            case "Notifications":

                mainPanel.add(
                        communicationView,
                        BorderLayout.CENTER
                );

                break;

            case "Lookup Management":

                mainPanel.add(
                        lookupView,
                        BorderLayout.CENTER
                );

                break;
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}