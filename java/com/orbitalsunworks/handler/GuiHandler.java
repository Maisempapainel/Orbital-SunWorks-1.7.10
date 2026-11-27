package com.orbitalsunworks.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.client.gui.GuiCraftingStation;
import com.orbitalsunworks.client.gui.GuiRocket;
import com.orbitalsunworks.craftingstation.TileCraftingStation;
import com.orbitalsunworks.rocket.TileRocket;

import cpw.mods.fml.common.network.IGuiHandler;

/**
 * Registro das GUIs do mod. IDs:
 * - 1: Estação de Crafting Orbital
 * - 2: Foguete (módulos + lançamento)
 * - 3: Manual
 * - 4: Seleção de satélite (foguete de navegação)
 * - 5: Estrutura do satélite na órbita (upgrades/reparos)
 */
public class GuiHandler implements IGuiHandler {

    public static final int ID_CRAFTING_STATION = 1;
    public static final int ID_ROCKET = 2;
    public static final int ID_MANUAL = 3;
    public static final int ID_NAV_SELECT = 4;
    public static final int ID_ORBIT_SATELLITE = 5;
    public static final int ID_NAV_ROCKET = 6;
    /** v0.8.0: HUD da nave estacionada na órbita (voltar à Terra). */
    public static final int ID_NAV_RETURN = 7;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        switch (id) {
            case ID_CRAFTING_STATION:
                if (te instanceof TileCraftingStation) {
                    return new com.orbitalsunworks.craftingstation.ContainerCraftingStation(player.inventory, (TileCraftingStation) te);
                }
                return null;
            case ID_ROCKET:
                if (te instanceof TileRocket) {
                    return new com.orbitalsunworks.rocket.ContainerRocket(player.inventory, (TileRocket) te);
                }
                return null;
            case ID_MANUAL:
                return null; // GUI de leitura, sem container
            case ID_NAV_SELECT:
                return null; // GUI de seleção, sem container (dados via rede)
            case ID_NAV_ROCKET:
                // v0.8.0: Container OBRIGATÓRIO no servidor — se retornar
                // null o FML 1.7.10 nem abre a GUI no cliente (bug do
                // "clique não faz nada").
                if (te instanceof com.orbitalsunworks.rocket.TileNavRocket) {
                    return new com.orbitalsunworks.rocket.ContainerNavRocket(
                        player.inventory, (com.orbitalsunworks.rocket.TileNavRocket) te);
                }
                return null;
            case ID_ORBIT_SATELLITE:
                if (te instanceof com.orbitalsunworks.satellite.TileOrbitSatellite) {
                    return new com.orbitalsunworks.satellite.ContainerOrbitSatellite(
                        player.inventory, (com.orbitalsunworks.satellite.TileOrbitSatellite) te);
                }
                return null;
            case ID_NAV_RETURN:
                // Container dummy (a GUI de retorno não tem slots, mas o
                // FML 1.7.10 exige não-null).
                return new com.orbitalsunworks.rocket.ContainerReturnHome(player.inventory);
            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        switch (id) {
            case ID_CRAFTING_STATION:
                if (te instanceof TileCraftingStation) {
                    return new GuiCraftingStation(player.inventory, (TileCraftingStation) te);
                }
                return null;
            case ID_ROCKET:
                if (te instanceof TileRocket) {
                    return new GuiRocket(player.inventory, (TileRocket) te);
                }
                return null;
            case ID_MANUAL:
                return new com.orbitalsunworks.client.gui.GuiManual();
            case ID_NAV_SELECT:
                return new com.orbitalsunworks.client.gui.GuiNavSelect(x, y, z);
            case ID_NAV_ROCKET:
                // Mapa orbital do BLOCO do foguete de navegação. O cliente
                // recebe o Container dummy do servidor e desenha por cima.
                if (te instanceof com.orbitalsunworks.rocket.TileNavRocket) {
                    return new com.orbitalsunworks.client.gui.GuiNavRocket(
                        player.inventory, (com.orbitalsunworks.rocket.TileNavRocket) te);
                }
                return null;
            case ID_ORBIT_SATELLITE:
                if (te instanceof com.orbitalsunworks.satellite.TileOrbitSatellite) {
                    return new com.orbitalsunworks.client.gui.GuiOrbitSatellite(
                        player.inventory, (com.orbitalsunworks.satellite.TileOrbitSatellite) te);
                }
                return null;
            case ID_NAV_RETURN:
                // Acha a NAVE ESTACIONADA perto das coords passadas.
                com.orbitalsunworks.rocket.EntityNavRocket parked =
                    findParkedRocket(world, x, y, z);
                if (parked != null) {
                    return new com.orbitalsunworks.client.gui.GuiReturnHome(
                        player.inventory, parked);
                }
                return null;
            default:
                return null;
        }
    }

    /** Acha a nave estacionada mais próxima das coords (raio 4 blocos). */
    private com.orbitalsunworks.rocket.EntityNavRocket findParkedRocket(
            World world, int x, int y, int z) {
        double best = 16.0; // 4 blocos ao quadrado
        com.orbitalsunworks.rocket.EntityNavRocket bestRocket = null;
        for (Object o : world.loadedEntityList) {
            if (o instanceof com.orbitalsunworks.rocket.EntityNavRocket) {
                com.orbitalsunworks.rocket.EntityNavRocket r =
                    (com.orbitalsunworks.rocket.EntityNavRocket) o;
                if (r.phaseOf() != com.orbitalsunworks.rocket.EntityNavRocket.PHASE_PARKED) {
                    continue;
                }
                double d = (r.posX - x) * (r.posX - x) + (r.posY - y) * (r.posY - y)
                    + (r.posZ - z) * (r.posZ - z);
                if (d < best) {
                    best = d;
                    bestRocket = r;
                }
            }
        }
        return bestRocket;
    }
}
