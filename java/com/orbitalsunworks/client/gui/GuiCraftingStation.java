package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.craftingstation.ContainerCraftingStation;
import com.orbitalsunworks.craftingstation.TileCraftingStation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial da Estação de Crafting Orbital (176x202).
 * Mantém as coordenadas exatas do ContainerCraftingStation:
 *  - Grade 3x5: (26 + col*18, 17 + row*18)
 *  - Resultado: (124, 35)
 *  - Inventário: y=108 | Hotbar: y=166
 */
@SideOnly(Side.CLIENT)
public class GuiCraftingStation extends GuiContainer {

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
        while (this.fontRendererObj.getStringWidth(title) > 134 && title.length() > 6) {
            title = title.substring(0, title.length() - 2) + "...";
        }
        this.fontRendererObj.drawStringWithShadow(title, 8, 4, GuiTheme.TEXT_TITLE);

        // Tag 3x5 discreta no cabeçalho à direita
        this.fontRendererObj.drawString("3x5", 150, 4, GuiTheme.TEXT_GOLD);

        // Micro rótulo sobre o slot de resultado
        this.fontRendererObj.drawString("OUT", 124, 24, GuiTheme.TEXT_MUTED);

        // Painel de status (y=75..94)
        int state = this.tile.clientState;
        String label;
        String right;
        int rightColor;
        if (state == TileCraftingStation.STATE_CRAFTING) {
            label = StatCollector.translateToLocal("gui.orbitalsunworks.crafting");
            float secs = Math.max(0, this.tile.clientTicksRemaining) / 20f;
            right = String.format("%.1fs", secs);
            rightColor = GuiTheme.TEXT_GOLD;
        } else if (state == TileCraftingStation.STATE_READY) {
            label = StatCollector.translateToLocal("gui.orbitalsunworks.done");
            right = StatCollector.translateToLocal("gui.orbitalsunworks.ready");
            rightColor = GuiTheme.TEXT_OK;
        } else {
            label = StatCollector.translateToLocal("gui.orbitalsunworks.idle");
            right = "";
            rightColor = GuiTheme.TEXT_LABEL;
        }

        this.fontRendererObj.drawString(label, 12, 77, GuiTheme.TEXT_LABEL);
        if (!right.isEmpty()) {
            this.fontRendererObj.drawString(right,
                164 - this.fontRendererObj.getStringWidth(right), 77, rightColor);
        }

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"), 8, 97, GuiTheme.TEXT_TITLE);

        // Dica no rodapé abaixo da hotbar
        String hint = StatCollector.translateToLocal("manual.os.station.note.short");
        while (this.fontRendererObj.getStringWidth(hint) > 160 && hint.length() > 6) {
            hint = hint.substring(0, hint.length() - 2) + "...";
        }
        this.fontRendererObj.drawString(hint,
            (this.xSize - this.fontRendererObj.getStringWidth(hint)) / 2, 188, GuiTheme.TEXT_MUTED);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        GuiTheme.drawAerospacePanel(x, y, this.xSize, this.ySize);
        GuiTheme.drawHeaderBar(x + 3, y + 2, this.xSize - 6, 12);

        // Baía da grade 3x5 + resultado
        GuiTheme.drawSubPanel(x + 7, y + 15, this.xSize - 14, 58);

        // 3 LEDs decorativos indicando as 3 linhas da grade
        int state = this.tile.clientState;
        int ledCol = state == TileCraftingStation.STATE_READY ? 0xFF7CFC00
            : (state == TileCraftingStation.STATE_CRAFTING ? 0xFF55E0FF : 0xFF284664);
        for (int row = 0; row < 3; row++) {
            drawRect(x + 13, y + 23 + row * 18, x + 17, y + 27 + row * 18, ledCol);
            for (int col = 0; col < 5; col++) {
                GuiTheme.drawSlotBox(x + 25 + col * 18, y + 16 + row * 18);
            }
        }

        // Seta/chevron entre a grade e o resultado
        float progress = 0f;
        if (state == TileCraftingStation.STATE_CRAFTING && this.tile.clientTicksTotal > 0) {
            progress = Math.min(1f, Math.max(0f,
                1f - (float) this.tile.clientTicksRemaining / this.tile.clientTicksTotal));
        } else if (state == TileCraftingStation.STATE_READY) {
            progress = 1f;
        }
        int arrowCol = state == TileCraftingStation.STATE_READY ? 0xFF7CFC00
            : (state == TileCraftingStation.STATE_CRAFTING ? 0xFF55E0FF : 0xFF324F6E);
        drawRect(x + 116, y + 41, x + 121, y + 45, arrowCol);
        drawRect(x + 118, y + 39, x + 120, y + 47, arrowCol);

        // Slot de resultado com destaque por estado
        int resBorder = state == TileCraftingStation.STATE_READY ? 0xFF7CFC00
            : (state == TileCraftingStation.STATE_CRAFTING ? 0xFF55E0FF : 0xFF3E6A8E);
        GuiTheme.drawSlotBox(x + 123, y + 34, resBorder, 0xFF0A121E);

        // Caixa do progresso abaixo da grade
        GuiTheme.drawSubPanel(x + 7, y + 75, this.xSize - 14, 19);
        int barTop = state == TileCraftingStation.STATE_READY ? 0xFF7CFC00 : 0xFF55E0FF;
        int barBot = state == TileCraftingStation.STATE_READY ? 0xFF3E9E00 : 0xFF0077AA;
        GuiTheme.drawProgressBar(x + 11, y + 87, 154, 5, progress, barTop, barBot);

        // Molduras do inventário 3x9 + hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiTheme.drawSlotBox(x + 7 + col * 18, y + 107 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiTheme.drawSlotBox(x + 7 + col * 18, y + 165);
        }
    }
}