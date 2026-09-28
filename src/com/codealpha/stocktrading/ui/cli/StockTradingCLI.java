package com.codealpha.stocktrading.ui.cli;

import com.codealpha.stocktrading.model.*;
import com.codealpha.stocktrading.service.*;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import java.util.*;

/**
 * Interactive Command Line Interface (CLI) for the Stock Trading Platform.
 */
public class StockTradingCLI {

    private final MarketService marketService;
    private final TradingService tradingService;
    private final PortfolioService portfolioService;
    private final UserService userService;
    private final PersistenceService persistenceService;
    private final Scanner scanner;

    public StockTradingCLI(MarketService marketService, TradingService tradingService,
                           PortfolioService portfolioService, UserService userService,
                           PersistenceService persistenceService) {
        this.marketService = marketService;
        this.tradingService = tradingService;
        this.portfolioService = portfolioService;
        this.userService = userService;
        this.persistenceService = persistenceService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        marketService.startSimulation();
        printBanner();

        boolean running = true;
        while (running) {
            User user = userService.getCurrentUser();
            System.out.println("\n" + "=".repeat(65));
            System.out.printf("  ACTIVE TRADER: %s (@%s) | CASH: %s\n",
                    user != null ? user.getFullName() : "None",
                    user != null ? user.getUsername() : "none",
                    user != null ? CurrencyFormatter.formatCurrency(user.getPortfolio().getCashBalance()) : "$0.00");
            System.out.println("=".repeat(65));
            System.out.println("  1. Live Market Quotes & Stock Prices");
            System.out.println("  2. Buy Stocks (Market Order)");
            System.out.println("  3. Sell Stocks (Market Order)");
            System.out.println("  4. View Portfolio Performance & Holdings");
            System.out.println("  5. Deposit or Withdraw Funds");
            System.out.println("  6. Manage Watchlist");
            System.out.println("  7. Transaction Ledger & Export Report");
            System.out.println("  8. Financial News Wire");
            System.out.println("  9. Advance Market Simulation Tick");
            System.out.println("  10. Switch / Register Trader Account");
            System.out.println("  0. Exit Platform");
            System.out.print("  Select an option [0-10]: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": showMarketQuotes(); break;
                case "2": executeBuy(); break;
                case "3": executeSell(); break;
                case "4": showPortfolio(); break;
                case "5": manageFunds(); break;
                case "6": manageWatchlist(); break;
                case "7": showTransactionsAndExport(); break;
                case "8": showNews(); break;
                case "9": 
                    marketService.tickMarket();
                    System.out.println(">>> Market tick simulated successfully!");
                    break;
                case "10": switchAccount(); break;
                case "0":
                    System.out.println("\nSaving state and shutting down... Happy Trading!");
                    userService.saveAll();
                    marketService.stopSimulation();
                    running = false;
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please enter a number between 0 and 10.");
            }
        }
    }

    private void printBanner() {
        System.out.println("=================================================================");
        System.out.println("             CODEALPHA STOCK TRADING SIMULATOR PLATFORM          ");
        System.out.println("          Enterprise Java Object-Oriented Financial Engine       ");
        System.out.println("=================================================================");
    }

    private void showMarketQuotes() {
        System.out.println("\n" + "-".repeat(78));
        System.out.printf("%-6s | %-22s | %-16s | %10s | %9s | %8s\n", 
                "Symbol", "Company", "Sector", "Price", "Change", "Volume");
        System.out.println("-".repeat(78));

        for (Stock s : marketService.getStocks().values()) {
            System.out.printf("%-6s | %-22s | %-16s | %10s | %+8.2f%% | %8s\n",
                    s.getSymbol(),
                    s.getCompanyName().length() > 22 ? s.getCompanyName().substring(0, 19) + "..." : s.getCompanyName(),
                    s.getSector().getDisplayName(),
                    CurrencyFormatter.formatCurrency(s.getCurrentPrice()),
                    s.getPriceChangePercent(),
                    CurrencyFormatter.formatVolume(s.getVolume()));
        }
        System.out.println("-".repeat(78));
    }

