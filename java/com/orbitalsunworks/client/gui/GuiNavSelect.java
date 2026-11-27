package com.orbitalsunworks.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageRequestSatellites;
import com.orbitalsunworks.network.MessageSatelliteList;
import com.orbitalsunworks.network.MessageTravelToSatellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * GUI de seleção de satélite — Foguete de Navegação (v0.6.0).
 *
 * Lista OS SATÉLITES DO JOGADOR (nome, vida útil %, tier), cada um com um
 * botão [IR] ao lado. Ao clicar IR: envia MessageTravelToSatellite, a GUI
 * fecha e a animação de lançamento começa (o jogador entra no foguete).
 *
 * A lista vem do servidor via MessageSatelliteList (pedido ao abrir).
 */
@SideOnly(Side.CLIENT)
public class GuiNavSelect extends GuiScreen {

    /** Última lista recebida (o handler do pacote alimenta). */
    private static MessageSatelliteList lastList;

    private int scrollOffset = 0;
    private static final int ROWS_VISIBLE = 6;

    private GuiButton goButtons[];
    private GuiButton btnUp, btnDown;

    /** Coordenadas da BASE do bloco do foguete (para o IR: o servidor
     *  valida o bloco, remove a coluna e spawna a entidade no pad). */
    private final int blockX, blockY, blockZ;

    public GuiNavSelect(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
    }

    /** Alimentada pelo handler do MessageSatelliteList. */
    public static void feedList(MessageSatelliteList list) {
        lastList = list;
        // Se a GUI está aberta, avisa para redesenhar (o Minecraft chama
        // o updateScreen do GuiScreen a cada tick de cliente).
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.goButtons = new GuiButton[ROWS_VISIBLE];
        for (int i = 0; i < ROWS_VISIBLE; i++) {
            int y = this.height / 2 - 60 + i * 22;
            this.goButtons[i] = new GuiButton(100 + i, this.width / 2 + 60, y, 40, 20, "IR");
            this.buttonList.add(this.goButtons[i]);
        }
        this.btnUp = new GuiButton(200, this.width / 2 - 100, this.height / 2 - 84, 20, 20, "^");
        this.btnDown = new GuiButton(201, this.width / 2 - 100, this.height / 2 + 76, 20, 20, "v");
        this.buttonList.add(this.btnUp);
        this.buttonList.add(this.btnDown);
        // Pede a lista ao servidor (SÓ os satélites do jogador).
        OrbitalSunworks.network.sendToServer(new MessageRequestSatellites());
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Re-pede a lista se ainda não chegou (fallback de rede).
        if (lastList == null && this.mc.theWorld != null
            && this.mc.theWorld.getTotalWorldTime() % 20 == 0) {
            OrbitalSunworks.network.sendToServer(new MessageRequestSatellites());
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (lastList == null) {
            return;
        }
        if (button.id == 200 && this.scrollOffset > 0) {
            this.scrollOffset--;
        } else if (button.id == 201
            && this.scrollOffset + ROWS_VISIBLE < lastList.getCount()) {
            this.scrollOffset++;
        } else if (button.id >= 100 && button.id < 100 + ROWS_VISIBLE) {
            int row = this.scrollOffset + (button.id - 100);
            if (row < lastList.getCount()) {
                java.util.UUID satId = java.util.UUID.fromString(lastList.getUuid(row));
                OrbitalSunworks.network.sendToServer(new MessageTravelToSatellite(
                    satId, this.blockX, this.blockY, this.blockZ));
                this.mc.displayGuiScreen(null); // fecha; a animação começa
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        // Fundo escurecido + painel.
        this.drawDefaultBackground();
        GL11.glColor4f(1, 1, 1, 1);
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Painel.
        drawRect(cx - 110, cy - 92, cx + 110, cy + 92, 0xC0101826);
        drawRect(cx - 110, cy - 92, cx + 110, cy - 88, 0xFF2A9D8F);

        // Título.
        String title = StatCollector.translateToLocal("gui.orbitalsunworks.nav.title");
        this.fontRendererObj.drawString(title, cx - this.fontRendererObj.getStringWidth(title) / 2,
            cy - 84, 0x55E0FF);

        int n = lastList != null ? lastList.getCount() : 0;
        if (n == 0) {
            String none = StatCollector.translateToLocal("gui.orbitalsunworks.nav.none");
            this.fontRendererObj.drawString(none,
                cx - this.fontRendererObj.getStringWidth(none) / 2, cy - 20, 0x9FB4C7);
            String hint = StatCollector.translateToLocal("gui.orbitalsunworks.nav.hint");
            this.fontRendererObj.drawString(hint,
                cx - this.fontRendererObj.getStringWidth(hint) / 2, cy - 4, 0x5A6878);
        } else {
            // Cabeçalho.
            this.fontRendererObj.drawString(StatCollector.translateToLocal(
                "gui.orbitalsunworks.nav.header.name"), cx - 100, cy - 70, 0x9FB4C7);
            this.fontRendererObj.drawString(StatCollector.translateToLocal(
                "gui.orbitalsunworks.nav.header.health"), cx + 20, cy - 70, 0x9FB4C7);
            this.fontRendererObj.drawString(StatCollector.translateToLocal(
                "gui.orbitalsunworks.nav.header.tier"), cx + 68, cy - 70, 0x9FB4C7);

            int rows = Math.min(ROWS_VISIBLE, n - this.scrollOffset);
            for (int i = 0; i < rows; i++) {
                int row = this.scrollOffset + i;
                int y = cy - 60 + i * 22;
                // Linha alternada.
                if (i % 2 == 0) {
                    drawRect(cx - 105, y - 3, cx + 105, y + 15, 0x18FFFFFF);
                }
                String name = lastList.getName(row);
                float health = lastList.getHealth(row);
                int tier = lastList.getTier(row);
                this.fontRendererObj.drawString(name, cx - 100, y, 0xFFFFFF);
                String h = Math.round(health * 100) + "%";
                this.fontRendererObj.drawString(h, cx + 20, y,
                    health > 0.4f ? 0x7CFC00 : 0xFF5555);
                this.fontRendererObj.drawString("T" + tier, cx + 68, y, 0xFFD970);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            this.mc.displayGuiScreen(null);
        }
    }
}
