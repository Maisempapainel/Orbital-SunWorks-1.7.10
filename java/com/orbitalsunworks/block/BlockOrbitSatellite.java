package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Estrutura física central do satélite em órbita (indestrutível e iluminada).
 */
public class BlockOrbitSatellite extends Block {

    @SideOnly(Side.CLIENT)
    private IIcon iconBase;

    public BlockOrbitSatellite() {
        super(Material.iron);
        this.setBlockName("blockOrbitSatellite");
        this.setCreativeTab(null);
        this.setHardness(-1.0f);
        this.setResistance(6000000.0f);
        this.setLightLevel(0.85f);
        this.setStepSound(soundTypeMetal);
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconBase = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockOrbitSatellite");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBase;
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return metadata == 0;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return metadata == 0 ? new TileOrbitSatellite() : null;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        int baseY = y - world.getBlockMetadata(x, y, z);
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, baseY, z);
            if (te instanceof TileOrbitSatellite) {
                player.openGui(OrbitalSunworks.instance,
                    GuiHandler.ID_ORBIT_SATELLITE, world, x, baseY, z);
            }
        }
        return true;
    }

    @Override
    public int quantityDropped(java.util.Random random) {
        return 0;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player,
            int x, int y, int z, boolean willHarvest) {
        return false;
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
    }

    @Override
    public boolean canHarvestBlock(EntityPlayer player, int metadata) {
        return false;
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
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
}