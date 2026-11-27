package com.orbitalsunworks.rocket;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;
import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.block.BlockNavRocket;
import com.orbitalsunworks.network.MessageLaunchShake;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.util.PlayerLookup;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;
import com.orbitalsunworks.world.orbit.OrbitStructureManager;
import com.orbitalsunworks.world.orbit.OrbitTransferScheduler;

public class EntityNavRocket extends Entity {

    public static final int PHASE_COUNTDOWN = 0;
    public static final int PHASE_LIFTOFF = 1;
    public static final int PHASE_ASCENT = 2;
    public static final int PHASE_WARP = 3;
    public static final int PHASE_PARKED = 4;

    private static final int COUNTDOWN_TICKS = 60;
    private static final int DW_IGNITED = 28;
    private static final int DW_PHASE = 29;
    private static final int DW_PARK_Y = 30;

    private int phase = PHASE_COUNTDOWN;
    private int countdown = COUNTDOWN_TICKS;
    private float speed = 0f;
    private float launchY = 0f;
    private int warpTicks = 0;

    private UUID targetSatellite;
    private int homeDim = 0;
    private double homeX, homeY, homeZ;
    private float homeYaw, homePitch;
    private UUID ownerUUID;
    private EntityPlayerMP lastRider;
    private boolean itemReturned = false;

    public EntityNavRocket(World world) {
        super(world);
        this.setSize(1.35f, 5.0f);
        this.noClip = true;
        this.preventEntitySpawning = true;
    }

