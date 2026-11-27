package com.orbitalsunworks.rocket;

import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.orbitalsunworks.block.BlockNavRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileNavRocket extends TileEntity {

    public int homeDim;
    public double homeX, homeY, homeZ;
    public float homeYaw, homePitch;
    public boolean homeSet = false;
    public boolean orbitReturn = false;
    public UUID ownerUUID;
    public String targetSatName = "";

    public void setHome(int dim, double x, double y, double z) {
        this.setHome(dim, x, y, z, 0f, 0f);
    }

    public void setHome(int dim, double x, double y, double z, float yaw, float pitch) {
        this.homeDim = dim;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.homeYaw = yaw;
        this.homePitch = pitch;
        this.homeSet = true;
        this.markDirty();
    }

    public void setOrbitReturn(UUID owner, String satName, int dim, double x, double y, double z, float yaw, float pitch) {
        this.orbitReturn = true;
        this.ownerUUID = owner;
        this.targetSatName = satName != null ? satName : "";
        this.setHome(dim, x, y, z, yaw, pitch);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.homeSet = tag.getBoolean("HomeSet");
        this.orbitReturn = tag.getBoolean("OrbitReturn");
        this.targetSatName = tag.getString("TargetSatName");
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
        if (this.homeSet) {
            this.homeDim = tag.getInteger("HomeDim");
            this.homeX = tag.getDouble("HomeX");
            this.homeY = tag.getDouble("HomeY");
            this.homeZ = tag.getDouble("HomeZ");
            this.homeYaw = tag.getFloat("HomeYaw");
            this.homePitch = tag.getFloat("HomePitch");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("HomeSet", this.homeSet);
        tag.setBoolean("OrbitReturn", this.orbitReturn);
        if (this.targetSatName != null && !this.targetSatName.isEmpty()) {
            tag.setString("TargetSatName", this.targetSatName);
        }
        if (this.ownerUUID != null) {
            tag.setString("Owner", this.ownerUUID.toString());
        }
        if (this.homeSet) {
            tag.setInteger("HomeDim", this.homeDim);
            tag.setDouble("HomeX", this.homeX);
            tag.setDouble("HomeY", this.homeY);
            tag.setDouble("HomeZ", this.homeZ);
            tag.setFloat("HomeYaw", this.homeYaw);
            tag.setFloat("HomePitch", this.homePitch);
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
            this.xCoord - 1, this.yCoord, this.zCoord - 1,
            this.xCoord + 2, this.yCoord + BlockNavRocket.HEIGHT + 1, this.zCoord + 2);
    }
}