    private void executeBuy() {
        User user = userService.getCurrentUser();
        System.out.print("Enter Stock Symbol to BUY (e.g. AAPL, NVDA): ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = marketService.getStock(symbol);

        if (stock == null) {
            System.out.println("[!] Stock not found.");
            return;
        }

        System.out.printf("Current Price for %s: %s | Available Cash: %s\n", 
                stock.getSymbol(), 
                CurrencyFormatter.formatCurrency(stock.getCurrentPrice()),
                CurrencyFormatter.formatCurrency(user.getPortfolio().getCashBalance()));
        System.out.print("Enter Number of Shares to Buy: ");

        try {
            int qty = Integer.parseInt(scanner.nextLine().trim());
            TradingService.TradeResult res = tradingService.executeMarketBuy(user, symbol, qty);
            if (res.isSuccess()) {
                System.out.println("[SUCCESS] " + res.getMessage());
                userService.saveAll();
            } else {
                System.out.println("[ERROR] " + res.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid quantity.");
        }
    }

    private void executeSell() {
        User user = userService.getCurrentUser();
        System.out.print("Enter Stock Symbol to SELL: ");
        String symbol = scanner.nextLine().trim().toUpperCase();
        Stock stock = marketService.getStock(symbol);

        if (stock == null) {
            System.out.println("[!] Stock not found.");
            return;
        }

        int owned = user.getPortfolio().getSharesOwned(symbol);
        System.out.printf("Current Price for %s: %s | Shares Owned: %d\n", 
                stock.getSymbol(), 
                CurrencyFormatter.formatCurrency(stock.getCurrentPrice()),
                owned);

        if (owned <= 0) {
            System.out.println("[!] You do not own any shares of " + symbol);
            return;
        }

        System.out.print("Enter Number of Shares to Sell (1 - " + owned + "): ");
        try {
            int qty = Integer.parseInt(scanner.nextLine().trim());
            TradingService.TradeResult res = tradingService.executeMarketSell(user, symbol, qty);
            if (res.isSuccess()) {
                System.out.println("[SUCCESS] " + res.getMessage());
                userService.saveAll();
            } else {
                System.out.println("[ERROR] " + res.getMessage());
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid quantity.");
        }
    }

    private void showPortfolio() {
        User user = userService.getCurrentUser();
        System.out.println("\n" + portfolioService.generatePortfolioReport(user));
    }

    private void manageFunds() {
        User user = userService.getCurrentUser();
        System.out.printf("Current Balance: %s\n", CurrencyFormatter.formatCurrency(user.getPortfolio().getCashBalance()));
        System.out.println("1. Deposit Cash");
        System.out.println("2. Withdraw Cash");
        System.out.print("Choose option [1-2]: ");
        String opt = scanner.nextLine().trim();

        if ("1".equals(opt)) {
            System.out.print("Enter deposit amount ($): ");
            try {
                double amt = Double.parseDouble(scanner.nextLine().trim());
                TradingService.TradeResult res = tradingService.depositFunds(user, amt);
                System.out.println(res.isSuccess() ? "[SUCCESS] " + res.getMessage() : "[ERROR] " + res.getMessage());
                if (res.isSuccess()) userService.saveAll();
            } catch (Exception e) {
                System.out.println("[!] Invalid amount.");
            }
        } else if ("2".equals(opt)) {
            System.out.print("Enter withdrawal amount ($): ");
            try {
                double amt = Double.parseDouble(scanner.nextLine().trim());
                TradingService.TradeResult res = tradingService.withdrawFunds(user, amt);
                System.out.println(res.isSuccess() ? "[SUCCESS] " + res.getMessage() : "[ERROR] " + res.getMessage());
                if (res.isSuccess()) userService.saveAll();
            } catch (Exception e) {
                System.out.println("[!] Invalid amount.");
            }
        }
    }

    private void manageWatchlist() {
        User user = userService.getCurrentUser();
        System.out.println("\n--- Current Watchlist ---");
        for (String sym : user.getWatchlist()) {
            Stock s = marketService.getStock(sym);
            if (s != null) {
                System.out.printf("  ⭐ %-6s: %10s (%+6.2f%%)\n", sym, 
                        CurrencyFormatter.formatCurrency(s.getCurrentPrice()), s.getPriceChangePercent());
            }
        }

        System.out.print("\nEnter Symbol to Toggle Watchlist (Add/Remove): ");
        String sym = scanner.nextLine().trim().toUpperCase();
        if (!sym.isEmpty()) {
            boolean added = user.toggleWatchlist(sym);
            System.out.println(added ? "Added " + sym + " to watchlist!" : "Removed " + sym + " from watchlist!");
            userService.saveAll();
        }
    }

    private void showTransactionsAndExport() {
        User user = userService.getCurrentUser();
        System.out.println("\n--- Recent Transactions ---");
        for (Transaction t : user.getTransactions()) {
            System.out.println("  " + t);
        }

        System.out.print("\nDo you want to export portfolio report to file? (y/n): ");
        String ans = scanner.nextLine().trim();
        if ("y".equalsIgnoreCase(ans)) {
            String report = portfolioService.generatePortfolioReport(user);
            String filename = "Portfolio_Report_" + user.getUsername() + ".txt";
            persistenceService.exportReportToFile(filename, report);
            System.out.println("[SUCCESS] Report saved to data/" + filename);
        }
    }

    private void showNews() {
        System.out.println("\n--- Financial News Wire ---");
        for (MarketNews n : marketService.getNewsFeed()) {
            System.out.printf("  [%s] %s | %s\n", n.getFormattedTime(), n.getHeadline(), n.getSentiment());
        }
    }

    private void switchAccount() {
        System.out.println("\n1. Sign in to existing account");
        System.out.println("2. Register new trader account");
        System.out.print("Choose option [1-2]: ");
        String opt = scanner.nextLine().trim();

        if ("1".equals(opt)) {
            System.out.print("Username: ");
            String u = scanner.nextLine().trim();
            System.out.print("Password: ");
            String p = scanner.nextLine().trim();
            if (userService.authenticate(u, p)) {
                System.out.println("[SUCCESS] Logged in as " + userService.getCurrentUser().getFullName());
            } else {
                System.out.println("[ERROR] Invalid credentials.");
            }
        } else if ("2".equals(opt)) {
            System.out.print("Choose Username: ");
            String u = scanner.nextLine().trim();
            System.out.print("Full Name: ");
            String n = scanner.nextLine().trim();
            System.out.print("Password: ");
            String p = scanner.nextLine().trim();
            System.out.print("Initial Demo Cash ($): ");
            try {
                double c = Double.parseDouble(scanner.nextLine().trim());
                if (userService.registerUser(u, n, p, c)) {
                    System.out.println("[SUCCESS] Registered and logged in as " + n);
                } else {
                    System.out.println("[ERROR] Username already taken.");
                }
            } catch (Exception e) {
                System.out.println("[!] Invalid cash amount.");
            }
        }
    }
}
