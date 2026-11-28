package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * CHÃO INVISÍVEL DA ÓRBITA (v0.10.0) — o "colchão" anti-void.
 *
 * O pedido do usuário: "eu quero um chão preto invisível lá em baixo,
 * sei lá, na camada 30, que não permita que meu personagem morra no
 * void, e ele tem que continuar vendo a Terra gigantesca lá embaixo".
 *
 * Regras:
 *  - SEM textura, SEM luz, SEM partícula: registerBlockIcons vazio, getIcon null
 *    e getRenderType() == -1 (SEM RENDER).
 *  - Colisão COMPLETA (full cube): segura o jogador na camada 30.
 *  - Indestrutível e inclicável (não aparece no creative, não quebra).
 *  - Sem drop, sem seleção (bounding box de raytrace vazia).
 */
public class BlockOrbitFloor extends Block {

    public BlockOrbitFloor() {
        super(Material.iron);
        this.setBlockName("blockOrbitFloor");
        this.setBlockUnbreakable();
        this.setResistance(Float.MAX_VALUE);
        this.setStepSound(soundTypeMetal);
        // SEM creative tab: o jogador nunca deve pegar este bloco.
    }

    /** Evita MISSING_ICON_BLOCK no atlas do cliente. */
    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
    }

    /** NUNCA desenha nada (render type -1 = sem render, igual barrier). */
    @Override
    public int getRenderType() {
        return -1;
    }

    /** Sem textura em lado nenhum. */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return null;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    /** Sem partícula de quebra/environment (invisível TOTAL). */
    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
    }

    /** NÃO adiciona hitbox de SELEÇÃO (clicar "atravessa" pro mundo). */
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        // Colisão cheia (o "chão" segura o jogador).
        this.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
    }

    /** Raytrace de seleção: NUNCA seleciona (bounding box zero). */
    @Override
    public void setBlockBoundsForItemRender() {
        this.setBlockBounds(0f, 0f, 0f, 0f, 0f, 0f);
    }

    /** Sem colisão "mole": é sólido como pedra. */
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    /** Indestrutível no creative também? Não: nem clicável. */
    @Override
    public boolean canHarvestBlock(EntityPlayer player, int meta) {
        return false;
    }

    @Override
    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        // Nada: inclicável.
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hx, float hy, float hz) {
        return false;
    }

    /** SEM drop nunca. */
    @Override
    public int quantityDropped(Random rand) {
        return 0;
    }
}