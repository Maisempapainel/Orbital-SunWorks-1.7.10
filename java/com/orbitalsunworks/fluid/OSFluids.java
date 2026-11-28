package com.orbitalsunworks.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.Reference;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Registro dos dois líquidos do mod: Petróleo (Bruto) e Petróleo Refinado,
 * seus blocos de fluido no mundo e suporte a baldes/tubulações Forge.
 */
public class OSFluids {

    public static Fluid fluidCrudeOil;
    public static Fluid fluidRefinedOil;

    public static BlockFluidOil blockCrudeOil;
    public static BlockFluidOil blockRefinedOil;

    public static void init() {
        fluidCrudeOil = new Fluid("orbitalsunworks_crude_oil")
            .setDensity(2400)
            .setViscosity(4500)
            .setTemperature(300)
            .setUnlocalizedName("crudeOil");
        FluidRegistry.registerFluid(fluidCrudeOil);

        fluidRefinedOil = new Fluid("orbitalsunworks_refined_oil")
            .setDensity(850)
            .setViscosity(950)
            .setTemperature(310)
            .setUnlocalizedName("refinedOil");
        FluidRegistry.registerFluid(fluidRefinedOil);

        blockCrudeOil = new BlockFluidOil(fluidCrudeOil, "fluidCrudeOil",
            "fluid_oil_still", "fluid_oil_flow");
        GameRegistry.registerBlock(blockCrudeOil, "fluidCrudeOil");
        fluidCrudeOil.setBlock(blockCrudeOil);

        blockRefinedOil = new BlockFluidOil(fluidRefinedOil, "fluidRefinedOil",
            "fluid_refined_oil_still", "fluid_refined_oil_flow");
        GameRegistry.registerBlock(blockRefinedOil, "fluidRefinedOil");
        fluidRefinedOil.setBlock(blockRefinedOil);
    }

    public static void registerContainersAndEvents() {
        FluidContainerRegistry.registerFluidContainer(
            fluidCrudeOil,
            new ItemStack(OSItems.bucketCrudeOil),
            new ItemStack(Items.bucket));
        FluidContainerRegistry.registerFluidContainer(
            fluidRefinedOil,
            new ItemStack(OSItems.bucketRefinedOil),
            new ItemStack(Items.bucket));
        MinecraftForge.EVENT_BUS.register(new BucketHandler());
    }

    public static class BlockFluidOil extends BlockFluidClassic {

        private final String stillTex;
        private final String flowTex;

        @SideOnly(Side.CLIENT)
        protected IIcon stillIcon;
        @SideOnly(Side.CLIENT)
        protected IIcon flowingIcon;

        public BlockFluidOil(Fluid fluid, String name, String stillTex, String flowTex) {
            super(fluid, Material.water);
            this.stillTex = stillTex;
            this.flowTex = flowTex;
            this.setBlockName(name);
            this.setCreativeTab(null);
            this.setLightOpacity(2);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void registerBlockIcons(IIconRegister reg) {
            this.stillIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + this.stillTex);
            this.flowingIcon = reg.registerIcon(Reference.RESOURCE_PREFIX + this.flowTex);
            this.getFluid().setIcons(this.stillIcon, this.flowingIcon);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public IIcon getIcon(int side, int meta) {
            return (side == 0 || side == 1) ? this.stillIcon : this.flowingIcon;
        }
    }

    public static class ItemOilBucket extends ItemBucket {

        public ItemOilBucket(Block containedBlock, String name) {
            super(containedBlock);
            this.setUnlocalizedName(name);
            this.setTextureName(Reference.RESOURCE_PREFIX + name);
            this.setCreativeTab(OSCreativeTab.INSTANCE);
            this.setContainerItem(Items.bucket);
            this.setMaxStackSize(1);
        }
    }

    public static class BucketHandler {

        @SubscribeEvent
        public void onBucketFill(FillBucketEvent event) {
            World world = event.world;
            MovingObjectPosition pos = event.target;
            if (world == null || pos == null || pos.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
                return;
            }
            int x = pos.blockX;
            int y = pos.blockY;
            int z = pos.blockZ;
            Block block = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);
            if (meta != 0) {
                return;
            }
            if (block == blockCrudeOil) {
                world.setBlockToAir(x, y, z);
                event.result = new ItemStack(OSItems.bucketCrudeOil);
                event.setResult(Event.Result.ALLOW);
            } else if (block == blockRefinedOil) {
                world.setBlockToAir(x, y, z);
                event.result = new ItemStack(OSItems.bucketRefinedOil);
                event.setResult(Event.Result.ALLOW);
            }
        }
    }
}