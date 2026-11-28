package com.orbitalsunworks.network;

import java.util.ArrayList;
import java.util.List;

import com.orbitalsunworks.client.gui.GuiControlRoom;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageControlDataResponse implements IMessage {

    public static List<SatInfo> clientSatellites = new ArrayList<SatInfo>();
    public static long clientTotalRFt = 0L;
    public static long clientAntennaStored = 0L;
    public static long clientAntennaMax = 0L;

    public static class SatInfo {
        public String id = "";
        public String name = "";
        public int tier = 1;
        public float health = 1.0f;
        public long generationRFt = 0L;
        public boolean targeted = false;
        public List<String> modules = new ArrayList<String>();
    }

    public long totalGenerationRFt;
    public long antennaStored;
    public long antennaMax;
    public List<SatInfo> satellites = new ArrayList<SatInfo>();

    public MessageControlDataResponse() {
    }

    public MessageControlDataResponse(long totalGenerationRFt, long antennaStored, long antennaMax, List<SatInfo> satellites) {
        this.totalGenerationRFt = totalGenerationRFt;
        this.antennaStored = antennaStored;
        this.antennaMax = antennaMax;
        if (satellites != null) {
            this.satellites.addAll(satellites);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.totalGenerationRFt = buf.readLong();
        this.antennaStored = buf.readLong();
        this.antennaMax = buf.readLong();
        int count = Math.max(0, Math.min(256, buf.readInt()));
        this.satellites.clear();
        for (int i = 0; i < count; i++) {
            SatInfo s = new SatInfo();
            s.id = ByteBufUtils.readUTF8String(buf);
            s.name = ByteBufUtils.readUTF8String(buf);
            s.tier = buf.readInt();
            s.health = buf.readFloat();
            s.generationRFt = buf.readLong();
            s.targeted = buf.readBoolean();
            int mCount = Math.max(0, Math.min(8, buf.readInt()));
            for (int m = 0; m < mCount; m++) {
                s.modules.add(ByteBufUtils.readUTF8String(buf));
            }
            this.satellites.add(s);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeLong(this.totalGenerationRFt);
        buf.writeLong(this.antennaStored);
        buf.writeLong(this.antennaMax);
        buf.writeInt(this.satellites.size());
        for (SatInfo s : this.satellites) {
            ByteBufUtils.writeUTF8String(buf, s.id != null ? s.id : "");
            ByteBufUtils.writeUTF8String(buf, s.name != null ? s.name : "");
            buf.writeInt(s.tier);
            buf.writeFloat(s.health);
            buf.writeLong(s.generationRFt);
            buf.writeBoolean(s.targeted);
            buf.writeInt(s.modules.size());
            for (String mod : s.modules) {
                ByteBufUtils.writeUTF8String(buf, mod != null ? mod : "");
            }
        }
    }

    public static class Handler implements IMessageHandler<MessageControlDataResponse, IMessage> {

        @Override
        public IMessage onMessage(MessageControlDataResponse msg, MessageContext ctx) {
            clientTotalRFt = msg.totalGenerationRFt;
            clientAntennaStored = msg.antennaStored;
            clientAntennaMax = msg.antennaMax;
            clientSatellites = new ArrayList<SatInfo>(msg.satellites);
            GuiControlRoom.updateClientData(
                msg.totalGenerationRFt,
                msg.antennaStored,
                msg.antennaMax,
                msg.satellites);
            return null;
        }
    }
}