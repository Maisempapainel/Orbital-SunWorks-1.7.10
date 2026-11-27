package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Pacote S->C: tremor de câmera SÓ para o jogador que lançou o foguete.
 *
 * A v0.5.x usava "fontes de tremor" por posição que QUALQUER cliente
 * próximo via; em servidor multiplayer isso fazia a tela de todo mundo
 * tremer (bug reportado). Agora o servidor envia este pacote direto ao
 * owner do lançamento (network.sendTo) — nenhum outro cliente recebe.
 *
 * O handler registra a intensidade no LaunchShakeHandler LOCAL do
 * cliente que recebeu (nada de proximidade, nada de broadcast).
 */
public class MessageLaunchShake implements IMessage {

    /** Intensidade do tremor neste tick (0..~1.2; teto 6 no handler). */
    private float intensity;

    public MessageLaunchShake() {
    }

    public MessageLaunchShake(float intensity) {
        this.intensity = intensity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.intensity = buf.readFloat();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeFloat(this.intensity);
    }

    public static class Handler implements IMessageHandler<MessageLaunchShake, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageLaunchShake message, MessageContext ctx) {
            // Registra a intensidade RECEBIDA no handler local — só este
            // cliente treme (ele é o dono do lançamento).
            com.orbitalsunworks.client.handler.LaunchShakeHandler.addLocalShake(
                message.intensity);
            return null;
        }
    }
}
