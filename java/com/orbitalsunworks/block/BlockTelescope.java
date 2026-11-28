package com.orbitalsunworks.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.satellite.TileTelescope;

/**
 * Telescópio do Observatório dentro da Estação de Controle.
 * Ao clicar, abre o seletor de satélites para visualização espacial em 3D.
 */
public class BlockTelescope extends BlockContainer {

    public BlockTelescope() {
        super(Material.iron);
        this.setBlockName("blockTelescope");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockTelescope");
        this.setCreativeTab(null);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setLightLevel(0.45f);
        this.setStepSound(soundTypeMetal);
        this.setBlockBounds(0.08f, 0.0f, 0.08f, 0.92f, 1.50f, 0.92f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileTelescope();
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
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_TELESCOPE, world, x, y, z);
        }
        return true;
    }
}