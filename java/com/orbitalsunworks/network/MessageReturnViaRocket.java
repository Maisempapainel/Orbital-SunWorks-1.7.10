package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayerMP;

import com.orbitalsunworks.rocket.EntityNavRocket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * C->S: botão "VOLTAR À TERRA" da GUI da NAVE ESTACIONADA na órbita
 * (v0.8.0). Diferente do MessageReturnHome (que lê a casa do item),
 * este usa a PRÓPRIA entidade da nave (a casa gravada nela na viagem
 * de ida) e repõe o bloco do foguete na plataforma ao chegar.
 *
 * O servidor valida: jogador na órbita, entidade existe, é dele e está
 * estacionada. Teleporte DIRETO (sem animação — o usuário pediu assim).
 */
public class MessageReturnViaRocket implements IMessage {

    private int rocketEntityId;

    public MessageReturnViaRocket() {
    }

    public MessageReturnViaRocket(int rocketEntityId) {
        this.rocketEntityId = rocketEntityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.rocketEntityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.rocketEntityId);
    }

    public static class Handler implements IMessageHandler<MessageReturnViaRocket, IMessage> {

        @Override
        public IMessage onMessage(MessageReturnViaRocket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!com.orbitalsunworks.world.orbit.OrbitDimensionManager
                    .isOrbitWorld(player.worldObj)) {
                return null; // só tem botão na órbita
            }
            // Acha a nave estacionada pela entity id (enviada pela GUI).
            Object o = player.worldObj.getEntityByID(message.rocketEntityId);
            if (!(o instanceof EntityNavRocket)) {
                com.orbitalsunworks.util.OSUtils.sendChat(player,
                    "\u00a7cNave n\u00e3o encontrada!");
                return null;
            }
            EntityNavRocket rocket = (EntityNavRocket) o;
            if (rocket.phaseOf() != EntityNavRocket.PHASE_PARKED
                || rocket.getOwnerUUID() == null
                || !rocket.getOwnerUUID().equals(player.getUniqueID())) {
                com.orbitalsunworks.util.OSUtils.sendChat(player,
                    "\u00a7cEsta nave n\u00e3o est\u00e1 estacionada pra voc\u00ea!");
                return null;
            }
            EntityNavRocket.sendParkedHome(player, rocket);
            return null;
        }
    }
}
