package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.rocket.EntityRocket;
import com.orbitalsunworks.rocket.TileRocket;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * Pacote enviado pelo cliente ao clicar em LANCAR na GUI do foguete.
 * O servidor revalida tudo (bloco, tile, plataforma) antes de lançar.
 */
public class MessageLaunchRocket implements IMessage {

    private int x;
    private int y;
    private int z;

    public MessageLaunchRocket() {
    }

    public MessageLaunchRocket(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.x = buf.readInt();
        this.y = buf.readInt();
        this.z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public static class Handler implements IMessageHandler<MessageLaunchRocket, IMessage> {

        @Override
        public IMessage onMessage(MessageLaunchRocket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.worldObj;

            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (!(te instanceof TileRocket)) {
                return null; // foguete ja saiu do lugar
            }
            TileRocket tile = (TileRocket) te;

            // Validacao: alcance e estrutura da plataforma.
            if (player.getDistanceSq(message.x + 0.5, message.y + 0.5, message.z + 0.5) > 64) {
                return null;
            }
            // O foguete precisa estar sobre o centro de um 3x3 de plataformas.
            boolean onPlatform = com.orbitalsunworks.block.BlockLaunchPlatform
                .isCompleteStructure(world, message.x, message.y - 1, message.z);
            if (!onPlatform) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cO foguete precisa estar no centro de uma plataforma 3x3!");
                return null;
            }
            // Trajeto livre: bedrock NAO conta (o foguete atravessa).
            if (!com.orbitalsunworks.handler.OSLaunchGuard.isTrajectoryClear(
                    world, message.x, message.y, message.z, new int[1])) {
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7cTrajeto obstru\u00eddo! Remova os blocos acima do foguete para lan\u00e7ar.");
                return null;
            }

            // Guarda os dados antes de remover o tile.
            float health = tile.getHealth();
            boolean shield = tile.hasDebrisShield();
            int tier = tile.getTier();
            java.util.UUID owner = player.getUniqueID();
            java.util.List<String> moduleIds = tile.getModuleIds();

            // Cria a entidade de animação NO CENTRO do bloco (a base do
            // modelo fica no chão da plataforma, igual ao TESR) — agora
            // com o DONO do lançamento (cinematic/shake só para ele).
            EntityRocket rocket = new EntityRocket(world,
                message.x + 0.5, message.y, message.z + 0.5, health, shield, owner, moduleIds);
            if (!world.isRemote) {
                world.spawnEntityInWorld(rocket);
                // Remove a coluna de 3 blocos do foguete.
                for (int i = 2; i >= 0; i--) {
                    if (world.getBlock(message.x, message.y + i, message.z) == OSBlocks.blockRocket) {
                        world.setBlockToAir(message.x, message.y + i, message.z);
                    }
                }
                world.playSoundAtEntity(rocket, "mob.enderdragon.growl", 0.3f, 1.8f);
                com.orbitalsunworks.util.OSUtils.sendChat(player, "\u00a7aLançamento autorizado! Foguete Tier " + tier + " em ascensão...");
            }
            return null;
        }
    }
}
