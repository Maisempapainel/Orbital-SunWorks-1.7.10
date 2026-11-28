package com.orbitalsunworks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.client.handler.ClientMeteorHandler;
import com.orbitalsunworks.item.ItemModule;
import com.orbitalsunworks.network.MessageControlDataRequest;
import com.orbitalsunworks.network.MessageControlDataResponse.SatInfo;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Sala de Controle Orbital — exibe todos os satélites, geração, saúde,
 * alerta de chuva de meteoros e ícones visuais dos módulos instalados (somente leitura).
 */
@SideOnly(Side.CLIENT)
public class GuiControlRoom extends GuiScreen {

    private static List<SatInfo> clientSatellites = new ArrayList<SatInfo>();
    private static long clientTotalRFt = 0L;
    private static long clientAntennaStored = 0L;
    private static long clientAntennaMax = 0L;

    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private int scrollOffset = 0;
    private int refreshTicks = 0;

    public GuiControlRoom() {
        this(0, 0, 0);
    }

    public GuiControlRoom(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    public static void updateClientData(long totalGen, long stored, long max, List<SatInfo> sats) {
        clientTotalRFt = totalGen;
        clientAntennaStored = stored;
        clientAntennaMax = max;
        clientSatellites = sats != null ? new ArrayList<SatInfo>(sats) : new ArrayList<SatInfo>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        this.buttonList.clear();
        int cx = this.width / 2;
        int cy = this.height / 2;
        this.buttonList.add(new GuiButton(1, cx - 155, cy + 88, 60, 18, "\u25b2 Subir"));
        this.buttonList.add(new GuiButton(2, cx - 90, cy + 88, 60, 18, "\u25bc Descer"));
        this.buttonList.add(new GuiButton(0, cx + 85, cy + 88, 70, 18, "Fechar"));
        OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.blockX, this.blockY, this.blockZ));
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.refreshTicks++;
        if (this.refreshTicks % 20 == 0) {
            OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.blockX, this.blockY, this.blockZ));
        }
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            this.mc.displayGuiScreen(null);
        } else if (btn.id == 1 && this.scrollOffset > 0) {
            this.scrollOffset--;
        } else if (btn.id == 2 && this.scrollOffset + 4 < clientSatellites.size()) {
            this.scrollOffset++;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int w = 344;
        int h = 230;
        int left = (this.width - w) / 2;
        int top = (this.height - h) / 2;

        drawRect(left, top, left + w, top + h, 0xEE08121E);
        drawHorizontalLine(left, left + w, top, 0xFF28D8FF);
        drawHorizontalLine(left, left + w, top + h, 0xFF1A6078);
        drawVerticalLine(left, top, top + h, 0xFF1A6078);
        drawVerticalLine(left + w, top, top + h, 0xFF1A6078);

        this.fontRendererObj.drawStringWithShadow("\u00a7b\u00a7lSALA DE CONTROLE ORBITAL", left + 12, top + 10, 0xFFFFFF);
        this.fontRendererObj.drawStringWithShadow(
            "Gera\u00e7\u00e3o Total: \u00a7a" + OSUtils.formatRF(clientTotalRFt) + "/t",
            left + 12, top + 24, 0xD0F0FF);
        this.fontRendererObj.drawStringWithShadow(
            "Sat\u00e9lites: \u00a7e" + clientSatellites.size(),
            left + 210, top + 24, 0xD0F0FF);

        int yCursor = top + 38;
        if (ClientMeteorHandler.active) {
            drawRect(left + 10, yCursor, left + w - 10, yCursor + 20, 0xCC4A1010);
            this.fontRendererObj.drawStringWithShadow(
                "\u00a7c\u00a7lALERTA METEORO \u00a77| Impacto em \u00a7e" + ClientMeteorHandler.formatCountdown()
                    + " \u00a77(" + ClientMeteorHandler.targetSatNames.size() + " alvo(s))",
                left + 16, yCursor + 6, 0xFFAAAA);
            yCursor += 24;
        }

        List<String> hoverTooltip = null;
        int maxRows = ClientMeteorHandler.active ? 4 : 5;
        if (clientSatellites.isEmpty()) {
            this.drawCenteredString(this.fontRendererObj, "\u00a77Nenhum sat\u00e9lite ativo em \u00f3rbita.",
                left + w / 2, yCursor + 38, 0x99AAB8);
        } else {
            for (int r = 0; r < maxRows && (this.scrollOffset + r) < clientSatellites.size(); r++) {
                SatInfo s = clientSatellites.get(this.scrollOffset + r);
                int rowTop = yCursor + r * 32;
                int rowBot = rowTop + 28;
                boolean threat = s.targeted || ClientMeteorHandler.isTargetString(s.id);
                drawRect(left + 10, rowTop, left + w - 10, rowBot, threat ? 0xAA3A1515 : 0xAA0F1F30);

                String tag = threat ? "\u00a7c[RISCO]" : "\u00a7a[OK]";
                this.fontRendererObj.drawStringWithShadow(
                    "\u00a7f" + s.name + " \u00a77(T" + s.tier + ") " + tag,
                    left + 16, rowTop + 4, 0xFFFFFF);

                int hpPct = Math.round(Math.max(0f, Math.min(1f, s.health)) * 100f);
                int barColor = hpPct >= 70 ? 0xFF32D870 : (hpPct >= 40 ? 0xFFE8B420 : 0xFFE83838);
                drawRect(left + 16, rowTop + 17, left + 116, rowTop + 23, 0xFF15202B);
                drawRect(left + 16, rowTop + 17, left + 16 + hpPct, rowTop + 23, barColor);
                this.fontRendererObj.drawStringWithShadow(hpPct + "%", left + 122, rowTop + 16, 0xD8E8F4);

                this.fontRendererObj.drawStringWithShadow(
                    "\u00a7a+" + OSUtils.formatRF(s.generationRFt) + "/t",
                    left + 158, rowTop + 4, 0x80FF98);

                // Ícones gráficos dos módulos (somente visualização, não removíveis)
                for (int m = 0; m < 2; m++) {
                    int bx = left + 250 + m * 36;
                    int by = rowTop + 5;
                    drawRect(bx, by, bx + 32, by + 18, 0xFF0A1622);
                    if (m < s.modules.size()) {
                        String modId = s.modules.get(m);
                        drawModuleBadgeIcon(bx, by, modId);
                        if (mouseX >= bx && mouseX <= bx + 32 && mouseY >= by && mouseY <= by + 18) {
                            hoverTooltip = new ArrayList<String>();
                            hoverTooltip.add("\u00a7b" + ItemModule.getDisplayName(modId));
                            hoverTooltip.add("\u00a77Status: \u00a7aInstalado no sat\u00e9lite");
                        }
                    } else {
                        this.fontRendererObj.drawString("---", bx + 7, by + 5, 0x45586A);
                    }
                }
            }
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (hoverTooltip != null) {
            this.drawHoveringText(hoverTooltip, mouseX, mouseY, this.fontRendererObj);
        }
    }

    private void drawModuleBadgeIcon(int x, int y, String modId) {
        int shieldTier = ItemModule.getShieldTier(modId);
        int effPct = ItemModule.getEfficiencyPercent(modId);
        if (shieldTier > 0) {
            int col = shieldTier == 3 ? 0xFFFFC840 : (shieldTier == 2 ? 0xFF40E890 : 0xFF38D4FF);
            drawRect(x + 3, y + 3, x + 13, y + 13, col);
            drawRect(x + 5, y + 5, x + 11, y + 11, 0xFF0B1C2C);
            this.fontRendererObj.drawString("S" + shieldTier, x + 15, y + 5, col);
        } else if (effPct > 0) {
            int col = effPct >= 18 ? 0xFF70FF50 : (effPct >= 12 ? 0xFFFFB830 : 0xFFFFE850);
            drawRect(x + 3, y + 4, x + 13, y + 14, col);
            drawRect(x + 6, y + 2, x + 10, y + 16, 0xFFFFFFFF);
            this.fontRendererObj.drawString("+" + effPct, x + 14, y + 5, col);
        } else {
            this.fontRendererObj.drawString("MOD", x + 6, y + 5, 0x90B0C8);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}