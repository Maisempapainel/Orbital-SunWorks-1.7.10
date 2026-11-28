package com.orbitalsunworks.world.orbit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.orbitalsunworks.handler.SpaceSuitHandler;
import com.orbitalsunworks.rocket.EntityNavRocket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Fly na dimensão orbital (liberado somente com Traje Espacial completo)
 * e trava anti-Shift no foguete de navegação (servidor).
 */
public class OrbitFlyHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || player.worldObj == null || player.worldObj.isRemote) {
            return;
        }
        if (event.phase == TickEvent.Phase.START
            && player.ridingEntity instanceof EntityNavRocket) {
            player.setSneaking(false);
        }

        if (event.phase == TickEvent.Phase.END && player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            if (mp.capabilities.isCreativeMode) {
                return;
            }
            if (OrbitDimensionManager.isOrbitWorld(mp.worldObj)) {
                boolean suited = SpaceSuitHandler.hasFullSuit(mp);
                if (suited && !mp.capabilities.allowFlying) {
                    mp.capabilities.allowFlying = true;
                    mp.sendPlayerAbilities();
                } else if (!suited && (mp.capabilities.allowFlying || mp.capabilities.isFlying)) {
                    mp.capabilities.allowFlying = false;
                    mp.capabilities.isFlying = false;
                    mp.sendPlayerAbilities();
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        EntityPlayer player = event.player;
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;
        if (mp.capabilities.isCreativeMode) {
            return;
        }
        boolean nowInOrbit = OrbitDimensionManager.isOrbitWorld(mp.worldObj);

        if (nowInOrbit && SpaceSuitHandler.hasFullSuit(mp)) {
            mp.capabilities.allowFlying = true;
            mp.capabilities.isFlying = true;
            mp.sendPlayerAbilities();
        } else {
            mp.capabilities.allowFlying = false;
            mp.capabilities.isFlying = false;
            mp.sendPlayerAbilities();
        }
    }
}