package com.orbitalsunworks.world.orbit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldServer;

import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * Gravidade lunar da órbita + limites seguros do setor orbital (teto superior
 * e bordas laterais para manter o jogador na região do satélite e nave).
 */
public class OrbitGravity {

    private static final double MIN_Y = 31.0;
    private static final double MAX_Y = 118.0;
    private static final double SECTOR_MARGIN = 55.0;

    private final Map<UUID, Long> lastWarnTick = new HashMap<UUID, Long>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) {
            return;
        }
        WorldServer orbit = net.minecraftforge.common.DimensionManager.getWorld(
            OrbitDimensionManager.getDimensionId());
        if (orbit == null) {
            return;
        }

        SatelliteManager manager = SatelliteManager.get(orbit);
        int maxIdx = Math.max(0, manager.getSatellites().size() - 1);
        double minX = 8.0 - SECTOR_MARGIN;
        double maxX = 8.0 + maxIdx * (double) OrbitStructureManager.SPACING + SECTOR_MARGIN;
        double minZ = 8.0 - SECTOR_MARGIN;
        double maxZ = 8.0 + SECTOR_MARGIN;

        float factor = WorldProviderOrbit.GRAVITY_FACTOR;
        long now = orbit.getTotalWorldTime();

        List entities = new ArrayList(orbit.loadedEntityList);
        for (Object o : entities) {
            if (!(o instanceof Entity)) {
                continue;
            }
            Entity e = (Entity) o;
            if (e.isDead) {
                continue;
            }

            boolean flying = false;
            if (e instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP) e;
                flying = p.capabilities.isFlying;
            }

            if (!flying && e.motionY < 0.0) {
                e.motionY *= factor;
                if (e.motionY < -0.10) {
                    e.motionY = -0.10;
                }
            }

            if (e instanceof EntityLivingBase) {
                e.fallDistance = 0f;
            }

            if (e instanceof EntityItem && e.motionY < -0.05) {
                e.motionY = -0.05;
            }

            // Limites superior e laterais para jogadores na órbita
            if (e instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP) e;
                if (p.ridingEntity != null) {
                    continue;
                }
                double nx = p.posX;
                double ny = p.posY;
                double nz = p.posZ;
                boolean hitCeiling = false;
                boolean hitSide = false;

                if (ny < MIN_Y) {
                    ny = MIN_Y;
                    if (p.motionY < 0.0) p.motionY = 0.0;
                } else if (ny > MAX_Y) {
                    ny = MAX_Y;
                    if (p.motionY > 0.0) p.motionY = 0.0;
                    hitCeiling = true;
                }

                if (nx < minX) {
                    nx = minX;
                    if (p.motionX < 0.0) p.motionX = 0.0;
                    hitSide = true;
                } else if (nx > maxX) {
                    nx = maxX;
                    if (p.motionX > 0.0) p.motionX = 0.0;
                    hitSide = true;
                }

                if (nz < minZ) {
                    nz = minZ;
                    if (p.motionZ < 0.0) p.motionZ = 0.0;
                    hitSide = true;
                } else if (nz > maxZ) {
                    nz = maxZ;
                    if (p.motionZ > 0.0) p.motionZ = 0.0;
                    hitSide = true;
                }

                if (hitCeiling || hitSide) {
                    p.playerNetServerHandler.setPlayerLocation(nx, ny, nz, p.rotationYaw, p.rotationPitch);
                    Long prev = this.lastWarnTick.get(p.getUniqueID());
                    if (prev == null || (now - prev.longValue()) >= 60L) {
                        this.lastWarnTick.put(p.getUniqueID(), Long.valueOf(now));
                        OSUtils.sendChat(p, hitCeiling
                            ? "\u00a7eAltitude m\u00e1xima de \u00f3rbita atingida!"
                            : "\u00a7eLimite do setor orbital atingido!");
                    }
                }
            }
        }
    }
}