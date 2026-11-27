package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Plataforma de Lançamento - multiblock universal 3x3.
 *
 * Como funciona:
 * 1. O jogador monta um quadrado 3x3 de plataformas no chão;
 * 2. Ao completar o 3x3, TODOS os blocos mudam de textura: viram a
 *    "Plataforma de Lançamento montada" (borda metálica + centro com
 *    marcação de foguete). Ao quebrar qualquer bloco, todos voltam ao
 *    normal (meta recalculado por vizinhança);
 * 3. Com o foguete na mão, clicar em QUALQUER plataforma da estrutura
 *    posiciona o foguete (3 blocos de altura) sobre o CENTRO do 3x3;
 * 4. Clicar no foguete posicionado abre a GUI (módulos + botão Lançar).
 *
 * Meta: 0 = solto (textura de placa individual); 1 = membro da estrutura
 * (textura montada); 2 = centro da estrutura (textura do pad do foguete).
 */
public class BlockLaunchPlatform extends Block {

    @SideOnly(Side.CLIENT)
    private IIcon iconPlain;
    @SideOnly(Side.CLIENT)
    private IIcon iconAssembled;
    @SideOnly(Side.CLIENT)
    private IIcon iconCenter;

    public BlockLaunchPlatform() {
        super(Material.iron);
        this.setBlockName("blockLaunchPlatform");
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setHardness(5.0f);
        this.setResistance(15.0f);
        this.setStepSound(soundTypeMetal);
        this.setHarvestLevel("pickaxe", 1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister reg) {
        this.iconPlain = reg.registerIcon(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockLaunchPlatform");
        this.iconAssembled = reg.registerIcon(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockLaunchPlatform_on");
        this.iconCenter = reg.registerIcon(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "blockLaunchPlatform_center");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Para o inventário: mostra a placa individual.
        return this.iconPlain;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        if (side == 1) { // topo
            if (meta == 2) {
                return this.iconCenter;
            }
            if (meta == 1) {
                return this.iconAssembled;
            }
        }
        return this.iconPlain;
    }

    /**
     * @return true se existe uma estrutura 3x3 completa de plataformas
     *         centrada em (x, y, z) — incluindo a regra do PERÍMETRO
     *         LIVRE: nada pode estar encostado nos lados da plataforma
     *         (as 12 células laterais do anel 5x5; os 4 cantos diagonais
     *         do anel não contam). Se algo encostar, a estrutura se
     *         desmonta (todos voltam a placa individual).
     */
    public static boolean isCompleteStructure(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != OSBlocks.blockLaunchPlatform) {
            return false;
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (world.getBlock(x + dx, y, z + dz) != OSBlocks.blockLaunchPlatform) {
                    return false;
                }
            }
        }
        // Perímetro lateral livre: as 12 células do anel 5x5 que não são
        // os cantos diagonais precisam estar vazias (ar ou substituível,
        // tipo grama alta — blocos de verdade quebram a estrutura).
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean inner = Math.abs(dx) <= 1 && Math.abs(dz) <= 1;
                boolean ringCorner = Math.abs(dx) == 2 && Math.abs(dz) == 2;
                if (inner || ringCorner) {
                    continue;
                }
                Block b = world.getBlock(x + dx, y, z + dz);
                if (!world.isAirBlock(x + dx, y, z + dz)
                    && !b.isReplaceable((net.minecraft.world.IBlockAccess) world, x + dx, y, z + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Procura, num raio de 1 bloco da plataforma clicada, qual posição é o
     * centro de um 3x3 completo. Retorna coordenadas no array ou null.
     */
    public static int[] findCenter(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (isCompleteStructure(world, x + dx, y, z + dz)) {
                    return new int[]{x + dx, y, z + dz};
                }
            }
        }
        return null;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        if (!world.isRemote) {
            // Scan ±2: cobre TODA a estrutura e o perímetro. O bug do
            // "bloco safado sem textura" era o scan ±1: quem ficava a 2
            // blocos de distância nunca era recalculado.
            updateArea(world, x, y, z);
        }
    }

    @Override
    public void onBlockAdded(World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
        if (!world.isRemote) {
            updateArea(world, x, y, z);
        }
    }

    /** Recalcula o meta de todas as plataformas num raio de 2 blocos. */
    private void updateArea(World world, int x, int y, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                updateStructureMeta(world, x + dx, y, z + dz);
            }
        }
    }

