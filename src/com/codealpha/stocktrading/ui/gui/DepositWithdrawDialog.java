package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.service.TradingService;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for depositing or withdrawing funds from the trading account.
 */
public class DepositWithdrawDialog extends JDialog {

    private final User user;
    private final TradingService tradingService;
    private final boolean isDeposit;
    private final Runnable onSuccessCallback;

    private JTextField amountField;
    private JLabel currentBalanceLabel;

    public DepositWithdrawDialog(Frame parent, User user, TradingService tradingService, 
                                 boolean isDeposit, Runnable onSuccessCallback) {
        super(parent, (isDeposit ? "Deposit Cash" : "Withdraw Cash"), true);
        this.user = user;
        this.tradingService = tradingService;
        this.isDeposit = isDeposit;
        this.onSuccessCallback = onSuccessCallback;

        setSize(400, 320);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);

        initComponents();
    }

    private void initComponents() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(UITheme.BG_DARK);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel(isDeposit ? "Deposit Funds to Wallet" : "Withdraw Funds to Bank");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        ModernComponents.ModernCard card = new ModernComponents.ModernCard();
        card.setLayout(new GridLayout(4, 1, 6, 6));

        currentBalanceLabel = new JLabel("Current Cash Balance: " + CurrencyFormatter.formatCurrency(user.getPortfolio().getCashBalance()));
        currentBalanceLabel.setFont(UITheme.FONT_BODY);
        currentBalanceLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel amountLabel = new JLabel("Enter Amount ($):");
        amountLabel.setFont(UITheme.FONT_BODY_BOLD);
        amountLabel.setForeground(UITheme.TEXT_PRIMARY);

        amountField = ModernComponents.createModernTextField(10);
        amountField.setText("1000.00");

        // Quick amount buttons
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quickPanel.setOpaque(false);
        int[] amounts = {500, 1000, 5000, 10000};
        for (int a : amounts) {
            JButton btn = ModernComponents.createSecondaryButton("$" + a);
            btn.setFont(UITheme.FONT_SMALL);
            btn.addActionListener(e -> amountField.setText(String.valueOf(a)));
            quickPanel.add(btn);
        }

        card.add(currentBalanceLabel);
        card.add(amountLabel);
        card.add(amountField);
        card.add(quickPanel);
        panel.add(card, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton cancelBtn = ModernComponents.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JButton confirmBtn = isDeposit ? 
                ModernComponents.createBuyButton("Confirm Deposit") : 
                ModernComponents.createSellButton("Confirm Withdrawal");

        confirmBtn.addActionListener(e -> processTransfer());

        actionPanel.add(cancelBtn);
        actionPanel.add(confirmBtn);
        panel.add(actionPanel, BorderLayout.SOUTH);

        getContentPane().add(panel);
    }

    private void processTransfer() {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());
            TradingService.TradeResult res = isDeposit ? 
                    tradingService.depositFunds(user, amount) : 
                    tradingService.withdrawFunds(user, amount);

            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Success", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Transaction Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid monetary amount.", "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
}
