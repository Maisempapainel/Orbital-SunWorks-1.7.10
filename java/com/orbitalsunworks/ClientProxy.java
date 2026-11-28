package com.orbitalsunworks;

import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
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

        RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityRocket.class,
            new com.orbitalsunworks.client.render.RenderRocket());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileRocket.class,
            new com.orbitalsunworks.client.render.TileRocketRenderer());

        RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityNavRocket.class,
            new com.orbitalsunworks.client.render.RenderNavRocket());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileNavRocket.class,
            new com.orbitalsunworks.client.render.TileNavRocketRenderer());

        RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityDispersionRocket.class,
            new com.orbitalsunworks.client.render.RenderDispersionRocket());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileDispersionRocket.class,
            new com.orbitalsunworks.client.render.TileDispersionRocketRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileOrbitSatellite.class,
            new com.orbitalsunworks.client.render.TileOrbitSatelliteRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.block.TileLaunchPlatform.class,
            new com.orbitalsunworks.client.render.TileLaunchPlatformRenderer());
        MinecraftForgeClient.registerItemRenderer(
            Item.getItemFromBlock(OSBlocks.blockLaunchPlatform),
            new com.orbitalsunworks.client.render.ItemLaunchPlatformRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileAntennaTower.class,
            new com.orbitalsunworks.client.render.TileAntennaTowerRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileTelescope.class,
            new com.orbitalsunworks.client.render.TileTelescopeRenderer());

        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileStationDoor.class,
            new com.orbitalsunworks.client.render.TileStationDoorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileStationLamp.class,
            new com.orbitalsunworks.client.render.TileStationLampRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileControlMonitor.class,
            new com.orbitalsunworks.client.render.TileControlMonitorRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileControlKeyboard.class,
            new com.orbitalsunworks.client.render.TileControlKeyboardRenderer());

        FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.handler.LaunchShakeHandler());
        FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.handler.ClientTravelHandler());
        MinecraftForge.EVENT_BUS.register(
            com.orbitalsunworks.client.handler.ClientTravelHandler.OVERLAY);
        MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.client.handler.OrbitDeployToast());
        MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.client.handler.NavRiderHider());
        FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.gui.GuiDeployCinematic.TickOpener());

        com.orbitalsunworks.client.handler.ClientMeteorHandler meteorHandler =
            new com.orbitalsunworks.client.handler.ClientMeteorHandler();
        FMLCommonHandler.instance().bus().register(meteorHandler);
        MinecraftForge.EVENT_BUS.register(meteorHandler);
    }
}