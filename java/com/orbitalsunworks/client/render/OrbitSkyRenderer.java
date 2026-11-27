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
 * Céu da dimensão orbital (v0.10.0 — REFEITO de novo, agora certo).
 *
 * O pedido do usuário: "quando chego no espaço quero ver o planeta TERRA
 * IMENSO abaixo de mim [...] e ele tem que continuar lá embaixo,
 * INDEPENDENTE de onde ele esteja".
 *
 * A regra de ouro do renderSky do 1.7.10: a matriz já vem com a rotação
 * da CÂMERA aplicada (o vanilla desenha sol/lua/estrelas a partir daqui).
 * Qualquer coisa desenhada aqui segue o OLHAR do jogador — é o que
 * queremos: a Terra fica 90° abaixo do horizonte VISUAL, ou seja, olhou
 * pra baixo em QUALQUER direção, vê o planeta.
 *
 * Versões antigas falhavam porque:
 *  1. compensavam o celestialAngle com uma rotação extra (a Terra girava
 *     pra fora da tela);
 *  2. desenhavam com o estado GL sujo do vanilla (fog, cull, textura
 *     ligada do frame anterior).
 *
 * Agora: estado GL explícito no início E no fim, disco NO PLANO -Y da
 * câmera (sem rotação extra nenhuma), raio 100 (horizonte inteiro),
 * profundidade desligada (skybox: nunca vira "parede").
 */
@SideOnly(Side.CLIENT)
public class OrbitSkyRenderer extends IRenderHandler {

    private static final ResourceLocation EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");

    /** Estrelas fixas (posições pseudo-aleatórias estáveis) + fase de pulso. */
    private static final int STAR_COUNT = 300;
    private static final float[][] STARS = new float[STAR_COUNT][4];

