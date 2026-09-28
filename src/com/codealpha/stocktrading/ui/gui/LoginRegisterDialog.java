package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.service.UserService;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for logging into an existing account or registering a new trading account.
 */
public class LoginRegisterDialog extends JDialog {

    private final UserService userService;
    private final Runnable onLoginSuccess;

    private JTextField loginUserField;
    private JPasswordField loginPassField;
    private JTextField regUserField;
    private JTextField regNameField;
    private JPasswordField regPassField;
    private JTextField regInitialCashField;

    public LoginRegisterDialog(Frame parent, UserService userService, Runnable onLoginSuccess) {
        super(parent, "Account Authentication & Switcher", true);
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;

        setSize(460, 480);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);

        initComponents();
    }

    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UITheme.BG_DARK);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);

        tabbedPane.addTab("Sign In", createLoginPanel());
        tabbedPane.addTab("Register New Trader", createRegisterPanel());

        getContentPane().add(tabbedPane);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        ModernComponents.ModernCard card = new ModernComponents.ModernCard();
        card.setLayout(new GridLayout(4, 1, 8, 8));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(UITheme.TEXT_SECONDARY);
        loginUserField = ModernComponents.createModernTextField(15);
        loginUserField.setText("trader");

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(UITheme.TEXT_SECONDARY);
        loginPassField = new JPasswordField();
        loginPassField.setBackground(UITheme.BG_INPUT);
        loginPassField.setForeground(UITheme.TEXT_PRIMARY);
        loginPassField.setCaretColor(UITheme.TEXT_PRIMARY);
        loginPassField.setText("password123");
        loginPassField.setBorder(new EmptyBorder(8, 10, 8, 10));

        card.add(userLabel);
        card.add(loginUserField);
        card.add(passLabel);
        card.add(loginPassField);
        panel.add(card, BorderLayout.CENTER);

        JButton loginBtn = ModernComponents.createPrimaryButton("Sign In to Account");
        loginBtn.addActionListener(e -> handleLogin());
        panel.add(loginBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        ModernComponents.ModernCard card = new ModernComponents.ModernCard();
        card.setLayout(new GridLayout(8, 1, 4, 4));

        regUserField = ModernComponents.createModernTextField(15);
        regNameField = ModernComponents.createModernTextField(15);
        regPassField = new JPasswordField();
        regPassField.setBackground(UITheme.BG_INPUT);
        regPassField.setForeground(UITheme.TEXT_PRIMARY);
        regPassField.setBorder(new EmptyBorder(8, 10, 8, 10));

        regInitialCashField = ModernComponents.createModernTextField(15);
        regInitialCashField.setText("50000.00");

        card.add(createFieldLabel("Username:"));
        card.add(regUserField);
        card.add(createFieldLabel("Full Name:"));
        card.add(regNameField);
        card.add(createFieldLabel("Password:"));
        card.add(regPassField);
        card.add(createFieldLabel("Initial Demo Funding ($):"));
        card.add(regInitialCashField);

        panel.add(card, BorderLayout.CENTER);

        JButton regBtn = ModernComponents.createBuyButton("Create Account");
        regBtn.addActionListener(e -> handleRegister());
        panel.add(regBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_SECONDARY);
        return l;
    }

    private void handleLogin() {
        String username = loginUserField.getText().trim();
        String pass = new String(loginPassField.getPassword()).trim();

        if (userService.authenticate(username, pass)) {
            JOptionPane.showMessageDialog(this, "Welcome back, " + userService.getCurrentUser().getFullName() + "!", 
                    "Signed In", JOptionPane.INFORMATION_MESSAGE);
            if (onLoginSuccess != null) onLoginSuccess.run();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String username = regUserField.getText().trim();
        String name = regNameField.getText().trim();
        String pass = new String(regPassField.getPassword()).trim();

        if (username.isEmpty() || name.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Incomplete Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            double initialCash = Double.parseDouble(regInitialCashField.getText().trim());
            if (userService.registerUser(username, name, pass, initialCash)) {
                JOptionPane.showMessageDialog(this, "Account created successfully! Welcome, " + name + "!", 
                        "Registration Complete", JOptionPane.INFORMATION_MESSAGE);
                if (onLoginSuccess != null) onLoginSuccess.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username already taken. Please choose another.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric starting cash amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
