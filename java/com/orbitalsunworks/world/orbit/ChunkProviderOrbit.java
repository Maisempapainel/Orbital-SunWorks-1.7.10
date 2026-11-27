package com.orbitalsunworks.world.orbit;

import java.util.List;

import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

/**
 * Chunk provider da dimensão orbital: mundo VAZIO (sem terreno, sem ores,
 * sem mobs). Cada chunk é ar puro — a estrutura do satélite é colocada
 * em coords fixas pelo OrbitStructureManager quando o jogador visita.
 *
 * Custo no servidor: praticamente zero (chunk de ar, sem população, sem
 * decoração — o mundo é "vazio leve"; nem ExtendedBlockStorage precisa
 * ser alocado).
 */
public class ChunkProviderOrbit implements IChunkProvider {

    private final World worldObj;

    public ChunkProviderOrbit(World world) {
        this.worldObj = world;
    }

    @Override
    public boolean chunkExists(int x, int z) {
        return true; // chunks void são sempre "válidos" (gerados sob demanda)
    }

    @Override
    public Chunk loadChunk(int x, int z) {
        return provideChunk(x, z);
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        // Chunk 100% vazio (ar) — sem terreno algum. O Chunk do vanilla
        // já nasce com storage vazio (= tudo ar); só geramos o skylight
        // para a luz "acima" ser céu aberto (o render de céu é nosso).
        Chunk chunk = new Chunk(this.worldObj, x, z);
        // v0.10.0: CHÃO INVISÍVEL na camada 30 em TODO chunk (o pedido do
        // usuário: "um chão preto invisível lá em baixo, na camada 30,
        // que não permita que meu personagem morra no void"). O bloco não
        // renderiza nada (getRenderType -1) — a Terra do céu continua
        // visível "através" dele lá embaixo.
        ExtendedBlockStorage storage = chunk.getBlockStorageArray()[FLOOR_Y >> 4];
        if (storage == null) {
            // v0.10.0 FIX: storage com SKYLIGHT (true) — o antigo (false)
            // crashava com NPE no generateSkylightMap: setExtSkylightValue
            // escreve no skylightArray, que era null.
            storage = new ExtendedBlockStorage(FLOOR_Y >> 4 << 4, true);
            chunk.getBlockStorageArray()[FLOOR_Y >> 4] = storage;
        }
        for (int bx = 0; bx < 16; bx++) {
            for (int bz = 0; bz < 16; bz++) {
                storage.func_150818_a(bx, FLOOR_Y & 15, bz,
                    com.orbitalsunworks.OSBlocks.blockOrbitFloor);
            }
        }
        chunk.generateSkylightMap();
        return chunk;
    }

    /** Camada do chão invisível (anti-void). */
    public static final int FLOOR_Y = 30;

    @Override
    public void populate(IChunkProvider provider, int x, int z) {
        // Mundo void: nada a popular.
    }

    @Override
    public boolean saveChunks(boolean all, net.minecraft.util.IProgressUpdate progress) {
        return true;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public String makeString() {
        return "OrbitSpaceGenerator";
    }

    @Override
    public List getPossibleCreatures(net.minecraft.entity.EnumCreatureType type, int x, int y, int z) {
        return null; // sem mobs no espaço
    }

    @Override
    public ChunkPosition func_147416_a(World world, String name, int x, int y, int z) {
        return null; // mundo void: sem estruturas
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int x, int z) {
    }

    @Override
    public void saveExtraData() {
    }
}
