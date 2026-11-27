package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.cinematic.CinematicTimeline;

/**
 * MODELO 3D DO SATÉLITE (v0.9.0 — REFEITO, "sanfona" de verdade).
 *
 * Como os satélites reais (Hubble, JWST, os de comunicação):
 *  - CORPO: caixa MLI dourada (multilayer insulation — o "papel alumínio
 *    dourado" das missões reais) com módulos, radiator branco e janelas;
 *  - BOCAL do motor de apogeu embaixo;
 *  - ANTENA de alto ganho: haste articulada + PRATO parabólico;
 *  - PAINÉIS SOLARES EM SANFONA: cada lado tem um YOKE (braço raiz que
 *    gira 90° no corpo) e 6 SEGMENTOS articulados que desdobram UM A UM
 *    (cascata), como uma régua dobrável — nunca instantâneo.
 *
 * O modelo é usado em três lugares (mesma identidade visual):
 *  - cinematic de deploy (GuiDeployCinematic): unfold animado 0->1;
 *  - estrutura na órbita (TileOrbitSatelliteRenderer): unfold=1 + spin;
 *  - GUI (pré-visualização).
 *
 * API: renderAll(unfold, spin) — unfold 0 = tudo fechado (tudo colado
 * no corpo, como no interior da coifa), 1 = implantado. A SANFONA usa
 * a CinematicTimeline (segmentExtend) para a cascata ficar PERFEITA:
 * aqui só recebemos o unfold global e derivamos os segmentos.
 */
public class ModelSatellite {

    private static final ResourceLocation TEX_BODY = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_PANEL = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_panel.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_gold.png");

    /** Direção da luz para o sombreamento (Sol "de cima e à frente"). */
    private static final float LX = 0.42f, LY = 0.82f, LZ = 0.38f;

    /** Segmentos da sanfona por lado. */
    public static final int SEGMENTS = 6;

    /**
     * Desenha o satélite COMPLETO.
     *
     * @param unfold 0 = painéis fechados (sanfona recolhida, yoke no
     *               corpo); 1 = tudo aberto (pose de operação).
     * @param spin   rotação lenta do conjunto em Y (graus).
     */
    public void renderAll(float unfold, float spin) {
        this.renderAll(unfold, spin, -1f);
    }

    /**
     * @param timeTicks tempo (ticks) da cinematic para a CASCATA dos
     *                 segmentos (se >= 0, usa a timeline real; se < 0,
     *                 deriva do unfold — usado pela estrutura na órbita).
     */
    public void renderAll(float unfold, float spin, float timeTicks) {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glRotatef(spin, 0f, 1f, 0f);

        // ---------------- Corpo central (MLI dourado) ----------------
        this.body();

        // ---------------- Braços + sanfona (esquerda/direita) ---------
        this.armAndAccordion(-1, unfold, timeTicks);
        this.armAndAccordion(1, unfold, timeTicks);

        // ---------------- Antena (haste + prato) ----------------------
        this.antenna(unfold);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
    }

    // ------------------------------------------------------------------
    // Corpo
    // ------------------------------------------------------------------

    /** Corpo: caixa MLI dourada + módulos + radiator + bocal. */
    private void body() {
        // Caixa principal 0.92 x 1.15 x 0.92 (eixo Y de 0 a 1.15).
        this.goldBox(-0.46f, 0.0f, -0.46f, 0.46f, 1.15f, 0.46f);
        // "Cinta" de separação de módulos (anel mais escuro).
        this.goldBox(-0.48f, 0.52f, -0.48f, 0.48f, 0.60f, 0.48f, 0.62f);
        this.goldBox(-0.48f, 0.94f, -0.48f, 0.48f, 1.00f, 0.48f, 0.62f);

        // Radiator branco (dissipador) num dos lados.
        this.plainBox(0.46f, 0.14f, -0.28f, 0.54f, 0.90f, 0.28f, 0.92f, 0.95f, 0.98f);

        // "Janelas" de instrumentos (painéis azul-escuro).
        this.plainBox(-0.47f, 0.66f, -0.20f, -0.44f, 0.88f, 0.20f, 0.25f, 0.32f, 0.45f);
        this.plainBox(-0.20f, 0.66f, -0.47f, 0.20f, 0.88f, -0.44f, 0.25f, 0.32f, 0.45f);

        // Bocal do motor de apogeu (embaixo).
        this.plainBox(-0.16f, -0.16f, -0.16f, 0.16f, 0.0f, 0.16f, 0.22f, 0.22f, 0.24f);
        this.plainBox(-0.10f, -0.26f, -0.10f, 0.10f, -0.16f, 0.10f, 0.12f, 0.12f, 0.13f);

        // Travas da coifa (4 ganchos no topo — soltam no deploy).
        for (int i = 0; i < 4; i++) {
            float a = (float) (Math.PI / 4 + i * Math.PI / 2);
            float x = (float) Math.cos(a) * 0.40f;
            float z = (float) Math.sin(a) * 0.40f;
            this.plainBox(x - 0.05f, 1.15f, z - 0.05f, x + 0.05f, 1.24f, z + 0.05f,
                0.35f, 0.38f, 0.42f);
        }
    }

