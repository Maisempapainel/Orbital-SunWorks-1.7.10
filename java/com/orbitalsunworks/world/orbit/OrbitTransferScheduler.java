package com.orbitalsunworks.world.orbit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

/**
 * AGENDADOR DE TRANSFERÊNCIAS DE DIMENSÃO (v0.9.0 — correção do CRASH).
 *
 * O CRASH reportado (IndexOutOfBoundsException: Index: 184, Size: 184 em
 * World.updateEntities) acontece porque:
 *  1. o arrive() do foguete roda DENTRO do loop updateEntities (que
 *     itera loadedEntityList por ÍNDICE);
 *  2. o transferPlayerToDimension remove o player da lista NO MEIO da
 *     iteração (removePlayerEntityDangerously → loadedEntityList.remove);
 *  3. o índice do loop fica maior que a lista → IndexOutOfBounds → o
 *     servidor morre ("Exception ticking world entities").
 *
 * A correção: NINGUÉM transfere dimensão de dentro de um tick de
 * entidade. Quem precisa transferir AGENDA aqui; o teleporte roda no
 * INÍCIO do próximo ServerTickEvent — fora do loop de entidades, que é
 * como o vanilla faz com o /tp entre dimensões por comando.
 *
 * Também usa um TELEPORTER NO-OP: o default do vanilla procuraria/criaria
 * um PORTAL DE OBSIDIANA na órbita (nada a ver com o mod).
 */
public final class OrbitTransferScheduler {

    /** Teleporter que NÃO cria portal: só posiciona nas coords dadas. */
    public static class NoPortalTeleporter extends Teleporter {

        private final WorldServer world;

        public NoPortalTeleporter(WorldServer world) {
            super(world);
            this.world = world;
        }

        @Override
        public void placeInPortal(Entity entity, double x, double y, double z, float yaw) {
            // Sem portal: só garante o chão (void world não tem nada
            // embaixo — o fly já é ativado pelo OrbitFlyHandler).
            entity.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, yaw, 0f);
        }

        @Override
        public boolean placeInExistingPortal(Entity entity, double x, double y, double z, float yaw) {
            return false; // nunca há portal
        }

        @Override
        public boolean makePortal(Entity entity) {
            return false; // NUNCA cria portal de obsidiana
        }

        @Override
        public void removeStalePortalLocations(long timer) {
            // nada
        }
    }

    /** Um pedido de transferência agendado. */
    private static class Request {
        final EntityPlayerMP player;
        final int targetDim;
        final double x, y, z;
        final float yaw, pitch;
        final Runnable after; // roda DEPOIS de chegar (fly, chat, etc)

        Request(EntityPlayerMP p, int dim, double x, double y, double z,
                float yaw, float pitch, Runnable after) {
            this.player = p;
            this.targetDim = dim;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.after = after;
        }
    }

    private static final List<Request> PENDING = new ArrayList<Request>();
    /** v0.10.0: runnables soltos pro próximo tick (retries de spawn etc). */
    private static final List<Runnable> NEXT_TICK = new ArrayList<Runnable>();
    private static boolean registered = false;

    private OrbitTransferScheduler() {
    }

    /** Registra o handler no bus (uma vez, pelo OrbitDimensionManager). */
    public static void register() {
        if (!registered) {
            cpw.mods.fml.common.FMLCommonHandler.instance().bus()
                .register(new OrbitTransferScheduler());
            registered = true;
        }
    }

    /**
     * Agenda a transferência do jogador para a dimensão alvo nas coords
     * dadas. Seguro de chamar DE DENTRO de qualquer tick de entidade ou
     * handler de rede — o teleporte roda no início do próximo tick.
     */
    public static void schedule(EntityPlayerMP player, int targetDim,
            double x, double y, double z, Runnable after) {
        schedule(player, targetDim, x, y, z, 0f, 0f, after);
    }

    public static void schedule(EntityPlayerMP player, int targetDim,
            double x, double y, double z, float yaw, float pitch, Runnable after) {
        if (player == null || player.isDead) {
            return;
        }
        synchronized (PENDING) {
            // Um pedido por jogador (spam de pacote não empilha).
            Iterator<Request> it = PENDING.iterator();
            while (it.hasNext()) {
                if (it.next().player == player) {
                    it.remove();
                }
            }
            PENDING.add(new Request(player, targetDim, x, y, z, yaw, pitch, after));
        }
    }

    /** Executa as transferências pendentes (início do tick do servidor). */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.side != Side.SERVER) {
            return;
        }
        // v0.10.0: runnables soltos primeiro (retries etc).
        List<Runnable> ticks = null;
        synchronized (NEXT_TICK) {
            if (!NEXT_TICK.isEmpty()) {
                ticks = new ArrayList<Runnable>(NEXT_TICK);
                NEXT_TICK.clear();
            }
        }
        if (ticks != null) {
            for (Runnable r : ticks) {
                try {
                    r.run();
                } catch (Exception e) {
                    System.out.println("[OS] scheduleTick: " + e);
                }
            }
        }
        List<Request> toRun = null;
        synchronized (PENDING) {
            if (PENDING.isEmpty()) {
                return;
            }
            toRun = new ArrayList<Request>(PENDING);
            PENDING.clear();
        }
        for (Request r : toRun) {
            this.transferNow(r);
        }
    }

    /** v0.10.0: roda um Runnable no início do PRÓXIMO tick do servidor
     *  (fora do loop de entidades). Usado por retries de spawn etc. */
    public static void scheduleTick(Runnable r) {
        synchronized (NEXT_TICK) {
            NEXT_TICK.add(r);
        }
    }

    /** A transferência de verdade — FORA do loop de entidades. */
    private void transferNow(Request r) {
        EntityPlayerMP player = r.player;
        if (player.isDead) {
            return;
        }
        MinecraftServer server = MinecraftServer.getServer();
        if (server == null) {
            return;
        }
        try {
            // Desmonta ANTES (transfer com mount é problemático).
            if (player.ridingEntity != null) {
                player.mountEntity(null);
            }
            if (player.dimension != r.targetDim) {
                WorldServer target = server.worldServerForDimension(r.targetDim);
                if (target != null) {
                    // Posiciona ANTES: o transfer usa as coords atuais.
                    player.setLocationAndAngles(r.x, r.y, r.z, r.yaw, r.pitch);
                    server.getConfigurationManager().transferPlayerToDimension(
                        player, r.targetDim, new NoPortalTeleporter(target));
                }
            }
            player.setPositionAndUpdate(r.x, r.y, r.z);
            player.playerNetServerHandler.setPlayerLocation(
                r.x, r.y, r.z, r.yaw, r.pitch);
            if (r.after != null) {
                r.after.run();
            }
        } catch (Exception e) {
            // Nunca derruba o servidor por causa de um teleporte.
            System.out.println("[OS] OrbitTransferScheduler: falha no teleporte de "
                + player.getCommandSenderName() + ": " + e);
        }
    }
}
