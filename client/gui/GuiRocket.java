package com.orbitalsunworks.client.gui;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.handler.OSLaunchGuard;
import com.orbitalsunworks.network.MessageLaunchRocket;
import com.orbitalsunworks.rocket.ContainerRocket;
import com.orbitalsunworks.rocket.TileRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD aeroespacial do Foguete Orbital Tier 1 (176x214).
 * Slots idênticos ao ContainerRocket:
 *  - Módulos: (62, 32) e (98, 32)
 *  - Inventário: y=118 | Hotbar: y=176
 */
@SideOnly(Side.CLIENT)
public class GuiRocket extends GuiContainer {

    private static final int BTN_X = 38;
    private static final int BTN_Y = 78;
    private static final int BTN_W = 100;
    private static final int BTN_H = 17;

    private final TileRocket tile;

    public GuiRocket(InventoryPlayer playerInv, TileRocket tile) {
        super(new ContainerRocket(playerInv, tile));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 214;
    }

    private boolean trajectoryBlocked() {
        return !OSLaunchGuard.isTrajectoryClear(
            this.tile.getWorldObj(), this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, null);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = StatCollector.translateToLocal("gui.orbitalsunworks.rocket.title");
        this.fontRendererObj.drawStringWithShadow(title, 8, 5, GuiTheme.TEXT_TITLE);

        // Módulos + Escudo
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.rocket.modules"),
            12, 20, GuiTheme.TEXT_LABEL);
        boolean shieldOn = this.tile.hasDebrisShield();
        String shield = StatCollector.translateToLocal("gui.orbitalsunworks.rocket.shield_short");
        String shieldTxt = shield + ": " + (shieldOn ? "ON" : "OFF");
        this.fontRendererObj.drawString(shieldTxt,
            164 - this.fontRendererObj.getStringWidth(shieldTxt), 20,
            shieldOn ? 0x00E8FF : GuiTheme.TEXT_MUTED);

        // Rótulos laterais das baías
        this.fontRendererObj.drawString("M1", 44, 36, GuiTheme.TEXT_MUTED);
        this.fontRendererObj.drawString("M2", 120, 36, GuiTheme.TEXT_MUTED);

        // Integridade
        int healthPct = Math.round(Math.max(0f, Math.min(1f, this.tile.getHealth())) * 100f);
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.rocket.health"),
            12, 56, GuiTheme.TEXT_LABEL);
        String pct = healthPct + "%";
        this.fontRendererObj.drawString(pct,
            164 - this.fontRendererObj.getStringWidth(pct), 56,
            healthPct > 40 ? GuiTheme.TEXT_OK : GuiTheme.TEXT_WARN);

        // Aviso de trajetória bloqueada logo abaixo do botão
        if (trajectoryBlocked()) {
            String warn = StatCollector.translateToLocal("gui.orbitalsunworks.launch.blocked");
            this.fontRendererObj.drawString(warn,
                (this.xSize - this.fontRendererObj.getStringWidth(warn)) / 2,
                BTN_Y + BTN_H + 2, GuiTheme.TEXT_WARN);
        }

        // Inventário
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"), 8, 106, GuiTheme.TEXT_TITLE);

        // Rodapé informativo
        String footer = trajectoryBlocked() ? "Desobstrua a vertical do foguete" : "Trajeto vertical livre";
        this.fontRendererObj.drawString(footer,
            (this.xSize - this.fontRendererObj.getStringWidth(footer)) / 2,
            199, trajectoryBlocked() ? GuiTheme.TEXT_WARN : GuiTheme.TEXT_MUTED);
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
        GuiTheme.drawBadge(this.fontRendererObj, x + 150, y + 3,
            "T" + this.tile.getTier(), 0xFF1F2B1A, 0xFF8C7228, GuiTheme.TEXT_GOLD);

        // Painel de módulos
        GuiTheme.drawSubPanel(x + 7, y + 18, this.xSize - 14, 34);
        drawRect(x + 79, y + 39, x + 97, y + 41, GuiTheme.BORDER_SUB);
        GuiTheme.drawSlotBox(x + 61, y + 31);
        GuiTheme.drawSlotBox(x + 97, y + 31);

        // Painel de integridade
        GuiTheme.drawSubPanel(x + 7, y + 54, this.xSize - 14, 21);
        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        GuiTheme.drawProgressBar(x + 11, y + 67, 154, 5, health,
            health > 0.4f ? 0xFF7CFC00 : 0xFFFF5555,
            health > 0.4f ? 0xFF3E9E00 : 0xFFB81E1E);

        // Botão LANÇAR
        boolean blocked = trajectoryBlocked();
        boolean hover = mx >= BTN_X && mx < BTN_X + BTN_W && my >= BTN_Y && my < BTN_Y + BTN_H;
        String launch = StatCollector.translateToLocal("gui.orbitalsunworks.launch");
        GuiTheme.drawButton(this.fontRendererObj, x + BTN_X, y + BTN_Y, BTN_W, BTN_H,
            launch, hover, !blocked, blocked);

        // Inventário 3x9 + hotbar
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                GuiTheme.drawSlotBox(x + 7 + col * 18, y + 117 + row * 18);
            }
        }
        for (int col = 0; col < 9; col++) {
            GuiTheme.drawSlotBox(x + 7 + col * 18, y + 175);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) {
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
            OrbitalSunworks.network.sendToServer(new MessageLaunchRocket(
                this.tile.xCoord, this.tile.yCoord, this.tile.zCoord));
        }
    }
}