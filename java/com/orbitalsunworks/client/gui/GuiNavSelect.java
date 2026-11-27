package com.orbitalsunworks.client.gui;

import java.util.UUID;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageRequestSatellites;
import com.orbitalsunworks.network.MessageSatelliteList;
import com.orbitalsunworks.network.MessageTravelToSatellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GUI de seleção de satélite (modo GuiScreen sem Container),
 * alinhada visualmente ao painel aeroespacial da GuiNavRocket.
 */
@SideOnly(Side.CLIENT)
public class GuiNavSelect extends GuiScreen {

    private static MessageSatelliteList lastList;

    private int scrollOffset = 0;
    private static final int ROWS_VISIBLE = 5;
    private static final int PANEL_W = 256;
    private static final int PANEL_H = 218;

    private static final int LIST_X = 10;
    private static final int LIST_Y = 40;
    private static final int CARD_W = 220;
    private static final int CARD_H = 26;
    private static final int ROW_STEP = 28;

    private static final int BTN_X_REL = 177;
    private static final int BTN_Y_REL = 4;
    private static final int BTN_W = 48;
    private static final int BTN_H = 18;

    private static final int SCROLL_X = 234;
    private static final int SCROLL_W = 12;
    private static final int SCROLL_TOP_Y = 40;
    private static final int SCROLL_BOT_Y = 162;
    private static final int SCROLL_BTN_H = 16;

    private final int blockX, blockY, blockZ;

