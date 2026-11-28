package com.orbitalsunworks.client.gui;

import java.util.List;
import java.util.Random;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.client.handler.ClientMeteorHandler;
import com.orbitalsunworks.client.render.ModelSatellite;
import com.orbitalsunworks.network.MessageControlDataRequest;
import com.orbitalsunworks.network.MessageControlDataResponse;
import com.orbitalsunworks.network.MessageControlDataResponse.SatInfo;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Visão orbital em tempo real pelo Telescópio:
 *  - Meteoro 3D craterado com magma incandescente e rastro de fogo laranja/amarelo/branco
 *  - Colisão com explosão de chamas, destroços e tranco realista
 *  - Botão para travar/destravar a rotação da câmera e arraste 360° com o mouse
 */
@SideOnly(Side.CLIENT)
public class GuiTelescopeView extends GuiScreen {

    private static final ResourceLocation TEX_EARTH = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth.png");
    private static final ResourceLocation TEX_CLOUDS = new ResourceLocation(
        "orbitalsunworks", "textures/environment/earth_clouds.png");

    private List<SatInfo> satellites;
    private int satIndex;
    private final int blockX;
    private final int blockY;
    private final int blockZ;

    private int ticksOpen = 0;
    private float camYaw = 28f;
    private float camPitch = 16f;
    private float camDist = 6.0f;
    private boolean lockRotation = false;
    private int dragLastX = -1;
    private int dragLastY = -1;
    private boolean dragging = false;

    private int impactTicks = -1;
    private long lastHandledImpactMs = 0L;
    private int lastSeenRemaining = -1;

    private GuiButton btnLockCam;

    private final ModelSatellite satelliteModel = new ModelSatellite();

    private static final int STAR_COUNT = 340;
    private static final float[][] STARS = new float[STAR_COUNT][4];
    private static final float[][] IMPACT_SHARDS = new float[40][7];

    static {
        Random r = new Random(774123L);
        for (int i = 0; i < STAR_COUNT; i++) {
            double th = r.nextDouble() * Math.PI * 2.0;
            double ph = Math.acos(2.0 * r.nextDouble() - 1.0);
            STARS[i][0] = (float) (Math.sin(ph) * Math.cos(th));
            STARS[i][1] = (float) Math.cos(ph);
            STARS[i][2] = (float) (Math.sin(ph) * Math.sin(th));
            STARS[i][3] = 0.45f + r.nextFloat() * 0.55f;
        }
        for (int i = 0; i < IMPACT_SHARDS.length; i++) {
            float th = (float) (r.nextDouble() * Math.PI * 2.0);
            float ph = (float) ((r.nextDouble() - 0.35) * Math.PI * 0.85);
            float spd = 0.045f + r.nextFloat() * 0.14f;
            IMPACT_SHARDS[i][0] = (float) (Math.cos(ph) * Math.cos(th)) * spd + 0.04f;
            IMPACT_SHARDS[i][1] = (float) Math.sin(ph) * spd;
            IMPACT_SHARDS[i][2] = (float) (Math.cos(ph) * Math.sin(th)) * spd - 0.02f;
            IMPACT_SHARDS[i][3] = (r.nextFloat() - 0.5f) * 20f;
            IMPACT_SHARDS[i][4] = 0.04f + r.nextFloat() * 0.10f;
            IMPACT_SHARDS[i][5] = (i < 22) ? 0f : (i < 33 ? 1f : 2f);
            IMPACT_SHARDS[i][6] = r.nextFloat() * 1.5f;
        }
    }

