package com.orbitalsunworks.client.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageLaunchRocket;
import com.orbitalsunworks.rocket.ContainerRocket;
import com.orbitalsunworks.rocket.TileRocket;

/**
 * GUI do Foguete Tier 1 — painel de controle v5 (176x214).
 *
 * Layout com FAIXAS PRÓPRIAS, nada encosta em nada:
 * - y 6:   título;
 * - y 21:  "MÓDULOS" à esquerda + status escudo à direita;
 * - y 32:  slots de módulo;
 * - y 57:  "INTEGRIDADE" (rótulo) e % à direita;
 * - y 67:  barra de vida (moldura na textura, preenchimento y 68..74);
 * - y 82:  botão LANCAR (centrado, 60x18) — CINZA e sem enviar pacote
 *          quando o trajeto está obstruído (aviso "TRAJETO BLOQUEADO"
 *          embaixo, em vermelho);
 * - y 106: "Inventário" (depois do botão, SEM sobreposição);
 * - y 118: inventário 3x9; hotbar y 176 (fim 194 -> ySize 214... ver abaixo).
 */
public class GuiRocket extends GuiContainer {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "orbitalsunworks", "textures/gui/rocket.png");

    /** Botão LANCAR: centrado. */
    private static final int BTN_X = 58, BTN_Y = 82, BTN_W = 60, BTN_H = 18;

    /** Trajeto livre? (scan no mundo do CLIENTE; servidor revalida). */
    private boolean trajectoryBlocked() {
        return !com.orbitalsunworks.handler.OSLaunchGuard.isTrajectoryClear(
            this.tile.getWorldObj(), this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, null);
    }

    private final TileRocket tile;

    public GuiRocket(InventoryPlayer playerInv, TileRocket tile) {
        super(new ContainerRocket(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 214;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        int mx = mouseX - (this.width - this.xSize) / 2;
        int my = mouseY - (this.height - this.ySize) / 2;

        // Título.
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.rocket.title"), 8, 6, 0x55E0FF);

        // Módulos: rótulo + escudo na mesma linha, lados opostos.
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.rocket.modules"), 8, 21, 0x9FB4C7);
        String shield = StatCollector.translateToLocal("gui.orbitalsunworks.rocket.shield_short");
        String shieldTxt = shield + ": " + (this.tile.hasDebrisShield() ? "ON" : "OFF");
        this.fontRendererObj.drawString(shieldTxt,
            168 - this.fontRendererObj.getStringWidth(shieldTxt), 21,
            this.tile.hasDebrisShield() ? 0x00E8FF : 0x5A6878);

        // Integridade: rótulo y57 (FORA da barra, que começa em y67).
        int healthPct = (int) (this.tile.getHealth() * 100);
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "gui.orbitalsunworks.rocket.health"), 8, 57, 0x9FB4C7);
        String pct = healthPct + "%";
        this.fontRendererObj.drawString(pct,
            168 - this.fontRendererObj.getStringWidth(pct), 57,
            healthPct > 40 ? 0x7CFC00 : 0xFF5555);

        // Texto do botão LANCAR (corpo na textura, faixa própria y82..100).
        String launch = StatCollector.translateToLocal("gui.orbitalsunworks.launch");
        int tx = BTN_X + (BTN_W - this.fontRendererObj.getStringWidth(launch)) / 2;
        boolean hover = mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H;
        boolean blocked = trajectoryBlocked();
        if (blocked) {
            // Bloqueado: botão apagado + aviso vermelho embaixo.
            this.fontRendererObj.drawString(launch, tx, BTN_Y + 5, 0x5A6878);
            String warn = StatCollector.translateToLocal("gui.orbitalsunworks.launch.blocked");
            int wx = (this.xSize - this.fontRendererObj.getStringWidth(warn)) / 2;
            this.fontRendererObj.drawString(warn, wx, BTN_Y + BTN_H + 2, 0xFF5555);
        } else {
            this.fontRendererObj.drawString(launch, tx, BTN_Y + 5, hover ? 0xFFFF80 : 0xFFE9B0);
        }

        // Inventário: DEPOIS do botão, sem encostar (y106).
        this.fontRendererObj.drawString(StatCollector.translateToLocal(
            "container.inventory"), 8, 106, 0x55E0FF);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Preenchimento da barra de integridade dentro da moldura da
        // textura: x 9..167, y 68..74.
        float health = this.tile.getHealth();
        int barW = (int) (158 * health);
        if (barW > 0) {
            boolean ok = health > 0.4f;
            this.drawGradientRect(x + 9, y + 68, x + 9 + barW, y + 74,
                ok ? 0xFF7CFC00 : 0xFFFF5555, ok ? 0xFF3E9E00 : 0xFFB81E1E);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;
        if (button == 0 && mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H) {
            if (trajectoryBlocked()) {
                // Bloqueado: som de erro, nenhum pacote enviado.
                net.minecraft.client.audio.PositionedSoundRecord snd =
                    net.minecraft.client.audio.PositionedSoundRecord.func_147674_a(
                        new ResourceLocation("note.bass"), 0.6F);
                this.mc.getSoundHandler().playSound(snd);
                return;
            }
            net.minecraft.client.audio.PositionedSoundRecord snd =
                net.minecraft.client.audio.PositionedSoundRecord.func_147674_a(
                    new ResourceLocation("random.click"), 1.0F);
            this.mc.getSoundHandler().playSound(snd);
            OrbitalSunworks.network.sendToServer(new MessageLaunchRocket(
                this.tile.xCoord, this.tile.yCoord, this.tile.zCoord));
        }
    }
}
