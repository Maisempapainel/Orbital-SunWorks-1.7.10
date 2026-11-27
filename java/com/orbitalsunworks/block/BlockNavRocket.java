package com.orbitalsunworks.block;

import java.util.ArrayList;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.orbitalsunworks.rocket.TileNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bloco do FOGUETE DE NAVEGAÇÃO posicionado na plataforma (v0.7.0).
 *
 * Espelha o BlockRocket do T1: multiblock de 3 blocos de altura (meta 0 =
 * base com GUI, 1 = meio, 2 = topo), colisão real em cada parte, quebrar
 * QUALQUER parte remove a coluna e devolve o item (survival). Clicar
 * com o direito abre a GUI de seleção de satélite (o "mapa orbital").
 *
 * Diferenças pro T1:
 *  - não tem lançamento de satélite: a GUI lista os satélites DO JOGADOR
 *    e embarca na EntityNavRocket (viagem de navegação);
 *  - não tem slots de módulo (o módulo fica no satélite lá na órbita).
 */
public class BlockNavRocket extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconBase;

    /** Guarda anti-recursão: já estamos removendo a coluna. */
    private boolean removingColumn = false;

    public BlockNavRocket() {
        super(Material.iron);
        this.setBlockName("blockNavRocket");
        this.setCreativeTab(null); // só existe posicionado (item: navRocket)
        this.setHardness(3.0f);
        this.setResistance(5.0f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return meta == 0 ? new TileNavRocket() : null;
    }

    // ------------------------------------------------------------------
    // Renderização: partes invisíveis (TESR desenha o modelo completo a
    // partir da base) — mas com colisão de verdade.
    // ------------------------------------------------------------------

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1; // TESR
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconBase = reg.registerIcon(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "navRocket");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBase;
    }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.15f, 0.0f, 0.15f, 0.85f, 1.0f, 0.85f);
    }

    // ------------------------------------------------------------------
    // Interação: clicar em QUALQUER parte abre o mapa orbital (GUI da base).
    // ------------------------------------------------------------------

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        int baseY = y - world.getBlockMetadata(x, y, z);
        if (!world.isRemote && world.getBlock(x, baseY, z) == this) {
            player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                com.orbitalsunworks.handler.GuiHandler.ID_NAV_ROCKET, world, x, baseY, z);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Pick-block: devolve o ITEM do foguete de navegação.
    // ------------------------------------------------------------------

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(com.orbitalsunworks.OSItems.itemNavRocket);
    }

    // ------------------------------------------------------------------
    // Quebra: remove a COLUNA INTEIRA, dropa o item 1x na base (survival).
    // ------------------------------------------------------------------

    @Override
    public boolean removedByPlayer(World world, net.minecraft.entity.player.EntityPlayer player,
            int x, int y, int z, boolean willHarvest) {
        if (this.removingColumn) {
            return super.removedByPlayer(world, player, x, y, z, willHarvest);
        }
        int meta = world.getBlockMetadata(x, y, z);
        int baseY = y - meta;
        boolean creative = player != null && player.capabilities.isCreativeMode;

        if (!world.isRemote && !creative) {
            this.manualDrop.set(Boolean.TRUE);
            try {
                dropBlockAsItem(world, x, baseY, z, 0, 0);
            } finally {
                this.manualDrop.set(Boolean.FALSE);
            }
        }

        this.removingColumn = true;
        try {
            for (int i = 2; i >= 0; i--) {
                if (world.getBlock(x, baseY + i, z) == this) {
                    world.setBlockToAir(x, baseY + i, z);
                }
            }
        } finally {
            this.removingColumn = false;
        }
        return true;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if (metadata == 0 && this.manualDrop.get() == Boolean.TRUE) {
            drops.add(new ItemStack(com.orbitalsunworks.OSItems.itemNavRocket));
        }
        return drops;
    }

    private final ThreadLocal<Boolean> manualDrop = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean canSilkHarvest() {
        return false;
    }
}
