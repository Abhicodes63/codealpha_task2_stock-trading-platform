package com.codealpha.stocktrading.service;

import com.codealpha.stocktrading.model.Transaction;
import com.codealpha.stocktrading.model.TransactionType;
import com.codealpha.stocktrading.model.User;

import java.util.*;

/**
 * Manages user authentication, profile management, and state persistence.
 */
public class UserService {

    private final PersistenceService persistenceService;
    private final Map<String, User> users;
    private User currentUser;

    public UserService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.users = new HashMap<>(persistenceService.loadUsers());

        if (this.users.isEmpty()) {
            createDemoUser();
        } else {
            // Pick first user as default
            this.currentUser = this.users.values().iterator().next();
        }
    }

    private void createDemoUser() {
        User demoUser = new User("trader", "Alex Morgan", "password123", 25000.0);
        
        // Seed demo user with some initial stock holdings for a realistic experience
        demoUser.getPortfolio().addStock("AAPL", 20, 175.50);
        demoUser.addTransaction(new Transaction(
                TransactionType.BUY, "AAPL", 20, 175.50, 3510.0, 0.0, "Portfolio Initialization"
        ));

        demoUser.getPortfolio().addStock("NVDA", 30, 115.20);
        demoUser.addTransaction(new Transaction(
                TransactionType.BUY, "NVDA", 30, 115.20, 3456.0, 0.0, "Portfolio Initialization"
        ));

        demoUser.getPortfolio().addStock("MSFT", 10, 410.00);
        demoUser.addTransaction(new Transaction(
                TransactionType.BUY, "MSFT", 10, 410.00, 4100.0, 0.0, "Portfolio Initialization"
        ));

        users.put(demoUser.getUsername(), demoUser);
        this.currentUser = demoUser;
        saveAll();
    }

    public synchronized boolean registerUser(String username, String fullName, String password, double initialCash) {
        if (username == null || username.trim().isEmpty()) return false;
        String cleanUsername = username.toLowerCase().trim();

        if (users.containsKey(cleanUsername)) {
            return false; // Already exists
        }

        User newUser = new User(cleanUsername, fullName, password, Math.max(100.0, initialCash));
        users.put(cleanUsername, newUser);
        this.currentUser = newUser;
        saveAll();
        return true;
    }

    public synchronized boolean authenticate(String username, String password) {
        if (username == null) return false;
        User user = users.get(username.toLowerCase().trim());
        if (user != null && user.validatePassword(password)) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    public synchronized void saveAll() {
        persistenceService.saveUsers(users);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public Collection<User> getAllUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}
