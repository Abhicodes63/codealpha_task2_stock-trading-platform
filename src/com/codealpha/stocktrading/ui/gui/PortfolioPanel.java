package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.Holding;
import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.service.MarketService;
import com.codealpha.stocktrading.service.PersistenceService;
import com.codealpha.stocktrading.service.PortfolioService;
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
import java.util.Map;

/**
 * Portfolio Dashboard panel providing detailed asset overview, performance analytics,
 * active positions table, cash management, and report export.
 */
public class PortfolioPanel extends JPanel {

    private final MarketService marketService;
    private final TradingService tradingService;
    private final PortfolioService portfolioService;
    private final UserService userService;
    private final PersistenceService persistenceService;
    private final Runnable onRefreshRequested;

    // KPI Stat Cards
    private ModernComponents.StatCard totalValueCard;
    private ModernComponents.StatCard cashCard;
    private ModernComponents.StatCard stockEquityCard;
    private ModernComponents.StatCard unrealizedPLCard;
    private ModernComponents.StatCard realizedPLCard;
    private ModernComponents.StatCard scoreCard;

    private JTable holdingsTable;
    private DefaultTableModel tableModel;

    private final String[] columns = {
            "Symbol", "Company", "Shares", "Avg Buy Price", "Current Price", "Market Value", "Cost Basis", "Unrealized P/L", "Return (%)"
    };

    public PortfolioPanel(MarketService marketService, TradingService tradingService,
                          PortfolioService portfolioService, UserService userService,
                          PersistenceService persistenceService, Runnable onRefreshRequested) {
        this.marketService = marketService;
        this.tradingService = tradingService;
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.persistenceService = persistenceService;
        this.onRefreshRequested = onRefreshRequested;

        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        initComponents();
        refreshPortfolio();
    }

