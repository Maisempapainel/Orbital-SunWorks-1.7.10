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
 * HUD do Satélite na órbita (176x214), espelhada no layout e paleta do
 * Foguete Tier 1: faixas bem separadas, zero sobreposição, zero distorção de UV.
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
        // 1. Cabeçalho (y=6)
        String name = this.tile.getSatelliteName();
        while (this.fontRendererObj.getStringWidth(name) > 125 && name.length() > 3) {
            name = name.substring(0, name.length() - 2) + "...";
        }
        this.fontRendererObj.drawStringWithShadow(name, 8, 6, 0x55E0FF);
        String tier = "T" + this.tile.getTier();
        this.fontRendererObj.drawStringWithShadow(tier,
            168 - this.fontRendererObj.getStringWidth(tier), 6, 0xFFD970);

        // 2. Vida útil (y=21) + barra (y=31..39)
        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        String pct = Math.round(health * 100) + "%";
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.health"), 8, 21, 0x9FB4C7);
        this.fontRendererObj.drawString(pct,
            168 - this.fontRendererObj.getStringWidth(pct), 21,
            health > 0.4f ? 0x7CFC00 : 0xFF5555);

        // 3. Geração (y=43)
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.generation"), 8, 43, 0x9FB4C7);
        String gen = OSUtils.formatRF(this.tile.getGenerationRFt()) + "/t";
        this.fontRendererObj.drawStringWithShadow(gen,
            168 - this.fontRendererObj.getStringWidth(gen), 43, 0x7CFC00);

        // 4. Módulos + Escudo (y=57)
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.modules"), 8, 57, 0x9FB4C7);
        boolean on = this.tile.hasDebrisShield();
        String shieldLbl = StatCollector.translateToLocal("gui.orbitalsunworks.rocket.shield_short");
        String shieldTxt = shieldLbl + ": " + (on ? "ON" : "OFF");
        this.fontRendererObj.drawString(shieldTxt,
            168 - this.fontRendererObj.getStringWidth(shieldTxt), 57,
            on ? 0x00E8FF : 0xFF5555);

        // Rótulos dos slots (y=68)
        String modLabel = "MOD";
        this.fontRendererObj.drawString(modLabel,
            43 - this.fontRendererObj.getStringWidth(modLabel) / 2, 68, 0x8FA3B8);
        String repLabel = "REPARO +10%";
        this.fontRendererObj.drawString(repLabel,
            132 - this.fontRendererObj.getStringWidth(repLabel) / 2, 68, 0x8FA3B8);

        // 5. Inventário do jogador (y=106, idêntico ao GuiRocket)
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "container.inventory"), 8, 106, 0x55E0FF);

        // Dica discreta no rodapé (y=200)
        String hint = StatCollector.translateToLocal("gui.orbitalsunworks.sat.repair");
        while (this.fontRendererObj.getStringWidth(hint) > 160 && hint.length() > 6) {
            hint = hint.substring(0, hint.length() - 2) + "...";
        }
        this.fontRendererObj.drawString(hint,
            (this.xSize - this.fontRendererObj.getStringWidth(hint)) / 2, 200, 0x6F849C);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Painel principal (mesma paleta do rocket.png)
        drawRect(x, y, x + this.xSize, y + this.ySize, 0xFF48607C);
        drawRect(x + 2, y + 2, x + this.xSize - 2, y + this.ySize - 2, 0xFF0E141E);
        drawRect(x + 3, y + 3, x + this.xSize - 3, y + this.ySize - 3, 0xFF182130);

        // Faixa do cabeçalho + divisores horizontais
        drawRect(x + 3, y + 3, x + this.xSize - 3, y + 17, 0xFF1E2B3E);
        drawRect(x + 3, y + 17, x + this.xSize - 3, y + 18, 0xFF48607C);
        drawRect(x + 3, y + 53, x + this.xSize - 3, y + 54, 0xFF48607C);
        drawRect(x + 3, y + 101, x + this.xSize - 3, y + 102, 0xFF48607C);

        // Separador vertical entre módulos e reparo
        drawRect(x + 87, y + 67, x + 88, y + 98, 0xFF2E4056);

        // Barra de vida útil (x=8..168, y=31..39)
        drawRect(x + 8, y + 31, x + 168, y + 39, 0xFF48607C);
        drawRect(x + 9, y + 32, x + 167, y + 38, 0xFF0E141E);
        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        int barW = (int) (158 * health);
        if (barW > 0) {
            boolean ok = health > 0.4f;
            this.drawGradientRect(x + 9, y + 32, x + 9 + barW, y + 38,
                ok ? 0xFF7CFC00 : 0xFFFF5555,
                ok ? 0xFF3E9E00 : 0xFFB81E1E);
        }

        // Molduras dos 3 slots do satélite
        drawSlotBox(x + 21, y + 78);
        drawSlotBox(x + 47, y + 78);
        drawSlotBox(x + 123, y + 78);

        // Molduras do inventário 3x9 + hotbar (idêntico ao rocket.png)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                drawSlotBox(x + 7 + col * 18, y + 117 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            drawSlotBox(x + 7 + col * 18, y + 175);
        }

        // Linha discreta acima da dica do rodapé
        drawRect(x + 8, y + 196, x + 168, y + 197, 0xFF2E4056);
    }

    private static void drawSlotBox(int sx, int sy) {
        drawRect(sx, sy, sx + 18, sy + 18, 0xFF48607C);
        drawRect(sx + 1, sy + 1, sx + 17, sy + 17, 0xFF384A62);
    }
}