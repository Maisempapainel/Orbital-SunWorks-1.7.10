package com.orbitalsunworks.client.gui;

import java.util.List;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.manual.ManualData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Manual aeroespacial dividido em 3 sessões clicáveis:
 *  1. Tutorial (explicação completa de cada sistema/item interagível)
 *  2. Craftings (Fornalha, Mesa 3x3, Estação 3x5 com tempos e tooltips)
 *  3. Multiblocks (diagramas visuais da Plataforma 7x7, Foguetes e Satélite)
 */
@SideOnly(Side.CLIENT)
public class GuiManual extends GuiScreen {

    private static final int GUI_W = 284;
    private static final int GUI_H = 216;
    private static final int LIST_ROWS = 8;
    private static final int LIST_ROW_H = 17;

    private static final RenderItem ITEM_RENDER = new RenderItem();

    private int activeSection = ManualData.SEC_TUTORIAL;
    private List<ManualData.Page> sectionPages;
    private int pageIndex = 0;
    private int listScroll = 0;

    private int guiLeft;
    private int guiTop;

    private ItemStack hoveredTooltipStack;
    private String hoveredCustomTooltip;

    public GuiManual() {
        this.sectionPages = ManualData.buildSection(this.activeSection);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - GUI_W) / 2;
        this.guiTop = (this.height - GUI_H) / 2;
        this.buttonList.clear();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void switchSection(int section) {
        if (this.activeSection == section) {
            return;
        }
        this.activeSection = section;
        this.sectionPages = ManualData.buildSection(section);
        this.pageIndex = 0;
        this.listScroll = 0;
        this.playClick(1.1f);
    }

