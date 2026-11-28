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
import com.orbitalsunworks.satellite.TileAntennaTower;
import com.orbitalsunworks.util.OSUtils;

/**
 * Torre da Antena Receptora Orbital posicionada sobre o TileAntennaPort.
 * Renderiza uma torre metálica alta com prato parabólico animado.
 */
public class BlockAntennaTower extends BlockContainer {

    public BlockAntennaTower() {
        super(Material.iron);
        this.setBlockName("blockAntennaTower");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockAntennaTower");
        this.setCreativeTab(null);
        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
        this.setLightLevel(0.6f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileAntennaTower();
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
        if (!world.isRemote) {
            TileEntity below = world.getTileEntity(x, y - 1, z);
            if (below instanceof TileAntennaPort) {
                TileAntennaPort port = (TileAntennaPort) below;
                OSUtils.sendChat(player,
                    "\u00a7b[Antena Receptora T1] \u00a7fEnergia: \u00a7a" + OSUtils.formatRF(port.getEnergyLong())
                    + " \u00a77/ " + OSUtils.formatRF(port.getCapacityLong())
                    + " \u00a78| \u00a7e+" + OSUtils.formatRF(port.getLastIntakeRFt()) + "/t"
                    + " \u00a77(Extraia pelo bloco logo abaixo da torre)");
            }
        }
        return true;
    }
}