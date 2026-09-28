package com.codealpha.stocktrading.ui.theme;

import java.awt.*;
import javax.swing.*;

/**
 * Modern Dark Fintech Theme Palette, typography, and UI constants.
 */
public final class UITheme {

    // Backgrounds
    public static final Color BG_DARK = new Color(15, 23, 42);       // #0F172A
    public static final Color BG_CARD = new Color(30, 41, 59);       // #1E293B
    public static final Color BG_CARD_HOVER = new Color(39, 53, 76); // #27354C
    public static final Color BG_INPUT = new Color(15, 23, 42);      // #0F172A
    public static final Color BORDER_COLOR = new Color(51, 65, 85);  // #334155
    public static final Color BORDER_LIGHT = new Color(71, 85, 105); // #475569

    // Accents & Signals
    public static final Color ACCENT_PRIMARY = new Color(99, 102, 241); // #6366F1 (Indigo)
    public static final Color ACCENT_HOVER = new Color(79, 70, 229);    // #4F46E5
    public static final Color COLOR_GAIN = new Color(16, 185, 129);     // #10B981 (Emerald Green)
    public static final Color COLOR_GAIN_BG = new Color(16, 185, 129, 35);
    public static final Color COLOR_LOSS = new Color(239, 68, 68);      // #EF4444 (Rose Red)
    public static final Color COLOR_LOSS_BG = new Color(239, 68, 68, 35);
    public static final Color COLOR_WARNING = new Color(245, 158, 11);  // #F59E0B (Amber)

    // Typography
    public static final Color TEXT_PRIMARY = new Color(248, 250, 252);   // #F8FAFC
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184); // #94A3B8
    public static final Color TEXT_MUTED = new Color(100, 116, 139);     // #64748B

    // Fonts
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 13);
    public static final Font FONT_MONO_BOLD = new Font("Consolas", Font.BOLD, 13);

    private UITheme() {}

    public static void enableAntiAliasing(Graphics g) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
    }
}
