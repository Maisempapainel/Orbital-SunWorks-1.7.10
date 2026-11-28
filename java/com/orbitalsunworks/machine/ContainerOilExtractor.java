package com.orbitalsunworks.machine;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidTankInfo;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Container do Extrator de Petróleo (Multibloco 3x3 Pumpjack).
 * Slots:
 *  - 0: Balde vazio (entrada) em (44, 52)
 *  - 1: Balde de petróleo bruto (saída) em (116, 52)
 *  - 2..28: Inventário do jogador (y=104)
 *  - 29..37: Hotbar (y=162)
 */
public class ContainerOilExtractor extends Container {

    private static final int BAR_STATUS = 0;
    private static final int BAR_ENERGY_LO = 1;
    private static final int BAR_ENERGY_HI = 2;
    private static final int BAR_OIL = 3;
    private static final int BAR_FORMED = 4;
    private static final int BAR_RUNNING = 5;
    private static final int BAR_MOTOR_SIDE = 6;

    private final TileOilExtractor tile;

    private int lastStatus = -1;
    private int lastEnergyLo = -1;
    private int lastEnergyHi = -1;
    private int lastOil = -1;
    private int lastFormed = -1;
    private int lastRunning = -1;
    private int lastMotorSide = -1;

    public int clientStatus;
    public int clientEnergy;
    public int clientOil;
    public boolean clientFormed;
    public boolean clientRunning;
    public int clientMotorSide;

    public ContainerOilExtractor(InventoryPlayer playerInv, TileOilExtractor tile) {
        this.tile = tile;

        // Slot 0: Balde vazio (entrada)
        this.addSlotToContainer(new Slot(tile, 0, 44, 52) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return tile.isItemValidForSlot(0, stack);
            }
        });

        // Slot 1: Balde cheio de petróleo bruto (somente saída)
        this.addSlotToContainer(new Slot(tile, 1, 116, 52) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return false;
            }
        });

        // Inventário do jogador (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInv, col + row * 9 + 9,
                    8 + col * 18, 104 + row * 18));
            }
        }

        // Hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 162));
        }
    }

    private int readServerOil() {
        FluidTankInfo[] info = this.tile.getTankInfo(ForgeDirection.UNKNOWN);
        if (info != null && info.length > 0 && info[0] != null && info[0].fluid != null) {
            return info[0].fluid.amount;
        }
        return 0;
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafter) {
        super.addCraftingToCrafters(crafter);
        if (this.tile == null || (this.tile.getWorldObj() != null && this.tile.getWorldObj().isRemote)) {
            return;
        }
        int status = this.tile.getStatus();
        int energy = Math.max(0, this.tile.getEnergy());
        int energyLo = energy & 0x7FFF;
        int energyHi = (energy >>> 15) & 0x7FFF;
        int oil = this.readServerOil();
        int formed = this.tile.isFormed() ? 1 : 0;
        int running = this.tile.isRunning() ? 1 : 0;
        int motorSide = this.tile.getMotorSide();

        crafter.sendProgressBarUpdate(this, BAR_STATUS, status);
        crafter.sendProgressBarUpdate(this, BAR_ENERGY_LO, energyLo);
        crafter.sendProgressBarUpdate(this, BAR_ENERGY_HI, energyHi);
        crafter.sendProgressBarUpdate(this, BAR_OIL, oil);
        crafter.sendProgressBarUpdate(this, BAR_FORMED, formed);
        crafter.sendProgressBarUpdate(this, BAR_RUNNING, running);
        crafter.sendProgressBarUpdate(this, BAR_MOTOR_SIDE, motorSide);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (this.tile == null || (this.tile.getWorldObj() != null && this.tile.getWorldObj().isRemote)) {
            return;
        }

        int status = this.tile.getStatus();
        int energy = Math.max(0, this.tile.getEnergy());
        int energyLo = energy & 0x7FFF;
        int energyHi = (energy >>> 15) & 0x7FFF;
        int oil = this.readServerOil();
        int formed = this.tile.isFormed() ? 1 : 0;
        int running = this.tile.isRunning() ? 1 : 0;
        int motorSide = this.tile.getMotorSide();

        for (Object o : this.crafters) {
            ICrafting crafter = (ICrafting) o;
            if (this.lastStatus != status) {
                crafter.sendProgressBarUpdate(this, BAR_STATUS, status);
            }
            if (this.lastEnergyLo != energyLo) {
                crafter.sendProgressBarUpdate(this, BAR_ENERGY_LO, energyLo);
            }
            if (this.lastEnergyHi != energyHi) {
                crafter.sendProgressBarUpdate(this, BAR_ENERGY_HI, energyHi);
            }
            if (this.lastOil != oil) {
                crafter.sendProgressBarUpdate(this, BAR_OIL, oil);
            }
            if (this.lastFormed != formed) {
                crafter.sendProgressBarUpdate(this, BAR_FORMED, formed);
            }
            if (this.lastRunning != running) {
                crafter.sendProgressBarUpdate(this, BAR_RUNNING, running);
            }
            if (this.lastMotorSide != motorSide) {
                crafter.sendProgressBarUpdate(this, BAR_MOTOR_SIDE, motorSide);
            }
        }

        this.lastStatus = status;
        this.lastEnergyLo = energyLo;
        this.lastEnergyHi = energyHi;
        this.lastOil = oil;
        this.lastFormed = formed;
        this.lastRunning = running;
        this.lastMotorSide = motorSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        if (id == BAR_STATUS) {
            this.clientStatus = data;
        } else if (id == BAR_ENERGY_LO) {
            this.clientEnergy = (this.clientEnergy & ~0x7FFF) | (data & 0x7FFF);
        } else if (id == BAR_ENERGY_HI) {
            this.clientEnergy = (this.clientEnergy & 0x7FFF) | ((data & 0x7FFF) << 15);
        } else if (id == BAR_OIL) {
            this.clientOil = data;
        } else if (id == BAR_FORMED) {
            this.clientFormed = data != 0;
        } else if (id == BAR_RUNNING) {
            this.clientRunning = data != 0;
        } else if (id == BAR_MOTOR_SIDE) {
            this.clientMotorSide = data;
        }
        if (this.tile != null) {
            this.tile.setClientSync(this.clientStatus, this.clientEnergy, this.clientOil,
                this.clientFormed, this.clientRunning, this.clientMotorSide);
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

            if (slotIndex < 2) {
                // Máquina -> Inventário do jogador
                if (!this.mergeItemStack(inSlot, 2, 38, true)) {
                    return null;
                }
            } else {
                // Inventário -> Slot 0 (balde vazio)
                if (this.tile.isItemValidForSlot(0, inSlot)) {
                    if (!this.mergeItemStack(inSlot, 0, 1, false)) {
                        return null;
                    }
                } else if (slotIndex < 29) {
                    if (!this.mergeItemStack(inSlot, 29, 38, false)) {
                        return null;
                    }
                } else if (!this.mergeItemStack(inSlot, 2, 29, false)) {
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