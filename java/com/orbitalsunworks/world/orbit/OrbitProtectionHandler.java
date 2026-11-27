package com.orbitalsunworks.world.orbit;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemRedstone;
import net.minecraft.item.ItemReed;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemSign;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Proteção total da dimensão orbital:
 * 1) Impede o spawn de qualquer mob (hostil, pacífico, ovo de spawn, spawner, etc.).
 * 2) Impede a colocação de qualquer bloco por jogadores, devolvendo o item ao inventário.
 */
public class OrbitProtectionHandler {

    @SubscribeEvent
    public void onCheckSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (OrbitDimensionManager.isOrbitWorld(event.world)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onSpecialSpawn(LivingSpawnEvent.SpecialSpawn event) {
        if (OrbitDimensionManager.isOrbitWorld(event.world)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!OrbitDimensionManager.isOrbitWorld(event.world)) {
            return;
        }
        if (event.entity instanceof EntityLivingBase && !(event.entity instanceof EntityPlayer)) {
            event.setCanceled(true);
            if (!event.world.isRemote) {
                event.entity.setDead();
            }
        }
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!OrbitDimensionManager.isOrbitWorld(event.world)) {
            return;
        }
        event.setCanceled(true);
        if (!event.world.isRemote && event.player != null) {
            if (event.player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) event.player).inventoryContainer.detectAndSendChanges();
            }
            OSUtils.sendChat(event.player, "\u00a7cN\u00e3o \u00e9 poss\u00edvel colocar blocos ou construir na \u00f3rbita!");
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!OrbitDimensionManager.isOrbitWorld(event.world) || event.entityPlayer == null) {
            return;
        }
        ItemStack held = event.entityPlayer.getHeldItem();

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block target = event.world.getBlock(event.x, event.y, event.z);
            if (!event.entityPlayer.isSneaking()
                && (target == OSBlocks.blockOrbitSatellite || target == OSBlocks.blockNavRocket)) {
                event.useBlock = Event.Result.ALLOW;
                event.useItem = Event.Result.DENY;
                return;
            }
            if (isBlockedOrbitItem(held)) {
                event.useItem = Event.Result.DENY;
                if (!event.world.isRemote) {
                    if (event.entityPlayer instanceof EntityPlayerMP) {
                        ((EntityPlayerMP) event.entityPlayer).inventoryContainer.detectAndSendChanges();
                    }
                    OSUtils.sendChat(event.entityPlayer, "\u00a7cN\u00e3o \u00e9 poss\u00edvel colocar blocos ou construir na \u00f3rbita!");
                }
            }
        } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (held != null && (held.getItem() instanceof ItemMonsterPlacer
                || (held.getItem() instanceof ItemBucket && held.getItem() != Items.bucket))) {
                event.setCanceled(true);
                if (!event.world.isRemote && event.entityPlayer instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) event.entityPlayer).inventoryContainer.detectAndSendChanges();
                }
            }
        }
    }

    private static boolean isBlockedOrbitItem(ItemStack stack) {
        if (stack == null) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemBlock
            || item instanceof ItemReed
            || item instanceof ItemDoor
            || item instanceof ItemSign
            || item instanceof ItemBed
            || item instanceof ItemRedstone
            || item instanceof ItemFlintAndSteel
            || item instanceof ItemMonsterPlacer
            || item instanceof ItemSeeds
            || item instanceof ItemSeedFood
            || item instanceof ItemSkull
            || item instanceof IPlantable) {
            return true;
        }
        return item instanceof ItemBucket && item != Items.bucket;
    }
}