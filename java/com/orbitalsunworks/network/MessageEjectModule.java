package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;

import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Pacote C->S: ejeta fisicamente um módulo do satélite na dimensão orbital.
 */
public class MessageEjectModule implements IMessage {

    private int x, y, z, moduleIndex;

    public MessageEjectModule() {
    }

    public MessageEjectModule(int x, int y, int z, int moduleIndex) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.moduleIndex = moduleIndex;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
        this.moduleIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
        buf.writeInt(this.moduleIndex);
    }

    public static class Handler implements IMessageHandler<MessageEjectModule, IMessage> {
        @Override
        public IMessage onMessage(MessageEjectModule msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.worldObj == null) {
                return null;
            }
            if (player.getDistanceSq(msg.x + 0.5, msg.y + 0.5, msg.z + 0.5) <= 64.0) {
                TileEntity te = player.worldObj.getTileEntity(msg.x, msg.y, msg.z);
                if (te instanceof TileOrbitSatellite) {
                    ((TileOrbitSatellite) te).ejectModuleAt(msg.moduleIndex);
                }
            }
            return null;
        }
    }
}