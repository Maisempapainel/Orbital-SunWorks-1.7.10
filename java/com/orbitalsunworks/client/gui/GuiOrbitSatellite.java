package com.orbitalsunworks.client.gui;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.item.ItemModule;
import com.orbitalsunworks.network.MessageEjectModule;
import com.orbitalsunworks.satellite.ContainerOrbitSatellite;
import com.orbitalsunworks.satellite.TileOrbitSatellite;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * HUD do Satélite na órbita (176x214).
 * Permite instalar módulos (Escudo T1-T3, Eficiência T1-T3),
 * ejetar módulos instalados clicando neles e reparar +10% com Kit de Reparo.
 */
@SideOnly(Side.CLIENT)
public class GuiOrbitSatellite extends GuiContainer {

    private static final int CHIP_Y = 66;
    private static final int CHIP_W = 39;
    private static final int CHIP_H = 10;
    private static final int CHIP_X0 = 11;
    private static final int CHIP_X1 = 52;

    private final TileOrbitSatellite tile;

    public GuiOrbitSatellite(InventoryPlayer playerInv, TileOrbitSatellite tile) {
        super(new ContainerOrbitSatellite(playerInv, tile));
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
        if (this.tile == null) {
            return;
        }
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;

        String name = GuiTheme.truncate(this.fontRendererObj, this.tile.getSatelliteName(), 125);
        this.fontRendererObj.drawStringWithShadow(name, 8, 5, GuiTheme.TEXT_TITLE);

        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        String pct = Math.round(health * 100) + "%";
        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.sat.health"),
            12, 21, GuiTheme.TEXT_LABEL);
        this.fontRendererObj.drawString(pct,
            164 - this.fontRendererObj.getStringWidth(pct), 21,
            health > 0.4f ? GuiTheme.TEXT_OK : GuiTheme.TEXT_WARN);

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("gui.orbitalsunworks.sat.generation"),
            12, 42, GuiTheme.TEXT_LABEL);
        int effPct = this.tile.getEfficiencyBonusPercent();
        String gen = OSUtils.formatRF(this.tile.getGenerationRFt()) + "/t"
            + (effPct > 0 ? " (+" + effPct + "%)" : "");
        this.fontRendererObj.drawStringWithShadow(gen,
            164 - this.fontRendererObj.getStringWidth(gen), 42, GuiTheme.TEXT_OK);

        this.fontRendererObj.drawString("MODULOS", 11, 57, GuiTheme.TEXT_LABEL);
        int shieldTier = this.tile.getShieldTier();
        String shieldTxt = "ESC: " + (shieldTier > 0 ? "T" + shieldTier : "OFF");
        this.fontRendererObj.drawString(shieldTxt,
            165 - this.fontRendererObj.getStringWidth(shieldTxt), 57,
            shieldTier > 0 ? 0x00E8FF : GuiTheme.TEXT_WARN);

        List<String> mods = this.tile.getInstalledModules();
        for (int i = 0; i < 2; i++) {
            int cx = i == 0 ? CHIP_X0 : CHIP_X1;
            if (i < mods.size()) {
                String lbl = ItemModule.getShortLabel(mods.get(i));
                String text = GuiTheme.truncate(this.fontRendererObj, lbl, CHIP_W - 8);
                this.fontRendererObj.drawString(text, cx + 2, CHIP_Y + 1, 0xFFE6A0);
                this.fontRendererObj.drawString("x", cx + CHIP_W - 6, CHIP_Y + 1, 0xFF7070);
            } else {
                String empty = "Vazio";
                this.fontRendererObj.drawString(empty,
                    cx + (CHIP_W - this.fontRendererObj.getStringWidth(empty)) / 2,
                    CHIP_Y + 1, GuiTheme.TEXT_MUTED);
            }
        }

        String repLabel = "REPARO +10%";
        this.fontRendererObj.drawString(repLabel,
            132 - this.fontRendererObj.getStringWidth(repLabel) / 2, 67, GuiTheme.TEXT_GOLD);

        this.fontRendererObj.drawString(
            StatCollector.translateToLocal("container.inventory"), 8, 106, GuiTheme.TEXT_TITLE);

        String hint = "Coloque Kit de Reparo p/ +10% | Clique no mod p/ ejetar";
        hint = GuiTheme.truncate(this.fontRendererObj, hint, 162);
        this.fontRendererObj.drawString(hint,
            (this.xSize - this.fontRendererObj.getStringWidth(hint)) / 2, 199, GuiTheme.TEXT_MUTED);

        // Tooltip de ejeção
        for (int i = 0; i < Math.min(2, mods.size()); i++) {
            int cx = i == 0 ? CHIP_X0 : CHIP_X1;
            if (mx >= cx && mx < cx + CHIP_W && my >= CHIP_Y && my < CHIP_Y + CHIP_H) {
                String full = ItemModule.getDisplayName(mods.get(i)) + " (clique p/ remover)";
                this.drawHoveringText(Collections.singletonList(full), mx, my, this.fontRendererObj);
            }
        }
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
        GuiTheme.drawHeaderBar(x + 3, y + 2, this.xSize - 6, 14);
        GuiTheme.drawBadge(this.fontRendererObj, x + 150, y + 3,
            "T" + this.tile.getTier(), 0xFF1F2B1A, 0xFF8C7228, GuiTheme.TEXT_GOLD);

        GuiTheme.drawSubPanel(x + 7, y + 18, this.xSize - 14, 35);
        float health = Math.max(0f, Math.min(1f, this.tile.getHealth()));
        GuiTheme.drawProgressBar(x + 11, y + 31, 154, 6, health,
            health > 0.4f ? 0xFF7CFC00 : 0xFFFF5555,
            health > 0.4f ? 0xFF3E9E00 : 0xFFB81E1E);

        GuiTheme.drawSubPanel(x + 7, y + 55, this.xSize - 14, 47);
        drawRect(x + 93, y + 66, x + 94, y + 99, GuiTheme.BORDER_SUB);

        List<String> mods = this.tile.getInstalledModules();
        for (int i = 0; i < 2; i++) {
            int cx = i == 0 ? CHIP_X0 : CHIP_X1;
            boolean has = i < mods.size();
            boolean hover = has && mx >= cx && mx < cx + CHIP_W && my >= CHIP_Y && my < CHIP_Y + CHIP_H;
            int border = has ? (hover ? 0xFFFF7070 : 0xFF367A9E) : 0xFF1A2838;
            int fill = has ? (hover ? 0xFF3A1A1E : 0xFF132638) : 0xFF0A111A;
            drawRect(x + cx, y + CHIP_Y, x + cx + CHIP_W, y + CHIP_Y + CHIP_H, border);
            drawRect(x + cx + 1, y + CHIP_Y + 1, x + cx + CHIP_W - 1, y + CHIP_Y + CHIP_H - 1, fill);
        }

        GuiTheme.drawSlotBox(x + 21, y + 78);
        GuiTheme.drawSlotBox(x + 47, y + 78);
        GuiTheme.drawSlotBox(x + 123, y + 78, 0xFF8C7228, 0xFF1A1B14);

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
        if (button != 0 || this.tile == null) {
            return;
        }
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        int mx = mouseX - x;
        int my = mouseY - y;
        List<String> mods = this.tile.getInstalledModules();
        for (int i = 0; i < Math.min(2, mods.size()); i++) {
            int cx = i == 0 ? CHIP_X0 : CHIP_X1;
            if (mx >= cx && mx < cx + CHIP_W && my >= CHIP_Y && my < CHIP_Y + CHIP_H) {
                this.mc.getSoundHandler().playSound(
                    PositionedSoundRecord.func_147674_a(new ResourceLocation("random.click"), 1.0F));
                OrbitalSunworks.network.sendToServer(
                    new MessageEjectModule(this.tile.xCoord, this.tile.yCoord, this.tile.zCoord, i));
                return;
            }
        }
    }
}