package com.codealpha.stocktrading.service;

import com.codealpha.stocktrading.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Persists user accounts, portfolios, order logs, and transaction histories to local disk storage.
 */
public class PersistenceService {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users_data.dat";

    public PersistenceService() {
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            System.err.println("Failed to create data directory: " + e.getMessage());
        }
    }

    /**
     * Saves all users to binary file.
     */
    public synchronized boolean saveUsers(Map<String, User> users) {
        ensureDataDirectoryExists();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving user data: " + e.getMessage());
            return false;
        }
    }

    /**
     * Loads users from storage, or returns an empty map if no file exists.
     */
    @SuppressWarnings("unchecked")
    public synchronized Map<String, User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                return (Map<String, User>) obj;
            }
        } catch (Exception e) {
            System.err.println("Could not load users file (creating fresh state): " + e.getMessage());
        }

        return new HashMap<>();
    }

    /**
     * Exports a portfolio report to a text file.
     */
    public boolean exportReportToFile(String filename, String content) {
        try {
            ensureDataDirectoryExists();
            Path filePath = Paths.get(DATA_DIR, filename);
            Files.writeString(filePath, content);
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting report: " + e.getMessage());
            return false;
        }
    }
}
