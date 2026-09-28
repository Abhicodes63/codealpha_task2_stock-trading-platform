package com.codealpha.stocktrading.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents simulated financial news headlines affecting stock sentiment.
 */
public class MarketNews implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public enum Sentiment {
        BULLISH, BEARISH, NEUTRAL
    }

    private final String id;
    private final LocalDateTime timestamp;
    private final String headline;
    private final String affectedSymbol;
    private final Sentiment sentiment;
    private final double impactPercent;

    public MarketNews(String headline, String affectedSymbol, Sentiment sentiment, double impactPercent) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.timestamp = LocalDateTime.now();
        this.headline = headline;
        this.affectedSymbol = affectedSymbol;
        this.sentiment = sentiment;
        this.impactPercent = impactPercent;
    }

    public String getId() {
        return id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return timestamp.format(FORMATTER);
    }

    public String getHeadline() {
        return headline;
    }

    public String getAffectedSymbol() {
        return affectedSymbol;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public double getImpactPercent() {
        return impactPercent;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", getFormattedTime(), headline, sentiment);
    }
}
