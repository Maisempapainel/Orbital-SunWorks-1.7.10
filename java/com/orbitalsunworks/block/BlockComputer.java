package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

/**
 * Computador Central - núcleo da futura Estação Solar Orbital. Nesta versão
 * ainda não tem GUI (a estação vem na próxima etapa do mod); ele já existe
 * porque entra na linha de produção (receita na Estação de Crafting).
 */
public class BlockComputer extends Block {

    public BlockComputer() {
        super(Material.iron);
        this.setBlockName("blockComputer");
        this.setBlockTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockComputer");
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setHardness(4.0f);
        this.setResistance(8.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }
}
