package com.orbitalsunworks.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.satellite.TileControlMonitor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Monitor Tático 2x3 fino montado na parede da Sala de Controle.
 * Clicar em qualquer bloco da tela abre a Sala de Controle Orbital.
 */
public class BlockControlMonitor extends BlockContainer {

    public BlockControlMonitor() {
        super(Material.iron);
        this.setBlockName("blockControlMonitor");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(3.5f);
        this.setResistance(15.0f);
        this.setStepSound(soundTypeMetal);
        this.setLightLevel(0.45f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileControlMonitor();
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
        this.blockIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockControlMonitor");
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        int facing = te instanceof TileControlMonitor ? ((TileControlMonitor) te).getFacing() : 0;
        if (facing == 0) {
            this.setBlockBounds(0.0f, 0.0f, 0.72f, 1.0f, 1.0f, 1.0f);
        } else if (facing == 1) {
            this.setBlockBounds(0.0f, 0.0f, 0.0f, 0.28f, 1.0f, 1.0f);
        } else if (facing == 2) {
            this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.28f);
        } else {
            this.setBlockBounds(0.72f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileControlMonitor) {
            ((TileControlMonitor) te).configure(facing, true);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_CONTROL_ROOM, world, x, y, z);
        }
        return true;
    }
}