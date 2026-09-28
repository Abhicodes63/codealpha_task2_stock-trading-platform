package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.*;
import com.codealpha.stocktrading.service.TradingService;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * Modern modal dialog for executing Buy/Sell market orders and Limit/Stop-Loss orders.
 */
public class TradeDialog extends JDialog {

    private final Stock stock;
    private final User user;
    private final TradingService tradingService;
    private final Runnable onSuccessCallback;

    private JRadioButton buyRadio;
    private JRadioButton sellRadio;
    private JComboBox<OrderType> orderTypeCombo;
    private JTextField quantityField;
    private JTextField targetPriceField;
    private JLabel totalEstimateLabel;
    private JLabel balanceInfoLabel;
    private JLabel targetPriceLabel;
    private JButton submitButton;

    public TradeDialog(Frame parent, Stock stock, User user, TradingService tradingService, 
                       boolean defaultIsBuy, Runnable onSuccessCallback) {
        super(parent, "Trade " + stock.getSymbol() + " — " + stock.getCompanyName(), true);
        this.stock = stock;
        this.user = user;
        this.tradingService = tradingService;
        this.onSuccessCallback = onSuccessCallback;

        setSize(480, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(UITheme.BG_DARK);

        initComponents(defaultIsBuy);
        updateCalculations();
    }

    private void initComponents(boolean defaultIsBuy) {
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(UITheme.BG_DARK);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(stock.getSymbol() + " - " + stock.getCompanyName());
        titleLabel.setFont(UITheme.FONT_HEADER);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel priceLabel = new JLabel("Current Price: " + CurrencyFormatter.formatCurrency(stock.getCurrentPrice()) + 
                " (" + CurrencyFormatter.formatPercent(stock.getPriceChangePercent()) + ")");
        priceLabel.setFont(UITheme.FONT_BODY_BOLD);
        priceLabel.setForeground(stock.getPriceChange() >= 0 ? UITheme.COLOR_GAIN : UITheme.COLOR_LOSS);

        headerPanel.add(titleLabel);
        headerPanel.add(priceLabel);
        contentPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Card
        ModernComponents.ModernCard formCard = new ModernComponents.ModernCard();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1.0;

        // Trade Side (Buy / Sell toggle)
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JPanel sidePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        sidePanel.setOpaque(false);

        buyRadio = new JRadioButton("BUY (Go Long)", defaultIsBuy);
        buyRadio.setFont(UITheme.FONT_BODY_BOLD);
        buyRadio.setForeground(UITheme.COLOR_GAIN);
        buyRadio.setOpaque(false);

        sellRadio = new JRadioButton("SELL (Liquidate)", !defaultIsBuy);
        sellRadio.setFont(UITheme.FONT_BODY_BOLD);
        sellRadio.setForeground(UITheme.COLOR_LOSS);
        sellRadio.setOpaque(false);

        ButtonGroup group = new ButtonGroup();
        group.add(buyRadio);
        group.add(sellRadio);
        sidePanel.add(buyRadio);
        sidePanel.add(sellRadio);
        formCard.add(sidePanel, gbc);

        // Order Type
        gbc.gridy = 1; gbc.gridwidth = 1; gbc.gridx = 0;
        JLabel typeLabel = new JLabel("Order Type:");
        typeLabel.setFont(UITheme.FONT_BODY);
        typeLabel.setForeground(UITheme.TEXT_SECONDARY);
        formCard.add(typeLabel, gbc);

        gbc.gridx = 1;
        orderTypeCombo = new JComboBox<>(OrderType.values());
        orderTypeCombo.setBackground(UITheme.BG_INPUT);
        orderTypeCombo.setForeground(UITheme.TEXT_PRIMARY);
        orderTypeCombo.setFont(UITheme.FONT_BODY);
        formCard.add(orderTypeCombo, gbc);

        // Quantity Field
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel qtyLabel = new JLabel("Shares Quantity:");
        qtyLabel.setFont(UITheme.FONT_BODY);
        qtyLabel.setForeground(UITheme.TEXT_SECONDARY);
        formCard.add(qtyLabel, gbc);

        gbc.gridx = 1;
        quantityField = ModernComponents.createModernTextField(10);
        quantityField.setText("10");
        formCard.add(quantityField, gbc);

        // Quick Quantity Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel quickQtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quickQtyPanel.setOpaque(false);
        int[] presets = {1, 5, 10, 25, 50, 100};
        for (int p : presets) {
            JButton qBtn = ModernComponents.createSecondaryButton(String.valueOf(p));
            qBtn.setFont(UITheme.FONT_SMALL);
            qBtn.addActionListener(e -> {
                quantityField.setText(String.valueOf(p));
                updateCalculations();
            });
            quickQtyPanel.add(qBtn);
        }
        formCard.add(quickQtyPanel, gbc);

        // Target Price Field (for Limit/Stop Loss)
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        targetPriceLabel = new JLabel("Trigger / Target Price ($):");
        targetPriceLabel.setFont(UITheme.FONT_BODY);
        targetPriceLabel.setForeground(UITheme.TEXT_SECONDARY);
        formCard.add(targetPriceLabel, gbc);

        gbc.gridx = 1;
        targetPriceField = ModernComponents.createModernTextField(10);
        targetPriceField.setText(String.format("%.2f", stock.getCurrentPrice()));
        formCard.add(targetPriceField, gbc);

        // Calculations & Account Status
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JPanel summaryBox = new JPanel(new GridLayout(2, 1, 4, 4));
        summaryBox.setOpaque(false);

        balanceInfoLabel = new JLabel("Cash Available: " + CurrencyFormatter.formatCurrency(user.getPortfolio().getCashBalance()));
        balanceInfoLabel.setFont(UITheme.FONT_BODY);
        balanceInfoLabel.setForeground(UITheme.TEXT_SECONDARY);

        totalEstimateLabel = new JLabel("Estimated Total: $0.00");
        totalEstimateLabel.setFont(UITheme.FONT_BODY_BOLD);
        totalEstimateLabel.setForeground(UITheme.TEXT_PRIMARY);

        summaryBox.add(balanceInfoLabel);
        summaryBox.add(totalEstimateLabel);
        formCard.add(summaryBox, gbc);

        contentPanel.add(formCard, BorderLayout.CENTER);

        // Actions Panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton cancelButton = ModernComponents.createSecondaryButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        submitButton = ModernComponents.createBuyButton("Execute Buy Order");
        submitButton.addActionListener(e -> executeTradeAction());

        actionPanel.add(cancelButton);
        actionPanel.add(submitButton);
        contentPanel.add(actionPanel, BorderLayout.SOUTH);

        getContentPane().add(contentPanel);

        // Event listeners for recalculation
        buyRadio.addActionListener(e -> onSideChanged());
        sellRadio.addActionListener(e -> onSideChanged());
        orderTypeCombo.addActionListener(e -> onOrderTypeChanged());
        quantityField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateCalculations));
        targetPriceField.getDocument().addDocumentListener(new SimpleDocumentListener(this::updateCalculations));