    private void selectPage(int idx) {
        if (idx < 0 || idx >= this.sectionPages.size()) {
            return;
        }
        this.pageIndex = idx;
        if (this.pageIndex < this.listScroll) {
            this.listScroll = this.pageIndex;
        } else if (this.pageIndex >= this.listScroll + LIST_ROWS) {
            this.listScroll = this.pageIndex - LIST_ROWS + 1;
        }
        this.playClick(1.25f);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.hoveredTooltipStack = null;
        this.hoveredCustomTooltip = null;

        this.drawDefaultBackground();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        int x0 = this.guiLeft;
        int y0 = this.guiTop;

        // Painel principal + cabeçalho
        GuiTheme.drawAerospacePanel(x0, y0, GUI_W, GUI_H);
        GuiTheme.drawHeaderBar(x0 + 2, y0 + 2, GUI_W - 4, 18);

        String header = "ORBITAL SUNWORKS  \u2022  MANUAL T\u00c9CNICO";
        this.fontRendererObj.drawStringWithShadow(header, x0 + 10, y0 + 7, GuiTheme.TEXT_TITLE);

        // 3 abas de sessão no topo
        String[] tabNames = { "1. TUTORIAL", "2. CRAFTINGS", "3. MULTIBLOCKS" };
        int tabX = x0 + 8;
        int tabY = y0 + 23;
        int tabW = 87;
        int tabH = 16;
        for (int t = 0; t < 3; t++) {
            int tx = tabX + t * (tabW + 3);
            boolean active = (this.activeSection == t);
            boolean hov = inBox(mouseX, mouseY, tx, tabY, tabW, tabH);
            int border = active ? GuiTheme.ACCENT_CYAN : (hov ? 0xFF3E84AD : GuiTheme.BORDER_SUB);
            int bgTop = active ? 0xFF1B577B : (hov ? 0xFF15324A : 0xFF0D1724);
            int bgBot = active ? 0xFF123B54 : 0xFF0A111B;
            drawRect(tx, tabY, tx + tabW, tabY + tabH, border);
            drawRect(tx + 1, tabY + 1, tx + tabW - 1, tabY + tabH / 2, bgTop);
            drawRect(tx + 1, tabY + tabH / 2, tx + tabW - 1, tabY + tabH - (active ? 0 : 1), bgBot);
            int lw = this.fontRendererObj.getStringWidth(tabNames[t]);
            this.fontRendererObj.drawStringWithShadow(tabNames[t],
                tx + (tabW - lw) / 2, tabY + 4,
                active ? 0xFFFFFF : (hov ? 0xB8EEFF : GuiTheme.TEXT_LABEL));
        }

        // Índice lateral esquerdo
        int sideX = x0 + 8;
        int sideY = y0 + 42;
        int sideW = 96;
        int sideH = 148;
        GuiTheme.drawSubPanel(sideX, sideY, sideW, sideH);

        int total = this.sectionPages.size();
        int maxScroll = Math.max(0, total - LIST_ROWS);
        if (this.listScroll > maxScroll) {
            this.listScroll = maxScroll;
        }

        int visible = Math.min(LIST_ROWS, total - this.listScroll);
        for (int i = 0; i < visible; i++) {
            int idx = this.listScroll + i;
            ManualData.Page pg = this.sectionPages.get(idx);
            int ry = sideY + 4 + i * LIST_ROW_H;
            int rx = sideX + 3;
            int rw = sideW - 14;
            int rh = LIST_ROW_H - 2;
            boolean sel = (idx == this.pageIndex);
            boolean hov = inBox(mouseX, mouseY, rx, ry, rw, rh);

            if (sel) {
                drawRect(rx, ry, rx + rw, ry + rh, 0xFF194B6B);
                drawRect(rx, ry, rx + 2, ry + rh, GuiTheme.ACCENT_CYAN);
            } else if (hov) {
                drawRect(rx, ry, rx + rw, ry + rh, 0xFF142436);
            }

            String label = this.fontRendererObj.trimStringToWidth(pg.title, rw - 6);
            this.fontRendererObj.drawString(label, rx + 5, ry + 3,
                sel ? 0xFFFFFF : (hov ? 0xBBEFFF : GuiTheme.TEXT_LABEL));
        }

        // Setas de rolagem da lista lateral
        int sbX = sideX + sideW - 10;
        boolean hovUp = inBox(mouseX, mouseY, sbX, sideY + 4, 8, 12);
        boolean hovDn = inBox(mouseX, mouseY, sbX, sideY + sideH - 16, 8, 12);
        drawRect(sbX, sideY + 4, sbX + 8, sideY + sideH - 4, 0xFF0A1019);
        this.fontRendererObj.drawString("\u25b2", sbX + 1, sideY + 6,
            this.listScroll > 0 ? (hovUp ? 0xFFFFFF : 0x55E0FF) : 0x2F4052);
        this.fontRendererObj.drawString("\u25bc", sbX + 1, sideY + sideH - 14,
            this.listScroll < maxScroll ? (hovDn ? 0xFFFFFF : 0x55E0FF) : 0x2F4052);

        if (total > LIST_ROWS) {
            int trackH = sideH - 36;
            int thumbH = Math.max(12, trackH * LIST_ROWS / total);
            int thumbY = sideY + 18 + (maxScroll > 0 ? (trackH - thumbH) * this.listScroll / maxScroll : 0);
            drawRect(sbX + 2, thumbY, sbX + 6, thumbY + thumbH, GuiTheme.ACCENT_DIM);
        }

        // Painel principal direito
        int viewX = x0 + 109;
        int viewY = y0 + 42;
        int viewW = 167;
        int viewH = 148;
        GuiTheme.drawSubPanel(viewX, viewY, viewW, viewH);

        if (!this.sectionPages.isEmpty()) {
            ManualData.Page cur = this.sectionPages.get(this.pageIndex);
            this.drawContentPage(cur, viewX, viewY, viewW, viewH, mouseX, mouseY);
        }

        // Barra de rodapé com Anterior / Contador / Próxima
        int footY = y0 + GUI_H - 22;
        boolean hovPrev = this.pageIndex > 0 && inBox(mouseX, mouseY, x0 + 8, footY, 64, 15);
        boolean hovNext = this.pageIndex < total - 1 && inBox(mouseX, mouseY, x0 + GUI_W - 72, footY, 64, 15);
        GuiTheme.drawButton(this.fontRendererObj, x0 + 8, footY, 64, 15, "< Anterior", hovPrev, this.pageIndex > 0, false);
        GuiTheme.drawButton(this.fontRendererObj, x0 + GUI_W - 72, footY, 64, 15, "Pr\u00f3xima >", hovNext, this.pageIndex < total - 1, false);

        String pageCounter = "P\u00e1g. " + (this.pageIndex + 1) + " / " + Math.max(1, total);
        this.fontRendererObj.drawString(pageCounter,
            x0 + (GUI_W - this.fontRendererObj.getStringWidth(pageCounter)) / 2,
            footY + 4, GuiTheme.TEXT_LABEL);

        // Tooltips sempre desenhados por último (acima de todos os ícones)
        GL11.glDisable(GL11.GL_LIGHTING);
        if (this.hoveredTooltipStack != null) {
            @SuppressWarnings("unchecked")
            List<String> lines = this.hoveredTooltipStack.getTooltip(
                this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
            this.drawHoveringText(lines, mouseX, mouseY, this.fontRendererObj);
        } else if (this.hoveredCustomTooltip != null) {
            this.drawHoveringText(java.util.Collections.singletonList(this.hoveredCustomTooltip),
                mouseX, mouseY, this.fontRendererObj);
        }
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawContentPage(ManualData.Page page, int vx, int vy, int vw, int vh, int mouseX, int mouseY) {
        // Cabeçalho interno do tópico
        drawRect(vx + 1, vy + 1, vx + vw - 1, vy + 22, 0xFF112033);
        drawRect(vx + 1, vy + 22, vx + vw - 1, vy + 23, GuiTheme.BORDER_SUB);

        String title = this.fontRendererObj.trimStringToWidth(page.title, vw - 14);
        this.fontRendererObj.drawStringWithShadow(title, vx + 7, vy + 5, 0xFFFFFF);

        GuiTheme.drawBadge(this.fontRendererObj, vx + 7, vy + 26, page.badge,
            0xFF14354D, GuiTheme.BORDER_MAIN, GuiTheme.TEXT_TITLE);

        if (page.type == ManualData.TYPE_STATION && page.craftSeconds > 0) {
            String timeTxt = page.craftSeconds + "s";
            GuiTheme.drawBadge(this.fontRendererObj,
                vx + vw - 16 - this.fontRendererObj.getStringWidth(timeTxt), vy + 26,
                timeTxt, 0xFF2B2310, 0xFFD99B2B, GuiTheme.TEXT_GOLD);
        } else if (page.shapeless) {
            String sh = "LIVRE";
            GuiTheme.drawBadge(this.fontRendererObj,
                vx + vw - 16 - this.fontRendererObj.getStringWidth(sh), vy + 26,
                sh, 0xFF183022, 0xFF42A868, GuiTheme.TEXT_OK);
        }

        if (page.type == ManualData.TYPE_GUIDE) {
            if (page.result != null) {
                int iconX = vx + vw - 28;
                int iconY = vy + 25;
                GuiTheme.drawSlotBox(iconX - 1, iconY - 1);
                drawItemIcon(page.result, iconX, iconY, 1, mouseX, mouseY);
            }
            drawWrappedText(page.description, vx + 7, vy + 45, vw - 14, vy + vh - 6, GuiTheme.TEXT_BODY);
            return;
        }

        if (page.type == ManualData.TYPE_FURNACE) {
            int cx = vx + vw / 2;
            int iy = vy + 46;
            GuiTheme.drawSlotBox(cx - 46, iy - 1);
            drawItemIcon(page.grid[0], cx - 45, iy, 1, mouseX, mouseY);

            GuiTheme.drawSlotBox(cx - 46, iy + 27);
            drawItemIcon(new ItemStack(Items.coal), cx - 45, iy + 28, 1, mouseX, mouseY);

            // Chama da fornalha
            drawRect(cx - 41, iy + 18, cx - 33, iy + 25, 0xFFFF8822);
            drawRect(cx - 39, iy + 19, cx - 35, iy + 24, 0xFFFFE577);

            drawArrow(cx - 16, iy + 14);

            GuiTheme.drawSlotBox(cx + 18, iy + 8, GuiTheme.ACCENT_CYAN, 0xFF183044);
            drawItemIcon(page.result, cx + 19, iy + 9, page.resultCount, mouseX, mouseY);

            drawWrappedText(page.description, vx + 7, vy + 98, vw - 14, vy + vh - 6, GuiTheme.TEXT_LABEL);
            return;
        }

        if (page.type == ManualData.TYPE_VANILLA || page.type == ManualData.TYPE_STATION) {
            int cols = (page.type == ManualData.TYPE_VANILLA) ? 3 : 5;
            int gridW = cols * 18;
            int gx = vx + (vw - (gridW + 44)) / 2;
            int gy = vy + 43;

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < cols; col++) {
                    int sx = gx + col * 18;
                    int sy = gy + row * 18;
                    GuiTheme.drawSlotBox(sx, sy);
                    drawItemIcon(page.grid[row * 5 + col], sx + 1, sy + 1, 1, mouseX, mouseY);
                }
            }

            int arrowX = gx + gridW + 5;
            drawArrow(arrowX, gy + 23);

            int outX = arrowX + 21;
            int outY = gy + 18;
            GuiTheme.drawSlotBox(outX, outY, GuiTheme.ACCENT_CYAN, 0xFF183044);
            drawItemIcon(page.result, outX + 1, outY + 1, page.resultCount, mouseX, mouseY);

            drawWrappedText(page.description, vx + 7, gy + 59, vw - 14, vy + vh - 5, GuiTheme.TEXT_LABEL);
            return;
        }

        if (page.type == ManualData.TYPE_MULTIBLOCK) {
            drawMultiblockDiagram(page, vx, vy + 41, vw, mouseX, mouseY);
            drawWrappedText(page.description, vx + 7, vy + 119, vw - 14, vy + vh - 5, GuiTheme.TEXT_BODY);
        }
    }

