package com.orbitalsunworks.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

/**
 * Utilitários gerais do mod.
 */
public final class OSUtils {

    private OSUtils() {
    }

    /** Envia uma mensagem de chat para o jogador (códigos § funcionam). */
    public static void sendChat(EntityPlayer player, String message) {
        if (player != null) {
            player.addChatMessage(new ChatComponentText(message));
        }
    }

    /** Anúncio global de lançamento (todos os jogadores do servidor). */
    public static void broadcastLaunch(net.minecraft.world.World world, String satelliteName) {
        if (world != null && !world.isRemote) {
            net.minecraft.util.ChatComponentText msg = new net.minecraft.util.ChatComponentText(
                "\u00a7b[Orbital Sunworks] \u00a7fSat\u00e9lite \u00a7e" + satelliteName
                + "\u00a7f entrou em \u00f3rbita! Gerando energia solar...");
            for (Object o : world.playerEntities) {
                if (o instanceof EntityPlayer) {
                    ((EntityPlayer) o).addChatMessage(msg);
                }
            }
        }
    }

    /** Anúncio global de satélite perdido (vida útil zerada). */
    public static void broadcastSatelliteLost(net.minecraft.world.World world, String satelliteName) {
        if (world != null && !world.isRemote) {
            net.minecraft.util.ChatComponentText msg = new net.minecraft.util.ChatComponentText(
                "\u00a7b[Orbital Sunworks] \u00a7cSat\u00e9lite " + satelliteName
                + "\u00a7c foi perdido! A poeira espacial destruiu seus pain\u00e9is solares.");
            for (Object o : world.playerEntities) {
                if (o instanceof EntityPlayer) {
                    ((EntityPlayer) o).addChatMessage(msg);
                }
            }
        }
    }

    /** Formata RF grande: 1.234.567 -> "1.23M RF" etc. */
    public static String formatRF(long rf) {
        if (rf >= 1000000000000L) {
            return String.format("%.2fT RF", rf / 1000000000000.0);
        }
        if (rf >= 1000000000L) {
            return String.format("%.2fB RF", rf / 1000000000.0);
        }
        if (rf >= 1000000L) {
            return String.format("%.2fM RF", rf / 1000000.0);
        }
        if (rf >= 1000L) {
            return String.format("%.1fK RF", rf / 1000.0);
        }
        return rf + " RF";
    }
}
