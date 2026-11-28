package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * MODELO 3D DO FOGUETE DE NAVEGAÇÃO (5 blocos de altura: y = 0.0 .. 5.0).
 */
@SideOnly(Side.CLIENT)
public class ModelNavRocket {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");
    private static final ResourceLocation TEX_PANEL = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_panel.png");

    private static final float LX = 0.42f, LY = 0.82f, LZ = 0.38f;

    public void renderAll() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        // 1. Cluster quádruplo de propulsores (y = 0.00 .. 0.40) + saia
        bind(TEX_DARK);
        float off = 0.19f;
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                float ex = sx * off;
                float ez = sz * off;
                shadedCylinder(0.145f, 0.085f, 0.40f, 12, ex, 0.0f, ez, 0.72f, 0.76f, 0.82f);
                shadedDisc(0.135f, 12, ex, 0.02f, ez, false, 0.14f, 0.15f, 0.18f);
            }
        }
        shadedCylinder(0.54f, 0.46f, 0.44f, 16, 0f, 0.36f, 0f, 0.62f, 0.66f, 0.72f);
        shadedDisc(0.54f, 16, 0f, 0.36f, 0f, false, 0.22f, 0.24f, 0.28f);

        // 2. 4 asas delta de titânio
        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            renderDeltaFin();
            GL11.glPopMatrix();
        }

        // 3. Corpo principal do propulsor (y = 0.80 .. 2.75)
        bind(TEX_BODY);
        shadedCylinder(0.46f, 0.46f, 1.95f, 16, 0f, 0.80f, 0f, 0.86f, 0.91f, 0.96f);

        bind(TEX_DARK);
        shadedCylinder(0.48f, 0.48f, 0.09f, 16, 0f, 0.80f, 0f, 0.48f, 0.52f, 0.58f);
        shadedCylinder(0.48f, 0.48f, 0.08f, 16, 0f, 1.72f, 0f, 0.48f, 0.52f, 0.58f);
        shadedCylinder(0.48f, 0.48f, 0.10f, 16, 0f, 2.65f, 0f, 0.46f, 0.50f, 0.56f);
        for (int s = -1; s <= 1; s += 2) {
            drawBox(s * 0.44f - 0.045f, 0.86f, -0.05f, s * 0.44f + 0.045f, 2.68f, 0.05f,
                0.55f, 0.60f, 0.66f, 0.22f, 0.25f, 0.30f);
        }

        // Faixa ciano de navegação
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        shadedCylinder(0.472f, 0.472f, 0.11f, 16, 0f, 2.48f, 0f, 0.18f, 0.86f, 1.00f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 4. Módulo de serviço (y = 2.75 .. 3.45) com radiadores e propulsores de manobra
        bind(TEX_BODY);
        shadedCylinder(0.46f, 0.54f, 0.70f, 16, 0f, 2.75f, 0f, 0.80f, 0.85f, 0.90f);

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(i * 90f, 0f, 1f, 0f);
            bind(TEX_PANEL);
            drawBox(-0.16f, 2.84f, 0.49f, 0.16f, 3.30f, 0.54f,
                0.58f, 0.72f, 1.00f, 0.24f, 0.30f, 0.46f);
            GL11.glPopMatrix();

            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            bind(TEX_DARK);
            drawBox(-0.08f, 3.22f, 0.46f, 0.08f, 3.52f, 0.63f,
                0.54f, 0.58f, 0.64f, 0.22f, 0.24f, 0.28f);
            GL11.glPopMatrix();
        }

        // 5. Escudo térmico e cápsula tripulada (y = 3.45 .. 4.65)
        bind(TEX_DARK);
        shadedCylinder(0.56f, 0.55f, 0.16f, 16, 0f, 3.45f, 0f, 0.40f, 0.43f, 0.48f);

        bind(TEX_BODY);
        shadedCylinder(0.55f, 0.42f, 0.58f, 16, 0f, 3.61f, 0f, 0.92f, 0.95f, 0.98f);
        shadedCylinder(0.42f, 0.25f, 0.44f, 16, 0f, 4.19f, 0f, 0.90f, 0.93f, 0.97f);

        // Visor panorâmico ciano (+Z)
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_F(0.18f, 0.22f, 0.28f);
        t.addVertex(-0.25f, 3.88f, 0.482f);
        t.addVertex(0.25f, 3.88f, 0.482f);
        t.addVertex(0.19f, 4.27f, 0.388f);
        t.addVertex(-0.19f, 4.27f, 0.388f);
        t.draw();

        t.startDrawingQuads();
        t.setColorOpaque_F(0.16f, 0.85f, 1.00f);
        t.addVertex(-0.22f, 3.92f, 0.492f);
        t.addVertex(0.22f, 3.92f, 0.492f);
        t.addVertex(0.16f, 4.24f, 0.402f);
        t.addVertex(-0.16f, 4.24f, 0.402f);
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 6. Anel de acoplamento & nariz (y = 4.63 .. 5.00)
        bind(TEX_DARK);
        shadedCylinder(0.26f, 0.26f, 0.14f, 16, 0f, 4.63f, 0f, 0.56f, 0.60f, 0.66f);
        bind(TEX_BODY);
        shadedCylinder(0.24f, 0.12f, 0.18f, 16, 0f, 4.77f, 0f, 0.84f, 0.88f, 0.94f);
        shadedDisc(0.12f, 16, 0f, 4.95f, 0f, true, 0.65f, 0.70f, 0.76f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawBox(-0.04f, 4.95f, -0.04f, 0.04f, 5.02f, 0.04f,
            0.25f, 0.92f, 1.00f, 0.15f, 0.65f, 0.80f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private void renderDeltaFin() {
        bind(TEX_DARK);
        Tessellator t = Tessellator.instance;
        float r = 0.42f, w = 0.86f, y0 = 0.22f, y1 = 1.48f, z = 0.04f;

        t.startDrawingQuads();
        t.setColorOpaque_F(0.42f, 0.46f, 0.52f);
        t.addVertexWithUV(r, y0 + 0.18f, z, 0, 0);
        t.addVertexWithUV(w, y0, z, 1, 0);
        t.addVertexWithUV(w, y0 + 0.34f, z, 1, 1);
        t.addVertexWithUV(r, y1, z, 0, 1);

        t.setColorOpaque_F(0.24f, 0.27f, 0.32f);
        t.addVertexWithUV(r, y0 + 0.18f, -z, 0, 0);
        t.addVertexWithUV(r, y1, -z, 0, 1);
        t.addVertexWithUV(w, y0 + 0.34f, -z, 1, 1);
        t.addVertexWithUV(w, y0, -z, 1, 0);

        t.setColorOpaque_F(0.32f, 0.36f, 0.42f);
        t.addVertexWithUV(w, y0, -z, 0, 0);
        t.addVertexWithUV(w, y0, z, 0, 1);
        t.addVertexWithUV(w, y0 + 0.34f, z, 1, 1);
        t.addVertexWithUV(w, y0 + 0.34f, -z, 1, 0);

        t.setColorOpaque_F(0.75f, 0.80f, 0.86f);
        t.addVertexWithUV(r, y1, -z, 0, 0);
        t.addVertexWithUV(r, y1, z, 0, 1);
        t.addVertexWithUV(w, y0 + 0.34f, z, 1, 1);
        t.addVertexWithUV(w, y0 + 0.34f, -z, 1, 0);
        t.draw();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawBox(w - 0.05f, y0 + 0.04f, -z - 0.005f, w + 0.01f, y0 + 0.30f, z + 0.005f,
            0.22f, 0.88f, 1.00f, 0.12f, 0.60f, 0.78f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void shadedCylinder(float rb, float rt, float h, int sides, float ox, float oy, float oz,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2 * i / sides;
            double a1 = Math.PI * 2 * (i + 1) / sides;
            float am = (float) ((a0 + a1) * 0.5);
            float f = 0.42f + 0.58f * Math.max(0f, (float) Math.cos(am) * LX + (float) Math.sin(am) * LZ);
            t.setColorOpaque_F(cr * f, cg * f, cb * f);
            t.addVertexWithUV(ox + Math.cos(a0) * rb, oy, oz + Math.sin(a0) * rb, i / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * rb, oy, oz + Math.sin(a1) * rb, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * rt, oy + h, oz + Math.sin(a1) * rt, (i + 1) / (double) sides, 0);
            t.addVertexWithUV(ox + Math.cos(a0) * rt, oy + h, oz + Math.sin(a0) * rt, i / (double) sides, 0);
        }
        t.draw();
    }

    private void shadedDisc(float r, int sides, float ox, float oy, float oz, boolean up,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque_F(cr, cg, cb);
        t.addVertexWithUV(ox, oy, oz, 0.5, 0.5);
        for (int i = 0; i <= sides; i++) {
            double a = Math.PI * 2 * i / sides * (up ? 1 : -1);
            t.addVertexWithUV(ox + Math.cos(a) * r, oy, oz + Math.sin(a) * r,
                0.5 + Math.cos(a) * 0.5, 0.5 + Math.sin(a) * 0.5);
        }
        t.draw();
    }

    private void drawBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float lr, float lg, float lb, float dr, float dg, float db) {
        Tessellator t = Tessellator.instance;
        float f;

        f = Math.max(0f, LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y1, z1, 0, 1); t.addVertexWithUV(x1, y1, z1, 1, 1);
        t.addVertexWithUV(x1, y1, z0, 1, 0); t.addVertexWithUV(x0, y1, z0, 0, 0);
        t.draw();

        f = Math.max(0f, -LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z0, 0, 1); t.addVertexWithUV(x1, y0, z0, 1, 1);
        t.addVertexWithUV(x1, y0, z1, 1, 0); t.addVertexWithUV(x0, y0, z1, 0, 0);
        t.draw();

        f = Math.max(0f, LX);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x1, y0, z1, 0, 0); t.addVertexWithUV(x1, y0, z0, 1, 0);
        t.addVertexWithUV(x1, y1, z0, 1, 1); t.addVertexWithUV(x1, y1, z1, 0, 1);
        t.draw();

        f = Math.max(0f, -LX);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z0, 0, 0); t.addVertexWithUV(x0, y0, z1, 1, 0);
        t.addVertexWithUV(x0, y1, z1, 1, 1); t.addVertexWithUV(x0, y1, z0, 0, 1);
        t.draw();

        f = Math.max(0f, LZ);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z1, 0, 0); t.addVertexWithUV(x1, y0, z1, 1, 0);
        t.addVertexWithUV(x1, y1, z1, 1, 1); t.addVertexWithUV(x0, y1, z1, 0, 1);
        t.draw();

        f = Math.max(0f, -LZ);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y1, z0, 0, 1); t.addVertexWithUV(x1, y1, z0, 1, 1);
        t.addVertexWithUV(x1, y0, z0, 1, 0); t.addVertexWithUV(x0, y0, z0, 0, 0);
        t.draw();
    }

    private void bind(ResourceLocation tex) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}