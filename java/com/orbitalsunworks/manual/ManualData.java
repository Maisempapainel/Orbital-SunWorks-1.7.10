package com.orbitalsunworks.manual;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.craftingstation.CraftingStationRecipes;

/**
 * Dados do Manual do Orbital Sunworks - todas as receitas organizadas em
 * páginas, espelhando o que está registrado no jogo:
 *
 * - TYPE_FURNACE: minério -> lingote (ícone de fornalha);
 * - TYPE_VANILLA: receitas da mesa 3x3 vanilla;
 * - TYPE_STATION: receitas da Estação de Crafting Orbital (3x5) — com o
 *   tempo de fabricação de cada uma (o craft da estação é temporizado).
 *
 * Cada página tem: tipo, título, grade de ingredientes (linha por linha
 * com até 5 colunas), resultado (item + quantidade) e tempo de craft.
 */
public class ManualData {

    public static final int TYPE_FURNACE = 0;
    public static final int TYPE_VANILLA = 1;
    public static final int TYPE_STATION = 2;

    public static class Page {
        public final int type;
        public final String titleKey;
        /** Grade: até 15 entradas (3 linhas x 5 colunas), null = vazio. */
        public final ItemStack[] grid = new ItemStack[15];
        public ItemStack result;
        public int resultCount = 1;
        /** Tempo de craft na estação, em segundos (0 = não aplicável). */
        public int craftSeconds = 0;

        public Page(int type, String titleKey) {
            this.type = type;
            this.titleKey = titleKey;
        }
    }

