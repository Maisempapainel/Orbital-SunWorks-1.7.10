package com.orbitalsunworks;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.oredict.OreDictionary;

import com.orbitalsunworks.craftingstation.CraftingStationRecipes;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.network.MessageLaunchRocket;
import com.orbitalsunworks.satellite.SatelliteTickHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class OrbitalSunworks {

    @Instance(Reference.MOD_ID)
    public static OrbitalSunworks instance;

    @SidedProxy(clientSide = "com.orbitalsunworks.ClientProxy", serverSide = "com.orbitalsunworks.CommonProxy")
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        OSBlocks.init();
        OSItems.init();

        network = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.CHANNEL);
        network.registerMessage(MessageLaunchRocket.Handler.class, MessageLaunchRocket.class, 0, Side.SERVER);
        network.registerMessage(com.orbitalsunworks.network.MessageLaunchShake.Handler.class,
            com.orbitalsunworks.network.MessageLaunchShake.class, 1, Side.CLIENT);
        network.registerMessage(com.orbitalsunworks.network.MessageDeployCinematic.Handler.class,
            com.orbitalsunworks.network.MessageDeployCinematic.class, 2, Side.CLIENT);
        network.registerMessage(com.orbitalsunworks.network.MessageRequestSatellites.Handler.class,
            com.orbitalsunworks.network.MessageRequestSatellites.class, 3, Side.SERVER);
        network.registerMessage(com.orbitalsunworks.network.MessageSatelliteList.Handler.class,
            com.orbitalsunworks.network.MessageSatelliteList.class, 4, Side.CLIENT);
        network.registerMessage(com.orbitalsunworks.network.MessageTravelToSatellite.Handler.class,
            com.orbitalsunworks.network.MessageTravelToSatellite.class, 5, Side.SERVER);
        network.registerMessage(com.orbitalsunworks.network.MessageReturnHome.Handler.class,
            com.orbitalsunworks.network.MessageReturnHome.class, 6, Side.SERVER);
        network.registerMessage(com.orbitalsunworks.network.MessageReturnViaRocket.Handler.class,
            com.orbitalsunworks.network.MessageReturnViaRocket.class, 7, Side.SERVER);

        com.orbitalsunworks.world.orbit.OrbitDimensionManager.register();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        registerOreDict();
        addFurnaceRecipes();
        CraftingStationRecipes.init();
        OSCrafting.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        cpw.mods.fml.common.registry.EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityRocket.class, "OrbitalRocket", 1, this, 64, 1, false);
        cpw.mods.fml.common.registry.EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityNavRocket.class, "OrbitalNavRocket", 2, this, 128, 1, false);

        FMLCommonHandler.instance().bus().register(new SatelliteTickHandler());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.handler.OSLaunchGuard());
        FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.world.orbit.OrbitFlyHandler());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.world.orbit.OrbitProtectionHandler());
        FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.world.orbit.SatelliteCollisionPhysics());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    private void registerOreDict() {
        OreDictionary.registerOre("oreRedmeg", OSBlocks.oreRedmeg);
        OreDictionary.registerOre("oreYellomite", OSBlocks.oreYellomite);
        OreDictionary.registerOre("oreEnergizedCoal", OSBlocks.oreEnergizedCoal);
        OreDictionary.registerOre("oreSilicon", OSBlocks.oreSilicon);
        OreDictionary.registerOre("oreCopper", OSBlocks.oreCopper);
        OreDictionary.registerOre("oreTin", OSBlocks.oreTin);
        OreDictionary.registerOre("oreAluminium", OSBlocks.oreAluminium);
        OreDictionary.registerOre("oreAluminum", OSBlocks.oreAluminium);
        OreDictionary.registerOre("oreTitanium", OSBlocks.oreTitanium);

        OreDictionary.registerOre("ingotRedmeg", OSItems.ingotRedmeg);
        OreDictionary.registerOre("ingotYellomite", OSItems.ingotYellomite);
        OreDictionary.registerOre("ingotCopper", OSItems.ingotCopper);
        OreDictionary.registerOre("ingotTin", OSItems.ingotTin);
        OreDictionary.registerOre("ingotAluminium", OSItems.ingotAluminium);
        OreDictionary.registerOre("ingotAluminum", OSItems.ingotAluminium);
        OreDictionary.registerOre("ingotTitanium", OSItems.ingotTitanium);
        OreDictionary.registerOre("ingotSteel", OSItems.ingotSteel);
        OreDictionary.registerOre("ingotAluminiumAlloy", OSItems.ingotAluminiumAlloy);
        OreDictionary.registerOre("ingotTitaniumAlloy", OSItems.ingotTitaniumAlloy);
        OreDictionary.registerOre("itemEnergizedCoal", OSItems.energizedCoal);
        OreDictionary.registerOre("itemCarbonFiber", OSItems.carbonFiber);
        OreDictionary.registerOre("itemSilicon", OSItems.itemSilicon);
    }

    private void addFurnaceRecipes() {
        smelt(OSBlocks.oreRedmeg, new ItemStack(OSItems.ingotRedmeg, 1), 0.7f);
        smelt(OSBlocks.oreYellomite, new ItemStack(OSItems.ingotYellomite, 1), 0.7f);
        smelt(OSBlocks.oreCopper, new ItemStack(OSItems.ingotCopper, 1), 0.7f);
        smelt(OSBlocks.oreTin, new ItemStack(OSItems.ingotTin, 1), 0.7f);
        smelt(OSBlocks.oreAluminium, new ItemStack(OSItems.ingotAluminium, 1), 0.7f);
        smelt(OSBlocks.oreTitanium, new ItemStack(OSItems.ingotTitanium, 1), 0.9f);
        smelt(OSBlocks.oreEnergizedCoal, new ItemStack(OSItems.energizedCoal, 2), 0.4f);
        smelt(OSBlocks.oreSilicon, new ItemStack(OSItems.itemSilicon, 1), 0.5f);
    }

    private void smelt(net.minecraft.block.Block ore, ItemStack result, float xp) {
        FurnaceRecipes.smelting().func_151396_a(net.minecraft.item.Item.getItemFromBlock(ore), result, xp);
    }
}