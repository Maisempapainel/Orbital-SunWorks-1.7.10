package com.orbitalsunworks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.client.handler.ClientMeteorHandler;
import com.orbitalsunworks.network.MessageControlDataRequest;
import com.orbitalsunworks.network.MessageControlDataResponse;
import com.orbitalsunworks.network.MessageControlDataResponse.SatInfo;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiControlRoom extends GuiScreen {

    private final int bx;
    private final int by;
    private final int bz;
    private int refreshTicks = 0;
    private int scrollOffset = 0;

    public GuiControlRoom() {
        this(0, 64, 0);
    }

    public GuiControlRoom(int x, int y, int z) {
        this.bx = x;
        this.by = y;
        this.bz = z;
    }

    public static void updateClientData(long totalRFt, long storedRF, long maxRF, List<SatInfo> sats) {
        MessageControlDataResponse.clientTotalRFt = totalRFt;
        MessageControlDataResponse.clientStoredRF = storedRF;
        MessageControlDataResponse.clientMaxRF = maxRF;
        MessageControlDataResponse.clientAntennaStored = storedRF;
        MessageControlDataResponse.clientAntennaMax = maxRF;
        if (sats != null) {
            MessageControlDataResponse.clientSatellites = new ArrayList<SatInfo>(sats);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.bx, this.by, this.bz));

        int gw = 340;
        int gh = 224;
        int left = (this.width - gw) / 2;
        int top = (this.height - gh) / 2;

        this.buttonList.add(new GuiButton(1, left + gw - 146, top + gh - 26, 136, 18,
            "\u00a7bAbrir Observat\u00f3rio 3D"));
        this.buttonList.add(new GuiButton(2, left + 10, top + gh - 26, 56, 18, "Atualizar"));
        this.buttonList.add(new GuiButton(3, left + 70, top + gh - 26, 22, 18, "\u25b2"));
        this.buttonList.add(new GuiButton(4, left + 95, top + gh - 26, 22, 18, "\u25bc"));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.refreshTicks++;
        if (this.refreshTicks % 20 == 0) {
            OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.bx, this.by, this.bz));
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 1) {
            this.mc.displayGuiScreen(new GuiTelescopeView(this.bx, this.by, this.bz));
        } else if (btn.id == 2) {
            OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.bx, this.by, this.bz));
        } else if (btn.id == 3) {
            this.scrollOffset = Math.max(0, this.scrollOffset - 1);
        } else if (btn.id == 4) {
            int max = Math.max(0, MessageControlDataResponse.clientSatellites.size() - 5);
            this.scrollOffset = Math.min(max, this.scrollOffset + 1);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        int gw = 340;
        int gh = 224;
        int left = (this.width - gw) / 2;
        int top = (this.height - gh) / 2;

        drawRect(left - 2, top - 2, left + gw + 2, top + gh + 2, 0xFF1F6E96);
        drawRect(left, top, left + gw, top + gh, 0xF00B111B);
        drawRect(left, top, left + gw, top + 22, 0xFF111C2C);
        drawRect(left, top + 22, left + gw, top + 23, 0xFF28B8EA);

        this.fontRendererObj.drawStringWithShadow(
            "\u00a7b\u00a7lSALA DE CONTROLE ORBITAL", left + 10, top + 7, 0xFFFFFF);

        String antTierStr = "TORRE T" + MessageControlDataResponse.clientAntennaTier;
        this.fontRendererObj.drawString(
            antTierStr, left + gw - this.fontRendererObj.getStringWidth(antTierStr) - 10, top + 7, 0x70E6FF);

        int cardY = top + 28;
        drawRect(left + 10, cardY, left + gw - 10, cardY + 40, 0xFF101928);
        this.fontRendererObj.drawString(
            "Gera\u00e7\u00e3o Orbital Total: \u00a7a"
            + OSUtils.formatRF(MessageControlDataResponse.clientTotalRFt) + "/t",
            left + 16, cardY + 6, 0xDCE8F4);

        long stored = MessageControlDataResponse.clientStoredRF;
        long max = Math.max(1L, MessageControlDataResponse.clientMaxRF);
        float fill = Math.min(1.0f, (float) ((double) stored / (double) max));
        this.fontRendererObj.drawString(
            "Buffer da Antena: \u00a7b" + OSUtils.formatRF(stored) + " / " + OSUtils.formatRF(max) + " RF",
            left + 16, cardY + 18, 0xB8CBE0);

        int barX = left + 16;
        int barY = cardY + 30;
        int barW = gw - 32;
        drawRect(barX, barY, barX + barW, barY + 6, 0xFF1B2738);
        drawRect(barX, barY, barX + (int) (barW * fill), barY + 6, 0xFF26D4FF);

        int listY = cardY + 46;
        if (ClientMeteorHandler.active) {
            drawRect(left + 10, listY, left + gw - 10, listY + 18, 0xFF3A1216);
            this.fontRendererObj.drawStringWithShadow(
                "\u00a7c\u00a7lALERTA METEORO! \u00a7eImpacto em "
                + ClientMeteorHandler.formatCountdown() + " \u00a77("
                + ClientMeteorHandler.targetSatNames.size() + " alvo(s))",
                left + 16, listY + 5, 0xFFFFFF);
            listY += 22;
        }

        List<SatInfo> sats = MessageControlDataResponse.clientSatellites;
        this.fontRendererObj.drawString(
            "SAT\u00c9LITES ATIVOS (" + sats.size() + ")", left + 12, listY, 0x8DB2D0);
        listY += 12;

        if (sats.isEmpty()) {
            this.fontRendererObj.drawString(
                "\u00a77Nenhum sat\u00e9lite em \u00f3rbita. Lance um Foguete Tier 1!",
                left + 14, listY + 12, 0x90A4B8);
        } else {
            int maxRows = ClientMeteorHandler.active ? 4 : 5;
            for (int i = 0; i < maxRows && (i + this.scrollOffset) < sats.size(); i++) {
                SatInfo s = sats.get(i + this.scrollOffset);
                int ry = listY + i * 21;
                boolean threat = s.targeted || ClientMeteorHandler.isTargetString(s.id);
                drawRect(left + 10, ry, left + gw - 10, ry + 19, threat ? 0xFF2D161E : 0xFF121C2A);

                String prefix = threat ? "\u00a7c\u26a0 " : "\u00a7b";
                this.fontRendererObj.drawString(
                    prefix + s.name + " \u00a77[T" + s.tier + "]", left + 16, ry + 5, 0xFFFFFF);

                int pct = Math.round(s.health * 100f);
                int hpCol = pct > 60 ? 0x58EE78 : (pct > 30 ? 0xFFD248 : 0xFF5252);
                String hpStr = pct + "%";
                this.fontRendererObj.drawString(hpStr, left + 115, ry + 5, hpCol);

                String mods = (s.shieldTier > 0 ? "\u00a7b[Def T" + s.shieldTier + "] " : "")
                    + (s.efficiencyPct > 0 ? "\u00a7a[+" + s.efficiencyPct + "%]" : "");
                this.fontRendererObj.drawString(mods, left + 150, ry + 5, 0xCCCCCC);

                String genStr = "\u00a7e" + OSUtils.formatRF(s.generationRFt) + "/t";
                this.fontRendererObj.drawString(
                    genStr, left + gw - 16 - this.fontRendererObj.getStringWidth(genStr), ry + 5, 0xFFFFFF);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}