package com.orbitalsunworks.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.manual.ManualData;

/**
 * GUI do Manual do Orbital Sunworks — livro técnico v2 (200x188).
 *
 * Melhorias da v2:
 * - Caixa maior (200x188) com margens internas generosas: nenhum texto
 *   encosta na borda ou sai da HUD (títulos truncados com "..." se
 *   necessário, textos com quebra de linha limitada à largura útil);
 * - Setas de navegação desenhadas na textura (com hover detectado no
 *   código e realce em overlay);
 * - Rodapé com número de página centralizado;
 * - Páginas de estação mostram o TEMPO de fabricação;
 * - Slots da grade com moldura sutil; tooltips nos ícones.
 */
public class GuiManual extends GuiScreen {

    private static final ResourceLocation TEXTURE = new ResourceLocation(
        "orbitalsunworks", "textures/gui/manual.png");

    private static final int GUI_W = 200;
    private static final int GUI_H = 188;

    /** Margem interna do conteúdo. */
    private static final int PAD = 14;

    private ManualData.Page[] pages;
    private int pageIndex = 0;

    private int guiLeft;
    private int guiTop;

    private static final RenderItem ITEM_RENDER = new RenderItem();

    public GuiManual() {
        super();
        this.pages = ManualData.buildPages();
    }

    @Override
    public void initGui() {
        super.initGui();
        this.guiLeft = (this.width - GUI_W) / 2;
        this.guiTop = (this.height - GUI_H) / 2;
        this.buttonList.clear();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        GL11.glColor4f(1f, 1f, 1f, 1f);
        this.mc.getTextureManager().bindTexture(TEXTURE);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, GUI_W, GUI_H);

        if (this.pageIndex == 0) {
            this.drawIndexPage();
        } else {
            this.drawRecipePage(mouseX, mouseY);
        }

        // Rodapé: número de página centralizado.
        String num = this.pageIndex + " / " + this.pages.length;
        this.fontRendererObj.drawStringWithShadow(num,
            this.guiLeft + (GUI_W - this.fontRendererObj.getStringWidth(num)) / 2,
            this.guiTop + GUI_H - 16, 0x55E0FF);

