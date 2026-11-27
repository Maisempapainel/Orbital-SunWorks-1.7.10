package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;
import java.util.UUID;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.block.BlockNavRocket;
import com.orbitalsunworks.rocket.EntityNavRocket;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageTravelToSatellite implements IMessage {

    private String satUuid;
    private int x, y, z;

    public MessageTravelToSatellite() {
    }

    public MessageTravelToSatellite(UUID satId, int x, int y, int z) {
        this.satUuid = satId.toString();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int len = buf.readInt();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(buf.readChar());
        }
        this.satUuid = sb.toString();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.satUuid.length());
        for (char c : this.satUuid.toCharArray()) {
            buf.writeChar(c);
        }
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public static class Handler implements IMessageHandler<MessageTravelToSatellite, IMessage> {

        @Override
        public IMessage onMessage(MessageTravelToSatellite message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            if (OrbitDimensionManager.isOrbitWorld(world)) {
                return null;
            }

            UUID satId;
            try {
                satId = UUID.fromString(message.satUuid);
            } catch (IllegalArgumentException e) {
                return null;
            }

            if (world.getBlock(message.x, message.y, message.z) != OSBlocks.blockNavRocket
                    || world.getBlockMetadata(message.x, message.y, message.z) != 0) {
                OSUtils.sendChat(player, "\u00a7cFoguete n\u00e3o encontrado na plataforma!");
                return null;
            }
            if (player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5) > 64) {
                return null;
            }
            if (player.ridingEntity != null) {
                return null;
            }

            SatelliteManager.Satellite sat = SatelliteManager.get(world).getById(satId);
            if (sat == null || !player.getUniqueID().equals(sat.owner)) {
                OSUtils.sendChat(player, "\u00a7cSat\u00e9lite inv\u00e1lido ou n\u00e3o \u00e9 seu!");
                return null;
            }

            for (Object o : world.loadedEntityList) {
                if (o instanceof EntityNavRocket) {
                    EntityNavRocket r = (EntityNavRocket) o;
                    if (r.getOwnerUUID() != null && r.getOwnerUUID().equals(player.getUniqueID())
                            && !r.isDead && r.phaseOf() != EntityNavRocket.PHASE_PARKED) {
                        OSUtils.sendChat(player, "\u00a7cSua nave j\u00e1 est\u00e1 em viagem!");
                        return null;
                    }
                }
            }

            int homeDim = world.provider.dimensionId;
            double homeX = player.posX;
            double homeY = player.posY;
            double homeZ = player.posZ;
            float homeYaw = player.rotationYaw;
            float homePitch = player.rotationPitch;

            for (int i = BlockNavRocket.HEIGHT - 1; i >= 0; i--) {
                if (world.getBlock(message.x, message.y + i, message.z) == OSBlocks.blockNavRocket) {
                    world.setBlockToAir(message.x, message.y + i, message.z);
                }
            }

            EntityNavRocket rocket = new EntityNavRocket(world,
                message.x + 0.5, message.y, message.z + 0.5, satId,
                homeDim, homeX, homeY, homeZ, homeYaw, homePitch, player.getUniqueID());
            if (!world.spawnEntityInWorld(rocket)) {
                ItemStack give = new ItemStack(OSItems.itemNavRocket);
                if (!player.inventory.addItemStackToInventory(give)) {
                    world.spawnEntityInWorld(new EntityItem(world, player.posX, player.posY, player.posZ, give));
                }
                return null;
            }
            player.mountEntity(rocket);
            OSUtils.sendChat(player, "\u00a7aDestino: \u00a7f" + sat.name + "\u00a7a. Lan\u00e7amento em 3s...");
            return null;
        }
    }
}