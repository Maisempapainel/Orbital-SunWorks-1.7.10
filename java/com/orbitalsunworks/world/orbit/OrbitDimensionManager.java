package com.orbitalsunworks.world.orbit;

import net.minecraftforge.common.DimensionManager;

/**
 * Registro da dimensão orbital (v0.6.0).
 *
 * ID DINÂMICO: pega o próximo ID livre do DimensionManager em vez de
 * hardcoded (nunca colide com outros mods — o servidor do usuário pode
 * ter Galacticraft, Twilight Forest etc.). O provider type é fixo
 * (número alto, 100+), o ID da DIMENSÃO é que é dinâmico.
 *
 * Registro em preInit (uma vez); o mundo é keepLoaded=false (só carrega
 * quando alguém visita — não pesa o servidor).
 */
public final class OrbitDimensionManager {

    private static final int PROVIDER_TYPE = 100;

    private static int dimensionId = -1;

    private OrbitDimensionManager() {
    }

    /** Registra provider + dimensão (chamar UMA vez no preInit). */
    public static void register() {
        if (dimensionId >= 0) {
            return; // já registrado
        }
        DimensionManager.registerProviderType(PROVIDER_TYPE, WorldProviderOrbit.class, false);
        dimensionId = DimensionManager.getNextFreeDimId();
        DimensionManager.registerDimension(dimensionId, PROVIDER_TYPE);
        // v0.9.0: teleportes de/para a órbita AGENDADOS (nunca dentro do
        // loop de entidades — é o que crashava o servidor).
        OrbitTransferScheduler.register();
        // v0.10.0: gravidade lunar + chão invisível (anti-void camada 30).
        cpw.mods.fml.common.FMLCommonHandler.instance().bus()
            .register(new OrbitGravity());
    }

    public static int getDimensionId() {
        return dimensionId;
    }

    public static boolean isOrbitWorld(net.minecraft.world.World world) {
        return world != null && world.provider != null
            && world.provider.dimensionId == dimensionId;
    }
}
