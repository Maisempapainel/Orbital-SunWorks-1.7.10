package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Kit de Reparos para satélites em órbita (+10% de vida útil por uso).
 */
public class ItemRepairKit extends Item {

    public ItemRepairKit() {
        this.setUnlocalizedName("repairKit");
        this.setTextureName(Reference.RESOURCE_PREFIX + "repairKit");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setMaxStackSize(16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add("\u00a7aRestaura +10% de vida \u00fatil do sat\u00e9lite.");
        tooltip.add("\u00a77Use no slot de reparo do sat\u00e9lite em \u00f3rbita");
        tooltip.add("\u00a77para reconstruir os pain\u00e9is danificados.");
    }
}