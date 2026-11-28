package com.orbitalsunworks.client.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import com.orbitalsunworks.network.MessageMeteorSync;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Estado sincronizado da chuva de meteoros no cliente.
 * Mantém campos públicos + getters para compatibilidade com todas as GUIs.
 */
@SideOnly(Side.CLIENT)
public class ClientMeteorHandler {

    public static boolean active = false;
    public static int remainingTicks = 0;
    public static int totalTicks = 0;
    public static List<String> targetSatIds = new ArrayList<String>();
    public static List<String> targetSatNames = new ArrayList<String>();
    public static long lastImpactTimestampMs = 0L;

    public static void update(boolean isActive, int remTicks, int totTicks,
            List<String> ids, List<String> names, boolean impactFlash) {
        active = isActive;
        remainingTicks = Math.max(0, remTicks);
        totalTicks = Math.max(0, totTicks);
        targetSatIds = ids != null ? new ArrayList<String>(ids) : new ArrayList<String>();
        targetSatNames = names != null ? new ArrayList<String>(names) : new ArrayList<String>();
        if (impactFlash) {
            lastImpactTimestampMs = System.currentTimeMillis();
        }
    }

    public static void onMeteorSync(MessageMeteorSync msg) {
        if (msg == null) {
            return;
        }
        update(msg.active, msg.remainingTicks, msg.totalTicks,
            msg.targetIds, msg.targetNames, msg.impactFlash);
    }

    public static boolean isActive() {
        return active;
    }

    public static int getRemainingTicks() {
        return remainingTicks;
    }

    public static int getTotalTicks() {
        return totalTicks;
    }

    public static List<String> getTargetIds() {
        return targetSatIds;
    }

    public static List<String> getTargetNames() {
        return targetSatNames;
    }

    public static long getLastImpactTimestampMs() {
        return lastImpactTimestampMs;
    }

    public static boolean isTargetString(String id) {
        return id != null && targetSatIds.contains(id);
    }

    public static String formatCountdown() {
        int totalSec = Math.max(0, remainingTicks / 20);
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return String.format("%02d:%02d", min, sec);
    }

    public static String getFormattedRemaining() {
        return formatCountdown();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null) {
            active = false;
            remainingTicks = 0;
            totalTicks = 0;
            targetSatIds.clear();
            targetSatNames.clear();
            return;
        }
        if (active && remainingTicks > 0 && !mc.isGamePaused()) {
            remainingTicks--;
        }
    }
}