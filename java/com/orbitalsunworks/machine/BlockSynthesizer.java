package com.orbitalsunworks.machine;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.handler.GuiHandler;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Máquina Sintetizadora: sintetiza Kevlar a partir de 5.000 mB de Petróleo Refinado.
 * Possui 1 entrada vermelha de energia RF na face traseira (oposta à frente).
 */
public class BlockSynthesizer extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconBackRf;
    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;

    public BlockSynthesizer() {
        super(Material.iron);
        this.setBlockName("blockSynthesizer");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(4.5f);
        this.setResistance(12.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileSynthesizer();
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int dir = MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta = 2;
        if (dir == 1) meta = 5;
        else if (dir == 2) meta = 3;
        else if (dir == 3) meta = 4;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.iconFront = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockSynthesizer_front");
        this.iconBackRf = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockSynthesizer_back_rf");
        this.iconTop = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockSynthesizer_top");
        this.iconSide = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockSynthesizer_side");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int front = (meta >= 2 && meta <= 5) ? meta : 3;
        if (side == 1) {
            return this.iconTop;
        }
        if (side == 0) {
            return this.iconSide;
        }
        if (side == front) {
            return this.iconFront;
        }
        if (side == ForgeDirection.getOrientation(front).getOpposite().ordinal()) {
            return this.iconBackRf;
        }
        return this.iconSide;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player != null) {
            player.openGui(OrbitalSunworks.instance, GuiHandler.ID_SYNTHESIZER, world, x, y, z);
        }
        return true;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileSynthesizer) {
            ((TileSynthesizer) te).dropInventory(world, x, y, z);
        }
        super.breakBlock(world, x, y, z, block, meta);
    }
}