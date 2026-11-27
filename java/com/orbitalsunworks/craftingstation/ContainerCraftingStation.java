package com.orbitalsunworks.craftingstation;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

/**
 * Container da Estação de Crafting com TEMPO (v6).
 *
 * AULA APRENDIDA (v5 -> v6): a v5 fez a LÓGICA rodar nos dois lados, mas
 * o tile continuava lendo craftState (campo que SÓ o servidor atualizava
 * — no cliente ele congelava no valor do load). Predição sempre errada ->
 * pacote suprimido -> shift-click fantasma e prévia invisível a partir do
 * 2º craft. A v6 conserta na FONTE (TileCraftingStation v6: estado
 * client-aware), e aqui só garantimos que o slot de resultado consulta o
 * estado efetivo (getCraftState) em vez do campo privado do servidor.
 *
 * O slot de resultado é um slot de verdade:
 * - canTakeStack: só em READY;
 * - decrStackSize: entrega via tile.takeResult() (único caminho);
 * - shift-click: também passa por takeResult — o merge é feito no stack
 *   RETIRADO, nunca numa cópia fantasma. Zero dupe.
 *
 * Clique no resultado em IDLE inicia o craft (slotClick -> startCraft).
 */
public class ContainerCraftingStation extends Container {

    private final TileCraftingStation tile;

    /** Canais de sync (progress bars vanilla). */
    private static final int BAR_STATE = 0;
    private static final int BAR_TICKS = 1;
    private static final int BAR_TOTAL = 2;

    private int lastState = -1;
    private int lastTicks = -1;
    private int lastTotal = -1;

    public ContainerCraftingStation(InventoryPlayer playerInv, TileCraftingStation tile) {
        this.tile = tile;

        // Grade 3x5 (slots 0-14 do tile).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                addSlotToContainer(new Slot(tile, row * 5 + col, 26 + col * 18, 17 + row * 18));
            }
        }

        // Slot de resultado (15) — à direita da grade.
        addSlotToContainer(new SlotCraftingResult(tile, 124, 35));

        // Inventário do jogador (y 108; GUI 176x202).
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 108 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 166));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUseableByPlayer(player);
    }

    // ------------------------------------------------------------------
    // Sync servidor -> cliente (estado + timer)
    // ------------------------------------------------------------------

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object o : this.crafters) {
            ICrafting crafter = (ICrafting) o;
            if (this.lastState != this.tile.getCraftState()) {
                crafter.sendProgressBarUpdate(this, BAR_STATE, this.tile.getCraftState());
            }
            if (this.lastTicks != this.tile.getTicksRemaining()) {
                crafter.sendProgressBarUpdate(this, BAR_TICKS, this.tile.getTicksRemaining());
            }
            if (this.lastTotal != this.tile.getTicksTotal()) {
                crafter.sendProgressBarUpdate(this, BAR_TOTAL, this.tile.getTicksTotal());
            }
        }
        this.lastState = this.tile.getCraftState();
        this.lastTicks = this.tile.getTicksRemaining();
        this.lastTotal = this.tile.getTicksTotal();
    }

    @Override
    public void updateProgressBar(int bar, int value) {
        if (bar == BAR_STATE) {
            this.tile.clientState = value;
        } else if (bar == BAR_TICKS) {
            this.tile.clientTicksRemaining = value;
        } else if (bar == BAR_TOTAL) {
            this.tile.clientTicksTotal = value;
        }
    }

    // ------------------------------------------------------------------
    // Slot de resultado
    // ------------------------------------------------------------------

    private class SlotCraftingResult extends Slot {

        public SlotCraftingResult(TileCraftingStation tile, int x, int y) {
            super(tile, TileCraftingStation.RESULT_SLOT, x, y);
        }

        @Override
        public boolean isItemValid(ItemStack stack) {
            return false;
        }

        @Override
        public boolean canTakeStack(EntityPlayer player) {
            return tile.isReady();
        }

        @Override
        public ItemStack decrStackSize(int amount) {
            // ÚNICO caminho de entrega do resultado.
            return tile.isReady() ? tile.takeResult() : null;
        }
    }

    // ------------------------------------------------------------------
    // Cliques no resultado: iniciar craft (IDLE) / pegar (READY)
    // ------------------------------------------------------------------

    @Override
    public ItemStack slotClick(int slotIndex, int button, int mode, EntityPlayer player) {
        if (slotIndex == 15 && mode == 0 && button == 0) {
            // Clique simples no resultado.
            if (this.tile.isCrafting()) {
                return null; // travado
            }
            if (this.tile.isReady()) {
                // Retirada normal — o vanilla cuida (passa pelo
                // decrStackSize do slot, que chama takeResult).
                return super.slotClick(slotIndex, button, mode, player);
            }
            // IDLE com prévia: inicia o craft. RODA NOS DOIS LADOS —
            // o cliente precisa consumir a grade na predição também,
            // senão os ingredientes viram fantasmas na GUI (v4 bug).
            boolean started = this.tile.startCraft();
            if (started && this.tile.getWorldObj() != null
                && !this.tile.getWorldObj().isRemote) {
                this.tile.getWorldObj().playSoundAtEntity(player, "random.click", 0.5f, 0.6f);
            }
            return null;
        }
        return super.slotClick(slotIndex, button, mode, player);
    }

    // ------------------------------------------------------------------
    // Shift-click — SEM DUPE (e SEM predição quebrada: roda nos 2 lados)
    // ------------------------------------------------------------------

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        // Resultado: retira de verdade (takeResult) e manda pro jogador.
        // Roda nos DOIS lados: o cliente prevê o item indo pro inventário
        // (a GUI atualiza na hora); o servidor entrega e, se a predição
        // divergir, o vanilla re-synca a janela inteira.
        if (slotIndex == 15) {
            if (!this.tile.isReady()) {
                return null;
            }
            ItemStack result = this.tile.takeResult(); // entrega única
            if (result == null) {
                return null;
            }
            // mergeItemStack: true = coube TUDO (stackSize vira 0).
            // Se sobrou algo (inventário cheio/parcial), devolve ao tile
            // em READY — nunca destrói o item (bug de perda da v5).
            boolean merged = this.mergeItemStack(result, 16, 52, true);
            if (!merged || result.stackSize > 0) {
                this.tile.restoreResult(result);
            }
            return null;
        }

        ItemStack stack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);

        if (slot != null && slot.getHasStack()) {
            ItemStack inSlot = slot.getStack();
            stack = inSlot.copy();

            if (slotIndex >= 16) {
                // Inventário -> grade.
                if (!this.mergeItemStack(inSlot, 0, 15, false)) {
                    return null;
                }
            } else {
                // Grade -> inventário.
                if (!this.mergeItemStack(inSlot, 16, 52, false)) {
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
