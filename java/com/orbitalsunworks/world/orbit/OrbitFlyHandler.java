package com.orbitalsunworks.world.orbit;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;

/**
 * Fly do lado SERVIDOR (v0.6.0): na dimensão orbital o fly fica ativo
 * (é o único jeito de se locomover até a sonda); ao SAIR da órbita o
 * estado volta ao normal (survival: fly off; criativo: como estava).
 *
 * O cliente espelha no ClientTravelHandler (mesma regra) para o
 * movimento responder imediatamente; o servidor é a autoridade.
 */
public class OrbitFlyHandler {

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        EntityPlayer player = event.player;
        if (!(player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP mp = (EntityPlayerMP) player;
        boolean nowInOrbit = OrbitDimensionManager.isOrbitWorld(mp.worldObj);

        if (nowInOrbit) {
            // Chegou na órbita: fly ON (não mexe em criativo — já voa).
            if (!mp.capabilities.isCreativeMode) {
                mp.capabilities.allowFlying = true;
                mp.capabilities.isFlying = true;
                mp.sendPlayerAbilities();
            }
        } else {
            // Saiu da órbita: fly OFF (a não ser criativo).
            if (!mp.capabilities.isCreativeMode) {
                mp.capabilities.allowFlying = false;
                mp.capabilities.isFlying = false;
                mp.sendPlayerAbilities();
            }
        }
    }
}
