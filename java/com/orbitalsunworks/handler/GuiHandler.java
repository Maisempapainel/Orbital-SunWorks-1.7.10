package com.orbitalsunworks.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.client.gui.GuiCraftingStation;
import com.orbitalsunworks.client.gui.GuiManual;
import com.orbitalsunworks.client.gui.GuiNavRocket;
import com.orbitalsunworks.client.gui.GuiNavSelect;
import com.orbitalsunworks.client.gui.GuiOrbitSatellite;
import com.orbitalsunworks.client.gui.GuiReturnHome;
import com.orbitalsunworks.client.gui.GuiRocket;
import com.orbitalsunworks.craftingstation.ContainerCraftingStation;
import com.orbitalsunworks.craftingstation.TileCraftingStation;
import com.orbitalsunworks.rocket.ContainerNavRocket;
import com.orbitalsunworks.rocket.ContainerReturnHome;
import com.orbitalsunworks.rocket.ContainerRocket;
import com.orbitalsunworks.rocket.TileNavRocket;
import com.orbitalsunworks.rocket.TileRocket;
import com.orbitalsunworks.satellite.ContainerOrbitSatellite;
import com.orbitalsunworks.satellite.TileOrbitSatellite;

import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

    public static final int ID_CRAFTING_STATION = 1;
    public static final int ID_ROCKET = 2;
    public static final int ID_MANUAL = 3;
    public static final int ID_NAV_SELECT = 4;
    public static final int ID_ORBIT_SATELLITE = 5;
    public static final int ID_NAV_ROCKET = 6;
    public static final int ID_NAV_RETURN = 7;

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        switch (id) {
            case ID_CRAFTING_STATION:
                if (te instanceof TileCraftingStation) {
                    return new ContainerCraftingStation(player.inventory, (TileCraftingStation) te);
                }
                return null;
            case ID_ROCKET:
                if (te instanceof TileRocket) {
                    return new ContainerRocket(player.inventory, (TileRocket) te);
                }
                return null;
            case ID_MANUAL:
                return null;
            case ID_NAV_SELECT:
                return null;
            case ID_NAV_ROCKET:
                if (te instanceof TileNavRocket) {
                    return new ContainerNavRocket(player.inventory, (TileNavRocket) te);
                }
                return null;
            case ID_ORBIT_SATELLITE:
                if (te instanceof TileOrbitSatellite) {
                    return new ContainerOrbitSatellite(player.inventory, (TileOrbitSatellite) te);
                }
                return null;
            case ID_NAV_RETURN:
                if (te instanceof TileNavRocket) {
                    return new ContainerReturnHome(player.inventory, (TileNavRocket) te);
                }
                return null;
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
                return new GuiManual();
            case ID_NAV_SELECT:
                return new GuiNavSelect(x, y, z);
            case ID_NAV_ROCKET:
                if (te instanceof TileNavRocket) {
                    return new GuiNavRocket(player.inventory, (TileNavRocket) te);
                }
                return null;
            case ID_ORBIT_SATELLITE:
                if (te instanceof TileOrbitSatellite) {
                    return new GuiOrbitSatellite(player.inventory, (TileOrbitSatellite) te);
                }
                return null;
            case ID_NAV_RETURN:
                if (te instanceof TileNavRocket) {
                    return new GuiReturnHome(player.inventory, (TileNavRocket) te);
                }
                return null;
            default:
                return null;
        }
    }
}