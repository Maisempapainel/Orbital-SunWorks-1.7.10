package com.orbitalsunworks.rocket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;

/**
 * Container "dummy" do Foguete de Navegação (v0.8.0).
 *
 * BUG CRÍTICO CORRIGIDO: no FML 1.7.10, se getServerGuiElement retorna
 * null, o servidor NÃO envia o pacote de abrir GUI — o cliente clica e
 * NADA acontece (o bug do "botão direito não abre nada"). A GUI do mapa
 * orbital não tem slots, mas o servidor PRECISA devolver um Container
 * não-nulo; este aqui não tem slot nenhum e só valida a distância até
 * o bloco (o mesmo alcance do clique).
 */
public class ContainerNavRocket extends Container {

    private final TileNavRocket tile;

    public ContainerNavRocket(InventoryPlayer playerInv, TileNavRocket tile) {
        this.tile = tile;
        // Nenhum slot: a GUI é só o mapa orbital (dados via rede).
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        // Perto do foguete posicionado (8 blocos, igual ao handler).
        return this.tile != null && !this.tile.isInvalid()
            && player.getDistanceSq(this.tile.xCoord + 0.5,
                this.tile.yCoord + 0.5, this.tile.zCoord + 0.5) <= 64;
    }
}
