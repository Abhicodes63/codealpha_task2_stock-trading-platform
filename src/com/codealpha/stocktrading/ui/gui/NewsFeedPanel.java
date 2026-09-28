package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.MarketNews;
import com.codealpha.stocktrading.service.MarketService;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * News Feed Panel displaying breaking market headlines, sentiment analysis,
 * and catalyst signals.
 */
public class NewsFeedPanel extends JPanel {

    private final MarketService marketService;
    private final JPanel listContainer;

    public NewsFeedPanel(MarketService marketService) {
        this.marketService = marketService;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("FINANCIAL NEWS WIRE & MARKET CATALYSTS");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Feed Container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setBackground(UITheme.BG_DARK);

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.getViewport().setBackground(UITheme.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
        refreshNews();
    }

    public synchronized void refreshNews() {
        listContainer.removeAll();
        List<MarketNews> newsList = marketService.getNewsFeed();

        for (MarketNews item : newsList) {
            listContainer.add(createNewsCard(item));
            listContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createNewsCard(MarketNews news) {
        ModernComponents.ModernCard card = new ModernComponents.ModernCard(8);
        card.setLayout(new BorderLayout(10, 8));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 85));

        // Top Row: Time + Ticker + Sentiment Pill
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);

        JLabel timeLabel = new JLabel(news.getFormattedTime());
        timeLabel.setFont(UITheme.FONT_SMALL);
        timeLabel.setForeground(UITheme.TEXT_MUTED);

        JLabel symLabel = new JLabel("[" + news.getAffectedSymbol() + "]");
        symLabel.setFont(UITheme.FONT_BODY_BOLD);
        symLabel.setForeground(UITheme.ACCENT_PRIMARY);

        Color sentBg = news.getSentiment() == MarketNews.Sentiment.BULLISH ? UITheme.COLOR_GAIN_BG :
                (news.getSentiment() == MarketNews.Sentiment.BEARISH ? UITheme.COLOR_LOSS_BG : new Color(71, 85, 105, 50));
        Color sentFg = news.getSentiment() == MarketNews.Sentiment.BULLISH ? UITheme.COLOR_GAIN :
                (news.getSentiment() == MarketNews.Sentiment.BEARISH ? UITheme.COLOR_LOSS : UITheme.TEXT_SECONDARY);

        ModernComponents.PillBadge badge = new ModernComponents.PillBadge(
                news.getSentiment().name() + " (" + (news.getImpactPercent() >= 0 ? "+" : "") + 
                String.format("%.1f%%", news.getImpactPercent()) + ")", 
                sentBg, sentFg
        );

        topRow.add(timeLabel);
        topRow.add(symLabel);
        topRow.add(badge);

        JLabel headlineLabel = new JLabel(news.getHeadline());
        headlineLabel.setFont(UITheme.FONT_BODY_BOLD);
        headlineLabel.setForeground(UITheme.TEXT_PRIMARY);

        card.add(topRow, BorderLayout.NORTH);
        card.add(headlineLabel, BorderLayout.CENTER);

        return card;
    }
}
