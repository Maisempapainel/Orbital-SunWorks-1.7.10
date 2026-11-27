package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Paleta e primitivas aeroespaciais compartilhadas por todas as HUDs do mod.
 */
@SideOnly(Side.CLIENT)
public final class GuiTheme {

    public static final int BG_OUTER = 0xFF070C14;
    public static final int BG_INNER = 0xFF0F1826;
    public static final int BG_HEADER = 0xFF152438;
    public static final int BG_SUB = 0xFF0A111C;

    public static final int BORDER_MAIN = 0xFF28557A;
    public static final int BORDER_SUB = 0xFF1E3B56;
    public static final int ACCENT_CYAN = 0xFF4BE0FF;
    public static final int ACCENT_DIM = 0xFF1B7799;

    public static final int TEXT_TITLE = 0x55E0FF;
    public static final int TEXT_LABEL = 0x9FB4C7;
    public static final int TEXT_BODY = 0xD2DFEC;
    public static final int TEXT_MUTED = 0x62778E;
    public static final int TEXT_GOLD = 0xFFD970;
    public static final int TEXT_OK = 0x7CFC00;
    public static final int TEXT_WARN = 0xFF5555;

    private GuiTheme() {
    }

    public static void drawAerospacePanel(int x, int y, int w, int h) {
        // Sombra / contorno externo
        Gui.drawRect(x, y, x + w, y + h, BG_OUTER);
        Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, BORDER_MAIN);
        Gui.drawRect(x + 2, y + 2, x + w - 2, y + h - 2, BG_INNER);

        // Cantoneiras luminosas ciano
        int c = ACCENT_CYAN;
        int len = 6;
        Gui.drawRect(x + 1, y + 1, x + 1 + len, y + 2, c);
        Gui.drawRect(x + 1, y + 1, x + 2, y + 1 + len, c);

        Gui.drawRect(x + w - 1 - len, y + 1, x + w - 1, y + 2, c);
        Gui.drawRect(x + w - 2, y + 1, x + w - 1, y + 1 + len, c);

        Gui.drawRect(x + 1, y + h - 2, x + 1 + len, y + h - 1, c);
        Gui.drawRect(x + 1, y + h - 1 - len, x + 2, y + h - 1, c);

        Gui.drawRect(x + w - 1 - len, y + h - 2, x + w - 1, y + h - 1, c);
        Gui.drawRect(x + w - 2, y + h - 1 - len, x + w - 1, y + h - 1, c);
    }

    public static void drawHeaderBar(int x, int y, int w, int h) {
        Gui.drawRect(x, y, x + w, y + h, BG_HEADER);
        Gui.drawRect(x, y + h - 1, x + w, y + h, BORDER_MAIN);
        Gui.drawRect(x + 6, y + h - 1, x + w - 6, y + h, ACCENT_DIM);
    }

    public static void drawSubPanel(int x, int y, int w, int h) {
        Gui.drawRect(x, y, x + w, y + h, BORDER_SUB);
        Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, BG_SUB);
    }

    public static void drawSlotBox(int sx, int sy) {
        drawSlotBox(sx, sy, BORDER_MAIN, 0xFF1A283A);
    }

    public static void drawSlotBox(int sx, int sy, int borderCol, int fillCol) {
        Gui.drawRect(sx, sy, sx + 18, sy + 18, borderCol);
        Gui.drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF0A1019);
        Gui.drawRect(sx + 1, sy + 1, sx + 17, sy + 2, 0xFF101B29);
        Gui.drawRect(sx + 2, sy + 2, sx + 17, sy + 17, fillCol);
    }

    public static void drawProgressBar(int x, int y, int w, int h, float pct, int colFillTop, int colFillBot) {
        Gui.drawRect(x, y, x + w, y + h, BORDER_MAIN);
        Gui.drawRect(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF080D15);
        float p = Math.max(0f, Math.min(1f, pct));
        int innerW = w - 2;
        int fillW = Math.round(innerW * p);
        if (fillW > 0) {
            int mid = y + 1 + (h - 2) / 2;
            Gui.drawRect(x + 1, y + 1, x + 1 + fillW, mid, colFillTop);
            Gui.drawRect(x + 1, mid, x + 1 + fillW, y + h - 1, colFillBot);
        }
    }

    public static void drawButton(FontRenderer fr, int x, int y, int w, int h,
            String text, boolean hover, boolean enabled, boolean danger) {
        int border;
        int topFill;
        int botFill;
        int textCol;
        if (!enabled) {
            border = danger ? 0xFF6B2A30 : 0xFF2B3848;
            topFill = danger ? 0xFF281216 : 0xFF161D26;
            botFill = danger ? 0xFF1E0D10 : 0xFF11171F;
            textCol = danger ? 0x995555 : 0x5E7185;
        } else if (danger) {
            border = hover ? 0xFFFF7070 : 0xFFC93B42;
            topFill = hover ? 0xFF782228 : 0xFF56181D;
            botFill = hover ? 0xFF4D1519 : 0xFF391014;
            textCol = hover ? 0xFFFFFF : 0xFFD0D0;
        } else {
            border = hover ? ACCENT_CYAN : 0xFF2E85AA;
            topFill = hover ? 0xFF216A91 : 0xFF164766;
            botFill = hover ? 0xFF14425E : 0xFF0E2F46;
            textCol = hover ? 0xFFFFFF : 0xB8F2FF;
        }

        Gui.drawRect(x, y, x + w, y + h, border);
        int mid = y + h / 2;
        Gui.drawRect(x + 1, y + 1, x + w - 1, mid, topFill);
        Gui.drawRect(x + 1, mid, x + w - 1, y + h - 1, botFill);
        if (enabled) {
            Gui.drawRect(x + 1, y + 1, x + w - 1, y + 2, hover ? 0x80FFFFFF : 0x40FFFFFF);
        }
        String shown = fr.trimStringToWidth(text, Math.max(8, w - 6));
        int tx = x + (w - fr.getStringWidth(shown)) / 2;
        int ty = y + (h - 8) / 2;
        fr.drawStringWithShadow(shown, tx, ty, textCol);
    }

    public static void drawBadge(FontRenderer fr, int x, int y, String text, int bg, int border, int fg) {
        int w = fr.getStringWidth(text) + 8;
        Gui.drawRect(x, y, x + w, y + 11, border);
        Gui.drawRect(x + 1, y + 1, x + w - 1, y + 10, bg);
        fr.drawString(text, x + 4, y + 2, fg);
    }
}