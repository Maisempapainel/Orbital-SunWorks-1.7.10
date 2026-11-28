package com.orbitalsunworks.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bloco arquitetônico multifuncional da Estação de Controle Orbital (8 variantes):
 *  0 = HULL   (Chassi blindado cinza-aço)
 *  1 = WALL   (Painel metálico tecnológico)
 *  2 = ACCENT (Painel ciano high-tech)
 *  3 = FLOOR  (Piso metálico com juntas)
 *  4 = GLASS  (Vidro blindado translúcido)
 *  5 = PILLAR (Pilar estrutural reforçado)
 *  6 = ROOF   (Cobertura aerodinâmica)
 *  7 = TRIM   (Friso luminoso azulado)
 */
public class BlockStationPart extends Block {

    public static final int HULL = 0;
    public static final int WALL = 1;
    public static final int ACCENT = 2;
    public static final int FLOOR = 3;
    public static final int GLASS = 4;
    public static final int PILLAR = 5;
    public static final int ROOF = 6;
    public static final int TRIM = 7;

    @SideOnly(Side.CLIENT)
    private IIcon[] icons;

    public BlockStationPart() {
        super(Material.iron);
        this.setBlockName("stationPart");
        this.setHardness(4.0f);
        this.setResistance(18.0f);
        this.setStepSound(soundTypeMetal);
        this.setCreativeTab(OSCreativeTab.INSTANCE);
    }

    @Override
    public int damageDropped(int meta) {
        return meta & 7;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z) & 7;
        if (meta == TRIM) {
            return 10;
        }
        if (meta == ACCENT) {
            return 4;
        }
        return 0;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInPass(int pass) {
        return pass == 0 || pass == 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        Block neighbor = world.getBlock(x, y, z);
        if (neighbor == this) {
            int myMeta = 0;
            int nMeta = world.getBlockMetadata(x, y, z) & 7;
            if (nMeta == GLASS) {
                return false;
            }
            return myMeta != GLASS;
        }
        return super.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.icons = new IIcon[8];
        for (int i = 0; i < 8; i++) {
            this.icons[i] = reg.registerIcon(Reference.RESOURCE_PREFIX + "blockStationPart_" + i);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (this.icons == null) {
            return null;
        }
        int idx = meta & 7;
        return this.icons[idx];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < 8; i++) {
            list.add(new ItemStack(item, 1, i));
        }
    }
}