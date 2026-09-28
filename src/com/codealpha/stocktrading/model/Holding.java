package com.codealpha.stocktrading.model;

import java.io.Serializable;

/**
 * Represents the shares held for a particular stock along with cost basis.
 */
public class Holding implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String stockSymbol;
    private int quantity;
    private double averageBuyPrice;

    public Holding(String stockSymbol, int quantity, double initialPrice) {
        this.stockSymbol = stockSymbol.toUpperCase();
        this.quantity = quantity;
        this.averageBuyPrice = Math.round(initialPrice * 100.0) / 100.0;
    }

    /**
     * Adds shares to this holding and recalculates the weighted average cost basis.
     */
    public synchronized void addShares(int additionalQuantity, double pricePerShare) {
        if (additionalQuantity <= 0) return;
        double currentTotalCost = this.quantity * this.averageBuyPrice;
        double addedTotalCost = additionalQuantity * pricePerShare;
        this.quantity += additionalQuantity;
        this.averageBuyPrice = Math.round(((currentTotalCost + addedTotalCost) / this.quantity) * 100.0) / 100.0;
    }

    /**
     * Removes shares upon sell and returns the realized profit/loss.
     */
    public synchronized double removeShares(int sellQuantity, double sellPrice) {
        if (sellQuantity <= 0 || sellQuantity > this.quantity) {
            throw new IllegalArgumentException("Invalid sell quantity: " + sellQuantity);
        }
        double costBasis = sellQuantity * this.averageBuyPrice;
        double proceeds = sellQuantity * sellPrice;
        double realizedPL = proceeds - costBasis;
        this.quantity -= sellQuantity;
        return Math.round(realizedPL * 100.0) / 100.0;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public synchronized int getQuantity() {
        return quantity;
    }

    public synchronized double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public synchronized double getTotalCostBasis() {
        return Math.round((quantity * averageBuyPrice) * 100.0) / 100.0;
    }

    public synchronized double getCurrentMarketValue(double currentPrice) {
        return Math.round((quantity * currentPrice) * 100.0) / 100.0;
    }

    public synchronized double getUnrealizedProfitLoss(double currentPrice) {
        return Math.round((getCurrentMarketValue(currentPrice) - getTotalCostBasis()) * 100.0) / 100.0;
    }

    public synchronized double getUnrealizedProfitLossPercent(double currentPrice) {
        double cost = getTotalCostBasis();
        if (cost == 0) return 0.0;
        return Math.round(((getCurrentMarketValue(currentPrice) - cost) / cost * 100.0) * 100.0) / 100.0;
    }

    @Override
    public String toString() {
        return String.format("%s: %d shares @ avg $%.2f (Total cost: $%.2f)", 
                stockSymbol, quantity, averageBuyPrice, getTotalCostBasis());
    }
}