    /**
     * Recalcula o meta da plataforma em (x,y,z):
     * - 2 se é o centro de um 3x3 completo (pad do foguete);
     * - 1 se é membro de um 3x3 completo (textura montada);
     * - 0 caso contrário (placa individual).
     */
    private void updateStructureMeta(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != OSBlocks.blockLaunchPlatform) {
            return;
        }
        int meta = world.getBlockMetadata(x, y, z);
        int newMeta;
        if (isCompleteStructure(world, x, y, z)) {
            newMeta = 2;
        } else if (isMemberOfComplete(world, x, y, z)) {
            newMeta = 1;
        } else {
            newMeta = 0;
        }
        if (meta != newMeta) {
            world.setBlockMetadataWithNotify(x, y, z, newMeta, 2);
        }
    }

    /** true se algum vizinho é o centro de um 3x3 que inclui (x,y,z). */
    private boolean isMemberOfComplete(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (isCompleteStructure(world, x + dx, y, z + dz)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (!world.isRemote) {
            // Quebrou um bloco: recalcula TUDO em volta antes de qualquer
            // outra coisa (mata o bug do bloco que ficava "montado").
            updateArea(world, x, y, z);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

        ItemStack held = player.getHeldItem();

        // Jogador segurando o FOGUETE DE NAVEGAÇÃO -> posiciona igual ao
        // T1 (multiblock 1x3), mas com o bloco do nav (v0.7.0).
        if (held != null && held.getItem() == OSItems.itemNavRocket) {
            if (world.isRemote) {
                return true; // logica no servidor
            }
            int[] center = findCenter(world, x, y, z);
            if (center == null) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cA plataforma 3x3 precisa estar completa!");
                return true;
            }
            int rx = center[0];
            int ry = center[1] + 1;
            int rz = center[2];
            for (int i = 0; i < 3; i++) {
                if (!world.isAirBlock(rx, ry + i, rz)) {
                    com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cO espa\u00e7o acima do centro da plataforma precisa estar livre (3 blocos)!");
                    return true;
                }
            }
            world.setBlock(rx, ry, rz, OSBlocks.blockNavRocket, 0, 3);
            world.setBlock(rx, ry + 1, rz, OSBlocks.blockNavRocket, 1, 3);
            world.setBlock(rx, ry + 2, rz, OSBlocks.blockNavRocket, 2, 3);
            // Casa = o pad (retorno da órbita cai aqui do lado).
            com.orbitalsunworks.rocket.TileNavRocket tile =
                (com.orbitalsunworks.rocket.TileNavRocket) world.getTileEntity(rx, ry, rz);
            if (tile != null) {
                tile.setHome(world.provider.dimensionId, rx + 2.5, ry, rz + 0.5);
            }
            world.playSoundAtEntity(player, "random.anvil_land", 0.4f, 1.4f);
            if (!player.capabilities.isCreativeMode) {
                held.stackSize--;
                if (held.stackSize <= 0) {
                    player.setCurrentItemOrArmor(0, null);
                }
            }
            com.orbitalsunworks.util.OSUtils.sendChat(player,
                "\u00a7aFoguete de Navega\u00e7\u00e3o posicionado! Clique nele para escolher o sat\u00e9lite.");
            return true;
        }

        // Sem foguete na mão: foguete NAV no centro acima -> abre a GUI dele.
        if (held == null) {
            int[] center = findCenter(world, x, y, z);
            if (center != null && world.getBlock(center[0], center[1] + 1, center[2]) == OSBlocks.blockNavRocket) {
                player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                    com.orbitalsunworks.handler.GuiHandler.ID_NAV_ROCKET, world, center[0], center[1] + 1, center[2]);
                return true;
            }
        }

        // Jogador segurando o foguete -> posiciona no centro da estrutura.
        if (held != null && held.getItem() == OSItems.itemRocketT1) {
            if (world.isRemote) {
                return true; // logica no servidor
            }
            int[] center = findCenter(world, x, y, z);
            if (center == null) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cA plataforma 3x3 precisa estar completa!");
                return true;
            }
            int rx = center[0];
            int ry = center[1] + 1;
            int rz = center[2];
            // Os 3 blocos acima do centro precisam estar livres.
            for (int i = 0; i < 3; i++) {
                if (!world.isAirBlock(rx, ry + i, rz)) {
                    com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cO espaço acima do centro da plataforma precisa estar livre (3 blocos)!");
                    return true;
                }
            }
            // Posiciona a coluna do foguete: base (meta 0), meio (1), topo (2).
            world.setBlock(rx, ry, rz, OSBlocks.blockRocket, 0, 3);
            world.setBlock(rx, ry + 1, rz, OSBlocks.blockRocket, 1, 3);
            world.setBlock(rx, ry + 2, rz, OSBlocks.blockRocket, 2, 3);
            // Copia a vida inicial para o tile da base.
            com.orbitalsunworks.rocket.TileRocket tile =
                (com.orbitalsunworks.rocket.TileRocket) world.getTileEntity(rx, ry, rz);
            if (tile != null) {
                tile.setTier(1);
                tile.setHealth(1.0f);
            }
            world.playSoundAtEntity(player, "random.anvil_land", 0.4f, 1.4f);
            if (!player.capabilities.isCreativeMode) {
                held.stackSize--;
                if (held.stackSize <= 0) {
                    player.setCurrentItemOrArmor(0, null);
                }
            }
            com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7aFoguete Tier 1 posicionado! Clique nele para abrir o painel.");
            return true;
        }

        // Sem foguete na mão: se há um foguete no centro acima, abre a GUI dele.
        if (held == null) {
            int[] center = findCenter(world, x, y, z);
            if (center != null && world.getBlock(center[0], center[1] + 1, center[2]) == OSBlocks.blockRocket) {
                player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                    com.orbitalsunworks.handler.GuiHandler.ID_ROCKET, world, center[0], center[1] + 1, center[2]);
                return true;
            }
        }
        return false;
    }
}
