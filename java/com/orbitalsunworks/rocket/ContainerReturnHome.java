package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container da HUD de retorno do Foguete de Navegação estático na órbita.
 * Mantém o mesmo alinhamento de inventário do ContainerRocket (176x214).
 */
public class ContainerReturnHome extends Container {

    private final TileNavRocket tile;

    public ContainerReturnHome(InventoryPlayer playerInv, TileNavRocket tile) {
        this.tile = tile;

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
    public boolean canInteractWith(EntityPlayer player) {
        return this.tile != null && !this.tile.isInvalid()
            && player.getDistanceSq(this.tile.xCoord + 0.5,
                this.tile.yCoord + 0.5, this.tile.zCoord + 0.5) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        return null;
    }
}