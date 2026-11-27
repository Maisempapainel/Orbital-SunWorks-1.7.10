package com.orbitalsunworks.client.gui;

import java.util.Random;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.orbitalsunworks.cinematic.CinematicTimeline;
import com.orbitalsunworks.client.render.ModelRocket;
import com.orbitalsunworks.client.render.ModelSatellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * CINEMÁTICA DE LANÇAMENTO + IMPLANTAÇÃO (v0.9.0 — REFEITA "estilo NASA").
 *
 * O que o usuário exigiu (e como foi feito):
 *
 * 1. "O espaço aparece na minha câmera, com minha casa de fundo, grama,
 *    animais — INACEITÁVEL": o fundo agora é DESENHADO pela própria
 *    cena — caixa de espaço ESCURO (cinza quase preto) envolvente em
 *    TODAS as direções desenhada PRIMEIRO, cobrindo 100% da tela. O
 *    mundo do Minecraft não aparece em NENHUM pixel.
 *
 * 2. "O foguete não se parece com o que lançamos da Terra": a cinematic
 *    usa o MESMO ModelRocket do jogo (a nave que sobe da plataforma),
 *    em escala maior — a coifa do topo carrega o satélite.
 *
 * 3. Sequência (como as animações de missão da NASA), via
 *    CinematicTimeline (classe PURA testada no servidor):
 *      F0 ORBIT INSERTION — o estágio superior chega à órbita (inércia);
 *      F1 FAIRING — a coifa abre em 2 metades que GIRAM e DERIVAM
 *          para trás (inércia, sem explosão);
 *      F2 SEPARATION — molas empurram o satélite SUAVEMENTE para
 *          frente; o estágio acende o motor 1s (burn) e DERIVA para
 *          trás gradualmente (NUNCA some instantaneamente);
 *      F3 DRIFT — câmera troca de alvo: agora acompanha o SATÉLITE;
 *      F4 UNFOLD — as travas soltam e os painéis desdobram em SANFONA
 *          (6 segmentos por lado, um a um, em cascata — nunca
 *          instantâneo), a antena ergue o prato;
 *      F5 INSPECT — a câmera faz um arco lento ao redor do satélite
 *          implantado com a TERRA ao fundo e se afasta.
 *
 * 4. Movimentos suaves (easings), inércia (nada para de repente), SEM
 *    explosões exageradas — jatos RCS discretos na estabilização.
 */
@SideOnly(Side.CLIENT)
public class GuiDeployCinematic extends GuiScreen {

    private static final ResourceLocation TEX_EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");

    private final UUID satId;
    private final String satName;
    private final int satTier;
    private final float satHealth;
    private final boolean debrisShield;

    private int tick = 0;
    private boolean finished = false;

    private final ModelRocket rocket = new ModelRocket();
    private final ModelSatellite satellite = new ModelSatellite();

    /** Estrelas fixas (esfera unitária, direções estáveis). */
    private static final int STARS = 320;
    private static final float[][] STAR_POS = new float[STARS][3];
    /** Detritos da coifa (2 metades com posição/rotação da timeline). */

    static {
        Random r = new Random(20260427);
        for (int i = 0; i < STARS; i++) {
            double theta = r.nextDouble() * Math.PI * 2;
            double phi = Math.acos(2 * r.nextDouble() - 1);
            STAR_POS[i][0] = (float) (Math.sin(phi) * Math.cos(theta));
            STAR_POS[i][1] = (float) (Math.cos(phi));
            STAR_POS[i][2] = (float) (Math.sin(phi) * Math.sin(theta));
        }
    }

    public GuiDeployCinematic(UUID id, String name, int tier, float health, boolean shield) {
        this.satId = id;
        this.satName = name;
        this.satTier = tier;
        this.satHealth = health;
        this.debrisShield = shield;
    }

