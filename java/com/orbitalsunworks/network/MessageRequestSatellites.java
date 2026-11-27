package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;

import com.orbitalsunworks.satellite.SatelliteManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * C->S: pede a lista de satélites DO JOGADOR (para a GUI do foguete de
 * navegação). O servidor responde com MessageSatelliteList (S->C) contendo
 * só os satélites do dono — nenhum outro jogador recebe nada.
 */
public class MessageRequestSatellites implements IMessage {

    public MessageRequestSatellites() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<MessageRequestSatellites, IMessage> {

        @Override
        public IMessage onMessage(MessageRequestSatellites message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            List<SatelliteManager.Satellite> sats =
                SatelliteManager.get(player.worldObj).getSatellitesOf(player.getUniqueID());

            List<String> uuids = new ArrayList<String>();
            List<String> names = new ArrayList<String>();
            List<Float> healths = new ArrayList<Float>();
            List<Integer> tiers = new ArrayList<Integer>();
            for (SatelliteManager.Satellite sat : sats) {
                uuids.add(sat.id.toString());
                names.add(sat.name);
                healths.add(sat.health);
                tiers.add(sat.tier);
            }
            return new MessageSatelliteList(uuids, names, healths, tiers);
        }
    }
}
