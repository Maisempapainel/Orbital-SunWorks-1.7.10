package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * MODELO 3D DA CÁPSULA LANÇADORA (v0.7.0) — para a cinematic de deploy.
 *
 * Cápsula realista de liberação (como as dos lançadores reais/Ariane):
 *  - cilindro metálico (a "coifa") com anéis de reforço;
 *  - tampa superior cônica;
 *  - ABRE EM DUAS METADES que giram para trás em dobradiças na base
 *    (parâmetro open 0..1: 0 fechada, 1 escancarada);
 *  - base com o motor de apogeu (bocal pequeno).
 *
 * O interior é visível quando aberta (faces internas sombreadas) — é de
 * lá que o satélite sai na cinematic.
 */
public class ModelCapsule {

    private static final ResourceLocation TEX = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");

    private static final float[] LIGHT = {0.45f, 0.8f, 0.35f, 1.0f};

    /**
     * Desenha a cápsula.
     *
     * @param open 0 = fechada; 1 = metades escancaradas para trás.
     * @param fade alpha global (0..1) — usado quando ela "some" no fim.
     */
    public void renderAll(float open, float fade) {
        if (fade <= 0.01f) {
            return;
        }
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, fade);

        this.bind(TEX);

        // Base (anel inferior + motor de apogeu): FIXA, não abre.
        this.baseAssembly();

        // Duas metades da coifa, cada uma numa dobradiça na base.
        this.halfShell(-1, open); // metade oeste (-X)
        this.halfShell(1, open);  // metade leste (+X)

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    // ------------------------------------------------------------------
    // Partes
    // ------------------------------------------------------------------

    /** Base fixa: anel de equipamento + motor de apogeu apontando -Y. */
    private void baseAssembly() {
        Tessellator t = Tessellator.instance;
        // Anel da base (disco cheio, cinza).
        this.shadedDisc(0.52f, 12, 0f, 0f, 0f, true, 0.55f);
        this.shadedDisc(0.52f, 12, 0f, 0.1f, 0f, true, 0.75f);
        // Lateral do anel.
        this.shadedCylinder(0.52f, 0.52f, 0.1f, 12, 0f, 0f, 0f, 0.6f);
        // Motor de apogeu (bocal para baixo).
        this.shadedCylinder(0.14f, 0.10f, 0.16f, 8, 0f, -0.16f, 0f, 0.35f);
        this.shadedDisc(0.10f, 8, 0f, -0.16f, 0f, false, 0.15f);
    }

    /**
     * Uma metade da coifa (meia casca de cilindro + meio cone do topo).
     * Com open=0: no lugar (fechada). Com open=1: girada ~110° para trás
     * na dobradiça na base da cápsula.
     *
     * @param side -1 = oeste (-X), +1 = leste (+X)
     */
    private void halfShell(int side, float open) {
        GL11.glPushMatrix();
        // Dobradiça: eixo Z na base da coifa (y=0.1), deslocada pro lado.
        GL11.glTranslatef(side * 0.5f, 0.1f, 0f);
        // Gira para trás (rotZ negativa abre pra fora/baixo).
        GL11.glRotatef(side * -110f * open, 0f, 0f, 1f);
        GL11.glTranslatef(side * -0.5f, -0.1f, 0f);

        this.shellGeometry(side);
        GL11.glPopMatrix();
    }

