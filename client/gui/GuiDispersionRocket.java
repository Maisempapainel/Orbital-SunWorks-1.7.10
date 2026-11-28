package com.orbitalsunworks.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.block.BlockDispersionRocket;
import com.orbitalsunworks.client.handler.ClientMeteorHandler;
import com.orbitalsunworks.handler.OSLaunchGuard;
import com.orbitalsunworks.network.MessageLaunchDispersion;
import com.orbitalsunworks.rocket.ContainerDispersionRocket;
import com.orbitalsunworks.rocket.TileDispersionRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GUI minimalista do Foguete Dispersor de Meteoros:
 * mostra apenas o estado resumido e o botão de LANÇAR.
 */
@SideOnly(Side.CLIENT)
public class GuiDispersionRocket extends GuiContainer {

    private static final int BTN_X = 33;
    private static final int BTN_Y = 58;
    private static final int BTN_W = 110;
    private static final int BTN_H = 22;

    private final TileDispersionRocket tile;

    public GuiDispersionRocket(InventoryPlayer playerInv, TileDispersionRocket tile) {
        super(new ContainerDispersionRocket(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 96;
    }

    private boolean trajectoryBlocked() {
        if (this.tile == null || this.tile.getWorldObj() == null) {
            return true;
        }
        return !OSLaunchGuard.isTrajectoryClear(
            this.tile.getWorldObj(),
            this.tile.xCoord,
            this.tile.yCoord + BlockDispersionRocket.HEIGHT - 1,
            this.tile.zCoord,
            null);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = "Foguete Dispersor";
        this.fontRendererObj.drawStringWithShadow(title, 8, 5, GuiTheme.TEXT_TITLE);

        boolean active = ClientMeteorHandler.isActive();
        String status = active
            ? "Meteoro detectado (" + ClientMeteorHandler.getFormattedRemaining() + ")"
            : "Nenhum meteoro ativo";
        this.fontRendererObj.drawString(
            GuiTheme.truncate(this.fontRendererObj, status, 152),
            (this.xSize - this.fontRendererObj.getStringWidth(GuiTheme.truncate(this.fontRendererObj, status, 152))) / 2,
            26,
            active ? GuiTheme.TEXT_WARN : GuiTheme.TEXT_OK);

        String sub = active
            ? "Lan\u00e7ar cancela o impacto imediatamente"
            : "Pronto para intercepta\u00e7\u00e3o orbital";
        this.fontRendererObj.drawString(sub,
            (this.xSize - this.fontRendererObj.getStringWidth(sub)) / 2,
            39, GuiTheme.TEXT_MUTED);

        if (trajectoryBlocked()) {
            String warn = StatCollector.translateToLocal("gui.orbitalsunworks.launch.blocked");
            this.fontRendererObj.drawString(warn,
                (this.xSize - this.fontRendererObj.getStringWidth(warn)) / 2,
                84, GuiTheme.TEXT_WARN);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;

        GuiTheme.drawAerospacePanel(x, y, this.xSize, this.ySize);
        GuiTheme.drawHeaderBar(x + 3, y + 2, this.xSize - 6, 14);
        GuiTheme.drawSubPanel(x + 8, y + 20, this.xSize - 16, 31);

        boolean blocked = trajectoryBlocked();
        boolean hover = mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H;
        GuiTheme.drawButton(this.fontRendererObj, x + BTN_X, y + BTN_Y, BTN_W, BTN_H,
            "LAN\u00c7AR", hover, !blocked, blocked || ClientMeteorHandler.isActive());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0 || this.tile == null) {
            return;
        }
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;
        if (mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H) {
            if (trajectoryBlocked()) {
                this.mc.getSoundHandler().playSound(
                    PositionedSoundRecord.func_147674_a(new ResourceLocation("note.bass"), 0.6F));
                return;
            }
            this.mc.getSoundHandler().playSound(
                PositionedSoundRecord.func_147674_a(new ResourceLocation("random.click"), 1.0F));
            OrbitalSunworks.network.sendToServer(
                new MessageLaunchDispersion(this.tile.xCoord, this.tile.yCoord, this.tile.zCoord));
            this.mc.thePlayer.closeScreen();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}