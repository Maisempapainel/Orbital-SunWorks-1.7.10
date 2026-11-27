package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

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
    }

    public int getTier() {
        return tier;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getHealth() {
        return health;
    }

    public boolean hasDebrisShield() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (slots[i] != null
                && slots[i].getItem() == com.orbitalsunworks.OSItems.itemModuleDebrisShield) {
                return true;
            }
        }
        return false;
    }

    public java.util.List<String> getModuleIds() {
        java.util.List<String> ids = new java.util.ArrayList<String>();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (slots[i] != null
                && slots[i].getItem() == com.orbitalsunworks.OSItems.itemModuleDebrisShield) {
                ids.add("debrisShield");
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
        return slots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slots[slot] != null) {
            ItemStack result;
            if (slots[slot].stackSize <= amount) {
                result = slots[slot];
                slots[slot] = null;
            } else {
                result = slots[slot].splitStack(amount);
                if (slots[slot].stackSize == 0) {
                    slots[slot] = null;
                }
            }
            markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        ItemStack stack = slots[slot];
        slots[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        slots[slot] = stack;
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
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return stack != null && stack.getItem() instanceof com.orbitalsunworks.item.ItemModule;
    }

    public void dropInventory(net.minecraft.world.World world, int x, int y, int z) {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            ItemStack stack = slots[i];
            if (stack != null) {
                net.minecraft.entity.item.EntityItem drop = new net.minecraft.entity.item.EntityItem(
                    world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
                world.spawnEntityInWorld(drop);
                slots[i] = null;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.tier = tag.getInteger("Tier");
        this.health = tag.getFloat("Health");
        NBTTagList list = tag.getTagList("Modules", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < TOTAL_SLOTS) {
                slots[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("Tier", tier);
        tag.setFloat("Health", health);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (slots[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Modules", list);
    }
}