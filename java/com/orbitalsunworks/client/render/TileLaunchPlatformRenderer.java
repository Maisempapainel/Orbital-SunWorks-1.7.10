package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.OSBlocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileLaunchPlatformRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEX_METAL = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");

    private static final float LX = 0.42f, LY = 0.82f, LZ = 0.38f;

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        int meta = tile.getBlockMetadata();
        if (meta == 1) {
            return;
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (meta == 0) {
            renderSingleTile();
        } else if (meta == 2) {
            boolean hasRocket = false;
            World w = tile.getWorldObj();
            if (w != null) {
                net.minecraft.block.Block above = w.getBlock(tile.xCoord, tile.yCoord + 1, tile.zCoord);
                hasRocket = (above == OSBlocks.blockRocket || above == OSBlocks.blockNavRocket);
            }
            float time = w != null ? (w.getTotalWorldTime() + partialTicks) : 0f;
            GL11.glTranslatef(0.5f, 0f, 0.5f);
            renderAssembled7x7(hasRocket, time);
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    public static void renderSingleTile() {
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX_DARK);
        drawTaperedPad(0.02f, 0.0f, 0.02f, 0.98f, 0.14f, 0.98f,
            0.06f, 0.14f, 0.06f, 0.94f, 0.24f, 0.94f,
            0.40f, 0.44f, 0.50f, 0.22f, 0.24f, 0.28f);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TEX_METAL);
        drawBox(0.10f, 0.24f, 0.10f, 0.90f, 0.27f, 0.90f,
            0.84f, 0.88f, 0.93f, 0.36f, 0.40f, 0.46f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawGlowBox(0.38f, 0.271f, 0.38f, 0.62f, 0.28f, 0.62f, 0.18f, 0.72f, 0.90f, 1.0f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void renderAssembled7x7(boolean hasRocket, float time) {
        Minecraft mc = Minecraft.getMinecraft();

        mc.getTextureManager().bindTexture(TEX_DARK);
        renderOctagonBevel(3.48f, 2.95f, 0.0f, 0.24f, 0.42f, 0.46f, 0.52f);

        mc.getTextureManager().bindTexture(TEX_METAL);
        renderOctagonDeck(2.95f, 0.24f, 0.84f, 0.88f, 0.93f);

        mc.getTextureManager().bindTexture(TEX_DARK);
        renderRingCylinder(2.15f, 2.22f, 0.03f, 16, 0.24f, 0.46f, 0.50f, 0.56f);
        mc.getTextureManager().bindTexture(TEX_METAL);
        renderOctagonDeck(1.75f, 0.265f, 0.78f, 0.82f, 0.88f);

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(i * 90f, 0f, 1f, 0f);
            mc.getTextureManager().bindTexture(TEX_DARK);
            drawBox(-0.42f, 0.16f, 0.95f, 0.42f, 0.275f, 2.92f,
                0.24f, 0.26f, 0.30f, 0.12f, 0.13f, 0.16f);
            mc.getTextureManager().bindTexture(TEX_METAL);
            for (float gz = 1.15f; gz <= 2.75f; gz += 0.32f) {
                drawBox(-0.36f, 0.275f, gz - 0.04f, 0.36f, 0.305f, gz + 0.04f,
                    0.72f, 0.76f, 0.82f, 0.34f, 0.36f, 0.40f);
            }
            GL11.glPopMatrix();
        }

        mc.getTextureManager().bindTexture(TEX_DARK);
        renderRingCylinder(1.30f, 0.96f, 0.38f, 16, 0.26f, 0.42f, 0.46f, 0.52f);
        mc.getTextureManager().bindTexture(TEX_METAL);
        renderRingCylinder(0.96f, 0.84f, 0.36f, 16, 0.64f, 0.84f, 0.88f, 0.94f);

        mc.getTextureManager().bindTexture(TEX_DARK);
        renderFlatRing(0.50f, 0.86f, 16, 1.00f, 0.56f, 0.60f, 0.66f);
        renderDisc(0.50f, 16, 0f, 0.72f, 0f, true, 0.14f, 0.15f, 0.18f);
        renderRingCylinder(0.50f, 0.50f, 0.28f, 16, 0.72f, 0.24f, 0.26f, 0.30f);

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(i * 90f, 0f, 1f, 0f);
            mc.getTextureManager().bindTexture(TEX_DARK);
            drawBox(-0.14f, 0.72f, 0.76f, 0.14f, 1.06f, 0.98f,
                0.44f, 0.48f, 0.54f, 0.20f, 0.22f, 0.26f);
            mc.getTextureManager().bindTexture(TEX_METAL);
            drawBox(-0.09f, 0.96f, 0.64f, 0.09f, 1.15f, 0.84f,
                0.86f, 0.90f, 0.95f, 0.42f, 0.45f, 0.50f);
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float pulse = 0.55f + 0.45f * (float) Math.sin(time * 0.14f);
        float lr = hasRocket ? 1.00f : 0.18f;
        float lg = hasRocket ? 0.68f : 0.85f;
        float lb = hasRocket ? 0.15f : 1.00f;

        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            drawGlowBox(-0.04f, 0.268f, 1.35f, 0.04f, 0.278f, 2.55f, lr, lg, lb, 0.75f * pulse);
            GL11.glPopMatrix();
        }

        for (int i = 0; i < 8; i++) {
            double ang = Math.PI * 2.0 * i / 8.0 + Math.PI / 8.0;
            float bx = (float) Math.cos(ang) * 2.82f;
            float bz = (float) Math.sin(ang) * 2.82f;
            drawGlowBox(bx - 0.07f, 0.24f, bz - 0.07f, bx + 0.07f, 0.29f, bz + 0.07f,
                lr, lg, lb, 0.5f + 0.5f * pulse);
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private static void renderOctagonBevel(float rOut, float rIn, float y0, float y1,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        float[] ox = octX(rOut), oz = octZ(rOut);
        float[] ix = octX(rIn), iz = octZ(rIn);
        t.startDrawingQuads();
        for (int i = 0; i < 8; i++) {
            int j = (i + 1) % 8;
            float nx = (ox[i] + ox[j]) * 0.5f;
            float nz = (oz[i] + oz[j]) * 0.5f;
            float len = (float) Math.sqrt(nx * nx + nz * nz);
            if (len > 0.001f) { nx /= len; nz /= len; }
            float f = 0.45f + 0.55f * Math.max(0f, nx * LX + 0.5f * LY + nz * LZ);
            t.setColorOpaque_F(cr * f, cg * f, cb * f);
            t.addVertexWithUV(ox[i], y0, oz[i], 0, 1);
            t.addVertexWithUV(ox[j], y0, oz[j], 1, 1);
            t.addVertexWithUV(ix[j], y1, iz[j], 1, 0);
            t.addVertexWithUV(ix[i], y1, iz[i], 0, 0);
        }
        t.draw();
    }

    private static void renderOctagonDeck(float r, float y, float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        float[] vx = octX(r), vz = octZ(r);
        float f = 0.65f + 0.35f * LY;
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque_F(cr * f, cg * f, cb * f);
        t.addVertexWithUV(0f, y, 0f, 0.5f, 0.5f);
        for (int i = 0; i <= 8; i++) {
            int k = i % 8;
            t.addVertexWithUV(vx[k], y, vz[k], 0.5f + vx[k] / (2f * r), 0.5f + vz[k] / (2f * r));
        }
        t.draw();
    }

    private static float[] octX(float r) {
        float c = r * 0.44f;
        return new float[]{ r, r, c, -c, -r, -r, -c, c };
    }

    private static float[] octZ(float r) {
        float c = r * 0.44f;
        return new float[]{ -c, c, r, r, c, -c, -r, -r };
    }

    private static void renderRingCylinder(float rb, float rt, float h, int sides, float oy,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2 * i / sides;
            double a1 = Math.PI * 2 * (i + 1) / sides;
            float am = (float) ((a0 + a1) * 0.5);
            float f = 0.42f + 0.58f * Math.max(0f, (float) Math.cos(am) * LX + (float) Math.sin(am) * LZ);
            t.setColorOpaque_F(cr * f, cg * f, cb * f);
            t.addVertexWithUV(Math.cos(a0) * rb, oy, Math.sin(a0) * rb, i / (double) sides, 1);
            t.addVertexWithUV(Math.cos(a1) * rb, oy, Math.sin(a1) * rb, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(Math.cos(a1) * rt, oy + h, Math.sin(a1) * rt, (i + 1) / (double) sides, 0);
            t.addVertexWithUV(Math.cos(a0) * rt, oy + h, Math.sin(a0) * rt, i / (double) sides, 0);
        }
        t.draw();
    }

    private static void renderFlatRing(float rIn, float rOut, int sides, float y,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        float f = 0.65f + 0.35f * LY;
        t.setColorOpaque_F(cr * f, cg * f, cb * f);
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2 * i / sides;
            double a1 = Math.PI * 2 * (i + 1) / sides;
            t.addVertexWithUV(Math.cos(a0) * rIn, y, Math.sin(a0) * rIn, 0, 0);
            t.addVertexWithUV(Math.cos(a0) * rOut, y, Math.sin(a0) * rOut, 1, 0);
            t.addVertexWithUV(Math.cos(a1) * rOut, y, Math.sin(a1) * rOut, 1, 1);
            t.addVertexWithUV(Math.cos(a1) * rIn, y, Math.sin(a1) * rIn, 0, 1);
        }
        t.draw();
    }

    private static void renderDisc(float r, int sides, float ox, float oy, float oz, boolean up,
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

    private static void drawBox(float x0, float y0, float z0, float x1, float y1, float z1,
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

    private static void drawTaperedPad(
            float bx0, float by0, float bz0, float bx1, float by0b, float bz1,
            float tx0, float ty1, float tz0, float tx1, float ty1b, float tz1,
            float lr, float lg, float lb, float dr, float dg, float db) {
        Tessellator t = Tessellator.instance;
        float f = Math.max(0f, LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(tx0, ty1, tz1, 0, 1); t.addVertexWithUV(tx1, ty1, tz1, 1, 1);
        t.addVertexWithUV(tx1, ty1, tz0, 1, 0); t.addVertexWithUV(tx0, ty1, tz0, 0, 0);
        t.draw();

        t.startDrawingQuads();
        t.setColorOpaque_F(dr, dg, db);
        t.addVertexWithUV(bx0, by0, bz0, 0, 1); t.addVertexWithUV(bx1, by0, bz0, 1, 1);
        t.addVertexWithUV(bx1, by0, bz1, 1, 0); t.addVertexWithUV(bx0, by0, bz1, 0, 0);
        t.draw();

        t.startDrawingQuads();
        f = 0.65f;
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(bx0, by0, bz1, 0, 0); t.addVertexWithUV(bx1, by0, bz1, 1, 0);
        t.addVertexWithUV(tx1, ty1, tz1, 1, 1); t.addVertexWithUV(tx0, ty1, tz1, 0, 1);

        t.addVertexWithUV(bx1, by0, bz0, 0, 0); t.addVertexWithUV(bx0, by0, bz0, 1, 0);
        t.addVertexWithUV(tx0, ty1, tz0, 1, 1); t.addVertexWithUV(tx1, ty1, tz0, 0, 1);

        t.addVertexWithUV(bx1, by0, bz1, 0, 0); t.addVertexWithUV(bx1, by0, bz0, 1, 0);
        t.addVertexWithUV(tx1, ty1, tz0, 1, 1); t.addVertexWithUV(tx1, ty1, tz1, 0, 1);

        t.addVertexWithUV(bx0, by0, bz0, 0, 0); t.addVertexWithUV(bx0, by0, bz1, 1, 0);
        t.addVertexWithUV(tx0, ty1, tz1, 1, 1); t.addVertexWithUV(tx0, ty1, tz0, 0, 1);
        t.draw();
    }

    private static void drawGlowBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b, float a) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, a);
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0);
        t.draw();
    }
}