package com.codealpha.stocktrading.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a registered user account containing their portfolio and history.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String username;
    private String fullName;
    private String password;
    private final Portfolio portfolio;
    private final List<Transaction> transactions;
    private final List<Order> orders;
    private final Set<String> watchlist;
    private final LocalDateTime registeredAt;

    public User(String username, String fullName, String password, double initialCash) {
        this.username = username.toLowerCase().trim();
        this.fullName = fullName;
        this.password = password;
        this.portfolio = new Portfolio(initialCash);
        this.transactions = new ArrayList<>();
        this.orders = new ArrayList<>();
        this.watchlist = new HashSet<>();
        this.registeredAt = LocalDateTime.now();

        // Default watchlist items
        this.watchlist.add("AAPL");
        this.watchlist.add("NVDA");
        this.watchlist.add("TSLA");
        this.watchlist.add("MSFT");

        // Initial deposit transaction
        this.transactions.add(new Transaction(
                TransactionType.DEPOSIT,
                "CASH",
                1,
                initialCash,
                initialCash,
                0.0,
                "Initial Account Funding"
        ));
    }

    public boolean validatePassword(String inputPassword) {
        return this.password != null && this.password.equals(inputPassword);
    }

    public synchronized void addTransaction(Transaction tx) {
        this.transactions.add(0, tx); // Newest first
    }

    public synchronized void addOrder(Order order) {
        this.orders.add(0, order);
    }

    public synchronized boolean toggleWatchlist(String symbol) {
        symbol = symbol.toUpperCase();
        if (watchlist.contains(symbol)) {
            watchlist.remove(symbol);
            return false; // Removed
        } else {
            watchlist.add(symbol);
            return true; // Added
        }
    }

    public synchronized boolean isWatchlisted(String symbol) {
        return watchlist.contains(symbol.toUpperCase());
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public synchronized List<Transaction> getTransactions() {
        return Collections.unmodifiableList(new ArrayList<>(transactions));
    }

    public synchronized List<Order> getOrders() {
        return Collections.unmodifiableList(new ArrayList<>(orders));
    }

    public synchronized Set<String> getWatchlist() {
        return Collections.unmodifiableSet(new HashSet<>(watchlist));
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
}
