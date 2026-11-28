package com.orbitalsunworks.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.item.ItemTossEvent;

import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.item.ItemSpaceSuit;
import com.orbitalsunworks.rocket.EntityNavRocket;
import com.orbitalsunworks.rocket.EntityRocket;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * Controla toda a lógica de sobrevivência e travamento do Traje Espacial:
 *  - Verificação de set completo (Capacete, Peitoral, Calça, Botas)
 *  - Trava absoluta durante o voo na nave (impede retirar ou jogar fora)
 *  - Sufocamento letal na órbita (10% da vida máxima por hit, ignorando armadura)
 */
public class SpaceSuitHandler {

    public static final DamageSource SPACE_SUFFOCATION = new DamageSource("orbitalSunworks.suffocation")
        .setDamageBypassesArmor()
        .setDamageIsAbsolute();

    private static final Map<UUID, ItemStack[]> FLIGHT_ARMOR_SNAPSHOT = new HashMap<UUID, ItemStack[]>();
    private static final Map<UUID, Integer> SUFFOCATE_TICKS = new HashMap<UUID, Integer>();
    private static final Map<UUID, Long> LAST_WARN_TICK = new HashMap<UUID, Long>();

    public static boolean hasFullSuit(EntityPlayer player) {
        if (player == null || player.inventory == null) {
            return false;
        }
        ItemStack boots = player.inventory.armorInventory[0];
        ItemStack legs = player.inventory.armorInventory[1];
        ItemStack chest = player.inventory.armorInventory[2];
        ItemStack helm = player.inventory.armorInventory[3];
        return helm != null && helm.getItem() == OSItems.spaceHelmet
            && chest != null && chest.getItem() == OSItems.spaceChestplate
            && legs != null && legs.getItem() == OSItems.spaceLeggings
            && boots != null && boots.getItem() == OSItems.spaceBoots;
    }

    public static boolean isInFlightLock(EntityPlayer player) {
        if (player == null || player.ridingEntity == null) {
            return false;
        }
        if (player.ridingEntity instanceof EntityRocket) {
            return true;
        }
        if (player.ridingEntity instanceof EntityNavRocket) {
            return ((EntityNavRocket) player.ridingEntity).phaseOf() != EntityNavRocket.PHASE_PARKED;
        }
        return false;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != Side.SERVER) {
            return;
        }
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP mp = (EntityPlayerMP) event.player;
        UUID pid = mp.getUniqueID();

        // 1. Trava anti-remoção durante o voo na nave espacial
        if (isInFlightLock(mp)) {
            ItemStack[] snap = FLIGHT_ARMOR_SNAPSHOT.get(pid);
            if (snap == null) {
                snap = new ItemStack[4];
                for (int i = 0; i < 4; i++) {
                    ItemStack cur = mp.inventory.armorInventory[i];
                    snap[i] = cur != null ? cur.copy() : null;
                }
                FLIGHT_ARMOR_SNAPSHOT.put(pid, snap);
            } else {
                boolean changed = false;
                for (int i = 0; i < 4; i++) {
                    ItemStack cur = mp.inventory.armorInventory[i];
                    if (!ItemStack.areItemStacksEqual(cur, snap[i])) {
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    // Limpa peça retirada do cursor ou inventário duplicado e devolve ao corpo
                    ItemStack held = mp.inventory.getItemStack();
                    if (held != null && held.getItem() instanceof ItemSpaceSuit) {
                        mp.inventory.setItemStack(null);
                        mp.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(-1, -1, null));
                    }
                    for (int i = 0; i < 4; i++) {
                        ItemStack expected = snap[i];
                        ItemStack current = mp.inventory.armorInventory[i];
                        if (!ItemStack.areItemStacksEqual(current, expected)) {
                            removeOneMatching(mp, expected);
                            if (current != null && !(current.getItem() instanceof ItemSpaceSuit)) {
                                if (!mp.inventory.addItemStackToInventory(current.copy())) {
                                    mp.dropPlayerItemWithRandomChoice(current.copy(), false);
                                }
                            }
                            mp.inventory.armorInventory[i] = expected != null ? expected.copy() : null;
                        }
                    }
                    mp.inventoryContainer.detectAndSendChanges();
                    warnThrottled(mp,
                        "\u00a7e[Orbital Sunworks] Voc\u00ea n\u00e3o pode retirar o Traje Espacial durante o voo!");
                }
            }
        } else {
            FLIGHT_ARMOR_SNAPSHOT.remove(pid);
        }

