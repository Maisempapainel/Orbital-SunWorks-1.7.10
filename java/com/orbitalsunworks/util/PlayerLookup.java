package com.orbitalsunworks.util;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Acha um EntityPlayerMP online por UUID (para pacotes diretos
 * servidor -> dono do lançamento). Nunca lança exceção: devolve null
 * se o jogador não estiver online (pacote simplesmente não é enviado).
 */
public final class PlayerLookup {

    private PlayerLookup() {
    }

    public static EntityPlayerMP byUUID(World world, UUID uuid) {
        if (world == null || uuid == null) {
            return null;
        }
        for (Object o : world.playerEntities) {
            if (o instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP) o;
                if (uuid.equals(p.getUniqueID())) {
                    return p;
                }
            }
        }
        return null;
    }
}
