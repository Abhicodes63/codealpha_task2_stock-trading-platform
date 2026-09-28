package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.service.*;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Main Application GUI Window containing navigation tabs, top user/market status bar,
 * live market simulation engine binding, and limit order monitoring.
 */
public class MainFrame extends JFrame implements MarketService.MarketUpdateListener {

    private final MarketService marketService;
    private final TradingService tradingService;
    private final PortfolioService portfolioService;
    private final UserService userService;
    private final PersistenceService persistenceService;

    // Sub-panels
    private MarketPanel marketPanel;
    private PortfolioPanel portfolioPanel;
    private WatchlistPanel watchlistPanel;
    private TransactionHistoryPanel transactionHistoryPanel;
    private NewsFeedPanel newsFeedPanel;

    // Header UI elements
    private JLabel userProfileLabel;
    private ModernComponents.PillBadge cashBalanceBadge;
    private JLabel marketClockLabel;
    private JButton simToggleButton;
    private JTabbedPane tabbedPane;

    public MainFrame(MarketService marketService, TradingService tradingService,
                     PortfolioService portfolioService, UserService userService,
                     PersistenceService persistenceService) {
        super("CodeAlpha Stock Trading Platform — Pro Trader Workstation");
        this.marketService = marketService;
        this.tradingService = tradingService;
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.persistenceService = persistenceService;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);

        initComponents();

        // Listen to market events
        marketService.addListener(this);
        marketService.startSimulation();

