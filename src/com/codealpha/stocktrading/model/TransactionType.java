package com.codealpha.stocktrading.model;

/**
 * Type of financial transaction.
 */
public enum TransactionType {
    BUY("Buy Stock"),
    SELL("Sell Stock"),
    DEPOSIT("Cash Deposit"),
    WITHDRAW("Cash Withdrawal"),
    DIVIDEND("Dividend Credit");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
