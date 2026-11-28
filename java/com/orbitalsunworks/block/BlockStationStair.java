package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

import com.orbitalsunworks.OSCreativeTab;

public class BlockStationStair extends BlockStairs {

    public BlockStationStair(Block baseBlock, int baseMeta) {
        super(baseBlock, baseMeta);
        this.setBlockName("blockStationStair");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setHardness(4.0f);
        this.setResistance(18.0f);
        this.setStepSound(soundTypeMetal);
        this.useNeighborBrightness = true;
    }
}