package com.orbitalsunworks.machine;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.fluid.OSFluids;

import cofh.api.energy.IEnergyReceiver;

/**
 * TileEntity da Máquina Sintetizadora:
 *  - Entrada RF exclusivamente na face traseira vermelha (8.000 RF/t)
 *  - Tanque interno de Petróleo Refinado (16.000 mB)
 *  - A cada 5.000 mB de Petróleo Refinado produz 1x Kevlar em 10s (200 ticks)
 */
public class TileSynthesizer extends TileEntity implements IInventory, IFluidHandler, IEnergyReceiver {

    public static final int ENERGY_CAPACITY = 800000;
    public static final int MAX_RECEIVE = 80000;
    public static final int RF_PER_TICK = 8000;
    public static final int TANK_CAPACITY = 16000;
    public static final int REFINED_COST_MB = 5000;
    public static final int SYNTH_TICKS = 200;

    private final FluidTank refinedTank = new FluidTank(OSFluids.fluidRefinedOil, 0, TANK_CAPACITY);
    // 0 = balde refinado in, 1 = balde vazio out, 2 = Kevlar out
    private final ItemStack[] slots = new ItemStack[3];

    private int energy = 0;
    private int progress = 0;

    public int clientEnergy = 0;
    public int clientRefined = 0;
    public int clientProgress = 0;

    public ForgeDirection getBackDirection() {
        int meta = this.worldObj != null ? this.getBlockMetadata() : 3;
        int front = (meta >= 2 && meta <= 5) ? meta : 3;
        return ForgeDirection.getOrientation(front).getOpposite();
    }

    @Override
    public void updateEntity() {
        if (this.worldObj == null || this.worldObj.isRemote) {
            return;
        }
        boolean dirty = false;

        // Balde de Petróleo Refinado -> tanque interno
        if (this.slots[0] != null && this.slots[0].getItem() == OSItems.bucketRefinedOil
            && (this.slots[1] == null || (this.slots[1].getItem() == Items.bucket
                && this.slots[1].stackSize < this.slots[1].getMaxStackSize()))
            && this.refinedTank.getFluidAmount() <= TANK_CAPACITY - 1000) {
            this.refinedTank.fill(new FluidStack(OSFluids.fluidRefinedOil, 1000), true);
            this.slots[0] = null;
            if (this.slots[1] == null) {
                this.slots[1] = new ItemStack(Items.bucket);
            } else {
                this.slots[1].stackSize++;
            }
            dirty = true;
        }

        // Síntese de Kevlar: 5.000 mB -> 1 Kevlar
        boolean canOutput = this.slots[2] == null
            || (this.slots[2].getItem() == OSItems.itemKevlar
                && this.slots[2].stackSize < this.slots[2].getMaxStackSize());

        if (this.refinedTank.getFluidAmount() >= REFINED_COST_MB && canOutput && this.energy >= RF_PER_TICK) {
            this.energy -= RF_PER_TICK;
            this.progress++;
            if (this.progress >= SYNTH_TICKS) {
                this.progress = 0;
                this.refinedTank.drain(REFINED_COST_MB, true);
                if (this.slots[2] == null) {
                    this.slots[2] = new ItemStack(OSItems.itemKevlar, 1);
                } else {
                    this.slots[2].stackSize++;
                }
            }
            dirty = true;
        } else if (this.refinedTank.getFluidAmount() < REFINED_COST_MB || !canOutput) {
            if (this.progress != 0) {
                this.progress = 0;
                dirty = true;
            }
        }

        if (dirty) {
            this.markDirty();
        }
    }

    public int getEnergy() {
        return (this.worldObj != null && this.worldObj.isRemote) ? this.clientEnergy : this.energy;
    }

    public int getRefinedAmount() {
        return (this.worldObj != null && this.worldObj.isRemote) ? this.clientRefined : this.refinedTank.getFluidAmount();
    }

    public int getProgress() {
        return (this.worldObj != null && this.worldObj.isRemote) ? this.clientProgress : this.progress;
    }

    // ======================================================================
    // IEnergyReceiver (somente na entrada vermelha traseira)
    // ======================================================================
    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return from == getBackDirection();
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        if (from != getBackDirection() || maxReceive <= 0 || this.energy >= ENERGY_CAPACITY) {
            return 0;
        }
        int acc = Math.min(ENERGY_CAPACITY - this.energy, Math.min(MAX_RECEIVE, maxReceive));
        if (!simulate && acc > 0) {
            this.energy += acc;
            this.markDirty();
        }
        return acc;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return this.energy;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return ENERGY_CAPACITY;
    }

    // ======================================================================
    // IFluidHandler
    // ======================================================================
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource == null || resource.getFluid() != OSFluids.fluidRefinedOil) {
            return 0;
        }
        int f = this.refinedTank.fill(resource, doFill);
        if (doFill && f > 0) {
            this.markDirty();
        }
        return f;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return fluid == null || fluid == OSFluids.fluidRefinedOil;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { this.refinedTank.getInfo() };
    }

    // ======================================================================
    // IInventory
    // ======================================================================
    @Override
    public int getSizeInventory() {
        return this.slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.slots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (this.slots[slot] == null) {
            return null;
        }
        ItemStack res;
        if (this.slots[slot].stackSize <= amount) {
            res = this.slots[slot];
            this.slots[slot] = null;
        } else {
            res = this.slots[slot].splitStack(amount);
            if (this.slots[slot].stackSize == 0) {
                this.slots[slot] = null;
            }
        }
        this.markDirty();
        return res;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack st = this.slots[slot];
        this.slots[slot] = null;
        return st;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.slots[slot] = stack;
        if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
            stack.stackSize = this.getInventoryStackLimit();
        }
        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.orbitalsunworks.synthesizer";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj != null
            && this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
            && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64.0;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0 && stack != null && stack.getItem() == OSItems.bucketRefinedOil;
    }

    public void dropInventory(World world, int x, int y, int z) {
        for (int i = 0; i < this.slots.length; i++) {
            ItemStack st = this.slots[i];
            if (st != null) {
                world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, st.copy()));
                this.slots[i] = null;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.energy = Math.max(0, Math.min(ENERGY_CAPACITY, tag.getInteger("Energy")));
        this.progress = tag.getInteger("Progress");
        this.refinedTank.readFromNBT(tag.getCompoundTag("RefinedTank"));

        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int s = itemTag.getByte("Slot") & 255;
            if (s >= 0 && s < this.slots.length) {
                this.slots[s] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Energy", this.energy);
        tag.setInteger("Progress", this.progress);
        NBTTagCompound rTag = new NBTTagCompound();
        this.refinedTank.writeToNBT(rTag);
        tag.setTag("RefinedTank", rTag);

        NBTTagList list = new NBTTagList();
        for (int i = 0; i < this.slots.length; i++) {
            if (this.slots[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                this.slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
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