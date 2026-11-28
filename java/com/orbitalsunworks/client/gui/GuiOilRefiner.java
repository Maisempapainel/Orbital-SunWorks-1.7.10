package com.orbitalsunworks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.machine.ContainerOilRefiner;
import com.orbitalsunworks.machine.TileOilRefiner;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial do Refinador de Petróleo (176x198).
 */
@SideOnly(Side.CLIENT)
public class GuiOilRefiner extends GuiContainer {

    private static final int RF_X = 11;
    private static final int RF_Y = 25;
    private static final int RF_W = 10;
    private static final int RF_H = 66;

    private static final int CRUDE_X = 49;
    private static final int CRUDE_Y = 25;
    private static final int CRUDE_W = 22;
    private static final int CRUDE_H = 66;

    private static final int REF_X = 105;
    private static final int REF_Y = 25;
    private static final int REF_W = 22;
    private static final int REF_H = 66;

    private final TileOilRefiner tile;

    public GuiOilRefiner(InventoryPlayer playerInv, TileOilRefiner tile) {
        super(new ContainerOilRefiner(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 198;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = GuiTheme.truncate(this.fontRendererObj, "Refinador", 104);
        this.fontRendererObj.drawStringWithShadow(title, 8, 5, GuiTheme.TEXT_TITLE);

        // Micro rótulos dos tanques e fluxo
        this.fontRendererObj.drawString("BRUTO", 46, 93, GuiTheme.TEXT_MUTED);
        this.fontRendererObj.drawString("REFIN.", 103, 93, GuiTheme.TEXT_GOLD);

        boolean active = this.tile != null && this.tile.clientProgress > 0;
        String status = active ? "REFINO" : "ESPERA";
        this.fontRendererObj.drawString(status,
            88 - this.fontRendererObj.getStringWidth(status) / 2, 42,
            active ? GuiTheme.TEXT_OK : GuiTheme.TEXT_MUTED);

        String rate = "10mB/5t";
        this.fontRendererObj.drawString(rate,
            88 - this.fontRendererObj.getStringWidth(rate) / 2, 66, GuiTheme.TEXT_LABEL);

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"), 8, 105, GuiTheme.TEXT_TITLE);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (this.tile == null) {
            return;
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        GuiTheme.drawAerospacePanel(x, y, this.xSize, this.ySize);
        GuiTheme.drawHeaderBar(x + 3, y + 2, this.xSize - 6, 14);
        GuiTheme.drawBadge(this.fontRendererObj, x + 114, y + 3,
            "4.000 RF/t", 0xFF281D0F, 0xFFB87E24, GuiTheme.TEXT_GOLD);

        // Baía da refinaria
        GuiTheme.drawSubPanel(x + 7, y + 18, this.xSize - 14, 85);

        // Barra vertical de RF
        float rfPct = Math.max(0f, Math.min(1f,
            this.tile.clientEnergy / (float) TileOilRefiner.ENERGY_CAPACITY));
        drawVerticalMeter(x + RF_X, y + RF_Y, RF_W, RF_H, rfPct,
            0xFFFFC840, 0xFFB85C00, 0xFF1A140A);

        // Slots esquerdos (bruto -> vazio)
        GuiTheme.drawSlotBox(x + 25, y + 27);
        GuiTheme.drawSlotBox(x + 25, y + 71);
        drawRect(x + 33, y + 48, x + 35, y + 67, 0xFF2E5474);
        drawRect(x + 31, y + 63, x + 37, y + 65, 0xFF4BE0FF);

        // Tanque de Petróleo Bruto
        float crudePct = Math.max(0f, Math.min(1f,
            this.tile.clientCrude / (float) TileOilRefiner.TANK_CAPACITY));
        drawVerticalMeter(x + CRUDE_X, y + CRUDE_Y, CRUDE_W, CRUDE_H, crudePct,
            0xFF3B3028, 0xFF161210, 0xFF070B10);

        // Barra horizontal de progresso (5 ticks)
        float progPct = Math.max(0f, Math.min(1f, this.tile.clientProgress / 5.0f));
        GuiTheme.drawProgressBar(x + 74, y + 54, 28, 8, progPct, 0xFFFFD055, 0xFFC27C10);

        // Tanque de Petróleo Refinado (âmbar dourado)
        float refinedPct = Math.max(0f, Math.min(1f,
            this.tile.clientRefined / (float) TileOilRefiner.TANK_CAPACITY));
        drawVerticalMeter(x + REF_X, y + REF_Y, REF_W, REF_H, refinedPct,
            0xFFF2BC3E, 0xFFC07612, 0xFF070B10);

        // Graduações nos tanques
        for (int t = 1; t <= 3; t++) {
            int ty = y + CRUDE_Y + (CRUDE_H * t) / 4;
            drawRect(x + CRUDE_X + 2, ty, x + CRUDE_X + 7, ty + 1, 0x604BE0FF);
            drawRect(x + REF_X + 2, ty, x + REF_X + 7, ty + 1, 0x604BE0FF);
        }

        // Slots direitos (vazio -> refinado)
        GuiTheme.drawSlotBox(x + 133, y + 27);
        GuiTheme.drawSlotBox(x + 133, y + 71);
        drawRect(x + 141, y + 48, x + 143, y + 67, 0xFF2E5474);
        drawRect(x + 139, y + 63, x + 145, y + 65, 0xFFFFD055);

        // Inventário 3x9 + Hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiTheme.drawSlotBox(x + 7 + col * 18, y + 115 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiTheme.drawSlotBox(x + 7 + col * 18, y + 173);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (this.tile == null) {
            return;
        }
        int mx = mouseX - this.guiLeft;
        int my = mouseY - this.guiTop;

        if (mx >= RF_X && mx < RF_X + RF_W && my >= RF_Y && my < RF_Y + RF_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a76Energia RF: \u00a7f" + formatNum(this.tile.clientEnergy)
                + " / " + formatNum(TileOilRefiner.ENERGY_CAPACITY) + " RF");
            tip.add("\u00a77Consumo: \u00a7e" + formatNum(TileOilRefiner.RF_PER_TICK) + " RF/t");
            tip.add("\u00a7cEntrada RF: face traseira (vermelha)");
            this.drawHoveringText(tip, mouseX, mouseY, this.fontRendererObj);
        } else if (mx >= CRUDE_X && mx < CRUDE_X + CRUDE_W && my >= CRUDE_Y && my < CRUDE_Y + CRUDE_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a78Petróleo Bruto: \u00a7f" + formatNum(this.tile.clientCrude)
                + " / " + formatNum(TileOilRefiner.TANK_CAPACITY) + " mB");
            this.drawHoveringText(tip, mouseX, mouseY, this.fontRendererObj);
        } else if (mx >= REF_X && mx < REF_X + REF_W && my >= REF_Y && my < REF_Y + REF_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a76Petróleo Refinado: \u00a7f" + formatNum(this.tile.clientRefined)
                + " / " + formatNum(TileOilRefiner.TANK_CAPACITY) + " mB");
            this.drawHoveringText(tip, mouseX, mouseY, this.fontRendererObj);
        }
    }

    private static void drawVerticalMeter(int x, int y, int w, int h, float pct,
            int fillTop, int fillBot, int bg) {
        drawRect(x, y, x + w, y + h, GuiTheme.BORDER_MAIN);
        drawRect(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        int innerH = h - 2;
        int fillH = Math.round(innerH * Math.max(0f, Math.min(1f, pct)));
        if (fillH > 0) {
            int topY = y + h - 1 - fillH;
            int midY = topY + fillH / 2;
            drawRect(x + 1, topY, x + w - 1, midY, fillTop);
            drawRect(x + 1, midY, x + w - 1, y + h - 1, fillBot);
            drawRect(x + 2, topY, x + 4, y + h - 1, 0x26FFFFFF);
        }
    }

    private static String formatNum(int v) {
        return String.format("%,d", v).replace(',', '.');
    }
}