    private void initComponents() {
        // Top KPI Cards Section (Grid 6 cards)
        JPanel statsGrid = new JPanel(new GridLayout(1, 6, 10, 10));
        statsGrid.setOpaque(false);

        totalValueCard = new ModernComponents.StatCard("Net Worth", "$0.00", "Total Portfolio");
        cashCard = new ModernComponents.StatCard("Cash Balance", "$0.00", "Available Liquidity");
        stockEquityCard = new ModernComponents.StatCard("Stock Equity", "$0.00", "Active Holdings");
        unrealizedPLCard = new ModernComponents.StatCard("Unrealized P/L", "$0.00", "Open Positions");
        realizedPLCard = new ModernComponents.StatCard("Realized P/L", "$0.00", "Closed Trades");
        scoreCard = new ModernComponents.StatCard("Diversification", "0 / 100", "Risk Spread");

        statsGrid.add(totalValueCard);
        statsGrid.add(cashCard);
        statsGrid.add(stockEquityCard);
        statsGrid.add(unrealizedPLCard);
        statsGrid.add(realizedPLCard);
        statsGrid.add(scoreCard);

        add(statsGrid, BorderLayout.NORTH);

        // Center Holdings Section
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Title + Quick Actions Header
        JPanel sectionHeader = new JPanel(new BorderLayout());
        sectionHeader.setOpaque(false);

        JLabel sectionTitle = new JLabel("ACTIVE ASSET HOLDINGS & POSITIONS");
        sectionTitle.setFont(UITheme.FONT_SUBHEADER);
        sectionTitle.setForeground(UITheme.TEXT_SECONDARY);

        JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonBar.setOpaque(false);

        JButton depositBtn = ModernComponents.createBuyButton("+ Deposit Cash");
        depositBtn.addActionListener(e -> openDepositWithdrawDialog(true));

        JButton withdrawBtn = ModernComponents.createSecondaryButton("- Withdraw Cash");
        withdrawBtn.addActionListener(e -> openDepositWithdrawDialog(false));

        JButton tradeSelectedBtn = ModernComponents.createPrimaryButton("Trade Position");
        tradeSelectedBtn.addActionListener(e -> tradeSelectedPosition());

        JButton exportBtn = ModernComponents.createSecondaryButton("Export Report (.txt)");
        exportBtn.addActionListener(e -> exportReport());

        buttonBar.add(depositBtn);
        buttonBar.add(withdrawBtn);
        buttonBar.add(tradeSelectedBtn);
        buttonBar.add(exportBtn);

        sectionHeader.add(sectionTitle, BorderLayout.WEST);
        sectionHeader.add(buttonBar, BorderLayout.EAST);
        centerPanel.add(sectionHeader, BorderLayout.NORTH);

        // Holdings Table
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        holdingsTable = new JTable(tableModel);
        ModernComponents.styleTable(holdingsTable);
        setupCustomRenderers();

        JScrollPane scrollPane = new JScrollPane(holdingsTable);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    private void setupCustomRenderers() {
        DefaultTableCellRenderer plRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("+")) {
                        c.setForeground(UITheme.COLOR_GAIN);
                    } else if (str.startsWith("-")) {
                        c.setForeground(UITheme.COLOR_LOSS);
                    } else {
                        c.setForeground(UITheme.TEXT_PRIMARY);
                    }
                }
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : new Color(24, 32, 47));
                }
                return c;
            }
        };

        holdingsTable.getColumnModel().getColumn(7).setCellRenderer(plRenderer);
        holdingsTable.getColumnModel().getColumn(8).setCellRenderer(plRenderer);
    }

    public synchronized void refreshPortfolio() {
        User user = userService.getCurrentUser();
        if (user == null) return;

        PortfolioService.PortfolioSummary summary = portfolioService.getPortfolioSummary(user);
        Map<String, Stock> market = marketService.getStocks();

        // Update KPI Cards
        totalValueCard.setValue(CurrencyFormatter.formatCurrency(summary.getTotalPortfolioValue()), UITheme.TEXT_PRIMARY);
        cashCard.setValue(CurrencyFormatter.formatCurrency(summary.getCashBalance()), UITheme.TEXT_PRIMARY);
        stockEquityCard.setValue(CurrencyFormatter.formatCurrency(summary.getHoldingsValue()), UITheme.TEXT_PRIMARY);

        Color plColor = summary.getTotalUnrealizedPL() >= 0 ? UITheme.COLOR_GAIN : UITheme.COLOR_LOSS;
        unrealizedPLCard.setValue((summary.getTotalUnrealizedPL() >= 0 ? "+" : "") + 
                CurrencyFormatter.formatCurrency(summary.getTotalUnrealizedPL()), plColor);
        unrealizedPLCard.setSubtext("Return: " + CurrencyFormatter.formatPercent(summary.getTotalUnrealizedPLPercent()), plColor);

        Color rplColor = summary.getTotalRealizedPL() >= 0 ? UITheme.COLOR_GAIN : UITheme.COLOR_LOSS;
        realizedPLCard.setValue((summary.getTotalRealizedPL() >= 0 ? "+" : "") + 
                CurrencyFormatter.formatCurrency(summary.getTotalRealizedPL()), rplColor);

        scoreCard.setValue(summary.getDiversificationScore() + " / 100", 
                summary.getDiversificationScore() >= 60 ? UITheme.COLOR_GAIN : UITheme.COLOR_WARNING);

        // Update Holdings Table
        tableModel.setRowCount(0);
        for (Holding h : user.getPortfolio().getHoldings().values()) {
            Stock s = market.get(h.getStockSymbol());
            String company = s != null ? s.getCompanyName() : "N/A";
            double curPrice = s != null ? s.getCurrentPrice() : h.getAverageBuyPrice();
            double marketVal = h.getCurrentMarketValue(curPrice);
            double costBasis = h.getTotalCostBasis();
            double unrealizedPL = h.getUnrealizedProfitLoss(curPrice);
            double plPercent = h.getUnrealizedProfitLossPercent(curPrice);

            tableModel.addRow(new Object[]{
                    h.getStockSymbol(),
                    company,
                    h.getQuantity(),
                    CurrencyFormatter.formatCurrency(h.getAverageBuyPrice()),
                    CurrencyFormatter.formatCurrency(curPrice),
                    CurrencyFormatter.formatCurrency(marketVal),
                    CurrencyFormatter.formatCurrency(costBasis),
                    (unrealizedPL >= 0 ? "+" : "") + CurrencyFormatter.formatCurrency(unrealizedPL),
                    CurrencyFormatter.formatPercent(plPercent)
            });
        }
    }

    private void openDepositWithdrawDialog(boolean isDeposit) {
        User u = userService.getCurrentUser();
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        DepositWithdrawDialog d = new DepositWithdrawDialog(topFrame, u, tradingService, isDeposit, () -> {
            userService.saveAll();
            refreshPortfolio();
            if (onRefreshRequested != null) onRefreshRequested.run();
        });
        d.setVisible(true);
    }

    private void tradeSelectedPosition() {
        int row = holdingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a holding from the table to trade.", "Select Position", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String symbol = (String) tableModel.getValueAt(row, 0);
        Stock s = marketService.getStock(symbol);
        if (s == null) return;

        User u = userService.getCurrentUser();
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TradeDialog dialog = new TradeDialog(topFrame, s, u, tradingService, false, () -> {
            userService.saveAll();
            refreshPortfolio();
            if (onRefreshRequested != null) onRefreshRequested.run();
        });
        dialog.setVisible(true);
    }

    private void exportReport() {
        User user = userService.getCurrentUser();
        if (user == null) return;

        String report = portfolioService.generatePortfolioReport(user);
        String filename = "Portfolio_Report_" + user.getUsername() + "_" + System.currentTimeMillis() + ".txt";

        if (persistenceService.exportReportToFile(filename, report)) {
            JOptionPane.showMessageDialog(this, 
                    "Portfolio report successfully exported to:\ndata/" + filename, 
                    "Report Exported", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to export report.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
