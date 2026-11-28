package com.orbitalsunworks.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.satellite.TileControlStation;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.world.ControlStationBuilder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Núcleo central visível da Estação de Controle.
 * Controla a auto-construção de 1 minuto e desmonta toda a estrutura ao ser quebrado.
 */
public class BlockControlStation extends BlockContainer {

    private boolean dismantling = false;

    public BlockControlStation() {
        super(Material.iron);
        this.setBlockName("blockControlStation");
        this.setBlockTextureName(Reference.RESOURCE_PREFIX + "blockControlComputer");
        this.setCreativeTab(null);
        this.setHardness(2.5f);
        this.setResistance(6000000.0f);
        this.setLightLevel(0.85f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileControlStation();
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
            if (te instanceof TileControlStation) {
                TileControlStation st = (TileControlStation) te;
                if (!st.isCompleted()) {
                    int pct = Math.round(st.getProgress() * 100f);
                    OSUtils.sendChat(player,
                        "\u00a7b[Orbital Sunworks] \u00a7fConstruindo Esta\u00e7\u00e3o de Controle: \u00a7e" + pct + "%");
                } else {
                    OSUtils.sendChat(player,
                        "\u00a7b[Orbital Sunworks] \u00a7aEsta\u00e7\u00e3o operacional! \u00a77Quebre este bloco central se desejar desmontar a esta\u00e7\u00e3o inteira.");
                }
            }
        }
        return true;
    }

    @Override
    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (!world.isRemote && !this.dismantling) {
            boolean creative = player != null && player.capabilities.isCreativeMode;
            this.performDismantle(world, x, y, z, !creative);
            if (player != null) {
                OSUtils.sendChat(player,
                    "\u00a7b[Orbital Sunworks] \u00a7eEsta\u00e7\u00e3o de Controle desmontada.");
            }
            return true;
        }
        return super.removedByPlayer(world, player, x, y, z, willHarvest);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!world.isRemote && !this.dismantling) {
            this.performDismantle(world, x, y, z, false);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    private void performDismantle(World world, int x, int y, int z, boolean dropItem) {
        this.dismantling = true;
        try {
            SatelliteManager.get(world).unregisterControlStationAt(world.provider.dimensionId, x, y, z);
            ControlStationBuilder.dismantle(world, x, y - 1, z);
            if (dropItem) {
                EntityItem drop = new EntityItem(world, x + 0.5, y + 0.5, z + 0.5,
                    new ItemStack(OSItems.itemControlStation));
                world.spawnEntityInWorld(drop);
            }
        } finally {
            this.dismantling = false;
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        return new ArrayList<ItemStack>();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(OSItems.itemControlStation);
    }
}