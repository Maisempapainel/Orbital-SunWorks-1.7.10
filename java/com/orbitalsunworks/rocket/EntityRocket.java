package com.orbitalsunworks.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.network.MessageDeployCinematic;
import com.orbitalsunworks.network.MessageLaunchShake;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.PlayerLookup;

public class EntityRocket extends Entity {

    public static final int PHASE_COUNTDOWN = 0;
    public static final int PHASE_LIFTOFF = 1;
    public static final int PHASE_ASCENT = 2;

    private static final int COUNTDOWN_TICKS = 60;
    private static final int DW_PHASE = 28;
    private static final int DW_IGNITED = 29;

    private int phase = PHASE_COUNTDOWN;
    private int countdown = COUNTDOWN_TICKS;
    private float speed = 0.0f;
    private float healthSatellite = 1.0f;
    private boolean hasDebrisShield = false;
    private UUID ownerUUID;
    private List<String> moduleIds = new ArrayList<String>();
    private float launchY = 0f;

    public EntityRocket(World world) {
        super(world);
        this.setSize(1.35f, 6.0f);
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

    public boolean isIgnited() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.dataWatcher.getWatchableObjectInt(DW_IGNITED) != 0;
        }
        return this.phase != PHASE_COUNTDOWN || this.countdown < COUNTDOWN_TICKS - 12;
    }

    public int phaseOf() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.dataWatcher.getWatchableObjectInt(DW_PHASE);
        }
        return this.phase;
    }

    private void syncWatchers() {
        this.dataWatcher.updateObject(DW_PHASE, Integer.valueOf(this.phase));
        int ign = (this.phase != PHASE_COUNTDOWN || this.countdown < COUNTDOWN_TICKS - 12) ? 1 : 0;
        this.dataWatcher.updateObject(DW_IGNITED, Integer.valueOf(ign));
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(DW_PHASE, Integer.valueOf(PHASE_COUNTDOWN));
        this.dataWatcher.addObject(DW_IGNITED, Integer.valueOf(0));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!this.worldObj.isRemote) {
            this.syncWatchers();
        }

        if (this.phaseOf() == PHASE_COUNTDOWN) {
            if (!this.worldObj.isRemote) {
                this.countdown--;
                this.syncWatchers();

                if (this.ownerUUID != null) {
                    float shakeRamp = 1f - (float) this.countdown / COUNTDOWN_TICKS;
                    EntityPlayerMP owner = PlayerLookup.byUUID(this.worldObj, this.ownerUUID);
                    if (owner != null) {
                        OrbitalSunworks.network.sendTo(new MessageLaunchShake(0.45f * shakeRamp), owner);
                    }
                }

                if (this.countdown == COUNTDOWN_TICKS - 12) {
                    this.worldObj.playSoundAtEntity(this, "fire.ignite", 1.1f, 0.65f);
                    this.worldObj.playSoundAtEntity(this, "tile.piston.out", 0.9f, 0.7f);
                    this.worldObj.playSoundAtEntity(this, "random.fizz", 0.9f, 0.75f);
                } else if (this.countdown > 0 && this.countdown % 10 == 0) {
                    float progress = 1f - (float) this.countdown / COUNTDOWN_TICKS;
                    this.worldObj.playSoundAtEntity(this, "note.pling", 0.5f, 0.8f + progress * 0.65f);
                }

                if (this.countdown <= 0) {
                    this.phase = PHASE_LIFTOFF;
                    this.syncWatchers();
                    this.ignite();
                }
            } else {
                if (this.isIgnited()) {
                    this.spawnExhaust(false);
                    if (this.ticksExisted % 6 == 0) {
                        this.worldObj.playSound(this.posX, this.posY, this.posZ,
                            "mob.enderdragon.growl", 0.65f, 0.55f, false);
                    }
                    if (this.ticksExisted % 5 == 0) {
                        this.worldObj.playSound(this.posX, this.posY, this.posZ,
                            "random.fizz", 0.75f, 0.7f, false);
                    }
                }
            }
            return;
        }

        if (this.phaseOf() == PHASE_LIFTOFF) {
            this.speed = Math.min(1.25f, this.speed + 0.055f);
            this.setPosition(this.posX, this.posY + this.speed, this.posZ);
            if (!this.worldObj.isRemote) {
                this.destroyBlocksInPath();
                if (this.ownerUUID != null) {
                    EntityPlayerMP owner = PlayerLookup.byUUID(this.worldObj, this.ownerUUID);
                    if (owner != null) {
                        OrbitalSunworks.network.sendTo(new MessageLaunchShake(1.2f), owner);
                    }
                }
                if (this.speed >= 1.25f) {
                    this.phase = PHASE_ASCENT;
                    this.syncWatchers();
                }
            } else {
                this.spawnExhaust(true);
                if (this.ticksExisted % 7 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "mob.enderdragon.growl", 1.1f, 0.45f, false);
                }
                if (this.ticksExisted % 10 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "fireworks.launch", 0.9f, 0.65f, false);
                }
                this.worldObj.playSound(this.posX, this.posY, this.posZ,
                    "random.fizz", 0.85f, 0.5f, false);
            }
        } else {
            this.setPosition(this.posX + Math.sin(this.ticksExisted * 0.17f) * 0.012,
                this.posY + 2.45, this.posZ);
            if (!this.worldObj.isRemote) {
                this.destroyBlocksInPath();
                if (this.ownerUUID != null) {
                    float dist = (float) Math.max(1.0, this.posY - this.launchY);
                    EntityPlayerMP owner = PlayerLookup.byUUID(this.worldObj, this.ownerUUID);
                    if (owner != null) {
                        OrbitalSunworks.network.sendTo(
                            new MessageLaunchShake(Math.max(0f, 0.85f - dist * 0.025f)), owner);
                    }
                }
            } else if (this.ticksExisted % 2 == 0) {
                this.spawnExhaust(false);
                if (this.ticksExisted % 10 == 0) {
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "mob.enderdragon.growl", 0.7f, 0.5f, false);
                    this.worldObj.playSound(this.posX, this.posY, this.posZ,
                        "fireworks.launch", 0.65f, 0.75f, false);
                }
            }
        }

        if (!this.worldObj.isRemote && this.posY >= 250.0) {
            SatelliteManager.Satellite sat = SatelliteManager.get(this.worldObj)
                .deploySatellite(this, this.healthSatellite, this.hasDebrisShield);
            if (sat != null && this.ownerUUID != null) {
                EntityPlayerMP owner = PlayerLookup.byUUID(this.worldObj, this.ownerUUID);
                if (owner != null) {
                    OrbitalSunworks.network.sendTo(
                        new MessageDeployCinematic(sat.id, sat.name, sat.tier, sat.health, sat.hasDebrisShield()),
                        owner);
                }
            }
            this.setDead();
        }
    }

    private void destroyBlocksInPath() {
        int minX = MathHelper.floor_double(this.posX - 0.55);
        int maxX = MathHelper.floor_double(this.posX + 0.55);
        int minZ = MathHelper.floor_double(this.posZ - 0.55);
        int maxZ = MathHelper.floor_double(this.posZ + 0.55);
        int minY = MathHelper.floor_double(this.posY);
        int maxY = MathHelper.floor_double(this.posY + 5.9);
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    Block b = this.worldObj.getBlock(x, y, z);
                    if (b == Blocks.air || b == Blocks.bedrock || b.isReplaceable((IBlockAccess) this.worldObj, x, y, z)) {
                        continue;
                    }
                    this.worldObj.setBlockToAir(x, y, z);
                    b.dropBlockAsItem(this.worldObj, x, y, z, this.worldObj.getBlockMetadata(x, y, z) & 15, 0);
                }
            }
        }
    }

    private void ignite() {
        this.launchY = (float) this.posY;
        if (!this.worldObj.isRemote) {
            this.worldObj.playSoundAtEntity(this, "random.explode", 1.3f, 0.8f);
            this.worldObj.playSoundAtEntity(this, "fireworks.largeBlast", 1.2f, 0.7f);
            this.worldObj.playSoundAtEntity(this, "fireworks.launch", 1.2f, 0.85f);
            this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 1.0f, 1.4f);

            List entities = this.worldObj.getEntitiesWithinAABBExcludingEntity(this,
                this.boundingBox.expand(3.5, 2.5, 3.5));
            for (Object o : entities) {
                if (o instanceof EntityLivingBase) {
                    EntityLivingBase living = (EntityLivingBase) o;
                    living.attackEntityFrom(DamageSource.onFire, 6.0f);
                    double dx = living.posX - this.posX;
                    double dz = living.posZ - this.posZ;
                    double mag = Math.sqrt(dx * dx + dz * dz);
                    if (mag > 0.01) {
                        living.motionX += dx / mag * 0.95;
                        living.motionZ += dz / mag * 0.95;
                        living.motionY += 0.38;
                    }
                }
            }
        } else {
            for (int i = 0; i < 48; i++) {
                double ang = this.rand.nextDouble() * Math.PI * 2;
                double spd = 0.25 + this.rand.nextDouble() * 0.65;
                this.worldObj.spawnParticle("flame",
                    this.posX, this.posY + 0.3, this.posZ,
                    Math.cos(ang) * spd, 0.08, Math.sin(ang) * spd);
            }
        }
    }

    private void spawnExhaust(boolean liftoff) {
        int count = liftoff ? 14 : 6;
        for (int i = 0; i < count; i++) {
            double ang = this.rand.nextDouble() * Math.PI * 2;
            double r = this.rand.nextDouble() * 0.35;
            this.worldObj.spawnParticle("flame",
                this.posX + Math.cos(ang) * r, this.posY, this.posZ + Math.sin(ang) * r,
                0, -0.32 - this.rand.nextDouble() * 0.25, 0);
            double spread = liftoff ? 0.75 : 0.18;
            this.worldObj.spawnParticle("largesmoke",
                this.posX + Math.cos(ang) * r, this.posY - 0.35, this.posZ + Math.sin(ang) * r,
                Math.cos(ang) * spread, -0.04, Math.sin(ang) * spread);
        }
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
        this.syncWatchers();
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
            mods.appendTag(new NBTTagString(m));
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