package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.machine.TileOilExtractor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderizador 3D do Extrator de Petróleo (Gangorra clássica industrial / Pumpjack):
 *  - Base reforçada de vigas I sobre a plataforma 3x3
 *  - Cavalete piramidal (Samson Post) em aço rebitado
 *  - Gangorra basculante articulada com cabeça-de-cavalo curva e cabo de aço
 *  - Haste polida mergulhando no cabeçote do poço + válvula industrial
 *  - Manivelas com contrapesos giratórios e bielas acopladas ao motor traseiro
 */
@SideOnly(Side.CLIENT)
public class TileOilExtractorRenderer extends TileEntitySpecialRenderer {

    private static final ResourceLocation TEX_STEEL = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_gold.png");

    private static final float LX = 0.42f;
    private static final float LY = 0.82f;
    private static final float LZ = 0.38f;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileOilExtractor)) {
            return;
        }
        TileOilExtractor tile = (TileOilExtractor) te;

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (!tile.formed) {
            renderUnformedCore();
        } else {
            // Alinha o lado traseiro (+Z local) com a Entrada de Energia RF
            float yaw;
            switch (tile.motorSide & 3) {
                case 1: yaw = -90f; break;
                case 2: yaw = 180f; break;
                case 3: yaw = 90f; break;
                default: yaw = 0f; break;
            }
            GL11.glRotatef(yaw, 0f, 1f, 0f);
            float phase = tile.swingPhase + (tile.running ? partialTicks * 0.085f : 0f);
            renderPumpjack(tile, phase);
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    private void renderUnformedCore() {
        bind(TEX_DARK);
        drawBox(-0.48f, 0.00f, -0.48f, 0.48f, 0.85f, 0.48f, 0.38f, 0.42f, 0.48f);
        bind(TEX_STEEL);
        drawBox(-0.38f, 0.85f, -0.38f, 0.38f, 1.00f, 0.38f, 0.72f, 0.78f, 0.86f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawGlowBox(-0.18f, 1.00f, -0.18f, 0.18f, 1.08f, 0.18f, 0.95f, 0.45f, 0.12f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderPumpjack(TileOilExtractor tile, float phase) {
        // 1. Tubo central no nível do poço (0.0..1.0) e tampa metálica do bloco central
        bind(TEX_DARK);
        drawBox(-0.49f, 0.00f, -0.49f, 0.49f, 1.00f, 0.49f, 0.32f, 0.36f, 0.42f);

        // Chassi de vigas I assentado em y = 1.00 .. 1.18 sobre a base 3x3
        bind(TEX_DARK);
        drawBox(-0.46f, 1.00f, -1.35f, -0.28f, 1.18f, 1.35f, 0.34f, 0.38f, 0.44f);
        drawBox(0.28f, 1.00f, -1.35f, 0.46f, 1.18f, 1.35f, 0.34f, 0.38f, 0.44f);
        drawBox(-0.55f, 1.00f, -1.35f, 0.55f, 1.15f, -1.12f, 0.30f, 0.34f, 0.40f);
        drawBox(-0.55f, 1.00f, 1.08f, 0.55f, 1.15f, 1.35f, 0.30f, 0.34f, 0.40f);
        drawBox(-0.48f, 1.00f, -0.18f, 0.48f, 1.16f, 0.22f, 0.36f, 0.40f, 0.46f);

        // 2. Cabeçote do poço (-Z frontal em z = -1.18) com válvula circular vermelha
        bind(TEX_STEEL);
        drawCylinderY(0f, 1.02f, -1.18f, 0.18f, 0.18f, 0.42f, 12, 0.65f, 0.70f, 0.76f);
        drawCylinderY(0f, 1.44f, -1.18f, 0.24f, 0.24f, 0.08f, 12, 0.48f, 0.52f, 0.58f);
        bind(TEX_DARK);
        drawCylinderY(0f, 1.52f, -1.18f, 0.09f, 0.09f, 0.22f, 10, 0.38f, 0.42f, 0.48f);

        // 3. Cavalete em A (Samson Post) sustentando o eixo da gangorra em (0, 3.05, 0.02)
        float topY = 3.05f;
        float topZ = 0.02f;
        bind(TEX_STEEL);
        drawStrut(-0.40f, 1.16f, -0.34f, -0.18f, topY, topZ, 0.065f, 0.78f, 0.82f, 0.88f);
        drawStrut(0.40f, 1.16f, -0.34f, 0.18f, topY, topZ, 0.065f, 0.78f, 0.82f, 0.88f);
        drawStrut(-0.40f, 1.16f, 0.38f, -0.18f, topY, topZ, 0.065f, 0.72f, 0.76f, 0.82f);
        drawStrut(0.40f, 1.16f, 0.38f, 0.18f, topY, topZ, 0.065f, 0.72f, 0.76f, 0.82f);

        // Travessas horizontais do cavalete
        bind(TEX_DARK);
        drawBox(-0.34f, 2.02f, -0.22f, 0.34f, 2.11f, 0.26f, 0.36f, 0.40f, 0.46f);
        // Mancal central do pivô
        drawBox(-0.26f, topY - 0.08f, topZ - 0.14f, 0.26f, topY + 0.12f, topZ + 0.14f, 0.42f, 0.46f, 0.52f);

        // 4. Motor elétrico traseiro (+Z) e caixa de redução
        bind(TEX_DARK);
        drawBox(-0.34f, 1.16f, 0.54f, 0.34f, 1.66f, 1.24f, 0.34f, 0.38f, 0.44f);
        bind(TEX_STEEL);
        drawBox(-0.28f, 1.66f, 0.62f, 0.28f, 1.88f, 1.18f, 0.62f, 0.68f, 0.74f);
        // Faixa amarela/laranja industrial no motor + LED de estado
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        float lr = 0.95f, lg = 0.25f, lb = 0.20f;
        if (tile.statusCode == TileOilExtractor.STATUS_EXTRACTING) {
            lr = 0.22f; lg = 0.95f; lb = 0.45f;
        } else if (tile.statusCode == TileOilExtractor.STATUS_FULL) {
            lr = 0.95f; lg = 0.78f; lb = 0.18f;
        }
        drawGlowBox(-0.14f, 1.72f, 1.18f, 0.14f, 1.82f, 1.24f, lr, lg, lb);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);

        // 5. Manivelas e contrapesos giratórios (z = 0.90, y = 1.66)
        float crankY = 1.66f;
        float crankZ = 0.90f;
        float crankRad = 0.34f;
        float crankDeg = (float) Math.toDegrees(phase);
        float beamDeg = (float) (Math.sin(phase) * 19.0);
        double beamRad = Math.toRadians(beamDeg);

        for (int side = -1; side <= 1; side += 2) {
            GL11.glPushMatrix();
            GL11.glTranslatef(side * 0.42f, crankY, crankZ);
            GL11.glRotatef(crankDeg, 1f, 0f, 0f);
            // Braço da manivela + contrapeso em leque antigo
            bind(TEX_DARK);
            drawBox(-0.04f, -0.10f, -0.08f, 0.04f, 0.42f, 0.08f, 0.34f, 0.38f, 0.44f);
            bind(TEX_GOLD);
            drawBox(-0.06f, -0.44f, -0.24f, 0.06f, -0.10f, 0.24f, 0.88f, 0.66f, 0.20f);
            GL11.glPopMatrix();
        }

        // Ponto exato do pino da manivela e da traseira da gangorra para acoplar as duas bielas
        float pinY = crankY + (float) Math.cos(phase) * crankRad;
        float pinZ = crankZ + (float) Math.sin(phase) * crankRad;
        float rearZ = topZ + (float) Math.cos(beamRad) * 0.92f;
        float rearY = topY + 0.14f - (float) Math.sin(beamRad) * 0.92f;

        bind(TEX_STEEL);
        for (int side = -1; side <= 1; side += 2) {
            drawStrut(side * 0.38f, pinY, pinZ, side * 0.24f, rearY, rearZ, 0.036f, 0.76f, 0.80f, 0.86f);
        }
        // Eixo equalizador traseiro
        bind(TEX_DARK);
        drawBox(-0.30f, rearY - 0.04f, rearZ - 0.04f, 0.30f, rearY + 0.04f, rearZ + 0.04f, 0.40f, 0.44f, 0.50f);

        // 6. Gangorra principal (Walking Beam) + Cabeça de Cavalo
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, topY + 0.14f, topZ);
        GL11.glRotatef(beamDeg, 1f, 0f, 0f);

        // Viga principal horizontal
        bind(TEX_STEEL);
        drawBox(-0.14f, -0.11f, -1.06f, 0.14f, 0.11f, 0.96f, 0.82f, 0.86f, 0.92f);
        bind(TEX_GOLD);
        drawBox(-0.15f, -0.03f, -0.96f, 0.15f, 0.04f, 0.88f, 0.92f, 0.68f, 0.16f);

        // Cabeça de Cavalo (Horsehead curva) na ponta frontal z = -1.06..-1.24
        bind(TEX_DARK);
        for (int seg = 0; seg < 6; seg++) {
            float a = -0.55f + seg * 0.24f;
            float hy = (float) Math.sin(a) * 0.46f;
            float hz = -1.04f - (float) Math.cos(a) * 0.22f;
            drawBox(-0.16f, hy - 0.08f, hz - 0.05f, 0.16f, hy + 0.08f, hz + 0.05f, 0.38f, 0.42f, 0.48f);
        }
        GL11.glPopMatrix();

        // 7. Cabo de aço + Haste polida vertical entrando no cabeçote do poço
        float headTipY = (topY + 0.14f) + (float) Math.sin(beamRad) * 1.18f;
        float rodBottom = 1.62f;
        if (headTipY > rodBottom) {
            bind(TEX_STEEL);
            drawCylinderY(-0.07f, rodBottom, -1.18f, 0.018f, 0.018f, headTipY - rodBottom, 8, 0.86f, 0.90f, 0.95f);
            drawCylinderY(0.07f, rodBottom, -1.18f, 0.018f, 0.018f, headTipY - rodBottom, 8, 0.86f, 0.90f, 0.95f);
            drawBox(-0.09f, rodBottom + 0.25f, -1.21f, 0.09f, rodBottom + 0.33f, -1.15f, 0.60f, 0.64f, 0.70f);
        }
    }

    private static void drawCylinderY(float cx, float y0, float cz, float rb, float rt, float h, int sides,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        float y1 = y0 + h;
        t.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2.0 * i / sides;
            double a1 = Math.PI * 2.0 * (i + 1) / sides;
            double am = 0.5 * (a0 + a1);
            float f = 0.48f + 0.52f * Math.max(0f, (float) Math.cos(am) * LX + (float) Math.sin(am) * LZ);
            t.setColorOpaque_F(cr * f, cg * f, cb * f);
            t.addVertexWithUV(cx + Math.cos(a0) * rb, y0, cz + Math.sin(a0) * rb, i / (double) sides, 1);
            t.addVertexWithUV(cx + Math.cos(a1) * rb, y0, cz + Math.sin(a1) * rb, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(cx + Math.cos(a1) * rt, y1, cz + Math.sin(a1) * rt, (i + 1) / (double) sides, 0);
            t.addVertexWithUV(cx + Math.cos(a0) * rt, y1, cz + Math.sin(a0) * rt, i / (double) sides, 0);
        }
        t.draw();
    }

    private static void drawStrut(float x0, float y0, float z0, float x1, float y1, float z1,
            float w, float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_F(r, g, b);
        t.addVertexWithUV(x0 - w, y0, z0, 0, 0);
        t.addVertexWithUV(x0 + w, y0, z0, 1, 0);
        t.addVertexWithUV(x1 + w, y1, z1, 1, 1);
        t.addVertexWithUV(x1 - w, y1, z1, 0, 1);

        t.setColorOpaque_F(r * 0.78f, g * 0.78f, b * 0.78f);
        t.addVertexWithUV(x0, y0, z0 - w, 0, 0);
        t.addVertexWithUV(x0, y0, z0 + w, 1, 0);
        t.addVertexWithUV(x1, y1, z1 + w, 1, 1);
        t.addVertexWithUV(x1, y1, z1 - w, 0, 1);
        t.draw();
    }

    private static void drawBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_F(r * 0.92f, g * 0.92f, b * 0.92f);
        t.addVertexWithUV(x0, y1, z1, 0, 1); t.addVertexWithUV(x1, y1, z1, 1, 1);
        t.addVertexWithUV(x1, y1, z0, 1, 0); t.addVertexWithUV(x0, y1, z0, 0, 0);

        t.setColorOpaque_F(r * 0.56f, g * 0.56f, b * 0.56f);
        t.addVertexWithUV(x0, y0, z0, 0, 1); t.addVertexWithUV(x1, y0, z0, 1, 1);
        t.addVertexWithUV(x1, y0, z1, 1, 0); t.addVertexWithUV(x0, y0, z1, 0, 0);

        t.setColorOpaque_F(r * 0.78f, g * 0.78f, b * 0.78f);
        t.addVertexWithUV(x1, y0, z1, 0, 0); t.addVertexWithUV(x1, y0, z0, 1, 0);
        t.addVertexWithUV(x1, y1, z0, 1, 1); t.addVertexWithUV(x1, y1, z1, 0, 1);

        t.setColorOpaque_F(r * 0.68f, g * 0.68f, b * 0.68f);
        t.addVertexWithUV(x0, y0, z0, 0, 0); t.addVertexWithUV(x0, y0, z1, 1, 0);
        t.addVertexWithUV(x0, y1, z1, 1, 1); t.addVertexWithUV(x0, y1, z0, 0, 1);

        t.setColorOpaque_F(r * 0.84f, g * 0.84f, b * 0.84f);
        t.addVertexWithUV(x0, y0, z1, 0, 0); t.addVertexWithUV(x1, y0, z1, 1, 0);
        t.addVertexWithUV(x1, y1, z1, 1, 1); t.addVertexWithUV(x0, y1, z1, 0, 1);

        t.setColorOpaque_F(r * 0.72f, g * 0.72f, b * 0.72f);
        t.addVertexWithUV(x0, y1, z0, 0, 1); t.addVertexWithUV(x1, y1, z0, 1, 1);
        t.addVertexWithUV(x1, y0, z0, 1, 0); t.addVertexWithUV(x0, y0, z0, 0, 0);
        t.draw();
    }

    private static void drawGlowBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, 1f);
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0);
        t.draw();
    }

    private static void bind(ResourceLocation loc) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
    }
}