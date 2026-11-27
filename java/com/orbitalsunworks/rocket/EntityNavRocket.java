package com.orbitalsunworks.rocket;

import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import com.orbitalsunworks.OrbitalSunworks;
import com.orbitalsunworks.satellite.SatelliteManager;
import com.orbitalsunworks.util.OSUtils;
import com.orbitalsunworks.util.PlayerLookup;
import com.orbitalsunworks.world.orbit.OrbitDimensionManager;
import com.orbitalsunworks.world.orbit.OrbitStructureManager;
import com.orbitalsunworks.world.orbit.OrbitTransferScheduler;

/**
 * FOGUETE DE NAVEGAÇÃO (v0.7.0) — o "táxi orbital", agora DECOLA DO PAD.
 *
 * Fluxo NOVO (igual ao que o usuário pediu, estilo Galacticraft):
 *  1. O item posiciona um BLOCO na plataforma (BlockLaunchPlatform);
 *  2. clicar no bloco abre o mapa orbital (satélites DO jogador);
 *  3. IR -> o servidor remove o bloco, spawna ESTA entidade no lugar e
 *     MONTA o jogador nela (câmera F5 no cliente);
 *  4. countdown 3s (tremor + fumaça + som) -> liftoff (sobe acelerando,
 *     exaustão) -> ascent rápido -> warp (som de portal);
 *  5. CHEGADA: teleporta o jogador pra DIMENSÃO ORBITAL ao lado da
 *     estrutura do satélite escolhido, desmonta, ativa o fly local e
 *     DEVOLVE o item do foguete pro inventário (não se perde).
 *
 * Segurança multijogador (regra do servidor do usuário):
 *  - tremor SÓ pro rider (pacote direto, ninguém mais recebe);
 *  - noClip/sem colisão/sem dano (não empurra nem mata ninguém);
 *  - viagem única por jogador (handler valida spam-click);
 *  - rider cai/desconecta -> entidade se limpa sozinha.
 */
public class EntityNavRocket extends Entity {

    public static final int PHASE_COUNTDOWN = 0;
    public static final int PHASE_LIFTOFF = 1;
    public static final int PHASE_ASCENT = 2;
    public static final int PHASE_WARP = 3;
    /** v0.8.0: estacionada na órbita — flutua esperando o dono voltar. */
    public static final int PHASE_PARKED = 4;

    private static final int COUNTDOWN_TICKS = 60; // 3s

    private int phase = PHASE_COUNTDOWN;
    private int countdown = COUNTDOWN_TICKS;
    private float speed = 0f;
    private float launchY = 0f;

    /** Satélite de destino (UUID). */
    private UUID targetSatellite;
    /** Para onde devolver o jogador na Terra (dim/x/y/z). */
    private int homeDim = 0;
    private double homeX, homeY, homeZ;
    /** Dono da viagem (quem montou). */
    private UUID ownerUUID;
    private boolean itemReturned = false;

    public EntityNavRocket(World world) {
        super(world);
        this.setSize(1.2f, 3.2f);
        this.noClip = true;
        this.preventEntitySpawning = true;
    }

