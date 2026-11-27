package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.cinematic.CinematicTimeline;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelSatellite {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_PANEL = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_panel.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_gold.png");

    private static final float LX = 0.42f, LY = 0.82f, LZ = 0.38f;
    public static final int SEGMENTS = 6;

    public void renderAll(float unfold, float spin) {
        this.renderAll(unfold, spin, -1f);
    }

    public void renderAll(float unfold, float spin, float timeTicks) {
        float ant = timeTicks >= 0f ? CinematicTimeline.antennaDeploy(timeTicks) : unfold;

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glRotatef(spin, 0f, 1f, 0f);

        this.body();
        this.armAndAccordion(-1, unfold, timeTicks);
        this.armAndAccordion(1, unfold, timeTicks);
        this.antenna(ant);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    private void body() {
        // Barramento principal em titânio escovado + chanfros estruturais.
        this.metalBox(-0.44f, 0.02f, -0.44f, 0.44f, 1.15f, 0.44f, 0.98f);
        this.metalBox(-0.47f, 0.00f, -0.47f, 0.47f, 0.08f, 0.47f, 0.70f);
        this.metalBox(-0.47f, 0.53f, -0.47f, 0.47f, 0.61f, 0.47f, 0.65f);
        this.metalBox(-0.47f, 1.07f, -0.47f, 0.47f, 1.17f, 0.47f, 0.72f);

        // Anel térmico dourado sutil (aerospace foil accent).
        this.goldBox(-0.48f, 0.94f, -0.48f, 0.48f, 0.99f, 0.48f, 0.78f);

        // Quatro longarinas verticais nos cantos.
        float[][] corners = { { -0.46f, -0.46f }, { 0.41f, -0.46f }, { -0.46f, 0.41f }, { 0.41f, 0.41f } };
        for (float[] c : corners) {
            this.metalBox(c[0], 0.04f, c[1], c[0] + 0.05f, 1.14f, c[1] + 0.05f, 1.10f);
        }

        // Radiador térmico lateral +X (aletas metálicas claras).
        this.plainBox(0.44f, 0.14f, -0.28f, 0.51f, 0.90f, 0.28f, 0.88f, 0.92f, 0.96f);
        for (int i = 0; i < 4; i++) {
            float fy = 0.20f + i * 0.18f;
            this.plainBox(0.51f, fy, -0.24f, 0.54f, fy + 0.04f, 0.24f, 0.68f, 0.74f, 0.80f);
        }

        // Bancos de telemetria / sensores ópticos (-X e -Z).
        this.plainBox(-0.48f, 0.66f, -0.22f, -0.44f, 0.88f, 0.22f, 0.18f, 0.28f, 0.42f);
        this.plainBox(-0.22f, 0.66f, -0.48f, 0.22f, 0.88f, -0.44f, 0.18f, 0.28f, 0.42f);
        this.plainBox(-0.20f, 0.16f, 0.44f, 0.20f, 0.42f, 0.49f, 0.22f, 0.30f, 0.38f);

        // Propulsor de apogeu (campânula grafite abaixo do barramento).
        this.plainBox(-0.18f, -0.10f, -0.18f, 0.18f, 0.02f, 0.18f, 0.28f, 0.30f, 0.34f);
        this.plainBox(-0.12f, -0.24f, -0.12f, 0.12f, -0.10f, 0.12f, 0.16f, 0.17f, 0.20f);

        // Travas de separação da coifa no topo.
        for (int i = 0; i < 4; i++) {
            float a = (float) (Math.PI / 4 + i * Math.PI / 2);
            float cx = (float) Math.cos(a) * 0.38f;
            float cz = (float) Math.sin(a) * 0.38f;
            this.metalBox(cx - 0.05f, 1.15f, cz - 0.05f, cx + 0.05f, 1.24f, cz + 0.05f, 0.78f);
        }
    }

    private void antenna(float antDeploy) {
        // 1) 4 antenas de telemetria nos cantos superiores (dobradas para dentro -> abertas a +38°).
        float whipAngle = -78f + 116f * antDeploy;
        float[][] mounts = { { -0.34f, -0.34f }, { 0.34f, -0.34f }, { -0.34f, 0.34f }, { 0.34f, 0.34f } };
        for (int i = 0; i < 4; i++) {
            float mx = mounts[i][0];
            float mz = mounts[i][1];
            float yaw = (float) (Math.atan2(mz, mx) * 180.0 / Math.PI);
            GL11.glPushMatrix();
            GL11.glTranslatef(mx, 1.18f, mz);
            GL11.glRotatef(-yaw, 0f, 1f, 0f);
            GL11.glRotatef(whipAngle, 0f, 0f, 1f);
            this.metalBox(-0.022f, 0f, -0.022f, 0.022f, 0.42f, 0.022f, 1.05f);
            this.plainBox(-0.04f, 0.40f, -0.04f, 0.04f, 0.46f, 0.04f, 0.35f, 0.85f, 1.0f);
            GL11.glPopMatrix();
        }

        // 2) Haste telescópica central + prato parabólico de alto ganho (0.22f recolhido -> 0.62f estendido).
        float tilt = 35f * antDeploy;
        float boomLen = 0.22f + 0.40f * antDeploy;
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1.22f, 0f);
        GL11.glRotatef(tilt, 1f, 0f, 0f);

        this.metalBox(-0.048f, 0f, -0.048f, 0.048f, boomLen, 0.048f, 0.90f);
        this.metalBox(-0.025f, boomLen, -0.025f, 0.025f, boomLen + 0.18f, 0.025f, 1.15f);
        this.plainBox(-0.07f, boomLen + 0.18f, -0.07f, 0.07f, boomLen + 0.22f, 0.07f, 0.92f, 0.95f, 0.98f);

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, boomLen + 0.24f, 0f);
        this.dish();
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    private void dish() {
        Tessellator t = Tessellator.instance;
        this.bind(TEX_BODY);
        int segs = 24;
        float rOuter = 0.38f, rInner = 0.11f, depth = 0.14f;

        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < segs; i++) {
            double a0 = Math.PI * 2 * i / segs;
            double a1 = Math.PI * 2 * (i + 1) / segs;
            float am = (float) ((a0 + a1) / 2);
            float nx = (float) Math.cos(am), nz = (float) Math.sin(am);
            float f = 0.62f + 0.38f * Math.max(0f, nx * LX + nz * LZ);
            t.setColorOpaque_F(f * 0.92f, f * 0.95f, f * 1.0f);
            t.addVertexWithUV(0f, depth, 0f, 0.5f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.44f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rInner, depth * 0.55f, (float) Math.sin(a1) * rInner, 0.56f, 0.5f);

            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.42f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rOuter, 0f, (float) Math.sin(a0) * rOuter, 0.30f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.30f, 0.62f);

            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.42f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.30f, 0.62f);
            t.addVertexWithUV((float) Math.cos(a1) * rInner, depth * 0.55f, (float) Math.sin(a1) * rInner, 0.56f, 0.5f);
        }
        t.draw();

        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < segs; i++) {
            double a0 = Math.PI * 2 * i / segs;
            double a1 = Math.PI * 2 * (i + 1) / segs;
            float am = (float) ((a0 + a1) / 2);
            float nx = (float) Math.cos(am), nz = (float) Math.sin(am);
            float f = 0.38f + 0.32f * Math.max(0f, nx * LX + nz * LZ);
            t.setColorOpaque_F(f * 0.78f, f * 0.82f, f * 0.88f);
            t.addVertexWithUV(0f, -depth * 0.35f, 0f, 0.5f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.7f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rOuter, 0f, (float) Math.sin(a0) * rOuter, 0.7f, 0.6f);
        }
        t.draw();

        // Tripé de suporte do feedhorn no centro do prato.
        for (int i = 0; i < 3; i++) {
            double ang = Math.PI * 2 * i / 3.0;
            GL11.glPushMatrix();
            GL11.glRotatef((float) (ang * 180.0 / Math.PI), 0f, 1f, 0f);
            GL11.glTranslatef(rOuter * 0.45f, 0.04f, 0f);
            GL11.glRotatef(38f, 0f, 0f, 1f);
            this.metalBox(-0.015f, 0f, -0.015f, 0.015f, 0.24f, 0.015f, 0.95f);
            GL11.glPopMatrix();
        }
        this.goldBox(-0.04f, 0.16f, -0.04f, 0.04f, 0.22f, 0.04f, 0.90f);
    }

    private void armAndAccordion(int side, float unfold, float timeTicks) {
        float yoke;
        float[] seg = new float[SEGMENTS];
        if (timeTicks >= 0f) {
            yoke = CinematicTimeline.yokeOpen(timeTicks, side);
            for (int i = 0; i < SEGMENTS; i++) {
                seg[i] = CinematicTimeline.segmentExtend(timeTicks, side, i);
            }
        } else {
            yoke = CinematicTimeline.clamp01(unfold / 0.35f);
            float span = (unfold - 0.35f) / 0.65f;
            for (int i = 0; i < SEGMENTS; i++) {
                float start = (float) i / SEGMENTS;
                float end = start + 0.55f;
                float local = (span - start) / (end - start);
                seg[i] = CinematicTimeline.easeInOut(local);
            }
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(side * 0.46f, 0.62f, 0f);
        GL11.glRotatef(-side * 90f * yoke, 0f, 0f, 1f);
        this.metalBox(-0.055f, 0f, -0.055f, 0.055f, 0.30f, 0.055f, 0.85f);

        GL11.glTranslatef(0f, 0.30f, 0f);
        GL11.glRotatef(90f, 0f, 0f, 1f);

        float segLen = 0.42f;
        float segW = 0.50f;
        float segThick = 0.035f;

        for (int i = 0; i < SEGMENTS; i++) {
            float ext = seg[i];
            float ang = (1f - ext) * 160f;
            GL11.glPushMatrix();
            GL11.glRotatef(ang, 0f, 1f, 0f);
            this.bind(TEX_PANEL);
            this.panelSegment(segLen, segW, segThick);
            GL11.glPopMatrix();

            float adv = segLen * ext;
            if (adv < 0.04f) {
                adv = 0.04f;
            }
            GL11.glTranslatef(adv, 0f, 0f);
        }

        GL11.glPopMatrix();
    }

    private void panelSegment(float len, float w, float thick) {
        Tessellator t = Tessellator.instance;
        float x0 = 0f, x1 = len;
        float z0 = -w / 2f, z1 = w / 2f;
        float y0 = -thick / 2f, y1 = thick / 2f;

        t.startDrawingQuads();
        float fTop = 0.78f + 0.22f * Math.max(0f, LY);
        t.setColorOpaque_F(fTop * 0.60f, fTop * 0.72f, fTop * 1.0f);
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 0f);
        t.addVertexWithUV(x0, y1, z0, 0f, 0f);
        t.draw();

        t.startDrawingQuads();
        t.setColorOpaque_F(0.46f, 0.50f, 0.56f);
        t.addVertexWithUV(x0, y0, z0, 0f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.draw();

        t.startDrawingQuads();
        t.setColorOpaque_F(0.68f, 0.74f, 0.80f);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1);
        t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0);
        t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0);
        t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.draw();

        this.metalBox(-0.03f, -0.045f, -w / 2f + 0.03f, 0.02f, 0.045f, w / 2f - 0.03f, 0.95f);
    }

    private void metalBox(float x0, float y0, float z0, float x1, float y1, float z1, float mul) {
        this.bind(TEX_BODY);
        this.drawingBox(x0, y0, z0, x1, y1, z1,
            0.94f * mul, 0.97f * mul, 1.00f * mul,
            0.34f * mul, 0.37f * mul, 0.42f * mul);
    }

    private void goldBox(float x0, float y0, float z0, float x1, float y1, float z1, float mul) {
        this.bind(TEX_GOLD);
        this.drawingBox(x0, y0, z0, x1, y1, z1,
            1.00f * mul, 0.84f * mul, 0.38f * mul,
            0.38f * mul, 0.30f * mul, 0.12f * mul);
    }

    private void plainBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        this.bind(TEX_BODY);
        this.drawingBox(x0, y0, z0, x1, y1, z1, r, g, b, r * 0.38f, g * 0.38f, b * 0.38f);
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