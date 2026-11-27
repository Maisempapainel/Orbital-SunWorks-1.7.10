package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileNavRocketRenderer extends TileEntitySpecialRenderer {

    public static final float SCALE_XZ = 1.55f;
    public static final float SCALE_Y = 1.60f;

    private final ModelNavRocket model = new ModelNavRocket();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (tile.getBlockMetadata() != 0) {
            return;
        }
        boolean inOrbit = tile.getWorldObj() != null && OrbitDimensionManager.isOrbitWorld(tile.getWorldObj());
        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        if (inOrbit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        }

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glScalef(SCALE_XZ, SCALE_Y, SCALE_XZ);
        this.model.renderAll();

        if (inOrbit) {
            long time = tile.getWorldObj().getTotalWorldTime();
            float pulse = 0.55f + 0.45f * (float) Math.sin((time + partialTicks) * 0.14f);
            boolean blink = ((time + partialTicks) % 36f) < 9f;

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            Tessellator t = Tessellator.instance;
            float r = 0.56f + pulse * 0.16f;
            t.startDrawing(GL11.GL_TRIANGLE_FAN);
            t.setColorRGBA_F(0.20f, 0.90f, 1.0f, 0.48f * pulse);
            t.addVertex(0.0, -0.10, 0.0);
            for (int i = 0; i <= 16; i++) {
                double a = Math.PI * 2 * i / 16.0;
                t.setColorRGBA_F(0.10f, 0.65f, 1.0f, 0.0f);
                t.addVertex(Math.cos(a) * r, -0.02, Math.sin(a) * r);
            }
            t.draw();

            t.startDrawingQuads();
            t.setColorRGBA_F(0.20f, 0.95f, 1.0f, blink ? 0.95f : 0.30f);
            float b = 0.06f;
            t.addVertex(-b, 5.04, -b);
            t.addVertex(-b, 5.04, b);
            t.addVertex(b, 5.04, b);
            t.addVertex(b, 5.04, -b);
            t.draw();

            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();

        if (inOrbit) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        }
    }
}