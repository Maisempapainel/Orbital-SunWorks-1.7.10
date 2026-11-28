package com.orbitalsunworks.satellite;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * TileEntity da porta automatizada por aproximação (2.8 blocos).
 */
public class TileStationDoor extends TileEntity {

    private int axis = 0; // 0 = abre ao longo de X, 1 = abre ao longo de Z
    private float openProgress = 0.0f;
    private float prevOpenProgress = 0.0f;
    private boolean wasOpenTarget = false;

    public int getAxis() {
        return this.axis;
    }

    public void setAxis(int axis) {
        this.axis = axis & 1;
        this.markDirty();
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public float getOpenProgress(float partialTicks) {
        return this.prevOpenProgress + (this.openProgress - this.prevOpenProgress) * partialTicks;
    }

    public boolean isPassable() {
        return this.openProgress >= 0.45f;
    }

    @Override
    public void updateEntity() {
        this.prevOpenProgress = this.openProgress;
        if (this.worldObj == null) {
            return;
        }
        EntityPlayer near = this.worldObj.getClosestPlayer(
            this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 0.5, 2.85);
        boolean shouldOpen = (near != null);

        if (!this.worldObj.isRemote && shouldOpen != this.wasOpenTarget) {
            this.wasOpenTarget = shouldOpen;
            this.worldObj.playSoundEffect(
                this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 0.5,
                shouldOpen ? "tile.piston.out" : "tile.piston.in",
                0.32f, shouldOpen ? 1.25f : 0.95f);
        }

        if (shouldOpen) {
            this.openProgress = Math.min(1.0f, this.openProgress + 0.095f);
        } else {
            this.openProgress = Math.max(0.0f, this.openProgress - 0.065f);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.axis = tag.getInteger("Axis") & 1;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Axis", this.axis);
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