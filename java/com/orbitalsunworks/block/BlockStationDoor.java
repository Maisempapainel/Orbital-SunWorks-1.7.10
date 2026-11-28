package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.TileStationDoor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Porta metálica automatizada (2 blocos de altura) com abertura deslizante
 * suave estilo pistão ao detectar jogador próximo.
 *  meta = 0: parte inferior (possui TileStationDoor)
 *  meta = 1: parte superior
 */
public class BlockStationDoor extends BlockContainer {

    public BlockStationDoor() {
        super(Material.iron);
        this.setBlockName("blockStationDoor");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(4.5f);
        this.setResistance(20.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return meta == 0 ? new TileStationDoor() : null;
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
        this.blockIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockStationDoor");
    }

    private TileStationDoor getMasterTile(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        int baseY = (meta == 1) ? y - 1 : y;
        TileEntity te = world.getTileEntity(x, baseY, z);
        return te instanceof TileStationDoor ? (TileStationDoor) te : null;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileStationDoor te = this.getMasterTile(world, x, y, z);
        int axis = te != null ? te.getAxis() : 0;
        if (axis == 0) {
            this.setBlockBounds(0.0f, 0.0f, 0.38f, 1.0f, 1.0f, 0.62f);
        } else {
            this.setBlockBounds(0.38f, 0.0f, 0.0f, 0.62f, 1.0f, 1.0f);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        TileStationDoor te = this.getMasterTile(world, x, y, z);
        if (te != null && te.isPassable()) {
            return null;
        }
        int axis = te != null ? te.getAxis() : 0;
        if (axis == 0) {
            return AxisAlignedBB.getBoundingBox(x, y, z + 0.38, x + 1.0, y + 1.0, z + 0.62);
        }
        return AxisAlignedBB.getBoundingBox(x + 0.38, y, z, x + 0.62, y + 1.0, z + 1.0);
    }

    @Override
    public boolean canPlaceBlockAt(World world, int x, int y, int z) {
        return y < world.getHeight() - 1
            && super.canPlaceBlockAt(world, x, y, z)
            && super.canPlaceBlockAt(world, x, y + 1, z);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int axis = (facing == 0 || facing == 2) ? 0 : 1;
        world.setBlockMetadataWithNotify(x, y, z, 0, 2);
        world.setBlock(x, y + 1, z, this, 1, 3);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileStationDoor) {
            ((TileStationDoor) te).setAxis(axis);
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (!world.isRemote) {
            if (meta == 0 && world.getBlock(x, y + 1, z) == this) {
                world.setBlockToAir(x, y + 1, z);
            } else if (meta == 1 && world.getBlock(x, y - 1, z) == this) {
                world.setBlockToAir(x, y - 1, z);
            }
        }
    }
}