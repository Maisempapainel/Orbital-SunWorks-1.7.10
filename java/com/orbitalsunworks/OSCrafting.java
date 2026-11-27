package com.orbitalsunworks;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import net.minecraft.init.Blocks;

/**
 * Receitas que funcionam na crafting table VANILLA (grade 3x3) - materiais
 * mais simples, para o jogador não depender da Estação de Crafting Orbital
 * no comecinho do mod.
 *
 * Receitas de componentes avançados (foguete, bloco revestido, antenas
 * maiores etc) ficam na Estação de Crafting (CraftingStationRecipes).
 */
public class OSCrafting {

    public static void init() {
        // Fibra de carbono: 2 carvão em cima + 2 vidro embaixo (SHAPED).
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSItems.carbonFiber, 2),
            "CC", "GG",
            'C', new ItemStack(Items.coal),
            'G', new ItemStack(Blocks.glass_pane)));

        // Aço: 2 ferro em cima + fibra embaixo (SHAPED).
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSItems.ingotSteel, 2),
            "II", "F ",
            'I', new ItemStack(Items.iron_ingot),
            'F', new ItemStack(OSItems.carbonFiber)));

        // Estação de Crafting Orbital: aço + circuitos + crafting table.
        CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
            new ItemStack(OSBlocks.blockCraftingStation),
            "SCS", "CWC", "SCS",
            'S', "ingotSteel",
            'C', new ItemStack(OSItems.itemCircuit),
            'W', new ItemStack(Blocks.crafting_table)));

        // Manual do Orbital Sunworks: livro + circuito (shapeless).
        CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(
            new ItemStack(OSItems.itemManual, 1),
            new ItemStack(Items.book), new ItemStack(OSItems.itemCircuit)));
    }
}