    // ------------------------------------------------------------------
    // Antena
    // ------------------------------------------------------------------

    /** Antena de alto ganho: haste que ERGUE no deploy + prato parabólico. */
    private void antenna(float unfold) {
        // A haste nasce do topo e se inclina ~35° com o unfold (a antena
        // viaja recolhida apontando pra cima).
        float tilt = 35f * unfold;
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1.22f, 0f);
        GL11.glRotatef(tilt, 1f, 0f, 0f);

        // Haste.
        this.goldBox(-0.045f, 0f, -0.045f, 0.045f, 0.62f, 0.045f);
        // Subrefletor (haste fina + discos).
        this.goldBox(-0.02f, 0.62f, -0.02f, 0.02f, 0.80f, 0.02f);
        this.plainBox(-0.07f, 0.80f, -0.07f, 0.07f, 0.84f, 0.07f, 0.90f, 0.92f, 0.95f);

        // PRATO parabólico (taça inclinada).
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 0.86f, 0f);
        this.dish();
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    /** Prato parabólico: taça com 12 gomos + borda + traseira em domo. */
    private void dish() {
        Tessellator t = Tessellator.instance;
        this.bind(TEX_GOLD);
        int segs = 12;
        float rOuter = 0.34f, rInner = 0.10f, depth = 0.13f;
        // Superfície côncava (a "boca" do prato aponta +Y).
        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < segs; i++) {
            double a0 = Math.PI * 2 * i / segs;
            double a1 = Math.PI * 2 * (i + 1) / segs;
            float am = (float) ((a0 + a1) / 2);
            float nx = (float) Math.cos(am), nz = (float) Math.sin(am);
            float f = 0.55f + 0.45f * Math.max(0f, nx * LX + nz * LZ);
            t.setColorOpaque_F(f * 0.95f, f * 0.85f, f * 0.45f); // dourado
            t.addVertexWithUV(0f, depth, 0f, 0.5f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.44f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rInner, depth * 0.55f, (float) Math.sin(a1) * rInner, 0.56f, 0.5f);

            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.42f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rOuter, 0f, (float) Math.sin(a0) * rOuter, 0.30f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.30f, 0.62f);

            t.addVertexWithUV((float) Math.cos(a0) * rInner, depth * 0.55f, (float) Math.sin(a0) * rInner, 0.42f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.30f, 0.62f);
            t.addVertexWithUV((float) Math.cos(a1) * rInner, depth * 0.55f, (float) Math.sin(a1) * rInner, 0.56f, 0.5f);
        }
        t.draw();
        // Traseira do prato (domo suave).
        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < segs; i++) {
            double a0 = Math.PI * 2 * i / segs;
            double a1 = Math.PI * 2 * (i + 1) / segs;
            float am = (float) ((a0 + a1) / 2);
            float nx = (float) Math.cos(am), nz = (float) Math.sin(am);
            float f = 0.35f + 0.30f * Math.max(0f, nx * LX + nz * LZ);
            t.setColorOpaque_F(f * 0.95f, f * 0.85f, f * 0.45f);
            t.addVertexWithUV(0f, -depth * 0.35f, 0f, 0.5f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a1) * rOuter, 0f, (float) Math.sin(a1) * rOuter, 0.7f, 0.5f);
            t.addVertexWithUV((float) Math.cos(a0) * rOuter, 0f, (float) Math.sin(a0) * rOuter, 0.7f, 0.6f);
        }
        t.draw();
    }

    // ------------------------------------------------------------------
    // Braço + SANFONA (o coração do desdobramento)
    // ------------------------------------------------------------------

    /**
     * Um lado do conjunto solar: YOKE (braço raiz) + SANFONA de 6
     * segmentos. No estado FECHADO tudo fica COLADO no corpo (como
     * dentro da coifa); no ABERTO o yoke está a 90° e os segmentos
     * esticados em linha (a régua dobrável aberta).
     *
     * @param side      -1 = oeste (-X), +1 = leste (+X)
     * @param unfold    0..1 global
     * @param timeTicks ticks da cinematic (>=0: cascata da timeline) ou
     *                  <0 (deriva do unfold — órbita/GUI)
     */
    private void armAndAccordion(int side, float unfold, float timeTicks) {
        // Frações: yoke abre primeiro; cada segmento estica em cascata.
        float yoke;
        float[] seg = new float[SEGMENTS];
        if (timeTicks >= 0f) {
            yoke = CinematicTimeline.yokeOpen(timeTicks, side);
            for (int i = 0; i < SEGMENTS; i++) {
                seg[i] = CinematicTimeline.segmentExtend(timeTicks, side, i);
            }
        } else {
            // Modo "pose": deriva do unfold global com a mesma cascata
            // (yoke nos primeiros 35%, segmentos nos 65% restantes, um
            // a um — mesma sensação de sanfona).
            yoke = CinematicTimeline.clamp01(unfold / 0.35f);
            float span = (unfold - 0.35f) / 0.65f;
            for (int i = 0; i < SEGMENTS; i++) {
                float start = (float) i / SEGMENTS;
                float end = start + 0.55f;
                float local = (span - start) / (end - start);
                seg[i] = CinematicTimeline.easeInOut(local);
            }
        }

        GL11.glPushMatrix();
        // Ponto de articulação no corpo (altura do meio).
        GL11.glTranslatef(side * 0.46f, 0.62f, 0f);

        // ---- YOKE em 2 ESTÁGIOS (v0.10.0 — validado NUMERICAMENTE no
        //      scripts/satellite_geometry_check.py: pontas em Y=0.62 nos
        //      DOIS lados = HORIZONTAL; versões anteriores giravam um
        //      braço pro zênite e outro pro nadir — os "painéis
        //      verticais, um pra baixo e outro pra cima" do usuário).
        // ESTÁGIO 1: braço desenhado apontando +Y local (fechado: colado
        // na lateral do corpo). Rotação -side*90*yoke: yoke=0 => nada
        // (painel deitado contra o corpo); yoke=1 => braço HORIZONTAL
        // apontando PARA FORA (-X à esquerda, +X à direita).
        GL11.glRotatef(-side * 90f * yoke, 0f, 0f, 1f);
        this.bind(TEX_GOLD);
        this.drawingBox(-0.055f, 0f, -0.055f, 0.055f, 0.30f, 0.055f,
            0.80f, 0.72f, 0.40f, 0.80f, 0.72f, 0.40f);

        // ---- SANFONA: articulada na ponta do yoke ----
        GL11.glTranslatef(0f, 0.30f, 0f);
        // ESTÁGIO 2: realinha o frame local (+90 constante nos dois
        // lados) para os segmentos correrem AO LONGO do braço — com o
        // yoke fechado a sanfona fica DEITADA contra a lateral do corpo
        // (como dentro da coifa); aberta, estica no plano horizontal.
        GL11.glRotatef(90f, 0f, 0f, 1f);

        float segLen = 0.42f;   // comprimento ESTICADO de cada segmento
        float segW = 0.50f;     // largura do painel
        float segThick = 0.035f;

        for (int i = 0; i < SEGMENTS; i++) {
            float ext = seg[i]; // 0 = dobrado (empilhado), 1 = esticado
            // Cada segmento gira na dobradiça ENTRE ele e o anterior:
            // dobrado = 160° (quase colado), esticado = 0° (em linha).
            float ang = (1f - ext) * 160f;
            GL11.glPushMatrix();
            GL11.glRotatef(ang, 0f, 1f, 0f);
            // O painel do segmento (apontando +X local).
            this.bind(TEX_PANEL);
            this.panelSegment(segLen, segW, segThick, ext);
            GL11.glPopMatrix();
            // A PRÓXIMA dobradiça fica na ponta deste segmento — mas
            // quando dobrado, a ponta efetiva é quase zero (empilhado).
            float adv = segLen * ext;
            if (adv < 0.04f) {
                adv = 0.04f; // mínimo pra não Z-fight quando empilhado
            }
            GL11.glTranslatef(adv, 0f, 0f);
        }

        GL11.glPopMatrix();
    }

    /** Um segmento do painel solar: placa com células + barra central. */
    private void panelSegment(float len, float w, float thick, float ext) {
        Tessellator t = Tessellator.instance;
        float x0 = 0f, x1 = len;
        float z0 = -w / 2f, z1 = w / 2f;
        float y0 = -thick / 2f, y1 = thick / 2f;

        // Face "solar" (cima): células azul-escuras com grade — a
        // textura sat_panel.png tem as células; sombreamento por face.
        t.startDrawingQuads();
        float fTop = 0.75f + 0.25f * Math.max(0f, LY);
        t.setColorOpaque_F(fTop * 0.55f, fTop * 0.65f, fTop * 1.0f);
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 0f);
        t.addVertexWithUV(x0, y1, z0, 0f, 0f);
        t.draw();

        // Face de baixo (estrutura cinza).
        t.startDrawingQuads();
        t.setColorOpaque_F(0.42f, 0.44f, 0.48f);
        t.addVertexWithUV(x0, y0, z0, 0f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.draw();

        // Bordas (laterais estreitas) — mais claras (reflexo da borda).
        t.startDrawingQuads();
        t.setColorOpaque_F(0.62f, 0.66f, 0.70f);
        // +Z
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1);
        t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        // -Z
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0);
        t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        // ponta +X (a "face" de dobra)
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0);
        t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.draw();

        // Dobradiça visual na raiz do segmento (calha dourada).
        this.bind(TEX_GOLD);
        this.drawingBox(-0.03f, -0.045f, -w / 2f + 0.03f, 0.02f, 0.045f, w / 2f - 0.03f,
            0.9f, 0.8f, 0.45f, 0.9f, 0.8f, 0.45f);
    }

    // ------------------------------------------------------------------
    // Primitivas de caixa sombreada (6 faces com luz própria)
    // ------------------------------------------------------------------

    /** Caixa dourada MLI (textura sat_gold + tom dourado no sombreado). */
    private void goldBox(float x0, float y0, float z0, float x1, float y1, float z1) {
        this.goldBox(x0, y0, z0, x1, y1, z1, 1f);
    }

    private void goldBox(float x0, float y0, float z0, float x1, float y1, float z1, float mul) {
        this.bind(TEX_GOLD);
        this.drawingBox(x0, y0, z0, x1, y1, z1,
            1.00f * mul, 0.84f * mul, 0.38f * mul,   // claro
            0.38f * mul, 0.30f * mul, 0.12f * mul);  // escuro
    }

    /** Caixa com cor plana (rgb claro / rgb escuro). */
    private void plainBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        this.drawingBox(x0, y0, z0, x1, y1, z1, r, g, b, r * 0.35f, g * 0.35f, b * 0.35f);
    }

    /**
     * A caixa de verdade: 6 quads com sombreamento por face conforme a
     * normal em relação à luz (como um render techne — funciona em
     * qualquer contexto, inclusive na cena 3D da GUI).
     */
    private void drawingBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float lr, float lg, float lb, float dr, float dg, float db) {
        Tessellator t = Tessellator.instance;

        // +Y (topo)
        float f = Math.max(0f, LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 0f);
        t.addVertexWithUV(x0, y1, z0, 0f, 0f);
        t.draw();

        // -Y (baixo)
        f = Math.max(0f, -LY);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z0, 0f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.draw();

        // +X
        f = Math.max(0f, LX);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x1, y0, z1, 0f, 0f);
        t.addVertexWithUV(x1, y0, z0, 1f, 0f);
        t.addVertexWithUV(x1, y1, z0, 1f, 1f);
        t.addVertexWithUV(x1, y1, z1, 0f, 1f);
        t.draw();

        // -X
        f = Math.max(0f, -LX);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z0, 0f, 0f);
        t.addVertexWithUV(x0, y0, z1, 1f, 0f);
        t.addVertexWithUV(x0, y1, z1, 1f, 1f);
        t.addVertexWithUV(x0, y1, z0, 0f, 1f);
        t.draw();

        // +Z
        f = Math.max(0f, LZ);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y0, z1, 0f, 0f);
        t.addVertexWithUV(x1, y0, z1, 1f, 0f);
        t.addVertexWithUV(x1, y1, z1, 1f, 1f);
        t.addVertexWithUV(x0, y1, z1, 0f, 1f);
        t.draw();

        // -Z
        f = Math.max(0f, -LZ);
        t.startDrawingQuads();
        t.setColorOpaque_F(dr + (lr - dr) * f, dg + (lg - dg) * f, db + (lb - db) * f);
        t.addVertexWithUV(x0, y1, z0, 0f, 1f);
        t.addVertexWithUV(x1, y1, z0, 1f, 1f);
        t.addVertexWithUV(x1, y0, z0, 1f, 0f);
        t.addVertexWithUV(x0, y0, z0, 0f, 0f);
        t.draw();
    }

    private void bind(ResourceLocation tex) {
        net.minecraft.client.Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
    }
}
