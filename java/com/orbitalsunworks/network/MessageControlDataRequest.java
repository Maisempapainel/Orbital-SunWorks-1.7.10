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
import com.orbitalsunworks.satellite.TileAntennaPort;

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
                info.shieldTier = s.getShieldTier();
                info.efficiencyPct = Math.round(s.getEfficiencyBonus() * 100f);
                info.modules = new ArrayList<String>(s.modules);
                out.add(info);
                if (s.health > 0f) {
                    totalGen += s.generationRFt;
                }
            }

            long antennaStored = 0L;
            long antennaMax = TileAntennaPort.CAPACITY_T1;
            int antennaTier = 1;

            TileAntennaPort port = findAntennaPort(world, msg.x, msg.y, msg.z, pid, pname);
            if (port != null) {
                antennaStored = port.getEnergyLong();
                antennaMax = port.getCapacityLong();
                antennaTier = port.getAntennaTier();
            } else {
                TileEntity te = world.getTileEntity(msg.x, msg.y, msg.z);
                if (te != null) {
                    long s = readEnergyLong(te, "getEnergyStored");
                    long m = readEnergyLong(te, "getMaxEnergyStored");
                    if (m > 0L) {
                        antennaStored = s;
                        antennaMax = m;
                    }
                }
            }

            return new MessageControlDataResponse(totalGen, antennaStored, antennaMax, antennaTier, out);
        }

        private static TileAntennaPort findAntennaPort(World world, int refX, int refY, int refZ,
                UUID pid, String pname) {
            TileEntity direct = world.getTileEntity(refX, refY, refZ);
            if (direct instanceof TileAntennaPort) {
                return (TileAntennaPort) direct;
            }
            TileAntennaPort best = null;
            double bestDistSq = Double.MAX_VALUE;
            List loaded = new ArrayList(world.loadedTileEntityList);
            for (Object o : loaded) {
                if (!(o instanceof TileAntennaPort)) {
                    continue;
                }
                TileAntennaPort p = (TileAntennaPort) o;
                boolean ownerOk = (pid != null && pid.equals(p.getOwnerUUID()))
                    || (pname != null && !pname.isEmpty() && pname.equalsIgnoreCase(p.getOwnerName()));
                double dSq = (p.xCoord - refX) * (double) (p.xCoord - refX)
                    + (p.yCoord - refY) * (double) (p.yCoord - refY)
                    + (p.zCoord - refZ) * (double) (p.zCoord - refZ);
                if (ownerOk || dSq <= 1024.0) {
                    if (dSq < bestDistSq) {
                        bestDistSq = dSq;
                        best = p;
                    }
                }
            }
            return best;
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