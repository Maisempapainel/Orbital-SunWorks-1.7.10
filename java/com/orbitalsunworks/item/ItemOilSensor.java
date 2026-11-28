package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.world.OilFieldManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Sensor Sísmico de Petróleo: verifica se a chunk atual possui jazida de petróleo
 * (raridade 1 em 20) e informa as coordenadas exatas dos limites da chunk.
 */
public class ItemOilSensor extends Item {

    public ItemOilSensor() {
        this.setUnlocalizedName("oilSensor");
        this.setTextureName(Reference.RESOURCE_PREFIX + "oilSensor");
        this.setCreativeTab(OSCreativeTab.INSTANCE);
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player != null) {
            scanAt(world, player,
                MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.posZ));
        }
        return stack;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world,
            int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && player != null) {
            scanAt(world, player, x, z);
        }
        return true;
    }

    private void scanAt(World world, EntityPlayer player, int x, int z) {
        if (world.provider == null || world.provider.dimensionId != 0) {
            world.playSoundAtEntity(player, "note.bass", 0.7f, 0.7f);
            OSUtils.sendChat(player,
                "\u00a7c[Sensor de Petr\u00f3leo] Jazidas s\u00f3 existem na superf\u00edcie (Overworld).");
            return;
        }
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        int minX = chunkX * 16;
        int maxX = minX + 15;
        int minZ = chunkZ * 16;
        int maxZ = minZ + 15;

        if (OilFieldManager.hasOilInChunk(world, chunkX, chunkZ)) {
            int rich = OilFieldManager.getOilRichnessPct(world, chunkX, chunkZ);
            world.playSoundAtEntity(player, "note.pling", 0.9f, 1.35f);
            OSUtils.sendChat(player,
                "\u00a7a[Sensor de Petr\u00f3leo] JAZIDA DETECTADA na Chunk (" + chunkX + ", " + chunkZ
                + ")! Press\u00e3o: " + rich + "%");
            OSUtils.sendChat(player,
                "\u00a77Posicione o Extrator 3x3 totalmente dentro de X[" + minX + ".." + maxX
                + "] Z[" + minZ + ".." + maxZ + "].");
        } else {
            world.playSoundAtEntity(player, "note.bass", 0.7f, 0.65f);
            OSUtils.sendChat(player,
                "\u00a7e[Sensor de Petr\u00f3leo] Chunk (" + chunkX + ", " + chunkZ
                + ") est\u00e9ril \u2014 sem reservas de petr\u00f3leo aqui.");
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
        list.add("\u00a77Clique direito para escanear jazidas na chunk");
        list.add("\u00a78Raridade: 1 em 20 chunks (Overworld)");
    }
}