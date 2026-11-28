package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.TileStationDoor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderiza porta dupla estilo pistão com folhas metálicas que deslizam
 * lateralmente conforme a aproximação do jogador e LED de status.
 */
@SideOnly(Side.CLIENT)
public class TileStationDoorRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileStationDoor)) {
            return;
        }
        TileStationDoor door = (TileStationDoor) te;
        float open = door.getOpenProgress(partialTicks);

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        if (door.getAxis() == 1) {
            GL11.glRotatef(90f, 0f, 1f, 0f);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // Moldura reforçada (topo e batentes laterais)
        this.drawBox(-0.50f, 0.0f, -0.14f, -0.42f, 2.0f, 0.14f, 0.18f, 0.23f, 0.30f);
        this.drawBox(0.42f, 0.0f, -0.14f, 0.50f, 2.0f, 0.14f, 0.18f, 0.23f, 0.30f);
        this.drawBox(-0.50f, 1.88f, -0.15f, 0.50f, 2.0f, 0.15f, 0.16f, 0.21f, 0.28f);

        // LED indicador de estado na travessa superior
        float rLed = open > 0.55f ? 0.18f : (open > 0.05f ? 1.0f : 0.12f);
        float gLed = open > 0.55f ? 0.96f : (open > 0.05f ? 0.68f : 0.58f);
        float bLed = open > 0.55f ? 0.55f : (open > 0.05f ? 0.12f : 0.92f);
        this.drawBox(-0.24f, 1.91f, -0.16f, 0.24f, 1.96f, 0.16f, rLed, gLed, bLed);

        // Folhas deslizantes (abrem até ±0.40 unidades para dentro dos batentes)
        float slide = open * 0.40f;
        for (int side = -1; side <= 1; side += 2) {
            GL11.glPushMatrix();
            GL11.glTranslatef(side * slide, 0f, 0f);
            float x0 = side < 0 ? -0.43f : 0.005f;
            float x1 = side < 0 ? -0.005f : 0.43f;
            // Chapa principal
            this.drawBox(x0, 0.02f, -0.07f, x1, 1.88f, 0.07f, 0.32f, 0.40f, 0.50f);
            // Nervura horizontal de pistão
            this.drawBox(x0, 0.85f, -0.09f, x1, 0.97f, 0.09f, 0.22f, 0.28f, 0.36f);
            // Filete ciano junto ao encaixe central
            float stripX0 = side < 0 ? -0.04f : 0.01f;
            float stripX1 = side < 0 ? -0.01f : 0.04f;
            this.drawBox(stripX0, 0.28f, -0.08f, stripX1, 1.62f, 0.08f, 0.15f, 0.85f, 1.0f);
            GL11.glPopMatrix();
        }

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void drawBox(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_F(r * 1.05f, g * 1.05f, b * 1.05f);
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.setColorOpaque_F(r * 0.72f, g * 0.72f, b * 0.72f);
        t.addVertex(x0, y0, z0); t.addVertex(x1, y0, z0); t.addVertex(x1, y0, z1); t.addVertex(x0, y0, z1);
        t.setColorOpaque_F(r * 0.92f, g * 0.92f, b * 0.92f);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.setColorOpaque_F(r * 0.85f, g * 0.85f, b * 0.85f);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.setColorOpaque_F(r * 0.88f, g * 0.88f, b * 0.88f);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.setColorOpaque_F(r * 0.88f, g * 0.88f, b * 0.88f);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0);
        t.draw();
    }
}