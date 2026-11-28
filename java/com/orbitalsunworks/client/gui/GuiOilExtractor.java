package com.orbitalsunworks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.machine.ContainerOilExtractor;
import com.orbitalsunworks.machine.TileEnergyInput;
import com.orbitalsunworks.machine.TileOilExtractor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial do Extrator de Petróleo (176x186).
 */
@SideOnly(Side.CLIENT)
public class GuiOilExtractor extends GuiContainer {

    private static final int RF_X = 14;
    private static final int RF_Y = 47;
    private static final int RF_W = 12;
    private static final int RF_H = 42;

    private static final int TANK_X = 76;
    private static final int TANK_Y = 47;
    private static final int TANK_W = 24;
    private static final int TANK_H = 42;

    private final ContainerOilExtractor container;

    public GuiOilExtractor(InventoryPlayer playerInv, TileOilExtractor tile) {
        super(new ContainerOilExtractor(playerInv, tile));
        this.container = (ContainerOilExtractor) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 186;
    }

    private String getStatusTitle(int status) {
        switch (status) {
            case 5: return "EXTRAINDO PETRÓLEO";
            case 4: return "TANQUE CHEIO";
            case 3: return "ENERGIA INSUFICIENTE";
            case 2: return "SEM RESERVA NO CHUNK";
            case 1: return "CHUNKS DIVIDIDOS";
            default: return "ESTRUTURA INCOMPLETA";
        }
    }

    private String getStatusHint(int status) {
        switch (status) {
            case 5: return "+" + TileOilExtractor.OIL_PER_TICK + " mB/t  |  -" + TileOilExtractor.RF_PER_TICK + " RF/t";
            case 4: return "Retire o petróleo bruto com baldes ou canos";
            case 3: return "Alimente a Entrada RF (" + TileOilExtractor.RF_PER_TICK + " RF/t)";
            case 2: return "Este chunk não possui jazida (use o Sensor)";
            case 1: return "Todo o multibloco 3x3 deve ficar em 1 chunk";
            default: return "7x Bloco Blindado + 1x Entrada RF na borda 3x3";
        }
    }

