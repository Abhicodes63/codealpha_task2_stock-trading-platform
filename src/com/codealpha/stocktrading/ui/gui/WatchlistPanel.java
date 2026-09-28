package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.service.MarketService;
import com.codealpha.stocktrading.service.TradingService;
import com.codealpha.stocktrading.service.UserService;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;

/**
 * Watchlist view to track favorite stocks and execute rapid trades.
 */
public class WatchlistPanel extends JPanel {

    private final MarketService marketService;
    private final TradingService tradingService;
    private final UserService userService;
    private final Runnable onDataChanged;

    private JTable watchlistTable;
    private DefaultTableModel tableModel;

    private final String[] columns = {
            "Symbol", "Company", "Sector", "Current Price", "Change ($)", "Change (%)", "Volume", "Market Cap"
    };

    public WatchlistPanel(MarketService marketService, TradingService tradingService,
                          UserService userService, Runnable onDataChanged) {
        this.marketService = marketService;
        this.tradingService = tradingService;
        this.userService = userService;
        this.onDataChanged = onDataChanged;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        initComponents();
        refreshWatchlist();
    }

    private void initComponents() {
        // Header Bar
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("STARRED WATCHLIST ASSETS");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.TEXT_SECONDARY);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton buyBtn = ModernComponents.createBuyButton("Quick Buy");
        buyBtn.addActionListener(e -> tradeSelected(true));

        JButton sellBtn = ModernComponents.createSellButton("Quick Sell");
        sellBtn.addActionListener(e -> tradeSelected(false));

        JButton removeBtn = ModernComponents.createSecondaryButton("Remove from Watchlist");
        removeBtn.addActionListener(e -> removeSelected());

        btnPanel.add(buyBtn);
        btnPanel.add(sellBtn);
        btnPanel.add(removeBtn);

        header.add(title, BorderLayout.WEST);
        header.add(btnPanel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        watchlistTable = new JTable(tableModel);
        ModernComponents.styleTable(watchlistTable);
        setupRenderers();

        JScrollPane scrollPane = new JScrollPane(watchlistTable);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupRenderers() {
        DefaultTableCellRenderer changeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("+")) c.setForeground(UITheme.COLOR_GAIN);
                    else if (str.startsWith("-")) c.setForeground(UITheme.COLOR_LOSS);
                    else c.setForeground(UITheme.TEXT_PRIMARY);
                }
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : new Color(24, 32, 47));
                }
                return c;
            }
        };

        watchlistTable.getColumnModel().getColumn(4).setCellRenderer(changeRenderer);
        watchlistTable.getColumnModel().getColumn(5).setCellRenderer(changeRenderer);
    }

    public synchronized void refreshWatchlist() {
        User user = userService.getCurrentUser();
        if (user == null) return;

        Set<String> watchlist = user.getWatchlist();
        tableModel.setRowCount(0);

        for (String symbol : watchlist) {
            Stock s = marketService.getStock(symbol);
            if (s != null) {
                tableModel.addRow(new Object[]{
                        s.getSymbol(),
                        s.getCompanyName(),
                        s.getSector().getDisplayName(),
                        CurrencyFormatter.formatCurrency(s.getCurrentPrice()),
                        (s.getPriceChange() >= 0 ? "+" : "") + CurrencyFormatter.formatDecimal(s.getPriceChange()),
                        CurrencyFormatter.formatPercent(s.getPriceChangePercent()),
                        CurrencyFormatter.formatVolume(s.getVolume()),
                        CurrencyFormatter.formatMarketCap(s.getMarketCapBillions())
                });
            }
        }
    }

    private void tradeSelected(boolean isBuy) {
        int row = watchlistTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a stock from your watchlist first.", "Select Stock", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String symbol = (String) tableModel.getValueAt(row, 0);
        Stock s = marketService.getStock(symbol);
        if (s == null) return;

        User u = userService.getCurrentUser();
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TradeDialog dialog = new TradeDialog(topFrame, s, u, tradingService, isBuy, () -> {
            userService.saveAll();
            refreshWatchlist();
            if (onDataChanged != null) onDataChanged.run();
        });
        dialog.setVisible(true);
    }

    private void removeSelected() {
        int row = watchlistTable.getSelectedRow();
        if (row < 0) return;

        String symbol = (String) tableModel.getValueAt(row, 0);
        User u = userService.getCurrentUser();
        if (u != null) {
            u.toggleWatchlist(symbol);
            userService.saveAll();
            refreshWatchlist();
            if (onDataChanged != null) onDataChanged.run();
        }
    }
}
