package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * S->C: a lista de satélites do jogador (resposta ao
 * MessageRequestSatellites). Alimenta a GuiNavSelect: nome, vida útil %,
 * tier de cada satélite DELE.
 */
public class MessageSatelliteList implements IMessage {

    private List<String> uuids = new ArrayList<String>();
    private List<String> names = new ArrayList<String>();
    private List<Float> healths = new ArrayList<Float>();
    private List<Integer> tiers = new ArrayList<Integer>();

    public MessageSatelliteList() {
    }

    public MessageSatelliteList(List<String> uuids, List<String> names,
            List<Float> healths, List<Integer> tiers) {
        this.uuids = uuids;
        this.names = names;
        this.healths = healths;
        this.tiers = tiers;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int n = buf.readInt();
        this.uuids = new ArrayList<String>();
        this.names = new ArrayList<String>();
        this.healths = new ArrayList<Float>();
        this.tiers = new ArrayList<Integer>();
        for (int i = 0; i < n; i++) {
            this.uuids.add(readString(buf));
            this.names.add(readString(buf));
            this.healths.add(buf.readFloat());
            this.tiers.add(buf.readInt());
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.uuids.size());
        for (int i = 0; i < this.uuids.size(); i++) {
            writeString(buf, this.uuids.get(i));
            writeString(buf, this.names.get(i));
            buf.writeFloat(this.healths.get(i));
            buf.writeInt(this.tiers.get(i));
        }
    }

    private static String readString(ByteBuf buf) {
        int len = buf.readInt();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(buf.readChar());
        }
        return sb.toString();
    }

    private static void writeString(ByteBuf buf, String s) {
        buf.writeInt(s.length());
        for (char c : s.toCharArray()) {
            buf.writeChar(c);
        }
    }

    public int getCount() {
        return this.uuids.size();
    }

    public String getUuid(int i) {
        return this.uuids.get(i);
    }

    public String getName(int i) {
        return this.names.get(i);
    }

    public float getHealth(int i) {
        return this.healths.get(i);
    }

    public int getTier(int i) {
        return this.tiers.get(i);
    }

    public static class Handler implements IMessageHandler<MessageSatelliteList, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageSatelliteList message, MessageContext ctx) {
            // Alimenta a GUI aberta (uma das duas formas do mapa orbital).
            com.orbitalsunworks.client.gui.GuiNavSelect.feedList(message);
            com.orbitalsunworks.client.gui.GuiNavRocket.feedList(message);
            return null;
        }
    }
}
