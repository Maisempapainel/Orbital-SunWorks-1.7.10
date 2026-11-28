package com.orbitalsunworks.satellite;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.world.World;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * Tick handler único do servidor: executa desgaste e eventos 1x por tick
 * (independente de quantas dimensões estiverem carregadas) e sincroniza o
 * estado ao jogador no login.
 */
public class SatelliteTickHandler {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) {
            return;
        }
        World overworld = DimensionManager.getWorld(0);
        if (overworld == null) {
            return;
        }
        SatelliteManager.get(overworld).updateServerTick(MinecraftServer.getServer());
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP mp = (EntityPlayerMP) event.player;
        SatelliteManager mgr = SatelliteManager.get(mp.worldObj);
        SatelliteManager.MeteorEvent ev = mgr.getMeteorEventFor(mp.getUniqueID(), mp.getCommandSenderName());
        mgr.syncMeteorToPlayer(mp, ev, false);
    }
}