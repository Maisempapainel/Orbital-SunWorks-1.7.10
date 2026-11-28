package com.orbitalsunworks.world;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.block.BlockStationPart;
import com.orbitalsunworks.satellite.TileControlKeyboard;
import com.orbitalsunworks.satellite.TileControlMonitor;
import com.orbitalsunworks.satellite.TileStationDoor;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.util.PlayerLookup;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * Construtor progressivo da Estação de Controle Orbital:
 *  - Silhueta arredondada (cantos curvos + telhado inclinado em degraus/escadas)
 *  - Duas salas internas divididas por divisória envidraçada com portal:
 *      * Direita/Centro: Sala de Controle Orbital (Monitor 2x3 + Teclado 3 blocos + Núcleo)
 *      * Esquerda: Sala de Observação Astronômica (Telescópio sob cúpula aberta)
 */
public final class ControlStationBuilder {

    private static final int BLOCKS_PER_TICK = 28;
    private static final List<BuildTask> ACTIVE_BUILDS = new ArrayList<BuildTask>();

    private ControlStationBuilder() {
    }

    public static int facingX(int facing) {
        switch (facing & 3) {
            case 1: return -1;
            case 3: return 1;
            default: return 0;
        }
    }

    public static int facingZ(int facing) {
        switch (facing & 3) {
            case 0: return 1;
            case 2: return -1;
            default: return 0;
        }
    }

    public static int[] getCorePosition(int cx, int baseY, int cz, int facing) {
        int fx = facingX(facing);
        int fz = facingZ(facing);
        int rx = -fz;
        int rz = fx;
        int wx = cx + 5 * fx + 2 * rx;
        int wz = cz + 5 * fz + 2 * rz;
        return new int[] { wx, baseY, wz };
    }

    public static boolean isCompletedAt(World world, int refX, int refY, int refZ) {
        return findActiveTaskNear(world, refX, refY, refZ) == null;
    }

    public static float getProgressAt(World world, int refX, int refY, int refZ) {
        BuildTask task = findActiveTaskNear(world, refX, refY, refZ);
        if (task == null || task.steps.isEmpty()) {
            return 1.0f;
        }
        return Math.min(1.0f, Math.max(0.0f, (float) task.cursor / (float) task.steps.size()));
    }

    private static BuildTask findActiveTaskNear(World world, int refX, int refY, int refZ) {
        if (world == null || world.provider == null) {
            return null;
        }
        int dim = world.provider.dimensionId;
        for (BuildTask task : ACTIVE_BUILDS) {
            if (task.dimensionId == dim) {
                long dx = task.centerX - refX;
                long dz = task.centerZ - refZ;
                if (dx * dx + dz * dz <= 22 * 22 && Math.abs(task.baseY - refY) <= 10) {
                    return task;
                }
            }
        }
        return null;
    }

    private static boolean inRoundedHull(int lx, int lz) {
        int ax = Math.abs(lx);
        int az = Math.abs(lz);
        if (ax > 7 || az > 7) {
            return false;
        }
        if (ax <= 4 || az <= 4) {
            return true;
        }
        int dx = ax - 4;
        int dz = az - 4;
        return (dx * dx + dz * dz) <= 10;
    }

    private static boolean isPerimeter(int lx, int lz) {
        if (!inRoundedHull(lx, lz)) {
            return false;
        }
        return !inRoundedHull(lx + 1, lz)
            || !inRoundedHull(lx - 1, lz)
            || !inRoundedHull(lx, lz + 1)
            || !inRoundedHull(lx, lz - 1);
    }

    private static boolean isObservatoryWell(int lx, int lz) {
        return lx == 1 && lz == -4;
    }

    private static boolean isObservatoryCollar(int lx, int lz) {
        return lx >= 0 && lx <= 2 && lz >= -5 && lz <= -3 && !isObservatoryWell(lx, lz);
    }

