package com.orbitalsunworks.machine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;

/**
 * Núcleo central do Extrator de Petróleo (Multiblock 3x3 tipo Gangorra).
 * Monte ao redor no mesmo nível: 7x Bloco de Carapaça + 1x Entrada de Energia RF
 * em uma das 4 faces cardeais (todo o 3x3 deve estar dentro da mesma chunk com jazida).
 */
public class BlockOilExtractor extends BlockContainer {

    public BlockOilExtractor() {
        super(Material.iron);
        this.setBlockName("blockOilExtractor");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockOilExtractor");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(5.0f);
        this.setResistance(15.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileOilExtractor();
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
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote || player == null) {
            return true;
        }
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileOilExtractor) {
            TileOilExtractor ext = (TileOilExtractor) te;
            ext.revalidateNow();

            // Clique com balde vazio enche diretamente se houver >= 1000 mB
            ItemStack held = player.getCurrentEquippedItem();
            if (held != null && held.getItem() == Items.bucket && ext.tryFillHandBucket()) {
                if (!player.capabilities.isCreativeMode) {
                    held.stackSize--;
                    if (held.stackSize <= 0) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                }
                ItemStack oilBucket = new ItemStack(OSItems.bucketCrudeOil);
                if (!player.inventory.addItemStackToInventory(oilBucket)) {
                    world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY + 0.5, player.posZ, oilBucket));
                }
                player.inventoryContainer.detectAndSendChanges();
                return true;
            }

            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_OIL_EXTRACTOR, world, x, y, z);
        }
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileOilExtractor) {
                ((TileOilExtractor) te).revalidateNow();
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileOilExtractor) {
            ((TileOilExtractor) te).dropInventory(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}