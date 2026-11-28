package com.orbitalsunworks.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Modelo 3D detalhado do Traje Espacial Pressurizado (EVA Astronaut Suit).
 * - Capacete (slot 0): cúpula esférica pressurizada branca, anel de titânio,
 *   visor dourado reflexivo curvo, pods de comunicação com antena lateral e faróis.
 * - Peitoral (slot 1): tronco EVA articulado, módulo peitoral DCM com telemetria,
 *   ombreiras com faixa espacial e mochila PLSS com dois cilindros metálicos de O2.
 * - Calça (slot 2): cinto utilitário com fivela, joelheiras sanfonadas e bolsos laterais.
 * - Botas (slot 3): botas lunares reforçadas + propulsores a gás traseiros/laterais
 *   com chama azulada quando o jogador está voando na órbita.
 */
@SideOnly(Side.CLIENT)
public class ModelSpaceSuit extends ModelBiped {

    private static final ResourceLocation TEX_WHITE = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_body.png");
    private static final ResourceLocation TEX_GOLD = new ResourceLocation(
        "orbitalsunworks", "textures/models/sat_gold.png");
    private static final ResourceLocation TEX_DARK = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_engine.png");

    private static final ModelSpaceSuit[] SLOT_MODELS = new ModelSpaceSuit[] {
        new ModelSpaceSuit(0),
        new ModelSpaceSuit(1),
        new ModelSpaceSuit(2),
        new ModelSpaceSuit(3)
    };

    private static final float LX = 0.42f;
    private static final float LY = 0.82f;
    private static final float LZ = 0.38f;

    private final int armorSlot;

    public ModelSpaceSuit(int armorSlot) {
        super(0.0f, 0.0f, 64, 32);
        this.armorSlot = armorSlot;
    }

    public static ModelSpaceSuit getModelForSlot(int slot, EntityLivingBase entity) {
        int idx = Math.max(0, Math.min(3, slot));
        ModelSpaceSuit m = SLOT_MODELS[idx];
        m.isChild = entity != null && entity.isChild();
        m.isSneak = entity != null && entity.isSneaking();
        m.isRiding = entity != null && entity.isRiding();
        return m;
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);

        float shade = 0.94f;
        boolean inOrbit = false;
        boolean flying = false;
        if (entity != null && entity.worldObj != null) {
            inOrbit = OrbitDimensionManager.isOrbitWorld(entity.worldObj);
            if (!inOrbit) {
                shade = Math.max(0.42f, Math.min(1.0f, entity.getBrightness(0f) * 0.85f + 0.25f));
            }
        }
        if (entity instanceof EntityPlayer) {
            flying = ((EntityPlayer) entity).capabilities.isFlying;
        }

