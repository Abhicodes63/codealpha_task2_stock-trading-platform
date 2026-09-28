package com.codealpha.stocktrading.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a publicly traded stock entity in the trading simulation.
 */
public class Stock implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private final String symbol;
    private final String companyName;
    private final Sector sector;
    private double currentPrice;
    private double previousClose;
    private double openPrice;
    private double dayHigh;
    private double dayLow;
    private long volume;
    private double marketCapBillions;
    private double volatility; // Daily volatility standard deviation (e.g., 0.015 = 1.5%)
    private double beta; // Sensitivity to market movements
    private final List<Double> priceHistory;
    private final int maxHistoryPoints = 100;

    public Stock(String symbol, String companyName, Sector sector, double initialPrice, 
                 double marketCapBillions, double volatility, double beta) {
        this.symbol = symbol.toUpperCase();
        this.companyName = companyName;
        this.sector = sector;
        this.currentPrice = Math.round(initialPrice * 100.0) / 100.0;
        this.previousClose = this.currentPrice;
        this.openPrice = this.currentPrice;
        this.dayHigh = this.currentPrice;
        this.dayLow = this.currentPrice;
        this.volume = (long) (Math.random() * 500_000 + 100_000);
        this.marketCapBillions = marketCapBillions;
        this.volatility = volatility;
        this.beta = beta;
        this.priceHistory = new ArrayList<>();
        
        // Seed initial history
        double p = initialPrice * 0.95;
        for (int i = 0; i < 20; i++) {
            p += (Math.random() - 0.48) * (initialPrice * 0.01);
            priceHistory.add(Math.round(p * 100.0) / 100.0);
        }
        priceHistory.add(this.currentPrice);
    }

    /**
     * Updates the stock price safely during simulation ticks.
     */
    public synchronized void updatePrice(double newPrice, long addedVolume) {
        this.currentPrice = Math.round(Math.max(0.01, newPrice) * 100.0) / 100.0;
        if (this.currentPrice > this.dayHigh) {
            this.dayHigh = this.currentPrice;
        }
        if (this.currentPrice < this.dayLow) {
            this.dayLow = this.currentPrice;
        }
        this.volume += addedVolume;

        priceHistory.add(this.currentPrice);
        if (priceHistory.size() > maxHistoryPoints) {
            priceHistory.remove(0);
        }
    }

    public synchronized double getPriceChange() {
        return Math.round((currentPrice - previousClose) * 100.0) / 100.0;
    }

    public synchronized double getPriceChangePercent() {
        if (previousClose == 0) return 0.0;
        return Math.round(((currentPrice - previousClose) / previousClose * 100.0) * 100.0) / 100.0;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Sector getSector() {
        return sector;
    }

    public synchronized double getCurrentPrice() {
        return currentPrice;
    }

    public synchronized double getPreviousClose() {
        return previousClose;
    }

    public synchronized void setPreviousClose(double previousClose) {
        this.previousClose = previousClose;
    }

    public synchronized double getOpenPrice() {
        return openPrice;
    }

    public synchronized double getDayHigh() {
        return dayHigh;
    }

    public synchronized double getDayLow() {
        return dayLow;
    }

    public synchronized long getVolume() {
        return volume;
    }

    public double getMarketCapBillions() {
        return marketCapBillions;
    }

    public double getVolatility() {
        return volatility;
    }

    public double getBeta() {
        return beta;
    }

    public synchronized List<Double> getPriceHistory() {
        return Collections.unmodifiableList(new ArrayList<>(priceHistory));
    }

    @Override
    public String toString() {
        return String.format("%s (%s): $%.2f [%+.2f%%]", symbol, companyName, currentPrice, getPriceChangePercent());
    }
}
