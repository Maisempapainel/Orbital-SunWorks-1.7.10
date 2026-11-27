package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayerMP;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * C->S: botão "VOLTAR À TERRA" na GUI da estrutura orbital. O servidor
 * valida (jogador está na dimensão orbital) e teleporta de volta para as
 * coords de casa gravadas no item do foguete de navegação (fallback:
 * spawn do overworld).
 */
public class MessageReturnHome implements IMessage {

    public MessageReturnHome() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageReturnHome, IMessage> {

        @Override
        public IMessage onMessage(MessageReturnHome message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (!com.orbitalsunworks.world.orbit.OrbitDimensionManager
                    .isOrbitWorld(player.worldObj)) {
                return null; // só tem botão na órbita
            }
            com.orbitalsunworks.rocket.EntityNavRocket.sendPlayerHome(player);
            return null;
        }
    }
}