    public EntityNavRocket(World world, double x, double y, double z, UUID targetSat,
            int homeDim, double homeX, double homeY, double homeZ, UUID owner) {
        this(world);
        this.setPosition(x, y, z);
        this.targetSatellite = targetSat;
        this.homeDim = homeDim;
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
        this.ownerUUID = owner;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    /** Coordenadas de casa (pad de origem) — usadas pela GUI de retorno. */
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

    /** O jogador montado (ou null). */
    public EntityPlayerMP getRider() {
        if (this.riddenByEntity instanceof EntityPlayerMP) {
            return (EntityPlayerMP) this.riddenByEntity;
        }
        return null;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Rider caiu/desconectou: limpa a entidade (o jogador volta ao
        // normal sozinho; item devolvido no returnHome). EXCETO se já
        // chegou e está estacionada na órbita (PHASE_PARKED): a nave
        // continua lá esperando o dono.
        if (this.riddenByEntity == null && this.phase != PHASE_COUNTDOWN
            && this.phase != PHASE_PARKED) {
            if (!this.worldObj.isRemote) {
                EntityPlayerMP gone = this.lastRider;
                if (gone != null && !this.itemReturned) {
                    this.returnItem(gone);
                }
            }
            this.setDead();
            return;
        }
        // v0.10.0 — REMOUNT ANTI-SHIFT: se o piloto APERTAR SHIFT (ou
        // qualquer coisa desmontar) durante a viagem, o SERVIDOR monta
        // de volta NO MESMO TICK. O pedido é literal: "se você apertar
        // SHIFT você sai dela e ela vai sem você, isso JAMAIS é pra
        // acontecer". O client manda o mountEntity(null); aqui
        // desfazemos no server-side (autoridade) enquanto estiver em
        // fase de voo (countdown -> warp).
        if (!this.worldObj.isRemote
            && this.phase != PHASE_PARKED
            && this.lastRider != null
            && !this.lastRider.isDead
            && this.riddenByEntity == null
            && this.lastRider.ridingEntity == null
            && this.lastRider.dimension == this.dimension) {
            this.lastRider.mountEntity(this);
        }
        if (this.phase == PHASE_PARKED) {
            this.updateParked();
            return;
        }
        if (this.riddenByEntity == null) {
            // Sem rider e em countdown: espera (o mount acontece logo).
            if (this.ticksExisted > 100) {
                this.setDead(); // ninguém embarcou em 5s
            }
            return;
        }
        if (this.riddenByEntity instanceof EntityPlayerMP) {
            this.lastRider = (EntityPlayerMP) this.riddenByEntity;
        }

        switch (this.phase) {
            case PHASE_COUNTDOWN:
                this.countdown--;
                // Tremor/efeitos SÓ pro rider (pacote direto).
                if (!this.worldObj.isRemote) {
                    EntityPlayerMP rider = getRider();
                    if (rider != null) {
                        float ramp = 1f - (float) this.countdown / COUNTDOWN_TICKS;
                        OrbitalSunworks.network.sendTo(
                            new com.orbitalsunworks.network.MessageLaunchShake(0.3f * ramp), rider);
                        if (this.countdown % 10 == 0) {
                            this.worldObj.playSoundAtEntity(this, "random.fizz", 0.7f, 0.8f);
                        }
                    }
                    // Fumaça no pad (todo mundo vê, é só partícula).
                    if (this.ticksExisted % 2 == 0) {
                        this.padSmoke();
                    }
                }
                if (this.countdown <= 0) {
                    this.phase = PHASE_LIFTOFF;
                    this.updatePhaseWatcher(); // v0.10.0: cliente sabendo
                    this.launchY = (float) this.posY;
                    if (!this.worldObj.isRemote) {
                        this.worldObj.playSoundAtEntity(this, "random.explode", 1.0f, 0.9f);
                        this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 0.9f, 1.5f);
                    }
                }
                break;

            case PHASE_LIFTOFF: {
                this.speed = Math.min(1.2f, this.speed + 0.05f);
                this.setPosition(this.posX, this.posY + this.speed, this.posZ);
                if (!this.worldObj.isRemote) {
                    EntityPlayerMP rider = getRider();
                    if (rider != null) {
                        OrbitalSunworks.network.sendTo(
                            new com.orbitalsunworks.network.MessageLaunchShake(0.9f), rider);
                    }
                    this.worldObj.playSoundAtEntity(this, "mob.enderdragon.growl", 0.5f, 0.45f);
                }
                if (this.speed >= 1.2f) {
                    this.phase = PHASE_ASCENT;
                    this.updatePhaseWatcher(); // v0.10.0
                }
                break;
            }

            case PHASE_ASCENT: {
                this.setPosition(this.posX + Math.sin(this.ticksExisted * 0.15f) * 0.015,
                    this.posY + 2.4, this.posZ);
                if (!this.worldObj.isRemote) {
                    EntityPlayerMP rider = getRider();
                    if (rider != null) {
                        float dist = (float) Math.max(1.0, this.posY - this.launchY);
                        OrbitalSunworks.network.sendTo(
                            new com.orbitalsunworks.network.MessageLaunchShake(
                                Math.max(0f, 0.7f - dist * 0.03f)), rider);
                    }
                }
                if (this.posY >= 180) {
                    this.phase = PHASE_WARP;
                    this.updatePhaseWatcher(); // v0.10.0
                    this.warpTicks = 0;
                    if (!this.worldObj.isRemote) {
                        this.worldObj.playSoundAtEntity(this, "portal.portal", 1.0f, 0.6f);
                    }
                }
                break;
            }

            case PHASE_WARP: {
                // 20 ticks de "tunel" e teleporta.
                this.setPosition(this.posX, this.posY + 3.0, this.posZ);
                this.warpTicks++;
                if (!this.worldObj.isRemote) {
                    EntityPlayerMP rider = getRider();
                    if (rider == null) {
                        this.setDead();
                        return;
                    }
                    if (this.warpTicks >= 20) {
                        this.arrive(rider);
                        return;
                    }
                }
                break;
            }
        }

        // Partículas de exaustão (no cliente de quem está vendo).
        if (this.worldObj.isRemote && this.phase != PHASE_COUNTDOWN) {
            this.spawnExhaust();
        }
    }

    private EntityPlayerMP lastRider;
    private int warpTicks = 0;

    /** Fumaça baixa no pad durante o countdown. */
    private void padSmoke() {
        for (int i = 0; i < 4; i++) {
            double ang = this.rand.nextDouble() * Math.PI * 2;
            double r = 0.5 + this.rand.nextDouble() * 0.6;
            this.worldObj.playAuxSFXAtEntity(null, 2000,
                net.minecraft.util.MathHelper.floor_double(this.posX + Math.cos(ang) * r),
                net.minecraft.util.MathHelper.floor_double(this.posY),
                net.minecraft.util.MathHelper.floor_double(this.posZ + Math.sin(ang) * r),
                0); // smoke particle fake — barato
        }
    }

    /** CHEGADA: teleporta o rider pra órbita, ao lado da estrutura.
     *  v0.9.0: o TELEPORTE é AGENDADO (OrbitTransferScheduler) — rodar o
     *  transferPlayerToDimension aqui dentro CRASHAVA o servidor
     *  (IndexOutOfBoundsException no World.updateEntities: o transfer
     *  remove o player da loadedEntityList NO MEIO do loop). */
    private void arrive(EntityPlayerMP rider) {
        // 1) Devolve o item do foguete (inventário viaja com o player).
        this.returnItem(rider);

        // 3) Posição de chegada: AO LADO da estrutura (não dentro).
        final int dim = OrbitDimensionManager.getDimensionId();
        SatelliteManager manager = SatelliteManager.get(this.worldObj);
        final SatelliteManager.Satellite sat = this.targetSatellite != null
            ? manager.getById(this.targetSatellite) : null;
        if (sat == null) {
            OSUtils.sendChat(rider, "§cSatélite de destino não encontrado!");
            this.returnHome(rider);
            return;
        }
        int[] c = OrbitStructureManager.coordsForIndex(
            OrbitStructureManager.indexOf(sat, manager));
        final double parkX = c[0] + 6.5, parkY = c[1] + 1.0, parkZ = c[2] + 0.5;
        final String satName = sat.name;

        // 4) A NAVE estaciona na órbita AGORA (clone NBT — sem teleporte
        //    de entidade, não mexe na lista do mundo atual).
        this.phase = PHASE_PARKED;
        this.noClip = true;
        this.parkBaseY = (float) (parkY + 1.2);
        this.updateParkBaseYWatcher();
        this.updatePhaseWatcher(); // v0.10.0: o cliente SABE que é parked
        this.cloneToOrbit(dim, parkX + 2.0, parkY + 1.2, parkZ + 1.5);
        this.setDead();

        // 5) O JOGADOR teleporta no início do PRÓXIMO tick (fora do loop
        //    de entidades — a correção do crash).
        final UUID riderId = rider.getUniqueID();
        OrbitTransferScheduler.schedule(rider, dim, parkX, parkY, parkZ, new Runnable() {
            @Override
            public void run() {
                // Roda DEPOIS de chegar (fora do tick de entidade).
                EntityPlayerMP p = com.orbitalsunworks.util.PlayerLookup
                    .byUUID(((net.minecraft.world.WorldServer) net.minecraft.server.MinecraftServer
                        .getServer().worldServerForDimension(dim)), riderId);
                if (p == null) {
                    // caiu do servidor no meio: nada a fazer
                    return;
                }
                // v0.10.0 FIX: a estrutura e a nave só existem DEPOIS que
                // a dimensão CARREGOU. O log do usuário mostrava
                // "Loading dimension 2" DEPOIS do arrive — a estrutura
                // era criada com o mundo null e falhava em silêncio (o
                // satélite nunca estava lá). Agora garantimos aqui, com
                // o player JÁ dentro da dimensão (ela acabou de carregar
                // pro teleporte dele).
                OrbitStructureManager.ensureStructure(p.worldObj, sat);
                p.capabilities.isFlying = true; // fly ativo na órbita
                p.sendPlayerAbilities();
                OSUtils.sendChat(p, "§bÓrbita alcançada! §fVoe até " + satName
                    + " e clique com o direito na estrutura. §bSua nave espera ao lado pra voltar.");
            }
        });
    }

    /**
     * "Transfere" a nave para a dimensão orbital por CLONE NBT (sem o
     * Teleporter do vanilla — nada de portal de obsidiana no espaço).
     * O clone carrega Phase=PARKED, casa, dono: 100% do estado.
     * v0.10.0: se a dimensão AINDA não carregou (o arrive roda antes do
     * teleporte do jogador — o log mostrava "Loading dimension 2" só
     * DEPOIS), RETENTA no próximo tick do servidor em vez de falhar em
     * silêncio (era por isso que a nave não esperava ninguém).
     */
    private void cloneToOrbit(int dim, double x, double y, double z) {
        WorldServer orbit = net.minecraft.server.MinecraftServer.getServer()
            .worldServerForDimension(dim);
        if (orbit == null) {
            // Dimensão não carregada AINDA: agenda a tentativa pro próximo
            // tick (o teleporte do jogador força a carga; então spawna).
            if (this.cloneRetries < 100) {
                this.cloneRetries++;
                final int fDim = dim;
                final double fx = x, fy = y, fz = z;
                final NBTTagCompound snapshot = new NBTTagCompound();
                this.writeToNBT(snapshot);
                // Snapshot IMUTÁVEL do estado (a entidade original morre
                // logo em seguida — o clone usa ESTE pacote).
                com.orbitalsunworks.world.orbit.OrbitTransferScheduler
                    .scheduleTick(new Runnable() {
                        @Override
                        public void run() {
                            spawnCloneFromNBT(fDim, fx, fy, fz, snapshot, 0);
                        }
                    });
            }
            return;
        }
        this.spawnCloneNow(orbit, x, y, z);
    }

    /** Tentativas de clone (retry quando a dimensão não carregou). */
    private int cloneRetries = 0;

    /** Spawna a nave a partir de um snapshot NBT (usado nos retries). */
    private static void spawnCloneFromNBT(int dim, double x, double y, double z,
            NBTTagCompound snapshot, int retries) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        WorldServer orbit = server.worldServerForDimension(dim);
        if (orbit == null) {
            if (retries < 100) {
                final int fDim = dim;
                final double fx = x, fy = y, fz = z;
                final NBTTagCompound snap = snapshot;
                final int next = retries + 1;
                com.orbitalsunworks.world.orbit.OrbitTransferScheduler
                    .scheduleTick(new Runnable() {
                        @Override
                        public void run() {
                            spawnCloneFromNBT(fDim, fx, fy, fz, snap, next);
                        }
                    });
            }
            return;
        }
        snapshot.setString("id", net.minecraft.entity.EntityList.getEntityString(
            new EntityNavRocket(orbit)));
        snapshot.setTag("Pos", newPosList(x, y, z));
        snapshot.setTag("Motion", newPosList(0, 0, 0));
        Entity clone = net.minecraft.entity.EntityList.createEntityFromNBT(snapshot, orbit);
        if (clone instanceof EntityNavRocket) {
            EntityNavRocket parked = (EntityNavRocket) clone;
            parked.setPosition(x, y, z);
            parked.noClip = true;
            int cx = net.minecraft.util.MathHelper.floor_double(x) >> 4;
            int cz = net.minecraft.util.MathHelper.floor_double(z) >> 4;
            orbit.getChunkProvider().loadChunk(cx, cz);
            orbit.spawnEntityInWorld(parked);
        }
    }

