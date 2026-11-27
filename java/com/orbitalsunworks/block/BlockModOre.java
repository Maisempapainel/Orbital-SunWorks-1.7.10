package com.orbitalsunworks.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Minério genérico do Orbital Sunworks. Cada minério é uma instância desta
 * classe com cor própria. Gera XP ao ser minerado (como ores do vanilla).
 *
 * Camadas de geração definidas no OSWorldGenerator:
 * - Redmeg:        camadas 5-30   (raro, endgame)
 * - Yellomite:     camadas 5-35   (raro)
 * - Cobre:         camadas 40-70  (comum)
 * - Estanho:       camadas 30-60  (comum)
 * - Alumínio:      camadas 20-50  (comum)
 * - Titânio:       camadas 5-25   (raro)
 * - Carvão Energizado: camadas 10-45 (médio)
 */
public class BlockModOre extends Block {

    /** Item que o minério dropa ao ser minerado (com fortune). */
    private final Item dropItem;
    private final int minDrop;
    private final int maxDrop;
    private final int minXp;
    private final int maxXp;

    public BlockModOre(String name, Item drop, int minDrop, int maxDrop, int minXp, int maxXp) {
        super(Material.rock);
        this.setBlockName(name);
        this.setBlockTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + name);
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setHardness(3.0f);
        this.setResistance(5.0f);
        this.setStepSound(soundTypeStone);
        this.setHarvestLevel("pickaxe", 2); // requer picareta de ferro+
        this.dropItem = drop;
        this.minDrop = minDrop;
        this.maxDrop = maxDrop;
        this.minXp = minXp;
        this.maxXp = maxXp;
    }

    @Override
    public Item getItemDropped(int metadata, Random random, int fortune) {
        return this.dropItem;
    }

    @Override
    public int quantityDropped(Random random) {
        return minDrop + random.nextInt(maxDrop - minDrop + 1);
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random random) {
        int base = this.quantityDropped(random);
        if (fortune > 0) {
            int bonus = random.nextInt(fortune + 2) - 1;
            if (bonus < 0) {
                bonus = 0;
            }
            return base * (bonus + 1);
        }
        return base;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, int x, int y, int z, int meta, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, x, y, z, meta, chance, fortune);
        // XP ao quebrar (como ores do vanilla)
        int xp = MathHelper.getRandomIntegerInRange(world.rand, this.minXp, this.maxXp);
        if (xp > 0) {
            this.dropXpOnBlockBreak(world, x, y, z, xp);
        }
    }

    @Override
    public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
        return MathHelper.getRandomIntegerInRange(new Random(), this.minXp, this.maxXp);
    }
}
