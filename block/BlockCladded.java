package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Bloco Revestido - o esqueleto estrutural do satélite. Feito com liga de
 * alumínio, fibra de carbono e liga de titânio na Estação de Crafting.
 */
public class BlockCladded extends Block {

    public BlockCladded() {
        super(Material.iron);
        this.setBlockName("blockCladded");
        this.setBlockTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockCladded");
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setHardness(6.0f);
        this.setResistance(12.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 2);
    }
}
