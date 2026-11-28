package com.orbitalsunworks.machine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.util.OSUtils;

/**
 * Bloco de Entrada de Energia RF (conector vermelho).
 * Aceita cabos RF de qualquer mod e alimenta o Extrator de Petróleo multiblock
 * (ou uma máquina adjacente caso conectado diretamente).
 */
public class BlockEnergyInput extends BlockContainer {

    public BlockEnergyInput() {
        super(Material.iron);
        this.setBlockName("blockEnergyInput");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockEnergyInput");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(4.5f);
        this.setResistance(12.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEnergyInput();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player != null) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEnergyInput) {
                TileEnergyInput input = (TileEnergyInput) te;
                OSUtils.sendChat(player,
                    "\u00a7c[Entrada RF] \u00a7fEnergia armazenada: \u00a7e"
                    + OSUtils.formatRF(input.getEnergyStored(null)) + " / "
                    + OSUtils.formatRF(input.getMaxEnergyStored(null)) + " RF");
            }
        }
        return true;
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        notifyNearbyExtractors(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        notifyNearbyExtractors(world, x, y, z);
    }

    public static void notifyNearbyExtractors(World world, int x, int y, int z) {
        if (world == null || world.isRemote) {
            return;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                TileEntity te = world.getTileEntity(x + dx, y, z + dz);
                if (te instanceof TileOilExtractor) {
                    ((TileOilExtractor) te).revalidateNow();
                }
            }
        }
    }
}