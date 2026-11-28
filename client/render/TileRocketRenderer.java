package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.rocket.TileRocket;

/**
 * Renderiza o foguete Tier 1 posicionado na plataforma (TESR).
 *
 * O foguete é um multiblock de 3 blocos de altura (meta 0 = base,
 * 1 = meio, 2 = topo; só a base tem inventário/GUI). O modelo completo
 * (3 blocos de altura) é desenhado UMA vez, a partir do tile da base.
 */
public class TileRocketRenderer extends TileEntitySpecialRenderer {

    private final ModelRocket model = new ModelRocket();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        // Só a base (meta 0) desenha; os blocos de cima são invisíveis.
        if (tile.getBlockMetadata() != 0) {
            return;
        }
        GL11.glPushMatrix();
        // (0,0,0) do modelo = centro do bloco base, no chão da plataforma.
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        this.model.renderAll();
        GL11.glPopMatrix();
    }
}
