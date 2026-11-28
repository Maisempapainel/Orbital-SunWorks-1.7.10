package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

/**
 * Container sem slots do Foguete Dispersor (necessário no FML 1.7.10 para abrir GUI no cliente).
 */
public class ContainerDispersionRocket extends Container {

    private final TileDispersionRocket tile;

    public ContainerDispersionRocket(InventoryPlayer playerInv, TileDispersionRocket tile) {
        this.tile = tile;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.tile != null && !this.tile.isInvalid()
            && player.getDistanceSq(this.tile.xCoord + 0.5,
                this.tile.yCoord + 0.5, this.tile.zCoord + 0.5) <= 64.0;
    }
}