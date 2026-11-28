package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.TileControlMonitor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderiza o Monitor Panorâmico de TV fino 2 blocos de altura x 3 blocos de largura
 * montado rente à parede, com radar orbital animado e barras de telemetria.
 */
@SideOnly(Side.CLIENT)
public class TileControlMonitorRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileControlMonitor)) {
            return;
        }
        TileControlMonitor mon = (TileControlMonitor) te;
        if (!mon.isMaster()) {
            return;
        }

        long worldTime = te.getWorldObj() != null ? te.getWorldObj().getTotalWorldTime() : 0L;
        float t = worldTime + partialTicks;

        int f = mon.getFacing();
        float rot = f == 0 ? 0f : (f == 1 ? -90f : (f == 2 ? 180f : 90f));

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(rot, 0f, 1f, 0f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // Suporte de parede + moldura ultra fina 3.0m x 2.0m rente à parede (+Z)
        this.box(-0.90f, 0.35f, 0.44f, 0.90f, 1.55f, 0.50f, 0.14f, 0.18f, 0.24f);
        this.box(-1.44f, 0.06f, 0.34f, 1.44f, 1.94f, 0.45f, 0.12f, 0.16f, 0.22f);
        // Friso metálico ao redor da tela
        this.box(-1.46f, 1.91f, 0.32f, 1.46f, 1.96f, 0.44f, 0.28f, 0.36f, 0.46f);
        this.box(-1.46f, 0.04f, 0.32f, 1.46f, 0.09f, 0.44f, 0.28f, 0.36f, 0.46f);
        this.box(-1.46f, 0.04f, 0.32f, -1.40f, 1.96f, 0.44f, 0.28f, 0.36f, 0.46f);
        this.box(1.40f, 0.04f, 0.32f, 1.46f, 1.96f, 0.44f, 0.28f, 0.36f, 0.46f);

        // Painel OLED escuro da tela (face frontal em z = 0.335 voltada para -Z)
        Tessellator tess = Tessellator.instance;
        float sz = 0.332f;
        tess.startDrawingQuads();
        tess.setColorOpaque_F(0.03f, 0.07f, 0.12f);
        tess.addVertex(-1.38, 0.11, sz);
        tess.addVertex(1.38, 0.11, sz);
        tess.addVertex(1.38, 1.89, sz);
        tess.addVertex(-1.38, 1.89, sz);
        tess.draw();

        // Grade e gráficos animados na tela (z = 0.326)
        float gz = 0.326f;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        // Cabeçalho ciano na tela
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0.12f, 0.78f, 0.98f, 0.55f);
        tess.addVertex(-1.34, 1.78, gz);
        tess.addVertex(1.34, 1.78, gz);
        tess.addVertex(1.34, 1.84, gz);
        tess.addVertex(-1.34, 1.84, gz);
        tess.draw();

        // Radar orbital circular animado à esquerda
        float rcx = -0.68f, rcy = 0.96f, rr = 0.54f;
        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.setColorRGBA_F(0.18f, 0.84f, 1.0f, 0.75f);
        for (int i = 0; i < 32; i++) {
            double a = Math.PI * 2.0 * i / 32.0;
            tess.addVertex(rcx + Math.cos(a) * rr, rcy + Math.sin(a) * rr, gz);
        }
        tess.draw();

        tess.startDrawing(GL11.GL_LINE_LOOP);
        tess.setColorRGBA_F(0.16f, 0.72f, 0.95f, 0.45f);
        for (int i = 0; i < 24; i++) {
            double a = Math.PI * 2.0 * i / 24.0;
            tess.addVertex(rcx + Math.cos(a) * rr * 0.55f, rcy + Math.sin(a) * rr * 0.55f, gz);
        }
        tess.draw();

        // Feixe varredor do radar
        double sweep = (t * 0.06) % (Math.PI * 2.0);
        tess.startDrawing(GL11.GL_TRIANGLES);
        tess.setColorRGBA_F(0.18f, 0.92f, 1.0f, 0.45f);
        tess.addVertex(rcx, rcy, gz);
        tess.setColorRGBA_F(0.10f, 0.75f, 0.95f, 0.0f);
        tess.addVertex(rcx + Math.cos(sweep) * rr, rcy + Math.sin(sweep) * rr, gz);
        tess.addVertex(rcx + Math.cos(sweep - 0.45) * rr, rcy + Math.sin(sweep - 0.45) * rr, gz);
        tess.draw();

        // Globo azul no centro do radar
        tess.startDrawing(GL11.GL_TRIANGLE_FAN);
        tess.setColorRGBA_F(0.16f, 0.72f, 1.0f, 0.72f);
        tess.addVertex(rcx, rcy, gz);
        for (int i = 0; i <= 16; i++) {
            double a = Math.PI * 2.0 * i / 16.0;
            tess.addVertex(rcx + Math.cos(a) * 0.14f, rcy + Math.sin(a) * 0.14f, gz);
        }
        tess.draw();

        // Barras animadas de geração RF à direita
        for (int b = 0; b < 8; b++) {
            float bx0 = 0.08f + b * 0.155f;
            float bx1 = bx0 + 0.10f;
            float bh = 0.24f + 0.38f * (0.5f + 0.5f * (float) Math.sin(t * 0.11f + b * 0.7f));
            tess.startDrawingQuads();
            tess.setColorRGBA_F(0.15f, 0.90f, 0.65f, 0.75f);
            tess.addVertex(bx0, 0.24f, gz);
            tess.addVertex(bx1, 0.24f, gz);
            tess.addVertex(bx1, 0.24f + bh, gz);
            tess.addVertex(bx0, 0.24f + bh, gz);
            tess.draw();
        }

        // Linhas horizontais de status no quadrante superior direito
        for (int ln = 0; ln < 4; ln++) {
            float ly = 1.54f - ln * 0.14f;
            float lw = 0.65f + 0.45f * (0.5f + 0.5f * (float) Math.cos(t * 0.04f + ln));
            tess.startDrawingQuads();
            tess.setColorRGBA_F(0.24f, 0.85f, 1.0f, 0.65f);
            tess.addVertex(0.08f, ly, gz);
            tess.addVertex(0.08f + lw, ly, gz);
            tess.addVertex(0.08f + lw, ly + 0.055f, gz);
            tess.addVertex(0.08f, ly + 0.055f, gz);
            tess.draw();
        }

        // Linha de varredura sutil (scanline)
        float scanY = 0.14f + ((t * 0.018f) % 1.62f);
        tess.startDrawingQuads();
        tess.setColorRGBA_F(0.40f, 0.95f, 1.0f, 0.18f);
        tess.addVertex(-1.36, scanY, gz - 0.002f);
        tess.addVertex(1.36, scanY, gz - 0.002f);
        tess.addVertex(1.36, scanY + 0.03f, gz - 0.002f);
        tess.addVertex(-1.36, scanY + 0.03f, gz - 0.002f);
        tess.draw();

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
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.addVertex(x0, y0, z0); t.addVertex(x1, y0, z0); t.addVertex(x1, y0, z1); t.addVertex(x0, y0, z1);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0);
        t.draw();
    }
}