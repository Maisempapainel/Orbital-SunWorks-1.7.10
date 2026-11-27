package com.orbitalsunworks.item;

import java.util.List;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.SatelliteManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * FOGUETE DE NAVEGAÇÃO (v0.6.0) — item "táxi orbital".
 *
 * Clique com o direito: abre a GUI de seleção de satélite (só os SEUS —
 * nome, vida útil, tier). Escolheu + IR: o jogador entra no foguete
 * (EntityNavRocket) e vai até a dimensão orbital visitar a estrutura.
 *
 * O item NÃO se perde: ao chegar na órbita ele volta pro inventário (com
 * as coords de casa gravadas — o "voltar à Terra" da GUI da estrutura
 * usa elas pra te devolver onde você lançou).
 */
public class ItemNavigationRocket extends Item {

    public ItemNavigationRocket() {
        this.setUnlocalizedName("navRocket");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setTextureName(Reference.RESOURCE_PREFIX + "navRocket");
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        // v0.7.0: o fluxo correto é POSICIONAR o foguete numa plataforma
        // de lançamento (clique com o direito NA PLATAFORMA segurando o
        // item) e interagir com o BLOCO. O clique no ar só orienta.
        if (!world.isRemote) {
            com.orbitalsunworks.util.OSUtils.sendChat(player,
                "\u00a7ePosicione este foguete clicando com o bot\u00e3o direito no CENTRO de uma plataforma de lan\u00e7amento 3x3!");
        }
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        tooltip.add("§7Visite seus satélites em órbita");
        tooltip.add("§7Clique direito para escolher o destino");
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("HomeX")) {
            tooltip.add("§8Última base: §7" + (int) stack.getTagCompound().getDouble("HomeX")
                + ", " + (int) stack.getTagCompound().getDouble("HomeY")
                + ", " + (int) stack.getTagCompound().getDouble("HomeZ"));
        }
    }
}
