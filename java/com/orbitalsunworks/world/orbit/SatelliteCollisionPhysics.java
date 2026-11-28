package com.orbitalsunworks.world.orbit;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Física OBB + SAT 2D exata e 100% sincronizada com o TileOrbitSatelliteRenderer:
 *  - Usa exatamente a mesma velocidade angular (SPIN_RATE) do renderizador;
 *  - Testa a AABB real do jogador no mundo contra todas as caixas rotacionadas
 *    do satélite (barramento, propulsor, asas solares esquerda/direita, mastro e prato);
 *  - Transporta suavemente o jogador que estiver apoiado em cima dos painéis/corpo;
 *  - Empurra o jogador caso ele fique parado na trajetória de rotação das asas.
 */
public class SatelliteCollisionPhysics {

    /** Velocidade angular compartilhada com TileOrbitSatelliteRenderer (graus/tick). */
    public static final float SPIN_RATE = 0.05f;
    /** Escala do ModelSatellite na órbita. */
    public static final float SAT_SCALE = 2.2f;

    private static final double PLAYER_HALF_W = 0.31;
    private static final double PLAYER_HEIGHT = 1.80;

    /**
     * Caixas sólidas no espaço local do satélite (já multiplicadas por SAT_SCALE).
     * Ordem: { minX, maxX, minY, maxY, minZ, maxZ }
     */
    private static final double[][] LOCAL_BOXES = {
        // Barramento central + bocal inferior
        { -0.49 * SAT_SCALE, 0.49 * SAT_SCALE, -0.24 * SAT_SCALE, 1.24 * SAT_SCALE, -0.49 * SAT_SCALE, 0.49 * SAT_SCALE },
        // Braço + Asa solar esquerda (-X)
        { -3.32 * SAT_SCALE, -0.45 * SAT_SCALE, 0.50 * SAT_SCALE, 0.72 * SAT_SCALE, -0.27 * SAT_SCALE, 0.27 * SAT_SCALE },
        // Braço + Asa solar direita (+X)
        { 0.45 * SAT_SCALE, 3.32 * SAT_SCALE, 0.50 * SAT_SCALE, 0.72 * SAT_SCALE, -0.27 * SAT_SCALE, 0.27 * SAT_SCALE },
        // Mastro e prato parabólico superior
        { -0.58 * SAT_SCALE, 0.58 * SAT_SCALE, 1.22 * SAT_SCALE, 2.34 * SAT_SCALE, -0.30 * SAT_SCALE, 0.96 * SAT_SCALE }
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
        double startX = px;
        double startZ = pz;
        double playerH = Math.max(1.5, player.boundingBox.maxY - player.boundingBox.minY);
        if (Double.isNaN(playerH) || playerH <= 0.0) {
            playerH = PLAYER_HEIGHT;
        }

        // 1) Transporte (carry): se o jogador estava apoiado no topo de alguma caixa no tick anterior, gira junto
        double cosPrev = Math.cos(prevTheta);
        double sinPrev = Math.sin(prevTheta);
        boolean carried = false;
        double carriedTopY = feetY;

        for (double[] box : LOCAL_BOXES) {
            double topY = cy + box[3];
            if (Math.abs(feetY - topY) <= 0.18 && player.motionY <= 0.08
                    && overlapsSat2D(px, pz, cx, cz, cosPrev, sinPrev, box)) {
                carried = true;
                carriedTopY = topY;
                break;
            }
        }

        if (carried) {
            double dx = px - cx;
            double dz = pz - cz;
            double cd = Math.cos(dTheta);
            double sd = Math.sin(dTheta);
            px = cx + (dx * cd + dz * sd);
            pz = cz + (-dx * sd + dz * cd);
            if (!player.capabilities.isFlying) {
                feetY = carriedTopY;
                if (player.motionY < 0.0) {
                    player.motionY = 0.0;
                }
                player.onGround = true;
                player.fallDistance = 0.0f;
            }
        }

        // 2) Resolução OBB sólida via SAT 2D (world AABB vs OBB rotacionado) em 2 iterações
        double cosT = Math.cos(theta);
        double sinT = Math.sin(theta);
        boolean moved = carried;
        boolean pushedHorizontally = false;

        for (int iter = 0; iter < 2; iter++) {
            for (double[] box : LOCAL_BOXES) {
                double bMinY = cy + box[2];
                double bMaxY = cy + box[3];
                double oy = Math.min(feetY + playerH, bMaxY) - Math.max(feetY, bMinY);
                if (oy <= 0.0) {
                    continue;
                }

                double localCx = (box[0] + box[1]) * 0.5;
                double localCz = (box[4] + box[5]) * 0.5;
                double hx = (box[1] - box[0]) * 0.5;
                double hz = (box[5] - box[4]) * 0.5;

                // Centro da caixa no mundo: glRotatef(theta, 0,1,0) => x' = x*cos + z*sin, z' = -x*sin + z*cos
                double bx = cx + localCx * cosT + localCz * sinT;
                double bz = cz - localCx * sinT + localCz * cosT;

                double relX = px - bx;
                double relZ = pz - bz;
                double absCos = Math.abs(cosT);
                double absSin = Math.abs(sinT);

                // Eixo 1: Mundo X (1, 0)
                double oWX = (PLAYER_HALF_W + hx * absCos + hz * absSin) - Math.abs(relX);
                if (oWX <= 0.0) continue;

                // Eixo 2: Mundo Z (0, 1)
                double oWZ = (PLAYER_HALF_W + hx * absSin + hz * absCos) - Math.abs(relZ);
                if (oWZ <= 0.0) continue;

                // Eixo 3: Local U (cosT, -sinT)
                double projU = relX * cosT - relZ * sinT;
                double oU = (hx + PLAYER_HALF_W * (absCos + absSin)) - Math.abs(projU);
                if (oU <= 0.0) continue;

                // Eixo 4: Local V (sinT, cosT)
                double projV = relX * sinT + relZ * cosT;
                double oV = (hz + PLAYER_HALF_W * (absCos + absSin)) - Math.abs(projV);
                if (oV <= 0.0) continue;

                double minHorizOverlap = Math.min(Math.min(oWX, oWZ), Math.min(oU, oV));
                moved = true;

                double fromTop = bMaxY - feetY;
                double fromBot = (feetY + playerH) - bMinY;

                if (fromTop > 0.0 && fromTop <= 0.42 && oy <= minHorizOverlap + 0.14) {
                    feetY = bMaxY;
                    if (player.motionY < 0.0) {
                        player.motionY = 0.0;
                    }
                    if (!player.capabilities.isFlying) {
                        player.onGround = true;
                    }
                    player.fallDistance = 0.0f;
                } else if (fromBot > 0.0 && fromBot <= 0.42 && oy <= minHorizOverlap) {
                    feetY = bMinY - playerH;
                    if (player.motionY > 0.0) {
                        player.motionY = 0.0;
                    }
                } else {
                    // Empurra no eixo de menor penetração (MTV)
                    double nx = 0.0;
                    double nz = 0.0;
                    if (minHorizOverlap == oU) {
                        double sgn = projU >= 0.0 ? 1.0 : -1.0;
                        nx = cosT * sgn;
                        nz = -sinT * sgn;
                    } else if (minHorizOverlap == oV) {
                        double sgn = projV >= 0.0 ? 1.0 : -1.0;
                        nx = sinT * sgn;
                        nz = cosT * sgn;
                    } else if (minHorizOverlap == oWX) {
                        nx = relX >= 0.0 ? 1.0 : -1.0;
                        nz = 0.0;
                    } else {
                        nx = 0.0;
                        nz = relZ >= 0.0 ? 1.0 : -1.0;
                    }

                    double pushDist = minHorizOverlap + 0.012;
                    px += nx * pushDist;
                    pz += nz * pushDist;
                    pushedHorizontally = true;

                    double vn = player.motionX * nx + player.motionZ * nz;
                    if (vn < 0.05) {
                        player.motionX += nx * (0.05 - vn);
                        player.motionZ += nz * (0.05 - vn);
                    }
                }
            }
        }

        if (moved) {
            player.setPosition(px, feetY + feetOffset, pz);
            // Se o servidor empurrou um jogador parado e houve desvio relevante, sincroniza posição
            if (!world.isRemote && pushedHorizontally && player instanceof EntityPlayerMP) {
                double movedDistSq = (px - startX) * (px - startX) + (pz - startZ) * (pz - startZ);
                if (movedDistSq > 0.25) {
                    ((EntityPlayerMP) player).playerNetServerHandler.setPlayerLocation(
                        px, feetY + feetOffset, pz, player.rotationYaw, player.rotationPitch);
                }
            }
        }
    }

    private static boolean overlapsSat2D(double px, double pz, double cx, double cz,
            double cosT, double sinT, double[] box) {
        double localCx = (box[0] + box[1]) * 0.5;
        double localCz = (box[4] + box[5]) * 0.5;
        double hx = (box[1] - box[0]) * 0.5;
        double hz = (box[5] - box[4]) * 0.5;
        double bx = cx + localCx * cosT + localCz * sinT;
        double bz = cz - localCx * sinT + localCz * cosT;
        double relX = px - bx;
        double relZ = pz - bz;
        double absCos = Math.abs(cosT);
        double absSin = Math.abs(sinT);

        if (Math.abs(relX) >= PLAYER_HALF_W + hx * absCos + hz * absSin) return false;
        if (Math.abs(relZ) >= PLAYER_HALF_W + hx * absSin + hz * absCos) return false;
        if (Math.abs(relX * cosT - relZ * sinT) >= hx + PLAYER_HALF_W * (absCos + absSin)) return false;
        if (Math.abs(relX * sinT + relZ * cosT) >= hz + PLAYER_HALF_W * (absCos + absSin)) return false;
        return true;
    }
}