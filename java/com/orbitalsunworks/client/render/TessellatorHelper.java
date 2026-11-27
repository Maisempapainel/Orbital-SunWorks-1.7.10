package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

/**
 * Helpers de desenho para os renderizadores do mod.
 */
final class TessellatorHelper {

    private TessellatorHelper() {
    }

    /**
     * Cone de fogo apontando para baixo (base no y=0, descendo len).
     * Desenhado com TRIANGLES, duas camadas (externa laranja, interna branca).
     * Precisa estar com a textura de chama já vinculada.
     */
    static void renderFlameCone(float radius, float len) {
        Tessellator t = Tessellator.instance;

        // Camada externa (laranja, mais larga e longa)
        t.startDrawing(GL11.GL_TRIANGLES);
        flameLayer(t, radius, len, 12, 0.0f);
        t.draw();

        // Camada interna (branca/azulada, mais curta - núcleo quente)
        t.startDrawing(GL11.GL_TRIANGLES);
        flameLayer(t, radius * 0.55f, len * 0.65f, 10, -0.05f);
        t.draw();
    }

    private static void flameLayer(Tessellator t, float radius, float len, int sides, float yOffset) {
        for (int i = 0; i < sides; i++) {
            double a0 = (Math.PI * 2 * i) / sides;
            double a1 = (Math.PI * 2 * (i + 1)) / sides;
            // base (no motor)
            t.addVertexWithUV(Math.cos(a0) * radius, yOffset, Math.sin(a0) * radius, i / (double) sides, 1);
            t.addVertexWithUV(Math.cos(a1) * radius, yOffset, Math.sin(a1) * radius, (i + 1) / (double) sides, 1);
            // ponta para baixo (ápice)
            t.addVertexWithUV(0, yOffset - len, 0, (i + 0.5) / (double) sides, 0);
        }
    }
}
