package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.ContainerOrbitSatellite;
import com.orbitalsunworks.satellite.TileOrbitSatellite;
import com.orbitalsunworks.util.OSUtils;

/**
 * GUI da ESTRUTURA DO SATÉLITE na órbita (v0.8.0 — REFEITA).
 *
 * HUD decente com a textura nova (painel espacial escuro + moldura
 * ciano): nome/tier no cabeçalho, barra de vida com %, geração de RF,
 * status do escudo, slots de módulo/reparo com molduras, botão VOLTAR
 * À TERRA bonito e o inventário do jogador embaixo.
 */
public class GuiOrbitSatellite extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "orbitalsunworks", "textures/gui/orbitSatellite.png");

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
        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        // ---------------- Cabeçalho ----------------
        // Nome do satélite (trunca) + tier à direita.
        String name = this.tile.getSatelliteName();
        while (this.fontRendererObj.getStringWidth(name) > 118 && name.length() > 3) {
            name = name.substring(0, name.length() - 2) + "…";
        }
        this.fontRendererObj.drawStringWithShadow(name, 8, 11, 0x55E0FF);
        String tier = "T" + this.tile.getTier();
        this.fontRendererObj.drawStringWithShadow(tier,
            168 - this.fontRendererObj.getStringWidth(tier), 11, 0xFFD970);

        // ---------------- Vida útil (barra 9..165 x 31..37) ----------------
        float health = this.tile.getHealth();
        String pct = Math.round(health * 100) + "%";
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.health"), 8, 21, 0x9FB4C7);
        this.fontRendererObj.drawString(pct,
            168 - this.fontRendererObj.getStringWidth(pct), 21,
            health > 0.4f ? 0x7CFC00 : 0xFF5555);
        int w = (int) (155 * health);
        if (w > 0) {
            drawRect(10, 32, 10 + w, 36, health > 0.4f ? 0xFF3DDC55 : 0xFFE04040);
        }

        // ---------------- Geração ----------------
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.generation"), 8, 42, 0x9FB4C7);
        String gen = OSUtils.formatRF(this.tile.getGenerationRFt()) + "/t";
        this.fontRendererObj.drawStringWithShadow(gen,
            168 - this.fontRendererObj.getStringWidth(gen), 42, 0x7CFC00);

        // ---------------- Módulos (escudo) ----------------
        com.orbitalsunworks.satellite.SatelliteManager.Satellite sat = this.tile.getSatellite();
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.modules"), 8, 54, 0x9FB4C7);
        String shield = StatCollector.translateToLocal("gui.orbitalsunworks.sat.shield");
        boolean on = sat != null && sat.hasDebrisShield();
        this.fontRendererObj.drawString(shield, 8, 64, 0xC7D0DA);
        String state = on ? "ON" : "OFF";
        this.fontRendererObj.drawStringWithShadow(state,
            168 - this.fontRendererObj.getStringWidth(state), 64, on ? 0x7CFC00 : 0xFF5555);

        // ---------------- Reparo ----------------
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.sat.repair"), 8, 78, 0x9FB4C7);

        // v0.10.0: SEM botão "voltar à Terra" aqui — o usuário tem
        // razão: "eu não volto com o satélite, eu volto com o foguete
        // de navegação". O botão de retorno vive na HUD da NAVE
        // ESTACIONADA (GuiReturnHome). Esta GUI é só do satélite:
        // status + slots de módulos (kits de reparo virão depois).

        // ---------------- Inventário do jogador ----------------
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "container.inventory"), 8, 120, 0x55E0FF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1, 1, 1, 1);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        // v0.10.0: sem botão de retorno aqui (a volta é na NAVE, não no
        // satélite) — o clique só maneja slots (super).
    }
}
