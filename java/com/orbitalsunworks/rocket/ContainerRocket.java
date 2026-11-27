package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container do foguete: 2 slots de módulos + inventário do jogador.
 *
 * Layout da GUI (176x208):
 * - Módulos: slots em (62,52) e (98,52);
 * - Inventário: começa em y=126, hotbar y=184.
 */
public class ContainerRocket extends Container {

    private final TileRocket tile;

    public ContainerRocket(InventoryPlayer playerInv, TileRocket tile) {
        this.tile = tile;

        // 2 slots de módulos, lado a lado no painel.
        addSlotToContainer(new ModuleSlot(tile, 0, 62, 32));
        addSlotToContainer(new ModuleSlot(tile, 1, 98, 32));

        // Inventário do jogador (GUI 176x214: começa y 118, hotbar y 176).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 118 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 176));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    /** Slot que só aceita módulos. */
    private class ModuleSlot extends Slot {

        public ModuleSlot(TileRocket tile, int index, int x, int y) {
            super(tile, index, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return tile.isItemValidForSlot(this.getSlotIndex(), stack);
        }

        @Override
        public int getSlotStackLimit() {
            return 1;
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack stack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack inSlot = slot.getStack();
            stack = inSlot.copy();

            if (slotIndex < 2) {
                // Módulo -> inventário.
                if (!this.mergeItemStack(inSlot, 2, 38, false)) {
                    return null;
                }
            } else {
                // Inventário -> slots de módulo (se for módulo).
                if (inSlot.getItem() instanceof com.orbitalsunworks.item.ItemModule) {
                    if (!this.mergeItemStack(inSlot, 0, 2, false)) {
                        return null;
                    }
                } else {
                    return null;
                }
            }

            if (inSlot.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (inSlot.stackSize == stack.stackSize) {
                return null;
            }
            slot.onPickupFromSlot(player, inSlot);
        }
        return stack;
    }
}
