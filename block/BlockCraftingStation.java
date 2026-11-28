package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.craftingstation.TileCraftingStation;

/**
 * Estação de Crafting Orbital - a "crafting table tecnológica" do mod.
 *
 * Diferenças para a mesa vanilla:
 * - Grade de 15 slots (3 linhas x 5 colunas) em vez de 9;
 * - Receitas grandes (foguetes) usam as duas colunas extras;
 * - Receitas simples continuam funcionando nos 9 slots centrais;
 * - GUI com HUD tecnológico próprio.
 */
public class BlockCraftingStation extends BlockContainer {

    public BlockCraftingStation() {
        super(Material.iron);
        this.setBlockName("blockCraftingStation");
        this.setBlockTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockCraftingStation");
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setHardness(5.0f);
        this.setResistance(10.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileCraftingStation();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(com.orbitalsunworks.OrbitalSunworks.instance, com.orbitalsunworks.handler.GuiHandler.ID_CRAFTING_STATION, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        // Dropa o conteúdo dos slots antes de remover o tile.
        TileCraftingStation tile = (TileCraftingStation) world.getTileEntity(x, y, z);
        if (tile != null) {
            tile.dropInventory(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}