    private void drawMultiblockDiagram(ManualData.Page page, int vx, int topY, int vw, int mouseX, int mouseY) {
        if (page.multiblockId == ManualData.MB_PLATFORM_7X7) {
            int cell = 10;
            int size = 7 * cell;
            int bx = vx + (vw - size) / 2;
            int by = topY + 2;
            drawRect(bx - 4, by - 4, bx + size + 4, by + size + 4, 0xFF0A1420);
            for (int r = 0; r < 7; r++) {
                for (int c = 0; c < 7; c++) {
                    int cx = bx + c * cell;
                    int cy = by + r * cell;
                    boolean center = (r == 3 && c == 3);
                    drawRect(cx, cy, cx + cell - 1, cy + cell - 1,
                        center ? 0xFFFFD045 : 0xFF34739E);
                    if (center) {
                        drawRect(cx + 2, cy + 2, cx + cell - 3, cy + cell - 3, 0xFF3BF0FF);
                    }
                    if (inBox(mouseX, mouseY, cx, cy, cell, cell)) {
                        this.hoveredCustomTooltip = center
                            ? "\u00a7bCentro da Plataforma (clique aqui c/ foguete)"
                            : "\u00a77Plataforma de Lan\u00e7amento (49 blocos)";
                    }
                }
            }
            if (page.result != null) {
                GuiTheme.drawSlotBox(vx + 7, topY + 2);
                drawItemIcon(page.result, vx + 8, topY + 3, 49, mouseX, mouseY);
            }
            return;
        }

        if (page.multiblockId == ManualData.MB_ROCKET_T1 || page.multiblockId == ManualData.MB_NAV_ROCKET) {
            int h = (page.multiblockId == ManualData.MB_NAV_ROCKET) ? 8 : 6;
            int segH = (h == 8) ? 7 : 9;
            int baseW = 54;
            int bx = vx + (vw - baseW) / 2;
            int baseY = topY + 66;
            drawRect(bx, baseY, bx + baseW, baseY + 6, 0xFF34739E);
            drawRect(bx + baseW / 2 - 4, baseY, bx + baseW / 2 + 4, baseY + 6, 0xFF55E0FF);

            int rx = vx + vw / 2 - 5;
            for (int i = 0; i < h; i++) {
                int sy = baseY - (i + 1) * segH;
                int col = (i == h - 1) ? 0xFFFFD045 : ((i % 2 == 0) ? 0xFFC5D5E8 : 0xFF8CA5C0);
                drawRect(rx, sy + 1, rx + 10, sy + segH, col);
            }
            if (inBox(mouseX, mouseY, rx - 4, baseY - h * segH, 18, h * segH)) {
                this.hoveredCustomTooltip = "\u00a7bColuna de " + h + " blocos no centro da 7x7";
            }
            if (page.result != null) {
                GuiTheme.drawSlotBox(vx + 10, topY + 26);
                drawItemIcon(page.result, vx + 11, topY + 27, 1, mouseX, mouseY);
            }
            return;
        }

        if (page.multiblockId == ManualData.MB_ORBIT_SAT) {
            int cx = vx + vw / 2;
            int cy = topY + 34;
            // Asas solares giratórias
            drawRect(cx - 46, cy - 5, cx - 10, cy + 5, 0xFF1F5FA8);
            drawRect(cx + 10, cy - 5, cx + 46, cy + 5, 0xFF1F5FA8);
            // Núcleo clicável 1x3
            for (int i = -1; i <= 1; i++) {
                drawRect(cx - 6, cy - 6 + i * 11, cx + 6, cy + 4 + i * 11,
                    i == 0 ? 0xFFFFCC44 : 0xFF9EB4CC);
            }
            if (inBox(mouseX, mouseY, cx - 8, cy - 18, 16, 36)) {
                this.hoveredCustomTooltip = "\u00a7bColuna central 1x3 clic\u00e1vel (abre painel de reparo)";
            }
            if (page.result != null) {
                GuiTheme.drawSlotBox(vx + 8, topY + 46);
                drawItemIcon(page.result, vx + 9, topY + 47, 1, mouseX, mouseY);
            }
        }
    }

