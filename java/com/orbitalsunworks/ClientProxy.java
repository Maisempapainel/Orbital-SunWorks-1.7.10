package com.orbitalsunworks;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Proxy do cliente: renderizadores e coisas visuais.
 */
public class ClientProxy extends CommonProxy {

    @Override
    public void init() {
        super.init();
        // Renderizador da ENTIDADE do foguete (animação de lançamento).
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityRocket.class,
            new com.orbitalsunworks.client.render.RenderRocket());
        // Renderizador do BLOCO do foguete (posicionado na plataforma).
        // Sem isto o foguete fica invisível (o bloco usa render especial).
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileRocket.class,
            new com.orbitalsunworks.client.render.TileRocketRenderer());
        // Tremor de câmera nos lançamentos (v2: local, por pacote).
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.handler.LaunchShakeHandler());
        // F5 automático + fly espelhado na órbita + gatilhos da
        // cinemática IN-WORLD (fade da subida / revelação da chegada).
        cpw.mods.fml.common.FMLCommonHandler.instance().bus().register(
            new com.orbitalsunworks.client.handler.ClientTravelHandler());
        // v0.10.0: overlays da cinemática in-world (EVENT_BUS — o Render
        // GameOverlayEvent dispara lá, a lição do NavRiderHider).
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            com.orbitalsunworks.client.handler.ClientTravelHandler.OVERLAY);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.client.handler.OrbitDeployToast());
        // v0.10.0: jogador INVISÍVEL na viagem. FIX do bug "Steve sentado
        // aparecendo pra fora do foguete": RenderPlayerEvent dispara no
        // MINECRAFTFORGE.EVENT_BUS — registrar no bus do FML (como era)
        // NUNCA dispara o handler (era isso que o boneco visível).
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(
            new com.orbitalsunworks.client.handler.NavRiderHider());
        // Foguete de navegação (render da entidade em voo).
        cpw.mods.fml.client.registry.RenderingRegistry.registerEntityRenderingHandler(
            com.orbitalsunworks.rocket.EntityNavRocket.class,
            new com.orbitalsunworks.client.render.RenderNavRocket());
        // Estrutura do satélite na órbita (TESR: painéis + antena).
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.satellite.TileOrbitSatellite.class,
            new com.orbitalsunworks.client.render.TileOrbitSatelliteRenderer());
        // Foguete de navegação POSICIONADO (TESR multiblock, igual ao T1).
        cpw.mods.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
            com.orbitalsunworks.rocket.TileNavRocket.class,
            new com.orbitalsunworks.client.render.TileNavRocketRenderer());
    }
}