        GL11.glPushMatrix();
        if (this.isChild) {
            GL11.glScalef(0.75f, 0.75f, 0.75f);
            GL11.glTranslatef(0.0f, 16.0f * f5, 0.0f);
        }
        if (entity != null && entity.isSneaking()) {
            GL11.glTranslatef(0.0f, 0.2f, 0.0f);
        }

        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);

        if (this.armorSlot == 0) {
            renderHelmet(f5, shade, f2);
        } else if (this.armorSlot == 1) {
            renderChestAndArms(f5, shade, f2);
        } else if (this.armorSlot == 2) {
            renderLeggings(f5, shade);
        } else if (this.armorSlot == 3) {
            renderBoots(f5, shade, flying, f2);
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glPopMatrix();
    }

    // ======================================================================
    // CAPACETE ESPACIAL ARREDONDADO + VISOR DOURADO REFLEXIVO
    // ======================================================================
    private void renderHelmet(float scale, float shade, float ticks) {
        GL11.glPushMatrix();
        this.bipedHead.postRender(scale);

        // Anel metálico de vedação do pescoço
        bind(TEX_DARK);
        drawCylinderY(0f, -0.02f, 0f, 0.285f, 0.295f, 0.075f, 16, 0.48f * shade, 0.52f * shade, 0.58f * shade);
        bind(TEX_WHITE);
        drawCylinderY(0f, -0.045f, 0f, 0.275f, 0.285f, 0.03f, 16, 0.85f * shade, 0.88f * shade, 0.92f * shade);

        // Cúpula esférica pressurizada branca (multi-anéis suaves)
        drawHelmetDome(0f, -0.265f, 0.005f, 0.335f, 0.355f, 0.345f, 10, 20, shade);

        // Moldura escura ao redor do visor frontal (z negativo é frente)
        bind(TEX_DARK);
        drawBox(-0.245f, -0.455f, -0.352f, 0.245f, -0.405f, -0.24f, 0.24f * shade, 0.27f * shade, 0.32f * shade);
        drawBox(-0.245f, -0.105f, -0.345f, 0.245f, -0.055f, -0.24f, 0.24f * shade, 0.27f * shade, 0.32f * shade);
        drawBox(-0.275f, -0.42f, -0.335f, -0.225f, -0.08f, -0.22f, 0.28f * shade, 0.31f * shade, 0.36f * shade);
        drawBox(0.225f, -0.42f, -0.335f, 0.275f, -0.08f, -0.22f, 0.28f * shade, 0.31f * shade, 0.36f * shade);

        // Visor dourado curvo (cobre todo o arco frontal)
        bind(TEX_GOLD);
        drawCurvedVisor(0f, -0.260f, -0.015f, 0.342f, 0.335f, 0.352f, shade);

        // Pods laterais de comunicação (orelhas) + trava da viseira solar
        bind(TEX_WHITE);
        drawBox(-0.375f, -0.34f, -0.08f, -0.31f, -0.17f, 0.10f, 0.86f * shade, 0.89f * shade, 0.94f * shade);
        drawBox(0.31f, -0.34f, -0.08f, 0.375f, -0.17f, 0.10f, 0.86f * shade, 0.89f * shade, 0.94f * shade);
        bind(TEX_DARK);
        drawBox(-0.388f, -0.30f, -0.04f, -0.365f, -0.21f, 0.06f, 0.35f * shade, 0.40f * shade, 0.48f * shade);
        drawBox(0.365f, -0.30f, -0.04f, 0.388f, -0.21f, 0.06f, 0.35f * shade, 0.40f * shade, 0.48f * shade);

        // Antena de comunicação lateral esquerda
        drawBox(0.355f, -0.62f, 0.02f, 0.375f, -0.28f, 0.04f, 0.68f * shade, 0.72f * shade, 0.78f * shade);

        // Faróis de capacete na testa + LED ciano na antena/pod
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        drawGlowBox(-0.22f, -0.495f, -0.345f, -0.13f, -0.445f, -0.295f, 0.88f, 0.96f, 1.0f);
        drawGlowBox(0.13f, -0.495f, -0.345f, 0.22f, -0.445f, -0.295f, 0.88f, 0.96f, 1.0f);
        float blink = (ticks % 40f) < 10f ? 1.0f : 0.35f;
        drawGlowBox(0.350f, -0.65f, 0.015f, 0.380f, -0.62f, 0.045f, 0.10f * blink, 0.92f * blink, 1.0f * blink);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    // ======================================================================
    // PEITORAL EVA + MOCHILA PLSS COM TANQUES DE OXIGÊNIO + BRAÇOS
    // ======================================================================
    private void renderChestAndArms(float scale, float shade, float ticks) {
        // Tronco & PLSS
        GL11.glPushMatrix();
        this.bipedBody.postRender(scale);

        // Casco branco pressurizado do tronco
        bind(TEX_WHITE);
        drawBox(-0.285f, -0.015f, -0.175f, 0.285f, 0.675f, 0.175f, 0.96f * shade, 0.97f * shade, 0.99f * shade);
        // Nervuras laterais e placa superior do ombro
        drawBox(-0.305f, 0.01f, -0.155f, 0.305f, 0.26f, 0.155f, 0.90f * shade, 0.93f * shade, 0.97f * shade);
        // Anel metálico da cintura
        bind(TEX_DARK);
        drawBox(-0.295f, 0.625f, -0.185f, 0.295f, 0.705f, 0.185f, 0.42f * shade, 0.46f * shade, 0.52f * shade);

        // Faixas espaciais no peito / ombros
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawGlowBox(-0.26f, 0.00f, -0.182f, -0.18f, 0.38f, -0.168f, 0.14f * shade, 0.45f * shade, 0.78f * shade);
        drawGlowBox(0.18f, 0.00f, -0.182f, 0.26f, 0.38f, -0.168f, 0.14f * shade, 0.45f * shade, 0.78f * shade);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // Módulo peitoral DCM (Display & Control Module)
        bind(TEX_DARK);
        drawBox(-0.145f, 0.12f, -0.235f, 0.145f, 0.40f, -0.170f, 0.38f * shade, 0.42f * shade, 0.48f * shade);
        bind(TEX_WHITE);
        drawBox(-0.125f, 0.14f, -0.245f, 0.125f, 0.38f, -0.225f, 0.78f * shade, 0.82f * shade, 0.88f * shade);

        // Conectores vermelho/azul + visor de telemetria O2 no peito
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float prevLX = OpenGlHelper.lastBrightnessX;
        float prevLY = OpenGlHelper.lastBrightnessY;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        drawGlowBox(-0.095f, 0.165f, -0.252f, 0.095f, 0.255f, -0.242f, 0.06f, 0.85f, 0.98f);
        drawGlowBox(-0.095f, 0.295f, -0.252f, -0.025f, 0.345f, -0.240f, 0.92f, 0.25f, 0.22f);
        drawGlowBox(0.025f, 0.295f, -0.252f, 0.095f, 0.345f, -0.240f, 0.20f, 0.62f, 0.98f);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        // MOCHILA DE SUPORTE À VIDA (PLSS) nas costas (+Z)
        bind(TEX_WHITE);
        drawBox(-0.245f, 0.02f, 0.175f, 0.245f, 0.64f, 0.375f, 0.92f * shade, 0.94f * shade, 0.98f * shade);
        drawBox(-0.215f, -0.05f, 0.195f, 0.215f, 0.02f, 0.345f, 0.82f * shade, 0.86f * shade, 0.92f * shade);
        // Radiador escuro superior na mochila
        bind(TEX_DARK);
        for (int i = 0; i < 4; i++) {
            float ry = 0.08f + i * 0.06f;
            drawBox(-0.175f, ry, 0.365f, 0.175f, ry + 0.025f, 0.395f, 0.35f * shade, 0.40f * shade, 0.48f * shade);
        }

        // DOIS TANQUES CILÍNDRICOS METÁLICOS DE OXIGÊNIO acoplados na mochila
        for (int side = -1; side <= 1; side += 2) {
            float tx = side * 0.145f;
            float tz = 0.395f;
            bind(TEX_WHITE);
            drawCylinderY(tx, 0.16f, tz, 0.068f, 0.068f, 0.42f, 12, 0.86f * shade, 0.91f * shade, 0.96f * shade);
            // Cúpulas superior/inferior dos cilindros
            drawCylinderY(tx, 0.12f, tz, 0.038f, 0.068f, 0.04f, 12, 0.72f * shade, 0.76f * shade, 0.84f * shade);
            drawCylinderY(tx, 0.58f, tz, 0.068f, 0.032f, 0.04f, 12, 0.72f * shade, 0.76f * shade, 0.84f * shade);
            // Cinta metálica dourada/escura segurando os tanques
            bind(TEX_GOLD);
            drawCylinderY(tx, 0.28f, tz, 0.072f, 0.072f, 0.04f, 12, 0.95f * shade, 0.82f * shade, 0.42f * shade);
            drawCylinderY(tx, 0.46f, tz, 0.072f, 0.072f, 0.04f, 12, 0.95f * shade, 0.82f * shade, 0.42f * shade);
        }

        // LED vertical de oxigênio atrás da mochila
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 235f, 235f);
        float pulse = 0.65f + 0.35f * (float) Math.sin(ticks * 0.14f);
        drawGlowBox(-0.025f, 0.36f, 0.375f, 0.025f, 0.56f, 0.395f, 0.10f * pulse, 0.95f * pulse, 0.60f * pulse);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();

        // Braço direito e esquerdo
        renderArm(this.bipedRightArm, -0.0625f, shade, -1);
        renderArm(this.bipedLeftArm, 0.0625f, shade, 1);
    }

    private void renderArm(net.minecraft.client.model.ModelRenderer arm, float cx, float shade, int side) {
        GL11.glPushMatrix();
        arm.postRender(0.0625f);

        // Ombreira curvada + patch lateral
        bind(TEX_WHITE);
        drawBox(cx - 0.16f, -0.145f, -0.155f, cx + 0.16f, 0.10f, 0.155f, 0.95f * shade, 0.97f * shade, 1.0f * shade);
        // Manga superior
        drawCylinderY(cx, 0.06f, 0f, 0.142f, 0.150f, 0.18f, 10, 0.92f * shade, 0.94f * shade, 0.98f * shade);

        // Sanfona articulada do cotovelo
        bind(TEX_DARK);
        drawCylinderY(cx, 0.24f, 0f, 0.146f, 0.146f, 0.10f, 10, 0.36f * shade, 0.40f * shade, 0.46f * shade);

        // Antebraço branco + anel de pulso metálico
        bind(TEX_WHITE);
        drawCylinderY(cx, 0.34f, 0f, 0.136f, 0.142f, 0.14f, 10, 0.90f * shade, 0.93f * shade, 0.97f * shade);
        bind(TEX_DARK);
        drawCylinderY(cx, 0.48f, 0f, 0.145f, 0.145f, 0.045f, 10, 0.48f * shade, 0.52f * shade, 0.58f * shade);

        // Luva espacial reforçada (mão)
        bind(TEX_WHITE);
        drawBox(cx - 0.138f, 0.525f, -0.138f, cx + 0.138f, 0.655f, 0.138f, 0.76f * shade, 0.80f * shade, 0.86f * shade);

        // Patch colorido da missão no ombro externo
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        float px = cx + side * 0.162f;
        float x0 = Math.min(px, px - side * 0.01f);
        float x1 = Math.max(px, px - side * 0.01f);
        drawGlowBox(x0, -0.08f, -0.06f, x1, 0.03f, 0.06f, 0.95f * shade, 0.48f * shade, 0.12f * shade);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopMatrix();
    }

    // ======================================================================
    // CALÇA PRESSURIZADA + CINTO UTILITÁRIO + JOELHEIRAS SANFONADAS
    // ======================================================================
    private void renderLeggings(float scale, float shade) {
        // Cinto na cintura do tronco
        GL11.glPushMatrix();
        this.bipedBody.postRender(scale);
        bind(TEX_DARK);
        drawBox(-0.295f, 0.65f, -0.19f, 0.295f, 0.77f, 0.19f, 0.34f * shade, 0.38f * shade, 0.44f * shade);
        bind(TEX_WHITE);
        drawBox(-0.085f, 0.66f, -0.21f, 0.085f, 0.76f, -0.18f, 0.76f * shade, 0.80f * shade, 0.88f * shade);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        drawGlowBox(-0.04f, 0.695f, -0.215f, 0.04f, 0.725f, -0.205f, 0.12f, 0.85f, 0.98f);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();

        renderLegUpper(this.bipedRightLeg, scale, shade, -1);
        renderLegUpper(this.bipedLeftLeg, scale, shade, 1);
    }

    private void renderLegUpper(net.minecraft.client.model.ModelRenderer leg, float scale, float shade, int side) {
        GL11.glPushMatrix();
        leg.postRender(scale);

        // Coxa branca pressurizada
        bind(TEX_WHITE);
        drawCylinderY(0f, -0.02f, 0f, 0.155f, 0.162f, 0.28f, 10, 0.92f * shade, 0.95f * shade, 0.98f * shade);
        // Bolso utilitário lateral na coxa
        drawBox(side > 0 ? 0.12f : -0.18f, 0.08f, -0.08f,
            side > 0 ? 0.18f : -0.12f, 0.24f, 0.08f, 0.78f * shade, 0.82f * shade, 0.88f * shade);

        // Joelheira articulada cinza escuro + placa protetora frontal
        bind(TEX_DARK);
        drawCylinderY(0f, 0.26f, 0f, 0.152f, 0.152f, 0.12f, 10, 0.38f * shade, 0.42f * shade, 0.48f * shade);
        drawBox(-0.10f, 0.25f, -0.175f, 0.10f, 0.39f, -0.135f, 0.48f * shade, 0.52f * shade, 0.60f * shade);

        // Canela superior até o encaixe da bota
        bind(TEX_WHITE);
        drawCylinderY(0f, 0.38f, 0f, 0.146f, 0.150f, 0.13f, 10, 0.88f * shade, 0.91f * shade, 0.96f * shade);

        GL11.glPopMatrix();
    }

    // ======================================================================
    // BOTAS LUNARES + PROPULSORES A GÁS (RCS)
    // ======================================================================
    private void renderBoots(float scale, float shade, boolean flying, float ticks) {
        renderBoot(this.bipedRightLeg, scale, shade, -1, flying, ticks);
        renderBoot(this.bipedLeftLeg, scale, shade, 1, flying, ticks);
    }

    private void renderBoot(net.minecraft.client.model.ModelRenderer leg, float scale, float shade,
            int side, boolean flying, float ticks) {
        GL11.glPushMatrix();
        leg.postRender(scale);

        // Anel metálico do tornozelo
        bind(TEX_DARK);
        drawCylinderY(0f, 0.48f, 0f, 0.156f, 0.156f, 0.05f, 10, 0.48f * shade, 0.52f * shade, 0.58f * shade);

        // Corpo da bota EVA + biqueira frontal alongada
        bind(TEX_WHITE);
        drawBox(-0.15f, 0.53f, -0.15f, 0.15f, 0.71f, 0.15f, 0.90f * shade, 0.93f * shade, 0.97f * shade);
        drawBox(-0.14f, 0.60f, -0.21f, 0.14f, 0.71f, -0.14f, 0.84f * shade, 0.88f * shade, 0.94f * shade);

        // Solado pesado de titânio
        bind(TEX_DARK);
        drawBox(-0.158f, 0.705f, -0.22f, 0.158f, 0.775f, 0.165f, 0.28f * shade, 0.31f * shade, 0.36f * shade);

        // PROPULSOR A GÁS montado no calcanhar/lateral externa
        float tx = side * 0.165f;
        float tz = 0.085f;
        GL11.glPushMatrix();
        GL11.glTranslatef(tx, 0.54f, tz);
        GL11.glRotatef(18f, 1f, 0f, 0f);
        GL11.glRotatef(-side * 12f, 0f, 0f, 1f);

        // Câmara do propulsor + bocal cônico
        bind(TEX_DARK);
        drawCylinderY(0f, 0.00f, 0f, 0.050f, 0.055f, 0.09f, 10, 0.50f * shade, 0.54f * shade, 0.62f * shade);
        bind(TEX_GOLD);
        drawCylinderY(0f, 0.09f, 0f, 0.042f, 0.072f, 0.09f, 10, 0.88f * shade, 0.76f * shade, 0.42f * shade);

        if (flying) {
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            float prevLX = OpenGlHelper.lastBrightnessX;
            float prevLY = OpenGlHelper.lastBrightnessY;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
            float flick = 0.82f + 0.18f * (float) Math.sin(ticks * 0.9f + side);
            drawFlameCone(0f, 0.18f, 0f, 0.055f, 0.24f * flick);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, prevLX, prevLY);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
        GL11.glPopMatrix();

        GL11.glPopMatrix();
    }

    // ======================================================================
    // GEOMETRIA PROCEDURAL SUAVE
    // ======================================================================
    private static void drawHelmetDome(float cx, float cy, float cz, float rx, float ry, float rz,
            int latSteps, int lonSteps, float shade) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < latSteps; i++) {
            double p0 = -Math.PI * 0.5 + Math.PI * i / latSteps;
            double p1 = -Math.PI * 0.5 + Math.PI * (i + 1) / latSteps;
            float y0 = cy + (float) Math.sin(p0) * ry;
            float y1 = cy + (float) Math.sin(p1) * ry;
            float r0 = (float) Math.cos(p0);
            float r1 = (float) Math.cos(p1);

            for (int j = 0; j < lonSteps; j++) {
                double a0 = Math.PI * 2.0 * j / lonSteps;
                double a1 = Math.PI * 2.0 * (j + 1) / lonSteps;
                double am = 0.5 * (a0 + a1);
                double pm = 0.5 * (p0 + p1);
                float nx = (float) (Math.cos(pm) * Math.cos(am));
                float ny = (float) (-Math.sin(pm));
                float nz = (float) (Math.cos(pm) * Math.sin(am));
                float d = 0.52f + 0.48f * Math.max(0f, nx * LX + ny * LY - nz * LZ);
                t.setColorOpaque_F(0.96f * shade * d, 0.97f * shade * d, 0.99f * shade * d);

                t.addVertexWithUV(cx + Math.cos(a0) * r0 * rx, y0, cz + Math.sin(a0) * r0 * rz,
                    j / (double) lonSteps, i / (double) latSteps);
                t.addVertexWithUV(cx + Math.cos(a1) * r0 * rx, y0, cz + Math.sin(a1) * r0 * rz,
                    (j + 1) / (double) lonSteps, i / (double) latSteps);
                t.addVertexWithUV(cx + Math.cos(a1) * r1 * rx, y1, cz + Math.sin(a1) * r1 * rz,
                    (j + 1) / (double) lonSteps, (i + 1) / (double) latSteps);
                t.addVertexWithUV(cx + Math.cos(a0) * r1 * rx, y1, cz + Math.sin(a0) * r1 * rz,
                    j / (double) lonSteps, (i + 1) / (double) latSteps);
            }
        }
        t.draw();
    }

    private static void drawCurvedVisor(float cx, float cy, float cz, float rx, float ry, float rz, float shade) {
        Tessellator t = Tessellator.instance;
        int latSteps = 5;
        int lonSteps = 10;
        double latMin = -0.50;
        double latMax = 0.55;
        // Frente em ModelBiped é -Z -> ângulo central -PI/2
        double lonMin = -Math.PI * 0.5 - 0.85;
        double lonMax = -Math.PI * 0.5 + 0.85;

        t.startDrawingQuads();
        for (int i = 0; i < latSteps; i++) {
            double p0 = latMin + (latMax - latMin) * i / latSteps;
            double p1 = latMin + (latMax - latMin) * (i + 1) / latSteps;
            float y0 = cy + (float) Math.sin(p0) * ry;
            float y1 = cy + (float) Math.sin(p1) * ry;
            float r0 = (float) Math.cos(p0);
            float r1 = (float) Math.cos(p1);
            for (int j = 0; j < lonSteps; j++) {
                double a0 = lonMin + (lonMax - lonMin) * j / lonSteps;
                double a1 = lonMin + (lonMax - lonMin) * (j + 1) / lonSteps;
                float gloss = 0.72f + 0.28f * (1f - Math.abs((float) i / latSteps - 0.30f));
                t.setColorOpaque_F(1.00f * shade * gloss, 0.84f * shade * gloss, 0.34f * shade * gloss);

                t.addVertexWithUV(cx + Math.cos(a0) * r0 * rx, y0, cz + Math.sin(a0) * r0 * rz,
                    j / (double) lonSteps, i / (double) latSteps);
                t.addVertexWithUV(cx + Math.cos(a1) * r0 * rx, y0, cz + Math.sin(a1) * r0 * rz,
                    (j + 1) / (double) lonSteps, i / (double) latSteps);
                t.addVertexWithUV(cx + Math.cos(a1) * r1 * rx, y1, cz + Math.sin(a1) * r1 * rz,
                    (j + 1) / (double) lonSteps, (i + 1) / (double) latSteps);
                t.addVertexWithUV(cx + Math.cos(a0) * r1 * rx, y1, cz + Math.sin(a0) * r1 * rz,
                    j / (double) lonSteps, (i + 1) / (double) latSteps);
            }
        }
        t.draw();
    }

    private static void drawCylinderY(float cx, float y0, float cz, float rBot, float rTop, float h, int sides,
            float cr, float cg, float cb) {
        Tessellator t = Tessellator.instance;
        float y1 = y0 + h;
        t.startDrawingQuads();
        for (int i = 0; i < sides; i++) {
            double a0 = Math.PI * 2.0 * i / sides;
            double a1 = Math.PI * 2.0 * (i + 1) / sides;
            double am = 0.5 * (a0 + a1);
            float f = 0.52f + 0.48f * Math.max(0f, (float) Math.cos(am) * LX - (float) Math.sin(am) * LZ);
            t.setColorOpaque_F(cr * f, cg * f, cb * f);
            t.addVertexWithUV(cx + Math.cos(a0) * rBot, y0, cz + Math.sin(a0) * rBot, i / (double) sides, 1);
            t.addVertexWithUV(cx + Math.cos(a1) * rBot, y0, cz + Math.sin(a1) * rBot, (i + 1) / (double) sides, 1);
            t.addVertexWithUV(cx + Math.cos(a1) * rTop, y1, cz + Math.sin(a1) * rTop, (i + 1) / (double) sides, 0);
            t.addVertexWithUV(cx + Math.cos(a0) * rTop, y1, cz + Math.sin(a0) * rTop, i / (double) sides, 0);
        }
        t.draw();

        // Tampas
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque_F(cr * 0.85f, cg * 0.85f, cb * 0.85f);
        t.addVertexWithUV(cx, y0, cz, 0.5, 0.5);
        for (int i = 0; i <= sides; i++) {
            double a = Math.PI * 2.0 * i / sides;
            t.addVertexWithUV(cx + Math.cos(a) * rBot, y0, cz + Math.sin(a) * rBot,
                0.5 + Math.cos(a) * 0.5, 0.5 + Math.sin(a) * 0.5);
        }
        t.draw();

        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque_F(cr * 0.72f, cg * 0.72f, cb * 0.72f);
        t.addVertexWithUV(cx, y1, cz, 0.5, 0.5);
        for (int i = 0; i <= sides; i++) {
            double a = -Math.PI * 2.0 * i / sides;
            t.addVertexWithUV(cx + Math.cos(a) * rTop, y1, cz + Math.sin(a) * rTop,
                0.5 + Math.cos(a) * 0.5, 0.5 + Math.sin(a) * 0.5);
        }
        t.draw();
    }

    private static void drawBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        float topF = 0.82f;
        float botF = 0.58f;
        float sideF = 0.74f;
        float frontF = 0.90f;

        t.startDrawingQuads();
        t.setColorOpaque_F(r * topF, g * topF, b * topF);
        t.addVertexWithUV(x0, y0, z0, 0, 0); t.addVertexWithUV(x1, y0, z0, 1, 0);
        t.addVertexWithUV(x1, y0, z1, 1, 1); t.addVertexWithUV(x0, y0, z1, 0, 1);

        t.setColorOpaque_F(r * botF, g * botF, b * botF);
        t.addVertexWithUV(x0, y1, z1, 0, 1); t.addVertexWithUV(x1, y1, z1, 1, 1);
        t.addVertexWithUV(x1, y1, z0, 1, 0); t.addVertexWithUV(x0, y1, z0, 0, 0);

        t.setColorOpaque_F(r * frontF, g * frontF, b * frontF);
        t.addVertexWithUV(x0, y0, z0, 0, 0); t.addVertexWithUV(x0, y1, z0, 0, 1);
        t.addVertexWithUV(x1, y1, z0, 1, 1); t.addVertexWithUV(x1, y0, z0, 1, 0);

        t.setColorOpaque_F(r * sideF, g * sideF, b * sideF);
        t.addVertexWithUV(x1, y0, z1, 0, 0); t.addVertexWithUV(x1, y1, z1, 0, 1);
        t.addVertexWithUV(x0, y1, z1, 1, 1); t.addVertexWithUV(x0, y0, z1, 1, 0);

        t.addVertexWithUV(x1, y0, z0, 0, 0); t.addVertexWithUV(x1, y1, z0, 0, 1);
        t.addVertexWithUV(x1, y1, z1, 1, 1); t.addVertexWithUV(x1, y0, z1, 1, 0);

        t.addVertexWithUV(x0, y0, z1, 0, 0); t.addVertexWithUV(x0, y1, z1, 0, 1);
        t.addVertexWithUV(x0, y1, z0, 1, 1); t.addVertexWithUV(x0, y0, z0, 1, 0);
        t.draw();
    }

    private static void drawGlowBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorRGBA_F(r, g, b, 1f);
        t.addVertex(x0, y0, z0); t.addVertex(x1, y0, z0); t.addVertex(x1, y0, z1); t.addVertex(x0, y0, z1);
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y0, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y0, z1);
        t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1); t.addVertex(x1, y0, z1);
        t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0); t.addVertex(x0, y0, z0);
        t.draw();
    }

    private static void drawFlameCone(float cx, float y0, float cz, float rad, float len) {
        Tessellator t = Tessellator.instance;
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorRGBA_F(0.65f, 0.94f, 1.0f, 0.85f);
        t.addVertex(cx, y0 + len, cz);
        t.setColorRGBA_F(0.20f, 0.72f, 1.0f, 0.15f);
        for (int i = 0; i <= 10; i++) {
            double a = Math.PI * 2.0 * i / 10;
            t.addVertex(cx + Math.cos(a) * rad, y0, cz + Math.sin(a) * rad);
        }
        t.draw();
    }

    private static void bind(ResourceLocation loc) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
    }
}