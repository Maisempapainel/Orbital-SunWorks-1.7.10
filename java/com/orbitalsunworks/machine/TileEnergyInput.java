package com.orbitalsunworks.machine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.api.energy.IEnergyReceiver;

/**
 * TileEntity do bloco vermelho de entrada RF.
 * Compatível com cabos RF de qualquer mod via CoFH IEnergyReceiver.
 */
public class TileEnergyInput extends TileEntity implements IEnergyReceiver {

    public static final int CAPACITY = 250000;
    public static final int MAX_RECEIVE = 50000;

    private int energy = 0;

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        if (maxReceive <= 0 || this.energy >= CAPACITY) {
            return 0;
        }
        int accepted = Math.min(CAPACITY - this.energy, Math.min(MAX_RECEIVE, maxReceive));
        if (!simulate && accepted > 0) {
            this.energy += accepted;
            this.markDirty();
        }
        return accepted;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return CAPACITY;
    }

    public boolean consumeEnergy(int amount) {
        if (this.energy < amount) {
            return false;
        }
        this.energy -= amount;
        this.markDirty();
        return true;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj == null || this.worldObj.isRemote || this.energy <= 0) {
            return;
        }
        // Se encostado em Refinador ou Sintetizadora pela entrada vermelha traseira, repassa RF
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (this.energy <= 0) {
                break;
            }
            int tx = this.xCoord + dir.offsetX;
            int ty = this.yCoord + dir.offsetY;
            int tz = this.zCoord + dir.offsetZ;
            if (!this.worldObj.blockExists(tx, ty, tz)) {
                continue;
            }
            TileEntity te = this.worldObj.getTileEntity(tx, ty, tz);
            if (te instanceof TileOilRefiner || te instanceof TileSynthesizer) {
                IEnergyReceiver recv = (IEnergyReceiver) te;
                ForgeDirection opp = dir.getOpposite();
                if (recv.canConnectEnergy(opp)) {
                    int sent = recv.receiveEnergy(opp, Math.min(this.energy, MAX_RECEIVE), false);
                    if (sent > 0) {
                        this.energy -= sent;
                        this.markDirty();
                    }
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.energy = Math.max(0, Math.min(CAPACITY, tag.getInteger("Energy")));
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Energy", this.energy);
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