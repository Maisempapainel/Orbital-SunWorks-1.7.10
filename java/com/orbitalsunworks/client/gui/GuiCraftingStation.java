package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.craftingstation.ContainerCraftingStation;
import com.orbitalsunworks.craftingstation.TileCraftingStation;

/**
 * GUI da Estação de Crafting Orbital (v4) — 176x202.
 *
 * Layout (nada sobreposto, status EMBAIXO do campo de crafting):
 * - y 6:   título;
 * - y 17:  grade 3x5 (termina y 68) + slot de resultado à direita (y 35);
 * - y 74:  PAINEL DE STATUS: rótulo à esquerda + tempo/PRONTO à direita,
 *          barra de progresso (moldura na textura) em y 84..90;
 * - y 97:  "Inventário";
 * - y 108: inventário 3x9 (termina y 162); hotbar y 166..184.
 */
public class GuiCraftingStation extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "orbitalsunworks", "textures/gui/crafting_station.png");

    private final TileCraftingStation tile;

    public GuiCraftingStation(InventoryPlayer playerInv, TileCraftingStation tile) {
        super(new ContainerCraftingStation(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 202;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = StatCollector.translateToLocal("gui.orbitalsunworks.craftingStation");
        this.fontRendererObj.drawString(title, 8, 6, 0x55E0FF);

        // ---- Painel de status (y 74..92): texto FORA da barra ----
        int state = this.tile.clientState;
        String label;
        String right;
        int rightColor;
        if (state == TileCraftingStation.STATE_CRAFTING) {
            label = StatCollector.translateToLocal("gui.orbitalsunworks.crafting");
            float secs = Math.max(0, this.tile.clientTicksRemaining) / 20f;
            right = String.format("%.1fs", secs);
            rightColor = 0xFFD24A;
        } else if (state == TileCraftingStation.STATE_READY) {
            label = StatCollector.translateToLocal("gui.orbitalsunworks.done");
            right = StatCollector.translateToLocal("gui.orbitalsunworks.ready");
            rightColor = 0x7CFC00;
        } else {
            label = StatCollector.translateToLocal("gui.orbitalsunworks.idle");
            right = "";
            rightColor = 0x9FB4C7;
        }
        this.fontRendererObj.drawString(label, 8, 74, 0x9FB4C7);
        if (!right.isEmpty()) {
            this.fontRendererObj.drawString(right,
                168 - this.fontRendererObj.getStringWidth(right), 74, rightColor);
        }

        this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"),
            8, 97, 0x55E0FF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Preenchimento da barra dentro da moldura da textura (x 9..167, y 85..89).
        int state = this.tile.clientState;
        int barW = 0;
        int colorTop, colorBot;
        if (state == TileCraftingStation.STATE_CRAFTING) {
            float progress = 0f;
            if (this.tile.clientTicksTotal > 0) {
                progress = 1f - (float) this.tile.clientTicksRemaining / this.tile.clientTicksTotal;
            }
            barW = (int) (158 * Math.min(1f, Math.max(0f, progress)));
            colorTop = 0xFF55E0FF;
            colorBot = 0xFF0077AA;
        } else if (state == TileCraftingStation.STATE_READY) {
            barW = 158;
            colorTop = 0xFF7CFC00;
            colorBot = 0xFF3E9E00;
        } else {
            return;
        }
        if (barW > 0) {
            this.drawGradientRect(x + 9, y + 85, x + 9 + barW, y + 89, colorTop, colorBot);
        }
    }
}
