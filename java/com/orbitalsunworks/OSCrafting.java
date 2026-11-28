package com.orbitalsunworks;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Receitas da bancada vanilla (3x3).
 * Ligas, componentes e foguetes avançados ficam na Estação de Crafting (CraftingStationRecipes).
 */
public class OSCrafting {

    public static void init() {
        // Fibra de carbono: 2 carvão em cima + 2 vidro embaixo
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSItems.carbonFiber, 2),
            "CC", "GG",
            'C', new ItemStack(Items.coal),
            'G', new ItemStack(Blocks.glass_pane)));

        // Aço: 2 ferro em cima + fibra embaixo
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSItems.ingotSteel, 2),
            "II", "F ",
            'I', new ItemStack(Items.iron_ingot),
            'F', new ItemStack(OSItems.carbonFiber)));

        // Estação de Crafting Orbital: aço + circuitos + crafting table
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSBlocks.blockCraftingStation),
            "SCS", "CWC", "SCS",
            'S', "ingotSteel",
            'C', new ItemStack(OSItems.itemCircuit),
            'W', new ItemStack(Blocks.crafting_table)));

        // Kit de Reparo (2x): recupera +10% da vida útil do satélite em órbita
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSItems.itemRepairKit, 2),
            "STS", "TCT", "SRS",
            'S', "ingotSteel",
            'T', "ingotTitaniumAlloy",
            'C', new ItemStack(OSItems.itemCircuit),
            'R', new ItemStack(Items.redstone)));

        // Manual do Orbital Sunworks: livro + circuito (shapeless)
        CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(
            new ItemStack(OSItems.itemManual, 1),
            new ItemStack(Items.book), new ItemStack(OSItems.itemCircuit)));
    }
}