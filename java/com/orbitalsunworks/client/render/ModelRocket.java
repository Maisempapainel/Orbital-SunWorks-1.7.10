package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * Modelo 3D do Foguete Tier 1, desenhado proceduralmente com o Tessellator
 * (sem arquivo de modelo externo). Usado pelo TESR (foguete na plataforma)
 * e pelo render da entidade (foguete em lançamento).
 *
 * Sombreamento "3D de verdade": iluminação desligada e cor por face
 * (setores do cilindro sombreados conforme o ângulo em relação à luz),
 * como um modelo techne — nada de faces pretas/invisíveis.
 *
 * Dimensões (cabe em 1x1x3 blocos):
 * - Sino do motor: 0.00-0.30 (cilindro alargando para baixo + boca escura);
 * - Corpo: 0.30-2.50 (12 lados, raio 0.38, janela na textura);
 * - 4 aletas diagonais (45°+90°*i), vermelhas, y 0.15-1.05;
 * - Nariz: 2.50-3.00 (cone).
 */
public class ModelRocket {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_body.png");
    private static final ResourceLocation TEX_NOSE = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_nose.png");
    private static final ResourceLocation TEX_FIN = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_fin.png");
    private static final ResourceLocation TEX_ENGINE = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");

    /** Direção da luz (ângulo no plano XZ) para o sombreamento. */
    private static final double LIGHT_ANGLE = Math.PI / 4.0;

    /**
     * Desenha o foguete inteiro. Chamar com a matriz já transladada para a
     * posição do bloco base (0,0,0 = chão sob o foguete).
     */
    public void renderAll() {
        // Sem cull (winding inconsistente não faz buraco) e sem iluminação
        // do Minecraft (o sombreamento vem das cores por face).
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        // Motor: sino alargando para baixo + boca escura.
        bind(TEX_ENGINE);
        this.renderCylinder(0.26f, 0.16f, 0.30f, 10, 0f, 0f, 0f);
        this.renderDisc(0.26f, 10, 0f, 0f, 0f, false, 70);

        // Corpo + tampas.
        bind(TEX_BODY);
        this.renderDisc(0.38f, 12, 0f, 0.30f, 0f, true, 230);
        this.renderCylinder(0.38f, 0.38f, 2.20f, 12, 0f, 0.30f, 0f);
        this.renderDisc(0.38f, 12, 0f, 2.50f, 0f, true, 255);

        // 4 aletas nas diagonais (não tampam a janela da frente).
        bind(TEX_FIN);
        for (int i = 0; i < 4; i++) {
            GL11.glPushMatrix();
            GL11.glRotatef(45f + i * 90f, 0f, 1f, 0f);
            this.renderFin();
            GL11.glPopMatrix();
        }

        // Nariz.
        bind(TEX_NOSE);
        this.renderCone(0.38f, 0.50f, 12, 0f, 2.50f, 0f);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    /** Sombreamento de um ângulo médio de face cilíndrica (0..255). */
    private int shadeFor(double angle) {
        double d = Math.cos(angle - LIGHT_ANGLE);
        float f = 0.60f + 0.40f * (float) Math.max(0.0, d);
        return (int) (f * 255);
    }

    /** Cilindro vertical com quads texturizados e sombreamento por setor. */
    private void renderCylinder(float rBottom, float rTop, float h, int sides, float ox, float oy, float oz) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = (Math.PI * 2 * i) / sides;
            double a1 = (Math.PI * 2 * (i + 1)) / sides;
            int c = this.shadeFor((a0 + a1) / 2.0);
            t.setColorOpaque(c, c, c);
            t.addVertexWithUV(ox + Math.cos(a0) * rBottom, oy, oz + Math.sin(a0) * rBottom, i / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * rBottom, oy, oz + Math.sin(a1) * rBottom, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * rTop, oy + h, oz + Math.sin(a1) * rTop, (i + 1) / (double) sides, 0);
            t.addVertexWithUV(ox + Math.cos(a0) * rTop, oy + h, oz + Math.sin(a0) * rTop, i / (double) sides, 0);
        }
        t.draw();
    }

    /** Disco (tampa) no plano XZ com sombreamento fixo. */
    private void renderDisc(float r, int sides, float ox, float oy, float oz, boolean up, int shade) {
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque(shade, shade, shade);
        t.addVertexWithUV(ox, oy, oz, 0.5, 0.5);
        for (int i = 0; i <= sides; i++) {
            double a = (Math.PI * 2 * i) / sides * (up ? 1 : -1);
            t.addVertexWithUV(ox + Math.cos(a) * r, oy, oz + Math.sin(a) * r,
                0.5 + Math.cos(a) * 0.5, 0.5 + Math.sin(a) * 0.5);
        }
        t.draw();
    }

    /** Cone com base em oy e ápice em oy+h (sombreado por setor). */
    private void renderCone(float r, float h, int sides, float ox, float oy, float oz) {
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < sides; i++) {
            double a0 = (Math.PI * 2 * i) / sides;
            double a1 = (Math.PI * 2 * (i + 1)) / sides;
            int c = this.shadeFor((a0 + a1) / 2.0);
            t.setColorOpaque(c, c, c);
            t.addVertexWithUV(ox + Math.cos(a0) * r, oy, oz + Math.sin(a0) * r, i / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * r, oy, oz + Math.sin(a1) * r, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(ox, oy + h, oz, (i + 0.5) / (double) sides, 0);
        }
        t.draw();
    }

    /** Uma aleta trapezoidal no eixo +X (chamar dentro de glRotate). */
    private void renderFin() {
        Tessellator t = Tessellator.instance;
        float r = 0.34f;   // raiz no corpo
        float w = 0.72f;   // ponta para fora
        float y0 = 0.15f;  // borda inferior (recuada na ponta)
        float y1 = 1.05f;  // topo da raiz
        float z = 0.045f;  // meia-espessura

        t.startDrawingQuads();
        // Face +Z (iluminada)
        t.setColorOpaque(210, 210, 210);
        t.addVertexWithUV(r, y0 + 0.2f, z, 0, 0);
        t.addVertexWithUV(w, y0, z, 1, 0);
        t.addVertexWithUV(w, y1 - 0.15f, z, 1, 1);
        t.addVertexWithUV(r, y1, z, 0, 1);
        // Face -Z (sombra)
        t.setColorOpaque(120, 120, 120);
        t.addVertexWithUV(r, y0 + 0.2f, -z, 0, 0);
        t.addVertexWithUV(r, y1, -z, 0, 1);
        t.addVertexWithUV(w, y1 - 0.15f, -z, 1, 1);
        t.addVertexWithUV(w, y0, -z, 1, 0);
        // Borda externa
        t.setColorOpaque(170, 170, 170);
        t.addVertexWithUV(w, y0, -z, 0, 0);
        t.addVertexWithUV(w, y0, z, 0, 1);
        t.addVertexWithUV(w, y1 - 0.15f, z, 1, 1);
        t.addVertexWithUV(w, y1 - 0.15f, -z, 1, 0);
        // Borda superior (da raiz até a ponta)
        t.setColorOpaque(230, 230, 230);
        t.addVertexWithUV(r, y1, -z, 0, 0);
        t.addVertexWithUV(r, y1, z, 0, 1);
        t.addVertexWithUV(w, y1 - 0.15f, z, 1, 1);
        t.addVertexWithUV(w, y1 - 0.15f, -z, 1, 0);
        t.draw();
    }

    private void bind(ResourceLocation tex) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}
