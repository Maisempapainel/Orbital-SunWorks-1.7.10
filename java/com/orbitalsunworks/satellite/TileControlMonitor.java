package com.orbitalsunworks.satellite;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileControlMonitor extends TileEntity {

    private int facing = 0; // Direção da parede atrás do monitor (0=+Z, 1=-X, 2=-Z, 3=+X)
    private boolean master = false;

    public int getFacing() {
        return this.facing;
    }

    public boolean isMaster() {
        return this.master;
    }

    public void configure(int facing, boolean master) {
        this.facing = facing & 3;
        this.master = master;
        this.markDirty();
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - 2.0, this.yCoord - 1.0, this.zCoord - 2.0,
            this.xCoord + 3.0, this.yCoord + 3.0, this.zCoord + 3.0);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.facing = tag.getInteger("Facing") & 3;
        this.master = tag.getBoolean("Master");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Facing", this.facing);
        tag.setBoolean("Master", this.master);
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