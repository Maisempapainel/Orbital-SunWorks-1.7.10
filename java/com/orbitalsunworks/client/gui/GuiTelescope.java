package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.client.handler.ClientMeteorHandler;
import com.orbitalsunworks.network.MessageControlDataRequest;
import com.orbitalsunworks.network.MessageControlDataResponse;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiTelescope extends GuiScreen {

    private final int bx;
    private final int by;
    private final int bz;

    public GuiTelescope() {
        this(0, 64, 0);
    }

    public GuiTelescope(int x, int y, int z) {
        this.bx = x;
        this.by = y;
        this.bz = z;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.bx, this.by, this.bz));
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.buttonList.add(new GuiButton(1, cx - 105, cy + 28, 210, 20,
            "\u00a7bAbrir Vis\u00e3o Orbital em Tempo Real"));
        this.buttonList.add(new GuiButton(2, cx - 50, cy + 54, 100, 18, "Fechar"));
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 1) {
            this.mc.displayGuiScreen(new GuiTelescopeView(this.bx, this.by, this.bz));
        } else if (btn.id == 2) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        int gw = 260;
        int gh = 165;
        int left = (this.width - gw) / 2;
        int top = (this.height - gh) / 2;

        drawRect(left - 2, top - 2, left + gw + 2, top + gh + 2, 0xFF1F6E96);
        drawRect(left, top, left + gw, top + gh, 0xF00B111B);
        drawRect(left, top, left + gw, top + 22, 0xFF111C2C);

        this.fontRendererObj.drawStringWithShadow(
            "\u00a7b\u00a7lOBSERVAT\u00d3RIO ASTRON\u00d4MICO", left + 12, top + 7, 0xFFFFFF);

        int satCount = MessageControlDataResponse.clientSatellites.size();
        this.fontRendererObj.drawString(
            "Sat\u00e9lites rastreados: \u00a7e" + satCount, left + 14, top + 34, 0xD0E4F5);

        if (ClientMeteorHandler.active) {
            drawRect(left + 12, top + 50, left + gw - 12, top + 92, 0xFF361216);
            this.fontRendererObj.drawStringWithShadow(
                "\u00a7c\u00a7lCHUVA DE METEOROS DETECTADA", left + 18, top + 56, 0xFFFFFF);
            this.fontRendererObj.drawString(
                "Impacto estimado: \u00a7e" + ClientMeteorHandler.formatCountdown(), left + 18, top + 69, 0xFFD8D8);
            this.fontRendererObj.drawString(
                "Alvos em risco: \u00a7c" + ClientMeteorHandler.targetSatNames.size(), left + 18, top + 80, 0xFFAAAA);
        } else {
            drawRect(left + 12, top + 50, left + gw - 12, top + 92, 0xFF10202C);
            this.fontRendererObj.drawString(
                "\u00a7aC\u00e9u orbital limpo \u2014 sem amea\u00e7as.", left + 18, top + 60, 0x90EEAA);
            this.fontRendererObj.drawString(
                "\u00a77Observe sat\u00e9lites e avarias em tempo real.", left + 18, top + 73, 0x90A8C0);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}