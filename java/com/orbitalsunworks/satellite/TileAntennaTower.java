package com.orbitalsunworks.satellite;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TileEntity visual da Torre da Antena Receptora.
 */
public class TileAntennaTower extends TileEntity {

    private UUID ownerUUID;
    private String ownerName = "";

    public void setOwner(UUID uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name != null ? name : "";
        this.markDirty();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - 4.0, this.yCoord - 1.0, this.zCoord - 4.0,
            this.xCoord + 5.0, this.yCoord + 10.0, this.zCoord + 5.0);
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