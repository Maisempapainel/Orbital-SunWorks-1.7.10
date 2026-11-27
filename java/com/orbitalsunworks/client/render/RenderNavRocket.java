package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.rocket.EntityNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderNavRocket extends Render {

    private static final ResourceLocation TEXTURE_FLAME = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_flame.png");

    private final ModelNavRocket model = new ModelNavRocket();

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityNavRocket nav = (EntityNavRocket) entity;
        float t = nav.ticksExisted + partialTicks;
        int phase = nav.phaseOf();

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);

        if (phase == EntityNavRocket.PHASE_LIFTOFF) {
            GL11.glRotatef((float) Math.sin(t * 0.6f) * 0.5f, 0f, 0f, 1f);
        } else if (phase >= EntityNavRocket.PHASE_ASCENT) {
            GL11.glRotatef((float) Math.sin(t * 0.3f) * 1.1f, 0f, 0f, 1f);
            GL11.glRotatef((float) Math.cos(t * 0.25f) * 0.7f, 1f, 0f, 0f);
        }

        this.model.renderAll();

        if (nav.isIgnited() || phase >= EntityNavRocket.PHASE_LIFTOFF) {
            this.renderQuadFlames(phase, t);
        }

        GL11.glPopMatrix();
    }

    private void renderQuadFlames(int phase, float t) {
        float pulse = 0.88f + 0.12f * (float) Math.sin(t * 2.1f);
        float len = phase == EntityNavRocket.PHASE_COUNTDOWN ? 0.55f * pulse
            : (phase == EntityNavRocket.PHASE_WARP ? 2.6f * pulse : 1.85f * pulse);
        float rad = phase == EntityNavRocket.PHASE_COUNTDOWN ? 0.11f : 0.16f;

        this.bindTexture(TEXTURE_FLAME);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glColor4f(0.75f, 0.94f, 1.0f, 0.92f);
        float d = 0.19f;
        float[][] nozzles = { { -d, -d }, { d, -d }, { -d, d }, { d, d } };
        for (float[] nz : nozzles) {
            GL11.glPushMatrix();
            GL11.glTranslatef(nz[0], 0.0f, nz[1]);
            TessellatorHelper.renderFlameCone(rad, len);
            GL11.glPopMatrix();
        }

        if (phase == EntityNavRocket.PHASE_WARP) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            Tessellator ts = Tessellator.instance;
            ts.startDrawing(GL11.GL_TRIANGLE_FAN);
            ts.setColorRGBA_F(0.25f, 0.90f, 1.0f, 0.45f * pulse);
            ts.addVertex(0.0, 2.5, 0.0);
            for (int i = 0; i <= 16; i++) {
                double a = Math.PI * 2 * i / 16.0;
                ts.setColorRGBA_F(0.10f, 0.60f, 1.0f, 0.0f);
                ts.addVertex(Math.cos(a) * 1.15, 0.0, Math.sin(a) * 1.15);
            }
            ts.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

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