    /** Geometria da meia casca (lado "side"), do y 0.1 ao 1.5 + meio cone. */
    private void shellGeometry(int side) {
        Tessellator t = Tessellator.instance;
        int segs = 7;         // segmentos da meia circunferência
        double a0 = side < 0 ? Math.PI / 2 : -Math.PI / 2; // de topo a baixo
        double a1 = side < 0 ? Math.PI * 1.5 : Math.PI / 2;
        float rBody = 0.52f;
        float y0 = 0.10f, y1 = 1.30f;
        float coneH = 0.35f, coneR = 0.52f;

        // ----- Casca externa (meia lateral do cilindro) -----
        for (int i = 0; i < segs; i++) {
            double ang0 = a0 + (a1 - a0) * i / segs;
            double ang1 = a0 + (a1 - a0) * (i + 1) / segs;
            float am = (float) ((ang0 + ang1) / 2);
            float nx = (float) Math.cos(am);
            float nz = (float) Math.sin(am);
            float f = 0.45f + 0.55f * Math.max(0f, nx * LIGHT[0] + nz * LIGHT[2]);
            t.startDrawingQuads();
            t.setColorOpaque((int) (f * 255), (int) (f * 255), (int) (f * 250));
            t.addVertexWithUV(Math.cos(ang0) * rBody, y0, Math.sin(ang0) * rBody, i / (double) segs, 1);
            t.addVertexWithUV(Math.cos(ang1) * rBody, y0, Math.sin(ang1) * rBody, (i + 1) / (double) segs, 1);
            t.addVertexWithUV(Math.cos(ang1) * rBody, y1, Math.sin(ang1) * rBody, (i + 1) / (double) segs, 0);
            t.addVertexWithUV(Math.cos(ang0) * rBody, y1, Math.sin(ang0) * rBody, i / (double) segs, 0);
            t.draw();
        }

        // ----- Casca interna (visível quando aberta; mais escura) -----
        for (int i = 0; i < segs; i++) {
            double ang0 = a0 + (a1 - a0) * i / segs;
            double ang1 = a0 + (a1 - a0) * (i + 1) / segs;
            t.startDrawingQuads();
            t.setColorOpaque(70, 72, 78);
            // winding invertido (interna)
            t.addVertexWithUV(Math.cos(ang0) * (rBody - 0.04f), y0, Math.sin(ang0) * (rBody - 0.04f), i / (double) segs, 1);
            t.addVertexWithUV(Math.cos(ang0) * (rBody - 0.04f), y1, Math.sin(ang0) * (rBody - 0.04f), i / (double) segs, 0);
            t.addVertexWithUV(Math.cos(ang1) * (rBody - 0.04f), y1, Math.sin(ang1) * (rBody - 0.04f), (i + 1) / (double) segs, 0);
            t.addVertexWithUV(Math.cos(ang1) * (rBody - 0.04f), y0, Math.sin(ang1) * (rBody - 0.04f), (i + 1) / (double) segs, 1);
            t.draw();
        }

        // ----- Meio cone do topo -----
        for (int i = 0; i < segs; i++) {
            double ang0 = a0 + (a1 - a0) * i / segs;
            double ang1 = a0 + (a1 - a0) * (i + 1) / segs;
            float am = (float) ((ang0 + ang1) / 2);
            float nx = (float) Math.cos(am);
            float nz = (float) Math.sin(am);
            float f = 0.45f + 0.55f * Math.max(0f, nx * LIGHT[0] + nz * LIGHT[2]);
            t.startDrawingQuads();
            t.setColorOpaque((int) (f * 255), (int) (f * 255), (int) (f * 250));
            t.addVertexWithUV(Math.cos(ang0) * coneR, y1, Math.sin(ang0) * coneR, i / (double) segs, 1);
            t.addVertexWithUV(Math.cos(ang1) * coneR, y1, Math.sin(ang1) * coneR, (i + 1) / (double) segs, 1);
            t.addVertexWithUV(Math.cos(ang1) * 0.07f, y1 + coneH, Math.sin(ang1) * 0.07f, (i + 1) / (double) segs, 0);
            t.addVertexWithUV(Math.cos(ang0) * 0.07f, y1 + coneH, Math.sin(ang0) * 0.07f, i / (double) segs, 0);
            t.draw();
        }

        // ----- Anel de reforço no meio da casca -----
        this.ringStripe(rBody + 0.015f, y0 + 0.45f, 0.06f, a0, a1, segs, 0.8f);
        this.ringStripe(rBody + 0.015f, y1 - 0.15f, 0.05f, a0, a1, segs, 0.8f);

        // ----- Borda de corte (a "boca" da metade; retângulo nas duas
        // extremidades verticais do semicírculo + tampa) -----
        this.cutEdge((float) (Math.cos(a0) * rBody), y0, (float) (Math.sin(a0) * rBody), 0.5f);
        this.cutEdge((float) (Math.cos(a1) * rBody), y0, (float) (Math.sin(a1) * rBody), 0.5f);
    }

