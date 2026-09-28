package com.codealpha.stocktrading.service;

import com.codealpha.stocktrading.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles core order execution, trade validations, limit order checks,
 * cash deposits, and withdrawals.
 */
public class TradingService {

    public static class TradeResult {
        private final boolean success;
        private final String message;
        private final Transaction transaction;
        private final Order order;

        private TradeResult(boolean success, String message, Transaction transaction, Order order) {
            this.success = success;
            this.message = message;
            this.transaction = transaction;
            this.order = order;
        }

        public static TradeResult ok(String message, Transaction transaction) {
            return new TradeResult(true, message, transaction, null);
        }

        public static TradeResult okOrder(String message, Order order) {
            return new TradeResult(true, message, null, order);
        }

        public static TradeResult fail(String message) {
            return new TradeResult(false, message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public Order getOrder() {
            return order;
        }
    }

    private final MarketService marketService;

    public TradingService(MarketService marketService) {
        this.marketService = marketService;
    }

    /**
     * Executes an immediate market buy order.
     */
    public synchronized TradeResult executeMarketBuy(User user, String symbol, int quantity) {
        if (user == null) return TradeResult.fail("No active user session.");
        if (quantity <= 0) return TradeResult.fail("Quantity must be greater than zero.");

        Stock stock = marketService.getStock(symbol);
        if (stock == null) return TradeResult.fail("Stock '" + symbol + "' not found.");

        double price = stock.getCurrentPrice();
        double totalCost = quantity * price;

        if (user.getPortfolio().getCashBalance() < totalCost) {
            return TradeResult.fail(String.format("Insufficient funds. Required: $%.2f, Available: $%.2f",
                    totalCost, user.getPortfolio().getCashBalance()));
        }

        user.getPortfolio().addStock(symbol, quantity, price);

        Transaction txn = new Transaction(
                TransactionType.BUY,
                symbol,
                quantity,
                price,
                totalCost,
                0.0,
                "Market Buy Execution"
        );
        user.addTransaction(txn);

        return TradeResult.ok(String.format("Successfully bought %d shares of %s at $%.2f", quantity, symbol, price), txn);
    }

    /**
     * Executes an immediate market sell order.
     */
    public synchronized TradeResult executeMarketSell(User user, String symbol, int quantity) {
        if (user == null) return TradeResult.fail("No active user session.");
        if (quantity <= 0) return TradeResult.fail("Quantity must be greater than zero.");

        Stock stock = marketService.getStock(symbol);
        if (stock == null) return TradeResult.fail("Stock '" + symbol + "' not found.");

        Holding holding = user.getPortfolio().getHolding(symbol);
        if (holding == null || holding.getQuantity() < quantity) {
            int owned = holding == null ? 0 : holding.getQuantity();
            return TradeResult.fail(String.format("Insufficient shares. Owned: %d, Attempted to sell: %d", owned, quantity));
        }

        double price = stock.getCurrentPrice();
        double totalProceeds = quantity * price;
        double realizedPL = user.getPortfolio().sellStock(symbol, quantity, price);

        Transaction txn = new Transaction(
                TransactionType.SELL,
                symbol,
                quantity,
                price,
                totalProceeds,
                realizedPL,
                String.format("Market Sell | Realized P/L: %s$%.2f", realizedPL >= 0 ? "+" : "-", Math.abs(realizedPL))
        );
        user.addTransaction(txn);

        return TradeResult.ok(String.format("Successfully sold %d shares of %s at $%.2f (P/L: %s$%.2f)",
                quantity, symbol, price, realizedPL >= 0 ? "+" : "-", Math.abs(realizedPL)), txn);
    }

    /**
     * Places a limit or stop loss order.
     */
    public synchronized TradeResult placeOrder(User user, String symbol, OrderType orderType, 
                                              TransactionType side, int quantity, double targetPrice) {
        if (user == null) return TradeResult.fail("No active user session.");
        if (quantity <= 0) return TradeResult.fail("Quantity must be greater than zero.");
        if (targetPrice <= 0) return TradeResult.fail("Target price must be greater than zero.");

        Stock stock = marketService.getStock(symbol);
        if (stock == null) return TradeResult.fail("Stock '" + symbol + "' not found.");

        if (orderType == OrderType.MARKET) {
            if (side == TransactionType.BUY) {
                return executeMarketBuy(user, symbol, quantity);
            } else {
                return executeMarketSell(user, symbol, quantity);
            }
        }

        // Validate prerequisites for limit/stop orders
        if (side == TransactionType.BUY) {
            double estimatedCost = quantity * targetPrice;
            if (user.getPortfolio().getCashBalance() < estimatedCost) {
                return TradeResult.fail(String.format("Insufficient cash balance ($%.2f) to place buy order totaling ~$%.2f",
                        user.getPortfolio().getCashBalance(), estimatedCost));
            }
        } else {
            Holding holding = user.getPortfolio().getHolding(symbol);
            if (holding == null || holding.getQuantity() < quantity) {
                return TradeResult.fail("Insufficient shares owned to place sell order.");
            }
        }

        Order order = new Order(user.getUsername(), symbol, orderType, side, quantity, targetPrice);
        user.addOrder(order);

        return TradeResult.okOrder(String.format("Order placed: %s %d %s @ $%.2f (%s)",
                side, quantity, symbol, targetPrice, orderType.getDisplayName()), order);
    }

    /**
     * Evaluates open pending orders against current market prices and executes if triggered.
     */
    public synchronized List<String> processPendingOrders(User user) {
        List<String> notifications = new ArrayList<>();
        if (user == null) return notifications;

        for (Order order : user.getOrders()) {
            if (order.getStatus() != OrderStatus.PENDING) continue;

            Stock stock = marketService.getStock(order.getSymbol());
            if (stock == null) continue;

            double currentPrice = stock.getCurrentPrice();
            if (order.shouldExecute(currentPrice)) {
                if (order.getSide() == TransactionType.BUY) {
                    TradeResult res = executeMarketBuy(user, order.getSymbol(), order.getQuantity());
                    if (res.isSuccess()) {
                        order.markExecuted();
                        notifications.add("Triggered & Executed " + order.getOrderType() + ": Bought " + 
                                order.getQuantity() + " " + order.getSymbol() + " @ $" + currentPrice);
                    }
                } else if (order.getSide() == TransactionType.SELL) {
                    TradeResult res = executeMarketSell(user, order.getSymbol(), order.getQuantity());
                    if (res.isSuccess()) {
                        order.markExecuted();
                        notifications.add("Triggered & Executed " + order.getOrderType() + ": Sold " + 
                                order.getQuantity() + " " + order.getSymbol() + " @ $" + currentPrice);
                    }
                }
            }
        }

        return notifications;
    }

    /**
     * Deposits funds into the user's cash account.
     */
    public synchronized TradeResult depositFunds(User user, double amount) {
        if (user == null) return TradeResult.fail("No active user session.");
        if (amount <= 0) return TradeResult.fail("Deposit amount must be greater than zero.");

        user.getPortfolio().deposit(amount);
        Transaction txn = new Transaction(
                TransactionType.DEPOSIT,
                "CASH",
                1,
                amount,
                amount,
                0.0,
                "Deposit via Electronic Funds Transfer"
        );
        user.addTransaction(txn);

        return TradeResult.ok(String.format("Successfully deposited $%.2f. New cash balance: $%.2f",
                amount, user.getPortfolio().getCashBalance()), txn);
    }

    /**
     * Withdraws funds from the user's cash account.
     */
    public synchronized TradeResult withdrawFunds(User user, double amount) {
        if (user == null) return TradeResult.fail("No active user session.");
        if (amount <= 0) return TradeResult.fail("Withdrawal amount must be greater than zero.");

        if (!user.getPortfolio().withdraw(amount)) {
            return TradeResult.fail(String.format("Insufficient cash balance. Available: $%.2f, Requested: $%.2f",
                    user.getPortfolio().getCashBalance(), amount));
        }

        Transaction txn = new Transaction(
                TransactionType.WITHDRAW,
                "CASH",
                1,
                amount,
                amount,
                0.0,
                "Withdrawal to Connected Bank Account"
        );
        user.addTransaction(txn);

        return TradeResult.ok(String.format("Successfully withdrew $%.2f. New cash balance: $%.2f",
                amount, user.getPortfolio().getCashBalance()), txn);
    }
}
