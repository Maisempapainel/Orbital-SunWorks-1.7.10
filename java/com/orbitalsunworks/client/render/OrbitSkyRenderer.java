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
 * Céu orbital: breu estrelado, Terra esférica 3D realista com camada de nuvens
 * e brilho atmosférico suave (sem triângulos na borda).
 *
 * A projeção é travada em 70° para o tamanho aparente da Terra NÃO variar
 * quando o jogador muda o FOV nas opções (enquanto entidades e blocos do
 * mundo continuam respeitando o FOV normal do jogador).
 */
@SideOnly(Side.CLIENT)
public class OrbitSkyRenderer extends IRenderHandler {

    private static final ResourceLocation EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");
    private static final ResourceLocation EARTH_CLOUDS = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth_clouds.png");

    private static final int STAR_COUNT = 380;
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
        // Trava FOV aparente da Terra e estrelas em 70° independente do FOV do jogador.
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float aspect = (float) mc.displayWidth / (float) Math.max(1, mc.displayHeight);
        Project.gluPerspective(70.0f, aspect, 0.1f, 1000.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_LIGHTING);

        // 1. Fundo preto espacial absoluto
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        drawBlackBox();

        // 2. Campo estelar pulsante com leve variação de temperatura de cor
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
            GL11.glVertex3f(STARS[i][0] * 360f, STARS[i][1] * 360f, STARS[i][2] * 360f);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

        // 3. Terra esférica 3D gigante embaixo: D=235, R=192 -> topo a -43u e curvatura realista
        float dist = 235.0f;
        float radius = 192.0f;
        float spinDeg = (float) ((t * 0.065) % 360.0);

        // 3a. Halo atmosférico contínuo atrás do limbo (zero triângulos espetados)
        drawAtmosphereHalo(radius, dist);

        // 3b. Esfera sólida texturizada da Terra com iluminação solar orbital realista
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(EARTH);
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -dist, 0f);
        GL11.glRotatef(22.0f, 0f, 0f, 1f);
        GL11.glRotatef(spinDeg, 0f, 1f, 0f);
        drawEarthSphere(radius, 44, 72, true);
        GL11.glPopMatrix();

        // 3c. Camada de nuvens girando independentemente logo acima da superfície
        mc.getTextureManager().bindTexture(EARTH_CLOUDS);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, -dist, 0f);
        GL11.glRotatef(22.0f, 0f, 0f, 1f);
        GL11.glRotatef(spinDeg * 1.38f + 42.0f, 0f, 1f, 0f);
        drawEarthSphere(radius * 1.007f, 40, 64, false);
        GL11.glPopMatrix();

        // Restaura estado OpenGL e matriz de projeção do jogador
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
    }

    private void drawBlackBox() {
        float s = 440f;
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

    /**
     * Anel contínuo ao redor da tangente exata da esfera vista da câmera (0,0,0),
     * criando um brilho atmosférico azul suave sem nenhum triângulo saltando fora.
     */
    private void drawAtmosphereHalo(float radius, float dist) {
        double tangentY = -dist + (double) (radius * radius) / dist;
        double tangentR = radius * Math.sqrt(dist * dist - radius * radius) / dist;
        double rInner = tangentR * 0.90;
        double rMid = tangentR * 1.005;
        double rOuter = tangentR * 1.075;
        int segs = 96;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2.0 * i / segs;
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            t.setColorRGBA_F(0.28f, 0.72f, 1.0f, 0.42f);
            t.addVertex(cos * rInner, tangentY, sin * rInner);
            t.setColorRGBA_F(0.32f, 0.76f, 1.0f, 0.36f);
            t.addVertex(cos * rMid, tangentY, sin * rMid);
        }
        t.draw();

        t.startDrawing(GL11.GL_QUAD_STRIP);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2.0 * i / segs;
            double cos = Math.cos(a);
            double sin = Math.sin(a);
            t.setColorRGBA_F(0.32f, 0.76f, 1.0f, 0.36f);
            t.addVertex(cos * rMid, tangentY, sin * rMid);
            t.setColorRGBA_F(0.10f, 0.45f, 0.95f, 0.0f);
            t.addVertex(cos * rOuter, tangentY, sin * rOuter);
        }
        t.draw();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void drawEarthSphere(float radius, int latSteps, int lonSteps, boolean isSurface) {
        // Vetor solar fixo em espaço local simplificado para dar volume diurno/crepuscular
        float sunX = 0.58f, sunY = 0.72f, sunZ = 0.38f;
        Tessellator t = Tessellator.instance;
        for (int lat = 0; lat < latSteps; lat++) {
            double v0 = (double) lat / latSteps;
            double v1 = (double) (lat + 1) / latSteps;
            double phi0 = (v0 - 0.5) * Math.PI;
            double phi1 = (v1 - 0.5) * Math.PI;

            double cos0 = Math.cos(phi0), sin0 = Math.sin(phi0);
            double cos1 = Math.cos(phi1), sin1 = Math.sin(phi1);

            t.startDrawing(GL11.GL_QUAD_STRIP);
            for (int lon = 0; lon <= lonSteps; lon++) {
                double u = (double) lon / lonSteps;
                double theta = u * Math.PI * 2.0;
                double ct = Math.cos(theta);
                double st = Math.sin(theta);

                float nx1 = (float) (cos1 * ct);
                float ny1 = (float) sin1;
                float nz1 = (float) (cos1 * st);
                float l1 = shadeVertex(nx1, ny1, nz1, sunX, sunY, sunZ, isSurface);
                if (isSurface) {
                    t.setColorRGBA_F(l1 * 0.96f, l1 * 0.98f, Math.min(1f, l1 * 1.04f), 1.0f);
                } else {
                    t.setColorRGBA_F(0.95f * l1, 0.98f * l1, 1.0f * l1, 0.85f);
                }
                t.addVertexWithUV(nx1 * radius, ny1 * radius, nz1 * radius, u, v1);

                float nx0 = (float) (cos0 * ct);
                float ny0 = (float) sin0;
                float nz0 = (float) (cos0 * st);
                float l0 = shadeVertex(nx0, ny0, nz0, sunX, sunY, sunZ, isSurface);
                if (isSurface) {
                    t.setColorRGBA_F(l0 * 0.96f, l0 * 0.98f, Math.min(1f, l0 * 1.04f), 1.0f);
                } else {
                    t.setColorRGBA_F(0.95f * l0, 0.98f * l0, 1.0f * l0, 0.85f);
                }
                t.addVertexWithUV(nx0 * radius, ny0 * radius, nz0 * radius, u, v0);
            }
            t.draw();
        }
    }

    private float shadeVertex(float nx, float ny, float nz, float sx, float sy, float sz, boolean isSurface) {
        float dot = nx * sx + ny * sy + nz * sz;
        float sun = Math.max(0f, dot);
        float ambient = isSurface ? 0.42f : 0.48f;
        return Math.min(1.0f, ambient + sun * 0.62f);
    }
}