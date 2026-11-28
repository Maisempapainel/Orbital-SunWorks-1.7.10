package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

import com.orbitalsunworks.rocket.EntityDispersionRocket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Pacote C->S: solicita o lançamento do Foguete Dispersor na plataforma.
 */
public class MessageLaunchDispersion implements IMessage {

    private int x, y, z;

    public MessageLaunchDispersion() {
    }

    public MessageLaunchDispersion(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public static class Handler implements IMessageHandler<MessageLaunchDispersion, IMessage> {
        @Override
        public IMessage onMessage(MessageLaunchDispersion msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null || player.worldObj == null) {
                return null;
            }
            World world = player.worldObj;
            if (player.getDistanceSq(msg.x + 0.5, msg.y + 0.5, msg.z + 0.5) <= 64.0) {
                EntityDispersionRocket.launchFromBlock(world, player, msg.x, msg.y, msg.z);
            }
            return null;
        }
    }
}