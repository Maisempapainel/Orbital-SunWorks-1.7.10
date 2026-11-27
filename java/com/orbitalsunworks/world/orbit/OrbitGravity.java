package com.orbitalsunworks.world.orbit;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * GRAVIDADE LUNAR da órbita (v0.10.0).
 *
 * O pedido: "lá a gravidade é pra ser a da lua ou parecido, quase zero,
 * então se eu parar de voar o meu personagem simplesmente cai extremamente
 * lento, e não despenca."
 *
 * Como o 1.7.10 não tem hook de gravidade por dimensão, este handler
 * roda no FIM do tick do servidor e REFATORA a física que o vanilla já
 * aplicou nas entidades da dimensão orbital:
 *  - queda: motionY escalado pelo GRAVITY_FACTOR (16% — quase lua);
 *  - fallDistance zerada continuamente (nunca acumula dano de queda);
 *  - itens dropados também caem devagar (nada "despenca" no espaço).
 *
 * Roda no END do tick: o vanilla já moveu a entidade; o fator entra na
 * PRÓXIMA aceleração (a correção é suave, sem teleporte).
 */
public class OrbitGravity {

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) {
            return;
        }
        WorldServer orbit = net.minecraftforge.common.DimensionManager.getWorld(
            OrbitDimensionManager.getDimensionId());
        if (orbit == null) {
            return; // dimensão não carregada: ninguém lá
        }
        float factor = WorldProviderOrbit.GRAVITY_FACTOR;

        // Cópia da lista (o update pode mover entidades entre mundos).
        List entities = new java.util.ArrayList(orbit.loadedEntityList);
        for (Object o : entities) {
            if (!(o instanceof Entity)) {
                continue;
            }
            Entity e = (Entity) o;
            if (e.isDead) {
                continue;
            }
            // Só quem está CAINDO de verdade (motionY negativo, sem fly).
            boolean flying = false;
            if (e instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP) e;
                flying = p.capabilities.isFlying;
            }
            if (!flying && e.motionY < 0.0) {
                // Escala a queda: 16% da velocidade do vanilla. Quem caiu
                // 1 tick de queda normal (0.08/t) passa a cair a ~0.013/t
                // — leva ~5s pra cair 1 bloto e ~30s pra descer 30 (da
                // estrutura ao chão invisível). "Cai extremamente lento".
                e.motionY *= factor;
                // Limita a velocidade terminal lunar (nunca "despenca").
                if (e.motionY < -0.10) {
                    e.motionY = -0.10;
                }
            }
            // SEM dano de queda na órbita (o chão invisível da camada 30
            // evita o void; aqui garantimos que POUSAR nele não dói).
            if (e instanceof EntityLivingBase) {
                e.fallDistance = 0f;
            }
            // Itens dropados também flutuam lento (visual do espaço).
            if (e instanceof EntityItem && e.motionY < -0.05) {
                e.motionY = -0.05;
            }
        }
    }
}