    private void drawWrappedText(String text, int x, int y, int maxW, int maxY, int color) {
        if (text == null || text.isEmpty()) {
            return;
        }
        String[] paragraphs = text.split("\n");
        int curY = y;
        for (String p : paragraphs) {
            if (p.isEmpty()) {
                curY += 5;
                continue;
            }
            @SuppressWarnings("unchecked")
            List<String> lines = this.fontRendererObj.listFormattedStringToWidth(p, maxW);
            for (String line : lines) {
                if (curY + 8 > maxY) {
                    return;
                }
                this.fontRendererObj.drawString(line, x, curY, color);
                curY += 9;
            }
        }
    }

    private void drawItemIcon(ItemStack stack, int x, int y, int count, int mouseX, int mouseY) {
        if (stack == null) {
            return;
        }
        ItemStack renderStack = stack.copy();
        renderStack.stackSize = Math.max(1, count);

        GL11.glPushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        ITEM_RENDER.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), renderStack, x, y);
        ITEM_RENDER.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), renderStack, x, y,
            count > 1 ? String.valueOf(count) : null);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();

        if (inBox(mouseX, mouseY, x - 1, y - 1, 18, 18)) {
            this.hoveredTooltipStack = renderStack;
        }
    }

    private void drawArrow(int x, int y) {
        int c = GuiTheme.ACCENT_CYAN;
        drawRect(x, y + 3, x + 11, y + 6, c);
        for (int i = 0; i < 4; i++) {
            drawRect(x + 11 + i, y + 1 + i, x + 12 + i, y + 8 - i, c);
        }
    }

    private static boolean inBox(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button != 0) {
            return;
        }
        int x0 = this.guiLeft;
        int y0 = this.guiTop;

        // Clique nas 3 abas
        int tabX = x0 + 8;
        int tabY = y0 + 23;
        int tabW = 87;
        for (int t = 0; t < 3; t++) {
            int tx = tabX + t * (tabW + 3);
            if (inBox(mouseX, mouseY, tx, tabY, tabW, 16)) {
                this.switchSection(t);
                return;
            }
        }

        // Clique no índice lateral
        int sideX = x0 + 8;
        int sideY = y0 + 42;
        int sideW = 96;
        int sideH = 148;
        int total = this.sectionPages.size();
        int maxScroll = Math.max(0, total - LIST_ROWS);
        int sbX = sideX + sideW - 10;

        if (inBox(mouseX, mouseY, sbX, sideY + 4, 8, 12)) {
            if (this.listScroll > 0) {
                this.listScroll--;
                this.playClick(1.0f);
            }
            return;
        }
        if (inBox(mouseX, mouseY, sbX, sideY + sideH - 16, 8, 12)) {
            if (this.listScroll < maxScroll) {
                this.listScroll++;
                this.playClick(1.0f);
            }
            return;
        }

        int visible = Math.min(LIST_ROWS, total - this.listScroll);
        for (int i = 0; i < visible; i++) {
            int ry = sideY + 4 + i * LIST_ROW_H;
            if (inBox(mouseX, mouseY, sideX + 3, ry, sideW - 14, LIST_ROW_H - 2)) {
                this.selectPage(this.listScroll + i);
                return;
            }
        }

        // Botões Anterior / Próxima
        int footY = y0 + GUI_H - 22;
        if (this.pageIndex > 0 && inBox(mouseX, mouseY, x0 + 8, footY, 64, 15)) {
            this.selectPage(this.pageIndex - 1);
        } else if (this.pageIndex < total - 1 && inBox(mouseX, mouseY, x0 + GUI_W - 72, footY, 64, 15)) {
            this.selectPage(this.pageIndex + 1);
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int d = Mouse.getEventDWheel();
        if (d == 0) {
            return;
        }
        int total = this.sectionPages.size();
        int maxScroll = Math.max(0, total - LIST_ROWS);
        if (d > 0) {
            if (this.listScroll > 0) {
                this.listScroll--;
            } else if (this.pageIndex > 0) {
                this.selectPage(this.pageIndex - 1);
            }
        } else {
            if (this.listScroll < maxScroll) {
                this.listScroll++;
            } else if (this.pageIndex < total - 1) {
                this.selectPage(this.pageIndex + 1);
            }
        }
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == Keyboard.KEY_LEFT) {
            this.selectPage(this.pageIndex - 1);
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            this.selectPage(this.pageIndex + 1);
        } else if (keyCode == Keyboard.KEY_1) {
            this.switchSection(ManualData.SEC_TUTORIAL);
        } else if (keyCode == Keyboard.KEY_2) {
            this.switchSection(ManualData.SEC_CRAFTINGS);
        } else if (keyCode == Keyboard.KEY_3) {
            this.switchSection(ManualData.SEC_MULTIBLOCKS);
        } else {
            super.keyTyped(c, keyCode);
        }
    }

    private void playClick(float pitch) {
        this.mc.getSoundHandler().playSound(
            PositionedSoundRecord.func_147674_a(new ResourceLocation("random.click"), pitch));
    }
}