        this.drawNavArrows(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /** Página 0: capa e índice. */
    private void drawIndexPage() {
        int x = this.guiLeft + PAD;
        int w = GUI_W - PAD * 2;

        // Título (cabe em uma linha; se não couber, trunca).
        this.drawTruncated("ORBITAL SUNWORKS", x, this.guiTop + 14, w, 0xFFD24A, true);
        this.drawTruncated(StatCollector.translateToLocal("manual.os.title"),
            x, this.guiTop + 26, w, 0x9FB4C7, false);

        // Índice.
        int y = this.guiTop + 48;
        this.fontRendererObj.drawString(StatCollector.translateToLocal("manual.os.index"),
            x, y, 0x55E0FF);
        y += 14;
        String[] sections = {
            "manual.os.index.furnace",
            "manual.os.index.vanilla",
            "manual.os.index.station"
        };
        for (String s : sections) {
            this.drawTruncated("- " + StatCollector.translateToLocal(s), x + 4, y, w - 4, 0xD8E4EE, false);
            y += 12;
        }

        // Como usar (texto com quebra garantida na largura útil).
        y += 8;
        List<String> lines = this.fontRendererObj.listFormattedStringToWidth(
            StatCollector.translateToLocal("manual.os.howto"), w);
        for (String line : lines) {
            if (y > this.guiTop + GUI_H - 24) {
                break; // segurança: nunca passa do rodapé
            }
            this.fontRendererObj.drawString(line, x, y, 0x9FB4C7);
            y += 10;
        }
    }

    /** Página de receita: título, tipo, grade, resultado, tempo. */
    private void drawRecipePage(int mouseX, int mouseY) {
        ManualData.Page page = this.pages[this.pageIndex - 1];
        int pad = PAD;
        int w = GUI_W - pad * 2;

        // Título (truncado se preciso) + rótulo do tipo.
        this.drawTruncated(StatCollector.translateToLocal(page.titleKey),
            this.guiLeft + pad, this.guiTop + 12, w, 0xFFD24A, true);

        String typeLabel;
        switch (page.type) {
            case ManualData.TYPE_FURNACE:
                typeLabel = StatCollector.translateToLocal("manual.os.type.furnace");
                break;
            case ManualData.TYPE_VANILLA:
                typeLabel = StatCollector.translateToLocal("manual.os.type.vanilla");
                break;
            default:
                typeLabel = StatCollector.translateToLocal("manual.os.type.station");
        }
        // Tempo de craft na mesma linha do tipo (à direita), se houver.
        this.drawTruncated(typeLabel, this.guiLeft + pad, this.guiTop + 24, w, 0x55E0FF, false);
        if (page.craftSeconds > 0) {
            String time = page.craftSeconds + "s";
            this.fontRendererObj.drawStringWithShadow(time,
                this.guiLeft + GUI_W - pad - this.fontRendererObj.getStringWidth(time),
                this.guiTop + 24, 0xFFD24A);
        }

        if (page.type == ManualData.TYPE_FURNACE) {
            // Fornalha: entrada -> seta -> saída.
            int cx = this.guiLeft + GUI_W / 2 - 44;
            int cy = this.guiTop + 56;
            this.drawSlotIcon(page.grid[6], cx, cy, mouseX, mouseY);
            this.drawArrow(cx + 22, cy + 4);
            this.drawSlotIcon(page.result, cx + 62, cy, mouseX, mouseY);
            this.drawCount(page.resultCount, cx + 62, cy);
            // Nota da fornalha (com quebra garantida).
            this.drawNote(StatCollector.translateToLocal("manual.os.furnace.note"));
            return;
        }

        // Grade 3x5 (ou 3x3 centrada para vanilla).
        int slot = 18;
        int cols = 5;
        int usedCols = page.type == ManualData.TYPE_VANILLA ? 3 : cols;
        int gridW = usedCols * slot;
        int gx = this.guiLeft + (GUI_W - gridW - 34) / 2;
        int gy = this.guiTop + 44;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < cols; col++) {
                int idx = row * cols + col;
                ItemStack stack = page.grid[idx];
                if (page.type == ManualData.TYPE_VANILLA && col >= 3) {
                    continue; // vanilla usa só 3 colunas
                }
                int sx = gx + col * slot;
                int sy = gy + row * slot;
                this.drawSlotIcon(stack, sx, sy, mouseX, mouseY);
            }
        }

        // Seta + resultado à direita.
        int arrowX = gx + gridW + 6;
        this.drawArrow(arrowX, gy + 24);
        this.drawSlotIcon(page.result, arrowX + 22, gy + 16, mouseX, mouseY);
        this.drawCount(page.resultCount, arrowX + 22, gy + 16);

