package com.orbitalsunworks.block;

import java.util.ArrayList;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import com.orbitalsunworks.rocket.TileRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Bloco do Foguete Tier 1 posicionado na plataforma.
 *
 * MULTIBLOCK: ocupa 3 blocos de altura — meta 0 = base (com GUI e módulos),
 * meta 1 = meio, meta 2 = topo. Colisão REAL em cada parte. Quebrar
 * QUALQUER parte remove a coluna inteira e devolve o item do foguete
 * (dropado uma vez, na posição da base) + módulos instalados.
 *
 * AULA APRENDIDA (v0.5.1: quebrar a parte de cima "bugava"): o código
 * antigo limpava as partes de cima em onBlockHarvested, mas esse método
 * SÓ roda no fluxo survival (ItemInWorldManager); no CRIATIVO o vanilla
 * chama direto removedByPlayer -> setBlockToAir, sem passar por lá — e
 * como só a base tinha lógica, quebrar o meio/topo deixava o resto da
 * coluna órfã (só a base clicável). Agora a limpeza da coluna vive em
 * removedByPlayer + breakBlock (os DOIS caminhos), com guarda
 * anti-recursão, e o drop do item é feito UMA vez pela base.
 */
public class BlockRocket extends BlockContainer {

    @SideOnly(Side.CLIENT)
    private IIcon iconBase;

    /** Guarda anti-recursão: já estamos removendo a coluna. */
    private boolean removingColumn = false;

    public BlockRocket() {
        super(Material.iron);
        this.setBlockName("blockRocket");
        // SEM aba criativa: só existe posicionado (o item craftável é o rocketT1).
        this.setCreativeTab(null);
        this.setHardness(3.0f);
        this.setResistance(5.0f);
        this.setStepSound(soundTypeMetal);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        // Só a base (meta 0) tem tile (módulos + estado).
        return meta == 0 ? new TileRocket() : null;
    }

    // ------------------------------------------------------------------
    // Renderização: todas as partes são invisíveis (TESR desenha o modelo
    // completo a partir da base) — mas têm colisão de verdade.
    // ------------------------------------------------------------------

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1; // TESR
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        // Ícone de BLOCO vive no atlas de blocos (o antigo registrava
        // "rocketT1" só no atlas de itens -> partículas roxo/preto e
        // pick-block quebrado, o "missing texture").
        this.iconBase = reg.registerIcon(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockRocket");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.iconBase;
    }

    @Override
    public void setBlockBoundsBasedOnState(net.minecraft.world.IBlockAccess world, int x, int y, int z) {
        // Hitbox por bloco (cada parte na sua altura).
        this.setBlockBounds(0.15f, 0.0f, 0.15f, 0.85f, 1.0f, 0.85f);
    }

    // ------------------------------------------------------------------
    // Interação
    // ------------------------------------------------------------------

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        // Clicar em QUALQUER parte abre a GUI da base.
        int baseY = y - world.getBlockMetadata(x, y, z);
        if (!world.isRemote && world.getBlock(x, baseY, z) == this) {
            player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                com.orbitalsunworks.handler.GuiHandler.ID_ROCKET, world, x, baseY, z);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Pick-block (botão do meio no criativo): devolve o ITEM do foguete,
    // nunca um bloco fantasma preto/rosa.
    // ------------------------------------------------------------------

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
        return new ItemStack(com.orbitalsunworks.OSItems.itemRocketT1);
    }

    // ------------------------------------------------------------------
    // Quebra: remove a COLUNA INTEIRA (qualquer parte quebrada), dropa
    // o item do foguete 1x na base + módulos. Roda em survival E criativo.
    // ------------------------------------------------------------------

    /**
     * Chamado pelo vanilla no fluxo de quebra (survival via
     * ItemInWorldManager.removeBlock, criativo também — ambos passam por
     * tryHarvestBlock -> removeBlock -> AQUI). Sobrescrevemos a versão do
     * Forge com willHarvest=true: o vanilla então NÃO chama harvestBlock
     * depois (não dropa 2x) e o bloco só vira ar quando devolvermos true.
     *
     * CRIATIVO (v0.5.3): blocos não dropam itens no criativo (comportamento
     * vanilla). Os MÓDULOS instalados continuam dropando (como o conteúdo
     * de um baú no criativo — itens guardados não somem).
     */
    @Override
    public boolean removedByPlayer(World world, net.minecraft.entity.player.EntityPlayer player,
            int x, int y, int z, boolean willHarvest) {
        if (this.removingColumn) {
            // Já estamos limpando a coluna: deixa o vanilla terminar.
            return super.removedByPlayer(world, player, x, y, z, willHarvest);
        }
        int meta = world.getBlockMetadata(x, y, z);
        int baseY = y - meta;
        boolean creative = player != null && player.capabilities.isCreativeMode;

        if (!world.isRemote) {
            // 1) Dropa módulos da base ANTES de remover o tile (também no
            //    criativo: itens guardados não somem, igual baú vanilla).
            TileEntity te = world.getTileEntity(x, baseY, z);
            if (te instanceof TileRocket) {
                ((TileRocket) te).dropInventory(world, x, baseY, z);
            }
            // 2) Dropa o item do foguete UMA vez (na base) — SÓ em survival.
            //    No criativo nada dropa (vanilla). A ThreadLocal autoriza o
            //    getDrops SÓ nesta chamada — qualquer outra chamada de
            //    getDrops (explosão etc.) não dropa 2x.
            if (!creative) {
                this.manualDrop.set(Boolean.TRUE);
                try {
                    dropBlockAsItem(world, x, baseY, z, 0, 0);
                } finally {
                    this.manualDrop.set(Boolean.FALSE);
                }
            }
        }

        // 3) Remove a coluna toda (base por último: é dela que o TESR
        //    desenha). A guarda impede recursão infinita.
        this.removingColumn = true;
        try {
            for (int i = 2; i >= 0; i--) {
                if (world.getBlock(x, baseY + i, z) == this) {
                    world.setBlockToAir(x, baseY + i, z);
                }
            }
        } finally {
            this.removingColumn = false;
        }
        // O bloco clicado já foi removido acima; true = "removido".
        return true;
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        // O item do foguete é entregue UMA vez pelo removedByPlayer (via
        // dropBlockAsItem -> getDrops, autorizado pela ThreadLocal). Sem
        // a autorização: lista vazia (explosões, partes não-base, etc.).
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        if (metadata == 0 && this.manualDrop.get() == Boolean.TRUE) {
            drops.add(new ItemStack(com.orbitalsunworks.OSItems.itemRocketT1));
        }
        return drops;
    }

    /**
     * Sinaliza que o drop atual vem do removedByPlayer (entrega única do
     * item do foguete). Usa ThreadLocal porque dropBlockAsItem ->
     * getDrops pode passar por harvestBlock no mesmo tick.
     */
    private final ThreadLocal<Boolean> manualDrop = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {
        // A coluna inteira já é removida em removedByPlayer; nada aqui.
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, net.minecraft.block.Block block, int meta) {
        // Módulos e item já dropados em removedByPlayer (uma vez, na
        // base). Aqui só a limpeza padrão do vanilla.
        super.breakBlock(world, x, y, z, block, meta);
    }

    @Override
    public boolean canSilkHarvest() {
        return false;
    }
}
