package com.orbitalsunworks;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Aba criativa do Orbital Sunworks na guia de inventário com ordenação determinística.
 */
public class OSCreativeTab extends CreativeTabs {

    public static final OSCreativeTab INSTANCE = new OSCreativeTab();

    private OSCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    public Item getTabIconItem() {
        return OSItems.ingotRedmeg != null ? OSItems.ingotRedmeg : Item.getItemFromBlock(OSBlocks.blockCladded);
    }

    private static int itemRank(String name) {
        if (name.equals("ingotRedmeg")) return 0;
        if (name.equals("ingotYellomite")) return 1;
        if (name.equals("ingotCopper")) return 2;
        if (name.equals("ingotTin")) return 3;
        if (name.equals("ingotAluminium")) return 4;
        if (name.equals("ingotTitanium")) return 5;
        if (name.equals("ingotSteel")) return 6;
        if (name.equals("ingotAluminiumAlloy")) return 7;
        if (name.equals("ingotTitaniumAlloy")) return 8;
        if (name.equals("energizedCoal")) return 9;
        if (name.equals("silicon")) return 10;
        if (name.equals("carbonFiber")) return 11;
        if (name.equals("circuit")) return 12;
        if (name.equals("controlModule")) return 13;
        if (name.equals("solarPanelT1")) return 14;
        if (name.startsWith("antennaT")) {
            try {
                return 20 + Integer.parseInt(name.substring(8));
            } catch (NumberFormatException ignored) {
            }
        }
        // Módulos em ordem lógica (Defletor T1..T3 -> Amplificador T1..T3)
        if (name.equals("moduleDebrisShield")) return 30;
        if (name.equals("moduleDebrisShieldT2")) return 31;
        if (name.equals("moduleDebrisShieldT3")) return 32;
        if (name.equals("moduleEfficiencyT1")) return 33;
        if (name.equals("moduleEfficiencyT2")) return 34;
        if (name.equals("moduleEfficiencyT3")) return 35;
        // Kit, foguetes e estação
        if (name.equals("repairKit")) return 36;
        if (name.equals("rocketT1")) return 37;
        if (name.equals("navRocket")) return 38;
        if (name.equals("dispersionRocket")) return 39;
        if (name.equals("controlStation")) return 40;
        // Blocos importantes
        if (name.equals("blockCraftingStation")) return 50;
        if (name.equals("blockLaunchPlatform")) return 51;
        if (name.equals("blockCladded")) return 52;
        if (name.equals("blockAntennaPort")) return 53;
        if (name.equals("blockAntennaTower")) return 54;
        if (name.equals("blockTelescope")) return 55;
        if (name.equals("blockControlMonitor")) return 56;
        if (name.equals("blockControlKeyboard")) return 57;
        if (name.equals("blockStationDoor")) return 58;
        if (name.equals("blockStationLamp")) return 59;
        if (name.equals("blockStationStair")) return 60;
        if (name.equals("blockStationPart")) return 61;
        if (name.equals("manual")) return 80;
        return 70;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void displayAllReleventItems(List items) {
        super.displayAllReleventItems(items);
        Collections.sort(items, new Comparator() {
            @Override
            public int compare(Object a, Object b) {
                return Integer.valueOf(rankOf(a)).compareTo(Integer.valueOf(rankOf(b)));
            }
        });
    }

    private static int rankOf(Object o) {
        if (!(o instanceof ItemStack)) {
            return 99;
        }
        ItemStack stack = (ItemStack) o;
        if (stack == null || stack.getItem() == null) {
            return 99;
        }
        String name = cpw.mods.fml.common.registry.GameData.getItemRegistry()
            .getNameForObject(stack.getItem());
        if (name == null) {
            return 99;
        }
        int colon = name.indexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1);
        }
        return itemRank(name);
    }
}