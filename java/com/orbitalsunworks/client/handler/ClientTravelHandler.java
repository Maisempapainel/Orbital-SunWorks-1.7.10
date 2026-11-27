package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Cliente: câmera F5 automática + fly na órbita + CINEMÁTICA IN-WORLD
 * (v0.10.0).
 *
 * - F5: quando o jogador local MONTA na EntityNavRocket, muda para a
 *   terceira pessoa (igual Galacticraft — ver a NAVE subindo de fora).
 *   Ao desmontar, volta para primeira pessoa.
 * - FLY: na dimensão orbital o fly fica SEMPRE ativo; ao sair, volta ao
 *   estado normal.
 * - CINEMÁTICA (novo): o overlay OrbitArrivalOverlay cuida do visual —
 *   aqui só DETECTAMOS os gatilhos:
 *     * a nave real entrou em PHASE_WARP (a phase agora viaja no
 *       DataWatcher — o cliente sabe) -> começa o FADE PRETO da subida;
 *     * o jogador apareceu na dimensão orbital -> REVELAÇÃO (a cortina
 *       abre mostrando a Terra imensa do céu de verdade).
 */
@SideOnly(Side.CLIENT)
public class ClientTravelHandler {

    private boolean wasRidingNavRocket = false;
    private boolean wasInOrbit = false;
    private int lastKnownPhase = -1;

    /** O overlay da cinemática (instância única, registrada no proxy). */
    public static final OrbitArrivalOverlay OVERLAY = new OrbitArrivalOverlay();

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        // ---- Câmera na viagem ----
        // v0.10.0: o jogador está INVISÍVEL (NavRiderHider) — o F5 mostra
        // só a NAVE de fora (o usuário quer ver a nave, não o boneco).
        boolean riding = mc.thePlayer.ridingEntity instanceof
            com.orbitalsunworks.rocket.EntityNavRocket;
        if (riding && !this.wasRidingNavRocket) {
            mc.gameSettings.thirdPersonView = 1; // deixa ver a NAVE
        } else if (!riding && this.wasRidingNavRocket) {
            mc.gameSettings.thirdPersonView = 0; // volta pra primeira pessoa
        }
        this.wasRidingNavRocket = riding;

        // ---- Gatilho 1: a nave entrou em WARP -> fade preto ----
        if (riding) {
            int phase = ((com.orbitalsunworks.rocket.EntityNavRocket)
                mc.thePlayer.ridingEntity).phaseOf();
            if (phase == com.orbitalsunworks.rocket.EntityNavRocket.PHASE_WARP
                && this.lastKnownPhase != phase) {
                OVERLAY.startFade();
            }
            this.lastKnownPhase = phase;
        } else {
            this.lastKnownPhase = -1;
        }

        // ---- Gatilho 2: CHEGOU na órbita -> revelação ----
        boolean inOrbit = com.orbitalsunworks.world.orbit.OrbitDimensionManager
            .isOrbitWorld(mc.theWorld);
        if (inOrbit && !this.wasInOrbit) {
            // A chegada de quem VIAJOU já é coberta pelo fim do fade;
            // este gatilho cobre QUALQUER chegada (inclusive re-login).
            OVERLAY.startReveal();
        }

        // ---- Fly na órbita ----
        if (inOrbit && !mc.thePlayer.capabilities.isCreativeMode) {
            // Fly sempre ativo na órbita (o servidor também ativa; o
            // cliente espelha para o movimento responder na hora).
            if (!mc.thePlayer.capabilities.isFlying) {
                mc.thePlayer.capabilities.allowFlying = true;
                mc.thePlayer.capabilities.isFlying = true;
            }
        } else if (!inOrbit && this.wasInOrbit && !mc.thePlayer.capabilities.isCreativeMode) {
            // Saiu da órbita: fly off (o servidor manda sendPlayerAbilities
            // que re-sincroniza; aqui é só o fallback local).
            mc.thePlayer.capabilities.isFlying = false;
            mc.thePlayer.capabilities.allowFlying = false;
        }
        this.wasInOrbit = inOrbit;
    }
}
