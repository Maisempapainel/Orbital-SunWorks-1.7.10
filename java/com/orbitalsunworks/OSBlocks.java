package com.orbitalsunworks;

import com.orbitalsunworks.block.BlockCladded;
import com.orbitalsunworks.block.BlockComputer;
import com.orbitalsunworks.block.BlockCraftingStation;
import com.orbitalsunworks.block.BlockLaunchPlatform;
import com.orbitalsunworks.block.BlockModOre;
import com.orbitalsunworks.block.BlockNavRocket;
import com.orbitalsunworks.block.BlockOrbitFloor;
import com.orbitalsunworks.block.BlockOrbitSatellite;
import com.orbitalsunworks.block.BlockRocket;

import cpw.mods.fml.common.registry.GameRegistry;

public class OSBlocks {

    public static BlockModOre oreRedmeg;
    public static BlockModOre oreYellomite;
    public static BlockModOre oreCopper;
    public static BlockModOre oreTin;
    public static BlockModOre oreAluminium;
    public static BlockModOre oreTitanium;
    public static BlockModOre oreEnergizedCoal;
    public static BlockModOre oreSilicon;

    public static BlockCladded blockCladded;
    public static BlockCraftingStation blockCraftingStation;
    public static BlockComputer blockComputer;
    public static BlockLaunchPlatform blockLaunchPlatform;
    public static BlockRocket blockRocket;
    public static BlockOrbitSatellite blockOrbitSatellite;
    public static BlockNavRocket blockNavRocket;
    public static BlockOrbitFloor blockOrbitFloor;

    public static void init() {
        oreRedmeg = registerOre("oreRedmeg", OSItems.ingotRedmeg, 1, 1, 3, 7);
        oreYellomite = registerOre("oreYellomite", OSItems.ingotYellomite, 1, 1, 3, 7);
        oreCopper = registerOre("oreCopper", OSItems.ingotCopper, 1, 1, 0, 2);
        oreTin = registerOre("oreTin", OSItems.ingotTin, 1, 1, 0, 2);
        oreAluminium = registerOre("oreAluminium", OSItems.ingotAluminium, 1, 1, 0, 2);
        oreTitanium = registerOre("oreTitanium", OSItems.ingotTitanium, 1, 1, 3, 7);
        oreEnergizedCoal = registerOre("oreEnergizedCoal", OSItems.energizedCoal, 1, 2, 0, 2);
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

        GameRegistry.registerTileEntity(com.orbitalsunworks.craftingstation.TileCraftingStation.class, "tileCraftingStation");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileRocket.class, "tileRocket");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileOrbitSatellite.class, "tileOrbitSatellite");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileNavRocket.class, "tileNavRocket");
        GameRegistry.registerTileEntity(com.orbitalsunworks.block.TileLaunchPlatform.class, "tileLaunchPlatform");

        com.orbitalsunworks.world.OSWorldGenerator.register();
    }

    private static BlockModOre registerOre(String name, net.minecraft.item.Item drop, int minDrop, int maxDrop, int minXp, int maxXp) {
        BlockModOre ore = new BlockModOre(name, drop, minDrop, maxDrop, minXp, maxXp);
        GameRegistry.registerBlock(ore, name);
        return ore;
    }
}