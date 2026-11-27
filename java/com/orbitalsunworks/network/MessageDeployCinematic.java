package com.orbitalsunworks.network;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Pacote S->C: dispara a CINEMÁTICA DE DEPLOY no cliente do jogador que
 * lançou o foguete (só ele recebe).
 *
 * O que a animação mostra (GuiDeployCinematic):
 *  1. tela escurece;
 *  2. a cápsula lançadora abre (as duas metades vão para trás);
 *  3. o satélite sai de dentro;
 *  4. os painéis solares desdobram até o formato final (foto do usuário:
 *     corpo central + painéis brancos nas laterais + antena);
 *  5. a cápsula fica para trás e some.
 *
 * O satélite de verdade (estrutura) já foi registrado pelo manager e a
 * estrutura na dimensão orbital já foi spawnada; a cinematic é só o
 * "filme" para o dono.
 */
public class MessageDeployCinematic implements IMessage {

    private String satId;
    private String satName;
    private int satTier;
    private float satHealth;
    private boolean debrisShield;

    public MessageDeployCinematic() {
    }

    public MessageDeployCinematic(UUID id, String name, int tier, float health, boolean shield) {
        this.satId = id.toString();
        this.satName = name;
        this.satTier = tier;
        this.satHealth = health;
        this.debrisShield = shield;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int len = buf.readInt();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(buf.readChar());
        }
        this.satId = sb.toString();
        len = buf.readInt();
        sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(buf.readChar());
        }
        this.satName = sb.toString();
        this.satTier = buf.readInt();
        this.satHealth = buf.readFloat();
        this.debrisShield = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.satId.length());
        for (char c : this.satId.toCharArray()) {
            buf.writeChar(c);
        }
        buf.writeInt(this.satName.length());
        for (char c : this.satName.toCharArray()) {
            buf.writeChar(c);
        }
        buf.writeInt(this.satTier);
        buf.writeFloat(this.satHealth);
        buf.writeBoolean(this.debrisShield);
    }

    public UUID getSatId() {
        return UUID.fromString(this.satId);
    }

    public String getSatName() {
        return this.satName;
    }

    public int getSatTier() {
        return this.satTier;
    }

    public float getSatHealth() {
        return this.satHealth;
    }

    public boolean hasDebrisShield() {
        return this.debrisShield;
    }

    public static class Handler implements IMessageHandler<MessageDeployCinematic, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(MessageDeployCinematic message, MessageContext ctx) {
            // v0.10.0: SEM GUI cinematic (o usuário detonou a antiga —
            // "porquíssima"). Agora é IN-WORLD: um toast de missão
            // estiloso + som, e o jogador CONTINUA JOGANDO (o satélite
            // real está na órbita; a "cinemática" é ver a estrutura).
            com.orbitalsunworks.client.handler.OrbitDeployToast.show(
                message.getSatName(), message.getSatTier(),
                message.getSatHealth());
            return null;
        }
    }
}