    private int getStatusColor(int status) {
        switch (status) {
            case 5: return GuiTheme.TEXT_OK;
            case 4:
            case 3: return GuiTheme.TEXT_GOLD;
            default: return GuiTheme.TEXT_WARN;
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = GuiTheme.truncate(this.fontRendererObj, "Extrator de Petróleo", 112);
        this.fontRendererObj.drawStringWithShadow(title, 8, 5, GuiTheme.TEXT_TITLE);

        int status = this.container.clientStatus;
        String statusTitle = GuiTheme.truncate(this.fontRendererObj, getStatusTitle(status), 154);
        String statusHint = GuiTheme.truncate(this.fontRendererObj, getStatusHint(status), 154);
        this.fontRendererObj.drawString(statusTitle, 11, 21, getStatusColor(status));
        this.fontRendererObj.drawString(statusHint, 11, 31, GuiTheme.TEXT_MUTED);

        // Rótulos discretos abaixo dos slots
        this.fontRendererObj.drawString("BALDE", 39, 71, GuiTheme.TEXT_MUTED);
        this.fontRendererObj.drawString("BRUTO", 111, 71, GuiTheme.TEXT_MUTED);

        // Micro rótulos laterais RF e tanque
        this.fontRendererObj.drawString("RF", 15, 81, GuiTheme.TEXT_GOLD);
        this.fontRendererObj.drawString("mB", 82, 81, GuiTheme.TEXT_LABEL);

        // Telemetria direita
        String stateTag = this.container.clientRunning ? "ON"
            : (this.container.clientFormed ? "PRONTO" : "OFF");
        int tagCol = this.container.clientRunning ? GuiTheme.TEXT_OK
            : (this.container.clientFormed ? GuiTheme.TEXT_GOLD : GuiTheme.TEXT_WARN);
        this.fontRendererObj.drawString(stateTag, 165 - this.fontRendererObj.getStringWidth(stateTag), 48, tagCol);

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"), 8, 94, GuiTheme.TEXT_TITLE);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        GuiTheme.drawAerospacePanel(x, y, this.xSize, this.ySize);
        GuiTheme.drawHeaderBar(x + 3, y + 2, this.xSize - 6, 14);
        GuiTheme.drawBadge(this.fontRendererObj, x + 123, y + 3,
            "PUMPJACK", 0xFF142736, 0xFF2E85AA, GuiTheme.TEXT_TITLE);

        // Painel de status superior
        GuiTheme.drawSubPanel(x + 7, y + 18, this.xSize - 14, 24);

        // Baía de operação central
        GuiTheme.drawSubPanel(x + 7, y + 44, this.xSize - 14, 48);

        // Barra vertical de RF
        float rfPct = Math.max(0f, Math.min(1f,
            this.container.clientEnergy / (float) TileEnergyInput.CAPACITY));
        drawVerticalMeter(x + RF_X, y + RF_Y, RF_W, RF_H, rfPct,
            0xFFFFC840, 0xFFB85C00, 0xFF1A140A);

        // Slots dos baldes (44,52) e (116,52) -> caixas (-1,-1)
        GuiTheme.drawSlotBox(x + 43, y + 51);
        GuiTheme.drawSlotBox(x + 115, y + 51);

        // Conectores entre slots e tanque
        int flowCol = this.container.clientRunning ? 0xFF4BE0FF : 0xFF2B4662;
        drawRect(x + 63, y + 59, x + 74, y + 61, flowCol);
        drawRect(x + 102, y + 59, x + 113, y + 61, flowCol);
        drawRect(x + 109, y + 57, x + 111, y + 63, flowCol);

        // Tanque central de petróleo bruto
        float oilPct = Math.max(0f, Math.min(1f,
            this.container.clientOil / (float) TileOilExtractor.TANK_CAPACITY));
        drawVerticalMeter(x + TANK_X, y + TANK_Y, TANK_W, TANK_H, oilPct,
            0xFF3B3028, 0xFF161210, 0xFF070B10);
        // Marcas graduadas do tanque
        for (int t = 1; t <= 3; t++) {
            int ty = y + TANK_Y + (TANK_H * t) / 4;
            drawRect(x + TANK_X + 2, ty, x + TANK_X + 8, ty + 1, 0x604BE0FF);
        }

        // LED indicador de atividade na direita
        int ledCol = this.container.clientRunning ? 0xFF7CFC00
            : (this.container.clientFormed ? 0xFFFFD970 : 0xFFFF5555);
        drawRect(x + 157, y + 59, x + 163, y + 65, 0xFF0A111C);
        drawRect(x + 158, y + 60, x + 162, y + 64, ledCol);

        // Inventário 3x9 + Hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiTheme.drawSlotBox(x + 7 + col * 18, y + 103 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiTheme.drawSlotBox(x + 7 + col * 18, y + 161);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        int mx = mouseX - this.guiLeft;
        int my = mouseY - this.guiTop;

        if (mx >= RF_X && mx < RF_X + RF_W && my >= RF_Y && my < RF_Y + RF_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a76Energia RF: \u00a7f" + formatNum(this.container.clientEnergy)
                + " / " + formatNum(TileEnergyInput.CAPACITY) + " RF");
            tip.add("\u00a77Consumo: \u00a7e" + formatNum(TileOilExtractor.RF_PER_TICK) + " RF/t");
            this.drawHoveringText(tip, mouseX, mouseY, this.fontRendererObj);
        } else if (mx >= TANK_X && mx < TANK_X + TANK_W && my >= TANK_Y && my < TANK_Y + TANK_H) {
            List<String> tip = new ArrayList<String>();
            tip.add("\u00a78Petróleo Bruto: \u00a7f" + formatNum(this.container.clientOil)
                + " / " + formatNum(TileOilExtractor.TANK_CAPACITY) + " mB");
            tip.add("\u00a77Extração: \u00a7b" + TileOilExtractor.OIL_PER_TICK + " mB/t");
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