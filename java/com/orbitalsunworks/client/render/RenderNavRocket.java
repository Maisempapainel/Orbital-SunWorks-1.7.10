package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Render da EntityNavRocket (foguete de navegação, v0.8.0).
 *
 * Dois estados visuais:
 *  - EM VOO (countdown→warp): o ModelRocket em escala 1.2x com o
 *    balanço da subida (o jogador montado é renderizado pelo vanilla);
 *  - ESTACIONADA na órbita (parked): a nave flutua com bob suave, uma
 *    LUZ DE MOTOR pulsando embaixo (o farol de "esperando o retorno")
 *    e um leve giro — ela é o ponto de referência do jogador.
 */
@SideOnly(Side.CLIENT)
public class RenderNavRocket extends Render {

    private final ModelRocket model = new ModelRocket();

    @Override
    public void doRender(Entity entity, double x, double y, double z,
            float yaw, float partialTicks) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y, (float) z);
        com.orbitalsunworks.rocket.EntityNavRocket nav =
            (com.orbitalsunworks.rocket.EntityNavRocket) entity;
        float phase = nav.phaseOf();
        if (phase == com.orbitalsunworks.rocket.EntityNavRocket.PHASE_PARKED) {
            // Estacionada: gira devagar (vida na órbita).
            GL11.glRotatef((entity.ticksExisted + partialTicks) * 0.5f % 360f, 0f, 1f, 0f);
        } else if (phase >= com.orbitalsunworks.rocket.EntityNavRocket.PHASE_LIFTOFF) {
            GL11.glRotatef((float) (Math.sin(entity.ticksExisted * 0.3) * 1.5), 0f, 0f, 1f);
        }
        GL11.glScalef(1.2f, 1.2f, 1.2f);
        this.model.renderAll();
        GL11.glPopMatrix();

        // Luz de engine quando estacionada (glow pulsante embaixo).
        if (phase == com.orbitalsunworks.rocket.EntityNavRocket.PHASE_PARKED) {
            float pulse = 0.5f + 0.5f
                * (float) Math.sin((entity.ticksExisted + partialTicks) * 0.15);
            GL11.glPushMatrix();
            GL11.glTranslatef((float) x, (float) y, (float) z);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // aditivo: brilha
            net.minecraft.client.renderer.Tessellator t =
                net.minecraft.client.renderer.Tessellator.instance;
            float r = 0.35f + pulse * 0.15f;
            t.startDrawing(GL11.GL_TRIANGLE_FAN);
            t.setColorRGBA_F(0.4f, 0.9f, 1.0f, 0.55f * pulse + 0.2f);
            t.addVertex(0, -0.15, 0);
            for (int i = 0; i <= 12; i++) {
                double a = Math.PI * 2 * i / 12;
                t.setColorRGBA_F(0.2f, 0.6f, 1.0f, 0f);
                t.addVertex(Math.cos(a) * r, -0.05, Math.sin(a) * r);
            }
            t.draw();
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return null; // texturas binds internas do modelo
    }
}
