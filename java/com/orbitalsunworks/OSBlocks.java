package com.orbitalsunworks;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlockWithMetadata;
import net.minecraft.item.ItemStack;

import com.orbitalsunworks.block.BlockAntennaPort;
import com.orbitalsunworks.block.BlockAntennaTower;
import com.orbitalsunworks.block.BlockCladded;
import com.orbitalsunworks.block.BlockComputer;
import com.orbitalsunworks.block.BlockControlComputer;
import com.orbitalsunworks.block.BlockControlKeyboard;
import com.orbitalsunworks.block.BlockControlMonitor;
import com.orbitalsunworks.block.BlockControlStation;
import com.orbitalsunworks.block.BlockCraftingStation;
import com.orbitalsunworks.block.BlockDispersionRocket;
import com.orbitalsunworks.block.BlockLaunchPlatform;
import com.orbitalsunworks.block.BlockModOre;
import com.orbitalsunworks.block.BlockNavRocket;
import com.orbitalsunworks.block.BlockOrbitFloor;
import com.orbitalsunworks.block.BlockOrbitSatellite;
import com.orbitalsunworks.block.BlockRocket;
import com.orbitalsunworks.block.BlockStationDoor;
import com.orbitalsunworks.block.BlockStationLamp;
import com.orbitalsunworks.block.BlockStationPart;
import com.orbitalsunworks.block.BlockStationStair;
import com.orbitalsunworks.block.BlockTelescope;
import com.orbitalsunworks.machine.BlockEnergyInput;
import com.orbitalsunworks.machine.BlockOilExtractor;
import com.orbitalsunworks.machine.BlockOilRefiner;
import com.orbitalsunworks.machine.BlockSynthesizer;

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

    public static BlockDispersionRocket blockDispersionRocket;
    public static BlockControlStation blockControlStation;
    public static BlockControlComputer blockControlComputer;
    public static BlockAntennaPort blockAntennaPort;
    public static BlockAntennaTower blockAntennaTower;
    public static BlockTelescope blockTelescope;

    // Máquinas de petróleo e entrada de energia RF
    public static BlockEnergyInput blockEnergyInput;
    public static BlockOilExtractor blockOilExtractor;
    public static BlockOilRefiner blockOilRefiner;
    public static BlockSynthesizer blockSynthesizer;

    // Blocos estruturais e interativos da Estação de Controle
    public static BlockStationPart blockStationPart;
    public static BlockStationStair blockStationStair;
    public static BlockStationDoor blockStationDoor;
    public static BlockStationLamp blockStationLamp;
    public static BlockControlMonitor blockControlMonitor;
    public static BlockControlKeyboard blockControlKeyboard;

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

        blockDispersionRocket = new BlockDispersionRocket();
        blockControlStation = new BlockControlStation();
        blockControlComputer = new BlockControlComputer();
        blockAntennaPort = new BlockAntennaPort();
        blockAntennaTower = new BlockAntennaTower();
        blockTelescope = new BlockTelescope();

        blockEnergyInput = new BlockEnergyInput();
        blockOilExtractor = new BlockOilExtractor();
        blockOilRefiner = new BlockOilRefiner();
        blockSynthesizer = new BlockSynthesizer();

        blockStationPart = new BlockStationPart();
        blockStationStair = new BlockStationStair(blockStationPart, BlockStationPart.ACCENT);
        blockStationDoor = new BlockStationDoor();
        blockStationLamp = new BlockStationLamp();
        blockControlMonitor = new BlockControlMonitor();
        blockControlKeyboard = new BlockControlKeyboard();

        GameRegistry.registerBlock(blockCladded, "blockCladded");
        GameRegistry.registerBlock(blockCraftingStation, "blockCraftingStation");
        GameRegistry.registerBlock(blockComputer, "blockComputer");
        GameRegistry.registerBlock(blockLaunchPlatform, "blockLaunchPlatform");
        GameRegistry.registerBlock(blockRocket, "blockRocket");
        GameRegistry.registerBlock(blockOrbitSatellite, "blockOrbitSatellite");
        GameRegistry.registerBlock(blockNavRocket, "blockNavRocket");
        GameRegistry.registerBlock(blockOrbitFloor, "blockOrbitFloor");

        GameRegistry.registerBlock(blockDispersionRocket, "blockDispersionRocket");
        GameRegistry.registerBlock(blockControlStation, "blockControlStation");
        GameRegistry.registerBlock(blockControlComputer, "blockControlComputer");
        GameRegistry.registerBlock(blockAntennaPort, "blockAntennaPort");
        GameRegistry.registerBlock(blockAntennaTower, "blockAntennaTower");
        GameRegistry.registerBlock(blockTelescope, "blockTelescope");

        GameRegistry.registerBlock(blockEnergyInput, "blockEnergyInput");
        GameRegistry.registerBlock(blockOilExtractor, "blockOilExtractor");
        GameRegistry.registerBlock(blockOilRefiner, "blockOilRefiner");
        GameRegistry.registerBlock(blockSynthesizer, "blockSynthesizer");

        GameRegistry.registerBlock(blockStationPart, ItemStationPart.class, "blockStationPart");
        GameRegistry.registerBlock(blockStationStair, "blockStationStair");
        GameRegistry.registerBlock(blockStationDoor, "blockStationDoor");
        GameRegistry.registerBlock(blockStationLamp, "blockStationLamp");
        GameRegistry.registerBlock(blockControlMonitor, "blockControlMonitor");
        GameRegistry.registerBlock(blockControlKeyboard, "blockControlKeyboard");

        GameRegistry.registerTileEntity(com.orbitalsunworks.craftingstation.TileCraftingStation.class, "tileCraftingStation");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileRocket.class, "tileRocket");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileNavRocket.class, "tileNavRocket");
        GameRegistry.registerTileEntity(com.orbitalsunworks.rocket.TileDispersionRocket.class, "tileDispersionRocket");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileOrbitSatellite.class, "tileOrbitSatellite");
        GameRegistry.registerTileEntity(com.orbitalsunworks.block.TileLaunchPlatform.class, "tileLaunchPlatform");

        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileControlStation.class, "tileControlStation");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileControlComputer.class, "tileControlComputer");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileAntennaPort.class, "tileAntennaPort");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileAntennaTower.class, "tileAntennaTower");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileTelescope.class, "tileTelescope");

        GameRegistry.registerTileEntity(com.orbitalsunworks.machine.TileEnergyInput.class, "tileEnergyInput");
        GameRegistry.registerTileEntity(com.orbitalsunworks.machine.TileOilExtractor.class, "tileOilExtractor");
        GameRegistry.registerTileEntity(com.orbitalsunworks.machine.TileOilRefiner.class, "tileOilRefiner");
        GameRegistry.registerTileEntity(com.orbitalsunworks.machine.TileSynthesizer.class, "tileSynthesizer");

        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileStationDoor.class, "tileStationDoor");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileStationLamp.class, "tileStationLamp");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileControlMonitor.class, "tileControlMonitor");
        GameRegistry.registerTileEntity(com.orbitalsunworks.satellite.TileControlKeyboard.class, "tileControlKeyboard");

        com.orbitalsunworks.world.OSWorldGenerator.register();
    }

    private static BlockModOre registerOre(String name, net.minecraft.item.Item drop,
            int minDrop, int maxDrop, int minXp, int maxXp) {
        BlockModOre ore = new BlockModOre(name, drop, minDrop, maxDrop, minXp, maxXp);
        GameRegistry.registerBlock(ore, name);
        return ore;
    }

    public static class ItemStationPart extends ItemBlockWithMetadata {

        public ItemStationPart(Block block) {
            super(block, block);
        }

        @Override
        public String getUnlocalizedName(ItemStack stack) {
            int dmg = stack != null ? stack.getItemDamage() : 0;
            return this.field_150939_a.getUnlocalizedName() + "." + dmg;
        }
    }
}