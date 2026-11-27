package com.orbitalsunworks.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageReturnViaRocket;
import com.orbitalsunworks.rocket.EntityNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD da NAVE ESTACIONADA na órbita (v0.8.0) — "Voltar à Terra".
 *
 * A nave do jogador ficou flutuando na órbita esperando; ao clicar com
 * o botão direito nela abre este painel: status da nave, coordenadas
 * do pad de origem e o botão INICIAR RETORNO. Sem animação de
 * decolagem: o teleporte é direto pro pad (pedido do usuário), e o
 * bloco do foguete é reposto na plataforma, com o item intacto.
 */
@SideOnly(Side.CLIENT)
public class GuiReturnHome extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "orbitalsunworks", "textures/gui/returnHome.png");

    private static final int BTN_X = 66, BTN_Y = 120, BTN_W = 124, BTN_H = 20;

    private final EntityNavRocket rocket;

    public GuiReturnHome(InventoryPlayer playerInv, EntityNavRocket rocket) {
        super(new com.orbitalsunworks.rocket.ContainerReturnHome(playerInv));
        this.rocket = rocket;
        this.xSize = 256;
        this.ySize = 166;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        int x0 = (this.width - this.xSize) / 2;
        int y0 = (this.height - this.ySize) / 2;
        this.buttonList.add(new GuiButton(1, x0 + BTN_X, y0 + BTN_Y, BTN_W, BTN_H,
            StatCollector.translateToLocal("gui.orbitalsunworks.return.button")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 1 && this.rocket != null) {
            OrbitalSunworks.network.sendToServer(
                new MessageReturnViaRocket(this.rocket.getEntityId()));
            this.mc.displayGuiScreen(null);
        }
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
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        if (this.rocket == null) {
            return;
        }
        // Título.
        String title = StatCollector.translateToLocal("gui.orbitalsunworks.return.title");
        this.fontRendererObj.drawStringWithShadow(title,
            (this.xSize - this.fontRendererObj.getStringWidth(title)) / 2, 14, 0x55E0FF);

        // Status da nave.
        String status = StatCollector.translateToLocal("gui.orbitalsunworks.return.status");
        this.fontRendererObj.drawString(status, 24, 40, 0x9FB4C7);
        String ready = StatCollector.translateToLocal("gui.orbitalsunworks.return.ready");
        this.fontRendererObj.drawString(ready,
            232 - this.fontRendererObj.getStringWidth(ready), 40, 0x7CFC00);

        // Destino: pad de origem (coords da casa gravadas na viagem).
        String dest = StatCollector.translateToLocal("gui.orbitalsunworks.return.dest");
        this.fontRendererObj.drawString(dest, 24, 58, 0x9FB4C7);
        String dimName = this.rocket.worldObj.provider.getDimensionName();
        this.fontRendererObj.drawString(dimName,
            232 - this.fontRendererObj.getStringWidth(dimName), 58, 0xFFFFFF);

        String pad = "X " + (int) Math.floor(this.rocket.getHomeX() - 2.5)
            + "  Y " + (int) Math.floor(this.rocket.getHomeY())
            + "  Z " + (int) Math.floor(this.rocket.getHomeZ() - 0.5);
        this.fontRendererObj.drawString(pad,
            232 - this.fontRendererObj.getStringWidth(net.minecraft.util.EnumChatFormatting
                .getTextWithoutFormattingCodes(pad)), 70, 0xC7D0DA);

        // Nota: sem animação (teleporte direto).
        String note = StatCollector.translateToLocal("gui.orbitalsunworks.return.note");
        this.fontRendererObj.drawSplitString(note, 24, 92, 208, 0x8FA3B8);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
