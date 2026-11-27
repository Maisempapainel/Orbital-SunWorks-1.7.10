package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TESR da ESTRUTURA DO SATÉLITE na órbita (v0.8.0 — turbinado).
 *
 * O multiblock físico (5 blocos de largura, 4 de altura) é só a HITBOX;
 * a imagem é o ModelSatellite em escala 2.2x com os painéis ABERTOS e
 * giro LENTO em Y. Novidades v0.8.0:
 *  - BRILHO nos painéis solares (células captando sol — quad aditivo
 *    azul por cima das placas, pulsando devagar);
 *  - luz de navegação VERMELHA piscando no topo (mais brilhante);
 *  - LUZ de janela no corpo (quad amarelado aditivo num dos lados).
 */
@SideOnly(Side.CLIENT)
public class TileOrbitSatelliteRenderer extends TileEntitySpecialRenderer {

    private final ModelSatellite model = new ModelSatellite();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileOrbitSatellite)) {
            return;
        }
        int meta = te.getWorldObj().getBlockMetadata(te.xCoord, te.yCoord, te.zCoord);
        if (meta != 0) {
            return; // só o âncora desenha
        }

        GL11.glPushMatrix();
        // Âncora no centro do bloco; modelo tem ~1.67 de altura * 2.2 de
        // escala = ~3.7 blocos (bate com o multiblock de 4 de altura).
        GL11.glTranslated(x + 0.5, y - 0.6, z + 0.5);

        // Giro lento (vida na órbita).
        long time = te.getWorldObj().getTotalWorldTime();
        float spin = (time + partialTicks) * 0.05f;

        GL11.glScalef(2.2f, 2.2f, 2.2f);
        // Painéis SEMPRE abertos na órbita (pose de operação).
        this.model.renderAll(1.0f, spin);

        // ---- Brilho dos painéis (varre devagar como "captando sol") ----
        float glimmer = 0.5f + 0.5f * (float) Math.sin((time + partialTicks) * 0.03f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // aditivo
        Tessellator t = Tessellator.instance;
        // Painéis (v0.10.0: HORIZONTAIS — a sanfona estica ao longo de
        // ±X, faixa 0.76..3.28 do centro, altura fixa Y=0.62).
        for (int side = -1; side <= 1; side += 2) {
            float px = side * 2.02f; // centro do enxame (braço + 3 segs)
            float w = 1.26f, d = 0.26f; // metade do comprimento/largura
            float py = 0.62f;           // altura do braço (horizontal)
            t.startDrawingQuads();
            t.setColorRGBA_F(0.25f, 0.45f, 0.85f, 0.18f + 0.14f * glimmer);
            t.addVertex(px - w, py, -d);
            t.addVertex(px - w, py, d);
            t.addVertex(px + w, py, d);
            t.addVertex(px + w, py, -d);
            t.draw();
        }

        // Luz de janela no corpo (quad amarelado no lado +X).
        t.startDrawingQuads();
        t.setColorRGBA_F(1.0f, 0.85f, 0.45f, 0.7f);
        t.addVertex(0.47f, 0.35f, -0.18f);
        t.addVertex(0.47f, 0.35f, 0.18f);
        t.addVertex(0.47f, 0.62f, 0.18f);
        t.addVertex(0.47f, 0.62f, -0.18f);
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);

        // Luz de navegação piscando no topo da antena (vermelha).
        float blink = ((time + partialTicks) % 40f) < 8f ? 1f : 0.15f;
        t.startDrawingQuads();
        t.setColorRGBA_F(1f, 0.1f, 0.1f, blink);
        t.addVertex(-0.06f, 2.35f, -0.06f);
        t.addVertex(-0.06f, 2.35f, 0.06f);
        t.addVertex(0.06f, 2.35f, 0.06f);
        t.addVertex(0.06f, 2.35f, -0.06f);
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }
}
