package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.ContainerOrbitSatellite;
import com.orbitalsunworks.satellite.TileOrbitSatellite;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial do Satélite na órbita (176x214).
 * Slots alinhados ao ContainerOrbitSatellite:
 *  - Módulos: (22, 79) e (48, 79)
 *  - Reparo: (124, 79)
 *  - Inventário: y=118 | Hotbar: y=176
 */
@SideOnly(Side.CLIENT)
public class GuiOrbitSatellite extends GuiContainer {

    private final TileOrbitSatellite tile;

    public GuiOrbitSatellite(InventoryPlayer playerInv, TileOrbitSatellite tile) {
        super(new ContainerOrbitSatellite(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 214;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (this.tile == null) {
            return;
        }
        // Cabeçalho: Nome + Tier
        String name = GuiTheme.truncate(this.fontRendererObj, this.tile.getSatelliteName(), 125);
        this.fontRendererObj.drawStringWithShadow(name, 8, 5, GuiTheme.TEXT_TITLE);

        // Telemetria: Vida útil + Geração
        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        String pct = Math.round(health * 100) + "%";
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.sat.health"),
            12, 21, GuiTheme.TEXT_LABEL);
        this.fontRendererObj.drawString(pct,
            164 - this.fontRendererObj.getStringWidth(pct), 21,
            health > 0.4f ? GuiTheme.TEXT_OK : GuiTheme.TEXT_WARN);

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.sat.generation"),
            12, 42, GuiTheme.TEXT_LABEL);
        String gen = OSUtils.formatRF(this.tile.getGenerationRFt()) + "/t";
        this.fontRendererObj.drawStringWithShadow(gen,
            164 - this.fontRendererObj.getStringWidth(gen), 42, GuiTheme.TEXT_OK);

        // Módulos + Escudo + Reparo
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.sat.modules"),
            12, 58, GuiTheme.TEXT_LABEL);
        boolean on = this.tile.hasDebrisShield();
        String shieldLbl = StatCollector.translateToLocal("gui.orbitalsunworks.rocket.shield_short");
        String shieldTxt = shieldLbl + ": " + (on ? "ON" : "OFF");
        this.fontRendererObj.drawString(shieldTxt,
            164 - this.fontRendererObj.getStringWidth(shieldTxt), 58,
            on ? 0x00E8FF : GuiTheme.TEXT_WARN);

        String modLabel = "MOD";
        this.fontRendererObj.drawString(modLabel,
            43 - this.fontRendererObj.getStringWidth(modLabel) / 2, 68, GuiTheme.TEXT_MUTED);
        String repLabel = "REPARO +10%";
        this.fontRendererObj.drawString(repLabel,
            132 - this.fontRendererObj.getStringWidth(repLabel) / 2, 68, GuiTheme.TEXT_GOLD);

        // Inventário do jogador
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"), 8, 106, GuiTheme.TEXT_TITLE);

        // Dica no rodapé
        String hint = GuiTheme.truncate(this.fontRendererObj,
            StatCollector.translateToLocal("gui.orbitalsunworks.sat.repair"), 160);
        this.fontRendererObj.drawString(hint,
            (this.xSize - this.fontRendererObj.getStringWidth(hint)) / 2, 199, GuiTheme.TEXT_MUTED);
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
        GuiTheme.drawBadge(this.fontRendererObj, x + 150, y + 3,
            "T" + this.tile.getTier(), 0xFF1F2B1A, 0xFF8C7228, GuiTheme.TEXT_GOLD);

        // Painel de telemetria
        GuiTheme.drawSubPanel(x + 7, y + 18, this.xSize - 14, 35);
        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        GuiTheme.drawProgressBar(x + 11, y + 31, 154, 6, health,
            health > 0.4f ? 0xFF7CFC00 : 0xFFFF5555,
            health > 0.4f ? 0xFF3E9E00 : 0xFFB81E1E);

        // Painel de módulos e reparo
        GuiTheme.drawSubPanel(x + 7, y + 55, this.xSize - 14, 47);
        drawRect(x + 87, y + 67, x + 88, y + 99, GuiTheme.BORDER_SUB);
        GuiTheme.drawSlotBox(x + 21, y + 78);
        GuiTheme.drawSlotBox(x + 47, y + 78);
        GuiTheme.drawSlotBox(x + 123, y + 78, 0xFF8C7228, 0xFF1A1B14);

        // Inventário 3x9 + hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiTheme.drawSlotBox(x + 7 + col * 18, y + 117 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiTheme.drawSlotBox(x + 7 + col * 18, y + 175);
        }
    }
}