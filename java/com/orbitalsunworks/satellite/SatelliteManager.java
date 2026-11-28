package com.orbitalsunworks.satellite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageMeteorSync;
import com.orbitalsunworks.rocket.EntityRocket;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.util.PlayerLookup;
import com.orbitalsunworks.world.orbit.OrbitStructureManager;

/**
 * Registro global de satélites em órbita, eventos de chuva de meteoros
 * e estações de controle (1 por jogador).
 *
 * Desgaste em 24h reais (86.400s / 1.440 impactos de detritos de 60s):
 *  - Sem escudo:   100% -> 20% (perde 80% em 24h)
 *  - Escudo T1:    100% -> 60% (perde 40% em 24h)
 *  - Escudo T2:    100% -> 80% (perde 20% em 24h)
 *  - Escudo T3:    100% -> 95% (perde  5% em 24h)
 *
 * Geração com vida útil:
 *  - 100% vida -> 100% geração
 *  -  40% vida ->  50% geração (metade)
 *  - Abaixo de 40% cai proporcionalmente até 0%; em 0% é destruído e apagado.
 *
 * Eficiência energética (módulos):
 *  - Eficiência T1: +8%
 *  - Eficiência T2: +12%
 *  - Eficiência T3: +18%
 */
public class SatelliteManager extends WorldSavedData {

    private static final String DATA_NAME = "OrbitalSunworks_Satellites";

    public static final String MOD_SHIELD_T1 = "debrisShield";
    public static final String MOD_SHIELD_T2 = "debrisShieldT2";
    public static final String MOD_SHIELD_T3 = "debrisShieldT3";
    public static final String MOD_EFF_T1 = "efficiencyT1";
    public static final String MOD_EFF_T2 = "efficiencyT2";
    public static final String MOD_EFF_T3 = "efficiencyT3";

    /** Intervalo dos micro-impactos de detritos: 60 segundos (1200 ticks). */
    public static final int DEBRIS_INTERVAL_TICKS = 1200;
    /** 24 horas reais = 1440 minutos. Sem escudo perde 80% (0.80f) em 24h. */
    public static final float BASE_WEAR_PER_MINUTE = 0.80f / 1440.0f;

    /** Evento de meteoro: mínimo 1x a cada ~20h reais quando logado. */
    public static final long METEOR_COOLDOWN_MS = 20L * 3600L * 1000L;
    public static final int DEFAULT_METEOR_TICKS = 10 * 60 * 20; // 10 minutos

    private static final Random RNG = new Random();

    public static class Satellite {

        public UUID id;
        public String name;
        public int tier = 1;
        public int orbitSlot = -1;
        public UUID owner;
        public String ownerName = "";
        public float health = 1.0f;
        public List<String> modules = new ArrayList<String>();
        public long generationRFt = 0L;

        public boolean hasModule(String modId) {
            return this.modules.contains(modId);
        }

        public int getShieldTier() {
            if (hasModule(MOD_SHIELD_T3)) return 3;
            if (hasModule(MOD_SHIELD_T2)) return 2;
            if (hasModule(MOD_SHIELD_T1)) return 1;
            return 0;
        }

        public boolean hasDebrisShield() {
            return getShieldTier() > 0;
        }

        public float getShieldWearMultiplier() {
            switch (getShieldTier()) {
                case 3: return 0.0625f; // 5% perda / 80% = 0.0625 -> termina em 95%
                case 2: return 0.25f;   // 20% perda / 80% = 0.25  -> termina em 80%
                case 1: return 0.50f;   // 40% perda / 80% = 0.50  -> termina em 60%
                default: return 1.0f;   // 80% perda              -> termina em 20%
            }
        }

        public float getEfficiencyBonus() {
            float bonus = 0f;
            for (String m : this.modules) {
                if (MOD_EFF_T1.equals(m)) {
                    bonus += 0.08f;
                } else if (MOD_EFF_T2.equals(m)) {
                    bonus += 0.12f;
                } else if (MOD_EFF_T3.equals(m)) {
                    bonus += 0.18f;
                }
            }
            return bonus;
        }

