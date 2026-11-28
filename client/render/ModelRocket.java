package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * MODELO 3D DO FOGUETE ORBITAL TIER 1 (6 blocos de altura: y = 0.0 .. 6.0).
 */
@SideOnly(Side.CLIENT)
public class ModelRocket {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");

    private static final float LX = 0.42f, LY = 0.82f, LZ = 0.38f;

    public static final float PAYLOAD_ADAPTER_Y = 4.35f;

    public void renderAll() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        renderCoreStages(0f);
        renderFairingHalf(-1);
        renderFairingHalf(1);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    public void renderBodyWithoutFairing(float nozzleHeat) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        renderCoreStages(nozzleHeat);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private void renderCoreStages(float nozzleHeat) {
        bind(TEX_DARK);
        float hr = 0.56f + 0.44f * nozzleHeat;
        float hg = 0.60f - 0.12f * nozzleHeat;
        float hb = 0.66f - 0.40f * nozzleHeat;
        shadedCylinder(0.32f, 0.16f, 0.48f, 16, 0f, 0.0f, 0f, hr, hg, hb);
        shadedDisc(0.30f, 16, 0f, 0.02f, 0f, false,
            0.12f + 0.85f * nozzleHeat, 0.13f + 0.42f * nozzleHeat, 0.16f + 0.10f * nozzleHeat);
        shadedCylinder(0.28f, 0.27f, 0.05f, 16, 0f, 0.14f, 0f, 0.72f, 0.75f, 0.80f);
        shadedCylinder(0.22f, 0.21f, 0.05f, 16, 0f, 0.30f, 0f, 0.72f, 0.75f, 0.80f);

        for (int s = -1; s <= 1; s += 2) {
            shadedCylinder(0.11f, 0.06f, 0.32f, 10, s * 0.36f, 0.06f, 0f, 0.55f, 0.58f, 0.64f);
            shadedDisc(0.10f, 10, s * 0.36f, 0.07f, 0f, false, 0.15f, 0.16f, 0.19f);
        }

        shadedCylinder(0.49f, 0.54f, 0.37f, 16, 0f, 0.48f, 0f, 0.54f, 0.58f, 0.64f);
        shadedDisc(0.49f, 16, 0f, 0.48f, 0f, false, 0.22f, 0.24f, 0.28f);

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            renderFoldedLegFairing();
            GL11.glPopMatrix();
        }

        bind(TEX_BODY);
        shadedCylinder(0.54f, 0.54f, 2.30f, 16, 0f, 0.85f, 0f, 0.90f, 0.93f, 0.97f);

