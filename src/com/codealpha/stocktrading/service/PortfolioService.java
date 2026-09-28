package com.codealpha.stocktrading.service;

import com.codealpha.stocktrading.model.*;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service to compute detailed portfolio analytics, asset allocations, 
 * performance metrics, and exportable reports.
 */
public class PortfolioService {

    public static class PortfolioSummary {
        private final double cashBalance;
        private final double holdingsValue;
        private final double totalPortfolioValue;
        private final double totalCostBasis;
        private final double totalUnrealizedPL;
        private final double totalUnrealizedPLPercent;
        private final double totalRealizedPL;
        private final int distinctStockCount;
        private final Map<String, Double> assetAllocationPercentages;
        private final Map<Sector, Double> sectorAllocationPercentages;
        private final int diversificationScore;

        public PortfolioSummary(double cashBalance, double holdingsValue, double totalPortfolioValue, 
                                double totalCostBasis, double totalUnrealizedPL, double totalUnrealizedPLPercent, 
                                double totalRealizedPL, int distinctStockCount, 
                                Map<String, Double> assetAllocationPercentages, 
                                Map<Sector, Double> sectorAllocationPercentages, 
                                int diversificationScore) {
            this.cashBalance = cashBalance;
            this.holdingsValue = holdingsValue;
            this.totalPortfolioValue = totalPortfolioValue;
            this.totalCostBasis = totalCostBasis;
            this.totalUnrealizedPL = totalUnrealizedPL;
            this.totalUnrealizedPLPercent = totalUnrealizedPLPercent;
            this.totalRealizedPL = totalRealizedPL;
            this.distinctStockCount = distinctStockCount;
            this.assetAllocationPercentages = assetAllocationPercentages;
            this.sectorAllocationPercentages = sectorAllocationPercentages;
            this.diversificationScore = diversificationScore;
        }

        public double getCashBalance() { return cashBalance; }
        public double getHoldingsValue() { return holdingsValue; }
        public double getTotalPortfolioValue() { return totalPortfolioValue; }
        public double getTotalCostBasis() { return totalCostBasis; }
        public double getTotalUnrealizedPL() { return totalUnrealizedPL; }
        public double getTotalUnrealizedPLPercent() { return totalUnrealizedPLPercent; }
        public double getTotalRealizedPL() { return totalRealizedPL; }
        public int getDistinctStockCount() { return distinctStockCount; }
        public Map<String, Double> getAssetAllocationPercentages() { return assetAllocationPercentages; }
        public Map<Sector, Double> getSectorAllocationPercentages() { return sectorAllocationPercentages; }
        public int getDiversificationScore() { return diversificationScore; }
    }

    private final MarketService marketService;

    public PortfolioService(MarketService marketService) {
        this.marketService = marketService;
    }

    /**
     * Computes real-time portfolio metrics for the given user.
     */
    public PortfolioSummary getPortfolioSummary(User user) {
        if (user == null) {
            return new PortfolioSummary(0, 0, 0, 0, 0, 0, 0, 0, Collections.emptyMap(), Collections.emptyMap(), 0);
        }

        Portfolio portfolio = user.getPortfolio();
        Map<String, Stock> marketStocks = marketService.getStocks();

        double cash = portfolio.getCashBalance();
        double holdingsValue = portfolio.getTotalHoldingsMarketValue(marketStocks);
        double totalVal = cash + holdingsValue;
        double costBasis = portfolio.getTotalCostBasis();
        double unrealizedPL = portfolio.getTotalUnrealizedPL(marketStocks);
        double unrealizedPLPercent = portfolio.getTotalUnrealizedPLPercent(marketStocks);
        double realizedPL = portfolio.getCumulativeRealizedPL();
        int stockCount = portfolio.getHoldings().size();

        // Asset allocation
        Map<String, Double> assetAllocations = new LinkedHashMap<>();
        if (totalVal > 0) {
            assetAllocations.put("Cash", Math.round((cash / totalVal * 100.0) * 10.0) / 10.0);
            for (Holding h : portfolio.getHoldings().values()) {
                Stock s = marketStocks.get(h.getStockSymbol());
                double p = s != null ? s.getCurrentPrice() : h.getAverageBuyPrice();
                double val = h.getCurrentMarketValue(p);
                assetAllocations.put(h.getStockSymbol(), Math.round((val / totalVal * 100.0) * 10.0) / 10.0);
            }
        }

        // Sector allocation
        Map<Sector, Double> sectorAllocations = new LinkedHashMap<>();
        if (holdingsValue > 0) {
            for (Holding h : portfolio.getHoldings().values()) {
                Stock s = marketStocks.get(h.getStockSymbol());
                if (s != null) {
                    double p = s.getCurrentPrice();
                    double val = h.getCurrentMarketValue(p);
                    sectorAllocations.merge(s.getSector(), val, Double::sum);
                }
            }
            for (Map.Entry<Sector, Double> entry : sectorAllocations.entrySet()) {
                entry.setValue(Math.round((entry.getValue() / holdingsValue * 100.0) * 10.0) / 10.0);
            }
        }

        // Diversification score (0 - 100)
        int diversificationScore = calculateDiversificationScore(portfolio, sectorAllocations);

        return new PortfolioSummary(
                cash, holdingsValue, totalVal, costBasis, 
                unrealizedPL, unrealizedPLPercent, realizedPL, 
                stockCount, assetAllocations, sectorAllocations, diversificationScore
        );
    }

