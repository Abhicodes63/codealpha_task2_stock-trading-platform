package com.codealpha.stocktrading.model;

/**
 * Enumeration of industry sectors for stocks.
 */
public enum Sector {
    TECHNOLOGY("Technology"),
    FINANCE("Financial Services"),
    HEALTHCARE("Healthcare"),
    CONSUMER_GOODS("Consumer Goods"),
    ENERGY("Energy"),
    AUTOMOTIVE("Automotive"),
    AEROSPACE("Aerospace & Defense"),
    TELECOM("Telecommunications");

    private final String displayName;

    Sector(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
