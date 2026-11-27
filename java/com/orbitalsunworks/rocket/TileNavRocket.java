package com.orbitalsunworks.rocket;

import net.minecraft.tileentity.TileEntity;

/**
 * Tile da BASE do foguete de navegação (v0.7.0).
 *
 * Não tem inventário: a GUI do bloco (ID_NAV_ROCKET) abre direto o mapa
 * orbital (lista de satélites do dono → embarque). Este tile existe para:
 *  - o multiblock ter base (padrão do BlockRocket);
 *  - guardar a "casa" (dim/coords de onde o foguete está) — usada como
 *    retorno da órbita quando o jogador aperta VOLTAR À TERRA.
 */
public class TileNavRocket extends TileEntity {

    /** Coordenadas do pad (onde voltar quando a viagem acabar). */
    public int homeDim;
    public double homeX, homeY, homeZ;
    public boolean homeSet = false;

    public void setHome(int dim, double x, double y, double z) {
        this.homeDim = dim;
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
        this.homeSet = true;
    }

    @Override
    public void readFromNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.homeSet = tag.getBoolean("HomeSet");
        if (this.homeSet) {
            this.homeDim = tag.getInteger("HomeDim");
            this.homeX = tag.getDouble("HomeX");
            this.homeY = tag.getDouble("HomeY");
            this.homeZ = tag.getDouble("HomeZ");
        }
    }

    @Override
    public void writeToNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setBoolean("HomeSet", this.homeSet);
        if (this.homeSet) {
            tag.setInteger("HomeDim", this.homeDim);
            tag.setDouble("HomeX", this.homeX);
            tag.setDouble("HomeY", this.homeY);
            tag.setDouble("HomeZ", this.homeZ);
        }
    }
}