    /** Faz o clone AGORA (dimensão já carregada). */
    private void spawnCloneNow(WorldServer orbit, double x, double y, double z) {
        if (this.riddenByEntity != null) {
            this.riddenByEntity.mountEntity(null);
        }
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        tag.setString("id", net.minecraft.entity.EntityList.getEntityString(this));
        tag.setTag("Pos", this.newDoubleNBTList(x, y, z));
        tag.setTag("Motion", this.newDoubleNBTList(0, 0, 0));
        Entity clone = net.minecraft.entity.EntityList.createEntityFromNBT(tag, orbit);
        if (clone instanceof EntityNavRocket) {
            EntityNavRocket parked = (EntityNavRocket) clone;
            parked.setPosition(x, y, z);
            parked.noClip = true;
            // Garante o CHUNK carregado antes do spawn (o arrive roda
            // logo após o transferPlayerToDimension — o chunk da
            // estrutura pode ainda não estar pronto; sem isto o
            // spawnEntityInWorld falha silenciosamente).
            int cx = net.minecraft.util.MathHelper.floor_double(x) >> 4;
            int cz = net.minecraft.util.MathHelper.floor_double(z) >> 4;
            net.minecraft.world.chunk.Chunk chunk = orbit.getChunkProvider()
                .loadChunk(cx, cz);
            boolean spawned = orbit.spawnEntityInWorld(parked);
            if (!spawned) {
                // Chunk ainda não carregado (não deveria — o player está
                // lá); perde-se só o visual (o VOLTAR pelo satélite segue).
            }
        }
    }

