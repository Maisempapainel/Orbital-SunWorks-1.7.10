package com.orbitalsunworks.satellite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraft.world.WorldSavedData;

import com.orbitalsunworks.rocket.EntityRocket;

/**
 * Registro de satélites em órbita — v2 (v0.6.0).
 *
 * Persistido no save do mundo (WorldSavedData, igual ao mapa de um mundo).
 * O MANAGER vive SEMPRE na dimensão 0 (perWorldStorage do overworld):
 * satélites são globais, não por dimensão — assim o foguete de navegação
 * lista eles de qualquer lugar e a estrutura na dimensão orbital acha o
 * mesmo dado.
 *
 * Cada satélite guarda:
 * - owner (UUID do jogador que lançou) — a GUI de navegação lista SÓ os
 *   satélites do dono (nome, vida, tier);
 * - tier e nome (gerado no lançamento);
 * - vida útil (1.0 -> 0.0, cai com poeira espacial; reparável com liga
 *   de titânio na estrutura orbital);
 * - módulos instalados (lista de strings — "debrisShield" etc.; escudo
 *   de detrito reduz o desgaste);
 * - taxa de geração de RF: 1M RF/t no Tier 1 com 100% de vida; com 30%
 *   de vida gera ~500K RF/t (linear de 100%->20%, piso de 50% abaixo).
 */
public class SatelliteManager extends WorldSavedData {

    private static final String DATA_NAME = "OrbitalSunworks_Satellites";

    // ------------------------------------------------------------------
    // Balanceamento do desgaste (poeira espacial)
    // ------------------------------------------------------------------
    /** Desgaste base por hora de jogo (20 min) sem escudo. */
    public static final float WEAR_PER_HOUR = 0.01f;
    /** Escudo de detrito reduz o desgaste à metade. */
    public static final float SHIELD_REDUCTION = 0.5f;

    /** Um satélite em órbita. */
    public static class Satellite {

        public UUID id;
        public String name;
        public int tier;
        /** Dono do satélite (quem lançou). */
        public UUID owner;
        public float health = 1.0f;
        /** Módulos instalados (ids: "debrisShield" etc). */
        public List<String> modules = new ArrayList<String>();
        /** Geração atual em RF/t (decai com a vida). */
        public long generationRFt = 0L;

        public boolean hasModule(String id) {
            return modules.contains(id);
        }

        /** Escudo de detrito instalado? (módulo legado boolean vira item da lista) */
        public boolean hasDebrisShield() {
            return hasModule("debrisShield");
        }

        public float currentGeneration() {
            // 1M RF/t no tier 1, decaindo: com 20% de vida gera ~500K RF/t
            // (metade), com 100% de vida gera o valor cheio. Linear entre
            // 100% e 20% de vida, travado em 50% abaixo disso.
            long base = tier == 1 ? 1000000L : 1000000L * tier;
            float factor;
            if (health >= 0.2f) {
                // 100% vida -> 1.0x | 20% vida -> 0.5x
                factor = 0.5f + (health - 0.2f) * (0.5f / 0.8f);
            } else {
                factor = 0.5f;
            }
            return base * factor;
        }
    }

    private final List<Satellite> satellites = new ArrayList<Satellite>();
    private int launchCounter = 0;

    public SatelliteManager(String name) {
        super(name);
    }

    /**
     * Manager global: vive SEMPRE no perWorldStorage da dimensão 0, não
     * importa de qual mundo chamaram (a estrutura orbital e a GUI do
     * foguete de navegação consultam o MESMO registro).
     */
    public static SatelliteManager get(World world) {
        World overworld = world;
        if (world.provider.dimensionId != 0) {
            overworld = DimensionManager.getWorld(0);
            if (overworld == null) {
                overworld = world; // fallback defensivo
            }
        }
        SatelliteManager data = (SatelliteManager) overworld.perWorldStorage
            .loadData(SatelliteManager.class, DATA_NAME);
        if (data == null) {
            data = new SatelliteManager(DATA_NAME);
            overworld.perWorldStorage.setData(DATA_NAME, data);
        }
        return data;
    }

    /** Satélite pelo id (ou null). */
    public Satellite getById(UUID id) {
        for (Satellite sat : satellites) {
            if (sat.id.equals(id)) {
                return sat;
            }
        }
        return null;
    }

    /** Satélites de um dono (para a GUI do foguete de navegação). */
    public List<Satellite> getSatellitesOf(UUID owner) {
        List<Satellite> out = new ArrayList<Satellite>();
        for (Satellite sat : satellites) {
            if (sat.owner != null && sat.owner.equals(owner)) {
                out.add(sat);
            }
        }
        return out;
    }

