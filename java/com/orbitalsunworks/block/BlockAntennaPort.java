package com.orbitalsunworks.block;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.TileAntennaPort;
import com.orbitalsunworks.util.OSUtils;

/**
 * Bloco extrator de energia RF posicionado logo abaixo da Torre da Antena.
 * Compatível com cabos universais do Mekanism e qualquer cabo RF (somente extração).
 */
public class BlockAntennaPort extends BlockContainer {

    public BlockAntennaPort() {
        super(Material.iron);
        this.setBlockName("blockAntennaPort");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockAntennaPort");
        this.setCreativeTab(null);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setLightLevel(0.45f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileAntennaPort();
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileAntennaPort) {
                TileAntennaPort port = (TileAntennaPort) te;
                OSUtils.sendChat(player,
                    "\u00a7b[Antena T1] \u00a7fEnergia: \u00a7a" + OSUtils.formatRF(port.getEnergyLong())
                    + " \u00a77/ " + OSUtils.formatRF(port.getCapacityLong())
                    + " \u00a78| \u00a7fRecebendo: \u00a7e+" + OSUtils.formatRF(port.getLastIntakeRFt()) + "/t"
                    + " \u00a77(Conecte cabos RF aqui)");
            }
        }
        return true;
    }
}