package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Modelo 3D esguio do Foguete Dispersor de Meteoros (interceptador orbital, 5.0 unidades locais).
 */
@SideOnly(Side.CLIENT)
public class ModelDispersionRocket {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");

    private static final float LX = 0.42f, LY = 0.82f, LZ = 0.38f;

    public void renderAll() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        // 1. Propulsor central + 4 micro-nozzles estabilizadores
        bind(TEX_DARK);
        shadedCylinder(0.28f, 0.22f, 0.46f, 16, 0f, 0.00f, 0f, 0.62f, 0.65f, 0.70f);
        shadedDisc(0.27f, 16, 0f, 0.02f, 0f, false, 0.14f, 0.15f, 0.18f);
        for (int i = 0; i < 4; i++) {
            double a = Math.PI * 0.25 + i * Math.PI * 0.5;
            float nx = (float) Math.cos(a) * 0.26f;
            float nz = (float) Math.sin(a) * 0.26f;
            shadedCylinder(0.11f, 0.08f, 0.36f, 10, nx, 0.02f, nz, 0.70f, 0.74f, 0.80f);
        }

        // 2. Base cônica + 4 aletas interceptoras com borda âmbar
        shadedCylinder(0.46f, 0.36f, 0.42f, 16, 0f, 0.38f, 0f, 0.42f, 0.46f, 0.52f);
        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            renderInterceptorFin();
            GL11.glPopMatrix();
        }

        // 3. Fuselagem principal aerodinâmica (cinza titânio + listras táticas laranja/ciano)
        bind(TEX_BODY);
        shadedCylinder(0.36f, 0.35f, 2.15f, 16, 0f, 0.80f, 0f, 0.82f, 0.86f, 0.90f);

        bind(TEX_DARK);
        shadedCylinder(0.38f, 0.38f, 0.09f, 16, 0f, 0.82f, 0f, 0.35f, 0.38f, 0.44f);
        shadedCylinder(0.38f, 0.38f, 0.09f, 16, 0f, 1.85f, 0f, 0.35f, 0.38f, 0.44f);
        shadedCylinder(0.38f, 0.38f, 0.10f, 16, 0f, 2.86f, 0f, 0.35f, 0.38f, 0.44f);

        // Faixas de advertência âmbar e anel de carga ionizada ciano
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        shadedCylinder(0.368f, 0.368f, 0.18f, 16, 0f, 1.15f, 0f, 0.95f, 0.45f, 0.10f);
        shadedCylinder(0.368f, 0.368f, 0.14f, 16, 0f, 2.65f, 0f, 0.12f, 0.88f, 1.00f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 4. Câmara da ogiva dispersora (y = 2.95 .. 3.70) com bobinas de plasma
        bind(TEX_DARK);
        shadedCylinder(0.36f, 0.42f, 0.35f, 16, 0f, 2.95f, 0f, 0.45f, 0.48f, 0.54f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        shadedCylinder(0.43f, 0.43f, 0.10f, 16, 0f, 3.30f, 0f, 0.18f, 0.92f, 1.00f);
        shadedCylinder(0.41f, 0.41f, 0.08f, 16, 0f, 3.45f, 0f, 0.98f, 0.40f, 0.12f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 5. Cone balístico + agulha dispersora (y = 3.53 .. 5.00)
        bind(TEX_BODY);
        shadedCylinder(0.40f, 0.24f, 0.65f, 16, 0f, 3.53f, 0f, 0.90f, 0.93f, 0.96f);
        shadedCylinder(0.24f, 0.06f, 0.46f, 14, 0f, 4.18f, 0f, 0.95f, 0.42f, 0.12f);
        bind(TEX_DARK);
        shadedCylinder(0.05f, 0.015f, 0.36f, 10, 0f, 4.64f, 0f, 0.72f, 0.85f, 0.95f);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private void renderInterceptorFin() {
        bind(TEX_DARK);
        Tessellator t = Tessellator.instance;
        float r = 0.34f, w = 0.82f, y0 = 0.20f, y1 = 1.40f, z = 0.035f;
        t.startDrawingQuads();
        t.setColorOpaque_F(0.40f, 0.44f, 0.50f);
        t.addVertexWithUV(r, y0 + 0.16f, z, 0, 0);
        t.addVertexWithUV(w, y0, z, 1, 0);
        t.addVertexWithUV(w, y0 + 0.28f, z, 1, 1);
        t.addVertexWithUV(r, y1, z, 0, 1);
        t.setColorOpaque_F(0.24f, 0.27f, 0.32f);
        t.addVertexWithUV(r, y0 + 0.16f, -z, 0, 0);
        t.addVertexWithUV(r, y1, -z, 0, 1);
        t.addVertexWithUV(w, y0 + 0.28f, -z, 1, 1);
        t.addVertexWithUV(w, y0, -z, 1, 0);
        t.draw();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        t.startDrawingQuads();
        t.setColorOpaque_F(0.98f, 0.42f, 0.12f);
        t.addVertex(w - 0.04f, y0 + 0.02f, -z - 0.005f);
        t.addVertex(w + 0.01f, y0 + 0.02f, -z - 0.005f);
        t.addVertex(w + 0.01f, y0 + 0.26f, z + 0.005f);
        t.addVertex(w - 0.04f, y0 + 0.26f, z + 0.005f);
        t.draw();
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
            float f = 0.44f + 0.56f * Math.max(0f, (float) Math.cos(am) * LX + (float) Math.sin(am) * LZ);
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

    private void bind(ResourceLocation tex) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}