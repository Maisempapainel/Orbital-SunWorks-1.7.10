package com.orbitalsunworks.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

/**
 * Entidade do foguete em animação de lançamento.
 *
 * Cinemática:
 * 1. COUNTDOWN (3s): motor QUEIMA desde o começo (chama curta no pad),
 *    fumaça e tremor de câmera crescentes;
 * 2. LIFTOFF: bola de fogo + dano de queimadura (raio 3) + empurrão,
 *    tremor forte, aceleração subindo;
 * 3. ASCENT: sobe RÁPIDO (2.4 blocos/tick) — o céu é grande;
 *    Blocos no caminho (colocados depois do lançamento) são DESTRUÍDOS
 *    com drop, como se o foguete tivesse atravessado à força. BEDROCK é
 *    atravessado sem destruir (comportamento mantido);
 * 4. Ao passar de Y=250: deploy do satélite + CINEMÁTICA DE DEPLOY
 *    (tela escurece, satélite sai, painéis desdobram) — pacote enviado
 *    SÓ para o jogador que lançou (owner). Tremor de câmera idem: só
 *    quem lançou (pacote dedicado, não mais por proximidade).
 */
public class EntityRocket extends Entity {

    /** Fases do lançamento. */
    public static final int PHASE_COUNTDOWN = 0;
    public static final int PHASE_LIFTOFF = 1;
    public static final int PHASE_ASCENT = 2;

    private static final int COUNTDOWN_TICKS = 60; // 3 segundos

    private int phase = PHASE_COUNTDOWN;
    private int countdown = COUNTDOWN_TICKS;
    private float speed = 0.0f;
    private float healthSatellite = 1.0f;
    private boolean hasDebrisShield = false;
    /** Dono do lançamento (quem apertou LANCAR). */
    private UUID ownerUUID;
    /** Módulos extras do tile no momento do lançamento (ids string). */
    private List<String> moduleIds = new ArrayList<String>();

    public EntityRocket(World world) {
        super(world);
        this.setSize(0.9f, 3.0f);
        this.noClip = true;
        this.preventEntitySpawning = true;
    }

    public EntityRocket(World world, double x, double y, double z, float satelliteHealth, boolean debrisShield) {
        this(world);
        this.setPosition(x, y, z);
        this.motionX = 0;
        this.motionY = 0;
        this.motionZ = 0;
        this.healthSatellite = satelliteHealth;
        this.hasDebrisShield = debrisShield;
    }

    public EntityRocket(World world, double x, double y, double z, float satelliteHealth,
            boolean debrisShield, UUID owner, List<String> modules) {
        this(world, x, y, z, satelliteHealth, debrisShield);
        this.ownerUUID = owner;
        if (modules != null) {
            this.moduleIds = new ArrayList<String>(modules);
        }
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public List<String> getModuleIds() {
        return this.moduleIds;
    }

    /** True depois da ignição (countdown já começou = motor aceso). */
    public boolean isIgnited() {
        return this.phase != PHASE_COUNTDOWN || this.countdown < COUNTDOWN_TICKS - 10;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.phase == PHASE_COUNTDOWN) {
            this.countdown--;

            // Tremor crescente durante o countdown — SÓ para quem lançou,
            // via pacote direto (servidor dedicado: ninguém mais treme).
            if (!this.worldObj.isRemote && this.ownerUUID != null) {
                float shakeRamp = 1f - (float) this.countdown / COUNTDOWN_TICKS;
                com.orbitalsunworks.OrbitalSunworks.network.sendTo(
                    new com.orbitalsunworks.network.MessageLaunchShake(0.35f * shakeRamp),
                    com.orbitalsunworks.util.PlayerLookup.byUUID(this.worldObj, this.ownerUUID));
            }

            if (this.worldObj.isRemote) {
                // Motor aceso desde o início: chama curta + fumaça no pad.
                this.spawnExhaust(false);
                if (this.countdown < COUNTDOWN_TICKS - 10 && this.countdown % 10 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "random.fizz", 0.6f, 0.8f, false);
                }
                // Rumble contínuo do motor (camada grave, crescente).
                if (this.countdown % 7 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "mob.enderdragon.growl",
                        0.35f + 0.4f * (1f - (float) this.countdown / COUNTDOWN_TICKS), 0.55f, false);
                }
            } else if (this.countdown == COUNTDOWN_TICKS - 10) {
                this.worldObj.playSoundAtEntity(this, "fire.ignite", 1.0f, 0.6f);
            }

