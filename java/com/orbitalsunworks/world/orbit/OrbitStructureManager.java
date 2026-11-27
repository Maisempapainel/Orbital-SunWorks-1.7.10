package com.orbitalsunworks.world.orbit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.satellite.TileOrbitSatellite;

/**
 * Cuida das estruturas físicas dos satélites na dimensão orbital (v0.6.0).
 *
 * Cada satélite registrado ganha uma estrutura em COORDS FIXAS: o slot N
 * (ordem de lançamento) fica em x = 8 + N*200, y = 64, z = 8 (distância
 * de 200 entre estruturas — nunca se sobrepõem, e voar de uma até a
 * outra dá a sensação de "navegar entre satélites").
 *
 * A estrutura é MONTADA SOB DEMANDA quando o dono visita a órbita (não
 * custa nada ao servidor enquanto ninguém está lá). Se já existe, não
 * refaz (idempotente). Se o satélite morreu (vida 0), a estrutura não é
 * montada (só "restos" ficariam).
 *
 * Layout da estrutura (vista de frente, igual à foto do usuário):
 *
 *      [antena]                y+2
 *   [painel][corpo][painel]    y+1  <- corpo central (o bloco com GUI)
 *      (abrangendo o corpo)    y
 *
 * O render 3D (TESR TileOrbitSatelliteRenderer) desenha painéis e antena
 * detalhados; os BLOCOS físicos são o corpo (com tile/UUID) + painéis
 * decorativos nas laterais — tudo indestrutível.
 */
public final class OrbitStructureManager {

    /** Distância entre estruturas de satélites vizinhos. */
    public static final int SPACING = 200;
    /** Altura das estruturas. */
    public static final int BASE_Y = 64;

    private OrbitStructureManager() {
    }

    /** Coordenadas do "slot" do i-ésimo satélite (ordem de lançamento). */
    public static int[] coordsForIndex(int index) {
        return new int[]{8 + index * SPACING, BASE_Y, 8};
    }

    /** Índice de lançamento do satélite (posição no manager). */
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

    /**
     * Garante que a estrutura do satélite existe na dimensão orbital.
     * Idempotente: se o corpo (bloco com tile e o mesmo UUID) já está
     * lá, não faz nada. Chamado ao CHEGAR na órbita.
     */
    public static void ensureStructure(World anyWorld, SatelliteManager.Satellite sat) {
        if (anyWorld == null || sat == null) {
            return;
        }
        WorldServer orbit = DimensionManagerHelper.getOrbitWorld();
        if (orbit == null) {
            return; // dimensão não registrada (não deveria acontecer)
        }
        SatelliteManager manager = SatelliteManager.get(anyWorld);
        int index = indexOf(sat, manager);
        if (index < 0) {
            return;
        }
        int[] c = coordsForIndex(index);
        int x = c[0], y = c[1], z = c[2];

        // Corpo já existe com o MESMO UUID? Estrutura garantida.
        TileEntity existing = orbit.getTileEntity(x, y, z);
        if (existing instanceof TileOrbitSatellite) {
            TileOrbitSatellite tile = (TileOrbitSatellite) existing;
            if (sat.id.equals(tile.getSatelliteId())) {
                return; // já montada
            }
        }

        // ------------------------------------------------------------
        // MULTIBLOCK GRANDE (v0.7.0): o satélite da foto, em tamanho
        // real na órbita. Layout (metas):
        //   0     = bloco-âncora com TILE (corpo central, base da cruz)
        //           — ÚNICO clicável, é dele que o TESR desenha TUDO;
        //   1     = painel (braços laterais + placas — só colisão);
        //   2     = antena (topo — só colisão);
        // A imagem 3D completa (ModelSatellite em escala 2.2x, painéis
        // ABERTOS) é desenhada pelo TESR a partir do âncora; os blocos
        // são a HITBOX: 5 de largura (painéis), 4 de altura (antena).
        // ------------------------------------------------------------
        // Zona 3x4x3 limpa primeiro (idempotente com estruturas velhas).
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy < 4; dy++) {
                    if (orbit.getBlock(x + dx, y + dy, z + dz) == OSBlocks.blockOrbitSatellite) {
                        orbit.setBlockToAir(x + dx, y + dy, z + dz);
                    }
                }
            }
        }
        // Âncora com tile (corpo central).
        orbit.setBlock(x, y, z, OSBlocks.blockOrbitSatellite, 0, 3);
        TileEntity te = orbit.getTileEntity(x, y, z);
        if (te instanceof TileOrbitSatellite) {
            ((TileOrbitSatellite) te).setSatelliteId(sat.id);
        }
        // Corpo estendido (2 de altura acima do âncora).
        orbit.setBlock(x, y + 1, z, OSBlocks.blockOrbitSatellite, 1, 3);
        orbit.setBlock(x, y + 2, z, OSBlocks.blockOrbitSatellite, 2, 3);
        // Braços + painéis (faixa de 5 blocos de largura, na altura do
        // corpo — hitbox dos painéis abertos).
        for (int dx = -2; dx <= 2; dx++) {
            if (dx == 0) {
                continue; // corpo já posto
            }
            orbit.setBlock(x + dx, y + 1, z, OSBlocks.blockOrbitSatellite, 1, 3);
        }
    }

    /** Mundo da dimensão orbital (ou null se não registrado). */
    private static class DimensionManagerHelper {
        static WorldServer getOrbitWorld() {
            int dim = OrbitDimensionManager.getDimensionId();
            if (dim < 0) {
                return null;
            }
            return net.minecraftforge.common.DimensionManager.getWorld(dim);
        }
    }
}
