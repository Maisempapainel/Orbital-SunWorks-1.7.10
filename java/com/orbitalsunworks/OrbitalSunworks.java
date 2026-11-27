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

/**
 * Orbital Sunworks - usina solar orbital para Minecraft 1.7.10.
 *
 * Fluxo do mod: minerar os minerios novos -> fundir lingotes -> montar ligas,
 * circuitos e componentes -> construir a Estacao de Crafting Orbital (15 slots)
 * -> montar o Foguete Tier 1 -> montar a Plataforma de Lancamento 3x3 ->
 * posicionar o foguete no centro -> lancar e colocar o satelite em orbita.
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class OrbitalSunworks {

    @Instance(Reference.MOD_ID)
    public static OrbitalSunworks instance;

    @SidedProxy(clientSide = "com.orbitalsunworks.ClientProxy", serverSide = "com.orbitalsunworks.CommonProxy")
    public static CommonProxy proxy;

    /** Canal de rede usado pelo botao Lancar da GUI do foguete. */
    public static SimpleNetworkWrapper network;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Blocos e itens precisam existir antes de qualquer receita.
        OSBlocks.init();
        OSItems.init();
        network = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.CHANNEL);
        network.registerMessage(MessageLaunchRocket.Handler.class, MessageLaunchRocket.class, 0, Side.SERVER);
        // v0.6.0: cinematic/shake (S->C) e seleção de satélite/viagem/retorno (C->S).
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
        // v0.8.0: retorno pela NAVE ESTACIONADA na órbita (teleporte direto).
        network.registerMessage(com.orbitalsunworks.network.MessageReturnViaRocket.Handler.class,
            com.orbitalsunworks.network.MessageReturnViaRocket.class, 7, Side.SERVER);
        // Dimensão orbital (id dinâmico — nunca colide com outros mods).
        com.orbitalsunworks.world.orbit.OrbitDimensionManager.register();
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
        registerOreDict();
        addFurnaceRecipes();
        // Receitas da estacao de crafting custom (grade 3x5).
        CraftingStationRecipes.init();
        // Receitas que tambem funcionam na crafting table vanilla (3x3).
        OSCrafting.init();
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
        // Entidade da animação de lançamento (obrigatório registrar antes de spawnar).
        cpw.mods.fml.common.registry.EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityRocket.class, "OrbitalRocket", 1, this, 64, 1, false);
        // Foguete de navegação (v0.6.0): tracking maior (o jogador viaja nele).
        cpw.mods.fml.common.registry.EntityRegistry.registerModEntity(
            com.orbitalsunworks.rocket.EntityNavRocket.class, "OrbitalNavRocket", 2, this, 128, 1, false);
        // Desgaste dos satelites por poeira espacial.
        FMLCommonHandler.instance().bus().register(new SatelliteTickHandler());
        // Protecao do trajeto de lancamento (PlaceEvent -> EVENT_BUS).
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.handler.OSLaunchGuard());
        // Fly/camera do lado do servidor: mantém o estado do jogador
        // coerente ao entrar/sair da órbita (o cliente espelha).
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.world.orbit.OrbitFlyHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    // ------------------------------------------------------------------
    // OreDictionary - compatibilidade com outros mods
    // ------------------------------------------------------------------

    /**
     * Cobre, estanho, aluminio e titanio sao registrados com os nomes padrao
     * do OreDictionary ("ingotCopper", "oreCopper" etc). Assim, se outro mod
     * (IC2, Thermal Expansion, Tinkers...) tambem tiver esses materiais, as
     * receitas dos dois mods aceitam os itens de ambos - e o que o usuario
     * chamava de "mesmo ID".
     */
    private void registerOreDict() {
        // Minerios (blocos)
        OreDictionary.registerOre("oreRedmeg", OSBlocks.oreRedmeg);
        OreDictionary.registerOre("oreYellomite", OSBlocks.oreYellomite);
        OreDictionary.registerOre("oreEnergizedCoal", OSBlocks.oreEnergizedCoal);
        OreDictionary.registerOre("oreSilicon", OSBlocks.oreSilicon);
        OreDictionary.registerOre("oreCopper", OSBlocks.oreCopper);
        OreDictionary.registerOre("oreTin", OSBlocks.oreTin);
        OreDictionary.registerOre("oreAluminium", OSBlocks.oreAluminium);
        OreDictionary.registerOre("oreAluminum", OSBlocks.oreAluminium);
        OreDictionary.registerOre("oreTitanium", OSBlocks.oreTitanium);
        // Lingotes
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
        // Outros itens
        OreDictionary.registerOre("itemEnergizedCoal", OSItems.energizedCoal);
        OreDictionary.registerOre("itemCarbonFiber", OSItems.carbonFiber);
        // Silício universal: mesmo nome do IC2/Applied Energistics ("itemSilicon").
        OreDictionary.registerOre("itemSilicon", OSItems.itemSilicon);
    }

    // ------------------------------------------------------------------
    // Fornalha
    // ------------------------------------------------------------------

    private void addFurnaceRecipes() {
        // Minerio -> lingote. O carvao energizado rende 2 por minerio.
        // (addSmelting aceita Item; blocos sao convertidos com getItemFromBlock)
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
        // No 1.7.10 este metodo nao tem nome MCP - e func_151396_a(Item, ItemStack, float).
        FurnaceRecipes.smelting().func_151396_a(net.minecraft.item.Item.getItemFromBlock(ore), result, xp);
    }
}
