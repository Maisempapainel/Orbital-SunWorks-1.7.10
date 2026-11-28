package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.rocket.EntityDispersionRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderiza o Foguete Dispersor em voo com chama de plasma laranja/ciano.
 */
@SideOnly(Side.CLIENT)
public class RenderDispersionRocket extends Render {

    private static final ResourceLocation TEXTURE_FLAME = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_flame.png");

    private final ModelDispersionRocket model = new ModelDispersionRocket();

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityDispersionRocket rocket = (EntityDispersionRocket) entity;
        float t = rocket.ticksExisted + partialTicks;
        int phase = rocket.phaseOf();

        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);
        if (phase >= EntityDispersionRocket.PHASE_ASCENT) {
            GL11.glRotatef((float) Math.sin(t * 0.4f) * 0.9f, 0f, 0f, 1f);
            GL11.glRotatef((float) Math.cos(t * 0.35f) * 0.7f, 1f, 0f, 0f);
        } else {
            GL11.glRotatef((float) Math.sin(t * 0.8f) * 0.4f, 0f, 0f, 1f);
        }

        GL11.glScalef(TileDispersionRocketRenderer.SCALE_XZ,
            TileDispersionRocketRenderer.SCALE_Y,
            TileDispersionRocketRenderer.SCALE_XZ);
        this.model.renderAll();

        if (rocket.isIgnited()) {
            this.renderExhaust(phase, t);
        }

        GL11.glPopMatrix();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
    }

    private void renderExhaust(int phase, float t) {
        float pulse = 0.88f + 0.12f * (float) Math.sin(t * 2.4f);
        float len = (phase >= EntityDispersionRocket.PHASE_ASCENT ? 2.6f : 0.85f) * pulse;
        float rad = phase >= EntityDispersionRocket.PHASE_ASCENT ? 0.25f : 0.16f;

        this.bindTexture(TEXTURE_FLAME);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glColor4f(1.0f, 0.72f, 0.32f, 0.94f);
        TessellatorHelper.renderFlameCone(rad, len);

        GL11.glColor4f(0.45f, 0.90f, 1.0f, 0.70f);
        TessellatorHelper.renderFlameCone(rad * 0.52f, len * 0.68f);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null;
    }
}