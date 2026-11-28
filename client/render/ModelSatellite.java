package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.cinematic.CinematicTimeline;

/**
 * Modelo procedural do Satélite Solar Orbital com:
 *  - Alinhamento perfeito de todos os 6 segmentos até a ponta da asa solar
 *  - Dano persistente dramático (chapas arrancadas, marcas de queimadura,
 *    arranhões, aletas danificadas e painéis solares ausentes) recuperado com Kits de Reparos
 */
public class ModelSatellite {

    public static final int SEGMENTS = 6;

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/satellite_body.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(
        "orbitalsunworks", "textures/models/satellite_gold.png");
    private static final ResourceLocation TEX_PANEL = new ResourceLocation(
        "orbitalsunworks", "textures/models/solar_cell.png");

    private static final float LX = 0.55f, LY = 0.72f, LZ = 0.42f;
    private static final int[] BREAK_ORDER = new int[] {
        11, 5, 9, 3, 10, 4, 8, 2, 7, 1, 6, 0
    };

    public static int getMissingSegmentCount(float health) {
        float hp = Math.max(0f, Math.min(1f, health));
        return Math.max(0, Math.min(SEGMENTS, Math.round((1.0f - hp) * SEGMENTS)));
    }

    public void renderAll(float unfold, float spin) {
        this.renderAll(unfold, spin, -1f, 1.0f);
    }

    public void renderAll(float unfold, float spin, float timeTicks) {
        this.renderAll(unfold, spin, timeTicks, 1.0f);
    }

