package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import com.orbitalsunworks.rocket.EntityNavRocket;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Cliente: trava do Shift durante o voo + câmera F5 automática + fly na órbita + cinemática in-world.
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
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        boolean riding = mc.thePlayer.ridingEntity instanceof EntityNavRocket;

        // Trava o Shift nos dois momentos do tick (START e END) enquanto estiver
        // montado no foguete de navegação em decolagem.
        if (riding) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
            while (mc.gameSettings.keyBindSneak.isPressed()) {
            }
            mc.thePlayer.setSneaking(false);
            if (mc.thePlayer.movementInput != null) {
                mc.thePlayer.movementInput.sneak = false;
            }
            mc.gameSettings.thirdPersonView = 1;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!riding && this.wasRidingNavRocket) {
            mc.gameSettings.thirdPersonView = 0;
        }
        this.wasRidingNavRocket = riding;

        // ---- Gatilho 1: a nave entrou em WARP -> fade preto ----
        if (riding) {
            int phase = ((EntityNavRocket) mc.thePlayer.ridingEntity).phaseOf();
            if (phase == EntityNavRocket.PHASE_WARP && this.lastKnownPhase != phase) {
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
            OVERLAY.startReveal();
        }

        // ---- Fly na órbita ----
        if (inOrbit && !mc.thePlayer.capabilities.isCreativeMode) {
            if (!mc.thePlayer.capabilities.isFlying) {
                mc.thePlayer.capabilities.allowFlying = true;
                mc.thePlayer.capabilities.isFlying = true;
            }
        } else if (!inOrbit && this.wasInOrbit && !mc.thePlayer.capabilities.isCreativeMode) {
            mc.thePlayer.capabilities.isFlying = false;
            mc.thePlayer.capabilities.allowFlying = false;
        }
        this.wasInOrbit = inOrbit;
    }
}