package com.orbitalsunworks.world.orbit;

import net.minecraft.world.biome.BiomeGenBase;

/**
 * Bioma da órbita: vazio, sem spawns, sem decoração. Só existe para o
 * WorldChunkManagerHell do provider (1.7.10 exige um bioma).
 */
public class BiomeGenOrbit extends BiomeGenBase {

    public static final BiomeGenOrbit INSTANCE = new BiomeGenOrbit(200);

    private BiomeGenOrbit(int id) {
        super(id, false); // false: não registrar no array de biomas vanilla
        this.setBiomeName("Órbita");
        this.setDisableRain();
        this.setTemperatureRainfall(0.0f, 0.0f);
        // Sem listas de spawn: espaço não tem mobs.
        this.spawnableCreatureList.clear();
        this.spawnableMonsterList.clear();
        this.spawnableWaterCreatureList.clear();
        this.spawnableCaveCreatureList.clear();
        this.theBiomeDecorator.treesPerChunk = 0;
        this.theBiomeDecorator.grassPerChunk = 0;
    }
}
