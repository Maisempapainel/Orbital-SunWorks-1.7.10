package com.orbitalsunworks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

import com.orbitalsunworks.block.BlockCladded;
import com.orbitalsunworks.block.BlockCraftingStation;
import com.orbitalsunworks.block.BlockLaunchPlatform;
import com.orbitalsunworks.block.BlockModOre;
import com.orbitalsunworks.block.BlockRocket;
import com.orbitalsunworks.block.BlockComputer;
import com.orbitalsunworks.block.BlockOrbitSatellite;
import com.orbitalsunworks.block.BlockNavRocket;
import com.orbitalsunworks.block.BlockOrbitFloor;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Registro de todos os blocos do mod.
 */
public class OSBlocks {

    // ------------------------------------------------------------------
    // Minérios
    // ------------------------------------------------------------------
    public static BlockModOre oreRedmeg;
    public static BlockModOre oreYellomite;
    public static BlockModOre oreCopper;
    public static BlockModOre oreTin;
    public static BlockModOre oreAluminium;
    public static BlockModOre oreTitanium;
    public static BlockModOre oreEnergizedCoal;
    public static BlockModOre oreSilicon;

    // ------------------------------------------------------------------
    // Blocos funcionais
    // ------------------------------------------------------------------
    /** Bloco revestido - esqueleto do satélite (craft com ligas + fibra). */
    public static BlockCladded blockCladded;
    /** Estação de Crafting Orbital - grade 3x5 (15 slots). */
    public static BlockCraftingStation blockCraftingStation;
    /** Computador central - núcleo da futura estação solar. */
    public static BlockComputer blockComputer;
    /** Plataforma de lançamento - estrutura 3x3 multiblock. */
    public static BlockLaunchPlatform blockLaunchPlatform;
    /** Foguete Tier 1 colocado no mundo (bloco com GUI e lançamento). */
    public static BlockRocket blockRocket;
    /** Estrutura do satélite na dimensão orbital (indestrutível). */
    public static BlockOrbitSatellite blockOrbitSatellite;
    /** Foguete de Navegação posicionado na plataforma (multiblock 1x3). */
    public static BlockNavRocket blockNavRocket;
    /** v0.10.0: chão INVISÍVEL da órbita (camada 30) — anti-void. */
    public static BlockOrbitFloor blockOrbitFloor;

    public static void init() {
        // Minérios: nome, item dropado, drop mín/máx, XP mín/máx
        oreRedmeg = registerOre("oreRedmeg", OSItems.ingotRedmeg, 1, 1, 3, 7);
        oreYellomite = registerOre("oreYellomite", OSItems.ingotYellomite, 1, 1, 3, 7);
        oreCopper = registerOre("oreCopper", OSItems.ingotCopper, 1, 1, 0, 2);
        oreTin = registerOre("oreTin", OSItems.ingotTin, 1, 1, 0, 2);
        oreAluminium = registerOre("oreAluminium", OSItems.ingotAluminium, 1, 1, 0, 2);
        oreTitanium = registerOre("oreTitanium", OSItems.ingotTitanium, 1, 1, 3, 7);
        oreEnergizedCoal = registerOre("oreEnergizedCoal", OSItems.energizedCoal, 1, 2, 0, 2);
        // Minério ENERGIZADO: brilha no escuro (luz 7, quase glowstone).
        oreEnergizedCoal.setLightLevel(0.5375f);
        oreSilicon = registerOre("oreSilicon", OSItems.itemSilicon, 1, 1, 0, 2);

        blockCladded = new BlockCladded();
        blockCraftingStation = new BlockCraftingStation();
        blockComputer = new BlockComputer();
        blockLaunchPlatform = new BlockLaunchPlatform();
        blockRocket = new BlockRocket();
        blockOrbitSatellite = new BlockOrbitSatellite();
        blockNavRocket = new BlockNavRocket();
        blockOrbitFloor = new BlockOrbitFloor();

        GameRegistry.registerBlock(blockCladded, "blockCladded");
        GameRegistry.registerBlock(blockCraftingStation, "blockCraftingStation");
        GameRegistry.registerBlock(blockComputer, "blockComputer");
        GameRegistry.registerBlock(blockLaunchPlatform, "blockLaunchPlatform");
        GameRegistry.registerBlock(blockRocket, "blockRocket");
        GameRegistry.registerBlock(blockOrbitSatellite, "blockOrbitSatellite");
        GameRegistry.registerBlock(blockNavRocket, "blockNavRocket");
        GameRegistry.registerBlock(blockOrbitFloor, "blockOrbitFloor");

        // Tile entities
        GameRegistry.registerTileEntity(com.orbitalsunworks.craftingstation.TileCraftingStation.class, "tileCraftingStation");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileRocket.class, "tileRocket");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileOrbitSatellite.class, "tileOrbitSatellite");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileNavRocket.class, "tileNavRocket");

        // Geração no mundo
        com.orbitalsunworks.world.OSWorldGenerator.register();
    }

    private static BlockModOre registerOre(String name, net.minecraft.item.Item drop, int minDrop, int maxDrop, int minXp, int maxXp) {
        BlockModOre ore = new BlockModOre(name, drop, minDrop, maxDrop, minXp, maxXp);
        GameRegistry.registerBlock(ore, name);
        return ore;
    }
}
