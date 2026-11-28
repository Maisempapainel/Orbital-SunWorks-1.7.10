package com.orbitalsunworks.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
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
import com.orbitalsunworks.rocket.TileNavRocket;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockNavRocket extends BlockContainer {

    public static final int HEIGHT = 8;

    @SideOnly(Side.CLIENT)
    private IIcon iconBase;
    private boolean removingColumn = false;

    private final ThreadLocal<Boolean> manualDrop = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public BlockNavRocket() {
        super(Material.iron);
        this.setBlockName("blockNavRocket");
        this.setCreativeTab(null);
        this.setHardness(3.0f);
        this.setResistance(5.0f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return meta == 0 ? new TileNavRocket() : null;
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
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int baseY = y - meta;
        if (baseY >= 0 && baseY < 256) {
            TileEntity te = world.getTileEntity(x, baseY, z);
            if (te instanceof TileNavRocket && ((TileNavRocket) te).orbitReturn) {
                return 13;
            }
        }
        return 4;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconBase = reg.registerIcon(Reference.RESOURCE_PREFIX + "navRocket");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBase;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.08f, 0.0f, 0.08f, 0.92f, 1.0f, 0.92f);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        int baseY = y - world.getBlockMetadata(x, y, z);
        if (!world.isRemote && world.getBlock(x, baseY, z) == this) {
            if (OrbitDimensionManager.isOrbitWorld(world)) {
                player.openGui(OrbitalSunworks.instance, GuiHandler.ID_NAV_RETURN, world, x, baseY, z);
            } else {
                player.openGui(OrbitalSunworks.instance, GuiHandler.ID_NAV_ROCKET, world, x, baseY, z);
            }
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(OSItems.itemNavRocket);
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        if (world instanceof World && OrbitDimensionManager.isOrbitWorld((World) world)) {
            return false;
        }
        return super.canEntityDestroy(world, x, y, z, entity);
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (OrbitDimensionManager.isOrbitWorld(world)) {
            return false;
        }
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
            drops.add(new ItemStack(OSItems.itemNavRocket));
        }
        return drops;
    }

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean canSilkHarvest() {
        return false;
    }
}