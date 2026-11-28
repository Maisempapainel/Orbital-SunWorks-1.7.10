package com.orbitalsunworks.rocket;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.orbitalsunworks.block.BlockDispersionRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TileEntity da base do Foguete Dispersor na plataforma.
 */
public class TileDispersionRocket extends TileEntity {

    public UUID ownerUUID;
    public String ownerName = "";

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
        } else {
            this.ownerUUID = null;
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

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - 3.0, this.yCoord - 1.0, this.zCoord - 3.0,
            this.xCoord + 4.0, this.yCoord + BlockDispersionRocket.HEIGHT + 2.0, this.zCoord + 4.0);
    }
}