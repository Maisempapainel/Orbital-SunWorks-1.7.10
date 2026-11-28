package com.orbitalsunworks.world.orbit;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Bloqueio total da dimensão orbital:
 *  - Impede spawn de mobs
 *  - Impede colocação ou quebra de blocos
 *  - Bloqueia uso/ativação de QUALQUER item na mão (espadas, arcos, cajados mágicos de mods, etc.),
 *    interrompendo a animação imediatamente antes do cast.
 *  - Permite apenas abrir os blocos do Satélite e do Foguete de Navegação sem acionar itens.
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.PlaceEvent event) {
        if (!OrbitDimensionManager.isOrbitWorld(event.world)) {
            return;
        }
        event.setCanceled(true);
        syncInventory(event.player);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!OrbitDimensionManager.isOrbitWorld(event.world)) {
            return;
        }
        if (event.getPlayer() == null || !event.getPlayer().capabilities.isCreativeMode) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!OrbitDimensionManager.isOrbitWorld(event.world) || event.entityPlayer == null) {
            return;
        }
        EntityPlayer p = event.entityPlayer;
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Block target = event.world.getBlock(event.x, event.y, event.z);
            if (!p.isSneaking() && (target == OSBlocks.blockOrbitSatellite || target == OSBlocks.blockNavRocket)) {
                // Abre a GUI do satélite/foguete, mas bloqueia 100% o item da mão
                event.useBlock = Event.Result.ALLOW;
                event.useItem = Event.Result.DENY;
                p.stopUsingItem();
                return;
            }
        }

        // Bloqueia botão direito no ar/bloco com qualquer item (cajados, arcos, espadas, poções, etc.)
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR
                || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            event.useItem = Event.Result.DENY;
            event.useBlock = Event.Result.DENY;
            event.setCanceled(true);
            p.stopUsingItem();
            syncInventory(p);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUseItemStart(PlayerUseItemEvent.Start event) {
        if (event.entityPlayer != null && OrbitDimensionManager.isOrbitWorld(event.entityPlayer.worldObj)) {
            event.setCanceled(true);
            event.entityPlayer.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUseItemTick(PlayerUseItemEvent.Tick event) {
        if (event.entityPlayer != null && OrbitDimensionManager.isOrbitWorld(event.entityPlayer.worldObj)) {
            event.setCanceled(true);
            event.entityPlayer.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteract(EntityInteractEvent event) {
        if (event.entityPlayer != null && OrbitDimensionManager.isOrbitWorld(event.entityPlayer.worldObj)) {
            event.setCanceled(true);
            event.entityPlayer.stopUsingItem();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onArrowLoose(ArrowLooseEvent event) {
        if (event.entityPlayer != null && OrbitDimensionManager.isOrbitWorld(event.entityPlayer.worldObj)) {
            event.setCanceled(true);
        }
    }

    private static void syncInventory(EntityPlayer player) {
        if (player instanceof EntityPlayerMP && !player.worldObj.isRemote) {
            ((EntityPlayerMP) player).inventoryContainer.detectAndSendChanges();
        }
    }
}