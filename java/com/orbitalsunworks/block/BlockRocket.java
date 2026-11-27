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

import com.orbitalsunworks.rocket.TileRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRocket extends BlockContainer {

    public static final int HEIGHT = 6;

    @SideOnly(Side.CLIENT)
    private IIcon iconBase;

    private boolean removingColumn = false;

    public BlockRocket() {
        super(Material.iron);
        this.setBlockName("blockRocket");
        this.setCreativeTab(null);
        this.setHardness(3.0f);
        this.setResistance(5.0f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return meta == 0 ? new TileRocket() : null;
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
        this.iconBase = reg.registerIcon(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockRocket");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBase;
    }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta >= HEIGHT - 1) {
            this.setBlockBounds(0.18f, 0.0f, 0.18f, 0.82f, 1.0f, 0.82f);
        } else {
            this.setBlockBounds(0.06f, 0.0f, 0.06f, 0.94f, 1.0f, 0.94f);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        int baseY = y - world.getBlockMetadata(x, y, z);
        if (!world.isRemote && world.getBlock(x, baseY, z) == this) {
            player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                com.orbitalsunworks.handler.GuiHandler.ID_ROCKET, world, x, baseY, z);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(com.orbitalsunworks.OSItems.itemRocketT1);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player,
            int x, int y, int z, boolean willHarvest) {
        if (this.removingColumn) {
            return super.removedByPlayer(world, player, x, y, z, willHarvest);
        }
        int meta = world.getBlockMetadata(x, y, z);
        int baseY = y - meta;
        boolean creative = player != null && player.capabilities.isCreativeMode;

        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, baseY, z);
            if (te instanceof TileRocket) {
                ((TileRocket) te).dropInventory(world, x, baseY, z);
            }
            if (!creative) {
                this.manualDrop.set(Boolean.TRUE);
                try {
                    dropBlockAsItem(world, x, baseY, z, 0, 0);
                } finally {
                    this.manualDrop.set(Boolean.FALSE);
                }
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
            drops.add(new ItemStack(com.orbitalsunworks.OSItems.itemRocketT1));
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