    public GuiNavSelect(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    public static void feedList(MessageSatelliteList list) {
        lastList = list;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        OrbitalSunworks.network.sendToServer(new MessageRequestSatellites());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (lastList == null && this.mc.theWorld != null
            && this.mc.theWorld.getTotalWorldTime() % 20 == 0) {
            OrbitalSunworks.network.sendToServer(new MessageRequestSatellites());
        }
        int n = lastList != null ? lastList.getCount() : 0;
        int maxScroll = Math.max(0, n - ROWS_VISIBLE);
        if (this.scrollOffset > maxScroll) {
            this.scrollOffset = maxScroll;
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && lastList != null) {
            int maxScroll = Math.max(0, lastList.getCount() - ROWS_VISIBLE);
            if (wheel > 0 && this.scrollOffset > 0) {
                this.scrollOffset--;
            } else if (wheel < 0 && this.scrollOffset < maxScroll) {
                this.scrollOffset++;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0 || lastList == null) {
            return;
        }
        int x0 = (this.width - PANEL_W) / 2;
        int y0 = (this.height - PANEL_H) / 2;
        int mx = mouseX - x0;
        int my = mouseY - y0;
        int n = lastList.getCount();
        int maxScroll = Math.max(0, n - ROWS_VISIBLE);

        if (mx >= SCROLL_X && mx < SCROLL_X + SCROLL_W
            && my >= SCROLL_TOP_Y && my < SCROLL_TOP_Y + SCROLL_BTN_H) {
            if (this.scrollOffset > 0) {
                this.scrollOffset--;
                playClick(0.9F);
            }
            return;
        }

        if (mx >= SCROLL_X && mx < SCROLL_X + SCROLL_W
            && my >= SCROLL_BOT_Y && my < SCROLL_BOT_Y + SCROLL_BTN_H) {
            if (this.scrollOffset < maxScroll) {
                this.scrollOffset++;
                playClick(0.9F);
            }
            return;
        }

        int rows = Math.min(ROWS_VISIBLE, Math.max(0, n - this.scrollOffset));
        for (int i = 0; i < rows; i++) {
            int by = LIST_Y + i * ROW_STEP + BTN_Y_REL;
            if (mx >= BTN_X_REL && mx < BTN_X_REL + BTN_W && my >= by && my < by + BTN_H) {
                int row = this.scrollOffset + i;
                if (row >= 0 && row < n) {
                    playClick(1.0F);
                    UUID satId = UUID.fromString(lastList.getUuid(row));
                    OrbitalSunworks.network.sendToServer(new MessageTravelToSatellite(
                        satId, this.blockX, this.blockY, this.blockZ));
                    this.mc.displayGuiScreen(null);
                }
                return;
            }
        }
    }

    private void playClick(float pitch) {
        this.mc.getSoundHandler().playSound(
            PositionedSoundRecord.func_147674_a(new ResourceLocation("random.click"), pitch));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glDisable(GL11.GL_LIGHTING);

        int x0 = (this.width - PANEL_W) / 2;
        int y0 = (this.height - PANEL_H) / 2;
        int mx = mouseX - x0;
        int my = mouseY - y0;

        GuiTheme.drawAerospacePanel(x0, y0, PANEL_W, PANEL_H);
        GuiTheme.drawHeaderBar(x0 + 3, y0 + 2, PANEL_W - 6, 20);
        drawRect(x0 + LIST_X, y0 + 37, x0 + LIST_X + CARD_W, y0 + 38, GuiTheme.BORDER_SUB);

        String title = StatCollector.translateToLocal("gui.orbitalsunworks.nav.title");
        this.fontRendererObj.drawStringWithShadow(title,
            x0 + (PANEL_W - this.fontRendererObj.getStringWidth(title)) / 2, y0 + 8, GuiTheme.TEXT_TITLE);

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.name"),
            x0 + 16, y0 + 27, GuiTheme.TEXT_LABEL);
        String tierLbl = StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.tier");
        this.fontRendererObj.drawString(tierLbl,
            x0 + 114 - this.fontRendererObj.getStringWidth(tierLbl) / 2, y0 + 27, GuiTheme.TEXT_LABEL);
        String healthLbl = StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.health");
        this.fontRendererObj.drawString(healthLbl,
            x0 + 150 - this.fontRendererObj.getStringWidth(healthLbl) / 2, y0 + 27, GuiTheme.TEXT_LABEL);

        int n = lastList != null ? lastList.getCount() : 0;
        String launchTxt = StatCollector.translateToLocal("gui.orbitalsunworks.launch");

        if (n == 0) {
            GuiTheme.drawSubPanel(x0 + LIST_X, y0 + LIST_Y, CARD_W, 138);
            String none = StatCollector.translateToLocal("gui.orbitalsunworks.nav.none");
            this.fontRendererObj.drawStringWithShadow(none,
                x0 + LIST_X + (CARD_W - this.fontRendererObj.getStringWidth(none)) / 2,
                y0 + LIST_Y + 46, GuiTheme.TEXT_BODY);
            String hint = StatCollector.translateToLocal("gui.orbitalsunworks.nav.hint");
            this.fontRendererObj.drawSplitString(hint, x0 + LIST_X + 14, y0 + LIST_Y + 66,
                CARD_W - 28, GuiTheme.TEXT_MUTED);
        } else {
            int rows = Math.min(ROWS_VISIBLE, Math.max(0, n - this.scrollOffset));
            for (int i = 0; i < rows; i++) {
                int row = this.scrollOffset + i;
                int cy = y0 + LIST_Y + i * ROW_STEP;
                int relCy = LIST_Y + i * ROW_STEP;

                GuiTheme.drawSubPanel(x0 + LIST_X, cy, CARD_W, CARD_H);
                boolean cardHover = mx >= LIST_X && mx < LIST_X + CARD_W
                    && my >= relCy && my < relCy + CARD_H;
                if (cardHover) {
                    drawRect(x0 + LIST_X + 1, cy + 1, x0 + LIST_X + CARD_W - 1, cy + 2, 0xFF28557A);
                }

                String name = lastList.getName(row);
                float health = Math.max(0f, Math.min(1f, lastList.getHealth(row)));
                int tier = lastList.getTier(row);

                drawRect(x0 + LIST_X + 2, cy + 3, x0 + LIST_X + 4, cy + CARD_H - 3,
                    health > 0.4f ? 0xFF55E088 : 0xFFFF5555);

                String shown = name;
                while (this.fontRendererObj.getStringWidth(shown) > 84 && shown.length() > 3) {
                    shown = shown.substring(0, shown.length() - 2) + "...";
                }
                this.fontRendererObj.drawStringWithShadow(shown, x0 + LIST_X + 7, cy + 9, 0xFFFFFF);

                GuiTheme.drawBadge(this.fontRendererObj, x0 + 104, cy + 6,
                    "T" + tier, 0xFF1F2B1A, 0xFF8C7228, GuiTheme.TEXT_GOLD);

                String h = Math.round(health * 100) + "%";
                this.fontRendererObj.drawString(h,
                    x0 + 150 - this.fontRendererObj.getStringWidth(h) / 2, cy + 4,
                    health > 0.4f ? GuiTheme.TEXT_OK : GuiTheme.TEXT_WARN);

                GuiTheme.drawProgressBar(x0 + 129, cy + 15, 42, 5, health,
                    health > 0.4f ? 0xFF7CFC00 : 0xFFFF5555,
                    health > 0.4f ? 0xFF3E9E00 : 0xFFB81E1E);

                boolean btnHover = mx >= BTN_X_REL && mx < BTN_X_REL + BTN_W
                    && my >= relCy + BTN_Y_REL && my < relCy + BTN_Y_REL + BTN_H;
                GuiTheme.drawButton(this.fontRendererObj,
                    x0 + BTN_X_REL, cy + BTN_Y_REL, BTN_W, BTN_H,
                    launchTxt, btnHover, true, false);
            }
        }

        // Scrollbar
        boolean canUp = this.scrollOffset > 0;
        boolean canDown = this.scrollOffset + ROWS_VISIBLE < n;
        boolean upHover = mx >= SCROLL_X && mx < SCROLL_X + SCROLL_W
            && my >= SCROLL_TOP_Y && my < SCROLL_TOP_Y + SCROLL_BTN_H;
        boolean downHover = mx >= SCROLL_X && mx < SCROLL_X + SCROLL_W
            && my >= SCROLL_BOT_Y && my < SCROLL_BOT_Y + SCROLL_BTN_H;
        GuiTheme.drawButton(this.fontRendererObj, x0 + SCROLL_X, y0 + SCROLL_TOP_Y,
            SCROLL_W, SCROLL_BTN_H, "^", upHover, canUp, false);
        GuiTheme.drawButton(this.fontRendererObj, x0 + SCROLL_X, y0 + SCROLL_BOT_Y,
            SCROLL_W, SCROLL_BTN_H, "v", downHover, canDown, false);

        int trackY = y0 + SCROLL_TOP_Y + SCROLL_BTN_H + 2;
        int trackH = (SCROLL_BOT_Y - (SCROLL_TOP_Y + SCROLL_BTN_H)) - 4;
        drawRect(x0 + SCROLL_X + 2, trackY, x0 + SCROLL_X + SCROLL_W - 2, trackY + trackH, 0xFF0A111B);
        if (n > ROWS_VISIBLE) {
            int thumbH = Math.max(12, trackH * ROWS_VISIBLE / n);
            int maxOff = n - ROWS_VISIBLE;
            int thumbY = trackY + (trackH - thumbH) * this.scrollOffset / Math.max(1, maxOff);
            drawRect(x0 + SCROLL_X + 3, thumbY, x0 + SCROLL_X + SCROLL_W - 3, thumbY + thumbH,
                GuiTheme.ACCENT_CYAN);
        }

        GuiTheme.drawSubPanel(x0 + LIST_X, y0 + 183, 236, 26);
        String tip = StatCollector.translateToLocal("gui.orbitalsunworks.nav.footer");
        String count = n == 0 ? "0 / 0"
            : (this.scrollOffset + 1) + "-" + Math.min(this.scrollOffset + ROWS_VISIBLE, n) + " / " + n;
        int countW = this.fontRendererObj.getStringWidth(count);
        int maxTipW = 224 - countW - 8;
        while (this.fontRendererObj.getStringWidth(tip) > maxTipW && tip.length() > 6) {
            tip = tip.substring(0, tip.length() - 2) + "...";
        }
        this.fontRendererObj.drawString(tip, x0 + LIST_X + 6, y0 + 192, GuiTheme.TEXT_MUTED);
        this.fontRendererObj.drawString(count, x0 + 240 - countW, y0 + 192, GuiTheme.TEXT_TITLE);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.displayGuiScreen(null);
        }
    }
}