        bind(TEX_DARK);
        shadedCylinder(0.558f, 0.558f, 0.09f, 16, 0f, 0.85f, 0f, 0.46f, 0.50f, 0.56f);
        shadedCylinder(0.555f, 0.555f, 0.07f, 16, 0f, 1.98f, 0f, 0.50f, 0.54f, 0.60f);
        shadedCylinder(0.558f, 0.558f, 0.09f, 16, 0f, 3.06f, 0f, 0.46f, 0.50f, 0.56f);
        for (int s = -1; s <= 1; s += 2) {
            drawBox(-0.06f, 0.88f, s * 0.52f - 0.045f, 0.06f, 3.10f, s * 0.52f + 0.045f,
                0.70f, 0.74f, 0.80f, 0.30f, 0.33f, 0.38f);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        shadedCylinder(0.548f, 0.548f, 0.07f, 16, 0f, 2.86f, 0f, 0.20f, 0.82f, 0.96f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(i * 90f, 0f, 1f, 0f);
            bind(TEX_DARK);
            drawBox(-0.14f, 2.82f, 0.53f, 0.14f, 3.14f, 0.66f,
                0.52f, 0.56f, 0.62f, 0.24f, 0.27f, 0.32f);
            GL11.glPopMatrix();
        }

        bind(TEX_DARK);
        shadedCylinder(0.54f, 0.50f, 0.50f, 16, 0f, 3.15f, 0f, 0.40f, 0.43f, 0.48f);

        bind(TEX_BODY);
        shadedCylinder(0.50f, 0.50f, 0.70f, 16, 0f, 3.65f, 0f, 0.88f, 0.91f, 0.95f);
        bind(TEX_DARK);
        shadedCylinder(0.515f, 0.515f, 0.08f, 16, 0f, 4.27f, 0f, 0.48f, 0.52f, 0.58f);
        shadedDisc(0.50f, 16, 0f, 4.35f, 0f, true, 0.36f, 0.39f, 0.44f);
        shadedCylinder(0.36f, 0.28f, 0.12f, 16, 0f, 4.35f, 0f, 0.52f, 0.56f, 0.62f);
        shadedDisc(0.28f, 16, 0f, 4.47f, 0f, true, 0.65f, 0.68f, 0.74f);

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            drawBox(-0.05f, 3.92f, 0.49f, 0.05f, 4.08f, 0.56f,
                0.56f, 0.60f, 0.66f, 0.24f, 0.27f, 0.32f);
            GL11.glPopMatrix();
        }
    }

    public void renderFairingHalf(int side) {
        Tessellator t = Tessellator.instance;
        int segs = 8;
        double a0 = side < 0 ? Math.PI / 2.0 : -Math.PI / 2.0;
        double a1 = side < 0 ? Math.PI * 1.5 : Math.PI / 2.0;

        float[] ys = { 4.35f, 4.62f, 5.20f, 5.62f, 5.88f, 6.00f };
        float[] rs = { 0.50f, 0.62f, 0.62f, 0.40f, 0.16f, 0.03f };
        float thickness = 0.045f;

        bind(TEX_BODY);
        for (int s = 0; s < ys.length - 1; s++) {
            float yA = ys[s], yB = ys[s + 1];
            float rA = rs[s], rB = rs[s + 1];

            t.startDrawingQuads();
            for (int i = 0; i < segs; i++) {
                double ang0 = a0 + (a1 - a0) * i / segs;
                double ang1 = a0 + (a1 - a0) * (i + 1) / segs;
                float am = (float) ((ang0 + ang1) * 0.5);
                float f = 0.45f + 0.55f * Math.max(0f, (float) Math.cos(am) * LX + (float) Math.sin(am) * LZ);
                t.setColorOpaque_F(0.92f * f, 0.95f * f, 0.99f * f);
                t.addVertexWithUV(Math.cos(ang0) * rA, yA, Math.sin(ang0) * rA, i / (double) segs, 1);
                t.addVertexWithUV(Math.cos(ang1) * rA, yA, Math.sin(ang1) * rA, (i + 1) / (double) segs, 1);
                t.addVertexWithUV(Math.cos(ang1) * rB, yB, Math.sin(ang1) * rB, (i + 1) / (double) segs, 0);
                t.addVertexWithUV(Math.cos(ang0) * rB, yB, Math.sin(ang0) * rB, i / (double) segs, 0);
            }
            t.draw();

            float irA = Math.max(0.01f, rA - thickness);
            float irB = Math.max(0.01f, rB - thickness);
            t.startDrawingQuads();
            t.setColorOpaque_F(0.24f, 0.26f, 0.30f);
            for (int i = 0; i < segs; i++) {
                double ang0 = a0 + (a1 - a0) * i / segs;
                double ang1 = a0 + (a1 - a0) * (i + 1) / segs;
                t.addVertexWithUV(Math.cos(ang0) * irA, yA, Math.sin(ang0) * irA, i / (double) segs, 1);
                t.addVertexWithUV(Math.cos(ang0) * irB, yB, Math.sin(ang0) * irB, i / (double) segs, 0);
                t.addVertexWithUV(Math.cos(ang1) * irB, yB, Math.sin(ang1) * irB, (i + 1) / (double) segs, 0);
                t.addVertexWithUV(Math.cos(ang1) * irA, yA, Math.sin(ang1) * irA, (i + 1) / (double) segs, 1);
            }
            t.draw();
        }

        bind(TEX_DARK);
        for (int end = 0; end < 2; end++) {
            double ang = end == 0 ? a0 : a1;
            float ca = (float) Math.cos(ang);
            float sa = (float) Math.sin(ang);
            t.startDrawingQuads();
            t.setColorOpaque_F(0.45f, 0.48f, 0.54f);
            for (int s = 0; s < ys.length - 1; s++) {
                float rA = rs[s], rB = rs[s + 1];
                float irA = Math.max(0.01f, rA - thickness);
                float irB = Math.max(0.01f, rB - thickness);
                t.addVertexWithUV(ca * rA, ys[s], sa * rA, 0, 0);
                t.addVertexWithUV(ca * irA, ys[s], sa * irA, 1, 0);
                t.addVertexWithUV(ca * irB, ys[s + 1], sa * irB, 1, 1);
                t.addVertexWithUV(ca * rB, ys[s + 1], sa * rB, 0, 1);
            }
            t.draw();
        }
    }

    private void renderFoldedLegFairing() {
        bind(TEX_DARK);
        Tessellator t = Tessellator.instance;
        float r = 0.51f, w = 0.78f, y0 = 0.34f, y1 = 1.92f, z = 0.06f;

        t.startDrawingQuads();
        t.setColorOpaque_F(0.48f, 0.52f, 0.58f);
        t.addVertexWithUV(r, y0 + 0.14f, z, 0, 0);
        t.addVertexWithUV(w, y0, z, 1, 0);
        t.addVertexWithUV(w - 0.10f, y0 + 0.52f, z * 0.7f, 1, 1);
        t.addVertexWithUV(r, y1, z * 0.6f, 0, 1);

        t.setColorOpaque_F(0.24f, 0.27f, 0.32f);
        t.addVertexWithUV(r, y0 + 0.14f, -z, 0, 0);
        t.addVertexWithUV(r, y1, -z * 0.6f, 0, 1);
        t.addVertexWithUV(w - 0.10f, y0 + 0.52f, -z * 0.7f, 1, 1);
        t.addVertexWithUV(w, y0, -z, 1, 0);

        t.setColorOpaque_F(0.36f, 0.40f, 0.46f);
        t.addVertexWithUV(w, y0, -z, 0, 0);
        t.addVertexWithUV(w, y0, z, 0, 1);
        t.addVertexWithUV(w - 0.10f, y0 + 0.52f, z * 0.7f, 1, 1);
        t.addVertexWithUV(w - 0.10f, y0 + 0.52f, -z * 0.7f, 1, 0);

        t.setColorOpaque_F(0.76f, 0.80f, 0.86f);
        t.addVertexWithUV(r, y1, -z * 0.6f, 0, 0);
        t.addVertexWithUV(r, y1, z * 0.6f, 0, 1);
        t.addVertexWithUV(w - 0.10f, y0 + 0.52f, z * 0.7f, 1, 1);
        t.addVertexWithUV(w - 0.10f, y0 + 0.52f, -z * 0.7f, 1, 0);
        t.draw();
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