    public List<Satellite> getSatellites() {
        return satellites;
    }

    /** Registra um satélite lançado com sucesso (v2: owner + módulos). */
    public Satellite deploySatellite(EntityRocket rocket, float health, boolean debrisShield) {
        Satellite sat = new Satellite();
        sat.id = UUID.randomUUID();
        this.launchCounter++;
        sat.name = "OS-" + String.format("%03d", this.launchCounter);
        sat.tier = 1;
        sat.health = health;
        sat.owner = rocket.getOwnerUUID();
        if (debrisShield) {
            sat.modules.add("debrisShield");
        }
        // sincroniza módulos EXTRAS que o tile guardou (futuros módulos)
        for (String modId : rocket.getModuleIds()) {
            if (!sat.modules.contains(modId)) {
                sat.modules.add(modId);
            }
        }
        sat.generationRFt = (long) sat.currentGeneration();
        satellites.add(sat);
        this.markDirty();

        // Aviso global no servidor (o sonho de todo engenheiro).
        com.orbitalsunworks.util.OSUtils.broadcastLaunch(worldObjToWorld(rocket), sat.name);
        return sat;
    }

    /** Repara o satélite (+ vida) — usado pela GUI da estrutura orbital. */
    public boolean repairSatellite(UUID id, float amount) {
        Satellite sat = getById(id);
        if (sat == null) {
            return false;
        }
        sat.health = Math.min(1.0f, sat.health + amount);
        sat.generationRFt = (long) sat.currentGeneration();
        this.markDirty();
        return true;
    }

    /** Instala/desinstala módulo — usado pela GUI da estrutura orbital. */
    public boolean toggleModule(UUID id, String moduleId) {
        Satellite sat = getById(id);
        if (sat == null) {
            return false;
        }
        if (sat.modules.contains(moduleId)) {
            sat.modules.remove(moduleId);
        } else {
            sat.modules.add(moduleId);
        }
        this.markDirty();
        return true;
    }

    private World worldObjToWorld(EntityRocket rocket) {
        return rocket.worldObj;
    }

    /** Desgaste por poeira espacial: chamado a cada tick lógico do mundo. */
    public void updateWear(World world) {
        if (world.getTotalWorldTime() % 20 != 0) {
            return; // roda 1x por segundo
        }
        boolean changed = false;
        for (Satellite sat : satellites) {
            if (sat.health > 0) {
                float wear = WEAR_PER_HOUR / 72000f; // por segundo
                if (sat.hasDebrisShield()) {
                    wear *= SHIELD_REDUCTION;
                }
                sat.health = Math.max(0f, sat.health - wear);
                sat.generationRFt = (long) sat.currentGeneration();
                if (sat.health <= 0f) {
                    sat.generationRFt = 0L;
                    // Satélite perdido!
                    com.orbitalsunworks.util.OSUtils.broadcastSatelliteLost(world, sat.name);
                }
                changed = true;
            }
        }
        if (changed) {
            this.markDirty();
        }
    }

    // ------------------------------------------------------------------
    // NBT (persistência)
    // ------------------------------------------------------------------

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        this.launchCounter = tag.getInteger("LaunchCounter");
        NBTTagList list = tag.getTagList("Satellites", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound satTag = list.getCompoundTagAt(i);
            Satellite sat = new Satellite();
            sat.id = UUID.fromString(satTag.getString("Id"));
            sat.name = satTag.getString("Name");
            sat.tier = satTag.getInteger("Tier");
            sat.health = satTag.getFloat("Health");
            sat.generationRFt = satTag.getLong("Generation");
            // Legado (v0.5.x): campo booleano unico
            if (satTag.hasKey("DebrisShield") && satTag.getBoolean("DebrisShield")) {
                sat.modules.add("debrisShield");
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
                if (!modId.isEmpty() && !sat.modules.contains(modId)) {
                    sat.modules.add(modId);
                }
            }
            satellites.add(sat);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setInteger("LaunchCounter", this.launchCounter);
        NBTTagList list = new NBTTagList();
        for (Satellite sat : satellites) {
            NBTTagCompound satTag = new NBTTagCompound();
            satTag.setString("Id", sat.id.toString());
            satTag.setString("Name", sat.name);
            satTag.setInteger("Tier", sat.tier);
            satTag.setFloat("Health", sat.health);
            if (sat.owner != null) {
                satTag.setString("Owner", sat.owner.toString());
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
    }
}
