package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.TileControlKeyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderiza a bancada aeroespacial de 3 blocos com um tecladão mecânico
 * retroiluminado de múltiplas teclas e controles laterais abaixo do monitor 2x3.
 */
@SideOnly(Side.CLIENT)
public class TileControlKeyboardRenderer extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileControlKeyboard)) {
            return;
        }
        TileControlKeyboard kb = (TileControlKeyboard) te;
        if (!kb.isMaster()) {
            return;
        }

        long worldTime = te.getWorldObj() != null ? te.getWorldObj().getTotalWorldTime() : 0L;
        float t = worldTime + partialTicks;

        int f = kb.getFacing();
        float rot = f == 0 ? 0f : (f == 1 ? -90f : (f == 2 ? 180f : 90f));

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glRotatef(rot, 0f, 1f, 0f);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);

        // Pedestais laterais e central sob a bancada
        this.box(-1.38f, 0.0f, -0.18f, -1.10f, 0.72f, 0.48f, 0.16f, 0.21f, 0.28f);
        this.box(1.10f, 0.0f, -0.18f, 1.38f, 0.72f, 0.48f, 0.16f, 0.21f, 0.28f);
        this.box(-0.30f, 0.0f, 0.02f, 0.30f, 0.72f, 0.48f, 0.13f, 0.17f, 0.23f);

        // Tampo largo de 3 blocos encostado na parede (+Z = 0.50)
        this.box(-1.48f, 0.72f, -0.36f, 1.48f, 0.84f, 0.50f, 0.22f, 0.28f, 0.36f);
        // Friso frontal iluminado
        this.box(-1.46f, 0.74f, -0.38f, 1.46f, 0.78f, -0.35f, 0.15f, 0.78f, 0.96f);

        // Base inclinada do tecladão ocupando a região central (2.1 blocos de largura!)
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 0.84f, 0.05f);
        GL11.glRotatef(8f, 1f, 0f, 0f);
        this.box(-1.05f, 0.0f, -0.26f, 1.05f, 0.045f, 0.22f, 0.11f, 0.14f, 0.19f);

        // 4 fileiras x 18 colunas de teclas individuais com retroiluminação ciano/âmbar
        for (int row = 0; row < 4; row++) {
            float kz0 = -0.20f + row * 0.095f;
            float kz1 = kz0 + 0.068f;
            for (int col = 0; col < 18; col++) {
                // Barra de espaço longa na fileira frontal central
                if (row == 0 && col >= 5 && col <= 11) {
                    if (col == 5) {
                        this.box(-0.96f + 5 * 0.105f, 0.045f, kz0, -0.96f + 12 * 0.105f - 0.025f, 0.072f, kz1,
                            0.20f, 0.78f, 0.96f);
                    }
                    continue;
                }
                float kx0 = -0.96f + col * 0.105f;
                float kx1 = kx0 + 0.080f;
                float pulse = 0.75f + 0.25f * (float) Math.sin(t * 0.10f + col * 0.45f + row);
                boolean accentKey = (row == 3 && (col == 0 || col == 17)) || (col >= 15 && row == 1);
                if (accentKey) {
                    this.box(kx0, 0.045f, kz0, kx1, 0.072f, kz1, 0.96f * pulse, 0.62f * pulse, 0.18f);
                } else {
                    this.box(kx0, 0.045f, kz0, kx1, 0.070f, kz1, 0.28f * pulse, 0.42f * pulse, 0.56f);
                }
            }
        }
        GL11.glPopMatrix();

        // Módulo trackball à direita e manete orbital à esquerda
        this.box(1.14f, 0.84f, -0.18f, 1.38f, 0.88f, 0.16f, 0.14f, 0.19f, 0.26f);
        this.box(1.22f, 0.88f, -0.06f, 1.30f, 0.94f, 0.02f, 0.20f, 0.88f, 1.0f);

        this.box(-1.38f, 0.84f, -0.18f, -1.12f, 0.88f, 0.18f, 0.14f, 0.19f, 0.26f);
        this.box(-1.30f, 0.88f, -0.04f, -1.20f, 1.02f, 0.06f, 0.36f, 0.44f, 0.54f);
        this.box(-1.34f, 1.00f, -0.07f, -1.16f, 1.05f, 0.09f, 0.18f, 0.85f, 1.0f);

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }

    private void box(float x0, float y0, float z0, float x1, float y1, float z1,
            float r, float g, float b) {
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.setColorOpaque_F(r * 1.04f, g * 1.04f, b * 1.04f);
        t.addVertex(x0, y1, z1); t.addVertex(x1, y1, z1); t.addVertex(x1, y1, z0); t.addVertex(x0, y1, z0);
        t.setColorOpaque_F(r * 0.72f, g * 0.72f, b * 0.72f);
        t.addVertex(x0, y0, z0); t.addVertex(x1, y0, z0); t.addVertex(x1, y0, z1); t.addVertex(x0, y0, z1);
        t.setColorOpaque_F(r * 0.92f, g * 0.92f, b * 0.92f);
        t.addVertex(x0, y0, z1); t.addVertex(x1, y0, z1); t.addVertex(x1, y1, z1); t.addVertex(x0, y1, z1);
        t.setColorOpaque_F(r * 0.84f, g * 0.84f, b * 0.84f);
        t.addVertex(x1, y0, z0); t.addVertex(x0, y0, z0); t.addVertex(x0, y1, z0); t.addVertex(x1, y1, z0);
        t.addVertex(x1, y0, z1); t.addVertex(x1, y0, z0); t.addVertex(x1, y1, z0); t.addVertex(x1, y1, z1);
        t.addVertex(x0, y0, z0); t.addVertex(x0, y0, z1); t.addVertex(x0, y1, z1); t.addVertex(x0, y1, z0);
        t.draw();
    }
}