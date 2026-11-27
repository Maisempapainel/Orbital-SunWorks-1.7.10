package com.orbitalsunworks.craftingstation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;

/**
 * Tile da Estação de Crafting Orbital — crafting com TEMPO (v6).
 *
 * AULA APRENDIDA (v5 -> v6, os "fantasmas" que sobreviveram): a v5 deixou
 * a LÓGICA simétrica nos dois lados, mas o ESTADO não era: o servidor
 * guardava o estado em craftState e sincronizava via progress bar para o
 * campo clientState — e o cliente continuava consultando craftState (que
 * nunca mudava nele!). Resultado: no cliente isReady()/takeResult() liam
 * estado VELHO para sempre -> predição sempre errada -> o vanilla
 * SUPRIME o pacote de slots quando a predição confere com o retorno
 * (EntityPlayerMP.sendSlotContents com isChangingQuantityOnly) -> o
 * primeiro craft funcionava (IDLE por acaso estava certo) e do segundo em
 * diante tudo virava fantasma: shift-click "não sai", resultado invisível
 * na prévia (updatePreview retornava cedo com estado CRAFTING congelado),
 * só consertando ao reabrir a GUI (pacote de janela inteira).
 *
 * REGRA DA v6 (final): o ESTADO é client-aware por construção:
 *  - getCraftState() lê clientState no cliente e craftState no servidor;
 *  - TODA transição de estado escreve NO CAMPO CERTO de cada lado (via
 *    setCraftState, que espelha conforme isRemote);
 *  - o servidor mantém refund/timer/NBT (autoridade); o cliente prevê
 *    as mesmas transições que o servidor (predição vanilla);
 *  - takeResult no cliente entrega a PRÉVIA (o slot 15 é a fonte local
 *    do que está pronto — nunca diverge do que a GUI mostra).
 *
 * Estados (o mesmo enum nos dois lados):
 *  IDLE: grade = ingredientes; slot 15 = PRÉVIA (não dropa, não salva).
 *  CRAFTING: ingredientes consumidos (refund guardado no servidor); slot
 *        15 mostra o resultado TRAVADO; timer corre no updateEntity.
 *  READY: resultado destravado; retirar = clique normal ou shift-click.
 *
 * Anti-dupe por construção (mantido):
 *  - prévia nunca é dropada nem salva em NBT;
 *  - startCraft consome a grade ANTES de travar o resultado;
 *  - takeResult é o ÚNICO caminho que entrega e limpa o estado;
 *  - quebrar em CRAFTING devolve ingredientes; em READY dropa o resultado.
 */
public class TileCraftingStation extends TileEntity implements IInventory {

    public static final int GRID_SIZE = 15;
    public static final int RESULT_SLOT = 15;
    public static final int TOTAL_SLOTS = 16;

    public static final int STATE_IDLE = 0;
    public static final int STATE_CRAFTING = 1;
    public static final int STATE_READY = 2;

    private final ItemStack[] slots = new ItemStack[TOTAL_SLOTS];

    private int craftState = STATE_IDLE;
    private ItemStack craftResult;
    private ItemStack[] craftRefund = new ItemStack[0];
    private int craftTicksRemaining = 0;
    private int craftTicksTotal = 0;

    // Espelho para o cliente (sync via progress bars do container).
    public int clientState = STATE_IDLE;
    public int clientTicksRemaining = 0;
    public int clientTicksTotal = 0;

    public float animationTick;

    // ------------------------------------------------------------------
    // Estado (servidor)
    // ------------------------------------------------------------------

    public boolean isCrafting() {
        return this.getCraftState() == STATE_CRAFTING;
    }

    public boolean isReady() {
        return this.getCraftState() == STATE_READY;
    }

    /** Estado efetivo: no cliente lê clientState (a predição precisa dele). */
    public int getCraftState() {
        return this.worldObj != null && this.worldObj.isRemote ? this.clientState : this.craftState;
    }

    /** Escreve o estado no lado certo (transições locais). */
    private void setCraftStateBoth(int state) {
        if (this.worldObj != null && this.worldObj.isRemote) {
            this.clientState = state;
        } else {
            this.craftState = state;
        }
    }

    public int getTicksRemaining() {
        return this.worldObj != null && this.worldObj.isRemote ? this.clientTicksRemaining : this.craftTicksRemaining;
    }

    public int getTicksTotal() {
        return this.worldObj != null && this.worldObj.isRemote ? this.clientTicksTotal : this.craftTicksTotal;
    }

