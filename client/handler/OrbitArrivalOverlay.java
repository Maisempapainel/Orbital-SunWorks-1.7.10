package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * CINEMÁTICA IN-WORLD da chegada à órbita (v0.10.0 — substitui a
 * GuiDeployCinematic "porca" que o usuário detonou).
 *
 * Filosofia NOVA: nada de GUI fake cobrindo o mundo com um foguete de
 * mentira. A viagem REAL acontece no mundo (a entidade sobe de verdade,
 * câmera F5 automática). Este handler só adiciona dois momentos:
 *
 *  1. ASCENSÃO (warp): quando a nave real entra em PHASE_WARP, a tela
 *     esmaece para o PRETO suavemente (1s) — "atravessando a atmosfera";
 *  2. REVELAÇÃO: quando o jogador aparece na dimensão orbital, o preto
 *     ABRE revelando o espaço: estrelas curtas piscando e a TERRA
 *     IMENSA já presente no céu do mundo de verdade (OrbitSkyRenderer).
 *     1.5s de abertura + faixa de título da missão por 3s.
 *
 * É OVERLAY (RenderGameOverlayEvent): o mundo real continua desenhado
 * embaixo — a Terra que aparece é a DO CÉU DA DIMENSÃO, não um PNG.
 */
@SideOnly(Side.CLIENT)
public class OrbitArrivalOverlay {

    /** Estado: -1 idle; >=0 ticks desde o início do efeito. */
    private int revealTicks = -1;
    private int fadeTicks = -1;

    private static final int REVEAL_TOTAL = 32;  // 1.6s abrindo
    private static final int TITLE_TOTAL = 70;   // 3.5s com o título

    /** Chamado pelo ClientTravelHandler ao detectar chegada na órbita. */
    public void startReveal() {
        this.revealTicks = 0;
    }

    /** Chamado quando a nave real entra em warp (o preto da subida). */
    public void startFade() {
        this.fadeTicks = 0;
    }

    public boolean isFading() {
        return this.fadeTicks >= 0;
    }

    @SubscribeEvent
    public void onOverlay(net.minecraftforge.client.event.RenderGameOverlayEvent.Pre event) {
        if (event.type != net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            this.revealTicks = -1;
            this.fadeTicks = -1;
            return;
        }

        // ---- 1. Fade PRETO na ascensão (warp da nave real) ----
        if (this.fadeTicks >= 0) {
            this.fadeTicks++;
            // Alpha sobe rápido (0 -> 1 em 12 ticks), segura no preto
            // até o teleporte acontecer (o handler de viagem desliga).
            float a = Math.min(1f, this.fadeTicks / 12f);
            if (com.orbitalsunworks.world.orbit.OrbitDimensionManager.isOrbitWorld(mc.theWorld)) {
                // Chegou: passa a bola pro reveal.
                this.fadeTicks = -1;
                if (this.revealTicks < 0) {
                    this.revealTicks = 0;
                }
            } else {
                drawBlackQuad(a, mc);
                return;
            }
        }

        // ---- 2. REVELAÇÃO da órbita (preto abre pra o mundo real) ----
        if (this.revealTicks >= 0) {
            this.revealTicks++;
            if (this.revealTicks > Math.max(REVEAL_TOTAL, TITLE_TOTAL)) {
                this.revealTicks = -1;
                return;
            }
            if (this.revealTicks <= REVEAL_TOTAL) {
                // Cortina que ABRE (alpha 1 -> 0): o mundo (com a Terra
                // gigante do céu) aparece por trás.
                float a = 1f - (float) this.revealTicks / REVEAL_TOTAL;
                // Easing: começa lento, acelera (a "abertura" dramática).
                a = a * a;
                drawBlackQuad(a, mc);
            }
            // Título da missão (durante e depois da cortina).
            if (this.revealTicks > 8) {
                ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
                int w = res.getScaledWidth();
                int h = res.getScaledHeight();
                float tFade = 1f;
                if (this.revealTicks > TITLE_TOTAL - 12) {
                    tFade = Math.max(0f, (TITLE_TOTAL - this.revealTicks) / 12f);
                }
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                String title = "ÓRBITA ALCANÇADA";
                int tw = mc.fontRenderer.getStringWidth(title);
                mc.fontRenderer.drawStringWithShadow(title,
                    (w - tw) / 2, h / 2 - 60, color(0x55E0FF, tFade));
                String sub = "Voe até o satélite — sua nave espera ao lado";
                int sw = mc.fontRenderer.getStringWidth(sub);
                mc.fontRenderer.drawStringWithShadow(sub,
                    (w - sw) / 2, h / 2 - 48, color(0xC7D0DA, tFade));
                GL11.glDisable(GL11.GL_BLEND);
            }
        }
    }

    /** Quad preto full-screen com alpha (a "cortina"). */
    private void drawBlackQuad(float alpha, Minecraft mc) {
        if (alpha <= 0f) {
            return;
        }
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int w = res.getScaledWidth();
        int h = res.getScaledHeight();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(0f, 0f, 0f, Math.min(1f, alpha));
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertex(0, 0, -500);
        t.addVertex(0, h, -500);
        t.addVertex(w, h, -500);
        t.addVertex(w, 0, -500);
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    /** Cor ARGB com alpha relativo (para o fade do título). */
    private int color(int rgb, float factor) {
        int a = (int) (255 * Math.min(1f, Math.max(0f, factor)));
        return (a << 24) | rgb;
    }
}
