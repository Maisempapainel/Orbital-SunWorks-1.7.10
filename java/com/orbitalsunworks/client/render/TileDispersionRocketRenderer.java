package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Renderiza o Foguete Dispersor estacionado na Plataforma de Lançamento 7x7.
 */
@SideOnly(Side.CLIENT)
public class TileDispersionRocketRenderer extends TileEntitySpecialRenderer {

    public static final float SCALE_XZ = 1.40f;
    public static final float SCALE_Y = 1.20f;

    private final ModelDispersionRocket model = new ModelDispersionRocket();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (tile.getBlockMetadata() != 0) {
            return;
        }
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        GL11.glScalef(SCALE_XZ, SCALE_Y, SCALE_XZ);
        this.model.renderAll();
        GL11.glPopMatrix();
    }
}