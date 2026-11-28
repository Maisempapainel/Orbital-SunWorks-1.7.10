package com.orbitalsunworks.handler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.util.OSUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class OSLaunchGuard {

    public static final int TRAJECTORY_HEIGHT = 256;

    public static boolean isRocketBlock(Block b) {
        return b == OSBlocks.blockRocket
            || b == OSBlocks.blockNavRocket
            || b == OSBlocks.blockDispersionRocket;
    }

    public static boolean isAboveLaunchColumn(World world, int x, int y, int z) {
        int baseY = -1;
        for (int dy = 1; dy <= 10; dy++) {
            Block below = world.getBlock(x, y - dy, z);
            if (isRocketBlock(below)) {
                baseY = y - dy;
                break;
            }
            if (below == OSBlocks.blockLaunchPlatform) {
                baseY = y - dy;
                break;
            }
            if (!world.isAirBlock(x, y - dy, z)
                && !below.isReplaceable((IBlockAccess) world, x, y - dy, z)) {
                return false;
            }
        }
        if (baseY < 0) {
            return false;
        }
        Block ground = world.getBlock(x, baseY, z);
        if (isRocketBlock(ground)) {
            return true;
        }
        return com.orbitalsunworks.block.BlockLaunchPlatform.isCompleteStructure(world, x, baseY, z);
    }

    @SubscribeEvent
    public void onBlockPlace(net.minecraftforge.event.world.BlockEvent.PlaceEvent event) {
        if (isRocketBlock(event.block)) {
            return;
        }
        World world = event.world;
        if (isAboveLaunchColumn(world, event.x, event.y, event.z)) {
            event.setCanceled(true);
            if (!world.isRemote) {
                try {
                    EntityPlayer player = event.player;
                    if (player != null) {
                        OSUtils.sendChat(player,
                            "\u00a7cN\u00e3o pode colocar blocos acima da plataforma de lan\u00e7amento \u2014 o trajeto do foguete precisa ficar livre!");
                    }
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static boolean isTrajectoryClear(World world, int x, int baseY, int z, int[] firstObstructionY) {
        int top = Math.min(world.getHeight(), baseY + TRAJECTORY_HEIGHT);
        for (int y = baseY + 1; y < top; y++) {
            Block b = world.getBlock(x, y, z);
            if (b == Blocks.air || isRocketBlock(b)) {
                continue;
            }
            if (b.isReplaceable((IBlockAccess) world, x, y, z)) {
                continue;
            }
            if (b == Blocks.bedrock) {
                continue;
            }
            if (firstObstructionY != null && firstObstructionY.length > 0) {
                firstObstructionY[0] = y;
            }
            return false;
        }
        return true;
    }
}