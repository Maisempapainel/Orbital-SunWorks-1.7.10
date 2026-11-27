package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

/**
 * Manual do Orbital Sunworks - o "livrinho" com todas as receitas.
 *
 * Clique com o direito segurando para abrir a GUI do manual: páginas
 * navegáveis (setas ou teclas) mostrando cada receita com ícones reais
 * dos itens (grade 3x5, fornalha e crafting vanilla), tooltips com o
 * nome e a lista de tudo que o mod tem.
 */
public class ItemManual extends Item {

    public ItemManual() {
        this.setUnlocalizedName("manual");
        this.setTextureName(com.orbitalsunworks.Reference.RESOURCE_PREFIX + "manual");
        this.setCreativeTab(com.orbitalsunworks.OSCreativeTab.INSTANCE);
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // Abre a GUI do manual (ID 3). Funciona em qualquer lugar.
        player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
            com.orbitalsunworks.handler.GuiHandler.ID_MANUAL, world,
            (int) player.posX, (int) player.posY, (int) player.posZ);
        return stack;
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add(net.minecraft.util.StatCollector.translateToLocal("tooltip.orbitalsunworks.manual"));
    }
}
