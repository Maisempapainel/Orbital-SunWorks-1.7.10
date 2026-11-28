package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TESR da estrutura do satélite na órbita com dano visual persistente conforme a vida útil.
 */
@SideOnly(Side.CLIENT)
public class TileOrbitSatelliteRenderer extends TileEntitySpecialRenderer {

    private final ModelSatellite model = new ModelSatellite();

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof TileOrbitSatellite)) {
            return;
        }
        int meta = te.getWorldObj().getBlockMetadata(te.xCoord, te.yCoord, te.zCoord);
        if (meta != 0) {
            return;
        }
        TileOrbitSatellite tile = (TileOrbitSatellite) te;
        float health = Math.max(0.01f, Math.min(1.0f, tile.getHealth()));

        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y - 0.6, z + 0.5);

        long time = te.getWorldObj().getTotalWorldTime();
        float spin = (time + partialTicks) * 0.05f;

        GL11.glScalef(2.2f, 2.2f, 2.2f);
        this.model.renderAll(1.0f, spin, -1f, health);

        // Brilho nos painéis solares apenas sobre as seções intactas
        float glimmer = 0.5f + 0.5f * (float) Math.sin((time + partialTicks) * 0.03f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        Tessellator t = Tessellator.instance;

        int missing = ModelSatellite.getMissingSegmentCount(health);
        for (int side = -1; side <= 1; side += 2) {
            int intact = 0;
            for (int seg = 0; seg < ModelSatellite.SEGMENTS; seg++) {
                int id = (side < 0 ? 0 : 6) + seg;
                boolean broken = false;
                for (int k = 0; k < missing && k < 11; k++) {
                    // Mesma contagem aproximada de painéis restantes por asa
                    if ((k % 2 == 0 && side > 0 && (5 - k / 2) == seg)
                        || (k % 2 == 1 && side < 0 && (5 - k / 2) == seg)) {
                        broken = true;
                    }
                }
                if (!broken) {
                    intact++;
                }
            }
            float ratio = intact / (float) ModelSatellite.SEGMENTS;
            if (ratio > 0.05f) {
                float w = 1.26f * ratio;
                float px = side * (0.76f + w);
                float d = 0.25f;
                float py = 0.62f;
                t.startDrawingQuads();
                t.setColorRGBA_F(0.25f, 0.45f, 0.85f, (0.16f + 0.14f * glimmer) * ratio);
                t.addVertex(px - w, py, -d);
                t.addVertex(px - w, py, d);
                t.addVertex(px + w, py, d);
                t.addVertex(px + w, py, -d);
                t.draw();
            }
        }

        // Luz de janela no corpo
        t.startDrawingQuads();
        t.setColorRGBA_F(1.0f, 0.85f, 0.45f, 0.7f * health);
        t.addVertex(0.47f, 0.35f, -0.18f);
        t.addVertex(0.47f, 0.35f, 0.18f);
        t.addVertex(0.47f, 0.62f, 0.18f);
        t.addVertex(0.47f, 0.62f, -0.18f);
        t.draw();

        // Farol na ponta da antena inclinada
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 1.22f, 0f);
        GL11.glRotatef(35f, 1f, 0f, 0f);
        GL11.glTranslatef(0f, 0.64f + 0.59f, 0f);
        float blink = ((time + partialTicks) % 40f) < 8f ? 1f : 0.15f;
        t.startDrawingQuads();
        t.setColorRGBA_F(1f, 0.12f, 0.12f, blink);
        t.addVertex(-0.05f, 0f, -0.05f);
        t.addVertex(-0.05f, 0f, 0.05f);
        t.addVertex(0.05f, 0f, 0.05f);
        t.addVertex(0.05f, 0f, -0.05f);
        t.draw();
        GL11.glPopMatrix();

        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_LIGHTING);

        GL11.glPopMatrix();
    }
}