    /** Todas as páginas do manual, em ordem de progressão do jogo. */
    public static Page[] buildPages() {
        java.util.List<Page> pages = new java.util.ArrayList<Page>();

        // ------------------------------------------------------------------
        // FORNALHA (8)
        // ------------------------------------------------------------------
        pages.add(furnace("manual.os.smelt.redmeg", OSBlocks.oreRedmeg, OSItems.ingotRedmeg, 1));
        pages.add(furnace("manual.os.smelt.yellomite", OSBlocks.oreYellomite, OSItems.ingotYellomite, 1));
        pages.add(furnace("manual.os.smelt.copper", OSBlocks.oreCopper, OSItems.ingotCopper, 1));
        pages.add(furnace("manual.os.smelt.tin", OSBlocks.oreTin, OSItems.ingotTin, 1));
        pages.add(furnace("manual.os.smelt.aluminium", OSBlocks.oreAluminium, OSItems.ingotAluminium, 1));
        pages.add(furnace("manual.os.smelt.titanium", OSBlocks.oreTitanium, OSItems.ingotTitanium, 1));
        pages.add(furnace("manual.os.smelt.coal", OSBlocks.oreEnergizedCoal, OSItems.energizedCoal, 2));
        pages.add(furnace("manual.os.smelt.silicon", OSBlocks.oreSilicon, OSItems.itemSilicon, 1));

        // ------------------------------------------------------------------
        // MESA VANILLA (4)
        // ------------------------------------------------------------------
        Page p = vanilla("manual.os.craft.fiber");
        p.grid[3] = new ItemStack(Items.coal);
        p.grid[4] = new ItemStack(Items.coal);
        p.grid[8] = new ItemStack(Blocks.glass_pane);
        p.grid[9] = new ItemStack(Blocks.glass_pane);
        p.result = new ItemStack(OSItems.carbonFiber);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("manual.os.craft.steel");
        p.grid[3] = new ItemStack(Items.iron_ingot);
        p.grid[4] = new ItemStack(Items.iron_ingot);
        p.grid[8] = new ItemStack(OSItems.carbonFiber);
        p.result = new ItemStack(OSItems.ingotSteel);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("manual.os.craft.station");
        p.grid[0] = ingotSteel(); p.grid[1] = circuit(); p.grid[2] = ingotSteel();
        p.grid[3] = circuit();    p.grid[4] = new ItemStack(Blocks.crafting_table); p.grid[5] = circuit();
        p.grid[6] = ingotSteel(); p.grid[7] = circuit(); p.grid[8] = ingotSteel();
        p.result = new ItemStack(OSBlocks.blockCraftingStation);
        p.resultCount = 1;
        pages.add(p);

        p = vanilla("manual.os.craft.manual");
        p.grid[4] = new ItemStack(Items.book);
        p.grid[5] = circuit();
        p.result = new ItemStack(OSItems.itemManual);
        p.resultCount = 1;
        pages.add(p);

        // ------------------------------------------------------------------
        // ESTAÇÃO DE CRAFTING (15) — com tempos
        // ------------------------------------------------------------------
        p = station("manual.os.craft.alloyAlu", 3);
        p.grid[0] = ingot("ingotAluminium"); p.grid[1] = ingot("ingotAluminium"); p.grid[2] = ingot("ingotAluminium");
        p.grid[6] = new ItemStack(OSItems.energizedCoal);
        p.result = new ItemStack(OSItems.ingotAluminiumAlloy);
        p.resultCount = 3;
        pages.add(p);

        p = station("manual.os.craft.alloyTi", 3);
        p.grid[0] = ingot("ingotTitanium"); p.grid[1] = ingot("ingotTitanium"); p.grid[2] = ingot("ingotTitanium");
        p.grid[6] = new ItemStack(OSItems.energizedCoal);
        p.result = new ItemStack(OSItems.ingotTitaniumAlloy);
        p.resultCount = 3;
        pages.add(p);

        p = station("manual.os.craft.circuit", 3);
        p.grid[0] = ingot("ingotCopper"); p.grid[1] = ingot("ingotTin"); p.grid[2] = ingot("ingotCopper");
        p.grid[5] = ingot("ingotAluminium"); p.grid[6] = new ItemStack(OSItems.ingotRedmeg); p.grid[7] = ingot("ingotAluminium");
        p.grid[10] = ingot("ingotCopper"); p.grid[11] = ingot("ingotTin"); p.grid[12] = ingot("ingotCopper");
        p.result = new ItemStack(OSItems.itemCircuit);
        p.resultCount = 2;
        pages.add(p);

        p = station("manual.os.craft.control", 6);
        p.grid[0] = circuit(); p.grid[1] = circuit(); p.grid[2] = circuit();
        p.grid[5] = ingotSteel(); p.grid[6] = new ItemStack(OSItems.ingotRedmeg); p.grid[7] = ingotSteel();
        p.grid[11] = ingotSteel();
        p.result = new ItemStack(OSItems.itemControlModule);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.antenna1", 6);
        p.grid[1] = circuit();
        p.grid[6] = ingot("ingotCopper");
        p.grid[11] = ingot("ingotCopper");
        p.result = new ItemStack(OSItems.itemAntennaT1);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.antenna2", 6);
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT1); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT2);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.antenna3", 6);
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT2); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT3);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.antenna4", 6);
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT3); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT4);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.antenna5", 6);
        p.grid[1] = new ItemStack(OSItems.ingotYellomite);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT4); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotYellomite);
        p.result = new ItemStack(OSItems.itemAntennaT5);
        p.resultCount = 1;
        pages.add(p);

        // Painel Solar T1 (receita nova: Si + aço + titânio + redmeg/yellomite)
        p = station("manual.os.craft.solar", 6);
        p.grid[0] = new ItemStack(Blocks.glass_pane); p.grid[1] = new ItemStack(Blocks.glass_pane); p.grid[2] = new ItemStack(Blocks.glass_pane);
        p.grid[5] = new ItemStack(OSItems.itemSilicon); p.grid[6] = new ItemStack(OSItems.ingotYellomite); p.grid[7] = new ItemStack(OSItems.itemSilicon);
        p.grid[10] = ingotSteel(); p.grid[11] = ingot("ingotTitanium"); p.grid[12] = circuit();
        p.result = new ItemStack(OSItems.itemSolarPanelT1);
        p.resultCount = 2;
        pages.add(p);

        p = station("manual.os.craft.cladded", 3);
        p.grid[0] = ingotAlloyAlu(); p.grid[1] = new ItemStack(OSItems.carbonFiber); p.grid[2] = ingotAlloyAlu();
        p.grid[5] = new ItemStack(OSItems.carbonFiber); p.grid[6] = ingotAlloyTi(); p.grid[7] = new ItemStack(OSItems.carbonFiber);
        p.grid[10] = ingotAlloyAlu(); p.grid[11] = new ItemStack(OSItems.carbonFiber); p.grid[12] = ingotAlloyAlu();
        p.result = new ItemStack(OSBlocks.blockCladded);
        p.resultCount = 4;
        pages.add(p);

        p = station("manual.os.craft.computer", 6);
        p.grid[0] = circuit(); p.grid[1] = ingotSteel(); p.grid[2] = circuit();
        p.grid[5] = ingotSteel(); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = ingotSteel();
        p.grid[10] = circuit(); p.grid[11] = ingotSteel(); p.grid[12] = circuit();
        p.result = new ItemStack(OSBlocks.blockComputer);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.shield", 6);
        p.grid[0] = ingotAlloyTi(); p.grid[1] = new ItemStack(OSItems.carbonFiber); p.grid[2] = ingotAlloyTi();
        p.grid[5] = new ItemStack(OSItems.carbonFiber); p.grid[6] = circuit(); p.grid[7] = new ItemStack(OSItems.carbonFiber);
        p.grid[10] = ingotAlloyTi(); p.grid[11] = new ItemStack(OSItems.carbonFiber); p.grid[12] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemModuleDebrisShield);
        p.resultCount = 1;
        pages.add(p);

        p = station("manual.os.craft.platform", 6);
        p.grid[0] = ingotSteel(); p.grid[1] = new ItemStack(OSBlocks.blockCladded); p.grid[2] = ingotSteel();
        p.grid[5] = new ItemStack(OSBlocks.blockCladded); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotSteel(); p.grid[11] = new ItemStack(OSBlocks.blockCladded); p.grid[12] = ingotSteel();
        p.result = new ItemStack(OSBlocks.blockLaunchPlatform);
        p.resultCount = 9;
        pages.add(p);

        p = station("manual.os.craft.rocket", 25);
        p.grid[0] = new ItemStack(OSItems.itemAntennaT1); p.grid[1] = new ItemStack(OSItems.itemSolarPanelT1); p.grid[2] = new ItemStack(OSItems.itemSolarPanelT1); p.grid[3] = new ItemStack(OSItems.itemSolarPanelT1); p.grid[4] = new ItemStack(OSItems.itemAntennaT1);
        p.grid[5] = new ItemStack(OSBlocks.blockCladded); p.grid[6] = new ItemStack(OSBlocks.blockCladded); p.grid[7] = new ItemStack(OSBlocks.blockCladded); p.grid[8] = new ItemStack(OSBlocks.blockCladded); p.grid[9] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotAlloyTi(); p.grid[11] = ingotAlloyTi(); p.grid[12] = new ItemStack(OSItems.itemControlModule); p.grid[13] = ingotAlloyTi(); p.grid[14] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemRocketT1);
        p.resultCount = 1;
        pages.add(p);

        // Foguete de Navegação (v0.6.0): o táxi orbital.
        p = station("manual.os.craft.navrocket", 25);
        p.grid[0] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[1] = new ItemStack(OSItems.itemAntennaT1);
        p.grid[2] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[3] = new ItemStack(OSBlocks.blockCladded);
        p.grid[4] = new ItemStack(OSItems.itemControlModule);
        p.grid[5] = new ItemStack(OSBlocks.blockCladded);
        p.grid[6] = ingotAlloyTi();
        p.grid[7] = ingotAlloyTi();
        p.grid[8] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemNavRocket);
        p.resultCount = 1;
        pages.add(p);

        return pages.toArray(new Page[pages.size()]);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Page furnace(String title, Block ore, Item ingotItem, int count) {
        Page p = new Page(TYPE_FURNACE, title);
        p.grid[6] = new ItemStack(ore);
        p.result = new ItemStack(ingotItem);
        p.resultCount = count;
        return p;
    }

    private static Page vanilla(String title) {
        return new Page(TYPE_VANILLA, title);
    }

    private static Page station(String title, int seconds) {
        Page p = new Page(TYPE_STATION, title);
        p.craftSeconds = seconds;
        return p;
    }

    private static ItemStack ingotSteel() {
        return new ItemStack(OSItems.ingotSteel);
    }

    private static ItemStack circuit() {
        return new ItemStack(OSItems.itemCircuit);
    }

    private static ItemStack ingotAlloyTi() {
        return new ItemStack(OSItems.ingotTitaniumAlloy);
    }

    private static ItemStack ingotAlloyAlu() {
        return new ItemStack(OSItems.ingotAluminiumAlloy);
    }

    /** Ingote por nome do OreDictionary (mostra o nosso item). */
    private static ItemStack ingot(String oreName) {
        if ("ingotCopper".equals(oreName)) return new ItemStack(OSItems.ingotCopper);
        if ("ingotTin".equals(oreName)) return new ItemStack(OSItems.ingotTin);
        if ("ingotAluminium".equals(oreName)) return new ItemStack(OSItems.ingotAluminium);
        if ("ingotTitanium".equals(oreName)) return new ItemStack(OSItems.ingotTitanium);
        return null;
    }
}
