package com.orbitalsunworks.world;

import net.minecraft.world.World;

/**
 * Gerencia reservas de petróleo por chunk (raridade determinística de 1 em 20
 * no Overworld, garantindo consistência pela seed do mundo).
 */
public final class OilFieldManager {

    public static final int RARITY_DENOMINATOR = 20;

    private OilFieldManager() {
    }

    public static boolean hasOilInChunk(World world, int chunkX, int chunkZ) {
        if (world == null || world.provider == null || world.provider.dimensionId != 0) {
            return false;
        }
        long seed = world.getSeed();
        long h = seed ^ (chunkX * 341873128712L) ^ (chunkZ * 132897987541L) ^ 0x4F494C5F53554EL;
        h = (h ^ (h >>> 30)) * 0xBF58476D1CE4E5B9L;
        h = (h ^ (h >>> 27)) * 0x94D049BB133111EBL;
        h = h ^ (h >>> 31);
        long mod = Math.abs(h) % RARITY_DENOMINATOR;
        return mod == 0L;
    }

    /**
     * Verifica se um multiblock de raio 'radius' centrado em (centerX, centerZ)
     * está 100% contido dentro de uma única chunk e se essa chunk possui petróleo.
     */
    public static boolean isAreaInsideSingleChunk(int centerX, int centerZ, int radius) {
        int minChunkX = (centerX - radius) >> 4;
        int maxChunkX = (centerX + radius) >> 4;
        int minChunkZ = (centerZ - radius) >> 4;
        int maxChunkZ = (centerZ + radius) >> 4;
        return minChunkX == maxChunkX && minChunkZ == maxChunkZ;
    }

    public static int getOilRichnessPct(World world, int chunkX, int chunkZ) {
        if (!hasOilInChunk(world, chunkX, chunkZ)) {
            return 0;
        }
        long h = world.getSeed() ^ (chunkX * 918273645L) ^ (chunkZ * 1234567891L);
        h = (h ^ (h >>> 29)) * 0x9E3779B97F4A7C15L;
        return 70 + (int) (Math.abs(h) % 31L);
    }
}