package com.orbitalsunworks.block;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.TileStationDoor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Porta metálica automática deslizante por aproximação (2 blocos de altura).
 * Possui esquadrias/bordas sólidas permanentes e só libera a passagem quando 100% aberta.
 */
public class BlockStationDoor extends BlockContainer {

    public BlockStationDoor() {
        super(Material.iron);
        this.setBlockName("stationDoor");
        this.setHardness(4.5f);
        this.setResistance(20.0f);
        this.setStepSound(soundTypeMetal);
        this.setCreativeTab(OSCreativeTab.INSTANCE);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileStationDoor();
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) {
        TileStationDoor door = resolveDoorTile(world, x, y, z);
        return door != null && door.isFullyOpen();
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int axis = resolveAxis(world, x, y, z);
        if (axis == 0) {
            this.setBlockBounds(0f, 0f, 0.34f, 1f, 1f, 0.66f);
        } else {
            this.setBlockBounds(0.34f, 0f, 0f, 0.66f, 1f, 1f);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addCollisionBoxesToList(World world, int x, int y, int z,
            AxisAlignedBB mask, List list, Entity entity) {
        int axis = resolveAxis(world, x, y, z);
        TileStationDoor door = resolveDoorTile(world, x, y, z);
        float open = door != null ? door.getOpenProgress() : 0f;
        boolean fullyOpen = open >= 0.92f;

        // 1) Esquadria / bordas laterais SEMPRE sólidas (0.14 blocos em cada borda)
        float jamb = 0.14f;
        if (axis == 0) {
            addIfIntersects(mask, list, x, y, z, 0f, 0f, 0.32f, jamb, 1f, 0.68f);
            addIfIntersects(mask, list, x, y, z, 1f - jamb, 0f, 0.32f, 1f, 1f, 0.68f);
        } else {
            addIfIntersects(mask, list, x, y, z, 0.32f, 0f, 0f, 0.68f, 1f, jamb);
            addIfIntersects(mask, list, x, y, z, 0.32f, 0f, 1f - jamb, 0.68f, 1f, 1f);
        }

        // 2) Folhas da porta: bloqueiam o vão até que a abertura esteja completa (>= 92%)
        if (!fullyOpen) {
            float slide = open * 0.36f;
            float innerGapLeft = 0.50f - slide;
            float innerGapRight = 0.50f + slide;
            if (axis == 0) {
                addIfIntersects(mask, list, x, y, z, jamb, 0f, 0.38f, Math.max(jamb + 0.02f, innerGapLeft), 1f, 0.62f);
                addIfIntersects(mask, list, x, y, z, Math.min(1f - jamb - 0.02f, innerGapRight), 0f, 0.38f, 1f - jamb, 1f, 0.62f);
            } else {
                addIfIntersects(mask, list, x, y, z, 0.38f, 0f, jamb, 0.62f, 1f, Math.max(jamb + 0.02f, innerGapLeft));
                addIfIntersects(mask, list, x, y, z, 0.38f, 0f, Math.min(1f - jamb - 0.02f, innerGapRight), 0.62f, 1f, 1f - jamb);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void addIfIntersects(AxisAlignedBB mask, List list, int x, int y, int z,
            float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            x + minX, y + minY, z + minZ,
            x + maxX, y + maxY, z + maxZ);
        if (box != null && mask.intersectsWith(box)) {
            list.add(box);
        }
    }

    private static TileStationDoor resolveDoorTile(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 1) {
            TileEntity below = world.getTileEntity(x, y - 1, z);
            if (below instanceof TileStationDoor) {
                return (TileStationDoor) below;
            }
        }
        TileEntity te = world.getTileEntity(x, y, z);
        return te instanceof TileStationDoor ? (TileStationDoor) te : null;
    }

    private static int resolveAxis(IBlockAccess world, int x, int y, int z) {
        TileStationDoor door = resolveDoorTile(world, x, y, z);
        return door != null ? door.getAxis() : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockStationDoor");
    }
}