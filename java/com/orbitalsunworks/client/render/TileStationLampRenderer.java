package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Luminária de teto tecnológica com animação sutil de onda luminosa correndo pelos difusores.
 */
@SideOnly(Side.CLIENT)
public class TileStationLampRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        long worldTime = te.getWorldObj() != null ? te.getWorldObj().getTotalWorldTime() : 0L;
        float t = worldTime + partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // Carcaça metálica de teto
        this.box(-0.42f, 0.89f, -0.34f, 0.42f, 1.00f, 0.34f, 0.16f, 0.21f, 0.28f);
        this.box(-0.38f, 0.85f, -0.30f, 0.38f, 0.90f, 0.30f, 0.24f, 0.30f, 0.38f);

        // 5 barras difusoras com pulso sutil de luz sendo transmitida
        for (int i = 0; i < 5; i++) {
            float bx0 = -0.32f + i * 0.135f;
            float bx1 = bx0 + 0.095f;
            float wave = 0.5f + 0.5f * (float) Math.sin(t * 0.12f - i * 0.85f);
            float r = 0.72f + 0.28f * wave;
            float g = 0.92f + 0.08f * wave;
            float b = 1.0f;
            this.box(bx0, 0.815f, -0.24f, bx1, 0.855f, 0.24f, r, g, b);
        }

        // Halo translúcido suave abaixo da luminária
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);
        float haloPulse = 0.10f + 0.05f * (float) Math.sin(t * 0.09f);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0.65f, 0.94f, 1.0f, haloPulse);
        tess.addVertex(-0.36, 0.80, -0.26);
        tess.addVertex(0.36, 0.80, -0.26);
        tess.addVertex(0.36, 0.80, 0.26);
        tess.addVertex(-0.36, 0.80, 0.26);
        tess.draw();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void box(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_F(r, g, b);
        t.addVertex(x0, y0, z0); t.addVertex(x1, y0, z0); t.addVertex(x1, y0, z1); t.addVertex(x0, y0, z1);
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0);
        t.draw();
    }
}