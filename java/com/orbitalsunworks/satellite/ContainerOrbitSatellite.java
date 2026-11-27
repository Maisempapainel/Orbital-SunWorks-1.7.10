package com.orbitalsunworks.satellite;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Container da estrutura do satélite orbital (176x214).
 * Slots alinhados ao padrão do ContainerRocket:
 *  - 0, 1: módulos em (22, 79) e (48, 79)
 *  - 2: reparo em (124, 79)
 *  - Inventário do jogador: y=118 (3x9) e hotbar y=176
 */
public class ContainerOrbitSatellite extends Container {

    private final TileOrbitSatellite tile;
    private int lastHealthInt = -1;
    private int lastTier = -1;
    private int lastShield = -1;
    private long lastGen = -1L;

    public ContainerOrbitSatellite(InventoryPlayer playerInv, TileOrbitSatellite tile) {
        this.tile = tile;

        // Slots de módulos (0-1)
        addSlotToContainer(new Slot(tile, 0, 22, 79) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });
        addSlotToContainer(new Slot(tile, 1, 48, 79) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });

        // Slot de reparo (2)
        addSlotToContainer(new Slot(tile, TileOrbitSatellite.REPAIR_SLOT, 124, 79) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });

        // Inventário do jogador (mesmas posições exatas do ContainerRocket)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9,
                    8 + col * 18, 118 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 176));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        if (this.tile.getWorldObj() != null && !this.tile.getWorldObj().isRemote) {
            this.tile.syncToClients();
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.tile.getWorldObj() == null || this.tile.getWorldObj().isRemote) {
            return;
        }
        int healthInt = Math.round(this.tile.getHealth() * 10000f);
        int tier = this.tile.getTier();
        int shield = this.tile.hasDebrisShield() ? 1 : 0;
        long gen = this.tile.getGenerationRFt();

        if (healthInt != this.lastHealthInt || tier != this.lastTier
            || shield != this.lastShield || gen != this.lastGen) {
            this.tile.syncToClients();
            for (Object o : this.crafters) {
                ICrafting crafter = (ICrafting) o;
                crafter.sendProgressBarUpdate(this, 0, healthInt);
                crafter.sendProgressBarUpdate(this, 1, tier);
                crafter.sendProgressBarUpdate(this, 2, shield);
            }
            this.lastHealthInt = healthInt;
            this.lastTier = tier;
            this.lastShield = shield;
            this.lastGen = gen;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        float health = this.tile.getHealth();
        int tier = this.tile.getTier();
        boolean shield = this.tile.hasDebrisShield();
        if (id == 0) {
            health = data / 10000f;
        } else if (id == 1) {
            tier = data;
        } else if (id == 2) {
            shield = data != 0;
        }
        this.tile.setClientStats(health, tier, shield);
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);
        if (slot == null || !slot.getHasStack()) {
            return null;
        }
        ItemStack stack = slot.getStack();
        ItemStack copy = stack.copy();

        if (slotIndex < TileOrbitSatellite.TOTAL_SLOTS) {
            if (!this.mergeItemStack(stack, TileOrbitSatellite.TOTAL_SLOTS,
                this.inventorySlots.size(), true)) {
                return null;
            }
        } else {
            boolean isModule = stack.getItem() instanceof com.orbitalsunworks.item.ItemModule;
            boolean isRepair = stack.getItem() == com.orbitalsunworks.OSItems.ingotTitaniumAlloy;
            if (isModule) {
                if (!this.mergeItemStack(stack, 0, TileOrbitSatellite.MODULE_SLOTS, false)) {
                    return null;
                }
            } else if (isRepair) {
                if (!this.mergeItemStack(stack, TileOrbitSatellite.REPAIR_SLOT,
                    TileOrbitSatellite.REPAIR_SLOT + 1, false)) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (stack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }
        return copy;
    }
}