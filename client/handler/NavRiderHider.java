package com.orbitalsunworks.client.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.client.event.RenderPlayerEvent;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Cliente: ESCONDE o boneco montado no foguete de navegação (v0.9.0).
 *
 * O pedido do usuário: na viagem, só a NAVE aparece — o jogador viaja
 * "dentro" dela, INVISÍVEL (antes o boneco ficava sentado com os pés
 * saindo pra fora, cabeça na turbina — feio e irreal).
 *
 * Cancela o RenderPlayerEvent.Pre (e os "specials" tipo capa/elmo)
 * enquanto o jogador local está montado na EntityNavRocket EM VOO.
 * A nave estacionada (parked) não monta ninguém, então nada muda lá.
 */
@SideOnly(Side.CLIENT)
public class NavRiderHider {

    @SubscribeEvent
    public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (this.isHidden(event.entityPlayer)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderSpecialsPre(RenderPlayerEvent.Specials.Pre event) {
        if (this.isHidden(event.entityPlayer)) {
            event.setCanceled(true);
        }
    }

    /** Escondido? O jogador está montado no foguete de navegação. */
    private boolean isHidden(EntityPlayer player) {
        return player != null
            && player.ridingEntity instanceof com.orbitalsunworks.rocket.EntityNavRocket;
    }
}