        public float currentGeneration() {
            if (this.health <= 0f) {
                return 0f;
            }
            long base = 1000000L * Math.max(1, this.tier);
            float healthFactor;
            if (this.health >= 0.4f) {
                // 100% vida -> 1.0x | 40% vida -> 0.50x
                healthFactor = 0.5f + ((this.health - 0.4f) / 0.6f) * 0.5f;
            } else {
                // Abaixo de 40% cai até 0x
                healthFactor = (this.health / 0.4f) * 0.5f;
            }
            float effMultiplier = 1.0f + getEfficiencyBonus();
            return base * healthFactor * effMultiplier;
        }
    }

    public static class MeteorEvent {
        public UUID owner;
        public String ownerName = "";
        public List<UUID> targetSatIds = new ArrayList<UUID>();
        public int remainingTicks;
        public int totalTicks;
        public boolean active = true;
    }

    public static class ControlStationRecord {
        public UUID owner;
        public String ownerName = "";
        public int dim;
        public int x;
        public int y;
        public int z;
        public int facing;
    }

    private final List<Satellite> satellites = new ArrayList<Satellite>();
    private final List<MeteorEvent> meteorEvents = new ArrayList<MeteorEvent>();
    private final Map<String, Long> lastMeteorByPlayer = new HashMap<String, Long>();
    private final List<ControlStationRecord> controlStations = new ArrayList<ControlStationRecord>();

    private int launchCounter = 0;
    private int nextOrbitSlot = 0;
    private int serverTickCounter = 0;

    public SatelliteManager(String name) {
        super(name);
    }

    public static SatelliteManager get(World world) {
        World overworld = DimensionManager.getWorld(0);
        if (overworld == null) {
            overworld = world;
        }
        if (overworld == null) {
            return new SatelliteManager(DATA_NAME);
        }
        SatelliteManager data = (SatelliteManager) overworld.perWorldStorage
            .loadData(SatelliteManager.class, DATA_NAME);
        if (data == null) {
            data = new SatelliteManager(DATA_NAME);
            overworld.perWorldStorage.setData(DATA_NAME, data);
        }
        return data;
    }

    private static boolean ownerMatches(UUID satOwner, String satOwnerName, UUID queryOwner, String queryName) {
        if (queryOwner != null && satOwner != null && queryOwner.equals(satOwner)) {
            return true;
        }
        if (queryName != null && !queryName.isEmpty() && satOwnerName != null && !satOwnerName.isEmpty()) {
            MinecraftServer srv = MinecraftServer.getServer();
            boolean offlineOrDev = srv == null || !srv.isServerInOnlineMode() || queryName.startsWith("Player");
            if (offlineOrDev && satOwnerName.equalsIgnoreCase(queryName)) {
                return true;
            }
        }
        return false;
    }

    public Satellite getById(UUID id) {
        if (id == null) {
            return null;
        }
        for (Satellite sat : this.satellites) {
            if (id.equals(sat.id)) {
                return sat;
            }
        }
        return null;
    }

    public List<Satellite> getSatellitesOf(UUID owner) {
        return getSatellitesOf(owner, null);
    }

    public List<Satellite> getSatellitesOf(UUID owner, String ownerName) {
        List<Satellite> out = new ArrayList<Satellite>();
        for (Satellite sat : this.satellites) {
            if (ownerMatches(sat.owner, sat.ownerName, owner, ownerName)) {
                if (owner != null && !owner.equals(sat.owner)) {
                    sat.owner = owner;
                    this.markDirty();
                }
                out.add(sat);
            }
        }
        return out;
    }

    public List<Satellite> getSatellites() {
        return this.satellites;
    }

    public long getTotalGenerationRFtOf(UUID owner, String ownerName) {
        long total = 0L;
        for (Satellite sat : getSatellitesOf(owner, ownerName)) {
            if (sat.health > 0f) {
                total += sat.generationRFt;
            }
        }
        return total;
    }

    public Satellite deploySatellite(EntityRocket rocket, float health, boolean debrisShield) {
        Satellite sat = new Satellite();
        sat.id = UUID.randomUUID();
        this.launchCounter++;
        sat.name = "OS-" + String.format("%03d", this.launchCounter);
        sat.tier = 1;
        sat.orbitSlot = this.nextOrbitSlot++;
        sat.health = Math.max(0.01f, Math.min(1.0f, health));
        sat.owner = rocket.getOwnerUUID();
        if (sat.owner != null) {
            EntityPlayerMP ownerPlayer = PlayerLookup.byUUID(rocket.worldObj, sat.owner);
            if (ownerPlayer != null) {
                sat.ownerName = ownerPlayer.getCommandSenderName();
            }
        }
        if (debrisShield) {
            sat.modules.add(MOD_SHIELD_T1);
        }
        for (String modId : rocket.getModuleIds()) {
            if (modId != null && !modId.isEmpty() && sat.modules.size() < 2) {
                if (isShieldModule(modId) && sat.hasDebrisShield()) {
                    continue;
                }
                sat.modules.add(modId);
            }
        }
        sat.generationRFt = (long) sat.currentGeneration();
        this.satellites.add(sat);
        this.markDirty();

        OSUtils.broadcastLaunch(rocket.worldObj, sat.name);
        return sat;
    }

