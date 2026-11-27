package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.block.BlockLaunchPlatform;
import com.orbitalsunworks.block.BlockRocket;
import com.orbitalsunworks.handler.OSLaunchGuard;
import com.orbitalsunworks.rocket.EntityRocket;
import com.orbitalsunworks.rocket.TileRocket;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageLaunchRocket implements IMessage {

    private int x;
    private int y;
    private int z;

    public MessageLaunchRocket() {
    }

    public MessageLaunchRocket(int x, int y, int z) {
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

    public static class Handler implements IMessageHandler<MessageLaunchRocket, IMessage> {

        @Override
        public IMessage onMessage(MessageLaunchRocket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (!(te instanceof TileRocket)) {
                return null;
            }
            TileRocket tile = (TileRocket) te;

            if (player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5) > 64) {
                return null;
            }
            if (!BlockLaunchPlatform.isCompleteStructure(world, message.x, message.y - 1, message.z)) {
                OSUtils.sendChat(player, "\u00a7cO foguete precisa estar no centro de uma plataforma 7x7!");
                return null;
            }
            if (!OSLaunchGuard.isTrajectoryClear(world, message.x, message.y, message.z, new int[1])) {
                OSUtils.sendChat(player, "\u00a7cTrajeto obstru\u00eddo! Remova os blocos acima do foguete para lan\u00e7ar.");
                return null;
            }

            float health = tile.getHealth();
            boolean shield = tile.hasDebrisShield();
            int tier = tile.getTier();
            UUID owner = player.getUniqueID();
            List<String> moduleIds = tile.getModuleIds();

            EntityRocket rocket = new EntityRocket(world,
                message.x + 0.5, message.y, message.z + 0.5, health, shield, owner, moduleIds);
            if (!world.isRemote) {
                world.spawnEntityInWorld(rocket);
                for (int i = BlockRocket.HEIGHT - 1; i >= 0; i--) {
                    if (world.getBlock(message.x, message.y + i, message.z) == OSBlocks.blockRocket) {
                        world.setBlockToAir(message.x, message.y + i, message.z);
                    }
                }
                world.playSoundAtEntity(rocket, "note.pling", 0.8f, 1.2f);
                OSUtils.sendChat(player, "\u00a7aLan\u00e7amento autorizado! Foguete Tier " + tier + " em contagem regressiva...");
            }
            return null;
        }
    }
}