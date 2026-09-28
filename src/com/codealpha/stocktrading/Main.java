package com.codealpha.stocktrading;

import com.codealpha.stocktrading.service.*;
import com.codealpha.stocktrading.ui.cli.StockTradingCLI;
import com.codealpha.stocktrading.ui.gui.MainFrame;

import javax.swing.*;

/**
 * Main application entry point for the CodeAlpha Stock Trading Platform.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize core backend services
        PersistenceService persistenceService = new PersistenceService();
        UserService userService = new UserService(persistenceService);
        MarketService marketService = new MarketService();
        TradingService tradingService = new TradingService(marketService);
        PortfolioService portfolioService = new PortfolioService(marketService);

        // Check if user specified CLI mode via command line arguments
        boolean isCliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg)) {
                isCliMode = true;
                break;
            }
        }

        if (isCliMode) {
            StockTradingCLI cli = new StockTradingCLI(
                    marketService, tradingService, portfolioService, userService, persistenceService
            );
            cli.start();
        } else {
            // Launch Modern Swing GUI
            SwingUtilities.invokeLater(() -> {
                try {
                    // Set System Look and Feel if applicable
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (Exception ignored) {}

                MainFrame frame = new MainFrame(
                        marketService, tradingService, portfolioService, userService, persistenceService
                );
                frame.setVisible(true);
            });
        }
    }
}
