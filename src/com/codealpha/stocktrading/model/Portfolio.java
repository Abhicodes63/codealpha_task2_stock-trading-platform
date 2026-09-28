package com.codealpha.stocktrading.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the user's cash balance and stock holdings.
 */
public class Portfolio implements Serializable {
    private static final long serialVersionUID = 1L;

    private double cashBalance;
    private final Map<String, Holding> holdings;
    private double cumulativeRealizedPL;

    public Portfolio(double initialCashBalance) {
        this.cashBalance = Math.round(initialCashBalance * 100.0) / 100.0;
        this.holdings = new HashMap<>();
        this.cumulativeRealizedPL = 0.0;
    }

    public synchronized double getCashBalance() {
        return cashBalance;
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.cashBalance = Math.round((this.cashBalance + amount) * 100.0) / 100.0;
    }

    public synchronized boolean withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > this.cashBalance) {
            return false;
        }
        this.cashBalance = Math.round((this.cashBalance - amount) * 100.0) / 100.0;
        return true;
    }

    public synchronized void addStock(String symbol, int quantity, double price) {
        symbol = symbol.toUpperCase();
        if (holdings.containsKey(symbol)) {
            holdings.get(symbol).addShares(quantity, price);
        } else {
            holdings.put(symbol, new Holding(symbol, quantity, price));
        }
        this.cashBalance = Math.round((this.cashBalance - (quantity * price)) * 100.0) / 100.0;
    }

    public synchronized double sellStock(String symbol, int quantity, double price) {
        symbol = symbol.toUpperCase();
        Holding holding = holdings.get(symbol);
        if (holding == null || holding.getQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient shares to sell.");
        }

        double realizedPL = holding.removeShares(quantity, price);
        cumulativeRealizedPL += realizedPL;
        this.cashBalance = Math.round((this.cashBalance + (quantity * price)) * 100.0) / 100.0;

        if (holding.getQuantity() == 0) {
            holdings.remove(symbol);
        }
        return realizedPL;
    }

    public synchronized Holding getHolding(String symbol) {
        return holdings.get(symbol.toUpperCase());
    }

    public synchronized Map<String, Holding> getHoldings() {
        return Collections.unmodifiableMap(new HashMap<>(holdings));
    }

    public synchronized int getSharesOwned(String symbol) {
        Holding h = holdings.get(symbol.toUpperCase());
        return h != null ? h.getQuantity() : 0;
    }

    public synchronized double getTotalCostBasis() {
        double sum = 0.0;
        for (Holding h : holdings.values()) {
            sum += h.getTotalCostBasis();
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public synchronized double getTotalHoldingsMarketValue(Map<String, Stock> marketStocks) {
        double sum = 0.0;
        for (Holding h : holdings.values()) {
            Stock stock = marketStocks.get(h.getStockSymbol());
            double price = stock != null ? stock.getCurrentPrice() : h.getAverageBuyPrice();
            sum += h.getCurrentMarketValue(price);
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public synchronized double getTotalPortfolioValue(Map<String, Stock> marketStocks) {
        return Math.round((cashBalance + getTotalHoldingsMarketValue(marketStocks)) * 100.0) / 100.0;
    }

    public synchronized double getTotalUnrealizedPL(Map<String, Stock> marketStocks) {
        double sum = 0.0;
        for (Holding h : holdings.values()) {
            Stock stock = marketStocks.get(h.getStockSymbol());
            double price = stock != null ? stock.getCurrentPrice() : h.getAverageBuyPrice();
            sum += h.getUnrealizedProfitLoss(price);
        }
        return Math.round(sum * 100.0) / 100.0;
    }

    public synchronized double getTotalUnrealizedPLPercent(Map<String, Stock> marketStocks) {
        double cost = getTotalCostBasis();
        if (cost == 0) return 0.0;
        return Math.round(((getTotalHoldingsMarketValue(marketStocks) - cost) / cost * 100.0) * 100.0) / 100.0;
    }

    public synchronized double getCumulativeRealizedPL() {
        return Math.round(cumulativeRealizedPL * 100.0) / 100.0;
    }
}
