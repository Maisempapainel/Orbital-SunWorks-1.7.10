package com.orbitalsunworks.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Container da Máquina Sintetizadora (Petróleo Refinado -> Kevlar).
 * Slots:
 *  - 0: Balde de petróleo refinado (entrada) em (26, 28)
 *  - 1: Balde vazio (saída) em (26, 72)
 *  - 2: Kevlar (saída) em (128, 48)
 *  - 3..29: Inventário (y=116)
 *  - 30..38: Hotbar (y=174)
 */
public class ContainerSynthesizer extends Container {

    private static final int BAR_ENERGY_LO = 0;
    private static final int BAR_ENERGY_HI = 1;
    private static final int BAR_REFINED = 2;
    private static final int BAR_PROGRESS = 3;

    private final TileSynthesizer tile;

    private int lastEnergyLo = -1;
    private int lastEnergyHi = -1;
    private int lastRefined = -1;
    private int lastProgress = -1;

    public ContainerSynthesizer(InventoryPlayer playerInv, TileSynthesizer tile) {
        this.tile = tile;

        // Slot 0: Balde de petróleo refinado
        this.addSlotToContainer(new Slot(tile, 0, 26, 28) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return tile.isItemValidForSlot(0, stack);
            }
        });

        // Slot 1: Balde vazio devolvido
        this.addSlotToContainer(new Slot(tile, 1, 26, 72) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Slot 2: Saída de Kevlar
        this.addSlotToContainer(new Slot(tile, 2, 128, 48) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Inventário do jogador (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9,
                    8 + col * 18, 116 + row * 18));
            }
        }

        // Hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 174));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        if (this.tile == null || (this.tile.getWorldObj() != null && this.tile.getWorldObj().isRemote)) {
            return;
        }
        int energy = Math.max(0, this.tile.getEnergy());
        crafter.sendProgressBarUpdate(this, BAR_ENERGY_LO, energy & 0x7FFF);
        crafter.sendProgressBarUpdate(this, BAR_ENERGY_HI, (energy >>> 15) & 0x7FFF);
        crafter.sendProgressBarUpdate(this, BAR_REFINED, this.tile.getRefinedAmount());
        crafter.sendProgressBarUpdate(this, BAR_PROGRESS, this.tile.getProgress());
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.tile == null || (this.tile.getWorldObj() != null && this.tile.getWorldObj().isRemote)) {
            return;
        }

        int energy = Math.max(0, this.tile.getEnergy());
        int energyLo = energy & 0x7FFF;
        int energyHi = (energy >>> 15) & 0x7FFF;
        int refined = this.tile.getRefinedAmount();
        int progress = this.tile.getProgress();

        for (Object o : this.crafters) {
            ICrafting crafter = (ICrafting) o;
            if (this.lastEnergyLo != energyLo) {
                crafter.sendProgressBarUpdate(this, BAR_ENERGY_LO, energyLo);
            }
            if (this.lastEnergyHi != energyHi) {
                crafter.sendProgressBarUpdate(this, BAR_ENERGY_HI, energyHi);
            }
            if (this.lastRefined != refined) {
                crafter.sendProgressBarUpdate(this, BAR_REFINED, refined);
            }
            if (this.lastProgress != progress) {
                crafter.sendProgressBarUpdate(this, BAR_PROGRESS, progress);
            }
        }

        this.lastEnergyLo = energyLo;
        this.lastEnergyHi = energyHi;
        this.lastRefined = refined;
        this.lastProgress = progress;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (this.tile == null) {
            return;
        }
        if (id == BAR_ENERGY_LO) {
            this.tile.clientEnergy = (this.tile.clientEnergy & ~0x7FFF) | (data & 0x7FFF);
        } else if (id == BAR_ENERGY_HI) {
            this.tile.clientEnergy = (this.tile.clientEnergy & 0x7FFF) | ((data & 0x7FFF) << 15);
        } else if (id == BAR_REFINED) {
            this.tile.clientRefined = data;
        } else if (id == BAR_PROGRESS) {
            this.tile.clientProgress = data;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tile != null && this.tile.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack result = null;
        Slot slot = (Slot) this.inventorySlots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack inSlot = slot.getStack();
            result = inSlot.copy();

            if (slotIndex < 3) {
                if (!this.mergeItemStack(inSlot, 3, 39, true)) {
                    return null;
                }
            } else {
                if (this.tile.isItemValidForSlot(0, inSlot)) {
                    if (!this.mergeItemStack(inSlot, 0, 1, false)) {
                        return null;
                    }
                } else if (slotIndex < 30) {
                    if (!this.mergeItemStack(inSlot, 30, 39, false)) {
                        return null;
                    }
                } else if (!this.mergeItemStack(inSlot, 3, 30, false)) {
                    return null;
                }
            }

            if (inSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (inSlot.stackSize == result.stackSize) {
                return null;
            }
            slot.onPickupFromSlot(player, inSlot);
        }
        return result;
    }
}