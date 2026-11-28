package com.orbitalsunworks.client.render;

import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Skybox orbital com:
 *  - FOV travado em 70° independente da configuração do jogador (Quake Pro / FOV 30 não altera a Terra)
 *  - Planeta Terra grande e próximo logo abaixo da órbita (dist=215, radius=178)
 */
@SideOnly(Side.CLIENT)
public class OrbitSkyRenderer extends IRenderHandler {

    private static final ResourceLocation EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");
    private static final ResourceLocation EARTH_CLOUDS = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth_clouds.png");

    private static final int STAR_COUNT = 420;
    private static final float[][] STARS = new float[STAR_COUNT][5];

    static {
        Random r = new Random(10842L);
        for (int i = 0; i < STAR_COUNT; i++) {
            double theta = r.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * r.nextDouble() - 1.0);
            STARS[i][0] = (float) (Math.sin(phi) * Math.cos(theta));
            STARS[i][1] = (float) Math.cos(phi);
            STARS[i][2] = (float) (Math.sin(phi) * Math.sin(theta));
            STARS[i][3] = r.nextFloat() * 6.283f;
            STARS[i][4] = r.nextFloat();
        }
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        // Trava FOV aparente da Terra e estrelas em 70° independente do FOV do jogador!
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float aspect = (float) mc.displayWidth / (float) Math.max(1, mc.displayHeight);
        Project.gluPerspective(70.0f, aspect, 0.1f, 1200.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_LIGHTING);

        // 1. Espaço negro profundo
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        drawBlackBox();

        // 2. Estrelas
        GL11.glPointSize(1.85f);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glBegin(GL11.GL_POINTS);
        double t = world.getTotalWorldTime() + partialTicks;
        for (int i = 0; i < STAR_COUNT; i++) {
            float tw = 0.42f + 0.58f * (0.5f + 0.5f * (float) Math.sin(t * 0.035 + STARS[i][3]));
            float tint = STARS[i][4];
            float r = tint < 0.25f ? 0.80f : 1.0f;
            float g = tint > 0.85f ? 0.88f : 0.96f;
            float b = tint > 0.85f ? 0.72f : 1.0f;
            GL11.glColor4f(tw * r, tw * g, tw * b, 1f);
            GL11.glVertex3f(STARS[i][0] * 380f, STARS[i][1] * 380f, STARS[i][2] * 380f);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

        // 3. Terra grande e bem próxima abaixo da estação (50%+ maior aparente)
        float dist = 215.0f;
        float radius = 178.0f;
        float spinDeg = (float) ((t * 0.055) % 360.0);

        drawAtmosphereHalo(radius, dist);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(EARTH);
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -dist, 0f);
        GL11.glRotatef(22.0f, 0f, 0f, 1f);
        GL11.glRotatef(spinDeg, 0f, 1f, 0f);
        drawEarthSphere(radius, 52, 88, true);
        GL11.glPopMatrix();

        mc.getTextureManager().bindTexture(EARTH_CLOUDS);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -dist, 0f);
        GL11.glRotatef(22.0f, 0f, 0f, 1f);
        GL11.glRotatef(spinDeg * 1.35f + 42.0f, 0f, 1f, 0f);
        drawEarthSphere(radius * 1.006f, 44, 72, false);
        GL11.glPopMatrix();

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void drawBlackBox() {
        float s = 480f;
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.addVertex(-s, -s, -s); tess.addVertex(s, -s, -s); tess.addVertex(s, -s, s); tess.addVertex(-s, -s, s);
        tess.addVertex(-s, s, s); tess.addVertex(s, s, s); tess.addVertex(s, s, -s); tess.addVertex(-s, s, -s);
        tess.addVertex(-s, -s, -s); tess.addVertex(-s, s, -s); tess.addVertex(s, s, -s); tess.addVertex(s, -s, -s);
        tess.addVertex(s, -s, s); tess.addVertex(s, s, s); tess.addVertex(-s, s, s); tess.addVertex(-s, -s, s);
        tess.addVertex(-s, -s, s); tess.addVertex(-s, s, s); tess.addVertex(-s, s, -s); tess.addVertex(-s, -s, -s);
        tess.addVertex(s, -s, -s); tess.addVertex(s, s, -s); tess.addVertex(s, s, s); tess.addVertex(s, -s, s);
        tess.draw();
    }

    private void drawAtmosphereHalo(float radius, float dist) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        Tessellator tess = Tessellator.instance;
        int segs = 72;
        float rInner = radius * 0.992f;
        float rOuter = radius * 1.075f;
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -dist, 0f);
        tess.startDrawing(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2.0 * i / segs;
            float cx = (float) Math.cos(a);
            float cz = (float) Math.sin(a);
            tess.setColorRGBA_F(0.24f, 0.58f, 1.0f, 0.34f);
            tess.addVertex(cx * rInner, 28f, cz * rInner);
            tess.setColorRGBA_F(0.10f, 0.36f, 0.95f, 0.0f);
            tess.addVertex(cx * rOuter, 28f, cz * rOuter);
        }
        tess.draw();
        GL11.glPopMatrix();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void drawEarthSphere(float radius, int stacks, int slices, boolean litSurface) {
        Tessellator tess = Tessellator.instance;
        for (int i = 0; i < stacks; i++) {
            double phi0 = Math.PI * i / stacks;
            double phi1 = Math.PI * (i + 1) / stacks;
            float v0 = (float) i / stacks;
            float v1 = (float) (i + 1) / stacks;
            tess.startDrawing(GL11.GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                double th = 2.0 * Math.PI * j / slices;
                float u = (float) j / slices;
                float x0 = (float) (Math.sin(phi0) * Math.cos(th));
                float y0 = (float) Math.cos(phi0);
                float z0 = (float) (Math.sin(phi0) * Math.sin(th));
                float x1 = (float) (Math.sin(phi1) * Math.cos(th));
                float y1 = (float) Math.cos(phi1);
                float z1 = (float) (Math.sin(phi1) * Math.sin(th));

                if (litSurface) {
                    float l0 = Math.max(0.18f, 0.44f + 0.56f * (y0 * 0.65f + x0 * 0.55f));
                    float l1 = Math.max(0.18f, 0.44f + 0.56f * (y1 * 0.65f + x1 * 0.55f));
                    tess.setColorRGBA_F(l0, l0, Math.min(1f, l0 * 1.04f), 1.0f);
                    tess.addVertexWithUV(x0 * radius, y0 * radius, z0 * radius, u, v0);
                    tess.setColorRGBA_F(l1, l1, Math.min(1f, l1 * 1.04f), 1.0f);
                    tess.addVertexWithUV(x1 * radius, y1 * radius, z1 * radius, u, v1);
                } else {
                    tess.setColorRGBA_F(1f, 1f, 1f, 0.90f);
                    tess.addVertexWithUV(x0 * radius, y0 * radius, z0 * radius, u, v0);
                    tess.addVertexWithUV(x1 * radius, y1 * radius, z1 * radius, u, v1);
                }
            }
            tess.draw();
        }
    }
}