    public static boolean isShieldModule(String modId) {
        return MOD_SHIELD_T1.equals(modId)
            || MOD_SHIELD_T2.equals(modId)
            || MOD_SHIELD_T3.equals(modId);
    }

    public boolean repairSatellite(UUID id, float amount) {
        Satellite sat = getById(id);
        if (sat == null || sat.health <= 0f) {
            return false;
        }
        sat.health = Math.min(1.0f, sat.health + amount);
        sat.generationRFt = (long) sat.currentGeneration();
        this.markDirty();
        return true;
    }

    public boolean addModuleToSatellite(UUID id, String moduleId) {
        Satellite sat = getById(id);
        if (sat == null || moduleId == null || moduleId.isEmpty() || sat.modules.size() >= 2) {
            return false;
        }
        if (isShieldModule(moduleId) && sat.hasDebrisShield()) {
            return false;
        }
        sat.modules.add(moduleId);
        sat.generationRFt = (long) sat.currentGeneration();
        this.markDirty();
        return true;
    }

    public String removeModuleAt(UUID id, int index) {
        Satellite sat = getById(id);
        if (sat == null || index < 0 || index >= sat.modules.size()) {
            return null;
        }
        String removed = sat.modules.remove(index);
        sat.generationRFt = (long) sat.currentGeneration();
        this.markDirty();
        return removed;
    }

    public void destroySatellite(World world, Satellite sat, String reason) {
        if (sat == null) {
            return;
        }
        this.satellites.remove(sat);
        for (MeteorEvent ev : this.meteorEvents) {
            ev.targetSatIds.remove(sat.id);
        }
        OrbitStructureManager.removeStructure(world, sat.orbitSlot);
        OSUtils.broadcastSatelliteLost(world, sat.name + (reason != null ? " (" + reason + ")" : ""));
        this.markDirty();
    }

    // ------------------------------------------------------------------
    // Estação de Controle: limite de 1 por jogador
    // ------------------------------------------------------------------
    public ControlStationRecord getControlStation(UUID owner, String ownerName) {
        for (ControlStationRecord rec : this.controlStations) {
            if (ownerMatches(rec.owner, rec.ownerName, owner, ownerName)) {
                if (owner != null && !owner.equals(rec.owner)) {
                    rec.owner = owner;
                    this.markDirty();
                }
                return rec;
            }
        }
        return null;
    }

    public void registerControlStation(UUID owner, String ownerName, int dim, int x, int y, int z, int facing) {
        ControlStationRecord existing = getControlStation(owner, ownerName);
        if (existing != null) {
            this.controlStations.remove(existing);
        }
        ControlStationRecord rec = new ControlStationRecord();
        rec.owner = owner;
        rec.ownerName = ownerName != null ? ownerName : "";
        rec.dim = dim;
        rec.x = x;
        rec.y = y;
        rec.z = z;
        rec.facing = facing;
        this.controlStations.add(rec);
        this.markDirty();
    }

    public void unregisterControlStation(UUID owner, String ownerName) {
        ControlStationRecord existing = getControlStation(owner, ownerName);
        if (existing != null) {
            this.controlStations.remove(existing);
            this.markDirty();
        }
    }

    public void unregisterControlStationAt(int dim, int x, int y, int z) {
        Iterator<ControlStationRecord> it = this.controlStations.iterator();
        while (it.hasNext()) {
            ControlStationRecord rec = it.next();
            if (rec.dim == dim && rec.x == x && rec.y == y && rec.z == z) {
                it.remove();
                this.markDirty();
            }
        }
    }

