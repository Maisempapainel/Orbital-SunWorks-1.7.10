package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.orbitalsunworks.item.ItemModule;
import com.orbitalsunworks.satellite.SatelliteManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TileRocket extends TileEntity implements IInventory {

    public static final int MODULE_SLOTS = 2;
    public static final int TOTAL_SLOTS = 2;

    private final ItemStack[] slots = new ItemStack[TOTAL_SLOTS];

    private int tier = 1;
    private float health = 1.0f;

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - 1.5, this.yCoord, this.zCoord - 1.5,
            this.xCoord + 2.5, this.yCoord + 7.0, this.zCoord + 2.5);
    }

    public void setTier(int tier) {
        this.tier = tier;
        this.markDirty();
    }

    public int getTier() {
        return this.tier;
    }

    public void setHealth(float health) {
        this.health = health;
        this.markDirty();
    }

    public float getHealth() {
        return this.health;
    }

    public boolean hasDebrisShield() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (this.slots[i] != null && this.slots[i].getItem() instanceof ItemModule) {
                String id = ((ItemModule) this.slots[i].getItem()).getModuleId();
                if (SatelliteManager.isShieldModule(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    public java.util.List<String> getModuleIds() {
        java.util.List<String> ids = new java.util.ArrayList<String>();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (this.slots[i] != null && this.slots[i].getItem() instanceof ItemModule) {
                String id = ((ItemModule) this.slots[i].getItem()).getModuleId();
                if (id != null && !id.isEmpty()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    @Override
    public int getSizeInventory() {
        return TOTAL_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.slots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (this.slots[slot] != null) {
            ItemStack result;
            if (this.slots[slot].stackSize <= amount) {
                result = this.slots[slot];
                this.slots[slot] = null;
            } else {
                result = this.slots[slot].splitStack(amount);
                if (this.slots[slot].stackSize == 0) {
                    this.slots[slot] = null;
                }
            }
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = this.slots[slot];
        this.slots[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.slots[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.orbitalsunworks.rocket";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
            && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemModule;
    }

    public void dropInventory(net.minecraft.world.World world, int x, int y, int z) {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            ItemStack stack = this.slots[i];
            if (stack != null) {
                net.minecraft.entity.item.EntityItem drop = new net.minecraft.entity.item.EntityItem(
                    world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
                world.spawnEntityInWorld(drop);
                this.slots[i] = null;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.tier = tag.hasKey("Tier") ? Math.max(1, tag.getInteger("Tier")) : 1;
        this.health = tag.hasKey("Health") ? tag.getFloat("Health") : 1.0f;
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            this.slots[i] = null;
        }
        NBTTagList list = tag.getTagList("Modules", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < TOTAL_SLOTS) {
                this.slots[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Tier", this.tier);
        tag.setFloat("Health", this.health);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (this.slots[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                this.slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Modules", list);
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