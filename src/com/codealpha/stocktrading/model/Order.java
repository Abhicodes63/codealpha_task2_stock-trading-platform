package com.codealpha.stocktrading.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents an open or historical order (Market, Limit, Stop Loss).
 */
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String orderId;
    private final String username;
    private final String symbol;
    private final OrderType orderType;
    private final TransactionType side; // BUY or SELL
    private final int quantity;
    private final double targetPrice;
    private OrderStatus status;
    private final LocalDateTime placedAt;
    private LocalDateTime executedAt;

    public Order(String username, String symbol, OrderType orderType, 
                 TransactionType side, int quantity, double targetPrice) {
        this.orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.username = username;
        this.symbol = symbol.toUpperCase();
        this.orderType = orderType;
        this.side = side;
        this.quantity = quantity;
        this.targetPrice = Math.round(targetPrice * 100.0) / 100.0;
        this.status = OrderStatus.PENDING;
        this.placedAt = LocalDateTime.now();
    }

    public boolean shouldExecute(double currentPrice) {
        if (status != OrderStatus.PENDING) {
            return false;
        }

        if (orderType == OrderType.MARKET) {
            return true;
        } else if (orderType == OrderType.LIMIT) {
            if (side == TransactionType.BUY && currentPrice <= targetPrice) {
                return true; // Buy limit triggered when price drops to or below target
            } else if (side == TransactionType.SELL && currentPrice >= targetPrice) {
                return true; // Sell limit triggered when price rises to or above target
            }
        } else if (orderType == OrderType.STOP_LOSS) {
            if (side == TransactionType.SELL && currentPrice <= targetPrice) {
                return true; // Stop loss triggered when price drops to target to cut losses
            } else if (side == TransactionType.BUY && currentPrice >= targetPrice) {
                return true; // Buy stop triggered when price breaks above target
            }
        }
        return false;
    }

    public void markExecuted() {
        this.status = OrderStatus.EXECUTED;
        this.executedAt = LocalDateTime.now();
    }

    public void markCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getUsername() {
        return username;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public TransactionType getSide() {
        return side;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getPlacedAt() {
        return placedAt;
    }

    public String getFormattedPlacedAt() {
        return placedAt.format(FORMATTER);
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public String getFormattedExecutedAt() {
        return executedAt != null ? executedAt.format(FORMATTER) : "—";
    }
}
