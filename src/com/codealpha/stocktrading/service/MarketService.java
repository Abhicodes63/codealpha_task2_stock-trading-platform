package com.codealpha.stocktrading.service;

import com.codealpha.stocktrading.model.MarketNews;
import com.codealpha.stocktrading.model.Sector;
import com.codealpha.stocktrading.model.Stock;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Simulates a realistic stock market with real-time price fluctuations,
 * market sentiment, sector trends, and news events.
 */
public class MarketService {

    public interface MarketUpdateListener {
        void onMarketTick(Map<String, Stock> stocks);
        void onNewsPublished(MarketNews news);
    }

    private final Map<String, Stock> stocks = new ConcurrentHashMap<>();
    private final List<MarketNews> newsFeed = new CopyOnWriteArrayList<>();
    private final List<MarketUpdateListener> listeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService scheduler;
    private boolean isRunning = false;
    private final Random random = new Random();

    // Market indices simulation
    private double alphaIndexValue = 5420.50;
    private double alphaIndexChangePercent = 0.0;

    public MarketService() {
        initializeStocks();
        initializeSeedNews();
    }

    private void initializeStocks() {
        // Technology
        addStock(new Stock("AAPL", "Apple Inc.", Sector.TECHNOLOGY, 185.40, 2850.0, 0.012, 1.05));
        addStock(new Stock("NVDA", "NVIDIA Corporation", Sector.TECHNOLOGY, 125.80, 3100.0, 0.024, 1.65));
        addStock(new Stock("MSFT", "Microsoft Corporation", Sector.TECHNOLOGY, 428.15, 3180.0, 0.011, 0.95));
        addStock(new Stock("GOOGL", "Alphabet Inc.", Sector.TECHNOLOGY, 178.60, 2240.0, 0.014, 1.10));
        addStock(new Stock("AMZN", "Amazon.com Inc.", Sector.CONSUMER_GOODS, 186.20, 1940.0, 0.016, 1.20));
        addStock(new Stock("META", "Meta Platforms Inc.", Sector.TECHNOLOGY, 510.30, 1300.0, 0.021, 1.35));
        addStock(new Stock("TSLA", "Tesla Inc.", Sector.AUTOMOTIVE, 245.90, 785.0, 0.028, 1.80));
        addStock(new Stock("AMD", "Advanced Micro Devices", Sector.TECHNOLOGY, 155.70, 252.0, 0.023, 1.55));
        
        // Financial Services
        addStock(new Stock("JPM", "JPMorgan Chase & Co.", Sector.FINANCE, 198.50, 570.0, 0.010, 0.85));
        addStock(new Stock("V", "Visa Inc.", Sector.FINANCE, 275.60, 560.0, 0.009, 0.75));
        addStock(new Stock("GS", "Goldman Sachs Group", Sector.FINANCE, 462.20, 155.0, 0.015, 1.15));

        // Healthcare
        addStock(new Stock("JNJ", "Johnson & Johnson", Sector.HEALTHCARE, 158.40, 380.0, 0.007, 0.55));
        addStock(new Stock("PFE", "Pfizer Inc.", Sector.HEALTHCARE, 28.75, 162.0, 0.013, 0.65));

        // Energy & Aerospace
        addStock(new Stock("XOM", "Exxon Mobil Corp.", Sector.ENERGY, 114.30, 455.0, 0.014, 0.80));
        addStock(new Stock("BA", "Boeing Company", Sector.AEROSPACE, 168.90, 104.0, 0.022, 1.40));
        addStock(new Stock("DIS", "Walt Disney Company", Sector.CONSUMER_GOODS, 95.80, 175.0, 0.016, 1.10));
    }

    private void addStock(Stock stock) {
        stocks.put(stock.getSymbol(), stock);
    }

    private void initializeSeedNews() {
        newsFeed.add(new MarketNews("Tech sector rallies on strong semiconductor earnings forecast", "NVDA", MarketNews.Sentiment.BULLISH, 2.5));
        newsFeed.add(new MarketNews("Federal Reserve holds interest rates steady; signals balanced economic outlook", "MARKET", MarketNews.Sentiment.NEUTRAL, 0.2));
        newsFeed.add(new MarketNews("Automakers accelerate AI autonomous driving fleet partnerships", "TSLA", MarketNews.Sentiment.BULLISH, 1.8));
    }

    public synchronized void startSimulation() {
        if (isRunning) return;
        isRunning = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MarketSimulator-Thread");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::tickMarket, 1000, 2000, TimeUnit.MILLISECONDS);
    }

