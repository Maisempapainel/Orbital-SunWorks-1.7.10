package com.orbitalsunworks.client.render;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.rocket.EntityRocket;

/**
 * Renderizador do foguete em lançamento (entidade). Usa o mesmo ModelRocket
 * do TESR — o foguete que sobe é idêntico ao que estava na plataforma, no
 * MESMO lugar (centro do bloco). A chama é um cone emissivo sob o motor.
 */
public class RenderRocket extends net.minecraft.client.renderer.entity.Render {

    private static final ResourceLocation TEXTURE_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_body.png");
    private static final ResourceLocation TEXTURE_FLAME = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_flame.png");

    private final ModelRocket model = new ModelRocket();

    @Override
    public void doRender(Entity entity, double x, double y, double z, float yaw, float partialTicks) {
        EntityRocket rocket = (EntityRocket) entity;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        // Balanço leve durante a ascensão.
        if (rocket.phaseOf() == EntityRocket.PHASE_ASCENT) {
            float sway = (float) Math.sin((rocket.ticksExisted + partialTicks) * 0.25f);
            GL11.glRotatef(sway * 0.8f, 0f, 0f, 1f);
            GL11.glRotatef((float) Math.cos((rocket.ticksExisted + partialTicks) * 0.2f) * 0.5f, 1f, 0f, 0f);
        }

        this.model.renderAll();

        // Chama do motor: desde a ignição (ainda parado no pad) até o fim.
        if (rocket.isIgnited()) {
            this.renderFlame(rocket, partialTicks);
        }

        GL11.glPopMatrix();
    }

    /** Chama animada: cone emissivo pulsante (curto na ignição, longo na subida). */
    private void renderFlame(EntityRocket rocket, float partialTicks) {
        float pulse = 0.85f + 0.15f * (float) Math.sin((rocket.ticksExisted + partialTicks) * 1.7f);
        float len;
        switch (rocket.phaseOf()) {
            case EntityRocket.PHASE_COUNTDOWN:
                len = 0.55f * pulse; // aquecendo no pad
                break;
            case EntityRocket.PHASE_LIFTOFF:
                len = 1.3f * pulse;
                break;
            default:
                len = 1.7f * pulse; // ascensão
        }

        this.bindTexture(TEXTURE_FLAME);
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);

        TessellatorHelper.renderFlameCone(0.24f, len);

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
