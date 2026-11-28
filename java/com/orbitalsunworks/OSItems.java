package com.orbitalsunworks;

import net.minecraft.item.Item;

import com.orbitalsunworks.item.ItemAntenna;
import com.orbitalsunworks.item.ItemControlStation;
import com.orbitalsunworks.item.ItemDispersionRocket;
import com.orbitalsunworks.item.ItemManual;
import com.orbitalsunworks.item.ItemModule;
import com.orbitalsunworks.item.ItemNavigationRocket;
import com.orbitalsunworks.item.ItemRepairKit;
import com.orbitalsunworks.satellite.SatelliteManager;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Registro de todos os itens do mod.
 */
public class OSItems {

    // Lingotes e materiais base
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
    public static Item itemSilicon;

    // Componentes
    public static Item itemControlModule;
    public static Item itemAntennaT1;
    public static Item itemAntennaT2;
    public static Item itemAntennaT3;
    public static Item itemAntennaT4;
    public static Item itemAntennaT5;
    public static Item itemSolarPanelT1;

    // Foguetes, módulos e equipamentos
    public static Item itemRocketT1;
    public static Item itemNavRocket;
    public static Item itemDispersionRocket;

    public static Item itemModuleDebrisShield;
    public static Item itemModuleDebrisShieldT2;
    public static Item itemModuleDebrisShieldT3;
    public static Item itemModuleEfficiencyT1;
    public static Item itemModuleEfficiencyT2;
    public static Item itemModuleEfficiencyT3;

    // Aliases de compatibilidade
    public static Item itemModuleShieldT2;
    public static Item itemModuleShieldT3;
    public static Item itemModuleEffT1;
    public static Item itemModuleEffT2;
    public static Item itemModuleEffT3;

    public static Item itemRepairKit;
    public static Item itemControlStation;
    public static Item itemManual;

    public static void init() {
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

        itemControlModule = register("controlModule");
        itemAntennaT1 = registerAntenna(1);
        itemAntennaT2 = registerAntenna(2);
        itemAntennaT3 = registerAntenna(3);
        itemAntennaT4 = registerAntenna(4);
        itemAntennaT5 = registerAntenna(5);
        itemSolarPanelT1 = register("solarPanelT1");

        itemRocketT1 = register("rocketT1");
        itemNavRocket = new ItemNavigationRocket();
        GameRegistry.registerItem(itemNavRocket, "navRocket");

        itemDispersionRocket = new ItemDispersionRocket();
        GameRegistry.registerItem(itemDispersionRocket, "dispersionRocket");

        // Módulos (garantindo T1..T3 de Defletor Cinético e T1..T3 de Amplificador Fotovoltaico)
        itemModuleDebrisShield = new ItemModule("moduleDebrisShield", SatelliteManager.MOD_SHIELD_T1);
        GameRegistry.registerItem(itemModuleDebrisShield, "moduleDebrisShield");

        itemModuleDebrisShieldT2 = new ItemModule("moduleDebrisShieldT2", SatelliteManager.MOD_SHIELD_T2);
        GameRegistry.registerItem(itemModuleDebrisShieldT2, "moduleDebrisShieldT2");
        itemModuleShieldT2 = itemModuleDebrisShieldT2;

        itemModuleDebrisShieldT3 = new ItemModule("moduleDebrisShieldT3", SatelliteManager.MOD_SHIELD_T3);
        GameRegistry.registerItem(itemModuleDebrisShieldT3, "moduleDebrisShieldT3");
        itemModuleShieldT3 = itemModuleDebrisShieldT3;

        itemModuleEfficiencyT1 = new ItemModule("moduleEfficiencyT1", SatelliteManager.MOD_EFF_T1);
        GameRegistry.registerItem(itemModuleEfficiencyT1, "moduleEfficiencyT1");
        itemModuleEffT1 = itemModuleEfficiencyT1;

        itemModuleEfficiencyT2 = new ItemModule("moduleEfficiencyT2", SatelliteManager.MOD_EFF_T2);
        GameRegistry.registerItem(itemModuleEfficiencyT2, "moduleEfficiencyT2");
        itemModuleEffT2 = itemModuleEfficiencyT2;

        itemModuleEfficiencyT3 = new ItemModule("moduleEfficiencyT3", SatelliteManager.MOD_EFF_T3);
        GameRegistry.registerItem(itemModuleEfficiencyT3, "moduleEfficiencyT3");
        itemModuleEffT3 = itemModuleEfficiencyT3;

        itemRepairKit = new ItemRepairKit();
        GameRegistry.registerItem(itemRepairKit, "repairKit");

        itemControlStation = new ItemControlStation();
        GameRegistry.registerItem(itemControlStation, "controlStation");

        itemManual = new ItemManual();
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

    private static Item registerAntenna(int tier) {
        Item item = new ItemAntenna(tier);
        GameRegistry.registerItem(item, "antennaT" + tier);
        return item;
    }
}