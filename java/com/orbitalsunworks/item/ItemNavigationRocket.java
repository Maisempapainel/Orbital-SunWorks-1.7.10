package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemNavigationRocket extends Item {

    public ItemNavigationRocket() {
        this.setUnlocalizedName("navRocket");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setTextureName(Reference.RESOURCE_PREFIX + "navRocket");
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote) {
            OSUtils.sendChat(player,
                "\u00a7ePosicione este foguete clicando com o bot\u00e3o direito no CENTRO de uma plataforma de lan\u00e7amento 7x7!");
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add("\u00a77Visite seus sat\u00e9lites em \u00f3rbita");
        tooltip.add("\u00a77Posicione no centro de uma plataforma 7x7");
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("HomeX")) {
            tooltip.add("\u00a78\u00daltima base: \u00a77" + (int) stack.getTagCompound().getDouble("HomeX")
                + ", " + (int) stack.getTagCompound().getDouble("HomeY")
                + ", " + (int) stack.getTagCompound().getDouble("HomeZ"));
        }
    }
}