package com.orbitalsunworks.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageReturnViaRocket;
import com.orbitalsunworks.rocket.ContainerReturnHome;
import com.orbitalsunworks.rocket.TileNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiReturnHome extends GuiContainer {

    private static final int BTN_X = 48, BTN_Y = 92, BTN_W = 80, BTN_H = 20;

    private final TileNavRocket tile;

    public GuiReturnHome(InventoryPlayer playerInv, TileNavRocket tile) {
        super(new ContainerReturnHome(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 128;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;

        drawRect(x, y, x + this.xSize, y + this.ySize, 0xFF0B111A);
        drawRect(x, y, x + this.xSize, y + 1, 0xFF28557A);
        drawRect(x, y + this.ySize - 1, x + this.xSize, y + this.ySize, 0xFF28557A);
        drawRect(x, y, x + 1, y + this.ySize, 0xFF28557A);
        drawRect(x + this.xSize - 1, y, x + this.xSize, y + this.ySize, 0xFF28557A);

        drawRect(x + 6, y + 6, x + this.xSize - 6, y + 22, 0xFF132234);
        drawRect(x + 8, y + 28, x + this.xSize - 8, y + 84, 0xFF101A28);

        boolean hover = mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H;
        int bx = x + BTN_X;
        int by = y + BTN_Y;
        this.drawGradientRect(bx, by, bx + BTN_W, by + BTN_H,
            hover ? 0xFF226E99 : 0xFF174A6B, hover ? 0xFF154566 : 0xFF0E3048);
        drawRect(bx, by, bx + BTN_W, by + 1, 0xFF55E0FF);
        drawRect(bx, by + BTN_H - 1, bx + BTN_W, by + BTN_H, 0xFF1E6A8F);
        drawRect(bx, by, bx + 1, by + BTN_H, 0xFF399EC4);
        drawRect(bx + BTN_W - 1, by, bx + BTN_W, by + BTN_H, 0xFF399EC4);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (this.tile == null) {
            return;
        }
        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        String title = StatCollector.translateToLocal("gui.orbitalsunworks.return.title");
        this.fontRendererObj.drawStringWithShadow(title,
            (this.xSize - this.fontRendererObj.getStringWidth(title)) / 2, 10, 0x55E0FF);

        String status = StatCollector.translateToLocal("gui.orbitalsunworks.return.status");
        this.fontRendererObj.drawString(status, 14, 34, 0x9FB4C7);
        String ready = StatCollector.translateToLocal("gui.orbitalsunworks.return.ready");
        this.fontRendererObj.drawString(ready,
            162 - this.fontRendererObj.getStringWidth(ready), 34, 0x7CFC00);

        String dest = StatCollector.translateToLocal("gui.orbitalsunworks.return.dest");
        this.fontRendererObj.drawString(dest, 14, 50, 0x9FB4C7);
        String dimLabel = "DIM " + this.tile.homeDim;
        this.fontRendererObj.drawString(dimLabel,
            162 - this.fontRendererObj.getStringWidth(dimLabel), 50, 0xFFFFFF);

        String coords = "X " + (int) Math.floor(this.tile.homeX)
            + "  Y " + (int) Math.floor(this.tile.homeY)
            + "  Z " + (int) Math.floor(this.tile.homeZ);
        this.fontRendererObj.drawString(coords,
            (this.xSize - this.fontRendererObj.getStringWidth(coords)) / 2, 66, 0xC7D0DA);

        String launch = StatCollector.translateToLocal("gui.orbitalsunworks.launch");
        boolean hover = mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H;
        this.fontRendererObj.drawStringWithShadow(launch,
            BTN_X + (BTN_W - this.fontRendererObj.getStringWidth(launch)) / 2,
            BTN_Y + 6, hover ? 0xFFFF80 : 0xFFE9B0);
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
            this.mc.getSoundHandler().playSound(
                PositionedSoundRecord.func_147674_a(new ResourceLocation("random.click"), 1.0F));
            OrbitalSunworks.network.sendToServer(
                new MessageReturnViaRocket(this.tile.xCoord, this.tile.yCoord, this.tile.zCoord));
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}