    /** Lista NBT de posições (helper estático pro retry). */
    private static NBTTagList newPosList(double x, double y, double z) {
        NBTTagList list = new NBTTagList();
        list.appendTag(new net.minecraft.nbt.NBTTagDouble(x));
        list.appendTag(new net.minecraft.nbt.NBTTagDouble(y));
        list.appendTag(new net.minecraft.nbt.NBTTagDouble(z));
        return list;
    }

    /** Sincroniza o parkBaseY (int ×100) pro cliente via DataWatcher. */
    private void updateParkBaseYWatcher() {
        this.dataWatcher.updateObject(30, (int) (this.parkBaseY * 100));
    }

    /** parkBaseY do watcher (cliente). */
    private float parkBaseYFromWatcher() {
        return this.dataWatcher.getWatchableObjectInt(30) / 100f;
    }

    /** Y de referência do bob (flutuação) quando estacionada. */
    private float parkBaseY = 0f;

    /** v0.8.0: interagir com a NAVE ESTACIONADA abre o HUD de volta. */
    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (this.phase == PHASE_PARKED && !this.worldObj.isRemote
            && player instanceof EntityPlayerMP) {
            // Só o dono embarca de volta (regra do servidor).
            if (this.ownerUUID != null && !this.ownerUUID.equals(player.getUniqueID())) {
                OSUtils.sendChat(player, "§cEsta nave não é sua!");
                return true;
            }
            player.openGui(com.orbitalsunworks.OrbitalSunworks.instance,
                com.orbitalsunworks.handler.GuiHandler.ID_NAV_RETURN, this.worldObj,
                MathHelper.floor_double(this.posX),
                MathHelper.floor_double(this.posY),
                MathHelper.floor_double(this.posZ));
            return true;
        }
        return super.interactFirst(player);
    }

    /** A nave estacionada segue flutuando (bob suave) perto de onde
     *  chegou. Se o dono sair da órbita por outro meio (morte, etc), a
     *  nave se limpa. */
    private void updateParked() {
        // Servidor: autoridade do parkBaseY (watcher); cliente: lê.
        float baseY = this.worldObj.isRemote ? this.parkBaseYFromWatcher() : this.parkBaseY;
        // Bob senoidal ±0.25 no eixo Y.
        float bob = (float) Math.sin((this.ticksExisted + this.getEntityId()) * 0.05);
        this.setPosition(this.posX, baseY + bob * 0.25, this.posZ);
        // Gimbal leve (vida na órbita).
        this.rotationYaw = (this.ticksExisted * 0.5f) % 360f;

        if (this.worldObj.isRemote) {
            return;
        }
        // Luz de motor piscando (partícula de portal é cara; usa spark).
        if (this.ticksExisted % 8 == 0) {
            this.worldObj.spawnParticle("reddust",
                this.posX, this.posY - 0.2, this.posZ, 1.0, 0.75, 0.2);
        }
        // Dono sumiu da dimensão orbital? Espera 60s e se limpa (o item
        // já foi devolvido — nada se perde).
        // v0.10.0 FIX: o antigo `ticksExisted % 1200 == 0` era VERDADE no
        // TICK 0 — no spawn o dono ainda nem tinha chegado à dimensão e
        // a nave já se marcava como órfã (o bug do "aparece e some em
        // 2-3 segundos"). Agora: só checa a partir de 1200 ticks e
        // exige 60s CONSECUTIVOS sem o dono na dimensão.
        if (this.ticksExisted >= 1200 && this.ticksExisted % 1200 == 0) {
            EntityPlayerMP owner = this.ownerUUID != null
                ? PlayerLookup.byUUID(this.worldObj, this.ownerUUID) : null;
            if (owner == null || !OrbitDimensionManager.isOrbitWorld(owner.worldObj)) {
                this.orphanTicks += 1200;
                if (this.orphanTicks >= 1200) {
                    this.setDead();
                }
            } else {
                this.orphanTicks = 0;
            }
        }
    }

    /** Ticks consecutivos sem o dono na dimensão (orphan check v0.10.0). */
    private int orphanTicks = 0;

    /** Botão "voltar à Terra" da GUI da nave estacionada: teleporta o
     *  dono direto pro pad SEM animação (o usuário pediu assim) e repõe
     *  o BLOCO do foguete na plataforma, com o item intacto no chão se
     *  não couber no inventário.
     *  v0.9.0: teleporte AGENDADO (o handler de rede NUNCA transfere
     *  dimensão direto — crash do loop de entidades). */
    public static void sendParkedHome(EntityPlayerMP player, EntityNavRocket parked) {
        // Casa gravada na entidade (a mesma da viagem de ida).
        final int dim = parked.homeDim;
        final double x = parked.homeX, y = parked.homeY, z = parked.homeZ;
        final EntityNavRocket theRocket = parked;
        // Consome o item ANTES (o jogador decidiu voltar; se o inventário
        // não tiver item, o bloco não é reposto — nada duplica).
        final boolean hasItem = player.inventory.hasItem(
            com.orbitalsunworks.OSItems.itemNavRocket);
        parked.setDead();
        OrbitTransferScheduler.schedule(player, dim, x, y, z, new Runnable() {
            @Override
            public void run() {
                // Roda já no overworld, fora do loop de entidades.
                player.capabilities.isFlying = false; // fly off na Terra
                player.sendPlayerAbilities();
                MinecraftServer server = MinecraftServer.getServer();
                World home = server.worldServerForDimension(dim);
                boolean restored = false;
                if (home != null && hasItem) {
                    restored = restoreRocketBlock(home, x, y, z);
                }
                if (restored) {
                    consumeOneNavRocket(player);
                    OSUtils.sendChat(player,
                        "§aDe volta à Terra! §fSeu foguete está posicionado na plataforma.");
                } else {
                    OSUtils.sendChat(player,
                        "§aDe volta à Terra! §eSem plataforma livre — o foguete ficou no seu inventário.");
                }
            }
        });
    }

    /** Consome 1 Foguete de Navegação do inventário (devolve nada). */
    private static void consumeOneNavRocket(EntityPlayerMP player) {
        for (int i = 0; i < player.inventory.mainInventory.length; i++) {
            ItemStack st = player.inventory.mainInventory[i];
            if (st != null && st.getItem() == com.orbitalsunworks.OSItems.itemNavRocket) {
                st.stackSize--;
                if (st.stackSize <= 0) {
                    player.inventory.mainInventory[i] = null;
                }
                player.inventory.markDirty();
                return;
            }
        }
    }

    /** Reconstrói a coluna do foguete de navegação no pad mais próximo
     *  das coords de casa (padrão: centro do pad = home - 2.5 em X).
     *  @return true se a coluna foi colocada. */
    private static boolean restoreRocketBlock(World world, double hx, double hy, double hz) {
        // A casa é gravada como (rx + 2.5, ry, rz + 0.5) na colocação —
        // o centro do pad é (hx - 2, hy - 1, hz).
        int cx = MathHelper.floor_double(hx) - 2;
        int cy = MathHelper.floor_double(hy);
        int cz = MathHelper.floor_double(hz);
        // Procura o pad: a base do foguete fica 1 acima do centro do pad.
        int baseY = cy;
        boolean found = false;
        // Varre em volta procurando a plataforma (robustez).
        outer:
        for (int dy = -2; dy <= 1; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int px = cx + dx, py = cy + dy, pz = cz + dz;
                    if (world.getBlock(px, py, pz) == com.orbitalsunworks.OSBlocks.blockLaunchPlatform) {
                        // Centro de 3x3? Testa se é o pad completo.
                        if (com.orbitalsunworks.block.BlockLaunchPlatform
                            .isCompleteStructure(world, px, py, pz)) {
                            cx = px;
                            baseY = py + 1;
                            cz = pz;
                            found = true;
                            break outer;
                        }
                    }
                }
            }
        }
        if (!found) {
            // Sem plataforma (quebraram enquanto viajava?): posiciona no
            // chão puro na coordenada da casa.
            baseY = cy;
            cx = MathHelper.floor_double(hx);
            cz = MathHelper.floor_double(hz);
        }
        // Coloca a coluna SE o espaço estiver livre (não sobrescreve).
        boolean placed = false;
        for (int i = 0; i < 3; i++) {
            if (!world.isAirBlock(cx, baseY + i, cz)) {
                return false; // obstruído: não repõe (item fica com o dono)
            }
        }
        for (int i = 0; i < 3; i++) {
            world.setBlock(cx, baseY + i, cz,
                com.orbitalsunworks.OSBlocks.blockNavRocket, i, 3);
        }
        placed = true;
        // Tile com a casa gravada (pra poder viajar de novo).
        TileEntity te = world.getTileEntity(cx, baseY, cz);
        if (te instanceof TileNavRocket) {
            ((TileNavRocket) te).setHome(world.provider.dimensionId, cx + 2.5, baseY, cz + 0.5);
        }
        world.playSoundEffect(cx + 0.5, baseY + 1, cz + 0.5, "random.anvil_land", 0.5f, 1.2f);
        return placed;
    }

    /** Devolve o item do foguete de navegação pro inventário do rider. */
    private void returnItem(EntityPlayerMP rider) {
        if (this.itemReturned) {
            return;
        }
        this.itemReturned = true;
        ItemStack stack = new ItemStack(com.orbitalsunworks.OSItems.itemNavRocket);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("HomeDim", this.homeDim);
        tag.setDouble("HomeX", this.homeX);
        tag.setDouble("HomeY", this.homeY);
        tag.setDouble("HomeZ", this.homeZ);
        stack.setTagCompound(tag);
        if (!rider.inventory.addItemStackToInventory(stack)) {
            // Inventário cheio: dropa no chão (na órbita, aos pés).
            net.minecraft.entity.item.EntityItem drop = new net.minecraft.entity.item.EntityItem(
                rider.worldObj, rider.posX, rider.posY, rider.posZ, stack);
            rider.worldObj.spawnEntityInWorld(drop);
            OSUtils.sendChat(rider, "§eInventário cheio — o foguete caiu no chão!");
        }
    }

    /** Aborta: devolve à Terra na posição de origem (item junto).
     *  v0.9.0: teleporte AGENDADO (nunca dentro do tick de entidade). */
    private void returnHome(EntityPlayerMP rider) {
        this.returnItem(rider);
        final int dim = this.homeDim;
        final double x = this.homeX, y = this.homeY, z = this.homeZ;
        this.setDead();
        OrbitTransferScheduler.schedule(rider, dim, x, y, z, new Runnable() {
            @Override
            public void run() {
                rider.capabilities.isFlying = false;
                rider.sendPlayerAbilities();
            }
        });
    }

    /** Botão "voltar à Terra" na GUI da estrutura orbital. */
    public static void sendPlayerHome(EntityPlayerMP player) {
        int dim = 0;
        double x = player.posX, y = player.posY, z = player.posZ;
        // Tenta ler do item do foguete (coords de casa gravadas na chegada).
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() == com.orbitalsunworks.OSItems.itemNavRocket
            && held.hasTagCompound() && held.getTagCompound().hasKey("HomeX")) {
            NBTTagCompound tag = held.getTagCompound();
            dim = tag.getInteger("HomeDim");
            x = tag.getDouble("HomeX");
            y = tag.getDouble("HomeY");
            z = tag.getDouble("HomeZ");
        } else {
            // Fallback: procurar o item no inventário inteiro.
            for (int i = 0; i < player.inventory.mainInventory.length; i++) {
                ItemStack st = player.inventory.mainInventory[i];
                if (st != null && st.getItem() == com.orbitalsunworks.OSItems.itemNavRocket
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
        // Sem item com casa: spawn do overworld.
        if (x == player.posX && y == player.posY && z == player.posZ) {
            WorldServer overworld = player.mcServer.worldServerForDimension(0);
            net.minecraft.util.ChunkCoordinates spawn = overworld.getSpawnPoint();
            x = spawn.posX + 0.5;
            y = spawn.posY;
            z = spawn.posZ + 0.5;
            dim = 0;
        }
        // v0.9.0: teleporte AGENDADO (o handler de rede nunca transfere
        // direto — crash do loop de entidades).
        final EntityPlayerMP p = player;
        final int fDim = dim;
        final double fx = x, fy = y, fz = z;
        OrbitTransferScheduler.schedule(player, dim, x, y, z, new Runnable() {
            @Override
            public void run() {
                p.capabilities.isFlying = false; // fly off de volta na Terra
                p.sendPlayerAbilities();
            }
        });
    }

    private void spawnExhaust() {
        for (int i = 0; i < 6; i++) {
            double ang = this.rand.nextDouble() * Math.PI * 2;
            double r = this.rand.nextDouble() * 0.4;
            this.worldObj.spawnParticle("flame",
                this.posX + Math.cos(ang) * r, this.posY, this.posZ + Math.sin(ang) * r,
                0, -0.3, 0);
            this.worldObj.spawnParticle("largesmoke",
                this.posX + Math.cos(ang) * r, this.posY - 0.5, this.posZ + Math.sin(ang) * r,
                Math.cos(ang) * 0.3, -0.05, Math.sin(ang) * 0.3);
        }
    }

    public int phaseOf() {
        // v0.10.0: no cliente lê do watcher (sincronizado); no servidor
        // o campo é a autoridade.
        return this.worldObj.isRemote
            ? this.dataWatcher.getWatchableObjectInt(29) : this.phase;
    }

    // ------------------------------------------------------------------
    // Montaria: o jogador viaja DENTRO do foguete
    // ------------------------------------------------------------------

    @Override
    public double getMountedYOffset() {
        // Boneco sentado no cockpit (afundado — nada pra fora da nave).
        return 0.4;
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public void updateRiderPosition() {
        super.updateRiderPosition();
        if (this.riddenByEntity != null) {
            // Mantém o rider preso na posição do foguete (subindo junto).
            this.riddenByEntity.setPosition(
                this.posX, this.posY + this.getMountedYOffset(), this.posZ);
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        // v0.8.0: estacionada pode ser CLICADA (raytrace precisa bater
        // na hitbox para o interactFirst rodar). Em voo continua
        // atravessável (não empurra ninguém).
        return this.phase == PHASE_PARKED;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false; // indestrutível durante o voo
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    protected void entityInit() {
        // v0.10.0: phase (0..4) e parkBaseY (int ×100) via DataWatcher —
        // o CLIENTE precisa dos dois (o bug da nave "aparecer e sumir em
        // 2-3s": o cliente nunca soube que ela estava PARKED, rodava a
        // lógica de countdown sem piloto e matava a entidade sozinho).
        this.dataWatcher.addObject(29, Integer.valueOf(PHASE_COUNTDOWN));
        this.dataWatcher.addObject(30, Integer.valueOf(0));
    }

    /** Sincroniza a phase pro cliente (chamar em TODA mudança). */
    private void updatePhaseWatcher() {
        this.dataWatcher.updateObject(29, Integer.valueOf(this.phase));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        this.phase = tag.getInteger("Phase");
        this.countdown = tag.getInteger("Countdown");
        this.parkBaseY = tag.getFloat("ParkBaseY");
        this.updateParkBaseYWatcher();
        this.updatePhaseWatcher(); // v0.10.0: cliente sabendo a phase real
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
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        tag.setInteger("Phase", this.phase);
        tag.setInteger("Countdown", this.countdown);
        tag.setFloat("ParkBaseY", this.parkBaseY);
        this.updateParkBaseYWatcher();
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
    }
}