    public EntityNavRocket(World world, double x, double y, double z, UUID targetSat,
            int homeDim, double homeX, double homeY, double homeZ,
            float homeYaw, float homePitch, UUID owner) {
        this(world);
        this.setPosition(x, y, z);
        this.targetSatellite = targetSat;
        this.homeDim = homeDim;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.homeYaw = homeYaw;
        this.homePitch = homePitch;
        this.ownerUUID = owner;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public int getHomeDim() {
        return this.homeDim;
    }

    public double getHomeX() {
        return this.homeX;
    }

    public double getHomeY() {
        return this.homeY;
    }

    public double getHomeZ() {
        return this.homeZ;
    }

    public EntityPlayerMP getRider() {
        if (this.riddenByEntity instanceof EntityPlayerMP) {
            return (EntityPlayerMP) this.riddenByEntity;
        }
        return null;
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
        return this.phase != PHASE_COUNTDOWN || this.countdown < COUNTDOWN_TICKS - 12;
    }

    private void syncWatchers() {
        this.dataWatcher.updateObject(DW_PHASE, Integer.valueOf(this.phase));
        int ign = (this.phase != PHASE_COUNTDOWN || this.countdown < COUNTDOWN_TICKS - 12) ? 1 : 0;
        this.dataWatcher.updateObject(DW_IGNITED, Integer.valueOf(ign));
    }

    @Override
    protected void entityInit() {
        this.dataWatcher.addObject(DW_IGNITED, Integer.valueOf(0));
        this.dataWatcher.addObject(DW_PHASE, Integer.valueOf(PHASE_COUNTDOWN));
        this.dataWatcher.addObject(DW_PARK_Y, Integer.valueOf(0));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (!this.worldObj.isRemote) {
            if (this.riddenByEntity instanceof EntityPlayerMP) {
                this.lastRider = (EntityPlayerMP) this.riddenByEntity;
            }
            if (this.phase != PHASE_PARKED
                    && this.lastRider != null
                    && !this.lastRider.isDead
                    && this.worldObj.playerEntities.contains(this.lastRider)
                    && this.riddenByEntity == null
                    && this.lastRider.ridingEntity == null
                    && this.lastRider.dimension == this.dimension) {
                this.lastRider.mountEntity(this);
            }
            this.syncWatchers();
        }

        if (this.phaseOf() == PHASE_PARKED) {
            this.setDead();
            return;
        }

        if (!this.worldObj.isRemote && this.riddenByEntity == null && this.phase != PHASE_COUNTDOWN) {
            EntityPlayerMP gone = this.lastRider;
            if (gone != null && !this.itemReturned) {
                this.returnItem(gone);
            }
            this.setDead();
            return;
        }

        if (!this.worldObj.isRemote && this.riddenByEntity == null) {
            if (this.ticksExisted > 100) {
                this.setDead();
            }
            return;
        }

        int curPhase = this.phaseOf();
        switch (curPhase) {
            case PHASE_COUNTDOWN:
                if (!this.worldObj.isRemote) {
                    this.countdown--;
                    this.syncWatchers();
                    EntityPlayerMP rider = this.getRider();
                    if (rider != null) {
                        float ramp = 1f - (float) this.countdown / COUNTDOWN_TICKS;
                        OrbitalSunworks.network.sendTo(new MessageLaunchShake(0.35f * ramp), rider);
                    }
                    if (this.countdown == COUNTDOWN_TICKS - 12) {
                        this.worldObj.playSoundAtEntity(this, "fire.ignite", 1.0f, 0.7f);
                        this.worldObj.playSoundAtEntity(this, "tile.piston.out", 0.85f, 0.75f);
                    } else if (this.countdown > 0 && this.countdown % 10 == 0) {
                        float prog = 1f - (float) this.countdown / COUNTDOWN_TICKS;
                        this.worldObj.playSoundAtEntity(this, "note.pling", 0.5f, 0.85f + prog * 0.6f);
                        this.worldObj.playSoundAtEntity(this, "random.fizz", 0.65f, 0.8f);
                    }
                    if (this.countdown <= 0) {
                        this.phase = PHASE_LIFTOFF;
                        this.launchY = (float) this.posY;
                        this.syncWatchers();
                        this.worldObj.playSoundAtEntity(this, "random.explode", 1.15f, 0.85f);
                        this.worldObj.playSoundAtEntity(this, "fireworks.largeBlast", 1.1f, 0.75f);
                        this.worldObj.playSoundAtEntity(this, "fireworks.launch", 1.15f, 0.9f);
                        this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 0.9f, 1.4f);
                    }
                } else {
                    if (this.isIgnited()) {
                        this.spawnExhaust();
                    } else if (this.ticksExisted % 2 == 0) {
                        this.padSmoke();
                    }
                }
                break;

            case PHASE_LIFTOFF:
                this.speed = Math.min(1.25f, this.speed + 0.055f);
                this.setPosition(this.posX, this.posY + this.speed, this.posZ);
                if (!this.worldObj.isRemote) {
                    EntityPlayerMP rider = this.getRider();
                    if (rider != null) {
                        OrbitalSunworks.network.sendTo(new MessageLaunchShake(1.0f), rider);
                    }
                    if (this.ticksExisted % 6 == 0) {
                        this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 0.6f, 0.5f);
                    }
                    if (this.speed >= 1.25f) {
                        this.phase = PHASE_ASCENT;
                        this.syncWatchers();
                    }
                } else {
                    this.spawnExhaust();
                }
                break;

            case PHASE_ASCENT:
                this.setPosition(this.posX + Math.sin(this.ticksExisted * 0.15f) * 0.014,
                    this.posY + 2.4, this.posZ);
                if (!this.worldObj.isRemote) {
                    EntityPlayerMP rider = this.getRider();
                    if (rider != null) {
                        float dist = (float) Math.max(1.0, this.posY - this.launchY);
                        OrbitalSunworks.network.sendTo(
                            new MessageLaunchShake(Math.max(0f, 0.75f - dist * 0.028f)), rider);
                    }
                    if (this.ticksExisted % 10 == 0) {
                        this.worldObj.playSoundAtEntity(this, "fireworks.launch", 0.7f, 0.8f);
                    }
                    if (this.posY >= 180.0) {
                        this.phase = PHASE_WARP;
                        this.warpTicks = 0;
                        this.syncWatchers();
                        this.worldObj.playSoundAtEntity(this, "portal.travel", 0.9f, 0.85f);
                        this.worldObj.playSoundAtEntity(this, "fireworks.launch", 1.0f, 1.2f);
                    }
                } else {
                    this.spawnExhaust();
                }
                break;

            case PHASE_WARP:
                this.setPosition(this.posX, this.posY + 3.1, this.posZ);
                if (!this.worldObj.isRemote) {
                    this.warpTicks++;
                    EntityPlayerMP rider = this.getRider();
                    if (rider == null) {
                        this.setDead();
                        return;
                    }
                    if (this.warpTicks >= 20) {
                        this.arrive(rider);
                        return;
                    }
                } else {
                    this.spawnExhaust();
                }
                break;
        }
    }

    private void padSmoke() {
        for (int i = 0; i < 4; i++) {
            double ang = this.rand.nextDouble() * Math.PI * 2;
            double r = 0.55 + this.rand.nextDouble() * 0.65;
            this.worldObj.spawnParticle("largesmoke",
                this.posX + Math.cos(ang) * r, this.posY + 0.1, this.posZ + Math.sin(ang) * r,
                Math.cos(ang) * 0.08, 0.02, Math.sin(ang) * 0.08);
        }
    }

    private void arrive(EntityPlayerMP rider) {
        final int dim = OrbitDimensionManager.getDimensionId();
        SatelliteManager manager = SatelliteManager.get(this.worldObj);
        final SatelliteManager.Satellite sat = this.targetSatellite != null
            ? manager.getById(this.targetSatellite) : null;
        if (sat == null) {
            OSUtils.sendChat(rider, "\u00a7cSat\u00e9lite de destino n\u00e3o encontrado!");
            this.returnHome(rider);
            return;
        }

        int[] c = OrbitStructureManager.coordsForIndex(OrbitStructureManager.indexOf(sat, manager));
        final double parkX = c[0] + 6.5, parkY = c[1] + 1.0, parkZ = c[2] + 0.5;
        final int navX = c[0] + 9, navY = c[1], navZ = c[2];
        final String satName = sat.name;
        final UUID riderId = rider.getUniqueID();
        final int fHomeDim = this.homeDim;
        final double fHomeX = this.homeX, fHomeY = this.homeY, fHomeZ = this.homeZ;
        final float fHomeYaw = this.homeYaw, fHomePitch = this.homePitch;

        this.itemReturned = true;
        this.setDead();

        OrbitTransferScheduler.schedule(rider, dim, parkX, parkY, parkZ, 90f, 0f, new Runnable() {
            @Override
            public void run() {
                WorldServer orbitWorld = MinecraftServer.getServer().worldServerForDimension(dim);
                EntityPlayerMP p = PlayerLookup.byUUID(orbitWorld, riderId);
                if (p == null) {
                    return;
                }
                OrbitStructureManager.ensureStructure(p.worldObj, sat);

                for (int i = BlockNavRocket.HEIGHT - 1; i >= 0; i--) {
                    if (p.worldObj.getBlock(navX, navY + i, navZ) == OSBlocks.blockNavRocket) {
                        p.worldObj.setBlockToAir(navX, navY + i, navZ);
                    }
                }
                for (int i = 0; i < BlockNavRocket.HEIGHT; i++) {
                    p.worldObj.setBlock(navX, navY + i, navZ, OSBlocks.blockNavRocket, i, 3);
                }
                TileEntity te = p.worldObj.getTileEntity(navX, navY, navZ);
                if (te instanceof TileNavRocket) {
                    ((TileNavRocket) te).setOrbitReturn(
                        riderId, satName, fHomeDim, fHomeX, fHomeY, fHomeZ, fHomeYaw, fHomePitch);
                }
                p.worldObj.markBlockForUpdate(navX, navY, navZ);

                p.capabilities.isFlying = true;
                p.sendPlayerAbilities();
                OSUtils.sendChat(p, "\u00a7b\u00d3rbita alcan\u00e7ada! \u00a7fVoe at\u00e9 \u00a7e" + satName
                    + "\u00a7f ou clique com o direito no seu foguete ancorado para voltar \u00e0 Terra.");
            }
        });
    }

    public static void returnFromOrbitBlock(final EntityPlayerMP player, TileNavRocket tile) {
        if (player == null || tile == null) {
            return;
        }
        final World orbitWorld = tile.getWorldObj();
        final int tx = tile.xCoord, ty = tile.yCoord, tz = tile.zCoord;
        final int dim;
        final double x, y, z;
        final float yaw, pitch;

        if (tile.homeSet) {
            dim = tile.homeDim;
            x = tile.homeX;
            y = tile.homeY;
            z = tile.homeZ;
            yaw = tile.homeYaw;
            pitch = tile.homePitch;
        } else {
            WorldServer overworld = player.mcServer.worldServerForDimension(0);
            ChunkCoordinates spawn = overworld.getSpawnPoint();
            dim = 0;
            x = spawn.posX + 0.5;
            y = spawn.posY;
            z = spawn.posZ + 0.5;
            yaw = player.rotationYaw;
            pitch = player.rotationPitch;
        }

        if (orbitWorld != null) {
            for (int i = BlockNavRocket.HEIGHT - 1; i >= 0; i--) {
                if (orbitWorld.getBlock(tx, ty + i, tz) == OSBlocks.blockNavRocket) {
                    orbitWorld.setBlockToAir(tx, ty + i, tz);
                }
            }
        }

        final UUID pid = player.getUniqueID();
        OrbitTransferScheduler.schedule(player, dim, x, y, z, yaw, pitch, new Runnable() {
            @Override
            public void run() {
                WorldServer dest = MinecraftServer.getServer().worldServerForDimension(dim);
                EntityPlayerMP p = PlayerLookup.byUUID(dest, pid);
                if (p == null) {
                    p = player;
                }
                p.capabilities.isFlying = p.capabilities.isCreativeMode;
                p.sendPlayerAbilities();

                ItemStack stack = new ItemStack(OSItems.itemNavRocket);
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("HomeDim", dim);
                tag.setDouble("HomeX", x);
                tag.setDouble("HomeY", y);
                tag.setDouble("HomeZ", z);
                stack.setTagCompound(tag);

                if (!p.inventory.addItemStackToInventory(stack)) {
                    p.worldObj.spawnEntityInWorld(new EntityItem(p.worldObj, p.posX, p.posY, p.posZ, stack));
                }
                p.inventory.markDirty();
                p.worldObj.playSoundAtEntity(p, "portal.travel", 0.7f, 1.15f);
                OSUtils.sendChat(p, "\u00a7aDe volta \u00e0 Terra! \u00a7fO Foguete de Navega\u00e7\u00e3o voltou para o seu invent\u00e1rio.");
            }
        });
    }

    private void returnItem(EntityPlayerMP rider) {
        if (this.itemReturned) {
            return;
        }
        this.itemReturned = true;
        ItemStack stack = new ItemStack(OSItems.itemNavRocket);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("HomeDim", this.homeDim);
        tag.setDouble("HomeX", this.homeX);
        tag.setDouble("HomeY", this.homeY);
        tag.setDouble("HomeZ", this.homeZ);
        stack.setTagCompound(tag);
        if (!rider.inventory.addItemStackToInventory(stack)) {
            rider.worldObj.spawnEntityInWorld(new EntityItem(rider.worldObj, rider.posX, rider.posY, rider.posZ, stack));
        }
    }

    private void returnHome(final EntityPlayerMP rider) {
        this.returnItem(rider);
        final int dim = this.homeDim;
        final double x = this.homeX, y = this.homeY, z = this.homeZ;
        final float yaw = this.homeYaw, pitch = this.homePitch;
        this.setDead();
        OrbitTransferScheduler.schedule(rider, dim, x, y, z, yaw, pitch, new Runnable() {
            @Override
            public void run() {
                rider.capabilities.isFlying = rider.capabilities.isCreativeMode;
                rider.sendPlayerAbilities();
            }
        });
    }

    public static void sendPlayerHome(EntityPlayerMP player) {
        int dim = 0;
        double x = player.posX, y = player.posY, z = player.posZ;
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == OSItems.itemNavRocket
                && held.hasTagCompound() && held.getTagCompound().hasKey("HomeX")) {
            NBTTagCompound tag = held.getTagCompound();
            dim = tag.getInteger("HomeDim");
            x = tag.getDouble("HomeX");
            y = tag.getDouble("HomeY");
            z = tag.getDouble("HomeZ");
        } else {
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack st = player.inventory.mainInventory[i];
                if (st != null && st.getItem() == OSItems.itemNavRocket
                        && st.hasTagCompound() && st.getTagCompound().hasKey("HomeX")) {
                    NBTTagCompound tag = st.getTagCompound();
                    dim = tag.getInteger("HomeDim");
                    x = tag.getDouble("HomeX");
                    y = tag.getDouble("HomeY");
                    z = tag.getDouble("HomeZ");
                    break;
                }
            }
        }
        if (x == player.posX && y == player.posY && z == player.posZ) {
            WorldServer overworld = player.mcServer.worldServerForDimension(0);
            ChunkCoordinates spawn = overworld.getSpawnPoint();
            x = spawn.posX + 0.5;
            y = spawn.posY;
            z = spawn.posZ + 0.5;
            dim = 0;
        }
        final EntityPlayerMP p = player;
        OrbitTransferScheduler.schedule(player, dim, x, y, z, new Runnable() {
            @Override
            public void run() {
                p.capabilities.isFlying = p.capabilities.isCreativeMode;
                p.sendPlayerAbilities();
            }
        });
    }

    private void spawnExhaust() {
        float d = 0.19f;
        float[][] nozzles = { { -d, -d }, { d, -d }, { -d, d }, { d, d } };
        for (float[] nz : nozzles) {
            this.worldObj.spawnParticle("flame",
                this.posX + nz[0], this.posY - 0.05, this.posZ + nz[1],
                (this.rand.nextDouble() - 0.5) * 0.06, -0.34, (this.rand.nextDouble() - 0.5) * 0.06);
        }
        for (int i = 0; i < 4; i++) {
            double ang = this.rand.nextDouble() * Math.PI * 2;
            double r = this.rand.nextDouble() * 0.35;
            this.worldObj.spawnParticle("largesmoke",
                this.posX + Math.cos(ang) * r, this.posY - 0.45, this.posZ + Math.sin(ang) * r,
                Math.cos(ang) * 0.25, -0.05, Math.sin(ang) * 0.25);
        }
    }

    @Override
    public double getMountedYOffset() {
        return 0.75;
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public void updateRiderPosition() {
        super.updateRiderPosition();
        if (this.riddenByEntity != null) {
            this.riddenByEntity.setPosition(this.posX, this.posY + this.getMountedYOffset(), this.posZ);
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
        String sat = tag.getString("TargetSat");
        if (!sat.isEmpty()) {
            try {
                this.targetSatellite = UUID.fromString(sat);
            } catch (IllegalArgumentException ignored) {
            }
        }
        String owner = tag.getString("Owner");
        if (!owner.isEmpty()) {
            try {
                this.ownerUUID = UUID.fromString(owner);
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.homeDim = tag.getInteger("HomeDim");
        this.homeX = tag.getDouble("HomeX");
        this.homeY = tag.getDouble("HomeY");
        this.homeZ = tag.getDouble("HomeZ");
        this.homeYaw = tag.getFloat("HomeYaw");
        this.homePitch = tag.getFloat("HomePitch");
        this.syncWatchers();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("Phase", this.phase);
        tag.setInteger("Countdown", this.countdown);
        if (this.targetSatellite != null) {
            tag.setString("TargetSat", this.targetSatellite.toString());
        }
        if (this.ownerUUID != null) {
            tag.setString("Owner", this.ownerUUID.toString());
        }
        tag.setInteger("HomeDim", this.homeDim);
        tag.setDouble("HomeX", this.homeX);
        tag.setDouble("HomeY", this.homeY);
        tag.setDouble("HomeZ", this.homeZ);
        tag.setFloat("HomeYaw", this.homeYaw);
        tag.setFloat("HomePitch", this.homePitch);
    }
}