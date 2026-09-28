package com.codealpha.stocktrading.model;

/**
 * Status of an active or past order.
 */
public enum OrderStatus {
    PENDING("Pending Trigger"),
    EXECUTED("Executed"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