            if (this.countdown <= 0) {
                this.phase = PHASE_LIFTOFF;
                this.ignite();
            }
            return;
        }

        if (this.phase == PHASE_LIFTOFF) {
            // Aceleração: 0.05 -> 1.2 blocos/tick.
            this.speed = Math.min(1.2f, this.speed + 0.05f);
            this.setPosition(this.posX, this.posY + this.speed, this.posZ);
            if (!this.worldObj.isRemote) {
                this.destroyBlocksInPath();
            }

            if (this.worldObj.isRemote) {
                this.spawnExhaust(true);
                // Camadas de som no liftoff: trovão + rugido grave.
                if (this.ticksExisted % 9 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "mob.enderdragon.growl", 0.9f, 0.45f, false);
                }
                if (this.ticksExisted % 13 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "random.explode", 0.7f, 0.6f, false);
                }
                this.worldObj.playSound(this.posX, this.posY, this.posZ,
                    "random.fizz", 0.8f, 0.5f, false);
            }
            // Tremor MUITO forte no liftoff — pacote ao owner (2.4 -> teto
            // de 6 no handler).
            if (!this.worldObj.isRemote && this.ownerUUID != null) {
                com.orbitalsunworks.OrbitalSunworks.network.sendTo(
                    new com.orbitalsunworks.network.MessageLaunchShake(1.1f),
                    com.orbitalsunworks.util.PlayerLookup.byUUID(this.worldObj, this.ownerUUID));
            }

        } else if (this.phase == PHASE_ASCENT) {
            // Subida rápida + balanço leve.
            this.setPosition(this.posX + Math.sin(this.ticksExisted * 0.17f) * 0.012,
                this.posY + 2.4, this.posZ);
            if (!this.worldObj.isRemote) {
                this.destroyBlocksInPath();
            }

            if (this.worldObj.isRemote && this.ticksExisted % 2 == 0) {
                this.spawnExhaust(false);
                // Rugido distante durante a subida, cada vez mais fraco.
                if (this.ticksExisted % 11 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "mob.enderdragon.growl", 0.6f, 0.5f, false);
                }
            }
            // Tremor decaindo com a distância — pacote ao owner.
            if (!this.worldObj.isRemote && this.ownerUUID != null) {
                float dist = (float) Math.max(1.0, this.posY - this.launchY);
                com.orbitalsunworks.OrbitalSunworks.network.sendTo(
                    new com.orbitalsunworks.network.MessageLaunchShake(
                        Math.max(0f, 0.8f - dist * 0.03f)),
                    com.orbitalsunworks.util.PlayerLookup.byUUID(this.worldObj, this.ownerUUID));
            }
        }

        if (!this.worldObj.isRemote) {
            if (this.phase == PHASE_LIFTOFF && this.speed >= 1.2f) {
                this.phase = PHASE_ASCENT;
            }
            if (this.posY >= 250) {
                // Deploy ao sumir de vista: registra o satélite (manager
                // v2 devolve o objeto com id/nome) e manda a CINEMÁTICA
                // para o dono do lançamento (ninguém mais).
                com.orbitalsunworks.satellite.SatelliteManager.Satellite sat =
                    com.orbitalsunworks.satellite.SatelliteManager.get(this.worldObj)
                        .deploySatellite(this, this.healthSatellite, this.hasDebrisShield);
                if (sat != null && this.ownerUUID != null) {
                    com.orbitalsunworks.OrbitalSunworks.network.sendTo(
                        new com.orbitalsunworks.network.MessageDeployCinematic(
                            sat.id, sat.name, sat.tier, sat.health, sat.hasDebrisShield()),
                        com.orbitalsunworks.util.PlayerLookup.byUUID(this.worldObj, this.ownerUUID));
                }
                this.setDead();
            }
        }
    }

    /** Posição Y original do lançamento (para decaimento do tremor). */
    private float launchY = 0f;

    /**
     * DESTRÓI os blocos que estiverem na caixa do foguete (o corpo ocupa
     * ~0.8x0.8 de largura, 3.0 de altura). Roda só no servidor; blocos
     * quebrados dropam itens normalmente. BEDROCK não é tocado (o foguete
     * atravessa, como sempre fez). Chama destroyBlock com drop=true.
     */
    private void destroyBlocksInPath() {
        int minX = net.minecraft.util.MathHelper.floor_double(this.posX - 0.45);
        int maxX = net.minecraft.util.MathHelper.floor_double(this.posX + 0.45);
        int minZ = net.minecraft.util.MathHelper.floor_double(this.posZ - 0.45);
        int maxZ = net.minecraft.util.MathHelper.floor_double(this.posZ + 0.45);
        int minY = net.minecraft.util.MathHelper.floor_double(this.posY);
        int maxY = net.minecraft.util.MathHelper.floor_double(this.posY + 2.9);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    net.minecraft.block.Block b = this.worldObj.getBlock(x, y, z);
                    if (b == net.minecraft.init.Blocks.air
                        || b == net.minecraft.init.Blocks.bedrock) {
                        continue;
                    }
                    if (b.isReplaceable((net.minecraft.world.IBlockAccess) this.worldObj, x, y, z)) {
                        continue; // grama alta etc: nem quebra
                    }
                    // Destrói COM drop (setBlockToAir + dropBlockAsItem —
                    // o destroyBlock do 1.7.10 é func_147480_a, sem nome MCP).
                    this.worldObj.setBlockToAir(x, y, z);
                    b.dropBlockAsItem(this.worldObj, x, y, z,
                        this.worldObj.getBlockMetadata(x, y, z) & 15, 0);
                }
            }
        }
    }

    /** Ignição: explosão de fogo, dano em quem está perto. */
    private void ignite() {
        this.launchY = (float) this.posY;
        if (!this.worldObj.isRemote) {
            this.worldObj.playSoundAtEntity(this, "random.explode", 1.2f, 0.9f);
            this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 0.8f, 1.6f);
            // Dano de queimadura em entidades num raio de 3 blocos.
            List entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                this.boundingBox.expand(3.0, 2.0, 3.0));
            for (Object o : entities) {
                if (o instanceof net.minecraft.entity.EntityLivingBase) {
                    net.minecraft.entity.EntityLivingBase living = (net.minecraft.entity.EntityLivingBase) o;
                    living.attackEntityFrom(DamageSource.onFire, 6.0f);
                    double dx = living.posX - this.posX;
                    double dz = living.posZ - this.posZ;
                    double mag = Math.sqrt(dx * dx + dz * dz);
                    if (mag > 0.01) {
                        living.motionX += dx / mag * 0.9;
                        living.motionZ += dz / mag * 0.9;
                        living.motionY += 0.35;
                    }
                }
            }
        } else {
            // Bola de fogo inicial no cliente.
            for (int i = 0; i < 40; i++) {
                double ang = this.rand.nextDouble() * Math.PI * 2;
                double spd = 0.2 + this.rand.nextDouble() * 0.5;
                this.worldObj.spawnParticle("flame",
                    this.posX, this.posY + 0.3, this.posZ,
                    Math.cos(ang) * spd, 0.1, Math.sin(ang) * spd);
            }
        }
    }

    /** Rastro do motor: chama + fumaça abundante. */
    private void spawnExhaust(boolean liftoff) {
        int count = liftoff ? 10 : 4;
        for (int i = 0; i < count; i++) {
            double ang = this.rand.nextDouble() * Math.PI * 2;
            double r = this.rand.nextDouble() * 0.25;
            this.worldObj.spawnParticle("flame",
                this.posX + Math.cos(ang) * r, this.posY, this.posZ + Math.sin(ang) * r,
                0, -0.25 - this.rand.nextDouble() * 0.2, 0);
            double spread = liftoff ? 0.6 : 0.1;
            this.worldObj.spawnParticle("largesmoke",
                this.posX + Math.cos(ang) * r, this.posY - 0.4, this.posZ + Math.sin(ang) * r,
                Math.cos(ang) * spread, -0.05, Math.sin(ang) * spread);
            if (this.rand.nextInt(3) == 0) {
                this.worldObj.spawnParticle("mobSpell",
                    this.posX + Math.cos(ang) * 0.3, this.posY - 0.2, this.posZ + Math.sin(ang) * 0.3,
                    0, -0.1, 0);
            }
        }
    }

    /** Fase atual do lançamento (para render/partículas). */
    public int phaseOf() {
        return this.phase;
    }

    @Override
    protected void entityInit() {
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        this.phase = tag.getInteger("Phase");
        this.countdown = tag.getInteger("Countdown");
        this.healthSatellite = tag.getFloat("SatHealth");
        this.hasDebrisShield = tag.getBoolean("DebrisShield");
        String owner = tag.getString("Owner");
        if (!owner.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
            }
        }
        NBTTagList mods = tag.getTagList("Modules", 8);
        this.moduleIds = new ArrayList<String>();
        for (int i = 0; i < mods.tagCount(); i++) {
            this.moduleIds.add(mods.getStringTagAt(i));
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("Phase", this.phase);
        tag.setInteger("Countdown", this.countdown);
        tag.setFloat("SatHealth", this.healthSatellite);
        tag.setBoolean("DebrisShield", this.hasDebrisShield);
        if (this.ownerUUID != null) {
            tag.setString("Owner", this.ownerUUID.toString());
        }
        NBTTagList mods = new NBTTagList();
        for (String m : this.moduleIds) {
            mods.appendTag(new net.minecraft.nbt.NBTTagString(m));
        }
        tag.setTag("Modules", mods);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }
}
