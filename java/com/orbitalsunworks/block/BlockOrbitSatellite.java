package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.world.orbit.OrbitStructureManager;

/**
 * ESTRUTURA DO SATÉLITE na dimensão orbital (v0.6.0).
 *
 * É o satélite DE VERDADE: uma estrutura física que o jogador encontra
 * quando navega até a órbita — corpo central + painéis solares + antena
 * (montada pelo OrbitStructureManager em coords fixas).
 *
 * Regras:
 * - INDESTRUTÍVEL: nenhuma quebra (survival, criativo, explosão). O
 *   satélite já está em órbita — não faz sentido "quebrá-lo" de lá;
 * - clicar com o direito abre a GUI de upgrades/reparos (vida útil,
 *   geração de RF, módulos instalados, reparo com liga de titânio);
 * - o tile sincroniza com o SatelliteManager por UUID (o mesmo dado do
 *   registro global — mexer aqui mexe no satélite "real").
 */
public class BlockOrbitSatellite extends Block {

    public BlockOrbitSatellite() {
        super(Material.iron);
        this.setBlockName("blockOrbitSatellite");
        this.setCreativeTab(null); // não tem no criativo: só existe em órbita
        this.setHardness(-1.0f);   // INDESTRUTÍVEL (unbreakable, tipo bedrock)
        this.setResistance(6000000.0f); // à prova de explosão
        this.setStepSound(soundTypeMetal);
        // Sem textura de bloco normal: a estrutura usa render especial.
    }

    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new com.orbitalsunworks.satellite.TileOrbitSatellite();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof com.orbitalsunworks.satellite.TileOrbitSatellite) {
                player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                    com.orbitalsunworks.handler.GuiHandler.ID_ORBIT_SATELLITE, world, x, y, z);
            }
        }
        return true;
    }

    // ------------------------------------------------------------------
    // INDESTRUTÍVEL: nenhuma via de quebra passa
    // ------------------------------------------------------------------

    @Override
    public int quantityDropped(java.util.Random random) {
        return 0; // nada dropa: estrutura indestrutível
    }

    @Override
    public boolean removedByPlayer(World world, net.minecraft.entity.player.EntityPlayer player,
            int x, int y, int z, boolean willHarvest) {
        return false; // NUNCA sai do mundo por quebra de jogador
    }

    @Override
    public boolean canEntityDestroy(net.minecraft.world.IBlockAccess world, int x, int y, int z,
            net.minecraft.entity.Entity entity) {
        return false; // dragon/wither/explosões: nada destrói
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z,
            net.minecraft.world.Explosion explosion) {
        // à prova de explosão (não faz nada — o bloco permanece)
    }

    @Override
    public boolean canHarvestBlock(net.minecraft.entity.player.EntityPlayer player, int metadata) {
        return false;
    }

    // Sem drop de item (quantityDropped acima já garante, mas explícito):
    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    // ------------------------------------------------------------------
    // Pick-block no criativo: devolve NADA (estrutura não é item)
    // ------------------------------------------------------------------

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1; // render especial (TESR) — igual ao foguete
    }
}
