package com.orbitalsunworks.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;

import com.orbitalsunworks.OSBlocks;

import cpw.mods.fml.common.IWorldGenerator;

/**
 * Geração dos minérios no mundo (por chunk):
 *
 * Minério            Camadas   Tentativas/chunk  Tamanho véu  Raridade
 * Redmeg             5-30      2                 4            raro (endgame)
 * Yellomite          5-35      2                 4            raro
 * Titânio            5-25      3                 5            raro
 * Alumínio           20-50     10                8            comum
 * Cobre              40-70     12                8            comum
 * Estanho            30-60     10                7            comum
 * Carvão Energizado  10-45     6                 6            médio
 */
public class OSWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
            net.minecraft.world.chunk.IChunkProvider chunkGenerator,
            net.minecraft.world.chunk.IChunkProvider chunkProvider) {
        // So gera no mundo normal (a dimensao espacial futura nao tera minerios).
        if (world.provider.dimensionId == 0) {
            generateOverworld(random, chunkX, chunkZ, world);
        }
    }

    private void generateOverworld(Random random, int chunkX, int chunkZ, World world) {
        // Cada chamada: tenta gerar um veio do minério em posicao aleatoria.
        // WorldGenMinable(bloco, tamanhoDoVeio).generate(world, rand, x, y, z)
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreRedmeg, 4, 5, 30, 2);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreYellomite, 4, 5, 35, 2);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreTitanium, 5, 5, 25, 3);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreAluminium, 8, 20, 50, 10);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreCopper, 8, 40, 70, 12);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreTin, 7, 30, 60, 10);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreEnergizedCoal, 6, 10, 45, 6);
        genOre(world, random, chunkX, chunkZ, OSBlocks.oreSilicon, 7, 10, 60, 8);
    }

    /**
     * Gera um minério: chance tentativas por chunk, cada tentativa sorteia
     * x/z dentro do chunk e y dentro do intervalo de camadas.
     */
    private void genOre(World world, Random random, int chunkX, int chunkZ,
            net.minecraft.block.Block ore, int veinSize, int minY, int maxY, int chances) {
        for (int i = 0; i < chances; i++) {
            int x = chunkX * 16 + random.nextInt(16);
            int y = minY + random.nextInt(maxY - minY);
            int z = chunkZ * 16 + random.nextInt(16);
            new WorldGenMinable(ore, veinSize).generate(world, random, x, y, z);
        }
    }

    public static void register() {
        cpw.mods.fml.common.registry.GameRegistry.registerWorldGenerator(new OSWorldGenerator(), 0);
    }
}