    public static String validateArea(World world, int cx, int baseY, int cz, int facing) {
        int fx = facingX(facing);
        int fz = facingZ(facing);
        int rx = -fz;
        int rz = fx;

        for (int lx = -10; lx <= 11; lx++) {
            for (int lz = -7; lz <= 7; lz++) {
                boolean inside = inRoundedHull(lx, lz)
                    || (lx >= -10 && lx <= -8 && Math.abs(lz) <= 2)
                    || (lx >= 8 && lx <= 11 && Math.abs(lz) <= 1);
                if (!inside) {
                    continue;
                }
                int wx = cx + lx * fx + lz * rx;
                int wz = cz + lx * fz + lz * rz;
                for (int dy = -1; dy <= 7; dy++) {
                    int wy = baseY + dy;
                    if (wy < 1 || wy >= world.getHeight() - 1) {
                        return "A estrutura ultrapassa os limites de altura do mundo.";
                    }
                    Block b = world.getBlock(wx, wy, wz);
                    if (b == Blocks.bedrock || b.getBlockHardness(world, wx, wy, wz) < 0f) {
                        return "H\u00e1 um bloco indestrut\u00edvel obstruindo a \u00e1rea.";
                    }
                    if (b == OSBlocks.blockLaunchPlatform
                            || b == OSBlocks.blockControlStation
                            || b == OSBlocks.blockRocket
                            || b == OSBlocks.blockNavRocket
                            || b == OSBlocks.blockDispersionRocket) {
                        return "H\u00e1 outra estrutura do Orbital Sunworks no caminho!";
                    }
                }
            }
        }
        return null;
    }

    public static void dismantle(World world, int refX, int refY, int refZ) {
        if (world == null || world.isRemote) {
            return;
        }
        int dim = world.provider.dimensionId;
        Iterator<BuildTask> it = ACTIVE_BUILDS.iterator();
        while (it.hasNext()) {
            BuildTask task = it.next();
            if (task.dimensionId == dim) {
                long dx = task.centerX - refX;
                long dz = task.centerZ - refZ;
                if (dx * dx + dz * dz <= 25 * 25 && Math.abs(task.baseY - refY) <= 10) {
                    it.remove();
                }
            }
        }

        for (int dy = -3; dy <= 10; dy++) {
            int wy = refY + dy;
            if (wy < 1 || wy >= world.getHeight()) {
                continue;
            }
            for (int dx = -19; dx <= 19; dx++) {
                for (int dz = -19; dz <= 19; dz++) {
                    int wx = refX + dx;
                    int wz = refZ + dz;
                    if (isStationBlock(world.getBlock(wx, wy, wz))) {
                        world.setBlockToAir(wx, wy, wz);
                    }
                }
            }
        }
    }

    private static boolean isStationBlock(Block b) {
        return b == OSBlocks.blockStationPart
            || b == OSBlocks.blockStationStair
            || b == OSBlocks.blockStationDoor
            || b == OSBlocks.blockStationLamp
            || b == OSBlocks.blockControlMonitor
            || b == OSBlocks.blockControlKeyboard
            || b == OSBlocks.blockControlComputer
            || b == OSBlocks.blockControlStation
            || b == OSBlocks.blockTelescope
            || b == OSBlocks.blockAntennaPort
            || b == OSBlocks.blockAntennaTower;
    }

