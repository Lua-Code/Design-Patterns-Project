package view;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public final class ModernTheme {

    public static final Color NAVY = new Color(24, 59, 86);
    public static final Color DARK_NAVY = new Color(18, 45, 66);
    public static final Color TEAL = new Color(24, 169, 153);
    public static final Color BACKGROUND = new Color(244, 247, 251);
    public static final Color TEXT = new Color(39, 54, 69);
    public static final Color MUTED = new Color(110, 124, 139);
    public static final Color BORDER = new Color(218, 227, 235);

    public static final Font NORMAL =
            new Font("Segoe UI", Font.PLAIN, 13);

    public static final Font BOLD =
            new Font("Segoe UI", Font.BOLD, 13);

    private ModernTheme() {
    }

    // Apply styling to an entire Swing panel and its children
    public static void apply(Component component) {

        if (component instanceof JButton) {
            styleButton((JButton) component);

        } else if (component instanceof JTextField) {
            styleTextField((JTextField) component);

        } else if (component instanceof JComboBox<?>) {
            styleComboBox((JComboBox<?>) component);

        } else if (component instanceof JTextArea) {
            styleTextArea((JTextArea) component);

        } else if (component instanceof JTable) {
            styleTable((JTable) component);

        } else if (component instanceof JCheckBox) {
            styleCheckBox((JCheckBox) component);

        } else if (component instanceof JLabel) {
            styleLabel((JLabel) component);

        } else if (component instanceof JScrollPane) {
            styleScrollPane((JScrollPane) component);
        }

        if (component instanceof Container) {

            for (Component child :
                    ((Container) component).getComponents()) {

                apply(child);
            }
        }
    }

    public static void styleButton(JButton button) {

        button.setFont(BOLD);
        button.setForeground(Color.WHITE);
        button.setBackground(TEAL);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);

        button.setBorder(
                new EmptyBorder(9, 18, 9, 18)
        );

        button.setUI(new RoundedButtonUI());
    }

    public static void styleTextField(JTextField field) {

        field.setFont(NORMAL);
        field.setForeground(TEXT);
        field.setBackground(Color.WHITE);

        field.setBorder(
                new RoundedBorder(BORDER, 12)
        );

        field.setPreferredSize(
                new Dimension(
                        Math.max(180, field.getPreferredSize().width),
                        32
                )
        );
    }

    public static void styleComboBox(JComboBox<?> comboBox) {

        comboBox.setFont(NORMAL);
        comboBox.setForeground(TEXT);
        comboBox.setBackground(Color.WHITE);

        comboBox.setPreferredSize(
                new Dimension(
                        Math.max(180, comboBox.getPreferredSize().width),
                        32
                )
        );

        comboBox.setFocusable(false);
    }

    public static void styleTextArea(JTextArea area) {

        area.setFont(NORMAL);
        area.setForeground(TEXT);
        area.setBackground(Color.WHITE);

        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        area.setBorder(
                new EmptyBorder(7, 10, 7, 10)
        );
    }

    public static void styleTable(JTable table) {

        table.setFont(NORMAL);

        table.setForeground(TEXT);
        table.setBackground(Color.WHITE);

        table.setRowHeight(30);

        table.setSelectionBackground(
                new Color(209, 241, 235)
        );

        table.setSelectionForeground(NAVY);

        table.setGridColor(
                new Color(235, 239, 243)
        );

        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();

        if (header != null) {

            header.setFont(BOLD);
            header.setForeground(NAVY);

            header.setBackground(
                    new Color(231, 240, 246)
            );

            header.setPreferredSize(
                    new Dimension(0, 35)
            );

            header.setReorderingAllowed(false);
        }
    }

    public static void styleCheckBox(JCheckBox checkBox) {

        checkBox.setFont(NORMAL);
        checkBox.setForeground(TEXT);
        checkBox.setBackground(Color.WHITE);
        checkBox.setFocusPainted(false);
    }

    public static void styleLabel(JLabel label) {

        // Preserve headings that already have large fonts
        if (label.getFont().getSize() >= 19) {

            label.setFont(
                    new Font(
                            "Segoe UI",
                            Font.BOLD,
                            label.getFont().getSize()
                    )
            );

            label.setForeground(NAVY);

        } else {

            label.setFont(NORMAL);
        }
    }

    public static void styleScrollPane(JScrollPane scrollPane) {

        scrollPane.getViewport().setBackground(Color.WHITE);

        scrollPane.getVerticalScrollBar()
                .setUnitIncrement(16);
    }

    // Rounded text-field border
    private static class RoundedBorder extends AbstractBorder {

        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(7, 11, 7, 11);
        }

        @Override
        public Insets getBorderInsets(
                Component c,
                Insets insets) {

            insets.top = 7;
            insets.left = 11;
            insets.bottom = 7;
            insets.right = 11;

            return insets;
        }

        @Override
        public void paintBorder(
                Component c,
                Graphics graphics,
                int x,
                int y,
                int width,
                int height) {

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            g.setColor(color);

            g.drawRoundRect(
                    x,
                    y,
                    width - 1,
                    height - 1,
                    radius,
                    radius
            );

            g.dispose();
        }
    }

    // Rounded buttons with a hover effect
    private static class RoundedButtonUI extends BasicButtonUI {

        @Override
        public void update(Graphics graphics, JComponent component) {

            AbstractButton button = (AbstractButton) component;

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            Color color = TEAL;

            if (!button.isEnabled()) {

                color = new Color(167, 190, 190);

            } else if (button.getModel().isPressed()) {

                color = new Color(13, 125, 115);

            } else if (button.getModel().isRollover()) {

                color = new Color(20, 147, 135);
            }

            g.setColor(color);

            g.fill(new RoundRectangle2D.Double(
                    0,
                    0,
                    component.getWidth(),
                    component.getHeight(),
                    16,
                    16
            ));

            g.dispose();

            paint(graphics, component);
        }

        @Override
        protected void installDefaults(AbstractButton button) {

            super.installDefaults(button);

            button.setRolloverEnabled(true);
        }
    }
}