        // 2. Sufocamento na órbita sem o traje completo (10% da vida máxima por hit)
        if (mp.worldObj != null
            && OrbitDimensionManager.isOrbitWorld(mp.worldObj)
            && !mp.capabilities.isCreativeMode
            && mp.isEntityAlive()
            && !isInFlightLock(mp)
            && !hasFullSuit(mp)) {

            Integer prevTicks = SUFFOCATE_TICKS.get(pid);
            int t = (prevTicks != null ? prevTicks.intValue() : 0) + 1;
            SUFFOCATE_TICKS.put(pid, Integer.valueOf(t));

            if (t == 1 || (t % 40 == 0)) {
                warnThrottled(mp,
                    "\u00a7c[ALERTA DE V\u00c1CUO] Sem Traje Espacial completo! Voc\u00ea est\u00e1 sufocando!");
            }

            if (t % 20 == 0) {
                float dmg = Math.max(1.0f, mp.getMaxHealth() * 0.10f);
                mp.attackEntityFrom(SPACE_SUFFOCATION, dmg);
            }
        } else {
            SUFFOCATE_TICKS.remove(pid);
        }
    }

    @SubscribeEvent
    public void onItemToss(ItemTossEvent event) {
        if (event.player == null || event.player.worldObj == null || event.player.worldObj.isRemote) {
            return;
        }
        if (isInFlightLock(event.player)
            && event.entityItem != null
            && event.entityItem.getEntityItem() != null
            && event.entityItem.getEntityItem().getItem() instanceof ItemSpaceSuit) {
            event.setCanceled(true);
            ItemStack[] snap = FLIGHT_ARMOR_SNAPSHOT.get(event.player.getUniqueID());
            if (snap != null) {
                for (int i = 0; i < 4; i++) {
                    if (snap[i] != null) {
                        event.player.inventory.armorInventory[i] = snap[i].copy();
                    }
                }
            }
            if (event.player instanceof EntityPlayerMP) {
                ((EntityPlayerMP) event.player).inventoryContainer.detectAndSendChanges();
            }
            warnThrottled(event.player,
                "\u00a7e[Orbital Sunworks] Voc\u00ea n\u00e3o pode largar o Traje Espacial durante o voo!");
        }
    }

    @SubscribeEvent
    public void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player != null) {
            UUID pid = event.player.getUniqueID();
            FLIGHT_ARMOR_SNAPSHOT.remove(pid);
            SUFFOCATE_TICKS.remove(pid);
            LAST_WARN_TICK.remove(pid);
        }
    }

    private static void removeOneMatching(EntityPlayerMP mp, ItemStack target) {
        if (target == null) {
            return;
        }
        for (int s = 0; s < mp.inventory.mainInventory.length; s++) {
            ItemStack st = mp.inventory.mainInventory[s];
            if (st != null && st.getItem() == target.getItem() && st.getItemDamage() == target.getItemDamage()) {
                mp.inventory.mainInventory[s] = null;
                return;
            }
        }
    }

    private static void warnThrottled(EntityPlayer player, String msg) {
        if (player == null || player.worldObj == null) {
            return;
        }
        long now = player.worldObj.getTotalWorldTime();
        Long prev = LAST_WARN_TICK.get(player.getUniqueID());
        if (prev == null || (now - prev.longValue()) >= 35L) {
            LAST_WARN_TICK.put(player.getUniqueID(), Long.valueOf(now));
            OSUtils.sendChat(player, msg);
        }
    }
}