package com.orbitalsunworks.rocket;

import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.block.BlockDispersionRocket;
import com.orbitalsunworks.network.MessageLaunchShake;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.util.PlayerLookup;

/**
 * Entidade de voo do Foguete Dispersor de Meteoros.
 * Ao ser lançado pelo jogador, dispersa o evento de meteoro imediatamente
 * e sobe em alta velocidade realizando a detonação dispersora na estratosfera.
 */
public class EntityDispersionRocket extends Entity {

    public static final int PHASE_IGNITION = 0;
    public static final int PHASE_ASCENT = 1;

    private static final int IGNITION_TICKS = 24;
    private static final int DW_PHASE = 28;
    private static final int DW_IGNITED = 29;

    private int phase = PHASE_IGNITION;
    private int countdown = IGNITION_TICKS;
    private float speed = 0.0f;
    private double launchY = 64.0;
    private UUID ownerUUID;

    public EntityDispersionRocket(World world) {
        super(world);
        this.setSize(1.1f, 6.0f);
        this.noClip = true;
        this.preventEntitySpawning = true;
    }

    public EntityDispersionRocket(World world, double x, double y, double z, UUID owner) {
        this(world);
        this.setPosition(x, y, z);
        this.launchY = y;
        this.ownerUUID = owner;
    }

    public static void launchFromBlock(World world, EntityPlayerMP player, int bx, int by, int bz) {
        if (world == null || world.isRemote || player == null) {
            return;
        }
        if (world.getBlock(bx, by, bz) != OSBlocks.blockDispersionRocket) {
            return;
        }
        int baseY = by - world.getBlockMetadata(bx, by, bz);
        for (int i = BlockDispersionRocket.HEIGHT - 1; i >= 0; i--) {
            if (world.getBlock(bx, baseY + i, bz) == OSBlocks.blockDispersionRocket) {
                world.setBlockToAir(bx, baseY + i, bz);
            }
        }
        EntityDispersionRocket rocket = new EntityDispersionRocket(
            world, bx + 0.5, baseY, bz + 0.5, player.getUniqueID());
        world.spawnEntityInWorld(rocket);
        world.playSoundAtEntity(rocket, "fire.ignite", 1.1f, 0.7f);
        world.playSoundAtEntity(rocket, "fireworks.launch", 1.0f, 0.75f);

        boolean dispersed = SatelliteManager.get(world).disperseMeteorFor(player);
        if (dispersed) {
            OSUtils.sendChat(player,
                "\u00a7b[Orbital Sunworks] \u00a7aFoguete Dispersor lan\u00e7ado! A chuva de meteoros foi neutralizada!");
        } else {
            OSUtils.sendChat(player,
                "\u00a7b[Orbital Sunworks] \u00a7eFoguete Dispersor lan\u00e7ado em patrulha atmosf\u00e9rica.");
        }
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(DW_PHASE, Integer.valueOf(PHASE_IGNITION));
        this.dataWatcher.addObject(DW_IGNITED, Integer.valueOf(1));
    }

    public int phaseOf() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.dataWatcher.getWatchableObjectInt(DW_PHASE);
        }
        return this.phase;
    }

    public boolean isIgnited() {
        if (this.worldObj != null && this.worldObj.isRemote) {
            return this.dataWatcher.getWatchableObjectInt(DW_IGNITED) != 0;
        }
        return true;
    }

    private void syncWatchers() {
        this.dataWatcher.updateObject(DW_PHASE, Integer.valueOf(this.phase));
        this.dataWatcher.updateObject(DW_IGNITED, Integer.valueOf(1));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!this.worldObj.isRemote) {
            this.syncWatchers();
        }
        if (this.phaseOf() == PHASE_IGNITION) {
            if (!this.worldObj.isRemote) {
                this.countdown--;
                this.sendShake(0.45f * (1f - (float) this.countdown / IGNITION_TICKS));
                if (this.countdown <= 0) {
                    this.phase = PHASE_ASCENT;
                    this.syncWatchers();
                    this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 1.0f, 0.55f);
                    this.worldObj.playSoundAtEntity(this, "fireworks.launch", 1.1f, 0.65f);
                }
            } else {
                this.spawnExhaust(false);
            }
            return;
        }

        this.speed = Math.min(2.8f, this.speed + 0.14f);
        this.setPosition(this.posX + Math.sin(this.ticksExisted * 0.25f) * 0.01,
            this.posY + this.speed, this.posZ);

        if (!this.worldObj.isRemote) {
            this.destroyBlocksInPath();
            float dist = (float) Math.max(0.0, this.posY - this.launchY);
            this.sendShake(Math.max(0f, 0.75f - dist * 0.02f));
            if (this.posY >= Math.min(255.0, this.launchY + 135.0)) {
                this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ,
                    "fireworks.largeBlast", 2.0f, 0.85f);
                this.setDead();
            }
        } else {
            this.spawnExhaust(true);
            if (this.ticksExisted % 8 == 0) {
                this.worldObj.playSound(this.posX, this.posY, this.posZ,
                    "fireworks.launch", 0.75f, 0.7f, false);
            }
        }
    }

    private void sendShake(float amount) {
        if (this.ownerUUID != null && amount > 0.02f) {
            EntityPlayerMP owner = PlayerLookup.byUUID(this.worldObj, this.ownerUUID);
            if (owner != null) {
                OrbitalSunworks.network.sendTo(new MessageLaunchShake(amount), owner);
            }
        }
    }

    private void destroyBlocksInPath() {
        int minX = MathHelper.floor_double(this.posX - 0.45);
        int maxX = MathHelper.floor_double(this.posX + 0.45);
        int minZ = MathHelper.floor_double(this.posZ - 0.45);
        int maxZ = MathHelper.floor_double(this.posZ + 0.45);
        int minY = MathHelper.floor_double(this.posY);
        int maxY = MathHelper.floor_double(this.posY + 5.8);
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

    private void spawnExhaust(boolean ascending) {
        for (int i = 0; i < (ascending ? 6 : 4); i++) {
            this.worldObj.spawnParticle("flame",
                this.posX + (this.rand.nextDouble() - 0.5) * 0.25,
                this.posY - 0.1,
                this.posZ + (this.rand.nextDouble() - 0.5) * 0.25,
                (this.rand.nextDouble() - 0.5) * 0.08, -0.45, (this.rand.nextDouble() - 0.5) * 0.08);
            this.worldObj.spawnParticle("largesmoke",
                this.posX + (this.rand.nextDouble() - 0.5) * 0.4,
                this.posY - 0.4,
                this.posZ + (this.rand.nextDouble() - 0.5) * 0.4,
                (this.rand.nextDouble() - 0.5) * 0.18, -0.12, (this.rand.nextDouble() - 0.5) * 0.18);
        }
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

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        this.phase = tag.getInteger("Phase");
        this.countdown = tag.getInteger("Countdown");
        this.speed = tag.getFloat("Speed");
        this.launchY = tag.getDouble("LaunchY");
        String owner = tag.getString("Owner");
        if (!owner.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.syncWatchers();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("Phase", this.phase);
        tag.setInteger("Countdown", this.countdown);
        tag.setFloat("Speed", this.speed);
        tag.setDouble("LaunchY", this.launchY);
        if (this.ownerUUID != null) {
            tag.setString("Owner", this.ownerUUID.toString());
        }
    }
}