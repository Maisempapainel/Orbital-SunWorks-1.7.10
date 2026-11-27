package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

/**
 * Container dummy da GUI de retorno da nave estacionada (v0.8.0).
 *
 * Igual ao ContainerNavRocket: o FML 1.7.10 exige um Container não-nulo
 * do servidor para abrir qualquer GUI. Esta não tem slots — só valida
 * que o jogador continua vivo e perto da nave.
 */
public class ContainerReturnHome extends Container {

    public ContainerReturnHome(InventoryPlayer playerInv) {
        // Nenhum slot: HUD de retorno apenas.
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return !player.isDead;
    }
}
