package com.orbitalsunworks.satellite;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import com.orbitalsunworks.world.ControlStationBuilder;

/**
 * TileEntity do bloco núcleo da Estação de Controle.
 */
public class TileControlStation extends TileEntity {

    private UUID ownerUUID;
    private String ownerName = "";
    private int facing = 0;

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

    public int getFacing() {
        return this.facing;
    }

    public void setFacing(int facing) {
        this.facing = facing & 3;
        this.markDirty();
    }

    public boolean isCompleted() {
        if (this.worldObj == null || this.worldObj.isRemote) {
            return true;
        }
        return ControlStationBuilder.isCompletedAt(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
    }

    public float getProgress() {
        if (this.worldObj == null || this.worldObj.isRemote) {
            return 1.0f;
        }
        return ControlStationBuilder.getProgressAt(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        String id = tag.getString("OwnerUUID");
        if (!id.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.ownerName = tag.getString("OwnerName");
        this.facing = tag.getInteger("Facing") & 3;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        if (this.ownerUUID != null) {
            tag.setString("OwnerUUID", this.ownerUUID.toString());
        }
        if (this.ownerName != null) {
            tag.setString("OwnerName", this.ownerName);
        }
        tag.setInteger("Facing", this.facing);
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