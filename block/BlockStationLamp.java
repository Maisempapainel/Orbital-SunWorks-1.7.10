package com.orbitalsunworks.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.TileStationLamp;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Luminária de teto high-tech com luz máxima (1.0F) e animação sutil de transmissão luminosa.
 */
public class BlockStationLamp extends BlockContainer {

    public BlockStationLamp() {
        super(Material.iron);
        this.setBlockName("blockStationLamp");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(2.5f);
        this.setResistance(10.0f);
        this.setStepSound(soundTypeGlass);
        this.setLightLevel(1.0f);
        this.setLightOpacity(0);
        this.setBlockBounds(0.08f, 0.80f, 0.08f, 0.92f, 1.0f, 0.92f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileStationLamp();
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
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return AxisAlignedBB.getBoundingBox(x + 0.08, y + 0.82, z + 0.08, x + 0.92, y + 1.0, z + 0.92);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockStationLamp");
    }
}