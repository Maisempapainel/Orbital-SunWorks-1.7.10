package com.orbitalsunworks;

import net.minecraft.item.Item;
import net.minecraftforge.oredict.OreDictionary;

import com.orbitalsunworks.item.ItemAntenna;
import com.orbitalsunworks.item.ItemModule;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Registro de todos os itens do mod.
 */
public class OSItems {

    // ------------------------------------------------------------------
    // Lingotes e materiais base
    // ------------------------------------------------------------------
    public static Item ingotRedmeg;
    public static Item ingotYellomite;
    public static Item ingotCopper;
    public static Item ingotTin;
    public static Item ingotAluminium;
    public static Item ingotTitanium;
    public static Item ingotSteel;
    public static Item ingotAluminiumAlloy;
    public static Item ingotTitaniumAlloy;
    public static Item energizedCoal;
    public static Item carbonFiber;
    public static Item itemCircuit;
    /** Silício — universal (OreDict "itemSilicon", como IC2/AE2). */
    public static Item itemSilicon;

    // ------------------------------------------------------------------
    // Componentes
    // ------------------------------------------------------------------
    public static Item itemControlModule;
    public static Item itemAntennaT1;
    public static Item itemAntennaT2;
    public static Item itemAntennaT3;
    public static Item itemAntennaT4;
    public static Item itemAntennaT5;
    public static Item itemSolarPanelT1;

    // ------------------------------------------------------------------
    // Foguete e módulos
    // ------------------------------------------------------------------
    /** Item do foguete Tier 1 (coloca o bloco do foguete na plataforma). */
    public static Item itemRocketT1;
    /** Foguete de Navegação — visita os satélites em órbita (v0.6.0). */
    public static Item itemNavRocket;
    /** Módulo "Escudo de Detrito" - mitiga o desgaste por poeira espacial. */
    public static Item itemModuleDebrisShield;
    /** Manual do mod - livro com todas as receitas. */
    public static Item itemManual;

    public static void init() {
        // Materiais base - nomes simples, sem tooltips especiais
        ingotRedmeg = register("ingotRedmeg");
        ingotYellomite = register("ingotYellomite");
        ingotCopper = register("ingotCopper");
        ingotTin = register("ingotTin");
        ingotAluminium = register("ingotAluminium");
        ingotTitanium = register("ingotTitanium");
        ingotSteel = register("ingotSteel");
        ingotAluminiumAlloy = register("ingotAluminiumAlloy");
        ingotTitaniumAlloy = register("ingotTitaniumAlloy");
        energizedCoal = register("energizedCoal");
        carbonFiber = register("carbonFiber");
        itemCircuit = register("circuit");
        itemSilicon = register("silicon");

        // Componentes
        itemControlModule = register("controlModule");
        // Antenas: registra T1 (usada na receita do foguete); os outros
        // tiers existem mas ficam SEM receita por enquanto (só T1 por ora).
        itemAntennaT1 = registerAntenna(1);
        itemAntennaT2 = registerAntenna(2);
        itemAntennaT3 = registerAntenna(3);
        itemAntennaT4 = registerAntenna(4);
        itemAntennaT5 = registerAntenna(5);
        itemSolarPanelT1 = register("solarPanelT1");

        // Foguete e módulos
        itemRocketT1 = register("rocketT1");
        itemNavRocket = new com.orbitalsunworks.item.ItemNavigationRocket();
        GameRegistry.registerItem(itemNavRocket, "navRocket");
        itemModuleDebrisShield = new ItemModule("moduleDebrisShield");
        GameRegistry.registerItem(itemModuleDebrisShield, "moduleDebrisShield");

        // Manual do mod (livrinho com todas as receitas)
        itemManual = new com.orbitalsunworks.item.ItemManual();
        GameRegistry.registerItem(itemManual, "manual");
    }

    private static Item register(String name) {
        Item item = new Item()
            .setUnlocalizedName(name)
            .setCreativeTab(OSCreativeTab.INSTANCE)
            .setTextureName(Reference.RESOURCE_PREFIX + name);
        GameRegistry.registerItem(item, name);
        return item;
    }

    /** Antena tem classe própria (tooltip de tier) — registra igual. */
    private static Item registerAntenna(int tier) {
        Item item = new ItemAntenna(tier);
        GameRegistry.registerItem(item, "antennaT" + tier);
        return item;
    }
}
