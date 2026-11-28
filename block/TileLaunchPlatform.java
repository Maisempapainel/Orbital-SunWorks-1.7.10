package com.orbitalsunworks.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileLaunchPlatform extends TileEntity {

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        if (this.worldObj != null && this.getBlockMetadata() == 2) {
            return AxisAlignedBB.getBoundingBox(
                this.xCoord - 4.0, this.yCoord, this.zCoord - 4.0,
                this.xCoord + 5.0, this.yCoord + 2.5, this.zCoord + 5.0);
        }
        return AxisAlignedBB.getBoundingBox(
            this.xCoord, this.yCoord, this.zCoord,
            this.xCoord + 1.0, this.yCoord + 1.0, this.zCoord + 1.0);
    }
}