package com.orbitalsunworks.satellite;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

/**
 * TileEntity da porta automática deslizante com animação de abertura/fechamento.
 */
public class TileStationDoor extends TileEntity {

    private int axis = 0;
    private float openProgress = 0.0f;
    private float prevOpenProgress = 0.0f;
    private boolean targetOpen = false;

    public void setAxis(int axis) {
        this.axis = axis & 1;
        this.markDirty();
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    public int getAxis() {
        return this.axis;
    }

    public float getOpenProgress() {
        return this.openProgress;
    }

    public float getOpenProgress(float partialTicks) {
        return this.getRenderProgress(partialTicks);
    }

    public float getRenderProgress(float partialTicks) {
        return this.prevOpenProgress + (this.openProgress - this.prevOpenProgress) * partialTicks;
    }

    public boolean isFullyOpen() {
        return this.openProgress >= 0.92f;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void updateEntity() {
        if (this.worldObj == null || this.getBlockMetadata() != 0) {
            return;
        }
        this.prevOpenProgress = this.openProgress;

        double r = this.targetOpen ? 2.95 : 2.65;
        AxisAlignedBB box = AxisAlignedBB.getBoundingBox(
            this.xCoord + 0.5 - r, this.yCoord - 1.0, this.zCoord + 0.5 - r,
            this.xCoord + 0.5 + r, this.yCoord + 2.5, this.zCoord + 0.5 + r);
        List players = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, box);
        boolean shouldOpen = players != null && !players.isEmpty();

        if (!this.worldObj.isRemote && shouldOpen != this.targetOpen) {
            this.targetOpen = shouldOpen;
            this.worldObj.playSoundEffect(
                this.xCoord + 0.5, this.yCoord + 1.0, this.zCoord + 0.5,
                "tile.piston.out", 0.45f, shouldOpen ? 1.35f : 0.85f);
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        } else if (this.worldObj.isRemote) {
            this.targetOpen = shouldOpen;
        }

        if (this.targetOpen && this.openProgress < 1.0f) {
            this.openProgress = Math.min(1.0f, this.openProgress + 0.12f);
        } else if (!this.targetOpen && this.openProgress > 0.0f) {
            this.openProgress = Math.max(0.0f, this.openProgress - 0.08f);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.axis = tag.getInteger("Axis") & 1;
        this.targetOpen = tag.getBoolean("TargetOpen");
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Axis", this.axis);
        tag.setBoolean("TargetOpen", this.targetOpen);
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