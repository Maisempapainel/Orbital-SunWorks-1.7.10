package com.orbitalsunworks.craftingstation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;

/**
 * Receitas da Estação de Crafting Orbital (grade 3x5).
 *
 * O sistema suporta:
 * - Receitas "shaped" (formato fixo, 5 colunas x 3 linhas);
 * - Receitas "shapeless" (qualquer posição);
 * - Ingredientes por item OU por nome do OreDictionary (ex.: "ingotCopper"
 *   aceita o cobre deste mod e de qualquer outro mod que o registre).
 *
 * Receitas dos materiais básicos também existem como receitas vanilla 3x3
 * (ver OSCrafting) para facilitar a vida do jogador.
 */
public class CraftingStationRecipes {

    /** Uma receita shaped da grade 3x5. */
    public static class ShapedRecipe {
        public final int width;        // 1-5
        public final int height;       // 1-3
        public final Object[] inputs;  // ItemStack/String por célula, null = vazio
        public final ItemStack output;
        public final int craftTime;    // ticks

        public ShapedRecipe(int width, int height, Object[] inputs, ItemStack output, int craftTime) {
            this.width = width;
            this.height = height;
            this.inputs = inputs;
            this.output = output;
            this.craftTime = craftTime;
        }
    }

    /** Uma receita shapeless (qualquer ordem). */
    public static class ShapelessRecipe {
        public final List<Object> inputs = new ArrayList<Object>();
        public final ItemStack output;
        public final int craftTime;    // ticks

        ShapelessRecipe(ItemStack output, int craftTime) {
            this.output = output;
            this.craftTime = craftTime;
        }
    }

    /** Tempo padrão de craft (ticks) para receitas simples: 3 segundos. */
    public static final int TIME_SMALL = 60;
    /** Receitas médias (painel solar, módulos): 6 segundos. */
    public static final int TIME_MEDIUM = 120;
    /** Receitas grandes (foguete): 25 segundos. */
    public static final int TIME_LARGE = 500;

    private static final List<ShapedRecipe> shapedRecipes = new ArrayList<ShapedRecipe>();
    private static final List<ShapelessRecipe> shapelessRecipes = new ArrayList<ShapelessRecipe>();

