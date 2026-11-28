package com.orbitalsunworks.craftingstation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;

/**
 * Receitas da Estação de Crafting Orbital (grade 3x5).
 */
public class CraftingStationRecipes {

    public static class ShapedRecipe {
        public final int width;
        public final int height;
        public final Object[] inputs;
        public final ItemStack output;
        public final int craftTime;

        public ShapedRecipe(int width, int height, Object[] inputs, ItemStack output, int craftTime) {
            this.width = width;
            this.height = height;
            this.inputs = inputs;
            this.output = output;
            this.craftTime = craftTime;
        }
    }

    public static class ShapelessRecipe {
        public final List<Object> inputs = new ArrayList<Object>();
        public final ItemStack output;
        public final int craftTime;

        ShapelessRecipe(ItemStack output, int craftTime) {
            this.output = output;
            this.craftTime = craftTime;
        }
    }

    public static final int TIME_SMALL = 60;
    public static final int TIME_MEDIUM = 120;
    public static final int TIME_LARGE = 500;

    private static final List<ShapedRecipe> shapedRecipes = new ArrayList<ShapedRecipe>();
    private static final List<ShapelessRecipe> shapelessRecipes = new ArrayList<ShapelessRecipe>();

    public static void init() {
        // ------------------------------------------------------------------
        // MATERIAIS & COMPONENTES
        // ------------------------------------------------------------------
        addShaped(2, 2, new Object[]{
            new ItemStack(Items.coal), new ItemStack(Items.coal),
            new ItemStack(Blocks.glass_pane), new ItemStack(Blocks.glass_pane)
        }, new ItemStack(OSItems.carbonFiber, 2), TIME_SMALL);

        addShaped(2, 2, new Object[]{
            "itemSilicon", new ItemStack(Blocks.glass),
            new ItemStack(Blocks.glass), "itemSilicon"
        }, new ItemStack(OSItems.itemGlassFiber, 2), TIME_SMALL);

        addShaped(2, 2, new Object[]{
            new ItemStack(Items.iron_ingot), new ItemStack(Items.iron_ingot),
            new ItemStack(OSItems.carbonFiber), null
        }, new ItemStack(OSItems.ingotSteel, 2), TIME_SMALL);

        addShaped(3, 2, new Object[]{
            "ingotAluminium", "ingotAluminium", "ingotAluminium",
            null, new ItemStack(OSItems.energizedCoal), null
        }, new ItemStack(OSItems.ingotAluminiumAlloy, 3), TIME_SMALL);

        addShaped(3, 2, new Object[]{
            "ingotTitanium", "ingotTitanium", "ingotTitanium",
            null, new ItemStack(OSItems.energizedCoal), null
        }, new ItemStack(OSItems.ingotTitaniumAlloy, 3), TIME_SMALL);

        addShaped(3, 3, new Object[]{
            "ingotCopper",    "ingotTin",     "ingotCopper",
            "ingotAluminium", new ItemStack(OSItems.ingotRedmeg), "ingotAluminium",
            "ingotCopper",    "ingotTin",     "ingotCopper"
        }, new ItemStack(OSItems.itemCircuit, 2), TIME_SMALL);

        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.itemCircuit),
            new ItemStack(OSItems.ingotSteel), new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSItems.ingotSteel),
            null, new ItemStack(OSItems.ingotSteel), null
        }, new ItemStack(OSItems.itemControlModule, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.itemCircuit), null,
            "itemSilicon", new ItemStack(Items.redstone), "itemSilicon",
            "ingotSteel", "ingotSteel", "ingotSteel"
        }, new ItemStack(OSItems.itemOilSensor, 1), TIME_SMALL);

        // Tanque de Oxigênio (EVA)
        addShaped(3, 3, new Object[]{
            null, "ingotSteel", null,
            "ingotAluminiumAlloy", new ItemStack(OSItems.itemGlassFiber), "ingotAluminiumAlloy",
            null, "ingotSteel", null
        }, new ItemStack(OSItems.itemOxygenTank, 1), TIME_MEDIUM);

        // Propulsores a Gás (EVA)
        addShaped(3, 3, new Object[]{
            "ingotTitaniumAlloy", new ItemStack(OSItems.itemCircuit), "ingotTitaniumAlloy",
            "ingotAluminiumAlloy", new ItemStack(OSItems.ingotRedmeg), "ingotAluminiumAlloy",
            new ItemStack(OSItems.carbonFiber), null, new ItemStack(OSItems.carbonFiber)
        }, new ItemStack(OSItems.itemGasThrusters, 1), TIME_MEDIUM);

        // Kevlar Vidro-Fibroso (TODOS os 15 slots da 3x5 -> 1x)
        addShaped(5, 3, new Object[]{
            new ItemStack(OSItems.itemKevlar), new ItemStack(OSItems.itemGlassFiber), new ItemStack(OSItems.carbonFiber), new ItemStack(OSItems.itemGlassFiber), new ItemStack(OSItems.itemKevlar),
            new ItemStack(OSItems.itemGlassFiber), new ItemStack(OSItems.itemKevlar), new ItemStack(OSItems.itemKevlar), new ItemStack(OSItems.itemKevlar), new ItemStack(OSItems.itemGlassFiber),
            new ItemStack(OSItems.itemKevlar), new ItemStack(OSItems.carbonFiber), new ItemStack(OSItems.itemGlassFiber), new ItemStack(OSItems.carbonFiber), new ItemStack(OSItems.itemKevlar)
        }, new ItemStack(OSItems.itemKevlarFiberglass, 1), TIME_MEDIUM);

        // ------------------------------------------------------------------
        // TRAJE ESPACIAL EVA (24 Kevlar Vidro-Fibroso no total)
        // ------------------------------------------------------------------
        // Capacete Espacial (5 KVF)
        addShaped(3, 2, new Object[]{
            new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass),
            new ItemStack(OSItems.itemKevlarFiberglass), null, new ItemStack(OSItems.itemKevlarFiberglass)
        }, new ItemStack(OSItems.spaceHelmet, 1), TIME_MEDIUM);

        // Torso Espacial (8 KVF + 1 Tanque de Oxigênio)
        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemOxygenTank), new ItemStack(OSItems.itemKevlarFiberglass),
            new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass),
            new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass)
        }, new ItemStack(OSItems.spaceChestplate, 1), TIME_LARGE);

        // Calça Espacial (7 KVF)
        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemKevlarFiberglass),
            new ItemStack(OSItems.itemKevlarFiberglass), null, new ItemStack(OSItems.itemKevlarFiberglass),
            new ItemStack(OSItems.itemKevlarFiberglass), null, new ItemStack(OSItems.itemKevlarFiberglass)
        }, new ItemStack(OSItems.spaceLeggings, 1), TIME_MEDIUM);

        // Botas Espaciais (4 KVF + 1 Propulsores a Gás)
        addShaped(3, 2, new Object[]{
            new ItemStack(OSItems.itemKevlarFiberglass), null, new ItemStack(OSItems.itemKevlarFiberglass),
            new ItemStack(OSItems.itemKevlarFiberglass), new ItemStack(OSItems.itemGasThrusters), new ItemStack(OSItems.itemKevlarFiberglass)
        }, new ItemStack(OSItems.spaceBoots, 1), TIME_MEDIUM);

        // ------------------------------------------------------------------
        // MÁQUINAS PETROQUÍMICAS & RF
        // ------------------------------------------------------------------
        // Entrada de Energia RF
        addShaped(3, 3, new Object[]{
            "ingotSteel", new ItemStack(Items.redstone), "ingotSteel",
            "ingotCopper", new ItemStack(OSItems.itemCircuit), "ingotCopper",
            "ingotSteel", new ItemStack(Items.redstone), "ingotSteel"
        }, new ItemStack(OSBlocks.blockEnergyInput, 1), TIME_SMALL);

        // Extrator de Petróleo (3x5)
        addShaped(5, 3, new Object[]{
            "ingotSteel", "ingotAluminiumAlloy", new ItemStack(OSItems.itemCircuit), "ingotAluminiumAlloy", "ingotSteel",
            new ItemStack(OSBlocks.blockCladded), "ingotSteel", new ItemStack(OSItems.itemControlModule), "ingotSteel", new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockEnergyInput), new ItemStack(OSBlocks.blockCladded), "ingotTitaniumAlloy"
        }, new ItemStack(OSBlocks.blockOilExtractor, 1), TIME_LARGE);

        // Refinador de Petróleo (3x5)
        addShaped(5, 3, new Object[]{
            "ingotSteel", new ItemStack(Blocks.glass), new ItemStack(OSItems.itemCircuit), new ItemStack(Blocks.glass), "ingotSteel",
            "ingotAluminiumAlloy", new ItemStack(Items.bucket), new ItemStack(OSItems.itemControlModule), new ItemStack(Items.bucket), "ingotAluminiumAlloy",
            "ingotTitaniumAlloy", "ingotSteel", new ItemStack(OSBlocks.blockEnergyInput), "ingotSteel", "ingotTitaniumAlloy"
        }, new ItemStack(OSBlocks.blockOilRefiner, 1), TIME_LARGE);

        // Máquina Sintetizadora (3x5)
        addShaped(5, 3, new Object[]{
            "ingotTitaniumAlloy", new ItemStack(OSItems.ingotYellomite), new ItemStack(OSItems.itemControlModule), new ItemStack(OSItems.ingotYellomite), "ingotTitaniumAlloy",
            new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.itemGlassFiber), new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.ingotRedmeg),
            new ItemStack(OSBlocks.blockCladded), "ingotSteel", new ItemStack(OSBlocks.blockEnergyInput), "ingotSteel", new ItemStack(OSBlocks.blockCladded)
        }, new ItemStack(OSBlocks.blockSynthesizer, 1), TIME_LARGE);

        // ------------------------------------------------------------------
        // ANTENAS T1-T5, PAINÉIS, BLOCOS & MÓDULOS
        // ------------------------------------------------------------------
        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.itemCircuit), null,
            null, "ingotCopper", null,
            null, "ingotCopper", null
        }, new ItemStack(OSItems.itemAntennaT1, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            null, antStack(1), null,
            "ingotAluminiumAlloy", new ItemStack(OSItems.itemCircuit), "ingotAluminiumAlloy",
            null, "ingotSteel", null
        }, new ItemStack(OSItems.itemAntennaT2, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            null, antStack(2), null,
            "ingotTitaniumAlloy", new ItemStack(OSItems.itemControlModule), "ingotTitaniumAlloy",
            null, "ingotSteel", null
        }, new ItemStack(OSItems.itemAntennaT3, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            null, antStack(3), null,
            new ItemStack(OSItems.ingotYellomite), new ItemStack(OSItems.itemControlModule), new ItemStack(OSItems.ingotYellomite),
            null, "ingotTitaniumAlloy", null
        }, new ItemStack(OSItems.itemAntennaT4, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            null, antStack(4), null,
            new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSItems.itemControlModule), new ItemStack(OSItems.ingotRedmeg),
            "ingotTitaniumAlloy", new ItemStack(OSItems.ingotYellomite), "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemAntennaT5, 1), TIME_MEDIUM);

        // Painel Solar T1
        addShaped(3, 3, new Object[]{
            "itemSilicon", "itemSilicon", "itemSilicon",
            new ItemStack(Blocks.glass), "ingotCopper", new ItemStack(Blocks.glass),
            "ingotSteel", new ItemStack(OSItems.itemCircuit), "ingotSteel"
        }, new ItemStack(OSItems.itemSolarPanelT1, 1), TIME_MEDIUM);

        // Bloco Blindado (4x)
        addShaped(3, 3, new Object[]{
            "ingotSteel", "ingotAluminiumAlloy", "ingotSteel",
            "ingotAluminiumAlloy", new ItemStack(OSItems.carbonFiber), "ingotAluminiumAlloy",
            "ingotSteel", "ingotAluminiumAlloy", "ingotSteel"
        }, new ItemStack(OSBlocks.blockCladded, 4), TIME_SMALL);

        // Plataforma de Lançamento (4x)
        addShaped(3, 3, new Object[]{
            "ingotSteel", "ingotSteel", "ingotSteel",
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.itemCircuit), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", "ingotTitaniumAlloy", "ingotTitaniumAlloy"
        }, new ItemStack(OSBlocks.blockLaunchPlatform, 4), TIME_SMALL);

        // Computador de Bordo
        addShaped(3, 3, new Object[]{
            "ingotSteel", new ItemStack(Blocks.glass_pane), "ingotSteel",
            new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.itemControlModule), new ItemStack(OSItems.itemCircuit),
            "ingotSteel", "ingotCopper", "ingotSteel"
        }, new ItemStack(OSBlocks.blockComputer, 1), TIME_MEDIUM);

        // Módulos de Escudo T1-T3
        addShaped(3, 3, new Object[]{
            "ingotSteel", "ingotTitaniumAlloy", "ingotSteel",
            "ingotTitaniumAlloy", new ItemStack(OSItems.itemCircuit), "ingotTitaniumAlloy",
            "ingotSteel", new ItemStack(OSItems.carbonFiber), "ingotSteel"
        }, new ItemStack(OSItems.itemModuleDebrisShield, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            "ingotAluminiumAlloy", new ItemStack(OSItems.itemModuleDebrisShield), "ingotAluminiumAlloy",
            "ingotTitaniumAlloy", new ItemStack(OSItems.itemControlModule), "ingotTitaniumAlloy",
            "ingotSteel", new ItemStack(OSItems.ingotYellomite), "ingotSteel"
        }, new ItemStack(OSItems.itemModuleDebrisShieldT2, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSItems.itemModuleDebrisShieldT2), new ItemStack(OSItems.ingotRedmeg),
            "ingotTitaniumAlloy", new ItemStack(OSItems.itemControlModule), "ingotTitaniumAlloy",
            new ItemStack(OSItems.ingotYellomite), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.ingotYellomite)
        }, new ItemStack(OSItems.itemModuleDebrisShieldT3, 1), TIME_MEDIUM);

        // Módulos de Eficiência T1-T3
        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.ingotYellomite), new ItemStack(OSItems.itemSolarPanelT1),
            "ingotAluminiumAlloy", new ItemStack(OSItems.itemCircuit), "ingotAluminiumAlloy",
            "ingotSteel", new ItemStack(OSItems.ingotRedmeg), "ingotSteel"
        }, new ItemStack(OSItems.itemModuleEfficiencyT1, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.ingotYellomite), new ItemStack(OSItems.itemModuleEfficiencyT1), new ItemStack(OSItems.ingotYellomite),
            "ingotTitaniumAlloy", new ItemStack(OSItems.ingotRedmeg), "ingotTitaniumAlloy",
            new ItemStack(OSItems.ingotYellomite), new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.ingotYellomite)
        }, new ItemStack(OSItems.itemModuleEfficiencyT2, 1), TIME_MEDIUM);

        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSItems.itemModuleEfficiencyT2), new ItemStack(OSItems.ingotRedmeg),
            "ingotTitaniumAlloy", new ItemStack(OSItems.itemControlModule), "ingotTitaniumAlloy",
            new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.ingotYellomite), new ItemStack(OSItems.itemSolarPanelT1)
        }, new ItemStack(OSItems.itemModuleEfficiencyT3, 1), TIME_MEDIUM);

        // ------------------------------------------------------------------
        // FOGUETES & ESTAÇÃO DE CONTROLE (3x5)
        // ------------------------------------------------------------------
        addShaped(5, 3, new Object[]{
            new ItemStack(OSItems.itemAntennaT1), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemAntennaT1),
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", "ingotTitaniumAlloy", new ItemStack(OSItems.itemControlModule), "ingotTitaniumAlloy", "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemRocketT1, 1), TIME_LARGE);

        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemAntennaT1), new ItemStack(OSItems.itemSolarPanelT1),
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.itemControlModule), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", "ingotTitaniumAlloy", "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemNavRocket, 1), TIME_LARGE);

        addShaped(5, 3, new Object[]{
            new ItemStack(OSItems.ingotRedmeg), new ItemStack(Items.gunpowder), new ItemStack(OSItems.itemCircuit), new ItemStack(Items.gunpowder), new ItemStack(OSItems.ingotRedmeg),
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.itemControlModule), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", new ItemStack(OSItems.carbonFiber), new ItemStack(OSItems.itemAntennaT1), new ItemStack(OSItems.carbonFiber), "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemDispersionRocket, 1), TIME_MEDIUM);

        addShaped(5, 3, new Object[]{
            antStack(2), new ItemStack(OSItems.ingotYellomite), new ItemStack(OSBlocks.blockComputer), new ItemStack(OSItems.ingotYellomite), antStack(2),
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemControlModule), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSBlocks.blockComputer), new ItemStack(OSItems.ingotRedmeg), "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemControlStation, 1), TIME_LARGE);
    }

    private static ItemStack antStack(int tier) {
        switch (tier) {
            case 1: return new ItemStack(OSItems.itemAntennaT1);
            case 2: return new ItemStack(OSItems.itemAntennaT2);
            case 3: return new ItemStack(OSItems.itemAntennaT3);
            case 4: return new ItemStack(OSItems.itemAntennaT4);
            default: return new ItemStack(OSItems.itemAntennaT5);
        }
    }

    public static void addShaped(int width, int height, Object[] inputs, ItemStack output, int craftTime) {
        shapedRecipes.add(new ShapedRecipe(width, height, inputs, output, craftTime));
    }

    public static void addShapeless(ItemStack output, int craftTime, Object... inputs) {
        ShapelessRecipe recipe = new ShapelessRecipe(output, craftTime);
        for (Object o : inputs) {
            recipe.inputs.add(o);
        }
        shapelessRecipes.add(recipe);
    }

    public static class RecipeMatch {
        public final ItemStack output;
        public final int craftTime;

        public RecipeMatch(ItemStack output, int craftTime) {
            this.output = output;
            this.craftTime = craftTime;
        }
    }

    public static RecipeMatch findRecipe(ItemStack[] slots) {
        ItemStack[][] grid = new ItemStack[3][5];
        for (int i = 0; i < 15; i++) {
            grid[i / 5][i % 5] = slots[i];
        }

        for (ShapedRecipe recipe : shapedRecipes) {
            for (int rowOff = 0; rowOff <= 3 - recipe.height; rowOff++) {
                for (int colOff = 0; colOff <= 5 - recipe.width; colOff++) {
                    if (matchesShapedAt(recipe, grid, rowOff, colOff)) {
                        return new RecipeMatch(recipe.output, recipe.craftTime);
                    }
                }
            }
        }

        List<ItemStack> present = new ArrayList<ItemStack>();
        for (int i = 0; i < 15; i++) {
            if (slots[i] != null) {
                present.add(slots[i]);
            }
        }
        for (ShapelessRecipe recipe : shapelessRecipes) {
            if (matchesShapeless(recipe, present)) {
                return new RecipeMatch(recipe.output, recipe.craftTime);
            }
        }
        return null;
    }

    private static boolean matchesShapedAt(ShapedRecipe recipe, ItemStack[][] grid, int rowOff, int colOff) {
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 5; c++) {
                ItemStack inGrid = grid[r][c];
                boolean insideRecipe = r >= rowOff && r < rowOff + recipe.height
                    && c >= colOff && c < colOff + recipe.width;
                Object required = insideRecipe
                    ? recipe.inputs[(r - rowOff) * recipe.width + (c - colOff)]
                    : null;
                if (!ingredientMatches(required, inGrid)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean ingredientMatches(Object required, ItemStack present) {
        if (required == null) {
            return present == null;
        }
        if (present == null) {
            return false;
        }
        if (required instanceof ItemStack) {
            ItemStack req = (ItemStack) required;
            return req.getItem() == present.getItem()
                && (req.getItemDamage() == OreDictionary.WILDCARD_VALUE
                    || req.getItemDamage() == present.getItemDamage());
        }
        if (required instanceof String) {
            for (ItemStack ore : OreDictionary.getOres((String) required)) {
                if (ore.getItem() == present.getItem()
                    && (ore.getItemDamage() == OreDictionary.WILDCARD_VALUE
                        || ore.getItemDamage() == present.getItemDamage())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean matchesShapeless(ShapelessRecipe recipe, List<ItemStack> present) {
        if (present.size() != recipe.inputs.size()) {
            return false;
        }
        List<ItemStack> restantes = new ArrayList<ItemStack>(present);
        for (Object required : recipe.inputs) {
            boolean found = false;
            for (int i = 0; i < restantes.size(); i++) {
                if (ingredientMatches(required, restantes.get(i))) {
                    restantes.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return restantes.isEmpty();
    }
}