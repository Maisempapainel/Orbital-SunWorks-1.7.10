package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

/**
 * Módulos instaláveis no foguete/satélite. No Tier 1 o foguete tem 2 slots.
 *
 * Módulo "Escudo de Detrito": reduz em 50% o desgaste por poeira espacial
 * enquanto estiver instalado no satélite.
 */
public class ItemModule extends Item {

    public static final int TYPE_DEBRIS_SHIELD = 1;

    private final int type;

    public ItemModule(String name) {
        this.setUnlocalizedName(name);
        this.setTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + name);
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        if ("moduleDebrisShield".equals(name)) {
            this.type = TYPE_DEBRIS_SHIELD;
        } else {
            this.type = 0;
        }
    }

    public int getModuleType() {
        return type;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        if (type == TYPE_DEBRIS_SHIELD) {
            tooltip.add(StatCollector.translateToLocal("tooltip.orbitalsunworks.module.debrisShield"));
        }
    }
}
