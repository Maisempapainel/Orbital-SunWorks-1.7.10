package com.orbitalsunworks.satellite;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * TileEntity do Supercomputador da Sala de Controle.
 */
public class TileControlComputer extends TileEntity {

    private UUID ownerUUID;
    private String ownerName = "";

    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name != null ? name : "";
        this.markDirty();
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
    }
}