        onSideChanged();
        onOrderTypeChanged();
    }

    private void onSideChanged() {
        boolean isBuy = buyRadio.isSelected();
        submitButton.setText(isBuy ? "Execute Buy Order" : "Execute Sell Order");
        if (isBuy) {
            submitButton.setBackground(UITheme.COLOR_GAIN);
        } else {
            submitButton.setBackground(UITheme.COLOR_LOSS);
        }
        updateCalculations();
    }

    private void onOrderTypeChanged() {
        OrderType ot = (OrderType) orderTypeCombo.getSelectedItem();
        boolean needsPrice = ot != OrderType.MARKET;
        targetPriceLabel.setVisible(needsPrice);
        targetPriceField.setVisible(needsPrice);
        updateCalculations();
    }

    private void updateCalculations() {
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            OrderType ot = (OrderType) orderTypeCombo.getSelectedItem();
            double price = (ot != OrderType.MARKET) ? 
                    Double.parseDouble(targetPriceField.getText().trim()) : stock.getCurrentPrice();

            double total = qty * price;
            totalEstimateLabel.setText("Estimated Total: " + CurrencyFormatter.formatCurrency(total));

            boolean isBuy = buyRadio.isSelected();
            if (isBuy) {
                double avail = user.getPortfolio().getCashBalance();
                balanceInfoLabel.setText(String.format("Cash Available: %s | Post-Trade: %s", 
                        CurrencyFormatter.formatCurrency(avail),
                        CurrencyFormatter.formatCurrency(avail - total)));
                submitButton.setEnabled(total <= avail && qty > 0);
            } else {
                int owned = user.getPortfolio().getSharesOwned(stock.getSymbol());
                balanceInfoLabel.setText(String.format("Shares Owned: %d | Post-Trade: %d", owned, owned - qty));
                submitButton.setEnabled(qty > 0 && qty <= owned);
            }
        } catch (Exception ex) {
            totalEstimateLabel.setText("Estimated Total: —");
            submitButton.setEnabled(false);
        }
    }

    private void executeTradeAction() {
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            OrderType ot = (OrderType) orderTypeCombo.getSelectedItem();
            boolean isBuy = buyRadio.isSelected();
            TransactionType side = isBuy ? TransactionType.BUY : TransactionType.SELL;

            double targetPrice = stock.getCurrentPrice();
            if (ot != OrderType.MARKET) {
                targetPrice = Double.parseDouble(targetPriceField.getText().trim());
            }

            TradingService.TradeResult res = tradingService.placeOrder(user, stock.getSymbol(), ot, side, qty, targetPrice);

            if (res.isSuccess()) {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Order Successful", JOptionPane.INFORMATION_MESSAGE);
                if (onSuccessCallback != null) onSuccessCallback.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, res.getMessage(), "Trade Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numeric inputs.", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;
        public SimpleDocumentListener(Runnable callback) { this.callback = callback; }
        public void insertUpdate(DocumentEvent e) { callback.run(); }
        public void removeUpdate(DocumentEvent e) { callback.run(); }
        public void changedUpdate(DocumentEvent e) { callback.run(); }
    }
}
