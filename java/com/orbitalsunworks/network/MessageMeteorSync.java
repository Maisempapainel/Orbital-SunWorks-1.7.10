package com.orbitalsunworks.network;

import java.util.ArrayList;
import java.util.List;

import com.orbitalsunworks.client.handler.ClientMeteorHandler;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageMeteorSync implements IMessage {

    public boolean active;
    public int remainingTicks;
    public int totalTicks;
    public List<String> targetIds = new ArrayList<String>();
    public List<String> targetNames = new ArrayList<String>();
    public boolean impactFlash;

    public MessageMeteorSync() {
    }

    public MessageMeteorSync(boolean active, int remainingTicks, int totalTicks,
            List<String> targetIds, List<String> targetNames, boolean impactFlash) {
        this.active = active;
        this.remainingTicks = remainingTicks;
        this.totalTicks = totalTicks;
        if (targetIds != null) {
            this.targetIds.addAll(targetIds);
        }
        if (targetNames != null) {
            this.targetNames.addAll(targetNames);
        }
        this.impactFlash = impactFlash;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.active = buf.readBoolean();
        this.remainingTicks = buf.readInt();
        this.totalTicks = buf.readInt();
        this.impactFlash = buf.readBoolean();

        int idCount = Math.max(0, Math.min(64, buf.readInt()));
        this.targetIds.clear();
        for (int i = 0; i < idCount; i++) {
            this.targetIds.add(ByteBufUtils.readUTF8String(buf));
        }

        int nameCount = Math.max(0, Math.min(64, buf.readInt()));
        this.targetNames.clear();
        for (int i = 0; i < nameCount; i++) {
            this.targetNames.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(this.active);
        buf.writeInt(this.remainingTicks);
        buf.writeInt(this.totalTicks);
        buf.writeBoolean(this.impactFlash);

        buf.writeInt(this.targetIds.size());
        for (String id : this.targetIds) {
            ByteBufUtils.writeUTF8String(buf, id != null ? id : "");
        }

        buf.writeInt(this.targetNames.size());
        for (String name : this.targetNames) {
            ByteBufUtils.writeUTF8String(buf, name != null ? name : "");
        }
    }

    public static class Handler implements IMessageHandler<MessageMeteorSync, IMessage> {

        @Override
        public IMessage onMessage(MessageMeteorSync msg, MessageContext ctx) {
            ClientMeteorHandler.update(
                msg.active,
                msg.remainingTicks,
                msg.totalTicks,
                msg.targetIds,
                msg.targetNames,
                msg.impactFlash);
            return null;
        }
    }
}