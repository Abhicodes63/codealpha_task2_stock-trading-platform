package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.Sector;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Live Market Dashboard panel with search, sector filters, interactive quotes table, 
 * live price charts, and quick-action trading controls.
 */
public class MarketPanel extends JPanel {

    private final MarketService marketService;
    private final TradingService tradingService;
    private final UserService userService;
    private final Runnable onPortfolioUpdated;

    private JTable stocksTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> sectorCombo;
    private StockChartPanel chartPanel;
    private JLabel marketIndexLabel;
    private ModernComponents.PillBadge indexBadge;

    private final String[] columns = {
            "Symbol", "Company Name", "Sector", "Price ($)", "Change ($)", "Change (%)", "Volume", "Market Cap", "Fav"
    };

    public MarketPanel(MarketService marketService, TradingService tradingService, 
                       UserService userService, Runnable onPortfolioUpdated) {
        this.marketService = marketService;
        this.tradingService = tradingService;
        this.userService = userService;
        this.onPortfolioUpdated = onPortfolioUpdated;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(12, 12, 12, 12));

        initComponents();
        refreshMarketData();
    }

    private void initComponents() {
        // Top Ticker / Header Banner
        JPanel topBanner = new ModernComponents.ModernCard();
        topBanner.setLayout(new BorderLayout(10, 10));

        JPanel indexPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        indexPanel.setOpaque(false);

        JLabel indexTitle = new JLabel("CODEALPHA COMPOSITE INDEX (ALPHA50):");
        indexTitle.setFont(UITheme.FONT_BODY_BOLD);
        indexTitle.setForeground(UITheme.TEXT_SECONDARY);

        marketIndexLabel = new JLabel("5,420.50");
        marketIndexLabel.setFont(UITheme.FONT_HEADER);
        marketIndexLabel.setForeground(UITheme.TEXT_PRIMARY);

        indexBadge = new ModernComponents.PillBadge("+0.00%", UITheme.COLOR_GAIN_BG, UITheme.COLOR_GAIN);

        indexPanel.add(indexTitle);
        indexPanel.add(marketIndexLabel);
        indexPanel.add(indexBadge);

        // Filter Bar (Search + Sector)
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setForeground(UITheme.TEXT_SECONDARY);
        searchField = ModernComponents.createModernTextField(12);
        searchField.setToolTipText("Filter by symbol or company name...");

        sectorCombo = new JComboBox<>();
        sectorCombo.setBackground(UITheme.BG_INPUT);
        sectorCombo.setForeground(UITheme.TEXT_PRIMARY);
        sectorCombo.addItem("All Sectors");
        for (Sector s : Sector.values()) {
            sectorCombo.addItem(s.getDisplayName());
        }

        filterPanel.add(searchLbl);
        filterPanel.add(searchField);
        filterPanel.add(sectorCombo);

        topBanner.add(indexPanel, BorderLayout.WEST);
        topBanner.add(filterPanel, BorderLayout.EAST);
        add(topBanner, BorderLayout.NORTH);

        // Center Split View: Quotes Table (Top) + Interactive Chart (Bottom)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);
        splitPane.setResizeWeight(0.60);

        // Table Setup
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        stocksTable = new JTable(tableModel);
        ModernComponents.styleTable(stocksTable);
        setupCustomRenderers();

        JScrollPane scrollPane = new JScrollPane(stocksTable);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        splitPane.setTopComponent(scrollPane);

        // Chart & Quick Action Panel (Bottom Component)
        JPanel bottomContainer = new JPanel(new BorderLayout(10, 10));
        bottomContainer.setOpaque(false);

        chartPanel = new StockChartPanel();
        bottomContainer.add(chartPanel, BorderLayout.CENTER);

        // Side Trade Quick-Actions
        ModernComponents.ModernCard actionCard = new ModernComponents.ModernCard();
        actionCard.setLayout(new GridLayout(4, 1, 8, 8));
        actionCard.setPreferredSize(new Dimension(160, 180));

        JButton buyBtn = ModernComponents.createBuyButton("BUY STOCK");
        buyBtn.addActionListener(e -> openTradeDialog(true));

        JButton sellBtn = ModernComponents.createSellButton("SELL STOCK");
        sellBtn.addActionListener(e -> openTradeDialog(false));

        JButton watchBtn = ModernComponents.createSecondaryButton("Toggle Watchlist");
        watchBtn.addActionListener(e -> toggleWatchlistSelected());

        JButton simTickBtn = ModernComponents.createSecondaryButton("Market Tick");
        simTickBtn.addActionListener(e -> marketService.tickMarket());

        actionCard.add(buyBtn);
        actionCard.add(sellBtn);
        actionCard.add(watchBtn);
        actionCard.add(simTickBtn);

        bottomContainer.add(actionCard, BorderLayout.EAST);
        splitPane.setBottomComponent(bottomContainer);

        add(splitPane, BorderLayout.CENTER);

        // Selection Listener
        stocksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectedStockChart();
            }
        });

        // Search & Filter Listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshMarketData(); }
            public void removeUpdate(DocumentEvent e) { refreshMarketData(); }
            public void changedUpdate(DocumentEvent e) { refreshMarketData(); }
        });

        sectorCombo.addActionListener(e -> refreshMarketData());
    }

    private void setupCustomRenderers() {
        // Price change colored renderer
        DefaultTableCellRenderer changeRenderer = new DefaultTableCellRenderer() {
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

        stocksTable.getColumnModel().getColumn(4).setCellRenderer(changeRenderer);
        stocksTable.getColumnModel().getColumn(5).setCellRenderer(changeRenderer);

        // Column widths
        stocksTable.getColumnModel().getColumn(0).setPreferredWidth(70);
        stocksTable.getColumnModel().getColumn(1).setPreferredWidth(160);
        stocksTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        stocksTable.getColumnModel().getColumn(8).setPreferredWidth(50);
    }

    public synchronized void refreshMarketData() {
        int selectedRow = stocksTable.getSelectedRow();
        String selectedSymbol = null;
        if (selectedRow >= 0 && selectedRow < tableModel.getRowCount()) {
            selectedSymbol = (String) tableModel.getValueAt(selectedRow, 0);
        }

        String query = searchField != null ? searchField.getText().trim() : "";
        Sector sector = null;
        if (sectorCombo != null && sectorCombo.getSelectedIndex() > 0) {
            String selectedName = (String) sectorCombo.getSelectedItem();
            for (Sector s : Sector.values()) {
                if (s.getDisplayName().equals(selectedName)) {
                    sector = s;
                    break;
                }
            }
        }

        List<Stock> stocks = marketService.searchStocks(query, sector);
        tableModel.setRowCount(0);

        User currentUser = userService.getCurrentUser();
        int newSelectedRow = -1;

        for (int i = 0; i < stocks.size(); i++) {
            Stock s = stocks.get(i);
            boolean isFav = currentUser != null && currentUser.isWatchlisted(s.getSymbol());
            tableModel.addRow(new Object[]{
                    s.getSymbol(),
                    s.getCompanyName(),
                    s.getSector().getDisplayName(),
                    CurrencyFormatter.formatDecimal(s.getCurrentPrice()),
                    (s.getPriceChange() >= 0 ? "+" : "") + CurrencyFormatter.formatDecimal(s.getPriceChange()),
                    CurrencyFormatter.formatPercent(s.getPriceChangePercent()),
                    CurrencyFormatter.formatVolume(s.getVolume()),
                    CurrencyFormatter.formatMarketCap(s.getMarketCapBillions()),
                    isFav ? "★" : "☆"
            });

            if (selectedSymbol != null && s.getSymbol().equals(selectedSymbol)) {
                newSelectedRow = i;
            }
        }

        if (newSelectedRow >= 0) {
            stocksTable.setRowSelectionInterval(newSelectedRow, newSelectedRow);
        } else if (tableModel.getRowCount() > 0 && selectedRow == -1) {
            stocksTable.setRowSelectionInterval(0, 0);
        }

        // Update Index Banner
        double idxVal = marketService.getAlphaIndexValue();
        double idxChg = marketService.getAlphaIndexChangePercent();
        marketIndexLabel.setText(CurrencyFormatter.formatDecimal(idxVal));
        if (idxChg >= 0) {
            indexBadge.updateBadge("+" + CurrencyFormatter.formatDecimal(idxChg) + "%", UITheme.COLOR_GAIN_BG, UITheme.COLOR_GAIN);
        } else {
            indexBadge.updateBadge(CurrencyFormatter.formatDecimal(idxChg) + "%", UITheme.COLOR_LOSS_BG, UITheme.COLOR_LOSS);
        }

        updateSelectedStockChart();
    }

    private void updateSelectedStockChart() {
        int row = stocksTable.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            String symbol = (String) tableModel.getValueAt(row, 0);
            Stock s = marketService.getStock(symbol);
            chartPanel.setStock(s);
        }
    }

    private Stock getSelectedStock() {
        int row = stocksTable.getSelectedRow();
        if (row >= 0 && row < tableModel.getRowCount()) {
            String symbol = (String) tableModel.getValueAt(row, 0);
            return marketService.getStock(symbol);
        }
        return null;
    }

    private void openTradeDialog(boolean isBuy) {
        Stock s = getSelectedStock();
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Please select a stock from the table first.", "Select Stock", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        User u = userService.getCurrentUser();
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TradeDialog dialog = new TradeDialog(topFrame, s, u, tradingService, isBuy, () -> {
            userService.saveAll();
            refreshMarketData();
            if (onPortfolioUpdated != null) onPortfolioUpdated.run();
        });
        dialog.setVisible(true);
    }

    private void toggleWatchlistSelected() {
        Stock s = getSelectedStock();
        if (s == null) return;

        User u = userService.getCurrentUser();
        if (u != null) {
            boolean added = u.toggleWatchlist(s.getSymbol());
            userService.saveAll();
            refreshMarketData();
            if (onPortfolioUpdated != null) onPortfolioUpdated.run();
        }
    }
}
