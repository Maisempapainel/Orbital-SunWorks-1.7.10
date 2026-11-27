package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.rocket.EntityNavRocket;
import com.orbitalsunworks.rocket.TileNavRocket;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * C->S: "IR" no mapa orbital (v0.7.0 — fluxo NOVO via bloco na plataforma).
 *
 * O foguete de navegação agora é um BLOCO posicionado na plataforma (igual
 * ao T1). O jogador clica no bloco -> GUI de seleção -> IR:
 *  1. servidor valida: bloco do nav existe e completo, satélite é do
 *     jogador, jogador perto e desmontado, sem viagem duplicada;
 *  2. remove a COLUNA do bloco (a nave vira entidade);
 *  3. spawna a EntityNavRocket NO PAD e monta o jogador nela (a câmera
 *     F5 é acionada no cliente ao detectar o mount);
 *  4. countdown -> liftoff -> warp -> teleporta pra órbita.
 *
 * A "casa" (retorno) vem do tile do bloco (o pad onde ele foi montado).
 */
public class MessageTravelToSatellite implements IMessage {

    private String satUuid;
    private int x, y, z; // base do bloco do nav

    public MessageTravelToSatellite() {
    }

    public MessageTravelToSatellite(UUID satId, int x, int y, int z) {
        this.satUuid = satId.toString();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int len = buf.readInt();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(buf.readChar());
        }
        this.satUuid = sb.toString();
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.satUuid.length());
        for (char c : this.satUuid.toCharArray()) {
            buf.writeChar(c);
        }
        buf.writeInt(this.x);
        buf.writeInt(this.y);
        buf.writeInt(this.z);
    }

    public static class Handler implements IMessageHandler<MessageTravelToSatellite, IMessage> {

        @Override
        public IMessage onMessage(MessageTravelToSatellite message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            // 1) Validacoes (seguranca de servidor):
            UUID satId;
            try {
                satId = UUID.fromString(message.satUuid);
            } catch (IllegalArgumentException e) {
                return null;
            }
            // Bloco do nav existe e é a base?
            if (world.getBlock(message.x, message.y, message.z) != OSBlocks.blockNavRocket
                || world.getBlockMetadata(message.x, message.y, message.z) != 0) {
                OSUtils.sendChat(player, "§cFoguete não encontrado na plataforma!");
                return null;
            }
            // Alcance e estado do jogador.
            if (player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5) > 64) {
                return null;
            }
            if (player.ridingEntity != null) {
                return null;
            }
            // Satélite pertence ao jogador?
            SatelliteManager.Satellite sat = SatelliteManager.get(world).getById(satId);
            if (sat == null || !player.getUniqueID().equals(sat.owner)) {
                OSUtils.sendChat(player, "§cSatélite inválido ou não é seu!");
                return null;
            }
            // Já existe uma viagem EM VOO deste jogador? (spam-click; a
            // nave ESTACIONADA na órbita não conta — ela já chegou).
            for (Object o : world.loadedEntityList) {
                if (o instanceof EntityNavRocket) {
                    EntityNavRocket r = (EntityNavRocket) o;
                    if (r.getOwnerUUID() != null && r.getOwnerUUID().equals(player.getUniqueID())
                        && !r.isDead
                        && r.phaseOf() != EntityNavRocket.PHASE_PARKED) {
                        OSUtils.sendChat(player, "§cSua nave já está em viagem!");
                        return null;
                    }
                }
            }
            // Nave estacionada antiga do jogador: recolhe (o bloco será
            // reposto no pad desta nova viagem — ele só tem UMA nave).
            for (Object o : world.loadedEntityList) {
                if (o instanceof EntityNavRocket) {
                    EntityNavRocket r = (EntityNavRocket) o;
                    if (r.getOwnerUUID() != null && r.getOwnerUUID().equals(player.getUniqueID())
                        && !r.isDead && r.phaseOf() == EntityNavRocket.PHASE_PARKED) {
                        r.setDead();
                    }
                }
            }

            // 2) Casa do tile (onde voltar depois).
            int homeDim = world.provider.dimensionId;
            double homeX = message.x + 2.5, homeY = message.y, homeZ = message.z + 0.5;
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof TileNavRocket && ((TileNavRocket) te).homeSet) {
                TileNavRocket tile = (TileNavRocket) te;
                homeDim = tile.homeDim;
                homeX = tile.homeX;
                homeY = tile.homeY;
                homeZ = tile.homeZ;
            }

            // 3) Remove a coluna do bloco (3 partes) — a vira entidade.
            for (int i = 2; i >= 0; i--) {
                if (world.getBlock(message.x, message.y + i, message.z) == OSBlocks.blockNavRocket) {
                    world.setBlockToAir(message.x, message.y + i, message.z);
                }
            }

            // 4) Spawna a entidade NO PAD e monta o jogador.
            EntityNavRocket rocket = new EntityNavRocket(world,
                message.x + 0.5, message.y, message.z + 0.5, satId,
                homeDim, homeX, homeY, homeZ, player.getUniqueID());
            if (!world.spawnEntityInWorld(rocket)) {
                // Falhou o spawn (não deveria): devolve o item pro jogador.
                ItemStack give = new ItemStack(OSItems.itemNavRocket);
                if (!player.inventory.addItemStackToInventory(give)) {
                    world.spawnEntityInWorld(new net.minecraft.entity.item.EntityItem(
                        world, player.posX, player.posY, player.posZ, give));
                }
                return null;
            }
            player.mountEntity(rocket);
            OSUtils.sendChat(player, "§aDestino: §f" + sat.name + "§a. Lançamento em 3s...");
            return null;
        }
    }
}
