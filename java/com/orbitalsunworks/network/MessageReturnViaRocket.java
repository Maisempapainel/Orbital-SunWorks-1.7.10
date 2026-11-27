package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.rocket.EntityNavRocket;
import com.orbitalsunworks.rocket.TileNavRocket;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageReturnViaRocket implements IMessage {

    private int x, y, z;

    public MessageReturnViaRocket() {
    }

    public MessageReturnViaRocket(int x, int y, int z) {
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

    public static class Handler implements IMessageHandler<MessageReturnViaRocket, IMessage> {

        @Override
        public IMessage onMessage(MessageReturnViaRocket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;
            if (!OrbitDimensionManager.isOrbitWorld(world)) {
                return null;
            }
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (!(te instanceof TileNavRocket)) {
                OSUtils.sendChat(player, "\u00a7cNave n\u00e3o encontrada!");
                return null;
            }
            TileNavRocket tile = (TileNavRocket) te;
            if (player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5) > 64) {
                return null;
            }
            if (tile.ownerUUID != null && !tile.ownerUUID.equals(player.getUniqueID())) {
                OSUtils.sendChat(player, "\u00a7cEsta nave n\u00e3o \u00e9 sua!");
                return null;
            }
            EntityNavRocket.returnFromOrbitBlock(player, tile);
            return null;
        }
    }
}