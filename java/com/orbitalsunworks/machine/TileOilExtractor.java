package com.orbitalsunworks.machine;

import net.minecraft.block.Block;
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
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.fluid.OSFluids;
import com.orbitalsunworks.world.OilFieldManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * TileEntity do Extrator de Petróleo (Gangorra 3x3).
 * Requer:
 *  - 7x Bloco de Carapaça + 1x Entrada de Energia RF (cardeal) no anel 3x3
 *  - Todo o 3x3 contido dentro de 1 única chunk
 *  - Jazida de petróleo válida na chunk (1 em 20)
 * Consumo: 2.500 RF/t | Produção: 1 mB/t de Petróleo Bruto.
 */
public class TileOilExtractor extends TileEntity implements IInventory, IFluidHandler {

    public static final int STATUS_INCOMPLETE = 0;
    public static final int STATUS_CROSS_CHUNK = 1;
    public static final int STATUS_NO_OIL = 2;
    public static final int STATUS_NO_POWER = 3;
    public static final int STATUS_FULL = 4;
    public static final int STATUS_EXTRACTING = 5;

    public static final int TANK_CAPACITY = 16000;
    public static final int RF_PER_TICK = 2500;
    public static final int OIL_PER_TICK = 1;

    private final FluidTank oilTank = new FluidTank(OSFluids.fluidCrudeOil, 0, TANK_CAPACITY);
    private final ItemStack[] slots = new ItemStack[2]; // 0 = balde vazio, 1 = balde cheio

    public boolean formed = false;
    public boolean running = false;
    public int statusCode = STATUS_INCOMPLETE;
    /** 0 = +Z (Sul), 1 = -X (Oeste), 2 = -Z (Norte), 3 = +X (Leste) */
    public int motorSide = 0;

    // Campos sincronizados para GUI / cliente
    public int clientEnergy = 0;
    public int clientEnergyMax = TileEnergyInput.CAPACITY;
    public int clientOil = 0;
    public float swingPhase = 0f;

    private int energyPortX, energyPortY, energyPortZ;
    private boolean hasEnergyPort = false;
    private int scanCooldown = 0;

    public void revalidateNow() {
        this.scanCooldown = 0;
    }

    private TileEnergyInput getEnergyPort() {
        if (!this.hasEnergyPort || this.worldObj == null) {
            return null;
        }
        TileEntity te = this.worldObj.getTileEntity(this.energyPortX, this.energyPortY, this.energyPortZ);
        return (te instanceof TileEnergyInput) ? (TileEnergyInput) te : null;
    }

