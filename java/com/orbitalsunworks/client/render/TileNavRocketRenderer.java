package com.orbitalsunworks.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import com.orbitalsunworks.rocket.TileNavRocket;

/**
 * TESR do Foguete de Navegação POSICIONADO na plataforma (v0.7.0).
 *
 * Igual ao TileRocketRenderer do T1: o multiblock é 1x3 (meta 0 = base
 * com tile), o modelo completo (3 blocos de altura) é desenhado UMA vez
 * a partir da base. O modelo é o mesmo ModelRocket com um DETALHE: a
 * faixa da nave é CIANO (o render aplica uma coloração antes de
 * desenhar, o TESR do T1 não mexe na cor).
 */
public class TileNavRocketRenderer extends TileEntitySpecialRenderer {

    private final ModelRocket model = new ModelRocket();

    @Override
    public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks) {
        if (tile.getBlockMetadata() != 0) {
            return; // só a base desenha
        }
        GL11.glPushMatrix();
        GL11.glTranslated(x + 0.5, y, z + 0.5);
        // Tint ciano-sutil: distingue a nave de navegação do foguete T1
        // no pad (o T1 fica branco puro).
        GL11.glColor4f(0.8f, 1.0f, 1.0f, 1.0f);
        this.model.renderAll();
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glPopMatrix();
    }
}
