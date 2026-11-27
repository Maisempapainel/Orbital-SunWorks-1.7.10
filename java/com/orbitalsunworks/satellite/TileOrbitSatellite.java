package com.orbitalsunworks.satellite;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

import com.orbitalsunworks.OSItems;

/**
 * Tile da estrutura do satélite na dimensão orbital (v0.6.0).
 *
 * A estrutura é só a "cara física" do satélite; o ESTADO (vida útil,
 * módulos, geração) vive no SatelliteManager (registro global por UUID).
 * O tile guarda o UUID e lê/escreve direto no manager:
 *  - getHealth()/getGenerationRFt() -> do Satellite (autoridade);
 *  - colocar "Escudo de Detrito" num slot de módulo -> instala no
 *    Satellite (toggle);
 *  - colocar "Liga de Titânio" no slot de REPARO -> +10% de vida útil
 *    (consome o item; só aceita se vida < 100%).
 *
 * Slots:
 *  0-1: módulos (Escudo de Detrito etc);
 *  2:   reparo (liga de titânio -> consome 1 por reparo).
 */
public class TileOrbitSatellite extends TileEntity implements IInventory {

    public static final int MODULE_SLOTS = 2;
    public static final int REPAIR_SLOT = 2;
    public static final int TOTAL_SLOTS = 3;

    public static final float REPAIR_AMOUNT = 0.10f; // +10% vida por liga

    /** UUID do satélite no manager (a "identidade" real). */
    private UUID satelliteId;

    private final ItemStack[] slots = new ItemStack[TOTAL_SLOTS];

    public void setSatelliteId(UUID id) {
        this.satelliteId = id;
    }

    public UUID getSatelliteId() {
        return this.satelliteId;
    }

    /** O Satellite do manager (ou null se o registro sumiu). */
    public SatelliteManager.Satellite getSatellite() {
        if (this.satelliteId == null || this.worldObj == null) {
            return null;
        }
        return SatelliteManager.get(this.worldObj).getById(this.satelliteId);
    }

    public float getHealth() {
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.health : 0f;
    }

    public long getGenerationRFt() {
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.generationRFt : 0L;
    }

    public String getSatelliteName() {
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.name : "???";
    }

    public int getTier() {
        SatelliteManager.Satellite sat = getSatellite();
        return sat != null ? sat.tier : 1;
    }

    // ------------------------------------------------------------------
    // Lógica de módulo/reparo (servidor)
    // ------------------------------------------------------------------

    /**
     * Processa os slots: instala módulos novos e repara com liga de
     * titânio. Chamado no updateEntity (1x por segundo no servidor).
     */
    private void processSlots() {
        SatelliteManager.Satellite sat = getSatellite();
        if (sat == null) {
            return;
        }
        SatelliteManager manager = SatelliteManager.get(this.worldObj);

        // Módulos: item no slot -> instala no Satellite (e o item SOME;
        // ele "vira parte" do satélite). Para RETIRAR o módulo (desinstalar
        // devolve o item), a GUI tem o botão de ejetar por slot.
        for (int i = 0; i < MODULE_SLOTS; i++) {
            ItemStack stack = this.slots[i];
            if (stack != null && stack.getItem() == OSItems.itemModuleDebrisShield
                && !sat.modules.contains("debrisShield")) {
                sat.modules.add("debrisShield");
                manager.markDirty();
                this.decrStackSize(i, 1);
                this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5,
                    this.zCoord + 0.5, "random.orb", 0.6f, 1.4f);
            }
        }

        // Reparo: liga de titânio no slot de reparo -> +10% de vida.
        ItemStack repair = this.slots[REPAIR_SLOT];
        if (repair != null && repair.getItem() == OSItems.ingotTitaniumAlloy
            && sat.health < 1.0f) {
            manager.repairSatellite(this.satelliteId, REPAIR_AMOUNT);
            this.decrStackSize(REPAIR_SLOT, 1);
            this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5,
                this.zCoord + 0.5, "random.anvil_land", 0.5f, 1.6f);
        }
    }

    /** Ejeta o módulo instalado de volta pro jogador (botão da GUI). */
    public void ejectModule(String moduleId) {
        SatelliteManager.Satellite sat = getSatellite();
        if (sat == null) {
            return;
        }
        if (sat.modules.remove(moduleId)) {
            SatelliteManager.get(this.worldObj).markDirty();
            net.minecraft.entity.item.EntityItem drop = new net.minecraft.entity.item.EntityItem(
                this.worldObj, this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5,
                new ItemStack(OSItems.itemModuleDebrisShield));
            this.worldObj.spawnEntityInWorld(drop);
        }
    }

    @Override
    public void updateEntity() {
        if (!this.worldObj.isRemote && this.worldObj.getTotalWorldTime() % 20 == 0) {
            this.processSlots();
        }
    }

    // ------------------------------------------------------------------
    // IInventory (slots visíveis na GUI)
    // ------------------------------------------------------------------

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
        return "container.orbitalsunworks.orbitSatellite";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1; // módulos não empilham; reparo 1 por vez
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this
            && player.getDistanceSq(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5) <= 64;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (stack == null) {
            return false;
        }
        if (slot < MODULE_SLOTS) {
            return stack.getItem() == OSItems.itemModuleDebrisShield;
        }
        // slot de reparo: só liga de titânio
        return stack.getItem() == OSItems.ingotTitaniumAlloy;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    // ------------------------------------------------------------------
    // NBT (a estrutura morre com o mundo; o estado real vive no manager)
    // ------------------------------------------------------------------

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
}
