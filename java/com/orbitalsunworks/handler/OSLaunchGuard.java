package com.orbitalsunworks.handler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * Proteção do TRAJETO DE LANÇAMENTO (v0.5.3).
 *
 * Regra: a coluna de ar acima da plataforma/foguete precisa ficar livre.
 *  1) PLACE: nenhum bloco pode ser colocado acima da plataforma (com ou
 *     sem foguete) — o PlaceEvent é cancelado com aviso no chat. O scan
 *     desce pela coluna de ar até achar uma plataforma (assim cobre
 *     colocar bloco em QUALQUER altura do trajeto, mesmo a 30 blocos).
 *  2) LAUNCH: se algo mesmo assim obstruir (bloco gerado pelo mundo,
 *     empurrado por pistão etc.), o botão LANCAR bloqueia com aviso e o
 *     servidor recusa o lançamento. BEDROCK NÃO CONTA como obstrução
 *     (o foguete atravessa, comportamento mantido).
 *  3) ASCENT: se o bloco aparecer DEPOIS do lançamento, o foguete DESTRÓI
 *     tudo que encontrar no caminho (menos bedrock, que ele atravessa).
 */
public class OSLaunchGuard {

    /** Altura máxima do trajeto verificada (a missão termina em Y=250). */
    public static final int TRAJECTORY_HEIGHT = 256;

    /**
     * True se (x, y, z) está na coluna de lançamento de uma plataforma
     * 3x3 completa OU do foguete posicionado nela. Scan descendo: acha o
     * "chão" da coluna (primeiro bloco sólido abaixo) e vê se é foguete
     * (base) ou plataforma centro/estrutura.
     */
    public static boolean isAboveLaunchColumn(World world, int x, int y, int z) {
        // Desce até 3 blocos procurando a base do foguete (o foguete tem
        // 3 de altura: colocando um bloco na altura do MEIO do foguete o
        // vizinho de baixo já é foguete).
        int baseY = -1;
        for (int dy = 1; dy <= 3; dy++) {
            Block below = world.getBlock(x, y - dy, z);
            if (below == OSBlocks.blockRocket || below == OSBlocks.blockNavRocket) {
                // É a coluna de um foguete (T1 ou de navegação): qualquer
                // rocket abaixo = trajeto (PlaceEvent não deixa colocar NO
                // foguete; o que interessa é ACIMA do topo).
                baseY = y - dy;
                break;
            }
            if (below == OSBlocks.blockLaunchPlatform) {
                baseY = y - dy;
                break;
            }
            // Sólido que não é nosso: encerra (não é trajeto de lançamento).
            if (!world.isAirBlock(x, y - dy, z)
                && !below.isReplaceable((net.minecraft.world.IBlockAccess) world, x, y - dy, z)) {
                return false;
            }
        }
        if (baseY < 0) {
            return false;
        }
        Block ground = world.getBlock(x, baseY, z);
        if (ground == OSBlocks.blockRocket || ground == OSBlocks.blockNavRocket) {
            // Base de um foguete (T1 ou nav): a coluna dele é trajeto.
            return true;
        }
        // Plataforma: só o CENTRO da estrutura completa é trajeto (o
        // foguete sobe pela coluna central; bloco acima do anel externo
        // não bloqueia nada).
        return com.orbitalsunworks.block.BlockLaunchPlatform.isCompleteStructure(world, x, baseY, z);
    }

    /**
     * Bloqueio de colocação: cancela o place de qualquer bloco na coluna
     * de lançamento (acima do centro da plataforma ou do foguete).
     */
    @SubscribeEvent
    public void onBlockPlace(net.minecraftforge.event.world.BlockEvent.PlaceEvent event) {
        World world = event.world;
        if (isAboveLaunchColumn(world, event.x, event.y, event.z)) {
            event.setCanceled(true);
            // Aviso no chat: defensivo (player pode estar sem conexão em
            // fluxos internos do Forge — nunca deixa o guard derrubar o servidor).
            if (!world.isRemote) {
                try {
                    EntityPlayer player = event.player;
                    if (player != null) {
                        OSUtils.sendChat(player, "\u00a7cNão pode colocar blocos acima da plataforma de lançamento — o trajeto do foguete precisa ficar livre!");
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    /**
     * @return true se o trajeto acima do foguete está LIVRE para lançar.
     *         Bedrock NÃO conta como obstrução (o foguete atravessa).
     *         Retorna também a altura do primeiro obstáculo via out[0].
     */
    public static boolean isTrajectoryClear(World world, int x, int baseY, int z, int[] firstObstructionY) {
        int top = Math.min(world.getHeight(), baseY + TRAJECTORY_HEIGHT);
        for (int y = baseY + 3; y < top; y++) { // +3: pula a própria coluna do foguete
            Block b = world.getBlock(x, y, z);
            if (b == Blocks.air) {
                continue;
            }
            if (b.isReplaceable((net.minecraft.world.IBlockAccess) world, x, y, z)) {
                continue; // grama alta etc: não bloqueia
            }
            if (b == Blocks.bedrock) {
                continue; // foguete atravessa bedrock (comportamento mantido)
            }
            if (firstObstructionY != null && firstObstructionY.length > 0) {
                firstObstructionY[0] = y;
            }
            return false;
        }
        return true;
    }
}
