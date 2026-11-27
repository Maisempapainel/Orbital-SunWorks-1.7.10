package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.rocket.EntityRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRocket extends net.minecraft.client.renderer.entity.Render {

    private static final ResourceLocation TEXTURE_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_body.png");
    private static final ResourceLocation TEXTURE_FLAME = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_flame.png");

    private final ModelRocket model = new ModelRocket();

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityRocket rocket = (EntityRocket) entity;
        float t = rocket.ticksExisted + partialTicks;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        if (rocket.phaseOf() == EntityRocket.PHASE_LIFTOFF) {
            GL11.glRotatef((float) Math.sin(t * 0.65f) * 0.45f, 0f, 0f, 1f);
            GL11.glRotatef((float) Math.cos(t * 0.55f) * 0.45f, 1f, 0f, 0f);
        } else if (rocket.phaseOf() == EntityRocket.PHASE_ASCENT) {
            GL11.glRotatef((float) Math.sin(t * 0.25f) * 0.9f, 0f, 0f, 1f);
            GL11.glRotatef((float) Math.cos(t * 0.20f) * 0.6f, 1f, 0f, 0f);
        }

        this.model.renderAll();

        if (rocket.isIgnited()) {
            this.renderFlame(rocket, partialTicks);
        }

        GL11.glPopMatrix();
    }

    private void renderFlame(EntityRocket rocket, float partialTicks) {
        float t = rocket.ticksExisted + partialTicks;
        float pulse = 0.88f + 0.12f * (float) Math.sin(t * 1.9f);
        float len;
        float rad;
        switch (rocket.phaseOf()) {
            case EntityRocket.PHASE_COUNTDOWN:
                len = 0.75f * pulse;
                rad = 0.32f;
                break;
            case EntityRocket.PHASE_LIFTOFF:
                len = 2.15f * pulse;
                rad = 0.42f;
                break;
            default:
                len = 2.95f * pulse;
                rad = 0.46f;
        }

        this.bindTexture(TEXTURE_FLAME);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glColor4f(1.0f, 0.92f, 0.78f, 0.95f);
        TessellatorHelper.renderFlameCone(rad, len);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator ts = Tessellator.instance;
        float halo = rad * 1.45f;
        ts.startDrawing(GL11.GL_TRIANGLE_FAN);
        ts.setColorRGBA_F(1.0f, 0.75f, 0.30f, 0.60f * pulse);
        ts.addVertex(0.0, -0.05, 0.0);
        for (int i = 0; i <= 14; i++) {
            double a = Math.PI * 2 * i / 14.0;
            ts.setColorRGBA_F(1.0f, 0.35f, 0.05f, 0.0f);
            ts.addVertex(Math.cos(a) * halo, -0.10, Math.sin(a) * halo);
        }
        ts.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TEXTURE_BODY;
    }
}