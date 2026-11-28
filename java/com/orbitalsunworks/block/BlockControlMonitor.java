package com.orbitalsunworks.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.satellite.TileControlMonitor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bloco do Monitor Panorâmico 2x3 colado na parede da Sala de Controle.
 */
public class BlockControlMonitor extends BlockContainer {

    public BlockControlMonitor() {
        super(Material.iron);
        this.setBlockName("controlMonitor");
        this.setHardness(3.0f);
        this.setResistance(12.0f);
        this.setLightLevel(0.45f);
        this.setStepSound(soundTypeGlass);
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setBlockBounds(0f, 0f, 0f, 1f, 1f, 1f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileControlMonitor();
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
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
            int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player != null) {
            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_CONTROL_ROOM, world, x, y, z);
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockControlMonitor");
    }
}