package com.orbitalsunworks.world.orbit;

import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;

/**
 * Dimensão ORBITAL — o "espaço" do Orbital Sunworks (v0.6.0).
 *
 * Aparência: céu PRETO com estrelas distantes e o PLANETA TERRA embaixo
 * (renderizado pelo OrbitSkyRenderer no cliente). Mundo VAZIO (sem
 * terreno) — a estrutura do satélite é spawnada em coords fixas perto
 * do spawn quando o jogador visita.
 *
 * Regras:
 * - sem respawn aqui (cai pro overworld ao morrer);
 * - gravidade ~40% menor (flutuar leve, não cair como pedra);
 * - fly liberado SOMENTE nesta dimensão (OrbitFlyHandler);
 * - fog escuro curto (o "horizonte" é o vazio).
 */
public class WorldProviderOrbit extends WorldProvider {

    @Override
    public void registerWorldChunkManager() {
        // Sem chunk manager especial: mundo void.
        this.worldChunkMgr = new net.minecraft.world.biome.WorldChunkManagerHell(
            com.orbitalsunworks.world.orbit.BiomeGenOrbit.INSTANCE, 0.0f);
        this.dimensionId = OrbitDimensionManager.getDimensionId();
    }

    /** Céu da órbita: espaço + estrelas + Terra embaixo. O Forge chama
     *  isto NO LUGAR do renderSky do vanilla (client-only class carregada
     *  apenas no cliente — o override é seguro no server pois o Forge
     *  nunca invoca em mundo sem clientes). */
    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public net.minecraftforge.client.IRenderHandler getSkyRenderer() {
        return new com.orbitalsunworks.client.render.OrbitSkyRenderer();
    }

    @Override
    public IChunkProvider createChunkGenerator() {
        return new ChunkProviderOrbit(this.worldObj);
    }

    @Override
    public String getDimensionName() {
        return "Órbita Terrestre";
    }

    @Override
    public boolean canRespawnHere() {
        return false; // morreu na órbita? respawn na Terra
    }

    @Override
    public int getRespawnDimension(net.minecraft.entity.player.EntityPlayerMP player) {
        return 0;
    }

    @Override
    public boolean isSurfaceWorld() {
        return false;
    }

    /** Céu sempre escuro (o renderer desenha Terra/estrelas). */
    @Override
    public Vec3 getFogColor(float angle, float partialTicks) {
        return Vec3.createVectorHelper(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isSkyColored() {
        return false;
    }

    /** v0.8.0: cor do céu PRETO PURO (o "breu" pedido — antes usava a
     *  cor de fog que podia acinzentar o horizonte). */
    @Override
    public Vec3 getSkyColor(net.minecraft.entity.Entity cameraEntity, float partialTicks) {
        return Vec3.createVectorHelper(0.0, 0.0, 0.0);
    }

    /** v0.8.0: SEM sol/lua (o espaço é o renderer próprio). */
    @Override
    public float getSunBrightness(float par1) {
        return 0.0f;
    }

    /** v0.8.0: nuvens do vanilla OFF (altura negativa = nunca desenha). */
    @Override
    public float getCloudHeight() {
        return -1.0f;
    }

    /** Estrelas sempre visíveis (noite permanente no render). */
    @Override
    public float calculateCelestialAngle(long worldTime, float partialTicks) {
        return 0.0f; // "meia-noite" eterna -> céu escuro
    }

    /** v0.8.0: SEM estrelas do VANILLA (as estrelas são do nosso
     *  renderer, com pulso; as do vanilla fariam "dobrar"). */
    @Override
    public float getStarBrightness(float par1) {
        return 0.0f;
    }

    @Override
    public boolean canCoordinateBeSpawn(int x, int z) {
        return true;
    }

    @Override
    public String getWelcomeMessage() {
        return "Entrando em órbita";
    }

    @Override
    public String getDepartMessage() {
        return "Retornando à Terra";
    }

    @Override
    public double getMovementFactor() {
        return 1.0;
    }

    // ------------------------------------------------------------------
    // v0.10.0 — GRAVIDADE LUNAR (o pedido: "gravidade da lua ou parecido,
    // quase zero, então se eu parar de voar o meu personagem simplesmente
    // cai extremamente lento, e não despenca").
    // O 1.7.10 não tem hook de gravidade por dimensão — o OrbitGravity
    // (server tick) aplica a física direto nas entidades desta dimensão.
    // ------------------------------------------------------------------

    /** Fração da gravidade terrestre (~16% — próximo da Lua: 17%). */
    public static final float GRAVITY_FACTOR = 0.16f;
}
