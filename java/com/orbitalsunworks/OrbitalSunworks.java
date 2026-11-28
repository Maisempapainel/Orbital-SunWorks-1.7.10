package com.orbitalsunworks;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

import com.orbitalsunworks.command.CommandMeteor;
import com.orbitalsunworks.craftingstation.CraftingStationRecipes;
import com.orbitalsunworks.handler.GuiHandler;
import com.orbitalsunworks.network.MessageControlDataRequest;
import com.orbitalsunworks.network.MessageControlDataResponse;
import com.orbitalsunworks.network.MessageDeployCinematic;
import com.orbitalsunworks.network.MessageEjectModule;
import com.orbitalsunworks.network.MessageLaunchDispersion;
import com.orbitalsunworks.network.MessageLaunchRocket;
import com.orbitalsunworks.network.MessageLaunchShake;
import com.orbitalsunworks.network.MessageMeteorSync;
import com.orbitalsunworks.network.MessageRequestSatellites;
import com.orbitalsunworks.network.MessageReturnHome;
import com.orbitalsunworks.network.MessageReturnViaRocket;
import com.orbitalsunworks.network.MessageSatelliteList;
import com.orbitalsunworks.network.MessageTravelToSatellite;
import com.orbitalsunworks.satellite.SatelliteTickHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
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
        OSItems.init();
        OSBlocks.init();

        network = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.CHANNEL);
        network.registerMessage(MessageLaunchRocket.Handler.class, MessageLaunchRocket.class, 0, Side.SERVER);
        network.registerMessage(MessageLaunchShake.Handler.class, MessageLaunchShake.class, 1, Side.CLIENT);
        network.registerMessage(MessageDeployCinematic.Handler.class, MessageDeployCinematic.class, 2, Side.CLIENT);
        network.registerMessage(MessageRequestSatellites.Handler.class, MessageRequestSatellites.class, 3, Side.SERVER);
        network.registerMessage(MessageSatelliteList.Handler.class, MessageSatelliteList.class, 4, Side.CLIENT);
        network.registerMessage(MessageTravelToSatellite.Handler.class, MessageTravelToSatellite.class, 5, Side.SERVER);
        network.registerMessage(MessageReturnHome.Handler.class, MessageReturnHome.class, 6, Side.SERVER);
        network.registerMessage(MessageReturnViaRocket.Handler.class, MessageReturnViaRocket.class, 7, Side.SERVER);
        network.registerMessage(MessageLaunchDispersion.Handler.class, MessageLaunchDispersion.class, 8, Side.SERVER);
        network.registerMessage(MessageEjectModule.Handler.class, MessageEjectModule.class, 9, Side.SERVER);
        network.registerMessage(MessageMeteorSync.Handler.class, MessageMeteorSync.class, 10, Side.CLIENT);
        network.registerMessage(MessageControlDataRequest.Handler.class, MessageControlDataRequest.class, 11, Side.SERVER);
        network.registerMessage(MessageControlDataResponse.Handler.class, MessageControlDataResponse.class, 12, Side.CLIENT);

        com.orbitalsunworks.world.orbit.OrbitDimensionManager.register();
        proxy.preInit(event);
        registerFallbackLocalizations();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        registerOreDict();
        addFurnaceRecipes();
        CraftingStationRecipes.init();
        OSCrafting.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityRocket.class, "OrbitalRocket", 1, this, 64, 1, false);
        EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityNavRocket.class, "OrbitalNavRocket", 2, this, 128, 1, false);
        EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityDispersionRocket.class, "OrbitalDispersionRocket", 3, this, 64, 1, false);

        FMLCommonHandler.instance().bus().register(new SatelliteTickHandler());
        MinecraftForge.EVENT_BUS.register(new com.orbitalsunworks.handler.OSLaunchGuard());
        FMLCommonHandler.instance().bus().register(new com.orbitalsunworks.world.orbit.OrbitFlyHandler());
        MinecraftForge.EVENT_BUS.register(new com.orbitalsunworks.world.orbit.OrbitProtectionHandler());
        FMLCommonHandler.instance().bus().register(new com.orbitalsunworks.world.orbit.SatelliteCollisionPhysics());
        FMLCommonHandler.instance().bus().register(new com.orbitalsunworks.world.ControlStationBuilder.TickHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMeteor());
    }

    @SuppressWarnings("deprecation")
    private void registerFallbackLocalizations() {
        loc("item.controlStation.name", "Esta\u00e7\u00e3o de Controle", "Control Station");
        loc("item.repairKit.name", "Kit de Reparos", "Repair Kit");
        loc("item.dispersionRocket.name", "Foguete de Dispers\u00e3o", "Dispersion Rocket");
        loc("item.moduleDebrisShield.name", "M\u00f3dulo: Defletor Cin\u00e9tico (Tier 1)", "Module: Kinetic Deflector (Tier 1)");
        loc("item.moduleDebrisShieldT2.name", "M\u00f3dulo: Defletor Cin\u00e9tico (Tier 2)", "Module: Kinetic Deflector (Tier 2)");
        loc("item.moduleDebrisShieldT3.name", "M\u00f3dulo: Defletor Cin\u00e9tico (Tier 3)", "Module: Kinetic Deflector (Tier 3)");
        loc("item.moduleEfficiencyT1.name", "M\u00f3dulo: Amplificador Fotovoltaico (Tier 1)", "Module: Photovoltaic Amplifier (Tier 1)");
        loc("item.moduleEfficiencyT2.name", "M\u00f3dulo: Amplificador Fotovoltaico (Tier 2)", "Module: Photovoltaic Amplifier (Tier 2)");
        loc("item.moduleEfficiencyT3.name", "M\u00f3dulo: Amplificador Fotovoltaico (Tier 3)", "Module: Photovoltaic Amplifier (Tier 3)");

        loc("tile.controlStation.name", "N\u00facleo da Esta\u00e7\u00e3o de Controle", "Control Station Core");
        loc("tile.controlComputer.name", "Terminal de Controle Orbital", "Orbital Control Terminal");
        loc("tile.antennaPort.name", "Porta de Antena RF", "RF Antenna Port");
        loc("tile.antennaTower.name", "Torre de Antena RF", "RF Antenna Tower");
        loc("tile.telescope.name", "Telesc\u00f3pio Orbital", "Orbital Telescope");
        loc("tile.dispersionRocket.name", "Foguete de Dispers\u00e3o", "Dispersion Rocket");
        loc("tile.stationDoor.name", "Porta Met\u00e1lica Autom\u00e1tica", "Automatic Metal Door");
        loc("tile.stationLamp.name", "Lumin\u00e1ria High-Tech de Teto", "High-Tech Ceiling Lamp");
        loc("tile.controlMonitor.name", "Monitor Panor\u00e2mico de Controle", "Panoramic Control Monitor");
        loc("tile.controlKeyboard.name", "Console de Teclado Orbital", "Orbital Keyboard Console");
        loc("tile.stationStair.name", "Escada da Esta\u00e7\u00e3o Orbital", "Orbital Station Stairs");
        loc("tile.stationPart.0.name", "Chassi Blindado da Esta\u00e7\u00e3o", "Station Armored Hull");
        loc("tile.stationPart.1.name", "Painel Met\u00e1lico da Esta\u00e7\u00e3o", "Station Tech Wall");
        loc("tile.stationPart.2.name", "Painel Ciano High-Tech", "High-Tech Cyan Accent");
        loc("tile.stationPart.3.name", "Piso Met\u00e1lico da Esta\u00e7\u00e3o", "Station Metallic Floor");
        loc("tile.stationPart.4.name", "Vidro Blindado Orbital", "Orbital Reinforced Glass");
        loc("tile.stationPart.5.name", "Pilar Estrutural da Esta\u00e7\u00e3o", "Station Structural Pillar");
        loc("tile.stationPart.6.name", "Cobertura da Esta\u00e7\u00e3o Orbital", "Station Roof Plating");
        loc("tile.stationPart.7.name", "Friso Luminoso da Esta\u00e7\u00e3o", "Station Luminous Trim");
    }

    @SuppressWarnings("deprecation")
    private void loc(String key, String ptBR, String enUS) {
        LanguageRegistry.instance().addStringLocalization(key, "pt_BR", ptBR);
        LanguageRegistry.instance().addStringLocalization(key, "en_US", enUS);
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