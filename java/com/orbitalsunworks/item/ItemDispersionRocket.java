package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.block.BlockDispersionRocket;
import com.orbitalsunworks.block.BlockLaunchPlatform;
import com.orbitalsunworks.util.OSUtils;

public class ItemDispersionRocket extends Item {

    public ItemDispersionRocket() {
        this.setUnlocalizedName("dispersionRocket");
        this.setTextureName(Reference.RESOURCE_PREFIX + "dispersionRocket");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setMaxStackSize(1);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add("\u00a77Foguete interceptor orbital de meteoros.");
        tooltip.add("\u00a7bClique na Plataforma 7x7 para posicionar.");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
            int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        int[] center = BlockLaunchPlatform.findCenter(world, x, y, z);
        if (center == null) {
            if (!world.isRemote) {
                OSUtils.sendChat(player, "\u00a7cPosicione sobre uma Plataforma de Lan\u00e7amento 7x7 completa!");
            }
            return false;
        }
        if (world.isRemote) {
            return true;
        }
        return tryPlaceOnCenter(world, center[0], center[1], center[2], player, stack);
    }

    public static boolean tryPlaceOnCenter(World world, int cx, int cy, int cz,
            EntityPlayer player, ItemStack stack) {
        int ry = cy + 1;
        for (int i = 0; i < BlockDispersionRocket.HEIGHT; i++) {
            if (!world.isAirBlock(cx, ry + i, cz)) {
                OSUtils.sendChat(player,
                    "\u00a7cO espa\u00e7o acima do centro da plataforma precisa estar livre ("
                    + BlockDispersionRocket.HEIGHT + " blocos)!");
                return true;
            }
        }
        for (int i = 0; i < BlockDispersionRocket.HEIGHT; i++) {
            world.setBlock(cx, ry + i, cz, OSBlocks.blockDispersionRocket, i, 3);
        }
        world.playSoundAtEntity(player, "random.anvil_land", 0.4f, 1.4f);
        if (!player.capabilities.isCreativeMode && stack != null) {
            stack.stackSize--;
            if (stack.stackSize <= 0) {
                player.setCurrentItemOrArmor(0, null);
            }
        }
        OSUtils.sendChat(player, "\u00a7aFoguete de Dispers\u00e3o posicionado! Clique nele para lan\u00e7ar.");
        return true;
    }
}