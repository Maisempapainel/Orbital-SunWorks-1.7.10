package com.orbitalsunworks.satellite;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Tick handler do servidor: roda o desgaste dos satélites 1x por segundo.
 */
public class SatelliteTickHandler {

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.world.isRemote) {
            SatelliteManager.get(event.world).updateWear(event.world);
        }
    }
}
