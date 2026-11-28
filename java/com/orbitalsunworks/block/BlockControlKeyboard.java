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
import com.orbitalsunworks.satellite.TileControlKeyboard;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Console com tecladão retroiluminado de 3 blocos abaixo do monitor 2x3.
 * Clicar em qualquer parte da bancada/teclado abre a Sala de Controle Orbital.
 */
public class BlockControlKeyboard extends BlockContainer {

    public BlockControlKeyboard() {
        super(Material.iron);
        this.setBlockName("blockControlKeyboard");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(3.5f);
        this.setResistance(15.0f);
        this.setStepSound(soundTypeMetal);
        this.setLightLevel(0.25f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileControlKeyboard();
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
        this.blockIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockControlKeyboard");
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.88f, 1.0f);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int facing = MathHelper.floor_double((placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileControlKeyboard) {
            ((TileControlKeyboard) te).configure(facing, true);
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