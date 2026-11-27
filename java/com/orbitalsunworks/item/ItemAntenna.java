package com.orbitalsunworks.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.Item;

/**
 * Antena de Transmissão - existem 5 tiers. Tiers maiores transmitem mais
 * energia para a Terra (implementação futura) e são pré-requisito para
 * foguetes de tier maior.
 */
public class ItemAntenna extends Item {

    private final int tier;

    public ItemAntenna(int tier) {
        this.tier = tier;
        this.setUnlocalizedName("antennaT" + tier);
        this.setTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "antennaT" + tier);
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setMaxStackSize(16);
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(StatCollector.translateToLocal("tooltip.orbitalsunworks.antenna.tier") + " " + tier);
        tooltip.add(StatCollector.translateToLocal("tooltip.orbitalsunworks.antenna.desc"));
    }
}