    public int getLinkedEnergyStored() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.clientEnergy;
        }
        TileEnergyInput port = getEnergyPort();
        return port != null ? port.getEnergyStored(null) : 0;
    }

    public int getLinkedEnergyMax() {
        return TileEnergyInput.CAPACITY;
    }

    public int getOilAmount() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.clientOil;
        }
        return this.oilTank.getFluidAmount();
    }

    public void setClientSync(int status, int energy, int oil, boolean formed, boolean running, int motorSide) {
        this.statusCode = status;
        this.clientEnergy = energy;
        this.clientOil = oil;
        this.formed = formed;
        this.running = running;
        this.motorSide = motorSide;
    }

    @Override
    public void updateEntity() {
        if (this.worldObj == null) {
            return;
        }
        if (this.worldObj.isRemote) {
            if (this.formed && this.running) {
                this.swingPhase += 0.085f;
            }
            return;
        }

        boolean dirty = false;
        boolean prevRunning = this.running;
        boolean prevFormed = this.formed;
        int prevStatus = this.statusCode;
        int prevMotor = this.motorSide;

        if (this.scanCooldown <= 0) {
            this.scanCooldown = 20;
            validateMultiblock();
        } else {
            this.scanCooldown--;
        }

        if (!this.formed) {
            this.running = false;
        } else if (!OilFieldManager.isAreaInsideSingleChunk(this.xCoord, this.zCoord, 1)) {
            this.statusCode = STATUS_CROSS_CHUNK;
            this.running = false;
        } else if (!OilFieldManager.hasOilInChunk(this.worldObj, this.xCoord >> 4, this.zCoord >> 4)) {
            this.statusCode = STATUS_NO_OIL;
            this.running = false;
        } else if (this.oilTank.getFluidAmount() >= TANK_CAPACITY) {
            this.statusCode = STATUS_FULL;
            this.running = false;
        } else {
            TileEnergyInput port = getEnergyPort();
            if (port != null && port.consumeEnergy(RF_PER_TICK)) {
                this.oilTank.fill(new FluidStack(OSFluids.fluidCrudeOil, OIL_PER_TICK), true);
                this.statusCode = STATUS_EXTRACTING;
                this.running = true;
                dirty = true;
            } else {
                this.statusCode = STATUS_NO_POWER;
                this.running = false;
            }
        }

        // Preenche balde vazio automaticamente se houver no slot 0
        if (processBucketSlot()) {
            dirty = true;
        }

        if (dirty) {
            this.markDirty();
        }
        if (prevRunning != this.running
            || prevFormed != this.formed
            || prevStatus != this.statusCode
            || prevMotor != this.motorSide
            || this.worldObj.getTotalWorldTime() % 10 == 0) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    private void validateMultiblock() {
        int claddedCount = 0;
        int energyCount = 0;
        int ex = 0, ez = 0;
        int foundSide = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                Block b = this.worldObj.getBlock(this.xCoord + dx, this.yCoord, this.zCoord + dz);
                if (b == OSBlocks.blockCladded) {
                    claddedCount++;
                } else if (b == OSBlocks.blockEnergyInput && (dx == 0 || dz == 0)) {
                    energyCount++;
                    ex = this.xCoord + dx;
                    ez = this.zCoord + dz;
                    if (dz == 1) foundSide = 0;
                    else if (dx == -1) foundSide = 1;
                    else if (dz == -1) foundSide = 2;
                    else foundSide = 3;
                }
            }
        }

        if (claddedCount == 7 && energyCount == 1) {
            this.formed = true;
            this.hasEnergyPort = true;
            this.energyPortX = ex;
            this.energyPortY = this.yCoord;
            this.energyPortZ = ez;
            this.motorSide = foundSide;
        } else {
            this.formed = false;
            this.hasEnergyPort = false;
            this.statusCode = STATUS_INCOMPLETE;
        }
    }

    private boolean processBucketSlot() {
        ItemStack in = this.slots[0];
        if (in == null || in.getItem() != Items.bucket || this.oilTank.getFluidAmount() < 1000) {
            return false;
        }
        ItemStack out = this.slots[1];
        if (out != null) {
            return false;
        }
        this.oilTank.drain(1000, true);
        in.stackSize--;
        if (in.stackSize <= 0) {
            this.slots[0] = null;
        }
        this.slots[1] = new ItemStack(OSItems.bucketCrudeOil);
        return true;
    }

    public boolean tryFillHandBucket() {
        if (this.oilTank.getFluidAmount() < 1000) {
            return false;
        }
        this.oilTank.drain(1000, true);
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - 2.0, this.yCoord - 0.5, this.zCoord - 2.0,
            this.xCoord + 3.0, this.yCoord + 5.0, this.zCoord + 3.0);
    }

    // ======================================================================
    // IFluidHandler (Extração por canos de qualquer mod)
    // ======================================================================
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(this.oilTank.getFluid())) {
            return null;
        }
        return this.drain(from, resource.amount, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        FluidStack drained = this.oilTank.drain(maxDrain, doDrain);
        if (doDrain && drained != null && drained.amount > 0) {
            this.markDirty();
        }
        return drained;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return fluid == null || fluid == OSFluids.fluidCrudeOil;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { this.oilTank.getInfo() };
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
        return "container.orbitalsunworks.oilExtractor";
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
        return slot == 0 && stack != null && stack.getItem() == Items.bucket;
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

    // ======================================================================
    // NBT & Sync
    // ======================================================================
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.oilTank.readFromNBT(tag.getCompoundTag("OilTank"));
        this.formed = tag.getBoolean("Formed");
        this.running = tag.getBoolean("Running");
        this.statusCode = tag.getInteger("Status");
        this.motorSide = tag.getInteger("MotorSide");
        this.hasEnergyPort = tag.getBoolean("HasPort");
        this.energyPortX = tag.getInteger("PortX");
        this.energyPortY = tag.getInteger("PortY");
        this.energyPortZ = tag.getInteger("PortZ");
        this.clientOil = this.oilTank.getFluidAmount();
        this.clientEnergy = tag.getInteger("SyncEnergy");

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
        NBTTagCompound tankTag = new NBTTagCompound();
        this.oilTank.writeToNBT(tankTag);
        tag.setTag("OilTank", tankTag);
        tag.setBoolean("Formed", this.formed);
        tag.setBoolean("Running", this.running);
        tag.setInteger("Status", this.statusCode);
        tag.setInteger("MotorSide", this.motorSide);
        tag.setBoolean("HasPort", this.hasEnergyPort);
        tag.setInteger("PortX", this.energyPortX);
        tag.setInteger("PortY", this.energyPortY);
        tag.setInteger("PortZ", this.energyPortZ);
        tag.setInteger("SyncEnergy", getLinkedEnergyStored());

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