package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.Stock;
import com.codealpha.stocktrading.ui.theme.UITheme;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.List;

/**
 * Custom 2D Graphics component that renders real-time stock price charts
 * with gradient fills, grid lines, high/low markers, and volume indicators.
 */
public class StockChartPanel extends JPanel {

    private Stock stock;

    public StockChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(500, 220));
    }

    public void setStock(Stock stock) {
        this.stock = stock;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        UITheme.enableAntiAliasing(g);
        Graphics2D g2 = (Graphics2D) g;

        int width = getWidth();
        int height = getHeight();

        // Background
        g2.setColor(UITheme.BG_CARD);
        g2.fillRoundRect(0, 0, width, height, 12, 12);
        g2.setColor(UITheme.BORDER_COLOR);
        g2.drawRoundRect(0, 0, width - 1, height - 1, 12, 12);

        if (stock == null) {
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(UITheme.FONT_BODY);
            String msg = "Select a stock to view real-time chart";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(msg, (width - fm.stringWidth(msg)) / 2, height / 2);
            return;
        }

        List<Double> history = stock.getPriceHistory();
        if (history.isEmpty()) return;

        int paddingLeft = 55;
        int paddingRight = 20;
        int paddingTop = 35;
        int paddingBottom = 30;

        int chartWidth = width - paddingLeft - paddingRight;
        int chartHeight = height - paddingTop - paddingBottom;

        // Find min and max price in history
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;

        for (double p : history) {
            if (p < minPrice) minPrice = p;
            if (p > maxPrice) maxPrice = p;
        }

        // Add 5% buffer above and below
        double priceRange = Math.max(0.5, maxPrice - minPrice);
        double paddedMin = Math.max(0.01, minPrice - (priceRange * 0.08));
        double paddedMax = maxPrice + (priceRange * 0.08);
        double effectiveRange = paddedMax - paddedMin;

        // Draw Header: Symbol, Name, Current Price, Change %
        g2.setFont(UITheme.FONT_HEADER);
        g2.setColor(UITheme.TEXT_PRIMARY);
        g2.drawString(stock.getSymbol() + "  " + stock.getCompanyName(), paddingLeft, 22);

        String priceStr = CurrencyFormatter.formatCurrency(stock.getCurrentPrice());
        String changeStr = CurrencyFormatter.formatPercent(stock.getPriceChangePercent());
        boolean isUp = stock.getPriceChange() >= 0;
        Color trendColor = isUp ? UITheme.COLOR_GAIN : UITheme.COLOR_LOSS;

        g2.setFont(UITheme.FONT_HEADER);
        g2.setColor(UITheme.TEXT_PRIMARY);
        FontMetrics fm = g2.getFontMetrics();
        int priceX = width - paddingRight - fm.stringWidth(priceStr + "  " + changeStr);
        g2.drawString(priceStr + " ", priceX, 22);
        
        g2.setColor(trendColor);
        g2.drawString("(" + changeStr + ")", priceX + fm.stringWidth(priceStr + " "), 22);

        // Draw horizontal grid lines and price labels (4 levels)
        g2.setFont(UITheme.FONT_SMALL);
        for (int i = 0; i <= 3; i++) {
            int y = paddingTop + (int) (chartHeight * (i / 3.0));
            double priceAtY = paddedMax - (effectiveRange * (i / 3.0));

            g2.setColor(new Color(51, 65, 85, 90));
            g2.drawLine(paddingLeft, y, width - paddingRight, y);

            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString(String.format("$%.2f", priceAtY), 10, y + 4);
        }

        if (history.size() < 2) return;

        // Build price points path
        GeneralPath linePath = new GeneralPath();
        GeneralPath areaPath = new GeneralPath();

        double stepX = (double) chartWidth / (history.size() - 1);

        double firstX = paddingLeft;
        double firstY = paddingTop + chartHeight - ((history.get(0) - paddedMin) / effectiveRange * chartHeight);

        linePath.moveTo(firstX, firstY);
        areaPath.moveTo(firstX, paddingTop + chartHeight);
        areaPath.lineTo(firstX, firstY);

        for (int i = 1; i < history.size(); i++) {
            double x = paddingLeft + (i * stepX);
            double y = paddingTop + chartHeight - ((history.get(i) - paddedMin) / effectiveRange * chartHeight);
            linePath.lineTo(x, y);
            areaPath.lineTo(x, y);
        }

        double lastX = paddingLeft + ((history.size() - 1) * stepX);
        areaPath.lineTo(lastX, paddingTop + chartHeight);
        areaPath.closePath();

        // Fill area gradient
        Color gradientTop = isUp ? new Color(16, 185, 129, 70) : new Color(239, 68, 68, 70);
        Color gradientBottom = new Color(15, 23, 42, 0);
        GradientPaint gp = new GradientPaint(0, paddingTop, gradientTop, 0, paddingTop + chartHeight, gradientBottom);
        g2.setPaint(gp);
        g2.fill(areaPath);

        // Draw main trend line
        g2.setColor(trendColor);
        g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(linePath);

        // Draw glowing point at current latest price
        double lastY = paddingTop + chartHeight - ((history.get(history.size() - 1) - paddedMin) / effectiveRange * chartHeight);
        g2.setColor(new Color(trendColor.getRed(), trendColor.getGreen(), trendColor.getBlue(), 120));
        g2.fillOval((int) lastX - 5, (int) lastY - 5, 10, 10);
        g2.setColor(trendColor);
        g2.fillOval((int) lastX - 3, (int) lastY - 3, 6, 6);
    }
}
