package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Tremor de câmera durante o lançamento do foguete — v2 (v0.6.0).
 *
 * AULA APRENDIDA (multijogador): a v1 usava "fontes de tremor" por
 * POSIÇÃO que qualquer cliente próximo via — em servidor, a tela de
 * TODO mundo que passasse perto tremia. Regra do usuário: SÓ a tela de
 * quem apertou LANCAR treme.
 *
 * Como funciona agora: o servidor envia MessageLaunchShake direto ao
 * dono do lançamento (network.sendTo — nenhum outro cliente recebe o
 * pacote) e este handler aplica a intensidade RECEBIDA na câmera LOCAL.
 * Sem listas, sem proximidade, sem custo por jogador.
 *
 * O offset é aplicado no RenderTickEvent (antes da renderização) como
 * jitter de pitch/yaw e revertido no fim do mesmo tick.
 */
@SideOnly(Side.CLIENT)
public class LaunchShakeHandler {

    /** Intensidade acumulada recebida NESTE tick (via pacote do servidor). */
    private static float pendingShake = 0f;

    /** Chamado pelo handler do MessageLaunchShake (cliente que lançou). */
    public static void addLocalShake(float intensity) {
        if (intensity > 0.01f) {
            pendingShake = Math.min(pendingShake + intensity, 1.2f);
        }
    }

    private float shakeOffsetX = 0f;
    private float shakeOffsetY = 0f;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            // Consome a intensidade do pacote recebido desde o último tick.
            float shake = pendingShake;
            pendingShake = 0f;

            if (shake > 0.01f && Minecraft.getMinecraft().thePlayer != null) {
                // Jitter em graus: tremedeira forte sem "girar" o jogador
                // de verdade (revertida no fim do mesmo tick).
                this.shakeOffsetX = (float) ((Math.random() - 0.5) * 5.0 * shake);
                this.shakeOffsetY = (float) ((Math.random() - 0.5) * 3.5 * shake);
            } else {
                this.shakeOffsetX = 0f;
                this.shakeOffsetY = 0f;
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START && (this.shakeOffsetX != 0f || this.shakeOffsetY != 0f)) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null && mc.theWorld != null) {
                // Aplica o jitter no ângulo de câmera (efeito clássico de
                // screen shake - a arma/corpo não treme, o olho treme).
                mc.thePlayer.rotationPitch += this.shakeOffsetY;
                mc.thePlayer.rotationYaw += this.shakeOffsetX;
                // Reverte logo em seguida para o jogador não "girar" de
                // verdade acumulando - guardamos para desfazer no fim.
                revertX = this.shakeOffsetX;
                revertY = this.shakeOffsetY;
                reverted = false;
            }
        } else if (event.phase == TickEvent.Phase.END && !reverted) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer != null) {
                mc.thePlayer.rotationPitch -= revertY;
                mc.thePlayer.rotationYaw -= revertX;
                reverted = true;
            }
        }
    }

    private float revertX = 0f;
    private float revertY = 0f;
    private boolean reverted = true;
}
