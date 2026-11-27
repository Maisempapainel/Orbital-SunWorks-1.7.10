package com.orbitalsunworks.world.orbit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Física OBB sincronizada (cliente + servidor) para o satélite giratório na órbita:
 *  - Bloqueia passagem por barramento central, asas solares giratórias e antena/prato;
 *  - Transporta suavemente o jogador que estiver apoiado em cima dos painéis/barramento;
 *  - Empurra suavemente o jogador para fora caso seja atingido pela varredura da asa.
 */
public class SatelliteCollisionPhysics {

    /** Velocidade angular compartilhada com o TESR (graus/tick). */
    public static final float SPIN_RATE = 0.08f;
    /** Escala do ModelSatellite na órbita. */
    public static final float SAT_SCALE = 2.2f;

    private static final double PLAYER_HALF_W = 0.34;
    private static final double PLAYER_HEIGHT = 1.80;

    /** Caixas sólidas no espaço local do satélite (já em blocos do mundo). */
    private static final double[][] LOCAL_BOXES = {
        // Barramento central
        { -0.49 * SAT_SCALE, 0.49 * SAT_SCALE, -0.20 * SAT_SCALE, 1.24 * SAT_SCALE, -0.49 * SAT_SCALE, 0.49 * SAT_SCALE },
        // Asa solar esquerda (-X)
        { -3.32 * SAT_SCALE, -0.46 * SAT_SCALE, 0.52 * SAT_SCALE, 0.70 * SAT_SCALE, -0.27 * SAT_SCALE, 0.27 * SAT_SCALE },
        // Asa solar direita (+X)
        { 0.46 * SAT_SCALE, 3.32 * SAT_SCALE, 0.52 * SAT_SCALE, 0.70 * SAT_SCALE, -0.27 * SAT_SCALE, 0.27 * SAT_SCALE },
        // Mastro e prato parabólico superior
        { -0.64 * SAT_SCALE, 0.64 * SAT_SCALE, 1.22 * SAT_SCALE, 2.32 * SAT_SCALE, -0.28 * SAT_SCALE, 0.98 * SAT_SCALE }
    };

    public static float spinDegrees(World world, float partialTicks) {
        if (world == null) {
            return 0f;
        }
        return (float) (((world.getTotalWorldTime() + partialTicks) * SPIN_RATE) % 360.0);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        EntityPlayer player = event.player;
        if (player == null || player.worldObj == null || player.isDead || player.ridingEntity != null) {
            return;
        }
        World world = player.worldObj;
        if (!OrbitDimensionManager.isOrbitWorld(world)) {
            return;
        }

        List tiles = new ArrayList(world.loadedTileEntityList);
        for (Object obj : tiles) {
            if (!(obj instanceof TileOrbitSatellite)) {
                continue;
            }
            TileOrbitSatellite sat = (TileOrbitSatellite) obj;
            if (world.getBlockMetadata(sat.xCoord, sat.yCoord, sat.zCoord) != 0) {
                continue;
            }

            double cx = sat.xCoord + 0.5;
            double cy = sat.yCoord - 0.6;
            double cz = sat.zCoord + 0.5;

            double dx = player.posX - cx;
            double dz = player.posZ - cz;
            if (dx * dx + dz * dz > 144.0) {
                continue;
            }

            resolveAgainstSatellite(player, world, cx, cy, cz);
        }
    }

