# CodeAlpha - Stock Trading Platform (Task 2)

![Java](https://img.shields.io/badge/Language-Java%20SE%2017+-orange.svg)
![Architecture](https://img.shields.io/badge/Architecture-Object--Oriented%20(OOP)-blue.svg)
![UI](https://img.shields.io/badge/GUI-Modern%20Swing%20Dark%20Fintech-success.svg)
![Persistence](https://img.shields.io/badge/Persistence-File%20I%2FO%20%26%20CSV%20Export-purple.svg)

An enterprise-grade, object-oriented **Stock Trading Simulation Platform** built in Java for the **CodeAlpha Java Programming Internship** (Task 2).

---

## 🌟 Key Features & Problem Statement Fulfillment

| Requirement | Implementation Detail |
|---|---|
| **1. Simulate Stock Trading Environment** | Real-time market simulation engine driven by background multi-threading, Geometric Brownian motion price fluctuations, sector trends, market index (*Alpha50 Index*), and breaking financial news catalysts. |
| **2. Market Data Display** | Live quote tables with price changes ($ and %), volume, market capitalization, sector filters, instant search, and real-time interactive 2D stock price charts. |
| **3. Buy & Sell Operations** | Immediate Market Orders, Limit Orders, and Stop-Loss orders with balance checks, share validations, weighted-average cost basis calculation, and transaction logging. |
| **4. Track Portfolio Performance** | Real-time calculations of Net Worth, Cash Balance, Active Stock Equity, Unrealized P/L ($ and %), Realized P/L, Diversification Score (0–100), and asset allocation breakdown. |
| **5. Object-Oriented Design (OOP)** | Clean separation of concerns across Models, Services, Utilities, and UI components using design patterns (Observer, Factory, Singleton, Strategy). |
| **6. File I/O & Persistence** | Automatic serialization of user accounts, portfolios, order ledgers, and transactions to local storage (`data/`), plus export of portfolio reports (.txt) and transaction ledgers (.csv). |
| **7. Dual User Interfaces** | Includes both a modern **Dark Theme Swing Desktop GUI** and an **Interactive Terminal CLI** mode. |

---

## 🏗️ Project Architecture & OOP Structure

```
com.codealpha.stocktrading/
│
├── Main.java                        # Application entry point (GUI / CLI routing)
│
├── model/                           # Domain Models (OOP Entities & State)
│   ├── Stock.java                   # Stock entity (price, history, volatility, beta, volume)
│   ├── Holding.java                 # Asset position with weighted average cost basis & P/L
│   ├── Portfolio.java               # Portfolio container (cash, active holdings, valuations)
│   ├── Transaction.java             # Immutable audit record of executed trades & cash flow
│   ├── TransactionType.java         # BUY, SELL, DEPOSIT, WITHDRAW, DIVIDEND
│   ├── Order.java                   # Limit / Stop-Loss order entity
│   ├── OrderType.java               # MARKET, LIMIT, STOP_LOSS
│   ├── OrderStatus.java             # PENDING, EXECUTED, CANCELLED
│   ├── User.java                    # Trader account profile, watchlist, and portfolio
│   ├── MarketNews.java              # Simulated market catalyst headlines & sentiment
│   └── Sector.java                  # Industry sectors (Tech, Finance, Healthcare, etc.)
│
├── service/                         # Business Logic & Core Simulation Engines
│   ├── MarketService.java           # Multi-threaded market simulation ticker & news generator
│   ├── TradingService.java          # Order validation, trade execution, and limit triggers
│   ├── PortfolioService.java        # Portfolio analytics, diversification rating, & reporting
│   ├── UserService.java             # Account authentication, multi-user switching, session state
│   └── PersistenceService.java      # Local file I/O serialization & report export
│
├── ui/                              # User Interface Layer
│   ├── theme/
│   │   ├── UITheme.java             # Dark fintech color tokens, typography, and anti-aliasing
│   │   └── ModernComponents.java    # Custom cards, stat badges, buttons, tables, text fields
│   ├── gui/
│   │   ├── MainFrame.java           # Master dashboard window with live clock & simulation sync
│   │   ├── MarketPanel.java         # Live market quotes, search, sector filters, & chart view
│   │   ├── PortfolioPanel.java      # Portfolio metrics cards, positions table, cash actions
│   │   ├── StockChartPanel.java     # Custom 2D Graphics real-time price trend visualizer
│   │   ├── TradeDialog.java         # Modal dialog for executing Buy/Sell/Limit trades
│   │   ├── DepositWithdrawDialog.java # Modal dialog for electronic cash deposits/withdrawals
│   │   ├── WatchlistPanel.java      # Starred stocks tracker with quick-trade buttons
│   │   ├── TransactionHistoryPanel.java # Comprehensive ledger with CSV export
│   │   ├── NewsFeedPanel.java       # Financial news wire with sentiment indicators
│   │   └── LoginRegisterDialog.java # User authentication & profile switching
│   └── cli/
│       └── StockTradingCLI.java     # Full-featured interactive terminal console
│
└── util/
    └── CurrencyFormatter.java       # Formats currencies, percentages, and trade volumes
```

---

## 🚀 How to Build and Run

### Prerequisites
- Java Development Kit (JDK 17 or higher, compatible with Java 21/26)

### Option 1: Quick Run (Batch / Script)
- **Launch GUI Mode (Default):** Double-click or run `run.bat` (or `./compile_and_run.ps1` in PowerShell).
- **Launch CLI Mode (Terminal):** Double-click or run `run-cli.bat`.

### Option 2: Manual Terminal Commands

1. **Compile all Java source files:**
   ```bash
   javac -d bin -sourcepath src src/com/codealpha/stocktrading/Main.java
   ```

2. **Run in GUI Mode (Modern Dark Fintech Interface):**
   ```bash
   java -cp bin com.codealpha.stocktrading.Main
   ```

3. **Run in CLI Mode (Interactive Terminal Interface):**
   ```bash
   java -cp bin com.codealpha.stocktrading.Main --cli
   ```

---

## 💼 Demonstration Guide for CodeAlpha Video Submission

When recording your project video for LinkedIn and CodeAlpha submission:

1. **Overview & Architecture:**
   - Mention that the platform is developed in Java using clean Object-Oriented Programming (OOP) principles.
   - Highlight the separation between data models, simulation services, persistence, and UI.
2. **Live Market Simulation:**
   - Demonstrate the real-time price updates and the interactive price trend chart.
   - Show how stocks can be searched and filtered by sector (Technology, Finance, Healthcare, etc.).
3. **Trading & Position Execution:**
   - Execute a **BUY** order for a stock (e.g., TSLA, NVDA).
   - Show how cash balance updates immediately and the holding is added with weighted average cost basis.
   - Execute a **SELL** order and highlight the calculated realized profit/loss.
4. **Portfolio Analytics & Risk:**
   - Navigate to the **Portfolio Dashboard** to display Net Worth, Unrealized P/L, and Diversification Score.
   - Demonstrate the **Deposit / Withdraw Cash** functionality.
5. **Ledger, News & Persistence:**
   - Show the **Financial News Wire** affecting market sentiment.
   - Demonstrate **Export CSV** and **Export Portfolio Report (.txt)**.
   - Show how restarting the application preserves user accounts and portfolios.

---

## 👤 Author
- **Internship:** CodeAlpha Java Programming Internship
- **Project Repository:** `CodeAlpha_StockTradingPlatform`
