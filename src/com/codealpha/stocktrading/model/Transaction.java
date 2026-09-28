package com.codealpha.stocktrading.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Immutable record of a executed trading or cash transaction.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String id;
    private final LocalDateTime timestamp;
    private final TransactionType type;
    private final String stockSymbol;
    private final int quantity;
    private final double pricePerShare;
    private final double totalAmount;
    private final double realizedProfitLoss;
    private final String notes;

    public Transaction(TransactionType type, String stockSymbol, int quantity, 
                       double pricePerShare, double totalAmount, double realizedProfitLoss, String notes) {
        this.id = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.stockSymbol = stockSymbol != null ? stockSymbol.toUpperCase() : "N/A";
        this.quantity = quantity;
        this.pricePerShare = Math.round(pricePerShare * 100.0) / 100.0;
        this.totalAmount = Math.round(totalAmount * 100.0) / 100.0;
        this.realizedProfitLoss = Math.round(realizedProfitLoss * 100.0) / 100.0;
        this.notes = notes != null ? notes : "";
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return timestamp.format(FORMATTER);
    }

    public TransactionType getType() {
        return type;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPricePerShare() {
        return pricePerShare;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getRealizedProfitLoss() {
        return realizedProfitLoss;
    }

    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | Qty: %d @ $%.2f = $%.2f", 
                getFormattedTimestamp(), id, type, quantity, pricePerShare, totalAmount);
    }
}
