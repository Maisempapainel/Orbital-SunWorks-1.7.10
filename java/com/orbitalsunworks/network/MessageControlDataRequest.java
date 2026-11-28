package com.orbitalsunworks.network;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.satellite.SatelliteManager.MeteorEvent;
import com.orbitalsunworks.satellite.SatelliteManager.Satellite;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class MessageControlDataRequest implements IMessage {

    public int x;
    public int y;
    public int z;

    public MessageControlDataRequest() {
    }

    public MessageControlDataRequest(int x, int y, int z) {
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

    public static class Handler implements IMessageHandler<MessageControlDataRequest, IMessage> {

        @Override
        public IMessage onMessage(MessageControlDataRequest msg, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) {
                return null;
            }
            World world = player.worldObj;
            SatelliteManager mgr = SatelliteManager.get(world);
            UUID pid = player.getUniqueID();
            String pname = player.getCommandSenderName();

            MeteorEvent ev = mgr.getMeteorEventFor(pid, pname);
            List<Satellite> sats = mgr.getSatellitesOf(pid, pname);
            List<MessageControlDataResponse.SatInfo> out = new ArrayList<MessageControlDataResponse.SatInfo>();
            long totalGen = 0L;

            for (Satellite s : sats) {
                MessageControlDataResponse.SatInfo info = new MessageControlDataResponse.SatInfo();
                info.id = s.id != null ? s.id.toString() : "";
                info.name = s.name != null ? s.name : "OS";
                info.tier = s.tier;
                info.health = s.health;
                info.generationRFt = s.generationRFt;
                info.targeted = ev != null && ev.active && ev.targetSatIds.contains(s.id);
                info.modules = new ArrayList<String>(s.modules);
                out.add(info);
                if (s.health > 0f) {
                    totalGen += s.generationRFt;
                }
            }

            long antennaStored = 0L;
            long antennaMax = 0L;
            TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
            if (te != null) {
                antennaStored = readEnergyLong(te, "getEnergyStored");
                antennaMax = readEnergyLong(te, "getMaxEnergyStored");
            }

            return new MessageControlDataResponse(totalGen, antennaStored, antennaMax, out);
        }

        private static long readEnergyLong(TileEntity te, String methodName) {
            try {
                Method m = te.getClass().getMethod(methodName);
                Object r = m.invoke(te);
                if (r instanceof Number) {
                    return ((Number) r).longValue();
                }
            } catch (Throwable ignored) {
            }
            return 0L;
        }
    }
}