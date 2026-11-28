package com.orbitalsunworks.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.rocket.TileDispersionRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Estrutura vertical (6 blocos) do Foguete Dispersor de Meteoros na plataforma.
 */
public class BlockDispersionRocket extends BlockContainer {

    public static final int HEIGHT = 6;

    @SideOnly(Side.CLIENT)
    private IIcon iconBase;
    private final ThreadLocal<Boolean> manualDrop = new ThreadLocal<Boolean>();
    private boolean removingColumn = false;

    public BlockDispersionRocket() {
        super(Material.iron);
        this.setBlockName("blockDispersionRocket");
        this.setCreativeTab(null);
        this.setHardness(3.0f);
        this.setResistance(6.0f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return meta == 0 ? new TileDispersionRocket() : null;
    }

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
        return -1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconBase = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockDispersionRocket");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBase;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.10f, 0.0f, 0.10f, 0.90f, 1.0f, 0.90f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        int baseY = y - world.getBlockMetadata(x, y, z);
        if (!world.isRemote && world.getBlock(x, baseY, z) == this) {
            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_DISPERSION_ROCKET, world, x, baseY, z);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(OSItems.itemDispersionRocket);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
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
            for (int i = HEIGHT - 1; i >= 0; i--) {
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
            drops.add(new ItemStack(OSItems.itemDispersionRocket));
        }
        return drops;
    }

    @Override
    public boolean canSilkHarvest() {
        return false;
    }
}