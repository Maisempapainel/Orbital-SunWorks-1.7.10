package com.orbitalsunworks.client.gui;

import java.util.UUID;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageRequestSatellites;
import com.orbitalsunworks.network.MessageSatelliteList;
import com.orbitalsunworks.network.MessageTravelToSatellite;
import com.orbitalsunworks.rocket.TileNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * MAPA ORBITAL — GUI do Foguete de Navegação posicionado (v0.8.0, REFEITA).
 *
 * HUD decente de verdade: painel com moldura, cabeçalho com título e
 * satélites do jogador em LINHAS com barra de vida, tier e botão LANÇAR
 * por linha; scrollbar própria para rolar a lista; rodapé com dica.
 *
 * Os dados vêm do servidor (MessageSatelliteList) — a GUI pede ao abrir
 * e re-pede a cada segundo se ainda não chegou (fallback de rede).
 *
 * Correções v0.8.0:
 *  - GUI baseada em GuiContainer com o Container dummy do servidor (o
 *    FML 1.7.10 NÃO abre GUI se o servidor devolver null — o bug do
 *    "clicar não faz nada");
 *  - botões desabilitados quando não há satélite na linha;
 *  - layout com textura própria (gui/navSelect.png 256x256) em vez de
 *    retângulos crus.
 */
