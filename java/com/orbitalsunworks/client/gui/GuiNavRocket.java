package com.orbitalsunworks.client.gui;

import java.util.UUID;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageRequestSatellites;
import com.orbitalsunworks.network.MessageSatelliteList;
import com.orbitalsunworks.network.MessageTravelToSatellite;
import com.orbitalsunworks.rocket.ContainerNavRocket;
import com.orbitalsunworks.rocket.TileNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial do Foguete de Navegação (256x218).
 * Exibe todos os satélites do jogador com cartões centralizados, barra de
 * integridade, badge de Tier, botões LANÇAR integrados e scroll por roda do mouse.
 */
@SideOnly(Side.CLIENT)
public class GuiNavRocket extends GuiContainer {

    private static MessageSatelliteList lastList;

    private int scrollOffset = 0;
    private static final int ROWS_VISIBLE = 5;

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

    private final TileNavRocket tile;
    private int requestTimer = 0;

    public GuiNavRocket(InventoryPlayer playerInv, TileNavRocket tile) {
        super(new ContainerNavRocket(playerInv, tile));
        this.tile = tile;
        this.xSize = 256;
        this.ySize = 218;
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
        this.requestTimer++;
        if (lastList == null && this.requestTimer % 20 == 0) {
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
        int x0 = (this.width - this.xSize) / 2;
        int y0 = (this.height - this.ySize) / 2;
        int mx = mouseX - x0;
        int my = mouseY - y0;
        int n = lastList.getCount();
        int maxScroll = Math.max(0, n - ROWS_VISIBLE);

        // Seta para cima
        if (mx >= SCROLL_X && mx < SCROLL_X + SCROLL_W
            && my >= SCROLL_TOP_Y && my < SCROLL_TOP_Y + SCROLL_BTN_H) {
            if (this.scrollOffset > 0) {
                this.scrollOffset--;
                playClick(0.9F);
            }
            return;
        }

        // Seta para baixo
        if (mx >= SCROLL_X && mx < SCROLL_X + SCROLL_W
            && my >= SCROLL_BOT_Y && my < SCROLL_BOT_Y + SCROLL_BTN_H) {
            if (this.scrollOffset < maxScroll) {
                this.scrollOffset++;
                playClick(0.9F);
            }
            return;
        }

        // Botões LANÇAR de cada cartão visível
        int rows = Math.min(ROWS_VISIBLE, Math.max(0, n - this.scrollOffset));
        for (int i = 0; i < rows; i++) {
            int bx = BTN_X_REL;
            int by = LIST_Y + i * ROW_STEP + BTN_Y_REL;
            if (mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H) {
                int row = this.scrollOffset + i;
                if (row >= 0 && row < n && this.tile != null) {
                    playClick(1.0F);
                    UUID satId = UUID.fromString(lastList.getUuid(row));
                    OrbitalSunworks.network.sendToServer(new MessageTravelToSatellite(
                        satId, this.tile.xCoord, this.tile.yCoord, this.tile.zCoord));
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
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x0 = (this.width - this.xSize) / 2;
        int y0 = (this.height - this.ySize) / 2;
        int mx = mouseX - x0;
        int my = mouseY - y0;

        GuiTheme.drawAerospacePanel(x0, y0, this.xSize, this.ySize);
        GuiTheme.drawHeaderBar(x0 + 3, y0 + 2, this.xSize - 6, 20);

        // Divisor do cabeçalho da tabela
        drawRect(x0 + LIST_X, y0 + 37, x0 + LIST_X + CARD_W, y0 + 38, GuiTheme.BORDER_SUB);

        int n = lastList != null ? lastList.getCount() : 0;
        String launchTxt = StatCollector.translateToLocal("gui.orbitalsunworks.launch");

        if (n == 0) {
            GuiTheme.drawSubPanel(x0 + LIST_X, y0 + LIST_Y, CARD_W, 138);
        } else {
            int rows = Math.min(ROWS_VISIBLE, Math.max(0, n - this.scrollOffset));
            for (int i = 0; i < rows; i++) {
                int row = this.scrollOffset + i;
                int cy = y0 + LIST_Y + i * ROW_STEP;
                int relCy = LIST_Y + i * ROW_STEP;

                GuiTheme.drawSubPanel(x0 + LIST_X, cy, CARD_W, CARD_H);

                // Faixa de destaque se mouse estiver no cartão
                boolean cardHover = mx >= LIST_X && mx < LIST_X + CARD_W
                    && my >= relCy && my < relCy + CARD_H;
                if (cardHover) {
                    drawRect(x0 + LIST_X + 1, cy + 1, x0 + LIST_X + CARD_W - 1, cy + 2, 0xFF28557A);
                }

                float health = Math.max(0f, Math.min(1f, lastList.getHealth(row)));
                int tier = lastList.getTier(row);

                // Led lateral de status
                drawRect(x0 + LIST_X + 2, cy + 3, x0 + LIST_X + 4, cy + CARD_H - 3,
                    health > 0.4f ? 0xFF55E088 : 0xFFFF5555);

                // Badge do Tier centralizada
                GuiTheme.drawBadge(this.fontRendererObj, x0 + 104, cy + 6,
                    "T" + tier, 0xFF1F2B1A, 0xFF8C7228, GuiTheme.TEXT_GOLD);

                // Barra de vida útil
                GuiTheme.drawProgressBar(x0 + 129, cy + 15, 42, 5, health,
                    health > 0.4f ? 0xFF7CFC00 : 0xFFFF5555,
                    health > 0.4f ? 0xFF3E9E00 : 0xFFB81E1E);

                // Botão LANÇAR dentro do cartão
                boolean btnHover = mx >= BTN_X_REL && mx < BTN_X_REL + BTN_W
                    && my >= relCy + BTN_Y_REL && my < relCy + BTN_Y_REL + BTN_H;
                GuiTheme.drawButton(this.fontRendererObj,
                    x0 + BTN_X_REL, cy + BTN_Y_REL, BTN_W, BTN_H,
                    launchTxt, btnHover, true, false);
            }
        }

        // Barra de rolagem à direita
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

        // Painel do rodapé
        GuiTheme.drawSubPanel(x0 + LIST_X, y0 + 183, 236, 26);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = StatCollector.translateToLocal("gui.orbitalsunworks.nav.title");
        this.fontRendererObj.drawStringWithShadow(title,
            (this.xSize - this.fontRendererObj.getStringWidth(title)) / 2, 8, GuiTheme.TEXT_TITLE);

        int n = lastList != null ? lastList.getCount() : 0;

        // Cabeçalhos das colunas alinhados exatamente com os dados
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.name"),
            16, 27, GuiTheme.TEXT_LABEL);
        String tierLbl = StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.tier");
        this.fontRendererObj.drawString(tierLbl,
            114 - this.fontRendererObj.getStringWidth(tierLbl) / 2, 27, GuiTheme.TEXT_LABEL);
        String healthLbl = StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.health");
        this.fontRendererObj.drawString(healthLbl,
            150 - this.fontRendererObj.getStringWidth(healthLbl) / 2, 27, GuiTheme.TEXT_LABEL);

        if (n == 0) {
            String none = StatCollector.translateToLocal("gui.orbitalsunworks.nav.none");
            this.fontRendererObj.drawStringWithShadow(none,
                LIST_X + (CARD_W - this.fontRendererObj.getStringWidth(none)) / 2,
                LIST_Y + 46, GuiTheme.TEXT_BODY);
            String hint = StatCollector.translateToLocal("gui.orbitalsunworks.nav.hint");
            this.fontRendererObj.drawSplitString(hint, LIST_X + 14, LIST_Y + 66, CARD_W - 28, GuiTheme.TEXT_MUTED);
        } else {
            int rows = Math.min(ROWS_VISIBLE, Math.max(0, n - this.scrollOffset));
            for (int i = 0; i < rows; i++) {
                int row = this.scrollOffset + i;
                int cy = LIST_Y + i * ROW_STEP;
                String name = lastList.getName(row);
                float health = Math.max(0f, Math.min(1f, lastList.getHealth(row)));

                String shown = name;
                while (this.fontRendererObj.getStringWidth(shown) > 84 && shown.length() > 3) {
                    shown = shown.substring(0, shown.length() - 2) + "...";
                }
                this.fontRendererObj.drawStringWithShadow(shown, LIST_X + 7, cy + 9, 0xFFFFFF);

                String h = Math.round(health * 100) + "%";
                this.fontRendererObj.drawString(h,
                    150 - this.fontRendererObj.getStringWidth(h) / 2, cy + 4,
                    health > 0.4f ? GuiTheme.TEXT_OK : GuiTheme.TEXT_WARN);
            }
        }

        // Rodapé: dica à esquerda + contador à direita
        String tip = StatCollector.translateToLocal("gui.orbitalsunworks.nav.footer");
        String count = n == 0 ? "0 / 0"
            : (this.scrollOffset + 1) + "-" + Math.min(this.scrollOffset + ROWS_VISIBLE, n) + " / " + n;
        int countW = this.fontRendererObj.getStringWidth(count);
        int maxTipW = 224 - countW - 8;
        while (this.fontRendererObj.getStringWidth(tip) > maxTipW && tip.length() > 6) {
            tip = tip.substring(0, tip.length() - 2) + "...";
        }
        this.fontRendererObj.drawString(tip, LIST_X + 6, 192, GuiTheme.TEXT_MUTED);
        this.fontRendererObj.drawString(count, 240 - countW, 192, GuiTheme.TEXT_TITLE);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}