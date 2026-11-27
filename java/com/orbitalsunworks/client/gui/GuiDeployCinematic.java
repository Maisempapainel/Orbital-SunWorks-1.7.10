package com.orbitalsunworks.client.gui;

import java.util.Random;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.orbitalsunworks.cinematic.CinematicTimeline;
import com.orbitalsunworks.client.handler.OrbitDeployToast;
import com.orbitalsunworks.client.render.ModelRocket;
import com.orbitalsunworks.client.render.ModelSatellite;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiDeployCinematic extends GuiScreen {

    private static final ResourceLocation TEX_EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");
    private static final ResourceLocation TEX_FLAME = new ResourceLocation(
        "orbitalsunworks", "textures/models/rocket_flame.png");

    private final UUID satId;
    private final String satName;
    private final int satTier;
    private final float satHealth;
    private final boolean debrisShield;

    private int tick = 0;
    private boolean finished = false;
    private boolean toastShown = false;

    private boolean sfxSeco = false;
    private boolean sfxFairing = false;
    private boolean sfxSeparate = false;
    private boolean sfxAntenna = false;
    private boolean sfxLeftWing = false;
    private boolean sfxRightWing = false;
    private boolean sfxOperational = false;

    private final ModelRocket rocket = new ModelRocket();
    private final ModelSatellite satellite = new ModelSatellite();

    private static final int STARS = 360;
    private static final float[][] STAR_POS = new float[STARS][3];

    static {
        Random r = new Random(20260427L);
        for (int i = 0; i < STARS; i++) {
            double theta = r.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * r.nextDouble() - 1.0);
            STAR_POS[i][0] = (float) (Math.sin(phi) * Math.cos(theta));
            STAR_POS[i][1] = (float) Math.cos(phi);
            STAR_POS[i][2] = (float) (Math.sin(phi) * Math.sin(theta));
        }
    }

    public static class TickOpener {
        private static UUID pendingId;
        private static String pendingName;
        private static int pendingTier;
        private static float pendingHealth;
        private static boolean pendingShield;
        private static boolean hasPending = false;

        public static synchronized void scheduleOpen(UUID id, String name, int tier, float health, boolean shield) {
            pendingId = id;
            pendingName = name;
            pendingTier = tier;
            pendingHealth = health;
            pendingShield = shield;
            hasPending = true;
        }

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            UUID id;
            String name;
            int tier;
            float health;
            boolean shield;
            synchronized (TickOpener.class) {
                if (!hasPending) {
                    return;
                }
                Minecraft mc = Minecraft.getMinecraft();
                if (mc == null || mc.theWorld == null || mc.thePlayer == null) {
                    return;
                }
                id = pendingId;
                name = pendingName;
                tier = pendingTier;
                health = pendingHealth;
                shield = pendingShield;
                hasPending = false;
            }
            GuiDeployCinematic.open(id, name, tier, health, shield);
        }
    }

    public GuiDeployCinematic(UUID id, String name, int tier, float health, boolean shield) {
        this.satId = id;
        this.satName = name;
        this.satTier = tier;
        this.satHealth = health;
        this.debrisShield = shield;
    }

    public static void open(UUID id, String name, int tier, float health, boolean shield) {
        Minecraft.getMinecraft().displayGuiScreen(
            new GuiDeployCinematic(id, name, tier, health, shield));
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.playSfx("mob.enderdragon.growl", 0.55f);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.finished) {
            return;
        }
        this.tick++;
        this.updateSounds(this.tick);
        if (this.tick >= CinematicTimeline.DURATION) {
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    private void updateSounds(int t) {
        if (t < 68 && t % 7 == 0) {
            this.playSfx("mob.enderdragon.growl", 0.48f);
            this.playSfx("random.fizz", 0.60f);
        }
        if (!this.sfxSeco && t >= 72) {
            this.sfxSeco = true;
            this.playSfx("tile.piston.in", 0.75f);
            this.playSfx("random.fizz", 0.90f);
        }
        if (!this.sfxFairing && t >= CinematicTimeline.F1_FAIRING) {
            this.sfxFairing = true;
            this.playSfx("fireworks.launch", 0.95f);
            this.playSfx("tile.piston.out", 0.85f);
        }
        if (!this.sfxSeparate && t >= CinematicTimeline.F2_SEPARATION) {
            this.sfxSeparate = true;
            this.playSfx("tile.piston.out", 0.95f);
            this.playSfx("note.pling", 1.15f);
        }
        if (t >= CinematicTimeline.F2_SEPARATION + 10 && t <= CinematicTimeline.F2_SEPARATION + 34 && t % 12 == 0) {
            this.playSfx("random.fizz", 0.70f);
        }
        if (t >= CinematicTimeline.F3_DRIFT + 10 && t <= CinematicTimeline.F3_DRIFT + 45 && t % 16 == 0) {
            this.playSfx("random.fizz", 1.25f);
        }
        if (!this.sfxAntenna && t >= CinematicTimeline.F4_UNFOLD + 2) {
            this.sfxAntenna = true;
            this.playSfx("tile.piston.out", 1.10f);
            this.playSfx("random.click", 1.20f);
        }
        if (!this.sfxLeftWing && t >= CinematicTimeline.F4_UNFOLD + 12) {
            this.sfxLeftWing = true;
            this.playSfx("tile.piston.out", 0.90f);
        }
        if (!this.sfxRightWing && t >= CinematicTimeline.F4_UNFOLD + 24) {
            this.sfxRightWing = true;
            this.playSfx("tile.piston.out", 0.95f);
        }
        if (t >= CinematicTimeline.F4_UNFOLD + 28 && t <= CinematicTimeline.F4_UNFOLD + 78 && t % 10 == 0) {
            this.playSfx("random.click", 1.15f);
        }
        if (!this.sfxOperational && t >= CinematicTimeline.F5_INSPECT + 8) {
            this.sfxOperational = true;
            this.playSfx("random.levelup", 0.90f);
        }
    }

    private void playSfx(String name, float pitch) {
        if (this.mc != null && this.mc.getSoundHandler() != null) {
            this.mc.getSoundHandler().playSound(
                PositionedSoundRecord.func_147674_a(new ResourceLocation(name), pitch));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float p = this.tick + partialTicks;
        int w = this.width;
        int h = this.height;

        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float aspect = Math.max(0.1f, (float) w / (float) Math.max(1, h));
        GLU.gluPerspective(56f, aspect, 0.1f, 500f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_LIGHTING);

        this.drawSpace(p);

        float focusSat = CinematicTimeline.cameraFocusSatellite(p);
        float satDist = CinematicTimeline.satelliteDistance(p);
        float stageRecede = CinematicTimeline.stageRecede(p);
        float mainBurn = CinematicTimeline.mainBurn(p);
        float nozzleHeat = CinematicTimeline.nozzleHeat(p);

        float rocketScale = 0.85f;
        float rocketPitch = 82f;
        float axisZ = (float) Math.sin(Math.toRadians(rocketPitch));
        float axisY = (float) Math.cos(Math.toRadians(rocketPitch));

        // Base do adaptador onde o satélite fica alojado dentro da coifa.
        float adapterOffset = ModelRocket.PAYLOAD_ADAPTER_Y * rocketScale;
        float satStartZ = -1.6f + adapterOffset * axisZ;
        float satStartY = adapterOffset * axisY;

        float satZ = satStartZ + satDist * 1.45f;
        float satY = satStartY + satDist * 0.12f;

        float lookZ = (1f - focusSat) * (satStartZ * 0.6f) + focusSat * satZ;
        float lookY = (1f - focusSat) * (satStartY * 0.5f) + focusSat * (satY + 0.35f);

        float camDist = CinematicTimeline.cameraDistance(p);
        float camAng = CinematicTimeline.cameraOrbit(p);
        float camH = CinematicTimeline.cameraHeight(p);
        float camX = (float) Math.cos(camAng) * camDist;
        float camZ = (float) Math.sin(camAng) * camDist + lookZ;
        GLU.gluLookAt(camX, lookY + camH, camZ, 0f, lookY, lookZ, 0f, 1f, 0f);

        // Vibração sutil durante a queima hipersônica inicial + linhas de velocidade orbital.
        if (mainBurn > 0.01f) {
            this.drawSpeedStreaks(p, mainBurn);
        }

        // ---- 1. Foguete Tier 1 (sem coifa, corpo principal + chama) ----
        float stageFade = 1f - CinematicTimeline.easeOut((p - (CinematicTimeline.F4_UNFOLD + 10f)) / 70f);
        if (stageFade > 0.01f) {
            float vib = mainBurn * 0.025f;
            float vx = (float) Math.sin(p * 2.3f) * vib;
            float vy = (float) Math.cos(p * 2.9f) * vib;

            GL11.glPushMatrix();
            GL11.glTranslatef(vx, vy - stageRecede * 0.55f, -1.6f - stageRecede * 2.4f);
            GL11.glRotatef(rocketPitch + stageRecede * 7.5f, 1f, 0f, 0f);
            GL11.glRotatef(stageRecede * 5.0f, 0f, 0f, 1f);
            GL11.glScalef(rocketScale, rocketScale, rocketScale);

            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1f, 1f, 1f, stageFade);
            this.rocket.renderBodyWithoutFairing(nozzleHeat);

            // Coifa fechada antes da F1; metades separando a partir da F1.
            float fairing = CinematicTimeline.fairingRelease(p);
            float fairingDrift = CinematicTimeline.fairingDrift(p);
            float fairingSpin = CinematicTimeline.fairingSpin(p);
            if (fairing < 0.99f) {
                for (int side = -1; side <= 1; side += 2) {
                    GL11.glPushMatrix();
                    if (fairing > 0.001f) {
                        GL11.glTranslatef(
                            side * (fairing * 0.55f + fairingDrift * 0.25f),
                            -fairingDrift * 0.95f,
                            fairing * 0.12f);
                        GL11.glTranslatef(0f, ModelRocket.PAYLOAD_ADAPTER_Y + 0.6f, 0f);
                        GL11.glRotatef(side * fairingSpin * 0.85f, 0.2f, 0.35f, 0.9f);
                        GL11.glTranslatef(0f, -(ModelRocket.PAYLOAD_ADAPTER_Y + 0.6f), 0f);
                    }
                    this.rocket.renderFairingHalf(side);
                    GL11.glPopMatrix();
                }
            }

            // Chama principal (Fase 0) ou burn de afastamento (Fase 2).
            float retroBurn = CinematicTimeline.stageBurn(p);
            float totalBurn = Math.max(mainBurn, retroBurn * 0.55f);
            if (totalBurn > 0.01f) {
                this.renderRocketPlume(p, totalBurn, mainBurn > 0.05f);
            }

            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopMatrix();
        }

        // ---- 2. Satélite Metálico (desacoplando da coifa -> abertura -> órbita) ----
        if (p >= CinematicTimeline.F1_FAIRING - 2f) {
            float unfold = CinematicTimeline.unfold(p);
            float spin = CinematicTimeline.satelliteSpin(p);
            float satPitch = rocketPitch * (1f - focusSat);
            float satScale = 0.62f + 0.20f * focusSat;

            GL11.glPushMatrix();
            GL11.glTranslatef(0f, satY, satZ);
            GL11.glRotatef(satPitch, 1f, 0f, 0f);
            GL11.glScalef(satScale, satScale, satScale);
            this.satellite.renderAll(unfold, spin, p);

            if (p >= CinematicTimeline.F3_DRIFT && p <= CinematicTimeline.F4_UNFOLD + 15f) {
                this.rcsPuff(p);
            }
            if (p >= CinematicTimeline.F5_INSPECT) {
                this.renderOrbitBeacon(p);
            }
            GL11.glPopMatrix();
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);

        // ---- HUD Cinematográfico ----
        this.drawGradientRect(0, 0, w, h / 8, 0xCC000000, 0x00000000);
        this.drawGradientRect(0, h - h / 8, w, h, 0x00000000, 0xCC000000);

        if (p < 18f) {
            this.fullScreenFade(1f - CinematicTimeline.clamp01(p / 18f));
        }
        if (p > CinematicTimeline.DURATION - 25f) {
            this.fullScreenFade(CinematicTimeline.clamp01((p - (CinematicTimeline.DURATION - 25f)) / 25f));
        }

        this.missionCaption(p, w, h);

        String skip = "[ESC] Pular cinem\u00e1tica";
        int sw = this.fontRendererObj.getStringWidth(skip);
        drawRect(w - sw - 14, 6, w - 6, 20, 0x900B111A);
        this.fontRendererObj.drawStringWithShadow(skip, w - sw - 10, 9, 0xB8D8EE);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSpace(float p) {
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        float s = 260f;
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor3f(0.025f, 0.030f, 0.042f);
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

        GL11.glPointSize(1.8f);
        GL11.glEnable(GL11.GL_POINT_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        t.startDrawing(GL11.GL_POINTS);
        for (int i = 0; i < STARS; i++) {
            float tw = 0.50f + 0.50f * (0.5f + 0.5f * (float) Math.sin(p * 0.04f + i * 1.7f));
            t.setColorRGBA_F(tw, tw, Math.min(1f, tw * 1.06f), 1f);
            t.addVertex(STAR_POS[i][0] * 235f, STAR_POS[i][1] * 235f, STAR_POS[i][2] * 235f);
        }
        t.draw();
        GL11.glDisable(GL11.GL_POINT_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);

        // Terra limpa e perfeitamente redonda (72 segmentos, sem triângulos azuis na borda).
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        this.mc.getTextureManager().bindTexture(TEX_EARTH);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        float er = 148f;
        float ey = -118f;
        int segs = 72;
        t.startDrawing(GL11.GL_TRIANGLE_FAN);
        t.setColorOpaque_F(1f, 1f, 1f);
        t.addVertexWithUV(0f, ey, 0f, 0.5f, 0.5f);
        for (int i = 0; i <= segs; i++) {
            double a = Math.PI * 2.0 * i / segs + p * 0.0010f;
            t.addVertexWithUV((float) Math.cos(a) * er, ey, (float) Math.sin(a) * er,
                0.5f + (float) Math.cos(a) * 0.5f, 0.5f + (float) Math.sin(a) * 0.5f);
        }
        t.draw();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawSpeedStreaks(float p, float burn) {
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glLineWidth(1.4f);
        t.startDrawing(GL11.GL_LINES);
        Random r = new Random(9911L);
        for (int i = 0; i < 42; i++) {
            float rx = (r.nextFloat() - 0.5f) * 16f;
            float ry = (r.nextFloat() - 0.5f) * 12f;
            if (Math.abs(rx) < 1.6f && Math.abs(ry) < 1.6f) {
                rx += 2.2f;
            }
            float phase = (r.nextFloat() * 24f + p * 0.95f) % 24f;
            float zHead = 10f - phase;
            float len = 1.6f + 1.4f * burn;
            t.setColorRGBA_F(0.70f, 0.88f, 1.0f, 0.42f * burn);
            t.addVertex(rx, ry, zHead);
            t.setColorRGBA_F(0.40f, 0.70f, 1.0f, 0.0f);
            t.addVertex(rx, ry, zHead - len);
        }
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderRocketPlume(float p, float strength, boolean supersonic) {
        Tessellator t = Tessellator.instance;
        this.mc.getTextureManager().bindTexture(TEX_FLAME);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_CULL_FACE);

        float flick = 0.88f + 0.12f * (float) Math.sin(p * 1.9f);
        float len = (supersonic ? 3.2f : 1.4f) * strength * flick;
        float rad = supersonic ? 0.36f : 0.22f;

        GL11.glColor4f(1.0f, 0.78f, 0.45f, 0.92f * strength);
        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < 14; i++) {
            double a0 = Math.PI * 2 * i / 14.0;
            double a1 = Math.PI * 2 * (i + 1) / 14.0;
            t.addVertexWithUV(Math.cos(a0) * rad, 0.0, Math.sin(a0) * rad, i / 14.0, 1.0);
            t.addVertexWithUV(Math.cos(a1) * rad, 0.0, Math.sin(a1) * rad, (i + 1) / 14.0, 1.0);
            t.addVertexWithUV(0.0, -len, 0.0, (i + 0.5) / 14.0, 0.0);
        }
        t.draw();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.82f, 0.94f, 1.0f, 0.95f * strength);
        float coreRad = rad * 0.55f;
        float coreLen = len * 0.62f;
        t.startDrawing(GL11.GL_TRIANGLES);
        for (int i = 0; i < 12; i++) {
            double a0 = Math.PI * 2 * i / 12.0;
            double a1 = Math.PI * 2 * (i + 1) / 12.0;
            t.addVertex(Math.cos(a0) * coreRad, -0.02, Math.sin(a0) * coreRad);
            t.addVertex(Math.cos(a1) * coreRad, -0.02, Math.sin(a1) * coreRad);
            t.addVertex(0.0, -coreLen, 0.0);
        }
        t.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void rcsPuff(float p) {
        float pulse = (p % 26f) < 10f ? 1f : 0f;
        if (pulse <= 0f) {
            return;
        }
        float fade = pulse * (1f - (p % 10f) / 10f);
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        for (int i = -1; i <= 1; i += 2) {
            t.startDrawingQuads();
            t.setColorRGBA_F(0.88f, 0.95f, 1.0f, 0.55f * fade);
            t.addVertex(i * 0.48, 0.85, -0.06);
            t.addVertex(i * 0.48, 0.97, 0.06);
            t.addVertex(i * (0.95 + 0.35 * fade), 0.97, 0.06);
            t.addVertex(i * (0.95 + 0.35 * fade), 0.85, -0.06);
            t.draw();
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void renderOrbitBeacon(float p) {
        float blink = (p % 30f) < 7f ? 1.0f : 0.2f;
        Tessellator t = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        t.startDrawingQuads();
        t.setColorRGBA_F(0.25f, 0.95f, 1.0f, blink);
        t.addVertex(-0.04, 1.66, -0.04);
        t.addVertex(-0.04, 1.66, 0.04);
        t.addVertex(0.04, 1.66, 0.04);
        t.addVertex(0.04, 1.66, -0.04);
        t.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    private void missionCaption(float p, int w, int h) {
        String caption = null;
        int color = 0xC4DAE8;
        if (p > 8f && p < 70f) {
            caption = "Propuls\u00e3o orbital em velocidade hipers\u00f4nica";
            color = 0x55E0FF;
        } else if (p >= 70f && p < CinematicTimeline.F1_FAIRING) {
            caption = "SECO \u2014 Corte dos motores principais";
            color = 0xFFD970;
        } else if (p >= CinematicTimeline.F1_FAIRING && p < CinematicTimeline.F2_SEPARATION) {
            caption = "Alijamento da coifa protetora";
        } else if (p >= CinematicTimeline.F2_SEPARATION && p < CinematicTimeline.F3_DRIFT) {
            caption = "Desacoplamento do sat\u00e9lite e descarte do est\u00e1gio";
        } else if (p >= CinematicTimeline.F3_DRIFT && p < CinematicTimeline.F4_UNFOLD) {
            caption = "Estabiliza\u00e7\u00e3o de atitude por jatos RCS";
        } else if (p >= CinematicTimeline.F4_UNFOLD && p < CinematicTimeline.F5_INSPECT) {
            caption = "Implantando antenas e pain\u00e9is solares";
            color = 0xFFD970;
        } else if (p >= CinematicTimeline.F5_INSPECT) {
            caption = this.satName + " \u2014 Tier " + this.satTier + " em \u00f3rbita operacional";
            color = 0x7CFC00;
        }

        if (caption != null) {
            this.drawCenteredString(this.fontRendererObj, caption, w / 2, h - 54, color);
        }
        if (p >= CinematicTimeline.F5_INSPECT + 12f) {
            String gen = OSUtils.formatRF(this.currentGenerationRFt()) + "/t de gera\u00e7\u00e3o inicial";
            this.drawCenteredString(this.fontRendererObj, gen, w / 2, h - 40, 0x7CFC00);
        }
    }

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

    private long currentGenerationRFt() {
        long base = 1000000L;
        float factor = this.satHealth >= 0.2f
            ? 0.5f + (this.satHealth - 0.2f) * (0.5f / 0.8f)
            : 0.5f;
        return (long) (base * factor);
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if (keyCode == 1 || (this.tick > 15 && (keyCode == 28 || keyCode == 57))) {
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.tick > 45) {
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        GL11.glColor4f(1, 1, 1, 1);
        if (!this.toastShown) {
            this.toastShown = true;
            OrbitDeployToast.show(this.satName, this.satTier, this.satHealth);
        }
    }
}