    /** Abre a cinematic no cliente (handler do MessageDeployCinematic). */
    public static void open(UUID id, String name, int tier, float health, boolean shield) {
        Minecraft.getMinecraft().displayGuiScreen(
            new GuiDeployCinematic(id, name, tier, health, shield));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (!this.finished) {
            this.tick++;
            if (this.tick >= CinematicTimeline.DURATION) {
                this.finished = true;
                this.mc.displayGuiScreen(null);
            }
        }
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float p = this.tick + partialTicks;
        int w = this.width;
        int h = this.height;
        Tessellator t = Tessellator.instance;

        // ================= CENA 3D (perspectiva) =================
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float aspect = (float) w / (float) h;
        GLU.gluPerspective(58f, aspect, 0.1f, 500f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        // ============ 1. O ESPAÇO (desenhado PRIMEIRO, cobre TUDO) ============
        // O pedido: "lugar escuro, tipo um cinza escuro quase preto,
        // igual ao espaço mesmo". Caixa envolvente + fundo + estrelas
        // + Terra — o mundo do jogo NÃO aparece em pixel nenhum.
        this.drawSpace(p, w, h);

        // ============ 2. CÂMERA ============
        // O alvo desliza do "conjunto" (foguete+satélite) para o
        // satélite sozinho (a troca de assunto da missão).
        float focusSat = CinematicTimeline.cameraFocusSatellite(p);
        float satDist = CinematicTimeline.satelliteDistance(p);
        float stageRecede = CinematicTimeline.stageRecede(p);

        // Alvo: posição do SATÉLITE no eixo +Z (ele é empurrado pra frente).
        float targetZ = 1.0f + satDist * 0.85f;
        float targetY = 0.9f;
        // A câmera segue a mistura conjunto->satélite.
        float lookZ = targetZ * focusSat;
        float lookY = targetY + (1f - focusSat) * 0.15f;

        float camDist = CinematicTimeline.cameraDistance(p);
        float camAng = CinematicTimeline.cameraOrbit(p);
        float camH = CinematicTimeline.cameraHeight(p);
        float cx = (float) Math.cos(camAng) * camDist;
        float cz = (float) Math.sin(camAng) * camDist + lookZ;
        GLU.gluLookAt(cx, lookY + camH, cz, 0f, lookY, lookZ, 0f, 1f, 0f);

        // ============ 3. ATORES ============

        // ---- 3a. As METADES DA COIFA (soltam na F1, derivam p/ trás) ----
        float fairing = CinematicTimeline.fairingRelease(p);
        float fairingDrift = CinematicTimeline.fairingDrift(p);
        float fairingSpin = CinematicTimeline.fairingSpin(p);
        if (fairing > 0.01f && fairing < 1f) {
            this.drawFairingHalves(p, fairing, fairingDrift, fairingSpin);
        }

        // ---- 3b. O SATÉLITE (sai do topo do estágio nas molas) ----
        // Posição: no TOPO do estágio (0, ~1.6, -0.35) e empurrado +Z.
        float unfold = CinematicTimeline.unfold(p);
        float spin = CinematicTimeline.satelliteSpin(p);
        GL11.glPushMatrix();
        float satScale = 0.80f;
        GL11.glTranslatef(0f, targetY - 0.45f, -0.35f + targetZ * 1.0f);
        GL11.glScalef(satScale, satScale, satScale);
        // O modelo tem base em y=0; centra o volume no ponto de translação.
        GL11.glTranslatef(0f, -0.55f, 0f);
        this.satellite.renderAll(unfold, spin, p);
        GL11.glPopMatrix();

        // Jatos RCS discretos na estabilização (pós-separação).
        if (p > CinematicTimeline.F2_SEPARATION + 30f
            && p < CinematicTimeline.F3_DRIFT + 25f) {
            this.rcsPuff(targetZ, targetY, p);
        }

        // ---- 3c. O ESTÁGIO SUPERIOR (recua GRADUALMENTE, não some) ----
        float stageFade = 1f - CinematicTimeline.easeOut(
            (p - (CinematicTimeline.F5_INSPECT + 20f)) / 60f);
        if (stageFade > 0.01f) {
            GL11.glPushMatrix();
            // Recua em -Z (trajetória própria) e desce um pouco (o
            // estágio segue sua vida num plano levemente diferente).
            GL11.glTranslatef(0f, 0.35f - stageRecede * 0.12f,
                -0.35f - stageRecede * 1.15f);
            // Atitude: gira devagar (inércia de rotação do estágio).
            GL11.glRotatef(4f + p * 0.10f, 0f, 1f, 0.02f);
            // O MESMO foguete do jogo (ModelRocket), em pé (base em y=0).
            GL11.glScalef(1.35f, 1.35f, 1.35f);
            GL11.glColor4f(1f, 1f, 1f, stageFade);
            this.rocket.renderAll();
            GL11.glColor4f(1f, 1f, 1f, 1f);
            // Chama do burn de afastamento (1s depois da separação).
            float burn = CinematicTimeline.stageBurn(p);
            if (burn > 0.01f) {
                this.engineBurn(burn);
            }
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);

        // ================= FIM DA CENA 3D =================
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);

        // ================= HUD (2D, por cima) =================
        // vinheta de cinema.
        this.drawGradientRect(0, 0, w, h / 7, 0xC0000000, 0x00000000);
        this.drawGradientRect(0, h - h / 7, w, h, 0x00000000, 0xC0000000);

        // fade-in / fade-out.
        if (p < 30f) {
            this.fullScreenFade(1f - CinematicTimeline.clamp01(p / 30f));
        }
        if (p > CinematicTimeline.DURATION - 25f) {
            this.fullScreenFade(CinematicTimeline.clamp01(
                (p - (CinematicTimeline.DURATION - 25f)) / 25f));
        }

        // Legendas de missão (aparecem na hora certa, estilo NASA).
        this.missionCaption(p, w, h);

        if (p > 15f && p < CinematicTimeline.DURATION - 15f) {
            String skip = "ESC para pular";
            this.fontRendererObj.drawString(skip,
                w - this.fontRendererObj.getStringWidth(skip) - 8, 8, 0x6A7888);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    // ------------------------------------------------------------------
    // O espaço: fundo escuro + estrelas + Terra (cobre a tela TODA)
    // ------------------------------------------------------------------

    private void drawSpace(float p, int w, int h) {
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        // ---- Fundo "cinza escuro quase preto" (o espaço do pedido) ----
        // Desenhado SEM depender da matriz de câmera (quad envolvente
        // gigante ao redor da origem — a câmera está sempre dentro).
        GL11.glPushMatrix();
        float s = 260f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor3f(0.035f, 0.038f, 0.048f); // cinza-azulado quase preto
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
        GL11.glPopMatrix();

        // ---- Estrelas (pontos com brilho individual pulsando) ----
        GL11.glPointSize(1.7f);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        t.startDrawing(GL11.GL_POINTS);
        for (int i = 0; i < STARS; i++) {
            float tw = 0.45f + 0.55f * (0.5f + 0.5f
                * (float) Math.sin(p * 0.045f + i * 1.7f));
            t.setColorRGBA_F(tw, tw, tw, 1f);
            t.addVertex(STAR_POS[i][0] * 240f, STAR_POS[i][1] * 240f, STAR_POS[i][2] * 240f);
        }
        t.draw();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);

        // ---- A TERRA "abaixo" (disco gigante, textura própria) ----
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        this.mc.getTextureManager().bindTexture(TEX_EARTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        float er = 120f, ey = -128f;
        int segs = 40;
        GL11.glPushMatrix();
        // Disco no plano y=ey (visto de baixo: câmera está acima).
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque_F(1f, 1f, 1f);
        t.addVertexWithUV(0f, ey, 0f, 0.5f, 0.5f);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs + p * 0.0012f; // giro lento
            t.addVertexWithUV((float) Math.cos(a) * er, ey, (float) Math.sin(a) * er,
                0.5f + (float) Math.cos(a) * 0.5f, 0.5f + (float) Math.sin(a) * 0.5f);
        }
        t.draw();

        // Limbo atmosférico (anel azul que "vaza" da borda do disco).
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        t.startDrawing(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2 * i / segs;
            float ca = (float) Math.cos(a), sa = (float) Math.sin(a);
            t.setColorRGBA_F(0.30f, 0.55f, 1f, 0.55f);
            t.addVertex(ca * er, ey - 0.5f, sa * er);
            t.setColorRGBA_F(0.25f, 0.45f, 1f, 0f);
            t.addVertex(ca * (er + 20f), ey - 0.5f, sa * (er + 20f));
        }
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    // ------------------------------------------------------------------
    // As metades da coifa (derivam para trás girando)
    // ------------------------------------------------------------------

    private void drawFairingHalves(float p, float release, float drift, float spin) {
        Tessellator t = Tessellator.instance;
        // Cada metade: um "concha" longa (2 quads curvos + borda) que
        // parte do TOPO do foguete (y ~1.6..2.9) e deriva em -Z.
        for (int side = -1; side <= 1; side += 2) {
            GL11.glPushMatrix();
            // Ponto de origem: topo do estágio.
            GL11.glTranslatef(0f, 2.0f, -0.35f);
            // Afastamento lateral (molas pirotecnicas) + deriva p/ trás.
            GL11.glTranslatef(side * release * 1.6f, release * 0.45f, -drift);
            // Gira (tumble lento de detrito inerte).
            GL11.glRotatef(spin, 0.3f, 0.85f, 0.45f);
            // A concha (semi-cilindro fino, cinza claro).
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            int segs = 5;
            float r = 0.55f, len = 1.3f;
            double a0 = side < 0 ? Math.PI / 2 : -Math.PI / 2;
            double a1 = side < 0 ? Math.PI * 1.5 : Math.PI / 2;
            t.startDrawingQuads();
            for (int i = 0; i < segs; i++) {
                double b0 = a0 + (a1 - a0) * i / segs;
                double b1 = a0 + (a1 - a0) * (i + 1) / segs;
                float am = (float) ((b0 + b1) / 2);
                float f = 0.55f + 0.45f * Math.max(0f,
                    (float) Math.cos(am) * 0.42f + (float) Math.sin(am) * 0.38f);
                t.setColorOpaque_F(f, f, f * 1.03f);
                t.addVertex((float) Math.cos(b0) * r, 0f, (float) Math.sin(b0) * r);
                t.addVertex((float) Math.cos(b1) * r, 0f, (float) Math.sin(b1) * r);
                t.addVertex((float) Math.cos(b1) * r, len, (float) Math.sin(b1) * r);
                t.addVertex((float) Math.cos(b0) * r, len, (float) Math.sin(b0) * r);
            }
            t.draw();
            GL11.glPopMatrix();
        }
    }

    // ------------------------------------------------------------------
    // Efeitos discretos
    // ------------------------------------------------------------------

    /** Chama do burn de afastamento do estágio (cone aditivo). */
    private void engineBurn(float strength) {
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE); // aditivo
        // Núcleo branco-azulado + saia laranja (flicker sutil).
        float flick = 0.85f + 0.15f * (float) Math.sin(tick * 1.7f);
        float len = (0.85f + 0.55f * strength) * flick;
        for (int layer = 0; layer < 2; layer++) {
            float r0 = layer == 0 ? 0.10f : 0.16f;
            float r1 = layer == 0 ? 0.03f : 0.09f;
            float a = layer == 0 ? 0.9f : 0.45f;
            t.startDrawing(GL11.GL_TRIANGLE_FAN);
            if (layer == 0) {
                t.setColorRGBA_F(0.85f, 0.92f, 1.0f, a * strength);
            } else {
                t.setColorRGBA_F(1.0f, 0.62f, 0.25f, a * strength);
            }
            t.addVertex(0f, -len, 0f);
            for (int i = 0; i <= 10; i++) {
                double ang = Math.PI * 2 * i / 10;
                if (layer == 0) {
                    t.setColorRGBA_F(0.85f, 0.92f, 1.0f, 0f);
                } else {
                    t.setColorRGBA_F(1.0f, 0.62f, 0.25f, 0f);
                }
                t.addVertex((float) Math.cos(ang) * r1, 0.02f, (float) Math.sin(ang) * r1);
                // (usa o raio pequeno no topo do bocal)
            }
            t.draw();
            // saia do jato (cilindro cônico)
            t.startDrawingQuads();
            for (int i = 0; i < 10; i++) {
                double a0 = Math.PI * 2 * i / 10;
                double a1 = Math.PI * 2 * (i + 1) / 10;
                if (layer == 0) {
                    t.setColorRGBA_F(0.85f, 0.92f, 1.0f, a * strength * 0.8f);
                } else {
                    t.setColorRGBA_F(1.0f, 0.62f, 0.25f, a * strength * 0.8f);
                }
                t.addVertex((float) Math.cos(a0) * r0, 0.02f, (float) Math.sin(a0) * r0);
                t.addVertex((float) Math.cos(a1) * r0, 0.02f, (float) Math.sin(a1) * r0);
                t.addVertex((float) Math.cos(a1) * r1, -len, (float) Math.sin(a1) * r1);
                t.addVertex((float) Math.cos(a0) * r1, -len, (float) Math.sin(a0) * r1);
            }
            t.draw();
        }
        GL11.glDisable(GL11.GL_BLEND);
    }

    /** Sopros RCS curtos (gás branco) no corpo do satélite. */
    private void rcsPuff(float satZ, float satY, float p) {
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        // 2 sopros alternados (pulso a cada 30 ticks).
        float pulse = (p % 30f) < 12f ? 1f : 0f;
        if (pulse <= 0f) {
            GL11.glDisable(GL11.GL_BLEND);
            return;
        }
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? -1f : 1f;
            float fade = pulse * (1f - (p % 12f) / 12f);
            t.startDrawingQuads();
            t.setColorRGBA_F(0.9f, 0.95f, 1f, 0.5f * fade);
            t.addVertex(side * 0.5f, satY, satZ);
            t.addVertex(side * 0.5f, satY + 0.14f, satZ);
            t.addVertex(side * (0.9f + 0.3f * fade), satY + 0.14f, satZ);
            t.addVertex(side * (0.9f + 0.3f * fade), satY, satZ);
            t.draw();
        }
        GL11.glDisable(GL11.GL_BLEND);
    }

