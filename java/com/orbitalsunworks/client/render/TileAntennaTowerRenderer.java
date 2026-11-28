package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderizador 3D da Torre da Antena Receptora Orbital:
 * treliça de aço reforçada com 8 blocos de altura, anéis receptores ciano
 * e prato parabólico gigante no topo rastreando o céu suavemente.
 */
@SideOnly(Side.CLIENT)
public class TileAntennaTowerRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        long worldTime = tile.getWorldObj() != null ? tile.getWorldObj().getTotalWorldTime() : 0L;
        float t = worldTime + partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        // 1. Base de acoplamento sobre o bloco extrator
        bind(TEX_DARK);
        drawBox(-0.46f, 0.0f, -0.46f, 0.46f, 0.35f, 0.46f, 0.42f, 0.46f, 0.52f);
        drawBox(-0.36f, 0.35f, -0.36f, 0.36f, 0.75f, 0.36f, 0.56f, 0.60f, 0.66f);

        // 2. Quatro pilares treliçados verticais até y = 6.2
        float bot = 0.34f, top = 0.20f, h = 6.2f;
        float[][] corners = { { -1, -1 }, { 1, -1 }, { 1, 1 }, { -1, 1 } };
        bind(TEX_BODY);
        for (float[] c : corners) {
            drawStrut(c[0] * bot, 0.70f, c[1] * bot, c[0] * top, h, c[1] * top, 0.055f, 0.78f, 0.84f, 0.90f);
        }

        // Anéis estruturais horizontais e cruzetas
        bind(TEX_DARK);
        for (int tier = 0; tier < 4; tier++) {
            float fy = 1.6f + tier * 1.35f;
            float r = bot + (top - bot) * ((fy - 0.7f) / (h - 0.7f));
            drawBox(-r - 0.04f, fy, -r - 0.04f, r + 0.04f, fy + 0.09f, r + 0.04f, 0.36f, 0.40f, 0.46f);
        }
        // Duto central de energia ciano brilhante
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        drawBox(-0.08f, 0.75f, -0.08f, 0.08f, 6.25f, 0.08f, 0.14f, 0.85f, 1.00f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // 3. Plataforma giratória no topo (y = 6.2 .. 6.5)
        float yawSweep = (float) Math.sin(t * 0.014f) * 38.0f + t * 0.18f;
        float pitchNod = 56.0f + (float) Math.sin(t * 0.022f) * 10.0f;

        GL11.glTranslatef(0f, 6.25f, 0f);
        GL11.glRotatef(yawSweep, 0f, 1f, 0f);

        bind(TEX_DARK);
        drawBox(-0.30f, 0.0f, -0.30f, 0.30f, 0.32f, 0.30f, 0.44f, 0.48f, 0.54f);
        drawBox(-0.34f, 0.32f, -0.10f, -0.22f, 0.90f, 0.10f, 0.52f, 0.56f, 0.62f);
        drawBox(0.22f, 0.32f, -0.10f, 0.34f, 0.90f, 0.10f, 0.52f, 0.56f, 0.62f);

        // 4. Prato parabólico apontando para o céu
        GL11.glTranslatef(0f, 0.76f, 0f);
        GL11.glRotatef(pitchNod, 1f, 0f, 0f);

        bind(TEX_BODY);
        renderParabolicDish(1.75f, 0.62f, 18);

        // Hastes do receptor focal e ponta luminosa
        bind(TEX_DARK);
        for (int i = 0; i < 3; i++) {
            double a = i * Math.PI * 2.0 / 3.0;
            drawStrut((float) Math.cos(a) * 1.45f, 0.50f, (float) Math.sin(a) * 1.45f,
                0f, 1.55f, 0f, 0.032f, 0.55f, 0.60f, 0.66f);
        }
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        float pulse = 0.7f + 0.3f * (float) Math.sin(t * 0.12f);
        drawBox(-0.11f, 1.45f, -0.11f, 0.11f, 1.72f, 0.11f, 0.15f * pulse, 0.92f * pulse, 1.0f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void renderParabolicDish(float radius, float depth, int sides) {
        Tessellator ts = Tessellator.instance;
        int rings = 3;
        for (int r = 0; r < rings; r++) {
            float r0 = radius * r / rings;
            float r1 = radius * (r + 1) / rings;
            float y0 = depth * (r / (float) rings) * (r / (float) rings);
            float y1 = depth * ((r + 1) / (float) rings) * ((r + 1) / (float) rings);
            ts.startDrawingQuads();
            float shade = 0.72f + 0.08f * r;
            ts.setColorOpaque_F(shade * 0.90f, shade * 0.94f, shade);
            for (int i = 0; i < sides; i++) {
                double a0 = Math.PI * 2.0 * i / sides;
                double a1 = Math.PI * 2.0 * (i + 1) / sides;
                ts.addVertexWithUV(Math.cos(a0) * r0, y0, Math.sin(a0) * r0, 0, 0);
                ts.addVertexWithUV(Math.cos(a1) * r0, y0, Math.sin(a1) * r0, 1, 0);
                ts.addVertexWithUV(Math.cos(a1) * r1, y1, Math.sin(a1) * r1, 1, 1);
                ts.addVertexWithUV(Math.cos(a0) * r1, y1, Math.sin(a0) * r1, 0, 1);
            }
            ts.draw();
        }
    }

    private void drawStrut(float x0, float y0, float z0, float x1, float y1, float z1,
            float w, float r, float g, float b) {
        Tessellator ts = Tessellator.instance;
        ts.startDrawingQuads();
        ts.setColorOpaque_F(r, g, b);
        ts.addVertexWithUV(x0 - w, y0, z0, 0, 0);
        ts.addVertexWithUV(x0 + w, y0, z0, 1, 0);
        ts.addVertexWithUV(x1 + w, y1, z1, 1, 1);
        ts.addVertexWithUV(x1 - w, y1, z1, 0, 1);
        ts.addVertexWithUV(x0, y0, z0 - w, 0, 0);
        ts.addVertexWithUV(x0, y0, z0 + w, 1, 0);
        ts.addVertexWithUV(x1, y1, z1 + w, 1, 1);
        ts.addVertexWithUV(x1, y1, z1 - w, 0, 1);
        ts.draw();
    }

    private void drawBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator ts = Tessellator.instance;
        ts.startDrawingQuads();
        ts.setColorOpaque_F(r, g, b);
        ts.addVertexWithUV(x0, y1, z1, 0, 1); ts.addVertexWithUV(x1, y1, z1, 1, 1);
        ts.addVertexWithUV(x1, y1, z0, 1, 0); ts.addVertexWithUV(x0, y1, z0, 0, 0);
        ts.addVertexWithUV(x0, y0, z0, 0, 1); ts.addVertexWithUV(x1, y0, z0, 1, 1);
        ts.addVertexWithUV(x1, y0, z1, 1, 0); ts.addVertexWithUV(x0, y0, z1, 0, 0);
        ts.addVertexWithUV(x1, y0, z1, 0, 0); ts.addVertexWithUV(x1, y0, z0, 1, 0);
        ts.addVertexWithUV(x1, y1, z0, 1, 1); ts.addVertexWithUV(x1, y1, z1, 0, 1);
        ts.addVertexWithUV(x0, y0, z0, 0, 0); ts.addVertexWithUV(x0, y0, z1, 1, 0);
        ts.addVertexWithUV(x0, y1, z1, 1, 1); ts.addVertexWithUV(x0, y1, z0, 0, 1);
        ts.addVertexWithUV(x0, y0, z1, 0, 0); ts.addVertexWithUV(x1, y0, z1, 1, 0);
        ts.addVertexWithUV(x1, y1, z1, 1, 1); ts.addVertexWithUV(x0, y1, z1, 0, 1);
        ts.addVertexWithUV(x1, y0, z0, 0, 0); ts.addVertexWithUV(x0, y0, z0, 1, 0);
        ts.addVertexWithUV(x0, y1, z0, 1, 1); ts.addVertexWithUV(x1, y1, z0, 0, 1);
        ts.draw();
    }

    private void bind(ResourceLocation tex) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}