    public GuiTelescopeView(int x, int y, int z) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
        this.satellites = MessageControlDataResponse.clientSatellites;
        this.satIndex = 0;
    }

    public GuiTelescopeView(List<SatInfo> satellites, int selectedIndex) {
        this.blockX = 0;
        this.blockY = 0;
        this.blockZ = 0;
        this.satellites = satellites != null ? satellites : MessageControlDataResponse.clientSatellites;
        this.satIndex = Math.max(0, selectedIndex);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initGui() {
        this.buttonList.clear();
        this.btnLockCam = new GuiButton(2, this.width - 196, this.height - 26, 114, 18,
            this.lockRotation ? "Rota\u00e7\u00e3o: Travada" : "Rota\u00e7\u00e3o: Livre");
        this.buttonList.add(new GuiButton(0, this.width - 78, this.height - 26, 68, 18, "Sair (ESC)"));
        this.buttonList.add(new GuiButton(1, 10, this.height - 26, 108, 18, "Pr\u00f3ximo Sat\u00e9lite"));
        this.buttonList.add(this.btnLockCam);
        this.lastHandledImpactMs = ClientMeteorHandler.lastImpactTimestampMs;
        OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.blockX, this.blockY, this.blockZ));
    }

    @Override
    protected void actionPerformed(GuiButton btn) {
        if (btn.id == 0) {
            this.mc.displayGuiScreen(null);
        } else if (btn.id == 1 && this.satellites != null && !this.satellites.isEmpty()) {
            this.satIndex = (this.satIndex + 1) % this.satellites.size();
        } else if (btn.id == 2) {
            this.lockRotation = !this.lockRotation;
            this.btnLockCam.displayString = this.lockRotation ? "Rota\u00e7\u00e3o: Travada" : "Rota\u00e7\u00e3o: Livre";
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.ticksOpen++;
        this.satellites = MessageControlDataResponse.clientSatellites;
        if (this.ticksOpen % 20 == 0) {
            OrbitalSunworks.network.sendToServer(new MessageControlDataRequest(this.blockX, this.blockY, this.blockZ));
        }

        if (ClientMeteorHandler.lastImpactTimestampMs != this.lastHandledImpactMs
                && ClientMeteorHandler.lastImpactTimestampMs > 0L) {
            this.lastHandledImpactMs = ClientMeteorHandler.lastImpactTimestampMs;
            this.impactTicks = 0;
        } else if (ClientMeteorHandler.active && ClientMeteorHandler.remainingTicks <= 2
                && this.lastSeenRemaining > 2 && this.impactTicks < 0) {
            this.impactTicks = 0;
        }
        this.lastSeenRemaining = ClientMeteorHandler.active ? ClientMeteorHandler.remainingTicks : -1;

        if (this.impactTicks >= 0) {
            this.impactTicks++;
            if (this.impactTicks > 110) {
                this.impactTicks = -1;
            }
        }
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int wheel = Mouse.getDWheel();
        if (wheel != 0) {
            this.camDist -= (wheel / 120.0f) * 0.45f;
            if (this.camDist < 3.0f) this.camDist = 3.0f;
            if (this.camDist > 12.0f) this.camDist = 12.0f;
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int btn) {
        super.mouseClicked(x, y, btn);
        if (btn == 0 && y < this.height - 32) {
            this.dragging = true;
            this.dragLastX = x;
            this.dragLastY = y;
        }
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int state) {
        super.mouseMovedOrUp(x, y, state);
        if (state == 0) {
            this.dragging = false;
        }
    }

    @Override
    protected void mouseClickMove(int x, int y, int btn, long dt) {
        super.mouseClickMove(x, y, btn, dt);
        if (btn == 0 && this.dragging) {
            this.camYaw += (x - this.dragLastX) * 0.55f;
            this.camPitch += (y - this.dragLastY) * 0.45f;
            if (this.camPitch < -78f) this.camPitch = -78f;
            if (this.camPitch > 78f) this.camPitch = 78f;
            this.dragLastX = x;
            this.dragLastY = y;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(0.01f, 0.02f, 0.04f, 1f);
        drawSolidQuad2D(0f, 0f, this.width, this.height);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        float aspect = (float) this.width / (float) Math.max(1, this.height);
        GLU.gluPerspective(54.0f, aspect, 0.1f, 800.0f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        float time = this.ticksOpen + partialTicks;
        float shakeX = 0f, shakeY = 0f;
        if (this.impactTicks >= 0 && this.impactTicks < 32) {
            float decay = 1.0f - (this.impactTicks / 32.0f);
            float amp = decay * 0.36f;
            shakeX = (float) Math.sin(time * 2.9f) * amp;
            shakeY = (float) Math.cos(time * 3.7f) * amp;
        }

        float autoYaw = this.lockRotation ? 0f : (time * 0.24f);
        float yawRad = (float) Math.toRadians(this.camYaw + autoYaw);
        float pitchRad = (float) Math.toRadians(this.camPitch);
        float cx = (float) (Math.cos(pitchRad) * Math.sin(yawRad) * this.camDist);
        float cy = (float) (Math.sin(pitchRad) * this.camDist) + 0.6f;
        float cz = (float) (Math.cos(pitchRad) * Math.cos(yawRad) * this.camDist);

        GLU.gluLookAt(cx + shakeX, cy + shakeY, cz, shakeX * 0.3f, 0.35f + shakeY * 0.3f, 0f, 0f, 1f, 0f);

        GL11.glPushMatrix();
        GL11.glTranslatef(cx, cy, cz);
        drawStars(time);
        GL11.glPopMatrix();

        drawEarth(time);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        SatInfo currentSat = (this.satellites != null && !this.satellites.isEmpty())
            ? this.satellites.get(this.satIndex % this.satellites.size()) : null;
        float satHealth = currentSat != null ? currentSat.health : 1.0f;

        float recoilX = 0f, recoilZ = 0f, recoilTilt = 0f;
        if (this.impactTicks >= 0) {
            float it = this.impactTicks + partialTicks;
            float impulse = (float) Math.exp(-it * 0.045f) * (float) Math.sin(Math.min(Math.PI * 0.5, it * 0.22));
            recoilX = impulse * 1.45f;
            recoilZ = -impulse * 0.65f;
            recoilTilt = impulse * 34f;
        }

        GL11.glPushMatrix();
        GL11.glTranslatef(recoilX, 0f, recoilZ);
        GL11.glRotatef(recoilTilt, 0.3f, 0f, -1f);
        this.satelliteModel.renderAll(1.0f, time * 0.45f, -1f, satHealth);
        GL11.glPopMatrix();

        boolean isTarget = currentSat != null && (currentSat.targeted || ClientMeteorHandler.isTargetString(currentSat.id));
        if (this.impactTicks >= 0) {
            float it = this.impactTicks + partialTicks;
            if (it <= 55f) {
                float progress = it / 55.0f;
                float mx = -1.2f + progress * 22.0f;
                float my = 0.8f - progress * 4.5f;
                float mz = 0.5f - progress * 9.0f;
                renderMeteorWithTrail(mx, my, mz, 0.82f, 0.40f, -0.18f, -0.35f, time);
            }
            renderImpactExplosion(it, recoilX, recoilZ);
        } else if (ClientMeteorHandler.active && (isTarget || currentSat == null)) {
            float total = Math.max(1f, ClientMeteorHandler.totalTicks);
            float rem = Math.max(0f, ClientMeteorHandler.remainingTicks - partialTicks);
            float approach = 1.0f - Math.min(1f, rem / total);
            float dist = 3.2f + (1.0f - approach) * 26.0f;
            float mx = -dist * 0.88f;
            float my = 0.8f + dist * 0.24f;
            float mz = dist * 0.42f;
            renderMeteorWithTrail(mx, my, mz, 0.80f, 0.88f, -0.24f, -0.42f, time);
        }

        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glPopClientAttrib();
        GL11.glPopAttrib();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        renderHud(currentSat, satHealth);
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

    private void drawStars(float time) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glPointSize(1.75f);
        GL11.glBegin(GL11.GL_POINTS);
        for (int i = 0; i < STAR_COUNT; i++) {
            float tw = STARS[i][3] * (0.72f + 0.28f * (float) Math.sin(time * 0.06f + i));
            GL11.glColor4f(tw, tw * 0.98f, Math.min(1f, tw * 1.05f), 1f);
            GL11.glVertex3f(STARS[i][0] * 300f, STARS[i][1] * 300f, STARS[i][2] * 300f);
        }
        GL11.glEnd();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawEarth(float time) {
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0f, -142f, 0f);
        GL11.glRotatef(22f, 0f, 0f, 1f);
        GL11.glRotatef(time * 0.08f, 0f, 1f, 0f);

        this.mc.getTextureManager().bindTexture(TEX_EARTH);
        drawTexturedSphere(114f, 34, 52, 1.0f);

        GL11.glRotatef(time * 0.04f + 40f, 0f, 1f, 0f);
        this.mc.getTextureManager().bindTexture(TEX_CLOUDS);
        drawTexturedSphere(115f, 30, 46, 0.85f);
        GL11.glPopMatrix();
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawTexturedSphere(float r, int stacks, int slices, float alpha) {
        Tessellator t = Tessellator.instance;
        for (int i = 0; i < stacks; i++) {
            double p0 = Math.PI * i / stacks;
            double p1 = Math.PI * (i + 1) / stacks;
            float v0 = (float) i / stacks;
            float v1 = (float) (i + 1) / stacks;
            t.startDrawing(GL11.GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                double th = 2.0 * Math.PI * j / slices;
                float u = (float) j / slices;
                float x0 = (float) (Math.sin(p0) * Math.cos(th));
                float y0 = (float) Math.cos(p0);
                float z0 = (float) (Math.sin(p0) * Math.sin(th));
                float l0 = Math.max(0.22f, 0.50f + 0.50f * (y0 * 0.65f + x0 * 0.55f));
                t.setColorRGBA_F(l0, l0, Math.min(1f, l0 * 1.04f), alpha);
                t.addVertexWithUV(x0 * r, y0 * r, z0 * r, u, v0);

                float x1 = (float) (Math.sin(p1) * Math.cos(th));
                float y1 = (float) Math.cos(p1);
                float z1 = (float) (Math.sin(p1) * Math.sin(th));
                float l1 = Math.max(0.22f, 0.50f + 0.50f * (y1 * 0.65f + x1 * 0.55f));
                t.setColorRGBA_F(l1, l1, Math.min(1f, l1 * 1.04f), alpha);
                t.addVertexWithUV(x1 * r, y1 * r, z1 * r, u, v1);
            }
            t.draw();
        }
    }

    private void renderMeteorWithTrail(float x, float y, float z, float radius,
            float vx, float vy, float vz, float time) {
        float vLen = (float) Math.sqrt(vx * vx + vy * vy + vz * vz);
        if (vLen < 1e-4f) vLen = 1f;
        float tx = -vx / vLen, ty = -vy / vLen, tz = -vz / vLen;

        float ax = Math.abs(ty) < 0.9f ? 0f : 1f;
        float ay = Math.abs(ty) < 0.9f ? 1f : 0f;
        float ux = ty * 0f - tz * ay;
        float uy = tz * ax - tx * 0f;
        float uz = tx * ay - ty * ax;
        float uLen = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= uLen; uy /= uLen; uz /= uLen;
        float rx = ty * uz - tz * uy;
        float ry = tz * ux - tx * uz;
        float rz = tx * uy - ty * ux;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);

        drawFlameCone(x, y, z, tx, ty, tz, ux, uy, uz, rx, ry, rz,
            radius * 1.30f, 8.8f, time, 0.95f, 0.28f, 0.03f, 0.52f);
        drawFlameCone(x, y, z, tx, ty, tz, ux, uy, uz, rx, ry, rz,
            radius * 0.95f, 6.2f, time * 1.3f, 1.00f, 0.58f, 0.08f, 0.78f);
        drawFlameCone(x, y, z, tx, ty, tz, ux, uy, uz, rx, ry, rz,
            radius * 0.62f, 3.6f, time * 1.7f, 1.00f, 0.94f, 0.58f, 0.95f);

        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        for (int i = 0; i < 60; i++) {
            float phase = ((time * (0.28f + (i % 5) * 0.05f) + i * 0.37f) % 1.0f);
            float distAlong = radius * 0.5f + phase * 9.5f;
            float spread = (0.18f + phase * 0.95f) * radius;
            float ang = i * 2.39996f + time * 0.14f;
            float offU = (float) Math.cos(ang) * spread;
            float offR = (float) Math.sin(ang) * spread;
            float px = x + tx * distAlong + ux * offU + rx * offR;
            float py = y + ty * distAlong + uy * offU + ry * offR;
            float pz = z + tz * distAlong + uz * offU + rz * offR;
            float sz = (0.04f + (1f - phase) * 0.06f) * radius;
            float a = (1f - phase) * 0.90f;
            float g = Math.max(0.18f, 0.95f - phase * 0.72f);
            float b = Math.max(0.02f, 0.55f - phase * 0.55f);
            t.setColorRGBA_F(1.0f, g, b, a);
            t.addVertex(px - sz, py - sz, pz);
            t.addVertex(px + sz, py - sz, pz);
            t.addVertex(px + sz, py + sz, pz);
            t.addVertex(px - sz, py + sz, pz);
        }
        t.draw();

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_CULL_FACE);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, y, z);
        GL11.glRotatef(time * 2.4f, 0.35f, 1.0f, 0.25f);
        drawCrateredRock(radius, -tx, -ty, -tz);
        GL11.glPopMatrix();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawFlameCone(float x, float y, float z,
            float tx, float ty, float tz, float ux, float uy, float uz, float rx, float ry, float rz,
            float baseRad, float len, float time, float r, float g, float b, float alpha) {
        Tessellator t = Tessellator.instance;
        int rings = 6;
        int slices = 14;
        for (int ring = 0; ring < rings; ring++) {
            float f0 = (float) ring / rings;
            float f1 = (float) (ring + 1) / rings;
            float rad0 = baseRad * (1f - f0 * 0.90f) * (1f + 0.08f * (float) Math.sin(time * 0.9f + f0 * 6f));
            float rad1 = baseRad * (1f - f1 * 0.90f) * (1f + 0.08f * (float) Math.sin(time * 0.9f + f1 * 6f));
            float a0 = alpha * (1f - f0 * 0.92f);
            float a1 = alpha * (1f - f1);

            t.startDrawing(GL11.GL_QUAD_STRIP);
            for (int s = 0; s <= slices; s++) {
                double ang = Math.PI * 2.0 * s / slices;
                float ca = (float) Math.cos(ang);
                float sa = (float) Math.sin(ang);
                float wob = 1f + 0.12f * (float) Math.sin(ang * 3.0 + time * 0.8f);

                float d0 = f0 * len;
                t.setColorRGBA_F(r, g, b, a0);
                t.addVertex(
                    x + tx * d0 + (ux * ca + rx * sa) * rad0 * wob,
                    y + ty * d0 + (uy * ca + ry * sa) * rad0 * wob,
                    z + tz * d0 + (uz * ca + rz * sa) * rad0 * wob);

                float d1 = f1 * len;
                t.setColorRGBA_F(r, Math.max(0.08f, g * 0.72f), b * 0.5f, a1);
                t.addVertex(
                    x + tx * d1 + (ux * ca + rx * sa) * rad1 * wob,
                    y + ty * d1 + (uy * ca + ry * sa) * rad1 * wob,
                    z + tz * d1 + (uz * ca + rz * sa) * rad1 * wob);
            }
            t.draw();
        }
    }

    private void drawCrateredRock(float radius, float hotX, float hotY, float hotZ) {
        Tessellator t = Tessellator.instance;
        int stacks = 14;
        int slices = 20;
        for (int i = 0; i < stacks; i++) {
            double p0 = Math.PI * i / stacks;
            double p1 = Math.PI * (i + 1) / stacks;
            t.startDrawing(GL11.GL_QUAD_STRIP);
            for (int j = 0; j <= slices; j++) {
                double th = 2.0 * Math.PI * j / slices;
                emitRockVertex(t, p0, th, radius, hotX, hotY, hotZ);
                emitRockVertex(t, p1, th, radius, hotX, hotY, hotZ);
            }
            t.draw();
        }
    }

    private void emitRockVertex(Tessellator t, double phi, double theta, float radius,
            float hotX, float hotY, float hotZ) {
        float nx = (float) (Math.sin(phi) * Math.cos(theta));
        float ny = (float) Math.cos(phi);
        float nz = (float) (Math.sin(phi) * Math.sin(theta));

        float crater = 0f;
        crater += craterProfile(nx, ny, nz, 0.52f, 0.71f, 0.46f, 0.44f);
        crater += craterProfile(nx, ny, nz, -0.68f, 0.38f, -0.62f, 0.40f);
        crater += craterProfile(nx, ny, nz, 0.25f, -0.78f, -0.57f, 0.42f);
        crater += craterProfile(nx, ny, nz, -0.35f, -0.35f, 0.86f, 0.36f);

        float ridge = 0.08f * (float) Math.sin(nx * 7f + ny * 5f) * (float) Math.cos(nz * 6f);
        float rLocal = radius * (0.94f + ridge - crater * 0.26f);

        float sun = Math.max(0.22f, 0.50f + 0.50f * (nx * 0.6f + ny * 0.7f + nz * 0.4f));
        float frontHeat = Math.max(0f, nx * hotX + ny * hotY + nz * hotZ);
        float magmaCrack = Math.max(0f, (float) Math.sin(nx * 11f - nz * 9f) * (float) Math.cos(ny * 10f) - 0.55f) * 2.2f;
        float glow = Math.min(1f, frontHeat * frontHeat * 0.85f + magmaCrack * 0.70f);

        float rockR = 0.24f * sun * (1f - crater * 0.45f) + glow * 0.95f;
        float rockG = 0.19f * sun * (1f - crater * 0.45f) + glow * 0.38f;
        float rockB = 0.16f * sun * (1f - crater * 0.45f) + glow * 0.04f;
        t.setColorOpaque_F(Math.min(1f, rockR), Math.min(1f, rockG), Math.min(1f, rockB));
        t.addVertex(nx * rLocal, ny * rLocal, nz * rLocal);
    }

    private float craterProfile(float x, float y, float z, float cx, float cy, float cz, float rad) {
        float dx = x - cx, dy = y - cy, dz = z - cz;
        float d = (float) Math.sqrt(dx * dx + dy * dy + dz * dz) / rad;
        if (d >= 1.25f) return 0f;
        if (d < 0.85f) return 1.0f - (d / 0.85f) * (d / 0.85f);
        return -0.22f * (1f - Math.abs(d - 1.0f) / 0.25f);
    }

    private void renderImpactExplosion(float it, float satX, float satZ) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glDepthMask(false);

        Tessellator t = Tessellator.instance;

        if (it < 24f) {
            float p = it / 24f;
            float ringR = 0.35f + p * 3.8f;
            float alpha = (1f - p) * 0.85f;
            t.startDrawing(GL11.GL_LINE_LOOP);
            t.setColorRGBA_F(1.0f, 0.65f, 0.15f, alpha);
            for (int i = 0; i < 32; i++) {
                double a = Math.PI * 2.0 * i / 32.0;
                t.addVertex(satX + Math.cos(a) * ringR, 0.35 + Math.sin(a) * ringR * 0.45f, satZ + Math.sin(a) * ringR);
            }
            t.draw();
        }

        for (int i = 0; i < IMPACT_SHARDS.length; i++) {
            float[] sh = IMPACT_SHARDS[i];
            float age = Math.max(0f, it - sh[6]);
            if (age <= 0f || age > 85f) continue;

            float life = 1f - age / 85f;
            float px = satX + sh[0] * age;
            float py = 0.35f + sh[1] * age;
            float pz = satZ + sh[2] * age;
            float sz = sh[4] * (sh[5] == 0f ? (0.6f + life * 0.9f) : 1.0f);

            GL11.glPushMatrix();
            GL11.glTranslatef(px, py, pz);
            GL11.glRotatef(age * sh[3], 1f, 0.7f, 0.4f);

            if (sh[5] == 0f) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                t.startDrawingQuads();
                t.setColorRGBA_F(1.0f, 0.42f + 0.50f * life, 0.08f * life, life * 0.95f);
                drawCubeFaces(t, sz);
                t.draw();
            } else if (sh[5] == 1f) {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                t.startDrawingQuads();
                t.setColorRGBA_F(0.95f, 0.76f, 0.25f, Math.min(1f, life * 1.4f));
                drawCubeFaces(t, sz * 1.3f);
                t.draw();
            } else {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                t.startDrawingQuads();
                t.setColorRGBA_F(0.32f, 0.25f, 0.21f, Math.min(1f, life * 1.4f));
                drawCubeFaces(t, sz * 1.15f);
                t.draw();
            }
            GL11.glPopMatrix();
        }

        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(1f, 1f, 1f, 1f);
    }

    private void drawCubeFaces(Tessellator t, float s) {
        t.addVertex(-s, s, s); t.addVertex(s, s, s); t.addVertex(s, s, -s); t.addVertex(-s, s, -s);
        t.addVertex(-s, -s, -s); t.addVertex(s, -s, -s); t.addVertex(s, -s, s); t.addVertex(-s, -s, s);
        t.addVertex(-s, -s, s); t.addVertex(s, -s, s); t.addVertex(s, s, s); t.addVertex(-s, s, s);
        t.addVertex(s, -s, -s); t.addVertex(-s, -s, -s); t.addVertex(-s, s, -s); t.addVertex(s, s, -s);
        t.addVertex(s, -s, s); t.addVertex(s, -s, -s); t.addVertex(s, s, -s); t.addVertex(s, s, s);
        t.addVertex(-s, -s, -s); t.addVertex(-s, -s, s); t.addVertex(-s, s, s); t.addVertex(-s, s, -s);
    }

    private void renderHud(SatInfo sat, float health) {
        int pct = Math.round(Math.max(0f, Math.min(1f, health)) * 100f);
        String name = sat != null ? sat.name : "\u00d3rbita Baixa";
        String status;
        int col = 0xD8EEFF;
        if (this.impactTicks >= 0) {
            status = "\u00a7c\u00a7lCOLIS\u00c3O DETECTADA! \u00a7eIntegridade: " + pct + "%";
            col = 0xFF8866;
        } else if (ClientMeteorHandler.active) {
            status = "\u00a7cMETEORO EM ROTA: \u00a7e" + ClientMeteorHandler.formatCountdown() + " \u00a77| Sa\u00fade: \u00a7b" + pct + "%";
        } else {
            status = "\u00a7aMONITORANDO \u00a77| Integridade: \u00a7b" + pct + "% \u00a77| Arraste p/ girar 360\u00b0";
        }
        this.fontRendererObj.drawStringWithShadow("\u00a7b\u00a7lTELESC\u00d3PIO \u2014 " + name, 12, 10, 0xFFFFFF);
        this.fontRendererObj.drawStringWithShadow(status, 12, 23, col);
    }

    @Override
    protected void keyTyped(char c, int key) {
        if (key == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}