    public static void init() {
        // ------------------------------------------------------------------
        // MATERIAIS
        // ------------------------------------------------------------------

        // Fibra de Carbono: carvão + vidro (SHAPED — posição fixa, igual
        // ao manual: carvão na linha de cima, vidro na de baixo).
        addShaped(2, 2, new Object[]{
            new ItemStack(Items.coal), new ItemStack(Items.coal),
            new ItemStack(Blocks.glass_pane), new ItemStack(Blocks.glass_pane)
        }, new ItemStack(OSItems.carbonFiber, 2), TIME_SMALL);

        // Aço: 2 ferro em cima + fibra embaixo (SHAPED, posição fixa).
        addShaped(2, 2, new Object[]{
            new ItemStack(Items.iron_ingot), new ItemStack(Items.iron_ingot),
            new ItemStack(OSItems.carbonFiber), null
        }, new ItemStack(OSItems.ingotSteel, 2), TIME_SMALL);

        // Liga de Alumínio: 3 alumínio em cima + carvão energizado embaixo
        // (SHAPED — a posição do carvão importa).
        addShaped(3, 2, new Object[]{
            "ingotAluminium", "ingotAluminium", "ingotAluminium",
            null, new ItemStack(OSItems.energizedCoal), null
        }, new ItemStack(OSItems.ingotAluminiumAlloy, 3), TIME_SMALL);

        // Liga de Titânio: 3 titânio em cima + carvão energizado embaixo.
        addShaped(3, 2, new Object[]{
            "ingotTitanium", "ingotTitanium", "ingotTitanium",
            null, new ItemStack(OSItems.energizedCoal), null
        }, new ItemStack(OSItems.ingotTitaniumAlloy, 3), TIME_SMALL);

        // Circuito Eletrônico: cobre + estanho + alumínio + redmeg.
        // Formato em cruz (shaped 3x3 dentro da grade 3x5).
        addShaped(3, 3, new Object[]{
            "ingotCopper",  "ingotTin",     "ingotCopper",
            "ingotAluminium", new ItemStack(OSItems.ingotRedmeg), "ingotAluminium",
            "ingotCopper",  "ingotTin",     "ingotCopper"
        }, new ItemStack(OSItems.itemCircuit, 2), TIME_SMALL);

        // Módulo de Controle: circuitos + aço + redmeg.
        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.itemCircuit),
            new ItemStack(OSItems.ingotSteel), new ItemStack(OSItems.ingotRedmeg), new ItemStack(OSItems.ingotSteel),
            null, new ItemStack(OSItems.ingotSteel), null
        }, new ItemStack(OSItems.itemControlModule, 1), TIME_MEDIUM);

        // Antena T1: aero com circuitos
        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.itemCircuit), null,
            null, "ingotCopper", null,
            null, "ingotCopper", null
        }, new ItemStack(OSItems.itemAntennaT1, 1), TIME_MEDIUM);

        // Antenas de tier maior: antena anterior + 2 redmeg + 2 liga de titânio.
        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.ingotRedmeg), null,
            "ingotTitaniumAlloy", antStack(1), "ingotTitaniumAlloy",
            null, new ItemStack(OSItems.ingotRedmeg), null
        }, new ItemStack(OSItems.itemAntennaT2, 1), TIME_MEDIUM);
        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.ingotRedmeg), null,
            "ingotTitaniumAlloy", antStack(2), "ingotTitaniumAlloy",
            null, new ItemStack(OSItems.ingotRedmeg), null
        }, new ItemStack(OSItems.itemAntennaT3, 1), TIME_MEDIUM);
        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.ingotRedmeg), null,
            "ingotTitaniumAlloy", antStack(3), "ingotTitaniumAlloy",
            null, new ItemStack(OSItems.ingotRedmeg), null
        }, new ItemStack(OSItems.itemAntennaT4, 1), TIME_MEDIUM);
        addShaped(3, 3, new Object[]{
            null, new ItemStack(OSItems.ingotYellomite), null,
            "ingotTitaniumAlloy", antStack(4), "ingotTitaniumAlloy",
            null, new ItemStack(OSItems.ingotYellomite), null
        }, new ItemStack(OSItems.itemAntennaT5, 1), TIME_MEDIUM);

        // Painel Solar T1: vidro + silício + circuitos + aço/titânio/redmeg/yellomite.
        addShaped(3, 3, new Object[]{
            new ItemStack(Blocks.glass_pane), new ItemStack(Blocks.glass_pane), new ItemStack(Blocks.glass_pane),
            "itemSilicon", new ItemStack(OSItems.ingotYellomite), "itemSilicon",
            "ingotSteel", "ingotTitanium", new ItemStack(OSItems.itemCircuit)
        }, new ItemStack(OSItems.itemSolarPanelT1, 2), TIME_MEDIUM);

        // ------------------------------------------------------------------
        // BLOCOS
        // ------------------------------------------------------------------

        // Bloco Revestido: ligas + fibra de carbono (o esqueleto do satélite).
        addShaped(3, 3, new Object[]{
            "ingotAluminiumAlloy", new ItemStack(OSItems.carbonFiber), "ingotAluminiumAlloy",
            new ItemStack(OSItems.carbonFiber), "ingotTitaniumAlloy", new ItemStack(OSItems.carbonFiber),
            "ingotAluminiumAlloy", new ItemStack(OSItems.carbonFiber), "ingotAluminiumAlloy"
        }, new ItemStack(OSBlocks.blockCladded, 4), TIME_SMALL);

        // Computador Central: módulo de controle + circuitos + aço.
        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.ingotSteel), new ItemStack(OSItems.itemCircuit),
            new ItemStack(OSItems.ingotSteel), new ItemStack(OSItems.itemControlModule), new ItemStack(OSItems.ingotSteel),
            new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.ingotSteel), new ItemStack(OSItems.itemCircuit)
        }, new ItemStack(OSBlocks.blockComputer, 1), TIME_MEDIUM);

        // Estação de Crafting (a própria estação é craftada em mesa vanilla - ver OSCrafting)

        // Plataforma de Lançamento: aço + bloco revestido.
        addShaped(3, 3, new Object[]{
            "ingotSteel", new ItemStack(OSBlocks.blockCladded), "ingotSteel",
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.itemControlModule), new ItemStack(OSBlocks.blockCladded),
            "ingotSteel", new ItemStack(OSBlocks.blockCladded), "ingotSteel"
        }, new ItemStack(OSBlocks.blockLaunchPlatform, 9), TIME_MEDIUM);

        // Escudo de Detrito (módulo): liga de titânio + fibra + circuito.
        addShaped(3, 3, new Object[]{
            "ingotTitaniumAlloy", new ItemStack(OSItems.carbonFiber), "ingotTitaniumAlloy",
            new ItemStack(OSItems.carbonFiber), new ItemStack(OSItems.itemCircuit), new ItemStack(OSItems.carbonFiber),
            "ingotTitaniumAlloy", new ItemStack(OSItems.carbonFiber), "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemModuleDebrisShield, 1), TIME_MEDIUM);

        // ------------------------------------------------------------------
        // FOGUETE TIER 1 - a primeira receita que usa as 15 posições (3x5).
        // Layout (grade 3x5, cada linha tem 5):
        //   [ A ] [ P ] [ P ] [ P ] [ A ]      A = antena T1, P = painel solar T1
        //   [ B ] [ B ] [ B ] [ B ] [ B ]      B = bloco revestido
        //   [ T ] [ T ] [ C ] [ T ] [ T ]      T = liga de titânio, C = módulo de controle
        // ------------------------------------------------------------------
        addShaped(5, 3, new Object[]{
            new ItemStack(OSItems.itemAntennaT1), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemAntennaT1),
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", "ingotTitaniumAlloy", new ItemStack(OSItems.itemControlModule), "ingotTitaniumAlloy", "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemRocketT1, 1), TIME_LARGE);

        // ------------------------------------------------------------------
        // FOGUETE DE NAVEGAÇÃO (v0.6.0) — o "táxi orbital". Mais leve que
        // o T1 (não leva satélite, só o piloto):
        //   [ P ] [ A ] [ P ]
        //   [ B ] [ C ] [ B ]
        //   [ T ] [ T ] [ T ]
        // ------------------------------------------------------------------
        addShaped(3, 3, new Object[]{
            new ItemStack(OSItems.itemSolarPanelT1), new ItemStack(OSItems.itemAntennaT1), new ItemStack(OSItems.itemSolarPanelT1),
            new ItemStack(OSBlocks.blockCladded), new ItemStack(OSItems.itemControlModule), new ItemStack(OSBlocks.blockCladded),
            "ingotTitaniumAlloy", "ingotTitaniumAlloy", "ingotTitaniumAlloy"
        }, new ItemStack(OSItems.itemNavRocket, 1), TIME_LARGE);
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

    // ------------------------------------------------------------------
    // Registro de receitas
    // ------------------------------------------------------------------

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

    // ------------------------------------------------------------------
    // Busca de receita
    // ------------------------------------------------------------------

    /** Receita encontrada: resultado + tempo de craft. */
    public static class RecipeMatch {
        public final ItemStack output;
        public final int craftTime;

        public RecipeMatch(ItemStack output, int craftTime) {
            this.output = output;
            this.craftTime = craftTime;
        }
    }

    /**
     * Procura uma receita que case com a grade atual (16 slots, 15 de grade).
     * Retorna o match (saída + tempo) ou null.
     */
    public static RecipeMatch findRecipe(ItemStack[] slots) {
        // Converte a grade 3x5 para matriz [row][col].
        ItemStack[][] grid = new ItemStack[3][5];
        for (int i = 0; i < 15; i++) {
            grid[i / 5][i % 5] = slots[i];
        }

        // 1) Receitas shaped: a receita pode estar em qualquer posição dentro
        //    da grade 3x5 (a grade é maior que a receita).
        for (ShapedRecipe recipe : shapedRecipes) {
            for (int rowOff = 0; rowOff <= 3 - recipe.height; rowOff++) {
                for (int colOff = 0; colOff <= 5 - recipe.width; colOff++) {
                    if (matchesShapedAt(recipe, grid, rowOff, colOff)) {
                        return new RecipeMatch(recipe.output, recipe.craftTime);
                    }
                }
            }
        }

        // 2) Receitas shapeless: juntar todos os itens e comparar quantidades.
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

    /** Um ingrediente (ItemStack, String oreDict ou null) casa com o slot? */
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
                && (req.getItemDamage() == net.minecraftforge.oredict.OreDictionary.WILDCARD_VALUE
                    || req.getItemDamage() == present.getItemDamage());
        }
        if (required instanceof String) {
            // Nome OreDictionary: aceita item de qualquer mod registrado.
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
        // Cada ingrediente precisa casar com um item presente, 1 a 1.
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
