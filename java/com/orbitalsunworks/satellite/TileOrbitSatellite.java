package com.orbitalsunworks.satellite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.item.ItemModule;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Tile da estrutura do satélite na dimensão orbital.
 * Suporta todos os módulos T1-T3, ejeção de módulos na órbita
 * e reparo imediato (+10% por Kit de Reparo).
 */
public class TileOrbitSatellite extends TileEntity implements IInventory {

    public static final int MODULE_SLOTS = 2;
    public static final int REPAIR_SLOT = 2;
    public static final int TOTAL_SLOTS = 3;
    public static final float REPAIR_AMOUNT = 0.10f;

    private UUID satelliteId;
    private final ItemStack[] slots = new ItemStack[TOTAL_SLOTS];

    private String clientName = "OS-???";
    private int clientTier = 1;
    private float clientHealth = 1.0f;
    private long clientGen = 0L;
    private boolean clientShield = false;
    private final List<String> clientModules = new ArrayList<String>();

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(
            this.xCoord - 10.0, this.yCoord - 2.0, this.zCoord - 10.0,
            this.xCoord + 11.0, this.yCoord + 8.5, this.zCoord + 11.0);
    }

    public void setSatelliteId(UUID id) {
        this.satelliteId = id;
        this.syncToClients();
    }

    public UUID getSatelliteId() {
        return this.satelliteId;
    }

    public SatelliteManager.Satellite getSatellite() {
        if (this.satelliteId == null || this.worldObj == null || this.worldObj.isRemote) {
            return null;
        }
        return SatelliteManager.get(this.worldObj).getById(this.satelliteId);
    }

    public float getHealth() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.clientHealth;
        }
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.health : 0f;
    }

    public long getGenerationRFt() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.clientGen;
        }
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.generationRFt : 0L;
    }

    public String getSatelliteName() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.clientName;
        }
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.name : "???";
    }

    public int getTier() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.clientTier;
        }
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.tier : 1;
    }

    public boolean hasDebrisShield() {
        return this.getShieldTier() > 0 || (this.worldObj != null && this.worldObj.isRemote && this.clientShield);
    }

    public List<String> getInstalledModules() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return new ArrayList<String>(this.clientModules);
        }
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? new ArrayList<String>(sat.modules) : new ArrayList<String>();
    }

    public int getShieldTier() {
        int best = 0;
        for (String mod : this.getInstalledModules()) {
            int st = ItemModule.getShieldTier(mod);
            if (st > best) {
                best = st;
            }
        }
        return best;
    }

    public int getEfficiencyBonusPercent() {
        int total = 0;
        for (String mod : this.getInstalledModules()) {
            total += ItemModule.getEfficiencyPercent(mod);
        }
        return total;
    }

    public void setClientStats(float health, int tier, boolean shield) {
        this.clientHealth = health;
        this.clientTier = tier;
        this.clientShield = shield;
    }

    public void syncToClients() {
        this.markDirty();
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    private void processSlots() {
        SatelliteManager.Satellite sat = getSatellite();
        if (sat == null) {
            return;
        }
        SatelliteManager manager = SatelliteManager.get(this.worldObj);
        boolean changed = false;

        for (int i = 0; i < MODULE_SLOTS; i++) {
            ItemStack stack = this.slots[i];
            if (stack != null && stack.getItem() instanceof ItemModule) {
                String modId = ((ItemModule) stack.getItem()).getModuleId();
                if (manager.addModuleToSatellite(this.satelliteId, modId)) {
                    this.decrStackSize(i, 1);
                    this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5,
                        this.zCoord + 0.5, "random.orb", 0.6f, 1.4f);
                    changed = true;
                }
            }
        }

        ItemStack repair = this.slots[REPAIR_SLOT];
        if (repair != null && repair.getItem() == OSItems.itemRepairKit && sat.health < 1.0f) {
            manager.repairSatellite(this.satelliteId, REPAIR_AMOUNT);
            this.decrStackSize(REPAIR_SLOT, 1);
            this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5,
                this.zCoord + 0.5, "random.anvil_land", 0.5f, 1.6f);
            changed = true;
        }

        if (changed) {
            this.syncToClients();
        }
    }

    public void ejectModuleAt(int index) {
        if (this.worldObj == null || this.worldObj.isRemote || this.satelliteId == null) {
            return;
        }
        String removed = SatelliteManager.get(this.worldObj).removeModuleAt(this.satelliteId, index);
        if (removed != null) {
            ItemStack dropStack = ItemModule.stackForModuleId(removed);
            if (dropStack != null) {
                EntityItem drop = new EntityItem(this.worldObj,
                    this.xCoord + 0.5, this.yCoord + 0.6, this.zCoord + 0.5, dropStack);
                drop.motionX = (this.worldObj.rand.nextDouble() - 0.5) * 0.08;
                drop.motionY = 0.14;
                drop.motionZ = (this.worldObj.rand.nextDouble() - 0.5) * 0.08;
                this.worldObj.spawnEntityInWorld(drop);
            }
            this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5,
                this.zCoord + 0.5, "tile.piston.out", 0.6f, 1.25f);
            this.syncToClients();
        }
    }

    public void ejectModule(String moduleId) {
        SatelliteManager.Satellite sat = getSatellite();
        if (sat == null || moduleId == null) {
            return;
        }
        int idx = sat.modules.indexOf(moduleId);
        if (idx >= 0) {
            this.ejectModuleAt(idx);
        }
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote && this.worldObj.getTotalWorldTime() % 5 == 0) {
            this.processSlots();
        }
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
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.processSlots();
        }
    }

    @Override
    public String getInventoryName() {
        return "container.orbitalsunworks.orbitSatellite";
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
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
            && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64;
    }

    @Override
    public void openInventory() {
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.syncToClients();
        }
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (slot < MODULE_SLOTS) {
            return stack.getItem() instanceof ItemModule;
        }
        return stack.getItem() == OSItems.itemRepairKit;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        String id = tag.getString("SatelliteId");
        if (!id.isEmpty()) {
            try {
                this.satelliteId = UUID.fromString(id);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (tag.hasKey("ClientName")) {
            this.clientName = tag.getString("ClientName");
            this.clientTier = tag.getInteger("ClientTier");
            this.clientHealth = tag.getFloat("ClientHealth");
            this.clientGen = tag.getLong("ClientGen");
            this.clientShield = tag.getBoolean("ClientShield");
            this.clientModules.clear();
            NBTTagList mList = tag.getTagList("ClientModules", 8);
            for (int i = 0; i < mList.tagCount(); i++) {
                this.clientModules.add(mList.getStringTagAt(i));
            }
        }
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            this.slots[i] = null;
        }
        NBTTagList list = tag.getTagList("Items", 10);
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
        if (this.satelliteId != null) {
            tag.setString("SatelliteId", this.satelliteId.toString());
        }
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (slots[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        SatelliteManager.Satellite sat = this.getSatellite();
        if (sat != null) {
            tag.setString("ClientName", sat.name);
            tag.setInteger("ClientTier", sat.tier);
            tag.setFloat("ClientHealth", sat.health);
            tag.setLong("ClientGen", sat.generationRFt);
            tag.setBoolean("ClientShield", sat.hasDebrisShield());
            NBTTagList mList = new NBTTagList();
            for (String m : sat.modules) {
                mList.appendTag(new NBTTagString(m));
            }
            tag.setTag("ClientModules", mList);
        }
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }
}