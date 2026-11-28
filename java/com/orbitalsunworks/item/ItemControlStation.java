package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.satellite.SatelliteManager.ControlStationRecord;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.world.ControlStationBuilder;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Item que constrói automaticamente (bloco a bloco) a Estação de Controle
 * Orbital completa na direção em que o jogador está olhando.
 * Limite de 1 estação ativa por jogador.
 */
public class ItemControlStation extends Item {

    public ItemControlStation() {
        this.setUnlocalizedName("controlStation");
        this.setTextureName(Reference.RESOURCE_PREFIX + "controlStation");
        this.setMaxStackSize(1);
        this.setCreativeTab(OSCreativeTab.INSTANCE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
        list.add("\u00a7bBase Orbital Autoconstru\u00edda (15x15 octogonal)");
        list.add("\u00a77Inclui:");
        list.add("\u00a78 - \u00a7fPorta Met\u00e1lica com sensor de aproxima\u00e7\u00e3o");
        list.add("\u00a78 - \u00a7fMonitor Panor\u00e2mico 2x3 + Teclado de Controle");
        list.add("\u00a78 - \u00a7fTelesc\u00f3pio Zenith e Torre de Antena RF");
        list.add("\u00a7eLimite: 1 Esta\u00e7\u00e3o de Controle ativa por jogador.");
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
            int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        }
        if (OrbitDimensionManager.isOrbitWorld(world)) {
            OSUtils.sendChat(player, "\u00a7cN\u00e3o \u00e9 poss\u00edvel construir uma Esta\u00e7\u00e3o de Controle na \u00f3rbita.");
            return false;
        }

        SatelliteManager mgr = SatelliteManager.get(world);
        ControlStationRecord existing = mgr.getControlStation(player.getUniqueID(), player.getCommandSenderName());
        if (existing != null) {
            World exWorld = DimensionManager.getWorld(existing.dim);
            if (exWorld != null && exWorld.blockExists(existing.x, existing.y, existing.z)
                    && exWorld.getBlock(existing.x, existing.y, existing.z) != OSBlocks.blockControlStation) {
                // O núcleo antigo foi destruído anteriormente; limpa o registro obsoleto.
                mgr.unregisterControlStation(player.getUniqueID(), player.getCommandSenderName());
            } else {
                OSUtils.sendChat(player, "\u00a7cVoc\u00ea j\u00e1 possui uma Esta\u00e7\u00e3o de Controle ativa (Dim "
                    + existing.dim + ", X=" + existing.x + " Y=" + existing.y + " Z=" + existing.z
                    + "). Destrua a primeira antes de construir outra!");
                return false;
            }
        }

        int facing = MathHelper.floor_double((player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int fx = ControlStationBuilder.facingX(facing);
        int fz = ControlStationBuilder.facingZ(facing);

        int baseY = (side == 1) ? y + 1 : y;
        // Centraliza a estrutura 10 blocos à frente do ponto clicado para a escada de entrada
        // ficar logo diante do jogador sem prendê-lo dentro das paredes.
        int cx = x + fx * 10;
        int cz = z + fz * 10;

        if (baseY < 5 || baseY + 12 >= world.getHeight()) {
            OSUtils.sendChat(player, "\u00a7cAltura inv\u00e1lida para construir a Esta\u00e7\u00e3o de Controle.");
            return false;
        }

        String blockReason = ControlStationBuilder.validateArea(world, cx, baseY, cz, facing);
        if (blockReason != null) {
            OSUtils.sendChat(player, "\u00a7c" + blockReason);
            return false;
        }

        int[] core = ControlStationBuilder.getCorePosition(cx, baseY, cz, facing);
        mgr.registerControlStation(
            player.getUniqueID(),
            player.getCommandSenderName(),
            world.provider.dimensionId,
            core[0], core[1], core[2],
            facing);

        ControlStationBuilder.startBuild(world, cx, baseY, cz, facing, player);

        if (!player.capabilities.isCreativeMode) {
            stack.stackSize--;
        }
        OSUtils.sendChat(player, "\u00a7b[Orbital Sunworks] \u00a7aIniciando montagem autom\u00e1tica da Esta\u00e7\u00e3o de Controle!");
        return true;
    }
}