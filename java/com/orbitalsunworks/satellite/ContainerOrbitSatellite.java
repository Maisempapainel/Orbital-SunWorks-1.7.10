package com.orbitalsunworks.satellite;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container da estrutura do satélite orbital (v0.6.0).
 *
 * Slots 0-1: módulos (só ItemModule — Escudo de Detrito).
 * Slot 2: reparo (só Liga de Titânio — o tile processa +10% vida/s).
 */
public class ContainerOrbitSatellite extends Container {

    private final TileOrbitSatellite tile;

    public ContainerOrbitSatellite(InventoryPlayer playerInv, TileOrbitSatellite tile) {
        this.tile = tile;

        // Módulos (0-1) + reparo (2) — na faixa y=90 da textura nova
        // (molduras dos slots desenhadas em (44,80),(80,80),(116,80)...
        // alinhadas ao y=90 do container: slot x,y = canto SUP-ESQ 18x18).
        for (int i = 0; i < TileOrbitSatellite.MODULE_SLOTS; i++) {
            addSlotToContainer(new Slot(tile, i, 45 + i * 36, 91) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
                }
            });
        }
        addSlotToContainer(new Slot(tile, TileOrbitSatellite.REPAIR_SLOT, 117, 91) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return this.inventory.isItemValidForSlot(this.getSlotIndex(), stack);
            }
        });

        // Inventário do jogador (área desenhada y 128..192 na textura).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 129 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 187));
        }
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
            // Do tile pro jogador.
            if (!this.mergeItemStack(stack, TileOrbitSatellite.TOTAL_SLOTS,
                this.inventorySlots.size(), true)) {
                return null;
            }
        } else {
            // Do jogador pro tile: módulo vai pros slots 0-1, liga de
            // titânio pro slot de reparo.
            boolean isModule = stack.getItem() instanceof com.orbitalsunworks.item.ItemModule;
            boolean isRepair = stack.getItem() == com.orbitalsunworks.OSItems.ingotTitaniumAlloy;
            int target = isModule ? 0 : (isRepair ? TileOrbitSatellite.REPAIR_SLOT : -1);
            if (target < 0) {
                return null;
            }
            if (!this.mergeItemStack(stack, target, target + 1, false)) {
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
