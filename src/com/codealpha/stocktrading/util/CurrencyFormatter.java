package com.codealpha.stocktrading.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility methods for formatting financial numbers, percentages, and currencies.
 */
public final class CurrencyFormatter {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.US);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");
    private static final DecimalFormat VOLUME_FORMAT = new DecimalFormat("#,###");

    private CurrencyFormatter() {}

    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatPercent(double percent) {
        if (percent > 0) {
            return String.format("+%.2f%%", percent);
        } else if (percent < 0) {
            return String.format("%.2f%%", percent);
        } else {
            return "0.00%";
        }
    }

    public static String formatDecimal(double value) {
        return DECIMAL_FORMAT.format(value);
    }

    public static String formatVolume(long volume) {
        if (volume >= 1_000_000_000) {
            return String.format("%.2fB", volume / 1_000_000_000.0);
        } else if (volume >= 1_000_000) {
            return String.format("%.2fM", volume / 1_000_000.0);
        } else if (volume >= 1_000) {
            return String.format("%.1fK", volume / 1_000.0);
        }
        return VOLUME_FORMAT.format(volume);
    }

    public static String formatMarketCap(double capInBillions) {
        if (capInBillions >= 1000) {
            return String.format("$%.2fT", capInBillions / 1000.0);
        }
        return String.format("$%.2fB", capInBillions);
    }
}
