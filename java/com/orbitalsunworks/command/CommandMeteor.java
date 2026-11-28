package com.orbitalsunworks.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import com.orbitalsunworks.satellite.SatelliteManager;

/**
 * Comando DEV para testar o evento de chuva de meteoros:
 *  - /meteor           -> inicia evento padrão de 10 minutos
 *  - /meteor <minutos> -> inicia evento com tempo customizado (ex: /meteor 0.5 para 30s)
 *  - /meteor stop      -> cancela o evento ativo
 */
public class CommandMeteor extends CommandBase {

    @Override
    public String getCommandName() {
        return "meteor";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/meteor [minutos|stop]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        SatelliteManager mgr = SatelliteManager.get(player.worldObj);

        if (args.length >= 1 && "stop".equalsIgnoreCase(args[0])) {
            mgr.cancelMeteorFor(player);
            sender.addChatMessage(new ChatComponentText("\u00a7a[DEV] Chuva de meteoros cancelada."));
            return;
        }

        float minutes = 10.0f;
        if (args.length >= 1) {
            try {
                minutes = Float.parseFloat(args[0]);
            } catch (NumberFormatException e) {
                sender.addChatMessage(new ChatComponentText("\u00a7cUso: /meteor <minutos> (ex: /meteor 10 ou /meteor 0.5)"));
                return;
            }
        }
        if (minutes < 0.05f) {
            minutes = 0.05f;
        } else if (minutes > 120f) {
            minutes = 120f;
        }

        if (!mgr.triggerMeteorFor(player, minutes)) {
            sender.addChatMessage(new ChatComponentText("\u00a7cVoc\u00ea precisa ter pelo menos 1 sat\u00e9lite em \u00f3rbita para testar o meteoro!"));
        }
    }
}