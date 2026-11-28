package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TOAST DE MISSÃO do deploy do satélite (v0.10.0 — substitui a
 * GuiDeployCinematic de tela cheia que o usuário classificou como
 * "porquíssima").
 *
 * IN-WORLD: nada de GUI, nada de pausar, nada de cena fake. Quando o
 * foguete T1 some no céu e o satélite entra em órbita, o dono recebe
 * UM CARTEIRO elegante:
 *
 *   [ OS-004 • TIER 1 ]         <- régua com o nome, ciano
 *   EM ÓRBITA — DOBRADIAS OK    <- status verde
 *   1.000.000 RF/t              <- geração inicial
 *   ▓▓▓▓▓▓▓▓▓▓▓▓░░  93%         <- barra de vida
 *
 * Entra deslizando da direita (6s), segura 5s, sai deslizando. Som
 * "random.levelup" (existe no vanilla — nada de soundEvent inventado).
 * Registrado no MinecraftForge.EVENT_BUS (a lição do NavRiderHider).
 */
@SideOnly(Side.CLIENT)
public class OrbitDeployToast {

    private static String satName = "";
    private static int satTier = 1;
    private static float satHealth = 1f;
    private static int ticks = -1;

    private static final int SLIDE_IN = 8;    // 0.4s entrando
    private static final int HOLD = 110;      // ~5.5s segurando
    private static final int SLIDE_OUT = 12;  // 0.6s saindo

    /** Mostra o toast (chamado pelo handler do MessageDeployCinematic). */
    public static void show(String name, int tier, float health) {
        satName = name;
        satTier = tier;
        satHealth = health;
        ticks = 0;
        Minecraft mc = Minecraft.getMinecraft();
        mc.getSoundHandler().playSound(PositionedSoundRecord.func_147674_a(
            new ResourceLocation("random.levelup"), 0.7F));
    }

    @SubscribeEvent
    public void onOverlay(net.minecraftforge.client.event.RenderGameOverlayEvent.Post event) {
        if (event.type != net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HOTBAR) {
            return;
        }
        if (ticks < 0) {
            return;
        }
        ticks++;
        int total = SLIDE_IN + HOLD + SLIDE_OUT;
        if (ticks > total) {
            ticks = -1;
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int w = res.getScaledWidth();

        // Largura do cartão pelo texto mais largo.
        String title = satName + "  •  TIER " + satTier;
        String line1 = "EM ÓRBITA — DOBRADIÇAS OK";
        long gen = Math.round(1000000L * (0.5f + (satHealth - 0.2f) * 0.625f));
        String line2 = String.format("%,d RF/t", gen).replace(',', '.');
        int cardW = Math.max(
            mc.fontRenderer.getStringWidth(title) + 36,
            Math.max(mc.fontRenderer.getStringWidth(line1),
                mc.fontRenderer.getStringWidth(line2)) + 36);
        int cardH = 46;
        int x = w - cardW - 6;
        int y = 8;

        // Deslize: entra da direita, segura, sai pra direita.
        float slide = 0f;
        if (ticks < SLIDE_IN) {
            slide = 1f - (float) ticks / SLIDE_IN; // 1 -> 0
        } else if (ticks > SLIDE_IN + HOLD) {
            slide = (float) (ticks - SLIDE_IN - HOLD) / SLIDE_OUT; // 0 -> 1
        }
        slide = slide * slide * (3f - 2f * slide); // smoothstep
        int drawX = x + (int) (slide * (cardW + 12));

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // Cartão escuro com régua ciano à esquerda.
        drawRect(drawX, y, drawX + cardW, y + cardH, 0xB00D1420);
        drawRect(drawX, y, drawX + 3, y + cardH, 0xFF55E0FF);
        drawRect(drawX + 3, y + cardH - 1, drawX + cardW, y + cardH, 0xFF1B2B3D);

        // Título.
        mc.fontRenderer.drawStringWithShadow(title, drawX + 12, y + 6, 0x55E0FF);
        mc.fontRenderer.drawStringWithShadow(line1, drawX + 12, y + 18, 0x7CFC00);
        mc.fontRenderer.drawStringWithShadow(line2, drawX + 12, y + 29, 0xFFD970);

        // Barra de vida.
        int bx = drawX + 12;
        int by = y + 40;
        int bw = cardW - 24;
        drawRect(bx, by, bx + bw, by + 3, 0xFF1B2B3D);
        int fill = (int) (bw * Math.max(0f, Math.min(1f, satHealth)));
        if (fill > 0) {
            drawRect(bx, by, bx + fill, by + 3,
                satHealth > 0.4f ? 0xFF3DDC55 : 0xFFE04040);
        }
        String pct = Math.round(satHealth * 100) + "%";
        mc.fontRenderer.drawStringWithShadow(pct,
            bx + bw + 4 - mc.fontRenderer.getStringWidth("100%"), by - 3, 0xC7D0DA);

        GL11.glDisable(GL11.GL_BLEND);
    }

    /** Rect ARGB sólido (helper local, sem depender de Gui). */
    private static void drawRect(int x0, int y0, int x1, int y1, int argb) {
        // Usa o utilitário do Gui via instância anônima não — desenha
        // direto com Tessellator (mais leve num overlay).
        net.minecraft.client.renderer.Tessellator t =
            net.minecraft.client.renderer.Tessellator.instance;
        float a = ((argb >>> 24) & 0xFF) / 255f;
        float r = ((argb >>> 16) & 0xFF) / 255f;
        float g = ((argb >>> 8) & 0xFF) / 255f;
        float b = (argb & 0xFF) / 255f;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(r, g, b, a);
        t.startDrawingQuads();
        t.addVertex(x0, y1, 0);
        t.addVertex(x1, y1, 0);
        t.addVertex(x1, y0, 0);
        t.addVertex(x0, y0, 0);
        t.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }
}