        // Nota fixa de rodapé sobre a estação nas páginas station.
        if (page.type == ManualData.TYPE_STATION) {
            this.drawNote(StatCollector.translateToLocal("manual.os.station.note.short"));
        }
    }

    /** Nota no rodapé da página (com quebra de linha e limite de altura). */
    private void drawNote(String text) {
        int x = this.guiLeft + PAD;
        int w = GUI_W - PAD * 2;
        int y = this.guiTop + 118;
        List<String> lines = this.fontRendererObj.listFormattedStringToWidth(text, w);
        for (String line : lines) {
            if (y > this.guiTop + GUI_H - 22) {
                break;
            }
            this.fontRendererObj.drawString(line, x, y, 0x8FA6BC);
            y += 10;
        }
    }

    /** String truncada com "..." se passar da largura. */
    private void drawTruncated(String s, int x, int y, int maxWidth, int color, boolean shadow) {
        String out = this.fontRendererObj.trimStringToWidth(s, maxWidth);
        if (shadow) {
            this.fontRendererObj.drawStringWithShadow(out, x, y, color);
        } else {
            this.fontRendererObj.drawString(out, x, y, color);
        }
    }

    /** Ícone 16x16 com fundo de slot; tooltip no hover. */
    private void drawSlotIcon(ItemStack stack, int x, int y, int mouseX, int mouseY) {
        this.drawGradientRect(x - 1, y - 1, x + 17, y + 17, 0xFF1A2432, 0xFF1A2432);

        if (stack == null) {
            return;
        }
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        ITEM_RENDER.renderItemAndEffectIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
        ITEM_RENDER.renderItemOverlayIntoGUI(this.fontRendererObj, this.mc.getTextureManager(), stack, x, y);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            @SuppressWarnings("unchecked")
            List<String> lines = stack.getTooltip(this.mc.thePlayer, this.mc.gameSettings.advancedItemTooltips);
            this.drawHoveringText(lines, mouseX, mouseY, this.fontRendererObj);
        }
    }

    private void drawCount(int count, int x, int y) {
        if (count > 1) {
            String s = String.valueOf(count);
            this.fontRendererObj.drawStringWithShadow(s, x + 9, y + 9, 0xFFFFFF);
        }
    }

    /** Seta -> desenhada com retângulos (dentro da página). */
    private void drawArrow(int x, int y) {
        int c = 0xFF00A8FF;
        this.drawGradientRect(x, y + 3, x + 10, y + 6, c, c);
        for (int i = 0; i < 4; i++) {
            this.drawGradientRect(x + 10 + i, y + 3 - i, x + 11 + i, y + 4 + i, c, c);
        }
    }

    /** Setas de navegação (clicáveis), desenhadas com overlay de hover. */
    private void drawNavArrows(int mouseX, int mouseY) {
        int y = this.guiTop + GUI_H - 17;
        int h = 11;
        int w = 13;

        // Anterior (esquerda).
        int px = this.guiLeft + 8;
        if (this.pageIndex > 0) {
            boolean hov = this.isHover(px, y, w, h, mouseX, mouseY);
            int c = hov ? 0xFF80E8FF : 0xFF55E0FF;
            this.drawGradientRect(px + 3, y, px + w, y + h, c, c);
            this.drawGradientRect(px, y + 3, px + 5, y + 8, c, c);
        }
        // Próxima (direita).
        int nx = this.guiLeft + GUI_W - 8 - w;
        if (this.pageIndex < this.pages.length) {
            boolean hov = this.isHover(nx, y, w, h, mouseX, mouseY);
            int c = hov ? 0xFF80E8FF : 0xFF55E0FF;
            this.drawGradientRect(nx, y, nx + w - 3, y + h, c, c);
            this.drawGradientRect(nx + w - 5, y + 3, nx + w, y + 8, c, c);
        }
    }

    private boolean isHover(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int y = this.guiTop + GUI_H - 17;
            if (this.pageIndex > 0 && this.isHover(this.guiLeft + 8, y, 13, 11, mouseX, mouseY)) {
                this.pageIndex--;
                this.playClick(1.4f);
            } else if (this.pageIndex < this.pages.length
                && this.isHover(this.guiLeft + GUI_W - 21, y, 13, 11, mouseX, mouseY)) {
                this.pageIndex++;
                this.playClick(1.2f);
            }
        }
    }

    private void playClick(float pitch) {
        this.mc.theWorld.playSound(this.mc.thePlayer.posX, this.mc.thePlayer.posY,
            this.mc.thePlayer.posZ, "random.click", 0.3f, pitch, false);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == org.lwjgl.input.Keyboard.KEY_LEFT) {
            if (this.pageIndex > 0) this.pageIndex--;
        } else if (keyCode == org.lwjgl.input.Keyboard.KEY_RIGHT) {
            if (this.pageIndex < this.pages.length) this.pageIndex++;
        } else {
            super.keyTyped(c, keyCode);
        }
    }
}