    public ItemStack getCraftResult() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.slots[RESULT_SLOT]; // fonte local (syncada)
        }
        return this.craftResult;
    }

    public CraftingStationRecipes.RecipeMatch findCurrentRecipe() {
        // findRecipe só olha os 15 primeiros slots (a grade).
        return CraftingStationRecipes.findRecipe(this.slots);
    }

    /**
     * Recalcula a PRÉVIA no slot 15 (só em IDLE). Roda no servidor (grade
     * mudou) E no cliente (predição de clique): a prévia é idempotente —
     * recalcular não consome nada. O markBlockForUpdate continua só no
     * servidor (cliente não manda pacotes).
     */
    public void updatePreview() {
        if (this.getCraftState() != STATE_IDLE) {
            return;
        }
        CraftingStationRecipes.RecipeMatch match = this.findCurrentRecipe();
        this.slots[RESULT_SLOT] = (match == null) ? null : match.output.copy();
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }

    /**
     * Inicia o craft: consome a grade (guarda refund), trava o resultado.
     * Roda nos DOIS lados — o cliente prevê o consumo da grade para a GUI
     * atualizar na hora; o servidor é a autoridade.
     */
    public boolean startCraft() {
        if (this.craftState != STATE_IDLE) {
            return false;
        }
        CraftingStationRecipes.RecipeMatch match = this.findCurrentRecipe();
        if (match == null) {
            return false;
        }
        List<ItemStack> refund = new ArrayList<ItemStack>();
        for (int i = 0; i < GRID_SIZE; i++) {
            if (this.slots[i] != null) {
                ItemStack one = this.slots[i].copy();
                one.stackSize = 1;
                refund.add(one);
                this.slots[i].stackSize--;
                if (this.slots[i].stackSize <= 0) {
                    this.slots[i] = null;
                }
            }
        }
        this.craftRefund = refund.toArray(new ItemStack[refund.size()]);
        this.craftResult = match.output.copy();
        this.craftTicksTotal = Math.max(1, match.craftTime);
        this.craftTicksRemaining = this.craftTicksTotal;
        this.clientTicksTotal = this.craftTicksTotal;      // cliente prevê o mesmo timer
        this.clientTicksRemaining = this.craftTicksTotal;
        this.setCraftStateBoth(STATE_CRAFTING);            // lado certo de cada JVM
        this.slots[RESULT_SLOT] = this.craftResult.copy();
        this.markDirty();
        if (this.worldObj != null && !this.worldObj.isRemote) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
        return true;
    }

    /**
     * Retira o resultado pronto (única saída legítima). Roda nos DOIS
     * lados: o servidor entrega o item real (craftResult, autoridade); o
     * cliente prevê entregando o que está no SLOT 15 (a fonte local — o
     * slot foi sincronizado com o servidor, então é exatamente o que a
     * GUI está mostrando; nunca diverge do que o jogador vê).
     */
    public ItemStack takeResult() {
        boolean remote = this.worldObj != null && this.worldObj.isRemote;
        if (this.getCraftState() != STATE_READY) {
            return null;
        }
        if (remote) {
            // CLIENTE (predição): entrega o que o slot 15 mostra.
            ItemStack shown = this.slots[RESULT_SLOT];
            if (shown == null) {
                return null;
            }
            ItemStack result = shown.copy();
            this.slots[RESULT_SLOT] = null;
            this.clientState = STATE_IDLE;
            this.clientTicksRemaining = 0;
            this.clientTicksTotal = 0;
            return result;
        }
        // SERVIDOR (autoridade).
        if (this.craftResult == null) {
            return null;
        }
        ItemStack result = this.craftResult.copy();
        this.craftState = STATE_IDLE;
        this.craftResult = null;
        this.craftRefund = new ItemStack[0];
        this.craftTicksRemaining = 0;
        this.craftTicksTotal = 0;
        this.slots[RESULT_SLOT] = null;
        // Grade está vazia (consumida): prévia some junto.
        this.updatePreview();
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        return result;
    }

    /**
     * Devolve o resultado ao estado READY (shift-click com inventário
     * cheio: o que não coube volta pro tile, sem virar item novo).
     */
    public void restoreResult(ItemStack result) {
        this.craftResult = result.copy();
        this.craftState = STATE_READY;
        this.clientState = STATE_READY;
        this.slots[RESULT_SLOT] = this.craftResult.copy();
        this.markDirty();
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        this.animationTick += 1f;

        if (!this.worldObj.isRemote && this.craftState == STATE_CRAFTING) {
            this.craftTicksRemaining--;
            if (this.craftTicksRemaining <= 0) {
                this.craftTicksRemaining = 0;
                this.craftState = STATE_READY;
                this.markDirty();
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                this.worldObj.playSoundEffect(this.xCoord + 0.5, this.yCoord + 0.5, this.zCoord + 0.5,
                    "random.orb", 0.4f, 1.2f);
            }
        } else if (this.worldObj.isRemote && this.clientState == STATE_CRAFTING) {
            // CLIENTE: espelha o timer para a barra andar mesmo sem pacote
            // (o progress bar do container corrige qualquer drift a cada
            // tick — aqui só a animação local).
            if (this.clientTicksRemaining > 0) {
                this.clientTicksRemaining--;
                if (this.clientTicksRemaining == 0) {
                    this.clientState = STATE_READY;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // IInventory
    // ------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        return TOTAL_SLOTS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return slots[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot == RESULT_SLOT) {
            // O container cuida da retirada (takeResult); aqui é defesa.
            if (this.isReady()) {
                return this.takeResult();
            }
            return null;
        }
        if (slots[slot] != null) {
            ItemStack result;
            if (slots[slot].stackSize <= amount) {
                result = slots[slot];
                slots[slot] = null;
            } else {
                result = slots[slot].splitStack(amount);
                if (slots[slot].stackSize == 0) {
                    slots[slot] = null;
                }
            }
            this.updatePreview();
            this.markDirty();
            return result;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        // Nada sai "na mão" ao fechar: prévia é fantasma.
        if (slot == RESULT_SLOT && this.craftState == STATE_IDLE) {
            return null;
        }
        ItemStack stack = slots[slot];
        slots[slot] = null;
        return stack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        slots[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        if (slot != RESULT_SLOT) {
            this.updatePreview();
        }
        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.orbitalsunworks.craftingStation";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return true;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) <= 64;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot < GRID_SIZE;
    }

    /**
     * Dropa o conteúdo quando a estação é quebrada — SEM DUPE:
     * IDLE: só a grade (a prévia é fantasma, não existe como item);
     * CRAFTING: grade + refund dos ingredientes consumidos;
     * READY: grade + o resultado pronto.
     */
    public void dropInventory(net.minecraft.world.World world, int x, int y, int z) {
        for (int i = 0; i < GRID_SIZE; i++) {
            dropStack(world, x, y, z, slots[i]);
            slots[i] = null;
        }
        if (this.craftState == STATE_CRAFTING) { // servidor only
            for (ItemStack refund : this.craftRefund) {
                dropStack(world, x, y, z, refund);
            }
        } else if (this.craftState == STATE_READY) {
            dropStack(world, x, y, z, this.craftResult);
        }
        this.craftState = STATE_IDLE;
        this.craftResult = null;
        this.craftRefund = new ItemStack[0];
        this.craftTicksRemaining = 0;
        this.craftTicksTotal = 0;
        this.slots[RESULT_SLOT] = null;
    }

    private void dropStack(net.minecraft.world.World world, int x, int y, int z, ItemStack stack) {
        if (stack != null) {
            net.minecraft.entity.item.EntityItem drop = new net.minecraft.entity.item.EntityItem(
                world, x + 0.5, y + 0.5, z + 0.5, stack.copy());
            world.spawnEntityInWorld(drop);
        }
    }

    // ------------------------------------------------------------------
    // NBT — o slot 15 NUNCA é salvo (prévia é recalculada no load; o
    // resultado real vive em CraftResult, atrelado ao estado).
    // ------------------------------------------------------------------

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        NBTTagList list = tag.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound itemTag = list.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot < GRID_SIZE) {
                slots[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
        this.craftState = tag.getByte("CraftState");
        this.craftTicksRemaining = tag.getInteger("CraftTicks");
        this.craftTicksTotal = tag.getInteger("CraftTicksTotal");
        this.craftResult = tag.hasKey("CraftResult")
            ? ItemStack.loadItemStackFromNBT(tag.getCompoundTag("CraftResult")) : null;
        NBTTagList refundList = tag.getTagList("CraftRefund", 10);
        this.craftRefund = new ItemStack[refundList.tagCount()];
        for (int i = 0; i < refundList.tagCount(); i++) {
            this.craftRefund[i] = ItemStack.loadItemStackFromNBT(refundList.getCompoundTagAt(i));
        }
        // Reconstrói o slot 15 conforme o estado.
        if (this.craftState == STATE_CRAFTING || this.craftState == STATE_READY) {
            this.slots[RESULT_SLOT] = this.craftResult == null ? null : this.craftResult.copy();
        } else {
            this.updatePreview();
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < GRID_SIZE; i++) {
            if (slots[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                slots[i].writeToNBT(itemTag);
                list.appendTag(itemTag);
            }
        }
        tag.setTag("Items", list);
        tag.setByte("CraftState", (byte) this.craftState);
        tag.setInteger("CraftTicks", this.craftTicksRemaining);
        tag.setInteger("CraftTicksTotal", this.craftTicksTotal);
        if (this.craftResult != null) {
            NBTTagCompound resultTag = new NBTTagCompound();
            this.craftResult.writeToNBT(resultTag);
            tag.setTag("CraftResult", resultTag);
        }
        NBTTagList refundList = new NBTTagList();
        for (ItemStack refund : this.craftRefund) {
            if (refund != null) {
                NBTTagCompound rTag = new NBTTagCompound();
                refund.writeToNBT(rTag);
                refundList.appendTag(rTag);
            }
        }
        tag.setTag("CraftRefund", refundList);
    }
}