@SideOnly(Side.CLIENT)
public class GuiNavRocket extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "orbitalsunworks", "textures/gui/navSelect.png");

    private static final int PANEL_X = 16, PANEL_Y = 12, PANEL_W = 224, PANEL_H = 190;
    private static final int LIST_X = 26, LIST_Y = 52, LIST_W = 204, LIST_H = 110;
    private static final int ROW_H = 22;
    private static final int ROWS_VISIBLE = 5;

    /** Última lista recebida (o handler do pacote alimenta). */
    private static MessageSatelliteList lastList;

    private int scrollOffset = 0;

    private final GuiButton launchButtons[] = new GuiButton[ROWS_VISIBLE];
    private GuiButton btnUp, btnDown;

    private final TileNavRocket tile;
    private int requestTimer = 0;

    public GuiNavRocket(InventoryPlayer playerInv, TileNavRocket tile) {
        super(new com.orbitalsunworks.rocket.ContainerNavRocket(playerInv, tile));
        this.tile = tile;
        this.xSize = 256;
        this.ySize = 214;
    }

    /** Alimentada pelo handler do MessageSatelliteList. */
    public static void feedList(MessageSatelliteList list) {
        lastList = list;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        int x0 = (this.width - this.xSize) / 2;
        int y0 = (this.height - this.ySize) / 2;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            this.launchButtons[i] = new GuiButton(100 + i,
                x0 + LIST_X + LIST_W - 56, y0 + LIST_Y + 2 + i * ROW_H, 50, 18, "LANÇAR");
            this.launchButtons[i].enabled = false;
            this.buttonList.add(this.launchButtons[i]);
        }
        // Scrollbar à direita da lista.
        this.btnUp = new GuiButton(200, x0 + PANEL_X + PANEL_W - 16, y0 + LIST_Y - 1, 14, 16, "^");
        this.btnDown = new GuiButton(201, x0 + PANEL_X + PANEL_W - 16, y0 + LIST_Y + LIST_H - 15, 14, 16, "v");
        this.buttonList.add(this.btnUp);
        this.buttonList.add(this.btnDown);
        // Pede a lista ao servidor (SÓ os satélites do jogador).
        OrbitalSunworks.network.sendToServer(new MessageRequestSatellites());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Re-pede a lista se ainda não chegou (1x/s; fallback de rede).
        this.requestTimer++;
        if (lastList == null && this.requestTimer % 20 == 0) {
            OrbitalSunworks.network.sendToServer(new MessageRequestSatellites());
        }
        this.refreshButtons();
    }

    private void refreshButtons() {
        int n = lastList != null ? lastList.getCount() : 0;
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int row = this.scrollOffset + i;
            this.launchButtons[i].enabled = row < n;
        }
        this.btnUp.enabled = this.scrollOffset > 0;
        this.btnDown.enabled = this.scrollOffset + ROWS_VISIBLE < n;
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (lastList == null) {
            return;
        }
        if (button.id == 200 && this.scrollOffset > 0) {
            this.scrollOffset--;
            this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord
                .func_147674_a(new ResourceLocation("random.click"), 0.8F));
        } else if (button.id == 201
            && this.scrollOffset + ROWS_VISIBLE < lastList.getCount()) {
            this.scrollOffset++;
            this.mc.getSoundHandler().playSound(net.minecraft.client.audio.PositionedSoundRecord
                .func_147674_a(new ResourceLocation("random.click"), 0.8F));
        } else if (button.id >= 100 && button.id < 100 + ROWS_VISIBLE) {
            int row = this.scrollOffset + (button.id - 100);
            if (row < lastList.getCount()) {
                UUID satId = UUID.fromString(lastList.getUuid(row));
                OrbitalSunworks.network.sendToServer(new MessageTravelToSatellite(
                    satId, this.tile.xCoord, this.tile.yCoord, this.tile.zCoord));
                this.mc.displayGuiScreen(null); // fecha; a animação começa
            }
        }
        this.refreshButtons();
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
        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        // ---------------- Cabeçalho ----------------
        String title = StatCollector.translateToLocal("gui.orbitalsunworks.nav.title");
        this.fontRendererObj.drawStringWithShadow(title,
            PANEL_X + (PANEL_W - this.fontRendererObj.getStringWidth(title)) / 2, 20, 0x55E0FF);

        int n = lastList != null ? lastList.getCount() : 0;

        // Colunas do cabeçalho da lista.
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.nav.header.name"), LIST_X, LIST_Y - 12, 0x9FB4C7);
        String gen = StatCollector.translateToLocal("gui.orbitalsunworks.nav.header.health");
        this.fontRendererObj.drawString(gen, LIST_X + 108 - this.fontRendererObj.getStringWidth(gen), LIST_Y - 12, 0x9FB4C7);

        if (n == 0) {
            String none = StatCollector.translateToLocal("gui.orbitalsunworks.nav.none");
            this.fontRendererObj.drawStringWithShadow(none,
                PANEL_X + (PANEL_W - this.fontRendererObj.getStringWidth(none)) / 2,
                LIST_Y + 34, 0xB8C7D8);
            String hint = StatCollector.translateToLocal("gui.orbitalsunworks.nav.hint");
            this.fontRendererObj.drawString(hint,
                PANEL_X + (PANEL_W - this.fontRendererObj.getStringWidth(hint)) / 2,
                LIST_Y + 50, 0x5A6878);
        } else {
            int rows = Math.min(ROWS_VISIBLE, n - this.scrollOffset);
            for (int i = 0; i < rows; i++) {
                int row = this.scrollOffset + i;
                int y = LIST_Y + i * ROW_H;
                String name = lastList.getName(row);
                float health = lastList.getHealth(row);
                int tier = lastList.getTier(row);

                // Nome (trunca se largo demais) + tier.
                String shown = name;
                while (this.fontRendererObj.getStringWidth(shown) > 96 && shown.length() > 3) {
                    shown = shown.substring(0, shown.length() - 2) + "…";
                }
                this.fontRendererObj.drawStringWithShadow(shown, LIST_X, y + 6, 0xFFFFFF);
                String t = "T" + tier;
                this.fontRendererObj.drawString(t, LIST_X + 100
                    - this.fontRendererObj.getStringWidth(t), y + 6, 0xFFD970);

                // Barra de vida (moldura na textura: faixa 32px na linha).
                int bx = LIST_X + 4;
                int by = y + 15;
                int bw = 64;
                int filled = (int) (bw * Math.max(0f, Math.min(1f, health)));
                drawRect(bx, by, bx + bw, by + 3, 0xFF10151F);
                if (filled > 0) {
                    drawRect(bx + 1, by + 1, bx + 1 + filled - 2, by + 2,
                        health > 0.4f ? 0xFF3DDC55 : 0xFFE04040);
                }
                String h = Math.round(health * 100) + "%";
                this.fontRendererObj.drawString(h, bx + bw + 4, by - 3,
                    health > 0.4f ? 0x7CFC00 : 0xFF5555);
            }
            // Contador no rodapé.
            String count = (this.scrollOffset + 1) + "-" +
                Math.min(this.scrollOffset + ROWS_VISIBLE, n) + " / " + n;
            this.fontRendererObj.drawString(count,
                PANEL_X + PANEL_W - 14 - this.fontRendererObj.getStringWidth(count),
                LIST_Y + LIST_H + 8, 0x9FB4C7);
        }

        // Rodapé: dica + cooldown de lançamento.
        String tip = StatCollector.translateToLocal("gui.orbitalsunworks.nav.footer");
        this.fontRendererObj.drawString(tip, PANEL_X + 8, 178, 0x5A6878);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
