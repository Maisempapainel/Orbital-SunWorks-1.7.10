package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.Reference;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Módulos instaláveis em foguetes e satélites:
 *  - Módulo: Defletor Cinético (Tier 1 / 2 / 3) -> proteção 60% / 80% / 95% em 24h
 *  - Módulo: Amplificador Fotovoltaico (Tier 1 / 2 / 3) -> +8% / +12% / +18% geração
 */
public class ItemModule extends Item {

    public static final int TYPE_NONE = 0;
    public static final int TYPE_DEBRIS_SHIELD = 1;
    public static final int TYPE_EFFICIENCY = 2;

    public static final String MOD_SHIELD_T1 = "debrisShield";
    public static final String MOD_SHIELD_T2 = "debrisShieldT2";
    public static final String MOD_SHIELD_T3 = "debrisShieldT3";
    public static final String MOD_EFF_T1 = "efficiencyT1";
    public static final String MOD_EFF_T2 = "efficiencyT2";
    public static final String MOD_EFF_T3 = "efficiencyT3";

    private final String moduleId;
    private final int type;
    private final int tier;

    public ItemModule(String unlocalizedAndTextureName) {
        this(unlocalizedAndTextureName, inferModuleId(unlocalizedAndTextureName));
    }

    public ItemModule(String unlocalizedAndTextureName, String moduleId) {
        this.setUnlocalizedName(unlocalizedAndTextureName);
        this.setTextureName(Reference.RESOURCE_PREFIX + unlocalizedAndTextureName);
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setMaxStackSize(16);
        this.moduleId = moduleId;

        if (MOD_SHIELD_T3.equals(moduleId)) {
            this.type = TYPE_DEBRIS_SHIELD;
            this.tier = 3;
        } else if (MOD_SHIELD_T2.equals(moduleId)) {
            this.type = TYPE_DEBRIS_SHIELD;
            this.tier = 2;
        } else if (MOD_SHIELD_T1.equals(moduleId)) {
            this.type = TYPE_DEBRIS_SHIELD;
            this.tier = 1;
        } else if (MOD_EFF_T3.equals(moduleId)) {
            this.type = TYPE_EFFICIENCY;
            this.tier = 3;
        } else if (MOD_EFF_T2.equals(moduleId)) {
            this.type = TYPE_EFFICIENCY;
            this.tier = 2;
        } else if (MOD_EFF_T1.equals(moduleId)) {
            this.type = TYPE_EFFICIENCY;
            this.tier = 1;
        } else {
            this.type = TYPE_NONE;
            this.tier = 0;
        }
    }

    private static String inferModuleId(String name) {
        if ("moduleDebrisShield".equals(name)) return MOD_SHIELD_T1;
        if ("moduleDebrisShieldT2".equals(name)) return MOD_SHIELD_T2;
        if ("moduleDebrisShieldT3".equals(name)) return MOD_SHIELD_T3;
        if ("moduleEfficiencyT1".equals(name)) return MOD_EFF_T1;
        if ("moduleEfficiencyT2".equals(name)) return MOD_EFF_T2;
        if ("moduleEfficiencyT3".equals(name)) return MOD_EFF_T3;
        return name != null ? name : "";
    }

    public String getModuleId() {
        return this.moduleId;
    }

    public int getModuleType() {
        return this.type;
    }

    public int getTier() {
        return this.tier;
    }

    public static int getShieldTier(String modId) {
        if (MOD_SHIELD_T3.equals(modId)) return 3;
        if (MOD_SHIELD_T2.equals(modId)) return 2;
        if (MOD_SHIELD_T1.equals(modId)) return 1;
        return 0;
    }

    public static int getEfficiencyPercent(String modId) {
        if (MOD_EFF_T3.equals(modId)) return 18;
        if (MOD_EFF_T2.equals(modId)) return 12;
        if (MOD_EFF_T1.equals(modId)) return 8;
        return 0;
    }

