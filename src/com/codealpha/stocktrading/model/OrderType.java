package com.codealpha.stocktrading.model;

/**
 * Supported stock order types.
 */
public enum OrderType {
    MARKET("Market Order"),
    LIMIT("Limit Order"),
    STOP_LOSS("Stop Loss Order");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