    // ------------------------------------------------------------------
    // HUD
    // ------------------------------------------------------------------

    /** Legendas de missão no estilo dos vídeos da NASA (por fase). */
    private void missionCaption(float p, int w, int h) {
        String caption = null;
        int color = 0x9FB4C7;
        if (p > 12f && p < 62f) {
            caption = "Inserção orbital confirmada";
            color = 0x55E0FF;
        } else if (p > CinematicTimeline.F1_FAIRING + 4f && p < CinematicTimeline.F1_FAIRING + 50f) {
            caption = "Liberação da coifa";
        } else if (p > CinematicTimeline.F2_SEPARATION + 4f && p < CinematicTimeline.F2_SEPARATION + 55f) {
            caption = "Separação — molas de impulso acionadas";
        } else if (p > CinematicTimeline.F3_DRIFT + 4f && p < CinematicTimeline.F3_DRIFT + 55f) {
            caption = "Estabilizando atitude";
        } else if (p > CinematicTimeline.F4_UNFOLD + 4f && p < CinematicTimeline.F4_UNFOLD + 90f) {
            caption = "Implantação dos painéis solares";
            color = 0xFFD970;
        } else if (p > CinematicTimeline.F5_INSPECT + 8f) {
            caption = this.satName + " — Tier " + this.satTier + " operacional";
            color = 0x7CFC00;
        }
        if (caption != null) {
            this.drawCenteredString(this.fontRendererObj, caption, w / 2, h - 56, color);
        }
        // Geração inicial (aparece no fim, com o satélite pronto).
        if (p > CinematicTimeline.F5_INSPECT + 25f) {
            String gen = com.orbitalsunworks.util.OSUtils
                .formatRF(this.currentGenerationRFt()) + "/t de geração inicial";
            this.drawCenteredString(this.fontRendererObj, gen, w / 2, h - 44, 0x7CFC00);
        }
    }

    /** Quad preto com alpha pela tela toda (fade). */
    private void fullScreenFade(float alpha) {
        Tessellator t = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, alpha);
        t.startDrawingQuads();
        t.addVertex(0, 0, 0);
        t.addVertex(0, this.height, 0);
        t.addVertex(this.width, this.height, 0);
        t.addVertex(this.width, 0, 0);
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
    }

    /** Geração atual do satélite (mesma fórmula do manager). */
    private long currentGenerationRFt() {
        long base = 1000000L;
        float factor;
        if (this.satHealth >= 0.2f) {
            factor = 0.5f + (this.satHealth - 0.2f) * (0.5f / 0.8f);
        } else {
            factor = 0.5f;
        }
        return (long) (base * factor);
    }

    // ------------------------------------------------------------------
    // Controles
    // ------------------------------------------------------------------

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == 1 || keyCode == 28 || keyCode == 57) { // ESC/ENTER/ESPAÇO
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.tick > 40) { // não pular sem querer
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false; // NUNCA pausar o servidor em multiplayer!
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GL11.glColor4f(1, 1, 1, 1);
    }
}