    private static void resolveAgainstSatellite(EntityPlayer player, World world,
            double cx, double cy, double cz) {
        long time = world.getTotalWorldTime();
        double theta = Math.toRadians((time * SPIN_RATE) % 360.0);
        double prevTheta = Math.toRadians(((time - 1L) * SPIN_RATE) % 360.0);
        double dTheta = theta - prevTheta;
        while (dTheta > Math.PI) dTheta -= Math.PI * 2.0;
        while (dTheta < -Math.PI) dTheta += Math.PI * 2.0;

        double feetOffset = player.posY - player.boundingBox.minY;
        double feetY = player.boundingBox.minY;
        double px = player.posX;
        double pz = player.posZ;
        double playerH = Math.max(1.5, player.boundingBox.maxY - player.boundingBox.minY);
        if (Double.isNaN(playerH) || playerH <= 0.0) {
            playerH = PLAYER_HEIGHT;
        }

        // 1) Transporte (carry): se apoiado no topo de alguma caixa no tick anterior, gira junto
        double cosPrev = Math.cos(prevTheta);
        double sinPrev = Math.sin(prevTheta);
        double dx = px - cx;
        double dz = pz - cz;
        double lxPrev = dx * cosPrev - dz * sinPrev;
        double lzPrev = dx * sinPrev + dz * cosPrev;

        boolean carried = false;
        double carriedTopY = feetY;
        for (double[] box : LOCAL_BOXES) {
            double topY = cy + box[3];
            if (Math.abs(feetY - topY) <= 0.16
                    && player.motionY <= 0.08
                    && lxPrev + PLAYER_HALF_W >= box[0] && lxPrev - PLAYER_HALF_W <= box[1]
                    && lzPrev + PLAYER_HALF_W >= box[4] && lzPrev - PLAYER_HALF_W <= box[5]) {
                carried = true;
                carriedTopY = topY;
                break;
            }
        }

        if (carried) {
            double cd = Math.cos(dTheta);
            double sd = Math.sin(dTheta);
            double newDx = dx * cd + dz * sd;
            double newDz = -dx * sd + dz * cd;
            px = cx + newDx;
            pz = cz + newDz;
            if (!player.capabilities.isFlying) {
                feetY = carriedTopY;
                player.motionY = Math.max(0.0, player.motionY);
                player.onGround = true;
                player.fallDistance = 0.0f;
            }
        }

        // 2) Resolução OBB sólida + empurrão suave pela varredura da asa
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);
        boolean moved = carried;

        for (double[] box : LOCAL_BOXES) {
            double ldx = px - cx;
            double ldz = pz - cz;
            double lx = ldx * cosT - ldz * sinT;
            double lz = ldx * sinT + ldz * cosT;

            double bMinY = cy + box[2];
            double bMaxY = cy + box[3];
            double ox = Math.min(lx + PLAYER_HALF_W, box[1]) - Math.max(lx - PLAYER_HALF_W, box[0]);
            double oy = Math.min(feetY + playerH, bMaxY) - Math.max(feetY, bMinY);
            double oz = Math.min(lz + PLAYER_HALF_W, box[5]) - Math.max(lz - PLAYER_HALF_W, box[4]);

            if (ox <= 0.0 || oy <= 0.0 || oz <= 0.0) {
                continue;
            }

            moved = true;
            double fromTop = bMaxY - feetY;
            double fromBot = (feetY + playerH) - bMinY;

            if (fromTop > 0.0 && fromTop <= 0.42 && oy <= Math.min(ox, oz) + 0.16) {
                feetY = bMaxY;
                if (player.motionY < 0.0) {
                    player.motionY = 0.0;
                }
                if (!player.capabilities.isFlying) {
                    player.onGround = true;
                }
                player.fallDistance = 0.0f;
            } else if (fromBot > 0.0 && fromBot <= 0.42 && oy <= Math.min(ox, oz)) {
                feetY = bMinY - playerH;
                if (player.motionY > 0.0) {
                    player.motionY = 0.0;
                }
            } else {
                // Empurrão horizontal no eixo de menor penetração local
                double pushLX = 0.0;
                double pushLZ = 0.0;
                double centerBoxX = (box[0] + box[1]) * 0.5;
                double centerBoxZ = (box[4] + box[5]) * 0.5;
                if (ox < oz) {
                    pushLX = (lx >= centerBoxX) ? (ox + 0.015) : -(ox + 0.015);
                } else {
                    pushLZ = (lz >= centerBoxZ) ? (oz + 0.015) : -(oz + 0.015);
                }

                // Converte vetor de empurrão do local -> mundo (rotação por +theta)
                double pushWX = pushLX * cosT + pushLZ * sinT;
                double pushWZ = -pushLX * sinT + pushLZ * cosT;
                px += pushWX;
                pz += pushWZ;

                // Zera componente de velocidade voltada para dentro do painel/corpo
                double len = Math.sqrt(pushWX * pushWX + pushWZ * pushWZ);
                if (len > 1.0e-5) {
                    double nx = pushWX / len;
                    double nz = pushWZ / len;
                    double vn = player.motionX * nx + player.motionZ * nz;
                    if (vn < 0.04) {
                        player.motionX += nx * (0.04 - vn);
                        player.motionZ += nz * (0.04 - vn);
                    }
                }
            }
        }

        if (moved) {
            player.setPosition(px, feetY + feetOffset, pz);
        }
    }
}