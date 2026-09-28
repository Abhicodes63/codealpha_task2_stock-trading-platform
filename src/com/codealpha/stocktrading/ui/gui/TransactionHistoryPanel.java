package com.codealpha.stocktrading.ui.gui;

import com.codealpha.stocktrading.model.Transaction;
import com.codealpha.stocktrading.model.TransactionType;
import com.codealpha.stocktrading.model.User;
import com.codealpha.stocktrading.service.PersistenceService;
import com.codealpha.stocktrading.service.UserService;
import com.codealpha.stocktrading.ui.theme.ModernComponents;
import com.codealpha.stocktrading.ui.theme.UITheme;
import com.codealpha.stocktrading.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Transaction History Panel displaying all historical trades, cash deposits,
 * realized gains/losses, and CSV export capability.
 */
public class TransactionHistoryPanel extends JPanel {

    private final UserService userService;
    private final PersistenceService persistenceService;

    private JTable txTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;

    private final String[] columns = {
            "Txn ID", "Date & Time", "Type", "Symbol", "Quantity", "Price", "Total Amount", "Realized P/L", "Notes"
    };

    public TransactionHistoryPanel(UserService userService, PersistenceService persistenceService) {
        this.userService = userService;
        this.persistenceService = persistenceService;

        setLayout(new BorderLayout(12, 12));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        initComponents();
        refreshTransactions();
    }

    private void initComponents() {
        // Header & Controls
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("TRANSACTION AUDIT LOG & ORDER LEDGER");
        title.setFont(UITheme.FONT_SUBHEADER);
        title.setForeground(UITheme.TEXT_SECONDARY);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel filterLbl = new JLabel("Filter Type:");
        filterLbl.setForeground(UITheme.TEXT_SECONDARY);

        filterCombo = new JComboBox<>(new String[]{"ALL", "BUY", "SELL", "DEPOSIT", "WITHDRAW"});
        filterCombo.setBackground(UITheme.BG_INPUT);
        filterCombo.setForeground(UITheme.TEXT_PRIMARY);
        filterCombo.addActionListener(e -> refreshTransactions());

        JButton exportCsvBtn = ModernComponents.createSecondaryButton("Export CSV");
        exportCsvBtn.addActionListener(e -> exportToCsv());

        controls.add(filterLbl);
        controls.add(filterCombo);
        controls.add(exportCsvBtn);

        header.add(title, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        txTable = new JTable(tableModel);
        ModernComponents.styleTable(txTable);
        setupRenderers();

        JScrollPane scrollPane = new JScrollPane(txTable);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        add(scrollPane, BorderLayout.CENTER);
    }

    private void setupRenderers() {
        DefaultTableCellRenderer plRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (value != null) {
                    String str = value.toString();
                    if (str.startsWith("+")) c.setForeground(UITheme.COLOR_GAIN);
                    else if (str.startsWith("-")) c.setForeground(UITheme.COLOR_LOSS);
                    else c.setForeground(UITheme.TEXT_MUTED);
                }
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : new Color(24, 32, 47));
                }
                return c;
            }
        };

        DefaultTableCellRenderer typeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                if (value != null) {
                    String type = value.toString();
                    if (type.contains("BUY") || type.contains("DEPOSIT")) c.setForeground(UITheme.COLOR_GAIN);
                    else if (type.contains("SELL") || type.contains("WITHDRAW")) c.setForeground(UITheme.COLOR_LOSS);
                    else c.setForeground(UITheme.TEXT_PRIMARY);
                }
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? UITheme.BG_CARD : new Color(24, 32, 47));
                }
                return c;
            }
        };

        txTable.getColumnModel().getColumn(2).setCellRenderer(typeRenderer);
        txTable.getColumnModel().getColumn(7).setCellRenderer(plRenderer);

        txTable.getColumnModel().getColumn(0).setPreferredWidth(90);
        txTable.getColumnModel().getColumn(1).setPreferredWidth(140);
        txTable.getColumnModel().getColumn(8).setPreferredWidth(200);
    }

    public synchronized void refreshTransactions() {
        User user = userService.getCurrentUser();
        if (user == null) return;

        String filter = (String) filterCombo.getSelectedItem();
        List<Transaction> txns = user.getTransactions();
        tableModel.setRowCount(0);

        for (Transaction t : txns) {
            boolean matches = "ALL".equals(filter) || t.getType().name().equalsIgnoreCase(filter);
            if (matches) {
                double pl = t.getRealizedProfitLoss();
                String plStr = pl != 0 ? ((pl > 0 ? "+" : "") + CurrencyFormatter.formatCurrency(pl)) : "—";
                
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getFormattedTimestamp(),
                        t.getType().name(),
                        t.getStockSymbol(),
                        t.getQuantity(),
                        CurrencyFormatter.formatCurrency(t.getPricePerShare()),
                        CurrencyFormatter.formatCurrency(t.getTotalAmount()),
                        plStr,
                        t.getNotes()
                });
            }
        }
    }

    private void exportToCsv() {
        User user = userService.getCurrentUser();
        if (user == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("Transaction ID,Timestamp,Type,Symbol,Quantity,PricePerShare,TotalAmount,RealizedProfitLoss,Notes\n");

        for (Transaction t : user.getTransactions()) {
            sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%d,%.2f,%.2f,%.2f,\"%s\"\n",
                    t.getId(),
                    t.getFormattedTimestamp(),
                    t.getType().name(),
                    t.getStockSymbol(),
                    t.getQuantity(),
                    t.getPricePerShare(),
                    t.getTotalAmount(),
                    t.getRealizedProfitLoss(),
                    t.getNotes().replace("\"", "\"\"")));
        }

        String filename = "Transactions_" + user.getUsername() + "_" + System.currentTimeMillis() + ".csv";
        if (persistenceService.exportReportToFile(filename, sb.toString())) {
            JOptionPane.showMessageDialog(this, 
                    "Transactions exported to CSV:\ndata/" + filename, 
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to export CSV.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