    private int calculateDiversificationScore(Portfolio portfolio, Map<Sector, Double> sectorAllocations) {
        if (portfolio.getHoldings().isEmpty()) return 0;
        
        int stockCount = portfolio.getHoldings().size();
        int sectorCount = sectorAllocations.size();

        int score = 0;
        // Points for number of stocks (up to 40 pts)
        score += Math.min(40, stockCount * 8);

        // Points for sector spread (up to 40 pts)
        score += Math.min(40, sectorCount * 10);

        // Points for balanced weights (up to 20 pts)
        boolean hasDominantHolding = false;
        for (double weight : sectorAllocations.values()) {
            if (weight > 50.0) {
                hasDominantHolding = true;
                break;
            }
        }
        if (!hasDominantHolding && sectorCount >= 2) {
            score += 20;
        } else {
            score += 10;
        }

        return Math.min(100, score);
    }

    /**
     * Generates a comprehensive plain-text exportable report.
     */
    public String generatePortfolioReport(User user) {
        if (user == null) return "No user selected.";
        
        PortfolioSummary summary = getPortfolioSummary(user);
        Map<String, Stock> market = marketService.getStocks();

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================\n");
        sb.append("                 CODEALPHA STOCK TRADING PLATFORM                       \n");
        sb.append("                        PORTFOLIO REPORT                                \n");
        sb.append("========================================================================\n");
        sb.append("Trader: ").append(user.getFullName()).append(" (@").append(user.getUsername()).append(")\n");
        sb.append("Generated At: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("------------------------------------------------------------------------\n");
        sb.append("FINANCIAL SUMMARY\n");
        sb.append(String.format("  Total Portfolio Value:   %s\n", CurrencyFormatter.formatCurrency(summary.getTotalPortfolioValue())));
        sb.append(String.format("  Available Cash Balance:  %s\n", CurrencyFormatter.formatCurrency(summary.getCashBalance())));
        sb.append(String.format("  Stock Holdings Value:    %s\n", CurrencyFormatter.formatCurrency(summary.getHoldingsValue())));
        sb.append(String.format("  Total Invested (Cost):   %s\n", CurrencyFormatter.formatCurrency(summary.getTotalCostBasis())));
        sb.append(String.format("  Unrealized Profit/Loss:  %s (%s)\n", 
                CurrencyFormatter.formatCurrency(summary.getTotalUnrealizedPL()),
                CurrencyFormatter.formatPercent(summary.getTotalUnrealizedPLPercent())));
        sb.append(String.format("  Realized Profit/Loss:    %s\n", CurrencyFormatter.formatCurrency(summary.getTotalRealizedPL())));
        sb.append(String.format("  Diversification Score:   %d / 100\n", summary.getDiversificationScore()));
        sb.append("------------------------------------------------------------------------\n");
        sb.append("CURRENT HOLDINGS\n");
        sb.append(String.format("%-8s | %-20s | %6s | %10s | %10s | %12s | %10s\n", 
                "Symbol", "Company", "Shares", "Avg Cost", "Price", "Market Val", "P/L ($)"));
        sb.append("------------------------------------------------------------------------\n");

        if (user.getPortfolio().getHoldings().isEmpty()) {
            sb.append("  (No active stock holdings)\n");
        } else {
            for (Holding h : user.getPortfolio().getHoldings().values()) {
                Stock s = market.get(h.getStockSymbol());
                String company = s != null ? s.getCompanyName() : "N/A";
                double curPrice = s != null ? s.getCurrentPrice() : h.getAverageBuyPrice();
                double mktCap = h.getCurrentMarketValue(curPrice);
                double pl = h.getUnrealizedProfitLoss(curPrice);

                sb.append(String.format("%-8s | %-20s | %6d | %10s | %10s | %12s | %10s\n",
                        h.getStockSymbol(),
                        company.length() > 20 ? company.substring(0, 17) + "..." : company,
                        h.getQuantity(),
                        CurrencyFormatter.formatCurrency(h.getAverageBuyPrice()),
                        CurrencyFormatter.formatCurrency(curPrice),
                        CurrencyFormatter.formatCurrency(mktCap),
                        CurrencyFormatter.formatCurrency(pl)));
            }
        }

        sb.append("------------------------------------------------------------------------\n");
        sb.append("RECENT TRANSACTIONS (Last 10)\n");
        List<Transaction> txns = user.getTransactions();
        int count = Math.min(10, txns.size());
        for (int i = 0; i < count; i++) {
            Transaction t = txns.get(i);
            sb.append(String.format("  [%s] %-8s %s %d shares @ %s (Total: %s)\n",
                    t.getFormattedTimestamp(),
                    t.getType(),
                    t.getStockSymbol(),
                    t.getQuantity(),
                    CurrencyFormatter.formatCurrency(t.getPricePerShare()),
                    CurrencyFormatter.formatCurrency(t.getTotalAmount())));
        }
        sb.append("========================================================================\n");

        return sb.toString();
    }
}
