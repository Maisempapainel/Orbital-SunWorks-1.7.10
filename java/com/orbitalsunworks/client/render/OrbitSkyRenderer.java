package com.orbitalsunworks.client.render;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.client.IRenderHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Céu da dimensão orbital: fundo preto puro, estrelas pulsantes e o planeta
 * Terra bem grande e próximo abaixo do jogador, sem triângulos na borda.
 */
@SideOnly(Side.CLIENT)
public class OrbitSkyRenderer extends IRenderHandler {

    private static final ResourceLocation EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");

    private static final int STAR_COUNT = 320;
    private static final float[][] STARS = new float[STAR_COUNT][4];

    static {
        Random r = new Random(10842);
        for (int i = 0; i < STAR_COUNT; i++) {
            double theta = r.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * r.nextDouble() - 1);
            STARS[i][0] = (float) (Math.sin(phi) * Math.cos(theta));
            STARS[i][1] = (float) (Math.cos(phi));
            STARS[i][2] = (float) (Math.sin(phi) * Math.sin(theta));
            STARS[i][3] = r.nextFloat() * 6.28f;
        }
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_LIGHTING);

        // 1. Fundo preto puro
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        drawBlackBox();

        // 2. Estrelas pulsando
        GL11.glPointSize(1.8f);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glBegin(GL11.GL_POINTS);
        long t = world.getTotalWorldTime() + (long) (partialTicks * 100f);
        for (int i = 0; i < STAR_COUNT; i++) {
            float tw = 0.35f + 0.65f * (0.5f + 0.5f
                * (float) Math.sin(t * 0.03 + STARS[i][3]));
            GL11.glColor4f(tw, tw, tw, 1f);
            GL11.glVertex3f(STARS[i][0] * 185f, STARS[i][1] * 185f, STARS[i][2] * 185f);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

        // 3. Terra maior (~30%+ aparente) e mais próxima, perfeitamente redonda
        // e sem a borda de triângulos azuis.
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(EARTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        drawEarthDisc(135f, 95f, world, partialTicks);

        // Restaura estado GL
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawBlackBox() {
        float s = 220f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        GL11.glVertex3f(-s, -s, -s); GL11.glVertex3f(s, -s, -s);
        GL11.glVertex3f(s, s, -s); GL11.glVertex3f(-s, s, -s);
        GL11.glVertex3f(-s, -s, s); GL11.glVertex3f(s, -s, s);
        GL11.glVertex3f(s, s, s); GL11.glVertex3f(-s, s, s);
        GL11.glVertex3f(-s, -s, -s); GL11.glVertex3f(-s, s, -s);
        GL11.glVertex3f(-s, s, s); GL11.glVertex3f(-s, -s, s);
        GL11.glVertex3f(s, -s, -s); GL11.glVertex3f(s, s, -s);
        GL11.glVertex3f(s, s, s); GL11.glVertex3f(s, -s, s);
        GL11.glVertex3f(-s, -s, -s); GL11.glVertex3f(s, -s, -s);
        GL11.glVertex3f(s, -s, s); GL11.glVertex3f(-s, -s, s);
        GL11.glVertex3f(-s, s, -s); GL11.glVertex3f(s, s, -s);
        GL11.glVertex3f(s, s, s); GL11.glVertex3f(-s, s, s);
        GL11.glEnd();
    }

    private void drawEarthDisc(float radius, float dist, WorldClient world, float partialTicks) {
        int segs = 72;
        float spinDeg = ((world.getTotalWorldTime() + partialTicks) * 0.08f) % 360f;

        // Camada principal redonda da Terra (gira suavemente no lugar)
        GL11.glPushMatrix();
        GL11.glRotatef(spinDeg, 0f, 1f, 0f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(0f, -dist, 0f);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs;
            float u = 0.5f + 0.495f * (float) Math.cos(a);
            float v = 0.5f + 0.495f * (float) Math.sin(a);
            GL11.glTexCoord2f(u, v);
            GL11.glVertex3f((float) Math.cos(a) * radius, -dist, (float) Math.sin(a) * radius);
        }
        GL11.glEnd();
        GL11.glPopMatrix();

        // Camada sutil de nuvens dentro da mesma circunferência
        GL11.glPushMatrix();
        GL11.glRotatef(spinDeg * 1.45f + 35f, 0f, 1f, 0f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(0.88f, 0.93f, 1f, 0.22f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(0f, -dist + 0.4f, 0f);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs;
            float u = 0.5f + 0.49f * (float) Math.cos(a);
            float v = 0.5f + 0.49f * (float) Math.sin(a);
            GL11.glTexCoord2f(u, v);
            GL11.glVertex3f((float) Math.cos(a) * (radius * 0.995f),
                -dist + 0.4f, (float) Math.sin(a) * (radius * 0.995f));
        }
        GL11.glEnd();
        GL11.glPopMatrix();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }
}