    public synchronized void stopSimulation() {
        if (!isRunning) return;
        isRunning = false;
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Executes a single market tick simulation step.
     */
    public synchronized void tickMarket() {
        // Market-wide sentiment drift (-0.2% to +0.2%)
        double marketDrift = (random.nextGaussian() * 0.002);
        
        // Update Alpha Index
        alphaIndexValue = Math.max(100.0, alphaIndexValue * (1.0 + marketDrift));
        alphaIndexChangePercent = (marketDrift * 100.0);

        for (Stock stock : stocks.values()) {
            double stockVolatility = stock.getVolatility();
            double beta = stock.getBeta();
            
            // Geometric Brownian motion step
            // deltaP/P = beta * marketDrift + idiosyncratic_shock * volatility
            double idiosyncraticShock = random.nextGaussian() * stockVolatility;
            double totalReturn = (beta * marketDrift) + idiosyncraticShock;

            // Micro-drift towards mean
            double newPrice = stock.getCurrentPrice() * (1.0 + totalReturn);
            long addedVolume = (long) (Math.abs(random.nextGaussian()) * 15_000 + 1_000);

            stock.updatePrice(newPrice, addedVolume);
        }

        // Random chance of breaking news (approx once every ~20 ticks)
        if (random.nextInt(20) == 0) {
            generateRandomNewsEvent();
        }

        notifyListenersTick();
    }

    private void generateRandomNewsEvent() {
        List<Stock> stockList = new ArrayList<>(stocks.values());
        Stock targetStock = stockList.get(random.nextInt(stockList.size()));

        boolean isPositive = random.nextBoolean();
        String symbol = targetStock.getSymbol();
        MarketNews news;

        if (isPositive) {
            String[] headlines = {
                    symbol + " beats quarterly earnings and revenue projections by wide margin",
                    "Analyst upgrades " + symbol + " to Strong Buy with increased price target",
                    symbol + " signs landmark strategic enterprise partnership deal",
                    "Surge in demand reported for " + targetStock.getCompanyName() + "'s flagship products"
            };
            String headline = headlines[random.nextInt(headlines.length)];
            double impact = 1.5 + (random.nextDouble() * 3.5);
            news = new MarketNews(headline, symbol, MarketNews.Sentiment.BULLISH, impact);
            // Apply immediate price boost
            targetStock.updatePrice(targetStock.getCurrentPrice() * (1.0 + (impact / 100.0)), 50_000);
        } else {
            String[] headlines = {
                    symbol + " faces supply chain bottlenecks, lowers upcoming guidance",
                    "Regulatory review launched regarding " + targetStock.getCompanyName() + " market practices",
                    "Analyst downgrades " + symbol + " citing macroeconomic headwinds",
                    symbol + " reports unexpected rise in operational expenses"
            };
            String headline = headlines[random.nextInt(headlines.length)];
            double impact = - (1.2 + (random.nextDouble() * 3.0));
            news = new MarketNews(headline, symbol, MarketNews.Sentiment.BEARISH, impact);
            // Apply immediate price drop
            targetStock.updatePrice(targetStock.getCurrentPrice() * (1.0 + (impact / 100.0)), 45_000);
        }

        newsFeed.add(0, news);
        if (newsFeed.size() > 50) {
            newsFeed.remove(newsFeed.size() - 1);
        }

        for (MarketUpdateListener listener : listeners) {
            try {
                listener.onNewsPublished(news);
            } catch (Exception e) {
                // Ignore listener exceptions
            }
        }
    }

    public void addListener(MarketUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(MarketUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListenersTick() {
        for (MarketUpdateListener listener : listeners) {
            try {
                listener.onMarketTick(stocks);
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public Map<String, Stock> getStocks() {
        return Collections.unmodifiableMap(stocks);
    }

    public Stock getStock(String symbol) {
        if (symbol == null) return null;
        return stocks.get(symbol.toUpperCase());
    }

    public List<Stock> searchStocks(String query, Sector sectorFilter) {
        List<Stock> result = new ArrayList<>();
        String q = query != null ? query.trim().toUpperCase() : "";

        for (Stock s : stocks.values()) {
            boolean matchesSector = (sectorFilter == null || s.getSector() == sectorFilter);
            boolean matchesQuery = q.isEmpty() || 
                    s.getSymbol().contains(q) || 
                    s.getCompanyName().toUpperCase().contains(q);

            if (matchesSector && matchesQuery) {
                result.add(s);
            }
        }

        result.sort(Comparator.comparing(Stock::getSymbol));
        return result;
    }

    public List<MarketNews> getNewsFeed() {
        return Collections.unmodifiableList(newsFeed);
    }

    public double getAlphaIndexValue() {
        return Math.round(alphaIndexValue * 100.0) / 100.0;
    }

    public double getAlphaIndexChangePercent() {
        return Math.round(alphaIndexChangePercent * 100.0) / 100.0;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
