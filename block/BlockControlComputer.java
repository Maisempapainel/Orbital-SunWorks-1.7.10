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
import com.orbitalsunworks.satellite.TileControlComputer;

/**
 * Supercomputador da Sala de Controle dentro da Estação de Controle.
 * Mostra telemetria completa, energia da antena, módulos instalados (somente leitura)
 * e alertas/instruções de chuva de meteoros.
 */
public class BlockControlComputer extends BlockContainer {

    public BlockControlComputer() {
        super(Material.iron);
        this.setBlockName("blockControlComputer");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockControlComputer");
        this.setCreativeTab(null);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setLightLevel(0.65f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileControlComputer();
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_CONTROL_ROOM, world, x, y, z);
        }
        return true;
    }
}