package com.orbitalsunworks.client.gui;

import java.util.Random;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
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
    private static final ResourceLocation TEX_CLOUDS = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth_clouds.png");
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

    private boolean paused = false;
    private boolean cameraLocked = false;
    private float manualYaw = 0f;
    private float manualPitch = 0f;
    private float manualZoom = 1.0f;
    private int lastDragX = -1;
    private int lastDragY = -1;
    private boolean dragging = false;

    private GuiButton btnLockCam;
    private GuiButton btnPause;
    private GuiButton btnSkip;

    private boolean sfxSeco = false;
    private boolean sfxFairing = false;
    private boolean sfxSeparate = false;
    private boolean sfxAntenna = false;
    private boolean sfxLeftWing = false;
    private boolean sfxRightWing = false;
    private boolean sfxOperational = false;

    private final ModelRocket rocket = new ModelRocket();
    private final ModelSatellite satellite = new ModelSatellite();

    private static final int STARS = 420;
    private static final float[][] STAR_POS = new float[STARS][4];

    static {
        Random r = new Random(20260427L);
        for (int i = 0; i < STARS; i++) {
            double theta = r.nextDouble() * Math.PI * 2.0;
            double phi = Math.acos(2.0 * r.nextDouble() - 1.0);
            STAR_POS[i][0] = (float) (Math.sin(phi) * Math.cos(theta));
            STAR_POS[i][1] = (float) Math.cos(phi);
            STAR_POS[i][2] = (float) (Math.sin(phi) * Math.sin(theta));
            STAR_POS[i][3] = 0.45f + r.nextFloat() * 0.55f;
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
        this.satName = name != null ? name : "OS-001";
        this.satTier = Math.max(1, tier);
        this.satHealth = health;
        this.debrisShield = shield;
    }

    public static void open(UUID id, String name, int tier, float health, boolean shield) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            mc.displayGuiScreen(new GuiDeployCinematic(id, name, tier, health, shield));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        this.buttonList.clear();
        this.btnLockCam = new GuiButton(0, this.width - 252, 8, 96, 18,
            this.cameraLocked ? "C\u00e2mera: Travada" : "C\u00e2mera: Auto");
        this.btnPause = new GuiButton(1, this.width - 152, 8, 66, 18,
            this.paused ? "Retomar" : "Pausar");
        this.btnSkip = new GuiButton(2, this.width - 82, 8, 74, 18, "Pular (ESC)");
        this.buttonList.add(this.btnLockCam);
        this.buttonList.add(this.btnPause);
        this.buttonList.add(this.btnSkip);
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            this.cameraLocked = !this.cameraLocked;
            this.btnLockCam.displayString = this.cameraLocked ? "C\u00e2mera: Travada" : "C\u00e2mera: Auto";
        } else if (btn.id == 1) {
            this.paused = !this.paused;
            this.btnPause.displayString = this.paused ? "Retomar" : "Pausar";
        } else if (btn.id == 2) {
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (this.finished || this.paused) {
            return;
        }
        this.tick++;
        triggerPhaseSounds(this.tick);
        if (this.tick >= CinematicTimeline.DURATION + 25) {
            this.finished = true;
            this.mc.displayGuiScreen(null);
        }
    }

    private void triggerPhaseSounds(int t) {
        if (t >= 70 && !this.sfxSeco) {
            this.sfxSeco = true;
            playUiSound("random.fizz", 0.55f, 0.7f);
        }
        if (t >= CinematicTimeline.F1_FAIRING && !this.sfxFairing) {
            this.sfxFairing = true;
            playUiSound("tile.piston.out", 0.75f, 0.85f);
        }
        if (t >= CinematicTimeline.F2_SEPARATION && !this.sfxSeparate) {
            this.sfxSeparate = true;
            playUiSound("tile.piston.out", 0.85f, 1.15f);
        }
        if (t >= CinematicTimeline.F4_UNFOLD && !this.sfxAntenna) {
            this.sfxAntenna = true;
            playUiSound("note.pling", 0.5f, 1.35f);
        }
        if (t >= CinematicTimeline.F4_UNFOLD + 15 && !this.sfxLeftWing) {
            this.sfxLeftWing = true;
            playUiSound("tile.piston.out", 0.6f, 1.25f);
        }
        if (t >= CinematicTimeline.F4_UNFOLD + 25 && !this.sfxRightWing) {
            this.sfxRightWing = true;
            playUiSound("tile.piston.out", 0.6f, 1.30f);
        }
        if (t >= CinematicTimeline.F5_INSPECT && !this.sfxOperational) {
            this.sfxOperational = true;
            playUiSound("random.levelup", 0.75f, 1.2f);
        }
    }

    private void playUiSound(String sound, float vol, float pitch) {
        if (this.mc != null && this.mc.getSoundHandler() != null) {
            this.mc.getSoundHandler().playSound(
                PositionedSoundRecord.func_147674_a(new ResourceLocation(sound), pitch));
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            this.manualZoom -= (wheel / 120.0f) * 0.12f;
            if (this.manualZoom < 0.45f) this.manualZoom = 0.45f;
            if (this.manualZoom > 2.40f) this.manualZoom = 2.40f;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);
        if (button == 0 && mouseY > 30) {
            this.dragging = true;
            this.lastDragX = mouseX;
            this.lastDragY = mouseY;
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int state) {
        super.mouseMovedOrUp(mouseX, mouseY, state);
        if (state == 0) {
            this.dragging = false;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (clickedMouseButton == 0 && this.dragging) {
            int dx = mouseX - this.lastDragX;
            int dy = mouseY - this.lastDragY;
            this.lastDragX = mouseX;
            this.lastDragY = mouseY;
            this.manualYaw += dx * 0.55f;
            this.manualPitch += dy * 0.45f;
            if (this.manualPitch < -82f) this.manualPitch = -82f;
            if (this.manualPitch > 82f) this.manualPitch = 82f;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        float p = this.paused ? (float) this.tick : Math.min(CinematicTimeline.DURATION + 25f, this.tick + partialTicks);

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);

        // 1) Cobertura preta 100% opaca em toda a tela ANTES da cena 3D
        // (impede qualquer vazamento do mundo, nuvens ou mão do personagem).
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        drawSolidQuad2D(0f, 0f, this.width, this.height);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        // 2) Cena espacial 3D
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float aspect = (float) this.width / (float) Math.max(1, this.height);
        GLU.gluPerspective(52.0f, aspect, 0.1f, 800.0f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        float focusSat = CinematicTimeline.cameraFocusSatellite(p);
        float satDist = CinematicTimeline.satelliteDistance(p);
        float stageBack = CinematicTimeline.stageRecede(p);
        float targetY = 4.65f + satDist * focusSat;

        float autoOrbit = this.cameraLocked ? 0f : CinematicTimeline.cameraOrbit(p);
        float yaw = autoOrbit + (float) Math.toRadians(this.manualYaw);
        float pitch = (float) Math.toRadians(this.manualPitch);
        float camDist = CinematicTimeline.cameraDistance(p) * this.manualZoom;
        float camH = (this.cameraLocked ? 1.2f : CinematicTimeline.cameraHeight(p)) * this.manualZoom;

        float horiz = (float) Math.cos(pitch) * camDist;
        float camX = (float) Math.sin(yaw) * horiz;
        float camZ = (float) Math.cos(yaw) * horiz;
        float camY = targetY + camH + (float) Math.sin(pitch) * camDist;

        GLU.gluLookAt(camX, camY, camZ, 0f, targetY, 0f, 0f, 1f, 0f);

        // Esfera estelar 360° centrada na câmera
        GL11.glPushMatrix();
        GL11.glTranslatef(camX, camY, camZ);
        drawStars(p);
        GL11.glPopMatrix();

        // Terra esférica 3D real embaixo (nunca some ao girar a câmera!)
        drawEarth3D(p);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        // Estágio do foguete
        float stageY = -stageBack;
        float heat = CinematicTimeline.nozzleHeat(p);
        float burn = CinematicTimeline.mainBurn(p);
        float stageThrust = CinematicTimeline.stageBurn(p);

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, stageY, 0f);
        this.rocket.renderBodyWithoutFairing(heat);
        if (burn > 0.01f) {
            renderRocketPlume(p, burn, true);
        } else if (stageThrust > 0.01f) {
            renderRocketPlume(p, stageThrust * 0.65f, false);
        }
        GL11.glPopMatrix();

        // Coifas abrindo sem atravessar o satélite
        float rel = CinematicTimeline.fairingRelease(p);
        float drift = CinematicTimeline.fairingDrift(p);
        float spin = CinematicTimeline.fairingSpin(p);
        for (int side = -1; side <= 1; side += 2) {
            GL11.glPushMatrix();
            GL11.glTranslatef(side * (0.18f * rel + drift), stageY - drift * 0.25f, 0f);
            GL11.glRotatef(-side * (24f * rel + spin), 0f, 0f, 1f);
            GL11.glRotatef(side * spin * 0.3f, 0f, 1f, 0f);
            this.rocket.renderFairingHalf(side);
            GL11.glPopMatrix();
        }

        // Satélite emergindo e desdobrando após sair da coifa
        float satY = ModelRocket.PAYLOAD_ADAPTER_Y + satDist;
        float unfold = CinematicTimeline.unfold(p);
        float satSpin = CinematicTimeline.satelliteSpin(p);

        GL11.glPushMatrix();
        GL11.glTranslatef(0f, satY, 0f);
        GL11.glRotatef(satSpin, 0f, 1f, 0f);
        this.satellite.renderAll(unfold, satSpin, p, this.satHealth);
        if (p >= CinematicTimeline.F3_DRIFT && p < CinematicTimeline.F4_UNFOLD + 15f) {
            rcsPuff(p);
        }
        if (p >= CinematicTimeline.F4_UNFOLD) {
            renderOrbitBeacon(p);
        }
        GL11.glPopMatrix();

        if (burn > 0.15f) {
            drawSpeedStreaks(p, burn);
        }

        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        // Restaura 2D limpo antes de desenhar HUD/Botões (evita retângulos cinzas!)
        GL11.glPopClientAttrib();
        GL11.glPopAttrib();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        missionCaption(p, this.width, this.height);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        this.fontRendererObj.drawStringWithShadow(
            "\u00a77Arraste para girar 360\u00b0 | Scroll: Zoom", 10, 12, 0xA8C0D0);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        // Fade-in inicial limpo
        if (p < 14f) {
            fullScreenFade(1f - p / 14f);
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawSolidQuad2D(float x0, float y0, float x1, float y1) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertex(x0, y1, 0.0);
        t.addVertex(x1, y1, 0.0);
        t.addVertex(x1, y0, 0.0);
        t.addVertex(x0, y0, 0.0);
        t.draw();
    }

    private void drawStars(float p) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glPointSize(1.8f);
        GL11.glBegin(GL11.GL_POINTS);
        for (int i = 0; i < STARS; i++) {
            float tw = STAR_POS[i][3] * (0.75f + 0.25f * (float) Math.sin(p * 0.08 + i));
            GL11.glColor4f(tw, tw * 0.98f, Math.min(1f, tw * 1.06f), 1f);
            GL11.glVertex3f(STAR_POS[i][0] * 320f, STAR_POS[i][1] * 320f, STAR_POS[i][2] * 320f);
        }
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawEarth3D(float p) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0f, -152f, 0f);
        GL11.glRotatef(20f, 0f, 0f, 1f);
        GL11.glRotatef(p * 0.14f, 0f, 1f, 0f);

        this.mc.getTextureManager().bindTexture(TEX_EARTH);
        drawTexturedSphere(118f, 36, 56, 1.0f);

        GL11.glRotatef(p * 0.06f + 35f, 0f, 1f, 0f);
        this.mc.getTextureManager().bindTexture(TEX_CLOUDS);
        drawTexturedSphere(119.0f, 32, 48, 0.85f);

        GL11.glPopMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawTexturedSphere(float r, int stacks, int slices, float alpha) {
        Tessellator t = Tessellator.instance;
        for (int i = 0; i < stacks; i++) {
            double phi0 = Math.PI * i / stacks;
            double phi1 = Math.PI * (i + 1) / stacks;
            float v0 = (float) i / stacks;
            float v1 = (float) (i + 1) / stacks;
            t.startDrawing(GL11.GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                double th = 2.0 * Math.PI * j / slices;
                float u = (float) j / slices;

                float x0 = (float) (Math.sin(phi0) * Math.cos(th));
                float y0 = (float) Math.cos(phi0);
                float z0 = (float) (Math.sin(phi0) * Math.sin(th));
                float l0 = Math.max(0.18f, 0.45f + 0.55f * (y0 * 0.65f + x0 * 0.55f));
                t.setColorRGBA_F(l0, l0, Math.min(1f, l0 * 1.04f), alpha);
                t.addVertexWithUV(x0 * r, y0 * r, z0 * r, u, v0);

                float x1 = (float) (Math.sin(phi1) * Math.cos(th));
                float y1 = (float) Math.cos(phi1);
                float z1 = (float) (Math.sin(phi1) * Math.sin(th));
                float l1 = Math.max(0.18f, 0.45f + 0.55f * (y1 * 0.65f + x1 * 0.55f));
                t.setColorRGBA_F(l1, l1, Math.min(1f, l1 * 1.04f), alpha);
                t.addVertexWithUV(x1 * r, y1 * r, z1 * r, u, v1);
            }
            t.draw();
        }
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

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
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
        float blink = (p % 30f) < 7f ? 1.0f : 0.25f;
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
        int color = 0xD2E8F8;
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
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            drawRect(w / 2 - 190, h - 52, w / 2 + 190, h - 18, 0x88060D16);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPushMatrix();
            GL11.glScalef(0.85f, 0.85f, 1f);
            float inv = 1f / 0.85f;
            this.drawCenteredString(this.fontRendererObj, caption, Math.round((w / 2f) * inv), Math.round((h - 46f) * inv), color);
            if (p >= CinematicTimeline.F5_INSPECT + 10f) {
                String gen = OSUtils.formatRF(this.currentGenerationRFt()) + "/t de gera\u00e7\u00e3o inicial";
                this.drawCenteredString(this.fontRendererObj, gen, Math.round((w / 2f) * inv), Math.round((h - 32f) * inv), 0x7CFC00);
            }
            GL11.glPopMatrix();
        }
    }

    private void fullScreenFade(float alpha) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0f, 0f, 0f, alpha);
        drawSolidQuad2D(0f, 0f, this.width, this.height);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private long currentGenerationRFt() {
        long base = 1000000L * this.satTier;
        float factor = this.satHealth >= 0.4f
            ? 0.5f + ((this.satHealth - 0.4f) / 0.6f) * 0.5f
            : (this.satHealth / 0.4f) * 0.5f;
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