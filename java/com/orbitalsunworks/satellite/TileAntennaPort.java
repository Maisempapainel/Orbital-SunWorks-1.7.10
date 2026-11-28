package com.orbitalsunworks.satellite;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

/**
 * TileEntity extrator de energia da Antena Receptora T1.
 * Armazena até 5 GRF (5.000.000.000 RF) em 'long', recebe geração contínua
 * dos satélites do dono e permite SOMENTE extração por cabos RF / Mekanism Universal Cables.
 */
public class TileAntennaPort extends TileEntity implements IEnergyProvider {

    public static final long CAPACITY_T1 = 5000000000L; // 5 GRF (5 bilhões de RF)
    private static final long MAX_PUSH_PER_FACE = 100000000L;

    private UUID ownerUUID;
    private String ownerName = "";
    private long energy = 0L;
    private long lastIntakeRFt = 0L;
    private int antennaTier = 1;

    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name != null ? name : "";
        this.markDirty();
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public long getEnergyLong() {
        return this.energy;
    }

    public long getCapacityLong() {
        return CAPACITY_T1;
    }

    public long getLastIntakeRFt() {
        return this.lastIntakeRFt;
    }

    public int getAntennaTier() {
        return this.antennaTier;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj == null || this.worldObj.isRemote) {
            return;
        }
        long gen = SatelliteManager.get(this.worldObj)
            .getTotalGenerationRFtOf(this.ownerUUID, this.ownerName);
        this.lastIntakeRFt = gen;
        if (gen > 0 && this.energy < CAPACITY_T1) {
            this.energy = Math.min(CAPACITY_T1, this.energy + gen);
            this.markDirty();
        }

        if (this.energy > 0) {
            ForgeDirection[] dirs = {
                ForgeDirection.DOWN, ForgeDirection.NORTH,
                ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST
            };
            for (ForgeDirection dir : dirs) {
                if (this.energy <= 0) {
                    break;
                }
                int tx = this.xCoord + dir.offsetX;
                int ty = this.yCoord + dir.offsetY;
                int tz = this.zCoord + dir.offsetZ;
                if (!this.worldObj.blockExists(tx, ty, tz)) {
                    continue;
                }
                TileEntity adj = this.worldObj.getTileEntity(tx, ty, tz);
                if (adj instanceof IEnergyReceiver && !(adj instanceof TileAntennaPort)) {
                    IEnergyReceiver recv = (IEnergyReceiver) adj;
                    ForgeDirection opp = dir.getOpposite();
                    if (recv.canConnectEnergy(opp)) {
                        int offer = (int) Math.min(this.energy, MAX_PUSH_PER_FACE);
                        int accepted = recv.receiveEnergy(opp, offer, false);
                        if (accepted > 0) {
                            this.energy -= accepted;
                            this.markDirty();
                        }
                    }
                }
            }
        }

        if (this.worldObj.getTotalWorldTime() % 20 == 0) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return from != ForgeDirection.UP;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        if (from == ForgeDirection.UP || maxExtract <= 0 || this.energy <= 0) {
            return 0;
        }
        int extracted = (int) Math.min(this.energy, maxExtract);
        if (!simulate && extracted > 0) {
            this.energy -= extracted;
            this.markDirty();
        }
        return extracted;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return this.energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) this.energy;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        String owner = tag.getString("Owner");
        if (!owner.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
                this.ownerUUID = null;
            }
        }
        this.ownerName = tag.getString("OwnerName");
        this.energy = Math.max(0L, Math.min(CAPACITY_T1, tag.getLong("Energy")));
        this.lastIntakeRFt = tag.getLong("LastIntake");
        this.antennaTier = Math.max(1, tag.getInteger("Tier"));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (this.ownerUUID != null) {
            tag.setString("Owner", this.ownerUUID.toString());
        }
        if (this.ownerName != null && !this.ownerName.isEmpty()) {
            tag.setString("OwnerName", this.ownerName);
        }
        tag.setLong("Energy", this.energy);
        tag.setLong("LastIntake", this.lastIntakeRFt);
        tag.setInteger("Tier", this.antennaTier);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }
}