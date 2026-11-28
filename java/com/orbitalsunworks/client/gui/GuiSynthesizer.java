package com.orbitalsunworks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.machine.ContainerSynthesizer;
import com.orbitalsunworks.machine.TileSynthesizer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial da Máquina Sintetizadora (176x198).
 */
@SideOnly(Side.CLIENT)
public class GuiSynthesizer extends GuiContainer {

    private static final int RF_X = 11;
    private static final int RF_Y = 25;
    private static final int RF_W = 10;
    private static final int RF_H = 66;

    private static final int TANK_X = 49;
    private static final int TANK_Y = 25;
    private static final int TANK_W = 22;
    private static final int TANK_H = 66;

    private static final int PROG_X = 76;
    private static final int PROG_Y = 52;
    private static final int PROG_W = 46;
    private static final int PROG_H = 8;

    private final TileSynthesizer tile;

    public GuiSynthesizer(InventoryPlayer playerInv, TileSynthesizer tile) {
        super(new ContainerSynthesizer(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 198;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = GuiTheme.truncate(this.fontRendererObj, "Sintetizadora", 104);
        this.fontRendererObj.drawStringWithShadow(title, 8, 5, GuiTheme.TEXT_TITLE);

        this.fontRendererObj.drawString("REFIN.", 46, 93, GuiTheme.TEXT_GOLD);
        this.fontRendererObj.drawString("KEVLAR", 121, 36, GuiTheme.TEXT_TITLE);

        int prog = this.tile != null ? this.tile.clientProgress : 0;
        int pct = Math.round((prog / (float) TileSynthesizer.SYNTH_TICKS) * 100f);
        if (prog > 0) {
            String pctTxt = pct + "%";
            this.fontRendererObj.drawString(pctTxt,
                PROG_X + (PROG_W - this.fontRendererObj.getStringWidth(pctTxt)) / 2,
                40, GuiTheme.TEXT_OK);
        } else {
            String idle = "5.000 mB";
            this.fontRendererObj.drawString(idle,
                PROG_X + (PROG_W - this.fontRendererObj.getStringWidth(idle)) / 2,
                40, GuiTheme.TEXT_MUTED);
        }

        String timeTxt = String.format("%.1fs", Math.max(0, TileSynthesizer.SYNTH_TICKS - prog) / 20f);
        this.fontRendererObj.drawString(timeTxt,
            PROG_X + (PROG_W - this.fontRendererObj.getStringWidth(timeTxt)) / 2,
            64, GuiTheme.TEXT_LABEL);

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
            "8.000 RF/t", 0xFF142736, 0xFF2E85AA, GuiTheme.TEXT_TITLE);

        // Baía da sintetizadora
        GuiTheme.drawSubPanel(x + 7, y + 18, this.xSize - 14, 85);

        // Barra vertical de RF
        float rfPct = Math.max(0f, Math.min(1f,
            this.tile.clientEnergy / (float) TileSynthesizer.ENERGY_CAPACITY));
        drawVerticalMeter(x + RF_X, y + RF_Y, RF_W, RF_H, rfPct,
            0xFFFFC840, 0xFFB85C00, 0xFF1A140A);

        // Slots esquerdos (balde refinado -> vazio)
        GuiTheme.drawSlotBox(x + 25, y + 27);
        GuiTheme.drawSlotBox(x + 25, y + 71);
        drawRect(x + 33, y + 48, x + 35, y + 67, 0xFF2E5474);
        drawRect(x + 31, y + 63, x + 37, y + 65, 0xFFFFD055);

        // Tanque de Petróleo Refinado + marca do custo de 5.000 mB
        float refinedPct = Math.max(0f, Math.min(1f,
            this.tile.clientRefined / (float) TileSynthesizer.TANK_CAPACITY));
        drawVerticalMeter(x + TANK_X, y + TANK_Y, TANK_W, TANK_H, refinedPct,
            0xFFF2BC3E, 0xFFC07612, 0xFF070B10);
        int costRatioH = Math.round((TANK_H - 2) * (TileSynthesizer.REFINED_COST_MB
            / (float) TileSynthesizer.TANK_CAPACITY));
        int markY = y + TANK_Y + TANK_H - 1 - costRatioH;
        drawRect(x + TANK_X + 1, markY, x + TANK_X + TANK_W - 1, markY + 1, 0xFF4BE0FF);

        // Barra horizontal de síntese
        float progPct = Math.max(0f, Math.min(1f,
            this.tile.clientProgress / (float) TileSynthesizer.SYNTH_TICKS));
        GuiTheme.drawProgressBar(x + PROG_X, y + PROG_Y, PROG_W, PROG_H, progPct,
            0xFF55E0FF, 0xFF0077AA);

        // Slot de saída de Kevlar (128, 48)
        boolean hasOut = this.tile.getStackInSlot(2) != null;
        GuiTheme.drawSlotBox(x + 127, y + 47,
            hasOut ? 0xFF7CFC00 : GuiTheme.BORDER_MAIN, 0xFF0A121E);

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
                + " / " + formatNum(TileSynthesizer.ENERGY_CAPACITY) + " RF");
            tip.add("\u00a77Consumo: \u00a7e" + formatNum(TileSynthesizer.RF_PER_TICK) + " RF/t");
            tip.add("\u00a7cEntrada RF: face traseira (vermelha)");
            this.drawHoveringText(tip, mouseX, mouseY, this.fontRendererObj);
        } else if (mx >= TANK_X && mx < TANK_X + TANK_W && my >= TANK_Y && my < TANK_Y + TANK_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a76Petróleo Refinado: \u00a7f" + formatNum(this.tile.clientRefined)
                + " / " + formatNum(TileSynthesizer.TANK_CAPACITY) + " mB");
            tip.add("\u00a77Custo por Kevlar: \u00a7b" + formatNum(TileSynthesizer.REFINED_COST_MB) + " mB");
            this.drawHoveringText(tip, mouseX, mouseY, this.fontRendererObj);
        } else if (mx >= PROG_X && mx < PROG_X + PROG_W && my >= PROG_Y && my < PROG_Y + PROG_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a7bSíntese: \u00a7f" + this.tile.clientProgress + " / "
                + TileSynthesizer.SYNTH_TICKS + " ticks (10s)");
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