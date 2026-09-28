package com.codealpha.stocktrading.ui.theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Collection of modern, reusable Swing GUI components styled for the Dark Fintech theme.
 */
public class ModernComponents {

    /**
     * Rounded dark container card panel.
     */
    public static class ModernCard extends JPanel {
        private final int cornerRadius;
        private Color backgroundColor = UITheme.BG_CARD;
        private Color borderColor = UITheme.BORDER_COLOR;

        public ModernCard() {
            this(12);
        }

        public ModernCard(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            setOpaque(false);
            setBorder(new EmptyBorder(14, 14, 14, 14));
        }

        public void setCardBackground(Color color) {
            this.backgroundColor = color;
            repaint();
        }

        public void setBorderColor(Color color) {
            this.borderColor = color;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.enableAntiAliasing(g);
            Graphics2D g2 = (Graphics2D) g;
            
            // Background
            g2.setColor(backgroundColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            
            // Border
            g2.setColor(borderColor);
            g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));

            super.paintComponent(g);
        }
    }

    /**
     * Dashboard KPI Statistic card with title, large value, and secondary status badge.
     */
    public static class StatCard extends ModernCard {
        private final JLabel titleLabel;
        private final JLabel valueLabel;
        private final JLabel subtextLabel;

        public StatCard(String title, String initialValue, String subtext) {
            super(12);
            setLayout(new BorderLayout(5, 5));
            setPreferredSize(new Dimension(200, 95));

            titleLabel = new JLabel(title.toUpperCase());
            titleLabel.setFont(UITheme.FONT_SMALL);
            titleLabel.setForeground(UITheme.TEXT_MUTED);

            valueLabel = new JLabel(initialValue);
            valueLabel.setFont(UITheme.FONT_TITLE);
            valueLabel.setForeground(UITheme.TEXT_PRIMARY);

            subtextLabel = new JLabel(subtext);
            subtextLabel.setFont(UITheme.FONT_BODY);
            subtextLabel.setForeground(UITheme.TEXT_SECONDARY);

            JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 2));
            centerPanel.setOpaque(false);
            centerPanel.add(titleLabel);
            centerPanel.add(valueLabel);

            add(centerPanel, BorderLayout.CENTER);
            add(subtextLabel, BorderLayout.SOUTH);
        }

        public void setValue(String value, Color color) {
            valueLabel.setText(value);
            if (color != null) {
                valueLabel.setForeground(color);
            }
        }

        public void setSubtext(String subtext, Color color) {
            subtextLabel.setText(subtext);
            if (color != null) {
                subtextLabel.setForeground(color);
            }
        }
    }

    /**
     * Custom styled button with hover and press effects.
     */
    public static class StyledButton extends JButton {
        private Color normalColor;
        private Color hoverColor;
        private Color pressedColor;
        private boolean isHovered = false;
        private boolean isPressed = false;
        private int radius = 8;

        public StyledButton(String text, Color normalColor, Color hoverColor) {
            super(text);
            this.normalColor = normalColor;
            this.hoverColor = hoverColor;
            this.pressedColor = normalColor.darker();

            setFont(UITheme.FONT_BODY_BOLD);
            setForeground(UITheme.TEXT_PRIMARY);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(8, 16, 8, 16));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled()) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.enableAntiAliasing(g);
            Graphics2D g2 = (Graphics2D) g;

            Color bg;
            if (!isEnabled()) {
                bg = UITheme.BORDER_COLOR;
            } else if (isPressed) {
                bg = pressedColor;
            } else if (isHovered) {
                bg = hoverColor;
            } else {
                bg = normalColor;
            }

            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));

            super.paintComponent(g);
        }
    }

    public static JButton createPrimaryButton(String text) {
        return new StyledButton(text, UITheme.ACCENT_PRIMARY, UITheme.ACCENT_HOVER);
    }

    public static JButton createBuyButton(String text) {
        return new StyledButton(text, UITheme.COLOR_GAIN, new Color(5, 150, 105));
    }

    public static JButton createSellButton(String text) {
        return new StyledButton(text, UITheme.COLOR_LOSS, new Color(220, 38, 38));
    }

    public static JButton createSecondaryButton(String text) {
        StyledButton btn = new StyledButton(text, UITheme.BG_CARD_HOVER, UITheme.BORDER_LIGHT);
        btn.setForeground(UITheme.TEXT_PRIMARY);
        return btn;
    }

    /**
     * Modern text field with sleek border and dark background.
     */
    public static JTextField createModernTextField(int columns) {
        JTextField tf = new JTextField(columns) {
            @Override
            protected void paintComponent(Graphics g) {
                UITheme.enableAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(UITheme.BG_INPUT);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                UITheme.enableAntiAliasing(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(hasFocus() ? UITheme.ACCENT_PRIMARY : UITheme.BORDER_COLOR);
                g2.setStroke(new BasicStroke(hasFocus() ? 1.5f : 1.0f));
                g2.draw(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
            }
        };
        tf.setOpaque(false);
        tf.setBackground(UITheme.BG_INPUT);
        tf.setForeground(UITheme.TEXT_PRIMARY);
        tf.setCaretColor(UITheme.TEXT_PRIMARY);
        tf.setFont(UITheme.FONT_BODY);
        tf.setBorder(new EmptyBorder(8, 10, 8, 10));
        return tf;
    }

    /**
     * Creates a pill badge for percentages, sectors, or statuses.
     */
    public static class PillBadge extends JLabel {
        private Color bgColor;
        private Color textColor;

        public PillBadge(String text, Color bgColor, Color textColor) {
            super(text, SwingConstants.CENTER);
            this.bgColor = bgColor;
            this.textColor = textColor;
            setFont(UITheme.FONT_BODY_BOLD);
            setForeground(textColor);
            setOpaque(false);
            setBorder(new EmptyBorder(4, 10, 4, 10));
        }

        public void updateBadge(String text, Color bgColor, Color textColor) {
            setText(text);
            this.bgColor = bgColor;
            this.textColor = textColor;
            setForeground(textColor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            UITheme.enableAntiAliasing(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
            super.paintComponent(g);
        }
    }

    /**
     * Styles a JTable with dark fintech theme, custom cell renderers, and clean row spacing.
     */
    public static void styleTable(JTable table) {
        table.setBackground(UITheme.BG_CARD);
        table.setForeground(UITheme.TEXT_PRIMARY);
        table.setGridColor(UITheme.BORDER_COLOR);
        table.setRowHeight(34);
        table.setFont(UITheme.FONT_BODY);
        table.setSelectionBackground(UITheme.BG_CARD_HOVER);
        table.setSelectionForeground(UITheme.TEXT_PRIMARY);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(UITheme.BG_DARK);
        header.setForeground(UITheme.TEXT_MUTED);
        header.setFont(UITheme.FONT_BODY_BOLD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Default cell alignment and padding
        DefaultTableCellRenderer defaultRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, 
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : new Color(24, 32, 47));
                }
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, defaultRenderer);
    }
}