    public static String getShortLabel(String modId) {
        if (MOD_SHIELD_T1.equals(modId)) return "Defletor T1 (60%)";
        if (MOD_SHIELD_T2.equals(modId)) return "Defletor T2 (80%)";
        if (MOD_SHIELD_T3.equals(modId)) return "Defletor T3 (95%)";
        if (MOD_EFF_T1.equals(modId)) return "Amplif. T1 (+8%)";
        if (MOD_EFF_T2.equals(modId)) return "Amplif. T2 (+12%)";
        if (MOD_EFF_T3.equals(modId)) return "Amplif. T3 (+18%)";
        return modId != null ? modId : "M\u00f3dulo";
    }

    public static String getDisplayName(String modId) {
        if (MOD_SHIELD_T1.equals(modId)) return "M\u00f3dulo: Defletor Cin\u00e9tico (Tier 1)";
        if (MOD_SHIELD_T2.equals(modId)) return "M\u00f3dulo: Defletor Cin\u00e9tico (Tier 2)";
        if (MOD_SHIELD_T3.equals(modId)) return "M\u00f3dulo: Defletor Cin\u00e9tico (Tier 3)";
        if (MOD_EFF_T1.equals(modId)) return "M\u00f3dulo: Amplificador Fotovoltaico (Tier 1)";
        if (MOD_EFF_T2.equals(modId)) return "M\u00f3dulo: Amplificador Fotovoltaico (Tier 2)";
        if (MOD_EFF_T3.equals(modId)) return "M\u00f3dulo: Amplificador Fotovoltaico (Tier 3)";
        return modId != null ? modId : "M\u00f3dulo";
    }

    public static Item itemForModuleId(String modId) {
        if (MOD_SHIELD_T1.equals(modId)) {
            return OSItems.itemModuleDebrisShield != null
                ? OSItems.itemModuleDebrisShield
                : GameRegistry.findItem(Reference.MOD_ID, "moduleDebrisShield");
        }
        if (MOD_SHIELD_T2.equals(modId)) {
            return GameRegistry.findItem(Reference.MOD_ID, "moduleDebrisShieldT2");
        }
        if (MOD_SHIELD_T3.equals(modId)) {
            return GameRegistry.findItem(Reference.MOD_ID, "moduleDebrisShieldT3");
        }
        if (MOD_EFF_T1.equals(modId)) {
            return GameRegistry.findItem(Reference.MOD_ID, "moduleEfficiencyT1");
        }
        if (MOD_EFF_T2.equals(modId)) {
            return GameRegistry.findItem(Reference.MOD_ID, "moduleEfficiencyT2");
        }
        if (MOD_EFF_T3.equals(modId)) {
            return GameRegistry.findItem(Reference.MOD_ID, "moduleEfficiencyT3");
        }
        return null;
    }

    public static ItemStack stackForModuleId(String modId) {
        Item item = itemForModuleId(modId);
        return item != null ? new ItemStack(item, 1) : null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (this.type == TYPE_DEBRIS_SHIELD) {
            int keepPct = (this.tier == 3) ? 95 : (this.tier == 2 ? 80 : 60);
            int lossPct = 100 - keepPct;
            tooltip.add("\u00a7bBlindagem contra detritos espaciais (Tier " + this.tier + ")");
            tooltip.add("\u00a77Ret\u00e9m \u00a7a" + keepPct + "%\u00a77 de integridade em 24h (apenas \u00a7e-" + lossPct + "%\u00a77 desgaste).");
            tooltip.add("\u00a78Instale no foguete ou no sat\u00e9lite em \u00f3rbita.");
        } else if (this.type == TYPE_EFFICIENCY) {
            int bonus = getEfficiencyPercent(this.moduleId);
            tooltip.add("\u00a7eAmplificador solar orbital (Tier " + this.tier + ")");
            tooltip.add("\u00a77Aumenta a gera\u00e7\u00e3o em \u00a7a+" + bonus + "% RF/t\u00a77.");
            tooltip.add("\u00a78Instale no foguete ou no sat\u00e9lite em \u00f3rbita.");
        }
    }
}