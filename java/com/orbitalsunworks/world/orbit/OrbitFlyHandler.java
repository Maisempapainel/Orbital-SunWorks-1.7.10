package com.orbitalsunworks.world.orbit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import com.orbitalsunworks.rocket.EntityNavRocket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Fly na dimensão orbital e trava anti-Shift no foguete de navegação (servidor).
 */
public class OrbitFlyHandler {

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (player == null || player.worldObj == null || player.worldObj.isRemote) {
            return;
        }
        // Antes do EntityPlayer.updateRidden() rodar: zera sneak enquanto estiver
        // viajando na EntityNavRocket para o Shift nunca desmontar.
        if (event.phase == TickEvent.Phase.START
            && player.ridingEntity instanceof EntityNavRocket) {
            player.setSneaking(false);
        }
        // Garante fly ativo se o jogador estiver na órbita (ex.: após relogar).
        if (event.phase == TickEvent.Phase.END && player instanceof EntityPlayerMP) {
            EntityPlayerMP mp = (EntityPlayerMP) player;
            if (OrbitDimensionManager.isOrbitWorld(mp.worldObj)
                && !mp.capabilities.isCreativeMode
                && !mp.capabilities.allowFlying) {
                mp.capabilities.allowFlying = true;
                mp.capabilities.isFlying = true;
                mp.sendPlayerAbilities();
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
        boolean nowInOrbit = OrbitDimensionManager.isOrbitWorld(mp.worldObj);

        if (nowInOrbit) {
            if (!mp.capabilities.isCreativeMode) {
                mp.capabilities.allowFlying = true;
                mp.capabilities.isFlying = true;
                mp.sendPlayerAbilities();
            }
        } else {
            if (!mp.capabilities.isCreativeMode) {
                mp.capabilities.allowFlying = false;
                mp.capabilities.isFlying = false;
                mp.sendPlayerAbilities();
            }
        }
    }
}