    public void renderAll(float unfold, float spin, float timeTicks, float health) {
        float hp = Math.max(0f, Math.min(1f, health));
        float dmg = 1.0f - hp;
        float ant = timeTicks >= 0f
            ? CinematicTimeline.antennaDeploy(timeTicks)
            : CinematicTimeline.easeInOut(unfold / 0.45f);

        GL11.glPushMatrix();

        float hullDark = 1.0f - dmg * 0.42f;
        this.goldBox(-0.46f, 0.00f, -0.46f, 0.46f, 1.02f, 0.46f, 1.00f * hullDark);

        if (hp > 0.35f) {
            this.metalBox(-0.48f, 0.90f, -0.48f, 0.48f, 1.04f, 0.48f, 0.92f * hullDark);
        }
        this.metalBox(-0.48f, -0.02f, -0.48f, 0.48f, 0.14f, 0.48f, 0.88f * hullDark);

        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                float cornerMul = (hp < 0.65f && sx == 1 && sz == 1) ? 0.50f : 0.90f;
                this.metalBox(
                    sx * 0.46f - 0.04f, 0.00f, sz * 0.46f - 0.04f,
                    sx * 0.46f + 0.04f, 1.02f, sz * 0.46f + 0.04f, cornerMul);
            }
        }

        this.plainBox(-0.22f, 0.30f, 0.46f, 0.22f, 0.72f, 0.52f, 0.18f, 0.22f, 0.28f);
        if (hp > 0.45f) {
            this.plainBox(-0.14f, 0.38f, 0.52f, 0.14f, 0.64f, 0.56f, 0.22f, 0.72f, 0.92f);
        }

        if (dmg > 0.05f) {
            float scar = Math.min(1f, dmg * 1.4f);
            this.plainBox(-0.34f, 0.35f, -0.472f, 0.32f, 0.62f, -0.462f, 0.11f, 0.10f, 0.11f);
            this.plainBox(0.462f, 0.28f, -0.32f, 0.474f, 0.78f, 0.36f, 0.13f, 0.11f, 0.10f);
            this.plainBox(-0.474f, 0.48f, -0.22f, -0.462f, 0.82f, 0.28f, 0.16f, 0.13f, 0.11f);
            if (scar > 0.35f) {
                this.plainBox(-0.20f, 0.18f, 0.461f, 0.28f, 0.27f, 0.473f, 0.09f, 0.09f, 0.10f);
                this.plainBox(-0.12f, 0.76f, -0.473f, 0.40f, 0.86f, -0.461f, 0.26f, 0.14f, 0.06f);
            }
        }

        for (int i = 0; i < 3; i++) {
            if (hp < 0.85f && i == 2) continue;
            if (hp < 0.55f && i == 0) continue;
            float y = 0.28f + i * 0.24f;
            this.metalBox(-0.12f, y, -0.60f, 0.12f, y + 0.04f, -0.46f, 0.86f * hullDark);
        }

        this.metalBox(-0.16f, -0.14f, -0.16f, 0.16f, 0.00f, 0.16f, 0.72f);

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1.04f, 0f);
        this.metalBox(-0.04f, 0f, -0.04f, 0.04f, 0.44f * ant, 0.04f, 0.90f);
        GL11.glPopMatrix();

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1.04f + 0.44f * ant, 0f);
        if (hp < 0.70f) {
            GL11.glRotatef(14f * (1f - hp), 0f, 0f, 1f);
        }
        GL11.glRotatef(22f, 1f, 0f, 0f);
        this.parabolicDish(0.58f * ant, 0.19f * ant, hp);
        GL11.glPopMatrix();

        this.armAndAccordion(-1, unfold, timeTicks, hp);
        this.armAndAccordion(1, unfold, timeTicks, hp);

        GL11.glPopMatrix();
    }

    private boolean isSegmentPresent(int side, int segIndex, float hp) {
        int remaining = Math.max(0, Math.min(12, Math.round(hp * 12.0f)));
        int slot = (side < 0 ? 0 : 6) + segIndex;
        for (int k = 0; k < 12 - remaining; k++) {
            if (BREAK_ORDER[k] == slot) {
                return false;
            }
        }
        return true;
    }

    private boolean isNextDamagedTip(int side, int segIndex, float hp) {
        int remaining = Math.max(0, Math.min(12, Math.round(hp * 12.0f)));
        if (remaining >= 12 || remaining <= 0) {
            return false;
        }
        int slot = (side < 0 ? 0 : 6) + segIndex;
        return BREAK_ORDER[12 - remaining] == slot;
    }

    private void parabolicDish(float rOuter, float depth, float hp) {
        if (rOuter < 0.05f) {
            return;
        }
        Tessellator t = Tessellator.instance;
        this.bind(TEX_BODY);
        int segs = 20;
        float rInner = rOuter * 0.28f;

        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < segs; i++) {
            if (hp < 0.60f && (i == 4 || i == 5 || i == 14)) {
                continue;
            }
            double a0 = Math.PI * 2 * i / segs;
            double a1 = Math.PI * 2 * (i + 1) / segs;
            float am = (float) ((a0 + a1) / 2);
            float f = 0.72f + 0.28f * Math.max(0f, LY * 0.8f + (float) Math.cos(am) * LX * 0.4f);
            t.setColorOpaque_F(f, f * 0.98f, f * 0.94f);
            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.42f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rOuter, 0f, (float) Math.sin(a0) * rOuter, 0.30f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.30f, 0.62f);
            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.42f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.30f, 0.62f);
            t.addVertexWithUV((float) Math.cos(a1) * rInner, depth * 0.55f, (float) Math.sin(a1) * rInner, 0.56f, 0.5f);
        }
        t.draw();

        for (int i = 0; i < 3; i++) {
            double ang = Math.PI * 2 * i / 3.0;
            GL11.glPushMatrix();
            GL11.glRotatef((float) (ang * 180.0 / Math.PI), 0f, 1f, 0f);
            GL11.glTranslatef(rOuter * 0.45f, 0.04f, 0f);
            GL11.glRotatef(38f, 0f, 0f, 1f);
            this.metalBox(-0.015f, 0f, -0.015f, 0.015f, 0.24f, 0.015f, 0.95f);
            GL11.glPopMatrix();
        }
        this.goldBox(-0.04f, 0.16f, -0.04f, 0.04f, 0.22f, 0.04f, 0.92f);
    }

    private void armAndAccordion(int side, float unfold, float timeTicks, float hp) {
        float yoke;
        float[] seg = new float[SEGMENTS];
        if (timeTicks >= 0f) {
            yoke = CinematicTimeline.yokeOpen(timeTicks, side);
            for (int i = 0; i < SEGMENTS; i++) {
                seg[i] = CinematicTimeline.segmentExtend(timeTicks, side, i);
            }
        } else {
            yoke = CinematicTimeline.clamp01(unfold / 0.35f);
            float span = CinematicTimeline.clamp01((unfold - 0.35f) / 0.65f);
            for (int i = 0; i < SEGMENTS; i++) {
                float start = (i / (float) SEGMENTS) * 0.45f;
                float end = start + 0.55f;
                float local = (span - start) / (end - start);
                seg[i] = CinematicTimeline.easeInOut(local);
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(side * 0.46f, 0.62f, 0f);
        GL11.glRotatef(-side * 90f * yoke, 0f, 0f, 1f);
        this.metalBox(-0.055f, 0f, -0.055f, 0.055f, 0.30f, 0.055f, 0.88f);
        GL11.glTranslatef(0f, 0.30f, 0f);
        GL11.glRotatef(90f, 0f, 0f, 1f);

        float segLen = 0.42f;
        float segW = 0.50f;
        float segThick = 0.035f;
        for (int i = 0; i < SEGMENTS; i++) {
            float ext = seg[i];
            float ang = (1f - ext) * 160f;
            if (isSegmentPresent(side, i, hp)) {
                boolean scorched = isNextDamagedTip(side, i, hp);
                GL11.glPushMatrix();
                GL11.glRotatef(ang, 0f, 1f, 0f);
                this.bind(TEX_PANEL);
                this.panelSegment(segLen, segW, segThick, scorched);
                GL11.glPopMatrix();
            } else {
                this.metalBox(0f, -0.015f, -0.06f, segLen * 0.35f, 0.015f, -0.03f, 0.48f);
            }
            float adv = Math.max(0.04f, segLen * ext);
            GL11.glTranslatef(adv, 0f, 0f);
        }
        GL11.glPopMatrix();
    }

    private void panelSegment(float len, float w, float thick, boolean scorched) {
        Tessellator t = Tessellator.instance;
        float x0 = 0f, x1 = len;
        float z0 = -w / 2f, z1 = w / 2f;
        float y0 = -thick / 2f, y1 = thick / 2f;

        t.startDrawingQuads();
        float fTop = (0.82f + 0.18f * Math.max(0f, LY)) * (scorched ? 0.42f : 1.0f);
        t.setColorOpaque_F(fTop * (scorched ? 0.90f : 0.64f), fTop * (scorched ? 0.50f : 0.76f), fTop * (scorched ? 0.42f : 1.0f));
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 0f);
        t.addVertexWithUV(x0, y1, z0, 0f, 0f);
        t.draw();

        t.startDrawingQuads();
        t.setColorOpaque_F(0.56f, 0.62f, 0.70f);
        t.addVertexWithUV(x0, y0, z0, 0f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.draw();

        this.metalBox(-0.03f, -0.045f, -w / 2f + 0.03f, 0.02f, 0.045f, w / 2f - 0.03f, 0.95f);
    }

    private void metalBox(float x0, float y0, float z0, float x1, float y1, float z1, float mul) {
        this.bind(TEX_BODY);
        this.drawingBox(x0, y0, z0, x1, y1, z1,
            0.96f * mul, 0.98f * mul, 1.00f * mul,
            0.50f * mul, 0.54f * mul, 0.60f * mul);
    }

    private void goldBox(float x0, float y0, float z0, float x1, float y1, float z1, float mul) {
        this.bind(TEX_GOLD);
        this.drawingBox(x0, y0, z0, x1, y1, z1,
            1.00f * mul, 0.86f * mul, 0.40f * mul,
            0.52f * mul, 0.42f * mul, 0.18f * mul);
    }

    private void plainBox(float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b) {
        this.bind(TEX_BODY);
        this.drawingBox(x0, y0, z0, x1, y1, z1, r, g, b, r * 0.52f, g * 0.52f, b * 0.52f);
    }

    private void drawingBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float lr, float lg, float lb, float dr, float dg, float db) {
        Tessellator t = Tessellator.instance;
        float f = Math.max(0f, LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 0f);
        t.addVertexWithUV(x0, y1, z0, 0f, 0f);
        t.draw();

        f = Math.max(0f, -LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z0, 0f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.draw();

        f = Math.max(0f, LX);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x1, y0, z1, 0f, 0f);
        t.addVertexWithUV(x1, y0, z0, 1f, 0f);
        t.addVertexWithUV(x1, y1, z0, 1f, 1f);
        t.addVertexWithUV(x1, y1, z1, 0f, 1f);
        t.draw();

        f = Math.max(0f, -LX);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z0, 0f, 0f);
        t.addVertexWithUV(x0, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y1, z1, 1f, 1f);
        t.addVertexWithUV(x0, y1, z0, 0f, 1f);
        t.draw();

        f = Math.max(0f, LZ);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.draw();

        f = Math.max(0f, -LZ);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y1, z0, 0f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 0f);
        t.addVertexWithUV(x0, y0, z0, 0f, 0f);
        t.draw();
    }

    private void bind(ResourceLocation tex) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}