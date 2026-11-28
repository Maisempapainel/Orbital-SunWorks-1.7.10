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

/**
 * Console de retorno à Terra no Foguete de Navegação estacionado em órbita (176x132).
 */
@SideOnly(Side.CLIENT)
public class GuiReturnHome extends GuiContainer {

    private static final int BTN_X = 28;
    private static final int BTN_Y = 92;
    private static final int BTN_W = 120;
    private static final int BTN_H = 22;

    private final TileNavRocket tile;

    public GuiReturnHome(InventoryPlayer playerInv, TileNavRocket tile) {
        super(new ContainerReturnHome(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 132;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        if (this.tile == null) {
            return;
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;

        GuiTheme.drawAerospacePanel(x, y, this.xSize, this.ySize);
        GuiTheme.drawHeaderBar(x + 3, y + 2, this.xSize - 6, 16);

        // Painel de telemetria de retorno
        GuiTheme.drawSubPanel(x + 8, y + 22, this.xSize - 16, 64);
        drawRect(x + 12, y + 39, x + this.xSize - 12, y + 40, GuiTheme.BORDER_SUB);

        // Caixa interna para coordenadas
        GuiTheme.drawSubPanel(x + 14, y + 58, this.xSize - 28, 21);

        boolean hover = mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H;
        String btnText = StatCollector.translateToLocal("gui.orbitalsunworks.return.button");
        GuiTheme.drawButton(this.fontRendererObj, x + BTN_X, y + BTN_Y, BTN_W, BTN_H,
            btnText, hover, true, false);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (this.tile == null) {
            return;
        }
        String title = GuiTheme.truncate(this.fontRendererObj,
            StatCollector.translateToLocal("gui.orbitalsunworks.return.title"), 162);
        this.fontRendererObj.drawStringWithShadow(title,
            (this.xSize - this.fontRendererObj.getStringWidth(title)) / 2, 6, GuiTheme.TEXT_TITLE);

        String status = StatCollector.translateToLocal("gui.orbitalsunworks.return.status");
        this.fontRendererObj.drawString(status, 14, 27, GuiTheme.TEXT_LABEL);
        String ready = StatCollector.translateToLocal("gui.orbitalsunworks.return.ready");
        this.fontRendererObj.drawString(ready,
            162 - this.fontRendererObj.getStringWidth(ready), 27, GuiTheme.TEXT_OK);

        String dest = StatCollector.translateToLocal("gui.orbitalsunworks.return.dest");
        this.fontRendererObj.drawString(dest, 14, 44, GuiTheme.TEXT_LABEL);
        String dimLabel = "DIM " + this.tile.homeDim;
        this.fontRendererObj.drawStringWithShadow(dimLabel,
            162 - this.fontRendererObj.getStringWidth(dimLabel), 44, GuiTheme.TEXT_GOLD);

        String coords = "X " + (int) Math.floor(this.tile.homeX)
            + "   Y " + (int) Math.floor(this.tile.homeY)
            + "   Z " + (int) Math.floor(this.tile.homeZ);
        this.fontRendererObj.drawString(coords,
            (this.xSize - this.fontRendererObj.getStringWidth(coords)) / 2, 65, GuiTheme.TEXT_BODY);

        String foot = GuiTheme.truncate(this.fontRendererObj,
            "Retorno direto \u00e0 plataforma de origem", this.xSize - 16);
        this.fontRendererObj.drawString(foot,
            (this.xSize - this.fontRendererObj.getStringWidth(foot)) / 2, 118, GuiTheme.TEXT_MUTED);
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