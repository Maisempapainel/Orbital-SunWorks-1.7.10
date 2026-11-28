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
 * Renderizador 3D do Telescópio do Observatório:
 *  - Base e console ocular no interior da sala do observatório (y = 0 .. 4.3)
 *  - Grande telescópio refrator ("lupa") montado acima do domo apontando para o céu
 *    com movimento sutil e contínuo de varredura orbital.
 */
@SideOnly(Side.CLIENT)
public class TileTelescopeRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_gold.png");

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        long worldTime = tile.getWorldObj() != null ? tile.getWorldObj().getTotalWorldTime() : 0L;
        float t = worldTime + partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        // ------------------------------------------------------------------
        // 1. ESTAÇÃO OCULAR INTERNA (acessível no piso do Observatório)
        // ------------------------------------------------------------------
        bind(TEX_DARK);
        drawCylinder(0.62f, 0.50f, 0.35f, 16, 0f, 0.0f, 0f, 0.35f, 0.39f, 0.45f);
        bind(TEX_BODY);
        drawCylinder(0.36f, 0.30f, 1.05f, 16, 0f, 0.35f, 0f, 0.78f, 0.84f, 0.90f);
        bind(TEX_GOLD);
        drawCylinder(0.38f, 0.38f, 0.08f, 16, 0f, 1.00f, 0f, 0.92f, 0.78f, 0.36f);

        // Console ocular iluminado voltado para os lados
        bind(TEX_DARK);
        drawBox(-0.28f, 0.85f, 0.25f, 0.28f, 1.32f, 0.56f, 0.28f, 0.32f, 0.38f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        drawBox(-0.20f, 0.96f, 0.55f, 0.20f, 1.24f, 0.58f, 0.18f, 0.88f, 1.00f);
        // Lente ocular brilhante
        drawBox(-0.07f, 1.32f, 0.32f, 0.07f, 1.48f, 0.46f, 0.30f, 0.92f, 1.00f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Coluna ótica subindo até o domo no telhado (y = 1.4 .. 5.2)
        bind(TEX_DARK);
        drawCylinder(0.26f, 0.24f, 3.90f, 16, 0f, 1.40f, 0f, 0.42f, 0.46f, 0.52f);
        bind(TEX_GOLD);
        drawCylinder(0.30f, 0.30f, 0.14f, 16, 0f, 2.80f, 0f, 0.88f, 0.72f, 0.32f);
        drawCylinder(0.32f, 0.32f, 0.16f, 16, 0f, 5.15f, 0f, 0.88f, 0.72f, 0.32f);

        // ------------------------------------------------------------------
        // 2. TELESCÓPIO GIGANTE ("LUPA") ACIMA DO DOMO APONTANDO PARA O CÉU
        // ------------------------------------------------------------------
        float panYaw = (float) Math.sin(t * 0.007f) * 34.0f + t * 0.09f;
        float tiltPitch = 36.0f + (float) Math.sin(t * 0.011f) * 7.5f;

        GL11.glTranslatef(0f, 5.45f, 0f);
        GL11.glRotatef(panYaw, 0f, 1f, 0f);

        // Montagem equatorial (garfo pesado em U)
        bind(TEX_DARK);
        drawCylinder(0.58f, 0.52f, 0.40f, 16, 0f, 0f, 0f, 0.38f, 0.42f, 0.48f);
        drawBox(-0.92f, 0.38f, -0.22f, -0.70f, 1.65f, 0.22f, 0.44f, 0.48f, 0.54f);
        drawBox(0.70f, 0.38f, -0.22f, 0.92f, 1.65f, 0.22f, 0.44f, 0.48f, 0.54f);
        drawBox(-0.76f, 0.38f, -0.20f, 0.76f, 0.58f, 0.20f, 0.36f, 0.40f, 0.46f);

        // Eixo de elevação e tubo gigante da lupa
        GL11.glTranslatef(0f, 1.48f, 0f);
        GL11.glRotatef(tiltPitch, 1f, 0f, 0f);

        // Contrapeso traseiro (para baixo / -Y ao longo do tubo)
        bind(TEX_DARK);
        drawCylinder(0.56f, 0.62f, 1.10f, 20, 0f, -1.20f, 0f, 0.34f, 0.38f, 0.44f);
        bind(TEX_GOLD);
        drawCylinder(0.64f, 0.64f, 0.14f, 20, 0f, -0.10f, 0f, 0.90f, 0.74f, 0.30f);

        // Corpo principal do telescópio refrator
        bind(TEX_BODY);
        drawCylinder(0.60f, 0.72f, 2.35f, 20, 0f, 0.04f, 0f, 0.88f, 0.92f, 0.96f);

        // Anéis de focalização dourados e bocal gigante da lente objetiva ("lupa")
        bind(TEX_GOLD);
        drawCylinder(0.75f, 0.75f, 0.16f, 20, 0f, 1.35f, 0f, 0.92f, 0.76f, 0.34f);
        bind(TEX_DARK);
        drawCylinder(0.74f, 0.92f, 0.75f, 20, 0f, 2.39f, 0f, 0.32f, 0.36f, 0.42f);
        bind(TEX_GOLD);
        drawCylinder(0.95f, 0.95f, 0.14f, 20, 0f, 3.06f, 0f, 0.95f, 0.80f, 0.36f);

        // Mira auxiliar lateral (luneta buscadora)
        bind(TEX_DARK);
        drawBox(0.60f, 0.80f, -0.06f, 0.76f, 0.96f, 0.06f, 0.45f, 0.48f, 0.54f);
        bind(TEX_BODY);
        drawCylinder(0.14f, 0.16f, 1.30f, 12, 0.76f, 0.45f, 0f, 0.84f, 0.88f, 0.92f);

        // Lente objetiva azul-ciano brilhante na ponta apontada para o céu
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        drawLensDisc(0.86f, 20, 0f, 3.12f, 0f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void drawLensDisc(float r, int sides, float ox, float oy, float oz) {
        Tessellator ts = Tessellator.instance;
        ts.startDrawing(GL11.GL_TRIANGLE_FAN);
        ts.setColorRGBA_F(0.42f, 0.92f, 1.00f, 0.82f);
        ts.addVertex(ox, oy + 0.05f, oz);
        for (int i = 0; i <= sides; i++) {
            double a = Math.PI * 2.0 * i / sides;
            ts.setColorRGBA_F(0.12f, 0.56f, 0.92f, 0.88f);
            ts.addVertex(ox + Math.cos(a) * r, oy, oz + Math.sin(a) * r);
        }
        ts.draw();
    }

    private void drawCylinder(float rb, float rt, float h, int sides, float ox, float oy, float oz,
            float cr, float cg, float cb) {
        Tessellator ts = Tessellator.instance;
        ts.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2.0 * i / sides;
            double a1 = Math.PI * 2.0 * (i + 1) / sides;
            float f = 0.55f + 0.45f * Math.max(0f, (float) Math.cos((a0 + a1) * 0.5) * 0.6f + 0.4f);
            ts.setColorOpaque_F(cr * f, cg * f, cb * f);
            ts.addVertexWithUV(ox + Math.cos(a0) * rb, oy, oz + Math.sin(a0) * rb, i / (double) sides, 1);
            ts.addVertexWithUV(ox + Math.cos(a1) * rb, oy, oz + Math.sin(a1) * rb, (i + 1) / (double) sides, 1);
            ts.addVertexWithUV(ox + Math.cos(a1) * rt, oy + h, oz + Math.sin(a1) * rt, (i + 1) / (double) sides, 0);
            ts.addVertexWithUV(ox + Math.cos(a0) * rt, oy + h, oz + Math.sin(a0) * rt, i / (double) sides, 0);
        }
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