    // ------------------------------------------------------------------
    // Chuva de Meteoros
    // ------------------------------------------------------------------
    public MeteorEvent getMeteorEventFor(UUID owner, String ownerName) {
        for (MeteorEvent ev : this.meteorEvents) {
            if (ev.active && ownerMatches(ev.owner, ev.ownerName, owner, ownerName)) {
                return ev;
            }
        }
        return null;
    }

    public boolean isSatelliteTargetedByMeteor(UUID satId, UUID owner, String ownerName) {
        MeteorEvent ev = getMeteorEventFor(owner, ownerName);
        return ev != null && ev.targetSatIds.contains(satId);
    }

    public boolean triggerMeteorFor(EntityPlayerMP player, float minutes) {
        if (player == null) {
            return false;
        }
        List<Satellite> owned = getSatellitesOf(player.getUniqueID(), player.getCommandSenderName());
        if (owned.isEmpty()) {
            return false;
        }
        MeteorEvent old = getMeteorEventFor(player.getUniqueID(), player.getCommandSenderName());
        if (old != null) {
            this.meteorEvents.remove(old);
        }

        List<Satellite> pool = new ArrayList<Satellite>(owned);
        Collections.shuffle(pool, RNG);
        int count = Math.min(pool.size(), 2 + RNG.nextInt(2)); // 2 a 3 satélites (ou todos se <2)

        MeteorEvent ev = new MeteorEvent();
        ev.owner = player.getUniqueID();
        ev.ownerName = player.getCommandSenderName();
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < count; i++) {
            Satellite target = pool.get(i);
            ev.targetSatIds.add(target.id);
            if (i > 0) {
                names.append(", ");
            }
            names.append(target.name);
        }
        int ticks = Math.max(40, Math.round(minutes * 60f * 20f));
        ev.totalTicks = ticks;
        ev.remainingTicks = ticks;
        ev.active = true;
        this.meteorEvents.add(ev);
        this.lastMeteorByPlayer.put(playerKey(player.getUniqueID(), player.getCommandSenderName()), System.currentTimeMillis());
        this.markDirty();