    /** Faixa horizontal (anel de reforço) na casca. */
    private void ringStripe(float r, float y, float h, double a0, double a1, int segs, float f) {
        Tessellator t = Tessellator.instance;
        for (int i = 0; i < segs; i++) {
            double ang0 = a0 + (a1 - a0) * i / segs;
            double ang1 = a0 + (a1 - a0) * (i + 1) / segs;
            t.startDrawingQuads();
            t.setColorOpaque((int) (f * 255), (int) (f * 255), (int) (f * 250));
            t.addVertex(Math.cos(ang0) * r, y, Math.sin(ang0) * r);
            t.addVertex(Math.cos(ang1) * r, y, Math.sin(ang1) * r);
            t.addVertex(Math.cos(ang1) * r, y + h, Math.sin(ang1) * r);
            t.addVertex(Math.cos(ang0) * r, y + h, Math.sin(ang0) * r);
            t.draw();
        }
    }

    /** Borda de corte vertical da metade (quad fino). */
    private void cutEdge(float x, float y, float z, float f) {
        Tessellator t = Tessellator.instance;
        int c = (int) (f * 255);
        t.startDrawingQuads();
        t.setColorOpaque(c, c, c);
        t.addVertex(x, y, z);
        t.addVertex(x * 0.92f, y + 1.65f, z * 0.92f);
        t.addVertex(x * 0.92f + 0.03f, y + 1.65f, z * 0.92f);
        t.addVertex(x + 0.03f, y, z);
        t.draw();
    }

    // ------------------------------------------------------------------
    // Primitivas
    // ------------------------------------------------------------------

    private void shadedCylinder(float rb, float rt, float h, int sides,
            float ox, float oy, float oz, float baseShade) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2 * i / sides;
            double a1 = Math.PI * 2 * (i + 1) / sides;
            float nx = (float) (Math.cos((a0 + a1) / 2));
            float nz = (float) (Math.sin((a0 + a1) / 2));
            float f = baseShade * (0.5f + 0.5f * Math.max(0f, nx * LIGHT[0] + nz * LIGHT[2]));
            int c = (int) (f * 255);
            t.setColorOpaque(c, c, c);
            t.addVertexWithUV(ox + Math.cos(a0) * rb, oy, oz + Math.sin(a0) * rb, i / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * rb, oy, oz + Math.sin(a1) * rb, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(ox + Math.cos(a1) * rt, oy + h, oz + Math.sin(a1) * rt, (i + 1) / (double) sides, 0);
            t.addVertexWithUV(ox + Math.cos(a0) * rt, oy + h, oz + Math.sin(a0) * rt, i / (double) sides, 0);
        }
        t.draw();
    }

    private void shadedDisc(float r, int sides, float ox, float oy, float oz, boolean up, float f) {
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        int c = (int) (f * 255);
        t.setColorOpaque(c, c, c);
        t.addVertexWithUV(ox, oy, oz, 0.5, 0.5);
        for (int i = 0; i <= sides; i++) {
            double a = Math.PI * 2 * i / sides * (up ? 1 : -1);
            t.addVertexWithUV(ox + Math.cos(a) * r, oy, oz + Math.sin(a) * r,
                0.5 + Math.cos(a) * 0.5, 0.5 + Math.sin(a) * 0.5);
        }
        t.draw();
    }

    private void bind(ResourceLocation tex) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}