    public static void startBuild(World world, int cx, int baseY, int cz, int facing, EntityPlayer owner) {
        if (world == null || world.isRemote) {
            return;
        }
        BuildTask task = new BuildTask();
        task.dimensionId = world.provider.dimensionId;
        task.centerX = cx;
        task.baseY = baseY;
        task.centerZ = cz;
        task.ownerUUID = owner != null ? owner.getUniqueID() : null;
        task.ownerName = owner != null ? owner.getCommandSenderName() : "";

        int fx = facingX(facing);
        int fz = facingZ(facing);
        int rx = -fz;
        int rz = fx;

        int doorAxis = (fz != 0) ? 0 : 1;
        int stairFrontMeta = stairMetaToward(fx, fz);
        int stairBackMeta = stairMetaToward(-fx, -fz);
        int stairRightMeta = stairMetaToward(rx, rz);
        int stairLeftMeta = stairMetaToward(-rx, -rz);

        // FASE 1: Limpeza de ar
        for (int dy = 7; dy >= 0; dy--) {
            for (int lx = -10; lx <= 11; lx++) {
                for (int lz = -7; lz <= 7; lz++) {
                    boolean clear = inRoundedHull(lx, lz)
                        || (dy <= 5 && lx >= -10 && lx <= -8 && Math.abs(lz) <= 2)
                        || (dy <= 6 && lx >= 8 && lx <= 11 && Math.abs(lz) <= 1);
                    if (clear) {
                        int wx = cx + lx * fx + lz * rx;
                        int wz = cz + lx * fz + lz * rz;
                        task.steps.add(BuildStep.block(wx, baseY + dy, wz, Blocks.air, 0));
                    }
                }
            }
        }

        // FASE 2: Fundação
        for (int dy = -3; dy <= -1; dy++) {
            for (int lx = -10; lx <= 11; lx++) {
                for (int lz = -7; lz <= 7; lz++) {
                    boolean base = inRoundedHull(lx, lz)
                        || (lx >= -10 && lx <= -8 && Math.abs(lz) <= 2)
                        || (lx >= 8 && lx <= 10 && Math.abs(lz) <= 1);
                    if (!base) {
                        continue;
                    }
                    int wx = cx + lx * fx + lz * rx;
                    int wz = cz + lx * fz + lz * rz;
                    if (dy == -1) {
                        task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.HULL));
                    } else {
                        task.steps.add(BuildStep.fillIfSoft(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.HULL));
                    }
                }
            }
        }

        // FASE 3: Piso, divisão visual dos pisos, escadaria frontal e ponte RF
        for (int lx = -9; lx <= 10; lx++) {
            for (int lz = -7; lz <= 7; lz++) {
                int wx = cx + lx * fx + lz * rx;
                int wz = cz + lx * fz + lz * rz;

                if (inRoundedHull(lx, lz)) {
                    if (lx == 5 && lz == 2) {
                        task.steps.add(BuildStep.core(wx, baseY, wz));
                    } else if (isPerimeter(lx, lz) || lz == -1) {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.HULL));
                    } else if ((lz == 2 && lx >= -5 && lx <= 4) || isObservatoryCollar(lx, lz) || isObservatoryWell(lx, lz)) {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.ACCENT));
                    } else {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.FLOOR));
                    }
                    continue;
                }

                // Entrada frontal alinhada à Sala de Controle (lz = 0..4, porta em lz = 2)
                if (lx == -8 && lz >= 0 && lz <= 4) {
                    int meta = (lz >= 1 && lz <= 3) ? BlockStationPart.FLOOR : BlockStationPart.ACCENT;
                    task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, meta));
                    continue;
                }
                if (lx == -9 && lz >= 0 && lz <= 4) {
                    if (lz >= 1 && lz <= 3) {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationStair, stairFrontMeta));
                    } else {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.ACCENT));
                    }
                    continue;
                }

                // Ponte traseira e base da antena
                if (lx >= 8 && lx <= 10 && Math.abs(lz) <= 1) {
                    if (lx == 10 && lz == 0) {
                        task.steps.add(BuildStep.antennaPort(wx, baseY, wz));
                    } else if (lz == 0) {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.ACCENT));
                    } else if (lx == 10) {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.HULL));
                    } else {
                        task.steps.add(BuildStep.block(wx, baseY, wz, OSBlocks.blockStationPart, BlockStationPart.TRIM));
                    }
                }
            }
        }

        // Balizadores da entrada
        for (int lz : new int[] { 0, 4 }) {
            int wx9 = cx + (-9) * fx + lz * rx;
            int wz9 = cz + (-9) * fz + lz * rz;
            task.steps.add(BuildStep.block(wx9, baseY + 1, wz9, OSBlocks.blockStationPart, BlockStationPart.TRIM));

            int wx8 = cx + (-8) * fx + lz * rx;
            int wz8 = cz + (-8) * fz + lz * rz;
            task.steps.add(BuildStep.block(wx8, baseY + 1, wz8, OSBlocks.blockStationPart, BlockStationPart.PILLAR));
        }

        // FASE 4: Paredes externas arredondadas + divisória entre Sala do Computador e Observatório
        for (int dy = 1; dy <= 4; dy++) {
            for (int lx = -7; lx <= 7; lx++) {
                for (int lz = -7; lz <= 7; lz++) {
                    if (!isPerimeter(lx, lz)) {
                        continue;
                    }
                    int wx = cx + lx * fx + lz * rx;
                    int wz = cz + lx * fz + lz * rz;

                    // Porta de entrada em (lx = -7, lz = 2)
                    if (lx == -7 && lz == 2) {
                        if (dy == 1 || dy == 2) {
                            continue;
                        }
                        if (dy == 3) {
                            task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.ACCENT));
                            continue;
                        }
                    }

                    if (dy == 4) {
                        task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.TRIM));
                        continue;
                    }

                    boolean isCornerStep = Math.abs(lx) >= 5 && Math.abs(lz) >= 5;
                    boolean isPillar = isCornerStep
                        || (lx == -7 && (lz == 1 || lz == 3))
                        || (lx == 7 && (lz == 0 || lz == 4));
                    if (isPillar) {
                        task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.PILLAR));
                        continue;
                    }

                    if (lx == 7 && lz == 0 && dy == 1) {
                        task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.ACCENT));
                        continue;
                    }

                    // Janelas panorâmicas (parede atrás do monitor lx=+7, lz=1..3 fica 100% sólida)
                    boolean isWindow = (dy == 2 || dy == 3) && (
                        (Math.abs(lz) == 7 && Math.abs(lx) <= 2)
                        || (lx == -7 && (lz == -3 || lz == -4 || lz == 0))
                    );
                    if (isWindow) {
                        task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.GLASS));
                        continue;
                    }

                    int wallMeta = (dy == 1) ? BlockStationPart.HULL : BlockStationPart.WALL;
                    task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, wallMeta));
                }
            }

            // Divisória interna entre Sala de Controle (lz >= 0) e Sala do Observatório (lz <= -2)
            for (int lx = -6; lx <= 6; lx++) {
                int wx = cx + lx * fx + (-1) * rx;
                int wz = cz + lx * fz + (-1) * rz;

                // Portal amplo de passagem entre as duas salas (lx = -2 e -1, dy = 1..3)
                if ((lx == -2 || lx == -1) && dy <= 3) {
                    continue;
                }
                if (dy == 4) {
                    task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.TRIM));
                } else if ((lx >= 1 && lx <= 4) && (dy == 2 || dy == 3)) {
                    task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.GLASS));
                } else if (lx == -3 || lx == 0 || lx == 5) {
                    task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, BlockStationPart.PILLAR));
                } else {
                    int meta = (dy == 1) ? BlockStationPart.HULL : BlockStationPart.WALL;
                    task.steps.add(BuildStep.block(wx, baseY + dy, wz, OSBlocks.blockStationPart, meta));
                }
            }
        }

        // Marquise arredondada sobre a entrada
        for (int lz = 0; lz <= 4; lz++) {
            int wx = cx + (-8) * fx + lz * rx;
            int wz = cz + (-8) * fz + lz * rz;
            int meta = (lz == 1 || lz == 3) ? BlockStationPart.GLASS : BlockStationPart.ROOF;
            task.steps.add(BuildStep.block(wx, baseY + 4, wz, OSBlocks.blockStationPart, meta));
        }

        // FASE 5: Telhado arredondado com beiral chanfrado em escadas + cúpula
        for (int lx = -7; lx <= 7; lx++) {
            for (int lz = -7; lz <= 7; lz++) {
                if (!inRoundedHull(lx, lz) || isObservatoryWell(lx, lz)) {
                    continue;
                }
                int wx = cx + lx * fx + lz * rx;
                int wz = cz + lx * fz + lz * rz;
                if (isObservatoryCollar(lx, lz)) {
                    task.steps.add(BuildStep.block(wx, baseY + 5, wz, OSBlocks.blockStationPart, BlockStationPart.GLASS));
                } else if (isPerimeter(lx, lz)) {
                    int eaveMeta = chooseInwardStairMeta(lx, lz, stairFrontMeta, stairBackMeta, stairRightMeta, stairLeftMeta);
                    if (eaveMeta >= 0) {
                        task.steps.add(BuildStep.block(wx, baseY + 5, wz, OSBlocks.blockStationStair, eaveMeta));
                    } else {
                        task.steps.add(BuildStep.block(wx, baseY + 5, wz, OSBlocks.blockStationPart, BlockStationPart.ROOF));
                    }
                } else {
                    task.steps.add(BuildStep.block(wx, baseY + 5, wz, OSBlocks.blockStationPart, BlockStationPart.ROOF));
                }
            }
        }

        for (int lx = -5; lx <= 5; lx++) {
            for (int lz = -5; lz <= 5; lz++) {
                int ax = Math.abs(lx);
                int az = Math.abs(lz);
                if (ax > 3 && az > 3 && (ax - 3) * (ax - 3) + (az - 3) * (az - 3) > 5) {
                    continue;
                }
                if (lx >= 0 && lx <= 2 && lz >= -5 && lz <= -3) {
                    continue;
                }
                int wx = cx + lx * fx + lz * rx;
                int wz = cz + lx * fz + lz * rz;
                if (lx >= -1 && lx <= 3 && lz >= 1 && lz <= 3) {
                    task.steps.add(BuildStep.block(wx, baseY + 6, wz, OSBlocks.blockStationPart, BlockStationPart.GLASS));
                } else if (ax == 5 || az == 5) {
                    int eaveMeta = chooseInwardStairMeta(lx, lz, stairFrontMeta, stairBackMeta, stairRightMeta, stairLeftMeta);
                    if (eaveMeta >= 0) {
                        task.steps.add(BuildStep.block(wx, baseY + 6, wz, OSBlocks.blockStationStair, eaveMeta));
                    } else {
                        task.steps.add(BuildStep.block(wx, baseY + 6, wz, OSBlocks.blockStationPart, BlockStationPart.TRIM));
                    }
                } else {
                    task.steps.add(BuildStep.block(wx, baseY + 6, wz, OSBlocks.blockStationPart, BlockStationPart.ROOF));
                }
            }
        }

        // FASE 6: Equipamentos (Porta, Lâmpadas, Monitor 2x3, Teclado 3x1, Telescópio, Antena)
        int doorX = cx + (-7) * fx + 2 * rx;
        int doorZ = cz + (-7) * fz + 2 * rz;
        task.steps.add(BuildStep.door(doorX, baseY + 1, doorZ, 0, doorAxis));
        task.steps.add(BuildStep.door(doorX, baseY + 2, doorZ, 1, doorAxis));

        int[][] lampCoords = new int[][] {
            { -3, 2 },
            { 2, 2 },
            { -3, -4 },
            { 4, -4 }
        };
        for (int[] lc : lampCoords) {
            int wx = cx + lc[0] * fx + lc[1] * rx;
            int wz = cz + lc[0] * fz + lc[1] * rz;
            task.steps.add(BuildStep.block(wx, baseY + 4, wz, OSBlocks.blockStationLamp, 0));
        }

        // Teclado 3x1 e Monitor 2x3 colados na parede traseira da Sala de Controle (lx = +6, lz = 1..3)
        for (int lz = 1; lz <= 3; lz++) {
            int wx = cx + 6 * fx + lz * rx;
            int wz = cz + 6 * fz + lz * rz;
            task.steps.add(BuildStep.keyboard(wx, baseY + 1, wz, facing, lz == 2));
        }
        for (int dy = 2; dy <= 3; dy++) {
            for (int lz = 1; lz <= 3; lz++) {
                int wx = cx + 6 * fx + lz * rx;
                int wz = cz + 6 * fz + lz * rz;
                task.steps.add(BuildStep.monitor(wx, baseY + dy, wz, facing, dy == 2 && lz == 2));
            }
        }

        // Telescópio no centro da Sala de Observação
        int telX = cx + 1 * fx + (-4) * rx;
        int telZ = cz + 1 * fz + (-4) * rz;
        task.steps.add(BuildStep.block(telX, baseY + 1, telZ, OSBlocks.blockTelescope, 0));

        // Torre da antena RF
        int antX = cx + 10 * fx;
        int antZ = cz + 10 * fz;
        task.steps.add(BuildStep.block(antX, baseY + 1, antZ, OSBlocks.blockAntennaTower, 0));

        ACTIVE_BUILDS.add(task);
    }

    private static int chooseInwardStairMeta(int lx, int lz, int front, int back, int right, int left) {
        if (Math.abs(lx) >= Math.abs(lz)) {
            return lx < 0 ? front : back;
        }
        return lz < 0 ? right : left;
    }

    private static int stairMetaToward(int dx, int dz) {
        if (dx > 0) return 0;
        if (dx < 0) return 1;
        if (dz > 0) return 2;
        return 3;
    }

    private static void applyStep(World world, BuildTask task, BuildStep step) {
        if (step.fillIfSoft) {
            Block existing = world.getBlock(step.x, step.y, step.z);
            if (!existing.isAir(world, step.x, step.y, step.z)
                    && !existing.getMaterial().isLiquid()
                    && !existing.getMaterial().isReplaceable()) {
                return;
            }
        }

        world.setBlock(step.x, step.y, step.z, step.block, step.meta, 2);

        if (step.kind == StepKind.DOOR) {
            TileEntity te = world.getTileEntity(step.x, step.y, step.z);
            if (te instanceof TileStationDoor) {
                ((TileStationDoor) te).setAxis(step.argA);
            }
        } else if (step.kind == StepKind.MONITOR) {
            TileEntity te = world.getTileEntity(step.x, step.y, step.z);
            if (te instanceof TileControlMonitor) {
                ((TileControlMonitor) te).configure(step.argA, step.flag);
            }
        } else if (step.kind == StepKind.KEYBOARD) {
            TileEntity te = world.getTileEntity(step.x, step.y, step.z);
            if (te instanceof TileControlKeyboard) {
                ((TileControlKeyboard) te).configure(step.argA, step.flag);
            }
        } else if (step.kind == StepKind.CORE || step.kind == StepKind.ANTENNA_PORT) {
            assignOwnerReflectively(world.getTileEntity(step.x, step.y, step.z), task.ownerUUID, task.ownerName);
        }
    }

    private static void assignOwnerReflectively(TileEntity te, UUID ownerUUID, String ownerName) {
        if (te == null) {
            return;
        }
        Class<?> cls = te.getClass();
        try {
            Method m = cls.getMethod("setOwner", UUID.class, String.class);
            m.invoke(te, ownerUUID, ownerName != null ? ownerName : "");
            te.markDirty();
            return;
        } catch (Throwable ignored) {
        }
        try {
            Method m = cls.getMethod("setOwner", UUID.class);
            m.invoke(te, ownerUUID);
            te.markDirty();
            return;
        } catch (Throwable ignored) {
        }
        try {
            Field f = cls.getDeclaredField("ownerUUID");
            f.setAccessible(true);
            f.set(te, ownerUUID);
        } catch (Throwable ignored) {
        }
        try {
            Field f = cls.getDeclaredField("ownerName");
            f.setAccessible(true);
            f.set(te, ownerName != null ? ownerName : "");
        } catch (Throwable ignored) {
        }
        te.markDirty();
    }

    private enum StepKind {
        NORMAL, DOOR, MONITOR, KEYBOARD, CORE, ANTENNA_PORT
    }

    private static final class BuildStep {
        final int x, y, z, meta, argA;
        final Block block;
        final boolean fillIfSoft, flag;
        final StepKind kind;

        private BuildStep(int x, int y, int z, Block block, int meta, boolean fillIfSoft,
                StepKind kind, int argA, boolean flag) {
            this.x = x; this.y = y; this.z = z;
            this.block = block; this.meta = meta;
            this.fillIfSoft = fillIfSoft; this.kind = kind;
            this.argA = argA; this.flag = flag;
        }

        static BuildStep block(int x, int y, int z, Block block, int meta) {
            return new BuildStep(x, y, z, block, meta, false, StepKind.NORMAL, 0, false);
        }

        static BuildStep fillIfSoft(int x, int y, int z, Block block, int meta) {
            return new BuildStep(x, y, z, block, meta, true, StepKind.NORMAL, 0, false);
        }

        static BuildStep door(int x, int y, int z, int meta, int axis) {
            return new BuildStep(x, y, z, OSBlocks.blockStationDoor, meta, false, StepKind.DOOR, axis, false);
        }

        static BuildStep monitor(int x, int y, int z, int facing, boolean master) {
            return new BuildStep(x, y, z, OSBlocks.blockControlMonitor, 0, false, StepKind.MONITOR, facing, master);
        }

        static BuildStep keyboard(int x, int y, int z, int facing, boolean master) {
            return new BuildStep(x, y, z, OSBlocks.blockControlKeyboard, 0, false, StepKind.KEYBOARD, facing, master);
        }

        static BuildStep core(int x, int y, int z) {
            return new BuildStep(x, y, z, OSBlocks.blockControlStation, 0, false, StepKind.CORE, 0, false);
        }

        static BuildStep antennaPort(int x, int y, int z) {
            return new BuildStep(x, y, z, OSBlocks.blockAntennaPort, 0, false, StepKind.ANTENNA_PORT, 0, false);
        }
    }

    private static final class BuildTask {
        int dimensionId, centerX, baseY, centerZ, cursor = 0;
        UUID ownerUUID;
        String ownerName = "";
        final List<BuildStep> steps = new ArrayList<BuildStep>();
    }

    public static class TickHandler {

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END || ACTIVE_BUILDS.isEmpty()) {
                return;
            }
            Iterator<BuildTask> it = ACTIVE_BUILDS.iterator();
            while (it.hasNext()) {
                BuildTask task = it.next();
                World world = DimensionManager.getWorld(task.dimensionId);
                if (world == null) {
                    it.remove();
                    continue;
                }
                int placed = 0;
                while (task.cursor < task.steps.size() && placed < BLOCKS_PER_TICK) {
                    applyStep(world, task, task.steps.get(task.cursor++));
                    placed++;
                }
                if (task.cursor > 0 && task.cursor <= task.steps.size() && (task.cursor % 56 == 0)) {
                    BuildStep last = task.steps.get(task.cursor - 1);
                    world.playSoundEffect(last.x + 0.5, last.y + 0.5, last.z + 0.5,
                        "random.anvil_land", 0.16f, 1.65f);
                }
                if (task.cursor >= task.steps.size()) {
                    EntityPlayerMP player = PlayerLookup.byUUID(world, task.ownerUUID);
                    if (player != null) {
                        OSUtils.sendChat(player, "\u00a7b[Orbital Sunworks] \u00a7aEsta\u00e7\u00e3o de Controle conclu\u00edda! Sala do Computador e Observat\u00f3rio prontos.");
                    }
                    it.remove();
                }
            }
        }
    }
}