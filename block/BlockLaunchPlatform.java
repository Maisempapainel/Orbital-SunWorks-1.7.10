package com.orbitalsunworks.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.item.ItemDispersionRocket;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLaunchPlatform extends BlockContainer {

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
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileLaunchPlatform();
    }

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
        return -1;
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 2) {
            this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        } else {
            this.setBlockBounds(0.0f, 0.0f, 0.0f, 1.0f, 0.28f, 1.0f);
        }
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
        return this.iconPlain;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        if (side == 1) {
            if (meta == 2) {
                return this.iconCenter;
            }
            if (meta == 1) {
                return this.iconAssembled;
            }
        }
        return this.iconPlain;
    }

    public static boolean isCompleteStructure(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != OSBlocks.blockLaunchPlatform) {
            return false;
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                if (world.getBlock(x + dx, y, z + dz) != OSBlocks.blockLaunchPlatform) {
                    return false;
                }
            }
        }
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                boolean inner = Math.abs(dx) <= 3 && Math.abs(dz) <= 3;
                boolean ringCorner = Math.abs(dx) == 4 && Math.abs(dz) == 4;
                if (inner || ringCorner) {
                    continue;
                }
                if (world.getBlock(x + dx, y, z + dz) == OSBlocks.blockLaunchPlatform) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[] findCenter(World world, int x, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (isCompleteStructure(world, x + dx, y, z + dz)) {
                    return new int[] { x + dx, y, z + dz };
                }
            }
        }
        return null;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        if (!world.isRemote) {
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

    private void updateArea(World world, int x, int y, int z) {
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                updateStructureMeta(world, x + dx, y, z + dz);
            }
        }
    }

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

    private boolean isMemberOfComplete(World world, int x, int y, int z) {
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
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
            updateArea(world, x, y, z);
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z,
            EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

        ItemStack held = player.getHeldItem();

        if (held != null && held.getItem() == OSItems.itemNavRocket) {
            if (world.isRemote) {
                return true;
            }
            int[] center = findCenter(world, x, y, z);
            if (center == null) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cA plataforma 7x7 precisa estar completa!");
                return true;
            }
            int rx = center[0];
            int ry = center[1] + 1;
            int rz = center[2];
            for (int i = 0; i < BlockNavRocket.HEIGHT; i++) {
                if (!world.isAirBlock(rx, ry + i, rz)) {
                    com.orbitalsunworks.util.OSUtils.sendChat(player,
                        "\u00a7cO espa\u00e7o acima do centro da plataforma precisa estar livre (" + BlockNavRocket.HEIGHT + " blocos)!");
                    return true;
                }
            }
            for (int i = 0; i < BlockNavRocket.HEIGHT; i++) {
                world.setBlock(rx, ry + i, rz, OSBlocks.blockNavRocket, i, 3);
            }
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

        if (held != null && held.getItem() == OSItems.itemRocketT1) {
            if (world.isRemote) {
                return true;
            }
            int[] center = findCenter(world, x, y, z);
            if (center == null) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cA plataforma 7x7 precisa estar completa!");
                return true;
            }
            int rx = center[0];
            int ry = center[1] + 1;
            int rz = center[2];
            for (int i = 0; i < BlockRocket.HEIGHT; i++) {
                if (!world.isAirBlock(rx, ry + i, rz)) {
                    com.orbitalsunworks.util.OSUtils.sendChat(player,
                        "\u00a7cO espa\u00e7o acima do centro da plataforma precisa estar livre (" + BlockRocket.HEIGHT + " blocos)!");
                    return true;
                }
            }
            for (int i = 0; i < BlockRocket.HEIGHT; i++) {
                world.setBlock(rx, ry + i, rz, OSBlocks.blockRocket, i, 3);
            }
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
            com.orbitalsunworks.util.OSUtils.sendChat(player,
                "\u00a7aFoguete Tier 1 posicionado! Clique nele para abrir o painel.");
            return true;
        }

        if (held != null && held.getItem() == OSItems.itemDispersionRocket) {
            if (world.isRemote) {
                return true;
            }
            int[] center = findCenter(world, x, y, z);
            if (center == null) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cA plataforma 7x7 precisa estar completa!");
                return true;
            }
            return ItemDispersionRocket.tryPlaceOnCenter(world, center[0], center[1], center[2], player, held);
        }

        if (held == null) {
            int[] center = findCenter(world, x, y, z);
            if (center != null) {
                Block above = world.getBlock(center[0], center[1] + 1, center[2]);
                if (above == OSBlocks.blockNavRocket) {
                    if (!world.isRemote) {
                        player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                            com.orbitalsunworks.handler.GuiHandler.ID_NAV_ROCKET, world, center[0], center[1] + 1, center[2]);
                    }
                    return true;
                }
                if (above == OSBlocks.blockRocket) {
                    if (!world.isRemote) {
                        player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                            com.orbitalsunworks.handler.GuiHandler.ID_ROCKET, world, center[0], center[1] + 1, center[2]);
                    }
                    return true;
                }
                if (above == OSBlocks.blockDispersionRocket) {
                    if (!world.isRemote) {
                        player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                            com.orbitalsunworks.handler.GuiHandler.ID_DISPERSION_ROCKET, world, center[0], center[1] + 1, center[2]);
                    }
                    return true;
                }
            }
        }
        return false;
    }
}