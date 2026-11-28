package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.handler.SpaceSuitHandler;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Efeito progressivo de hipóxia no espaço quando o jogador retira qualquer peça
 * do Traje Espacial na órbita: visão cada vez mais turva, embaçada e escurecida
 * em túnel até a morte por sufocamento.
 */
@SideOnly(Side.CLIENT)
public class SpaceHypoxiaOverlay {

    private float hypoxia = 0.0f;
    private float pulseTick = 0.0f;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
            this.hypoxia = 0.0f;
            return;
        }
        boolean suffocating = OrbitDimensionManager.isOrbitWorld(mc.theWorld)
            && !mc.thePlayer.capabilities.isCreativeMode
            && mc.thePlayer.isEntityAlive()
            && !SpaceSuitHandler.isInFlightLock(mc.thePlayer)
            && !SpaceSuitHandler.hasFullSuit(mc.thePlayer);

        if (suffocating) {
            // Acompanha os ~10s (10 hits de 10%) até o escurecimento quase total
            this.hypoxia = Math.min(1.0f, this.hypoxia + 0.0053f);
            this.pulseTick += 0.22f;
        } else {
            this.hypoxia = Math.max(0.0f, this.hypoxia - 0.04f);
            this.pulseTick = 0.0f;
        }
    }

    @SubscribeEvent
    public void onFovUpdate(FOVUpdateEvent event) {
        if (this.hypoxia > 0.08f) {
            float wobble = (float) Math.sin(this.pulseTick * 1.15f) * 0.07f * this.hypoxia;
            event.newfov = event.newfov * (1.0f - 0.14f * this.hypoxia + wobble);
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.ALL || this.hypoxia <= 0.01f) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = event.resolution;
        int w = sr.getScaledWidth();
        int h = sr.getScaledHeight();

        float lvl = this.hypoxia;
        float heartbeat = 0.5f + 0.5f * (float) Math.sin(this.pulseTick * 1.6f);

        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        // 1. Camadas turvas oscilantes simulando perda de foco/visão embaçada
        float hazeAlpha = Math.min(0.68f, lvl * 0.56f + heartbeat * 0.12f * lvl);
        drawFullRect(0, 0, w, h, 0.04f, 0.07f, 0.12f, hazeAlpha);

        float blurShiftX = (float) Math.sin(this.pulseTick * 1.7f) * 8.0f * lvl;
        float blurShiftY = (float) Math.cos(this.pulseTick * 1.3f) * 5.0f * lvl;
        drawFullRect(blurShiftX, blurShiftY, w + blurShiftX, h + blurShiftY,
            0.15f, 0.24f, 0.34f, lvl * 0.22f);
        drawFullRect(-blurShiftX, -blurShiftY, w - blurShiftX, h - blurShiftY,
            0.05f, 0.04f, 0.08f, lvl * 0.22f);

        // 2. Vinheta radial progressiva fechando a visão das bordas para o centro
        drawRadialVignette(w, h, lvl, heartbeat);

        // 3. Escurecimento final próximo à morte
        if (lvl > 0.65f) {
            float blackout = (lvl - 0.65f) / 0.35f;
            drawFullRect(0, 0, w, h, 0.0f, 0.0f, 0.0f, Math.min(0.88f, blackout * 0.85f));
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Alerta discreto piscando no topo
        if (heartbeat > 0.30f) {
            String warn = "\u00a7c\u00a7lO2 CR\u00cdTICO \u2014 VISTA O TRAJE ESPACIAL COMPLETO";
            int tw = mc.fontRenderer.getStringWidth(warn);
            mc.fontRenderer.drawStringWithShadow(warn, (w - tw) / 2, 18, 0xFFFFFF);
        }

        GL11.glPopMatrix();
    }

    private static void drawRadialVignette(int w, int h, float lvl, float heartbeat) {
        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float outerR = (float) Math.sqrt(cx * cx + cy * cy) * 1.05f;
        float clearRadius = Math.max(0.06f, 0.68f - lvl * 0.56f) * Math.min(cx, cy);
        float edgeAlpha = Math.min(0.96f, 0.32f + lvl * 0.62f + heartbeat * 0.08f * lvl);

        Tessellator t = Tessellator.instance;
        int segs = 32;
        t.startDrawing(GL11.GL_TRIANGLE_STRIP);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2.0 * i / segs;
            float cos = (float) Math.cos(a);
            float sin = (float) Math.sin(a);
            t.setColorRGBA_F(0.0f, 0.0f, 0.02f, edgeAlpha);
            t.addVertex(cx + cos * outerR, cy + sin * outerR, 0.0);
            t.setColorRGBA_F(0.02f, 0.03f, 0.06f, 0.02f * lvl);
            t.addVertex(cx + cos * clearRadius, cy + sin * clearRadius, 0.0);
        }
        t.draw();
    }

    private static void drawFullRect(float x0, float y0, float x1, float y1,
            float r, float g, float b, float a) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, a);
        t.addVertex(x0, y1, 0.0);
        t.addVertex(x1, y1, 0.0);
        t.addVertex(x1, y0, 0.0);
        t.addVertex(x0, y0, 0.0);
        t.draw();
    }
}