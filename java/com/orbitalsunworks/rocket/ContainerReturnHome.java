package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

/**
 * Container da HUD de retorno do Foguete de Navegação estático na órbita (176x132).
 * Sem slots de inventário para não vazar fora do painel compacto nem sobrepor o rodapé.
 */
public class ContainerReturnHome extends Container {

    private final TileNavRocket tile;

    public ContainerReturnHome(InventoryPlayer playerInv, TileNavRocket tile) {
        this.tile = tile;
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