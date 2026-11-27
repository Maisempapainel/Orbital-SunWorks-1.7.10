package com.orbitalsunworks;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        com.orbitalsunworks.client.render.OSProceduralResourcePack.install();
    }

    @Override
    public void init() {
        super.init();
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityRocket.class,
            new com.orbitalsunworks.client.render.RenderRocket());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileRocket.class,
            new com.orbitalsunworks.client.render.TileRocketRenderer());
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.handler.LaunchShakeHandler());
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.handler.ClientTravelHandler());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            com.orbitalsunworks.client.handler.ClientTravelHandler.OVERLAY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.client.handler.OrbitDeployToast());
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.client.handler.NavRiderHider());
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityNavRocket.class,
            new com.orbitalsunworks.client.render.RenderNavRocket());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileOrbitSatellite.class,
            new com.orbitalsunworks.client.render.TileOrbitSatelliteRenderer());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileNavRocket.class,
            new com.orbitalsunworks.client.render.TileNavRocketRenderer());
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.block.TileLaunchPlatform.class,
            new com.orbitalsunworks.client.render.TileLaunchPlatformRenderer());
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(OSBlocks.blockLaunchPlatform),
            new com.orbitalsunworks.client.render.ItemLaunchPlatformRenderer());
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.gui.GuiDeployCinematic.TickOpener());
    }
}