        OSUtils.sendChat(player, "\u00a7c\u00a7l[ALERTA ORBITAL] \u00a7cChuva de meteoros detectada! Alvos: \u00a7e"
            + names.toString() + "\u00a7c. Lance um Foguete de Dispers\u00e3o em "
            + formatMinutesLabel(ticks) + "!");
        syncMeteorToPlayer(player, ev, false);
        return true;
    }

    public boolean disperseMeteorFor(EntityPlayerMP player) {
        if (player == null) {
            return false;
        }
        MeteorEvent ev = getMeteorEventFor(player.getUniqueID(), player.getCommandSenderName());
        if (ev == null) {
            return false;
        }
        ev.active = false;
        this.meteorEvents.remove(ev);
        this.markDirty();
        syncMeteorToPlayer(player, null, false);
        OSUtils.sendChat(player, "\u00a7a\u00a7l[ORBITAL SUNWORKS] \u00a7aFoguete de Dispers\u00e3o interceptou a chuva de meteoros! Seus sat\u00e9lites est\u00e3o seguros.");
        return true;
    }

    public void cancelMeteorFor(EntityPlayerMP player) {
        if (player == null) {
            return;
        }
        MeteorEvent ev = getMeteorEventFor(player.getUniqueID(), player.getCommandSenderName());
        if (ev != null) {
            this.meteorEvents.remove(ev);
            this.markDirty();
        }
        syncMeteorToPlayer(player, null, false);
    }

    public void syncMeteorToPlayer(EntityPlayerMP player, MeteorEvent ev, boolean impactFlash) {
        if (player == null) {
            return;
        }
        if (ev == null || !ev.active) {
            OrbitalSunworks.network.sendTo(new MessageMeteorSync(false, 0, 0,
                new ArrayList<String>(), new ArrayList<String>(), impactFlash), player);
            return;
        }
        List<String> ids = new ArrayList<String>();
        List<String> names = new ArrayList<String>();
        for (UUID sid : ev.targetSatIds) {
            Satellite s = getById(sid);
            if (s != null) {
                ids.add(s.id.toString());
                names.add(s.name);
            }
        }
        OrbitalSunworks.network.sendTo(new MessageMeteorSync(true, ev.remainingTicks, ev.totalTicks,
            ids, names, impactFlash), player);
    }

    private static String playerKey(UUID id, String name) {
        return id != null ? id.toString() : (name != null ? name.toLowerCase() : "unknown");
    }

    private static String formatMinutesLabel(int ticks) {
        int totalSec = ticks / 20;
        int min = totalSec / 60;
        int sec = totalSec % 60;
        return String.format("%d:%02d", min, sec);
    }

    // ------------------------------------------------------------------
    // Loop de servidor (1x por tick do servidor, NÃO por dimensão!)
    // ------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public void updateServerTick(MinecraftServer server) {
        if (server == null) {
            return;
        }
        World overworld = DimensionManager.getWorld(0);
        if (overworld == null) {
            return;
        }
        this.serverTickCounter++;

        // 1) Desgaste por detritos espaciais a cada 60 segundos (1200 ticks)
        if (this.serverTickCounter % DEBRIS_INTERVAL_TICKS == 0 && !this.satellites.isEmpty()) {
            List<Satellite> copy = new ArrayList<Satellite>(this.satellites);
            boolean dirty = false;
            for (Satellite sat : copy) {
                if (sat.health <= 0f) {
                    destroySatellite(overworld, sat, "poeira espacial");
                    dirty = true;
                    continue;
                }
                float wear = BASE_WEAR_PER_MINUTE * sat.getShieldWearMultiplier();
                sat.health = Math.max(0f, sat.health - wear);
                sat.generationRFt = (long) sat.currentGeneration();
                if (sat.health <= 0f) {
                    destroySatellite(overworld, sat, "detritos espaciais");
                }
                dirty = true;
            }
            if (dirty) {
                this.markDirty();
            }
        }

        // 2) Eventos de chuva de meteoros ativos (só conta tempo enquanto o dono estiver logado)
        if (!this.meteorEvents.isEmpty()) {
            Iterator<MeteorEvent> it = this.meteorEvents.iterator();
            while (it.hasNext()) {
                MeteorEvent ev = it.next();
                if (!ev.active) {
                    it.remove();
                    continue;
                }
                EntityPlayerMP ownerPlayer = findOnlinePlayer(server, ev.owner, ev.ownerName);
                if (ownerPlayer == null) {
                    continue; // jogador offline: pausa o contador
                }
                ev.remainingTicks--;
                if (ev.remainingTicks <= 0) {
                    // IMPACTO! Remove 50% da vida atual de cada satélite alvo
                    StringBuilder hitNames = new StringBuilder();
                    int destroyedCount = 0;
                    for (UUID sid : new ArrayList<UUID>(ev.targetSatIds)) {
                        Satellite sat = getById(sid);
                        if (sat == null) {
                            continue;
                        }
                        if (hitNames.length() > 0) {
                            hitNames.append(", ");
                        }
                        hitNames.append(sat.name);
                        sat.health = sat.health * 0.50f;
                        if (sat.health < 0.01f) {
                            sat.health = 0f;
                            destroyedCount++;
                            destroySatellite(overworld, sat, "colis\u00e3o de meteoro");
                        } else {
                            sat.generationRFt = (long) sat.currentGeneration();
                        }
                    }
                    it.remove();
                    this.markDirty();
                    OSUtils.sendChat(ownerPlayer, "\u00a74\u00a7l[IMPACTO DE METEORO] \u00a7cSat\u00e9lites atingidos: \u00a7e"
                        + (hitNames.length() > 0 ? hitNames.toString() : "nenhum")
                        + "\u00a7c (-50% vida \u00fatil"
                        + (destroyedCount > 0 ? ", " + destroyedCount + " destru\u00eddo(s)" : "") + ")!");
                    syncMeteorToPlayer(ownerPlayer, null, true);
                } else if (this.serverTickCounter % 20 == 0) {
                    syncMeteorToPlayer(ownerPlayer, ev, false);
                }
            }
        }

        // 3) Disparo raro automático (1x por dia real quando logado)
        if (this.serverTickCounter % 200 == 0) {
            long now = System.currentTimeMillis();
            List players = server.getConfigurationManager().playerEntityList;
            for (Object obj : players) {
                if (!(obj instanceof EntityPlayerMP)) {
                    continue;
                }
                EntityPlayerMP p = (EntityPlayerMP) obj;
                if (getMeteorEventFor(p.getUniqueID(), p.getCommandSenderName()) != null) {
                    continue;
                }
                List<Satellite> owned = getSatellitesOf(p.getUniqueID(), p.getCommandSenderName());
                if (owned.isEmpty()) {
                    continue;
                }
                String key = playerKey(p.getUniqueID(), p.getCommandSenderName());
                Long last = this.lastMeteorByPlayer.get(key);
                if (last == null) {
                    // Primeira vez que entra com satélite: agenda entre 4h e 18h reais (ou ~1x/dia)
                    long seedOffset = (long) (RNG.nextDouble() * 14L * 3600L * 1000L);
                    this.lastMeteorByPlayer.put(key, now - seedOffset);
                    this.markDirty();
                    continue;
                }
                if (now - last.longValue() >= METEOR_COOLDOWN_MS) {
                    // Pequena aleatoriedade entre 20h e 24h, garantindo até 24h
                    if (now - last.longValue() >= 24L * 3600L * 1000L || RNG.nextInt(144) == 0) {
                        triggerMeteorFor(p, 10.0f);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static EntityPlayerMP findOnlinePlayer(MinecraftServer server, UUID uuid, String name) {
        List players = server.getConfigurationManager().playerEntityList;
        for (Object o : players) {
            if (o instanceof EntityPlayerMP) {
                EntityPlayerMP p = (EntityPlayerMP) o;
                if (ownerMatches(uuid, name, p.getUniqueID(), p.getCommandSenderName())) {
                    return p;
                }
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // NBT (persistência)
    // ------------------------------------------------------------------
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.satellites.clear();
        this.meteorEvents.clear();
        this.controlStations.clear();
        this.lastMeteorByPlayer.clear();

        this.launchCounter = tag.getInteger("LaunchCounter");
        this.nextOrbitSlot = tag.getInteger("NextOrbitSlot");

        NBTTagList list = tag.getTagList("Satellites", 10);
        int maxSlotSeen = -1;
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound satTag = list.getCompoundTagAt(i);
            Satellite sat = new Satellite();
            try {
                sat.id = UUID.fromString(satTag.getString("Id"));
            } catch (IllegalArgumentException e) {
                sat.id = UUID.randomUUID();
            }
            sat.name = satTag.getString("Name");
            sat.tier = Math.max(1, satTag.getInteger("Tier"));
            sat.health = satTag.getFloat("Health");
            sat.ownerName = satTag.getString("OwnerName");
            if (satTag.hasKey("OrbitSlot")) {
                sat.orbitSlot = satTag.getInteger("OrbitSlot");
            } else {
                sat.orbitSlot = i;
            }
            if (sat.orbitSlot > maxSlotSeen) {
                maxSlotSeen = sat.orbitSlot;
            }
            if (satTag.hasKey("DebrisShield") && satTag.getBoolean("DebrisShield")) {
                sat.modules.add(MOD_SHIELD_T1);
            }
            String ownerStr = satTag.getString("Owner");
            if (!ownerStr.isEmpty()) {
                try {
                    sat.owner = UUID.fromString(ownerStr);
                } catch (IllegalArgumentException ignored) {
                }
            }
            NBTTagList mods = satTag.getTagList("Modules", 8);
            for (int m = 0; m < mods.tagCount(); m++) {
                String modId = mods.getStringTagAt(m);
                if (!modId.isEmpty() && sat.modules.size() < 2) {
                    if (MOD_SHIELD_T1.equals(modId) && sat.modules.contains(MOD_SHIELD_T1)) {
                        continue;
                    }
                    sat.modules.add(modId);
                }
            }
            sat.generationRFt = (long) sat.currentGeneration();
            if (sat.health > 0f) {
                this.satellites.add(sat);
            }
        }
        if (this.nextOrbitSlot <= maxSlotSeen) {
            this.nextOrbitSlot = maxSlotSeen + 1;
        }

        NBTTagList meteorList = tag.getTagList("MeteorEvents", 10);
        for (int i = 0; i < meteorList.tagCount(); i++) {
            NBTTagCompound mt = meteorList.getCompoundTagAt(i);
            MeteorEvent ev = new MeteorEvent();
            String own = mt.getString("Owner");
            if (!own.isEmpty()) {
                try {
                    ev.owner = UUID.fromString(own);
                } catch (IllegalArgumentException ignored) {
                }
            }
            ev.ownerName = mt.getString("OwnerName");
            ev.remainingTicks = mt.getInteger("Remaining");
            ev.totalTicks = mt.getInteger("Total");
            ev.active = mt.getBoolean("Active");
            NBTTagList tids = mt.getTagList("Targets", 8);
            for (int t = 0; t < tids.tagCount(); t++) {
                try {
                    ev.targetSatIds.add(UUID.fromString(tids.getStringTagAt(t)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            if (ev.active && ev.remainingTicks > 0 && !ev.targetSatIds.isEmpty()) {
                this.meteorEvents.add(ev);
            }
        }

        NBTTagList lastList = tag.getTagList("MeteorHistory", 10);
        for (int i = 0; i < lastList.tagCount(); i++) {
            NBTTagCompound lt = lastList.getCompoundTagAt(i);
            this.lastMeteorByPlayer.put(lt.getString("Key"), lt.getLong("Time"));
        }

        NBTTagList stList = tag.getTagList("ControlStations", 10);
        for (int i = 0; i < stList.tagCount(); i++) {
            NBTTagCompound st = stList.getCompoundTagAt(i);
            ControlStationRecord rec = new ControlStationRecord();
            String own = st.getString("Owner");
            if (!own.isEmpty()) {
                try {
                    rec.owner = UUID.fromString(own);
                } catch (IllegalArgumentException ignored) {
                }
            }
            rec.ownerName = st.getString("OwnerName");
            rec.dim = st.getInteger("Dim");
            rec.x = st.getInteger("X");
            rec.y = st.getInteger("Y");
            rec.z = st.getInteger("Z");
            rec.facing = st.getInteger("Facing");
            this.controlStations.add(rec);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("LaunchCounter", this.launchCounter);
        tag.setInteger("NextOrbitSlot", this.nextOrbitSlot);

        NBTTagList list = new NBTTagList();
        for (Satellite sat : this.satellites) {
            NBTTagCompound satTag = new NBTTagCompound();
            satTag.setString("Id", sat.id.toString());
            satTag.setString("Name", sat.name);
            satTag.setInteger("Tier", sat.tier);
            satTag.setInteger("OrbitSlot", sat.orbitSlot);
            satTag.setFloat("Health", sat.health);
            if (sat.owner != null) {
                satTag.setString("Owner", sat.owner.toString());
            }
            if (sat.ownerName != null) {
                satTag.setString("OwnerName", sat.ownerName);
            }
            NBTTagList mods = new NBTTagList();
            for (String modId : sat.modules) {
                mods.appendTag(new NBTTagString(modId));
            }
            satTag.setTag("Modules", mods);
            satTag.setLong("Generation", sat.generationRFt);
            list.appendTag(satTag);
        }
        tag.setTag("Satellites", list);

        NBTTagList meteorList = new NBTTagList();
        for (MeteorEvent ev : this.meteorEvents) {
            NBTTagCompound mt = new NBTTagCompound();
            if (ev.owner != null) {
                mt.setString("Owner", ev.owner.toString());
            }
            mt.setString("OwnerName", ev.ownerName != null ? ev.ownerName : "");
            mt.setInteger("Remaining", ev.remainingTicks);
            mt.setInteger("Total", ev.totalTicks);
            mt.setBoolean("Active", ev.active);
            NBTTagList tids = new NBTTagList();
            for (UUID sid : ev.targetSatIds) {
                tids.appendTag(new NBTTagString(sid.toString()));
            }
            mt.setTag("Targets", tids);
            meteorList.appendTag(mt);
        }
        tag.setTag("MeteorEvents", meteorList);

        NBTTagList lastList = new NBTTagList();
        for (Map.Entry<String, Long> e : this.lastMeteorByPlayer.entrySet()) {
            NBTTagCompound lt = new NBTTagCompound();
            lt.setString("Key", e.getKey());
            lt.setLong("Time", e.getValue());
            lastList.appendTag(lt);
        }
        tag.setTag("MeteorHistory", lastList);

        NBTTagList stList = new NBTTagList();
        for (ControlStationRecord rec : this.controlStations) {
            NBTTagCompound st = new NBTTagCompound();
            if (rec.owner != null) {
                st.setString("Owner", rec.owner.toString());
            }
            st.setString("OwnerName", rec.ownerName != null ? rec.ownerName : "");
            st.setInteger("Dim", rec.dim);
            st.setInteger("X", rec.x);
            st.setInteger("Y", rec.y);
            st.setInteger("Z", rec.z);
            st.setInteger("Facing", rec.facing);
            stList.appendTag(st);
        }
        tag.setTag("ControlStations", stList);
    }
}