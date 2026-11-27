package com.orbitalsunworks.world.orbit;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.satellite.TileOrbitSatellite;

/**
 * Monta e mantém a âncora central clicável (1x3x1) dos satélites em órbita.
 * A colisão dinâmica completa (incluindo as asas solares giratórias e o prato)
 * fica a cargo de SatelliteCollisionPhysics.
 */
public final class OrbitStructureManager {

    public static final int SPACING = 200;
    public static final int BASE_Y = 64;

    private OrbitStructureManager() {
    }

    public static int[] coordsForIndex(int index) {
        return new int[]{ 8 + index * SPACING, BASE_Y, 8 };
    }

    public static int indexOf(SatelliteManager.Satellite sat, SatelliteManager manager) {
        int idx = 0;
        for (SatelliteManager.Satellite s : manager.getSatellites()) {
            if (s == sat) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    public static void ensureStructure(World anyWorld, SatelliteManager.Satellite sat) {
        if (anyWorld == null || sat == null) {
            return;
        }
        int dim = OrbitDimensionManager.getDimensionId();
        if (dim < 0) {
            return;
        }
        WorldServer orbit = net.minecraftforge.common.DimensionManager.getWorld(dim);
        if (orbit == null) {
            return;
        }

        SatelliteManager manager = SatelliteManager.get(anyWorld);
        int index = indexOf(sat, manager);
        if (index < 0) {
            return;
        }

        int[] c = coordsForIndex(index);
        int x = c[0], y = c[1], z = c[2];

        // Remove eventuais blocos estáticos laterais legados que não giravam com os painéis
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                for (int dy = 0; dy < 4; dy++) {
                    if (orbit.getBlock(x + dx, y + dy, z + dz) == OSBlocks.blockOrbitSatellite) {
                        orbit.setBlockToAir(x + dx, y + dy, z + dz);
                    }
                }
            }
        }

        TileEntity existing = orbit.getTileEntity(x, y, z);
        if (existing instanceof TileOrbitSatellite) {
            TileOrbitSatellite tile = (TileOrbitSatellite) existing;
            if (sat.id.equals(tile.getSatelliteId())) {
                return;
            }
        }

        for (int dy = 0; dy < 4; dy++) {
            if (orbit.getBlock(x, y + dy, z) == OSBlocks.blockOrbitSatellite) {
                orbit.setBlockToAir(x, y + dy, z);
            }
        }

        // Coluna central 1x3x1 clicável (meta 0 = âncora com TileEntity)
        orbit.setBlock(x, y, z, OSBlocks.blockOrbitSatellite, 0, 3);
        TileEntity te = orbit.getTileEntity(x, y, z);
        if (te instanceof TileOrbitSatellite) {
            ((TileOrbitSatellite) te).setSatelliteId(sat.id);
        }
        orbit.setBlock(x, y + 1, z, OSBlocks.blockOrbitSatellite, 1, 3);
        orbit.setBlock(x, y + 2, z, OSBlocks.blockOrbitSatellite, 2, 3);
    }
}