        // Save state on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                marketService.stopSimulation();
                userService.saveAll();
                dispose();
                System.exit(0);
            }
        });

        // Market clock timer
        Timer clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
        updateClock();
    }

    private void initComponents() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG_DARK);

        // 1. Top Global Navigation & Status Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UITheme.BG_CARD);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(10, 18, 10, 18)
        ));

        // App Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoIcon = new JLabel("▲");
        logoIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoIcon.setForeground(UITheme.COLOR_GAIN);

        JLabel brandName = new JLabel("CODEALPHA TRADER");
        brandName.setFont(UITheme.FONT_TITLE);
        brandName.setForeground(UITheme.TEXT_PRIMARY);

        ModernComponents.PillBadge liveBadge = new ModernComponents.PillBadge("LIVE SIMULATION", UITheme.COLOR_GAIN_BG, UITheme.COLOR_GAIN);

        brandPanel.add(logoIcon);
        brandPanel.add(brandName);
        brandPanel.add(liveBadge);
        topBar.add(brandPanel, BorderLayout.WEST);

        // Right side: Clock, Cash, User, Actions
        JPanel rightStatusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightStatusPanel.setOpaque(false);

        marketClockLabel = new JLabel("00:00:00 EST");
        marketClockLabel.setFont(UITheme.FONT_MONO);
        marketClockLabel.setForeground(UITheme.TEXT_MUTED);

        userProfileLabel = new JLabel("User: Alex Morgan");
        userProfileLabel.setFont(UITheme.FONT_BODY_BOLD);
        userProfileLabel.setForeground(UITheme.TEXT_PRIMARY);

        cashBalanceBadge = new ModernComponents.PillBadge("Cash: $0.00", new Color(99, 102, 241, 40), UITheme.TEXT_PRIMARY);

        JButton switchUserBtn = ModernComponents.createSecondaryButton("Switch Trader");
        switchUserBtn.setFont(UITheme.FONT_SMALL);
        switchUserBtn.addActionListener(e -> openAuthDialog());

        simToggleButton = ModernComponents.createSecondaryButton("Pause Market");
        simToggleButton.setFont(UITheme.FONT_SMALL);
        simToggleButton.addActionListener(e -> toggleSimulation());

        rightStatusPanel.add(marketClockLabel);
        rightStatusPanel.add(new JSeparator(JSeparator.VERTICAL));
        rightStatusPanel.add(userProfileLabel);
        rightStatusPanel.add(cashBalanceBadge);
        rightStatusPanel.add(switchUserBtn);
        rightStatusPanel.add(simToggleButton);

        topBar.add(rightStatusPanel, BorderLayout.EAST);
        rootPanel.add(topBar, BorderLayout.NORTH);

        // 2. Central Tabbed Workspace
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UITheme.BG_DARK);
        tabbedPane.setForeground(UITheme.TEXT_PRIMARY);
        tabbedPane.setFont(UITheme.FONT_BODY_BOLD);

        marketPanel = new MarketPanel(marketService, tradingService, userService, this::refreshAllViews);
        portfolioPanel = new PortfolioPanel(marketService, tradingService, portfolioService, userService, persistenceService, this::refreshAllViews);
        watchlistPanel = new WatchlistPanel(marketService, tradingService, userService, this::refreshAllViews);
        transactionHistoryPanel = new TransactionHistoryPanel(userService, persistenceService);
        newsFeedPanel = new NewsFeedPanel(marketService);

        tabbedPane.addTab("  Live Market & Trading  ", marketPanel);
        tabbedPane.addTab("  Portfolio Dashboard  ", portfolioPanel);
        tabbedPane.addTab("  Watchlist  ", watchlistPanel);
        tabbedPane.addTab("  Orders & Transactions  ", transactionHistoryPanel);
        tabbedPane.addTab("  Financial News Wire  ", newsFeedPanel);

        rootPanel.add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(rootPanel);

        updateHeaderUserData();
    }

    private void updateClock() {
        marketClockLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " MARKET CLOCK");
    }

    private void toggleSimulation() {
        if (marketService.isRunning()) {
            marketService.stopSimulation();
            simToggleButton.setText("Resume Market");
            simToggleButton.setForeground(UITheme.COLOR_GAIN);
        } else {
            marketService.startSimulation();
            simToggleButton.setText("Pause Market");
            simToggleButton.setForeground(UITheme.TEXT_PRIMARY);
        }
    }

    private void openAuthDialog() {
        LoginRegisterDialog d = new LoginRegisterDialog(this, userService, this::refreshAllViews);
        d.setVisible(true);
    }

    public synchronized void refreshAllViews() {
        updateHeaderUserData();
        if (marketPanel != null) marketPanel.refreshMarketData();
        if (portfolioPanel != null) portfolioPanel.refreshPortfolio();
        if (watchlistPanel != null) watchlistPanel.refreshWatchlist();
        if (transactionHistoryPanel != null) transactionHistoryPanel.refreshTransactions();
        if (newsFeedPanel != null) newsFeedPanel.refreshNews();
    }

    private void updateHeaderUserData() {
        User u = userService.getCurrentUser();
        if (u != null) {
            userProfileLabel.setText("Trader: " + u.getFullName());
            cashBalanceBadge.updateBadge("Cash: " + CurrencyFormatter.formatCurrency(u.getPortfolio().getCashBalance()),
                    new Color(99, 102, 241, 40), UITheme.TEXT_PRIMARY);
        }
    }

    @Override
    public void onMarketTick(Map<String, Stock> stocks) {
        SwingUtilities.invokeLater(() -> {
            // Check limit/stop-loss orders for current user
            User u = userService.getCurrentUser();
            if (u != null) {
                List<String> orderNotifications = tradingService.processPendingOrders(u);
                for (String note : orderNotifications) {
                    userService.saveAll();
                    JOptionPane.showMessageDialog(this, note, "Order Triggered & Executed", JOptionPane.INFORMATION_MESSAGE);
                }
            }

            if (marketPanel != null) marketPanel.refreshMarketData();
            if (portfolioPanel != null) portfolioPanel.refreshPortfolio();
            if (watchlistPanel != null) watchlistPanel.refreshWatchlist();
        });
    }

    @Override
    public void onNewsPublished(com.codealpha.stocktrading.model.MarketNews news) {
        SwingUtilities.invokeLater(() -> {
            if (newsFeedPanel != null) newsFeedPanel.refreshNews();
        });
    }
}