    static {
        Random r = new Random(10842);
        for (int i = 0; i < STAR_COUNT; i++) {
            // Direção aleatória na esfera (evita o polo: espalha melhor).
            double theta = r.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * r.nextDouble() - 1);
            STARS[i][0] = (float) (Math.sin(phi) * Math.cos(theta));
            STARS[i][1] = (float) (Math.cos(phi));
            STARS[i][2] = (float) (Math.sin(phi) * Math.sin(theta));
            STARS[i][3] = r.nextFloat() * 6.28f; // fase do pulso
        }
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        // ------------------------------------------------------------
        // Estado GL EXPLÍCITO (o renderer antigo herdava fog/alpha do
        // frame anterior — às vezes a Terra aparecia, às vezes não).
        // ------------------------------------------------------------
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false); // céu nunca escreve no depth (skybox)
        GL11.glDisable(GL11.GL_LIGHTING);

        // ------------------------------------------------------------
        // 1. Fundo preto puro (o "breu").
        // ------------------------------------------------------------
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        drawBlackBox();

        // ------------------------------------------------------------
        // 2. Estrelas PULSANDO (brilho individual senoidal).
        // ------------------------------------------------------------
        GL11.glPointSize(1.8f);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glBegin(GL11.GL_POINTS);
        long t = world.getTotalWorldTime() + (long) (partialTicks * 100f);
        for (int i = 0; i < STAR_COUNT; i++) {
            float tw = 0.35f + 0.65f * (0.5f + 0.5f
                * (float) Math.sin(t * 0.03 + STARS[i][3]));
            GL11.glColor4f(tw, tw, tw, 1f);
            GL11.glVertex3f(STARS[i][0] * 120f, STARS[i][1] * 120f, STARS[i][2] * 120f);
        }
        GL11.glEnd();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);

        // ------------------------------------------------------------
        // 3. A TERRA IMENSA, SEMPRE no "chão" do olhar.
        //    A matriz aqui já contém a rotação da câmera: desenhar o
        //    disco no plano y=-dist o coloca 90° abaixo do horizonte em
        //    qualquer direção que o jogador olhe (o pedido literal).
        //    SEM compensação de celestialAngle (era o bug da versão
        //    anterior — a Terra girava pra fora da tela).
        // ------------------------------------------------------------
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mc.getTextureManager().bindTexture(EARTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        drawEarthDisc(100f, 105f, world, partialTicks);

        // ------------------------------------------------------------
        // Estado GL restaurado (não vaza pro resto do frame).
        // ------------------------------------------------------------
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_FOG);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    /** Caixa preta envolvente (6 faces) — o breu em qualquer direção. */
    private void drawBlackBox() {
        float s = 150f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        // -Z (fundo)
        GL11.glVertex3f(-s, -s, -s); GL11.glVertex3f(s, -s, -s);
        GL11.glVertex3f(s, s, -s); GL11.glVertex3f(-s, s, -s);
        // +Z
        GL11.glVertex3f(-s, -s, s); GL11.glVertex3f(s, -s, s);
        GL11.glVertex3f(s, s, s); GL11.glVertex3f(-s, s, s);
        // -X
        GL11.glVertex3f(-s, -s, -s); GL11.glVertex3f(-s, s, -s);
        GL11.glVertex3f(-s, s, s); GL11.glVertex3f(-s, -s, s);
        // +X
        GL11.glVertex3f(s, -s, -s); GL11.glVertex3f(s, s, -s);
        GL11.glVertex3f(s, s, s); GL11.glVertex3f(s, -s, s);
        // -Y (embaixo — coberto pela Terra depois)
        GL11.glVertex3f(-s, -s, -s); GL11.glVertex3f(s, -s, -s);
        GL11.glVertex3f(s, -s, s); GL11.glVertex3f(-s, -s, s);
        // +Y (cima)
        GL11.glVertex3f(-s, s, -s); GL11.glVertex3f(s, s, -s);
        GL11.glVertex3f(s, s, s); GL11.glVertex3f(-s, s, s);
        GL11.glEnd();
    }

    /**
     * Disco da Terra no plano y = -dist (na matriz da CÂMERA — fica
     * "embaixo" em qualquer direção que o jogador olhe).
     * Camadas: oceano/continentes (UV girando devagar), nuvens (deriva
     * própria), limbo atmosférico azul na borda.
     */
    private void drawEarthDisc(float radius, float dist, WorldClient world, float partialTicks) {
        int segs = 48;
        // Rotação lenta do planeta (UV offset) — 1 volta a cada ~6 min.
        float spin = ((world.getTotalWorldTime() + partialTicks) * 0.003f) % 1f;

        // ---- Camada 1: oceano + continentes ----
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(0f, -dist, 0f);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs;
            float u = 0.5f + 0.5f * (float) Math.cos(a);
            float v = 0.5f + 0.5f * (float) Math.sin(a);
            GL11.glTexCoord2f(u, v);
            GL11.glVertex3f((float) Math.cos(a) * radius, -dist, (float) Math.sin(a) * radius);
        }
        GL11.glEnd();

        // ---- Camada 2: nuvens (mesma textura, deslocada + aditiva) ----
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(0.85f, 0.9f, 1f, 0.35f);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glTexCoord2f(0.54f, 0.5f);
        GL11.glVertex3f(0f, -dist + 0.5f, 0f);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs;
            float u = 0.5f + 0.42f * (float) Math.cos(a) + 0.04f + spin * 0.5f;
            float v = 0.5f + 0.42f * (float) Math.sin(a);
            GL11.glTexCoord2f(u, v);
            GL11.glVertex3f((float) Math.cos(a) * (radius * 0.98f),
                -dist + 0.5f, (float) Math.sin(a) * (radius * 0.98f));
        }
        GL11.glEnd();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // ---- Camada 3: limbo atmosférico (anel azul na borda) ----
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs;
            float ca = (float) Math.cos(a), sa = (float) Math.sin(a);
            // Borda do disco: azul claro forte.
            GL11.glColor4f(0.35f, 0.6f, 1f, 0.8f);
            GL11.glVertex3f(ca * radius, -dist - 0.1f, sa * radius);
            // Anel externo: esmaece pra transparente.
            GL11.glColor4f(0.3f, 0.5f, 1f, 0f);
            GL11.glVertex3f(ca * (radius + 16f), -dist - 0.1f, sa * (radius + 16f));
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }
}
