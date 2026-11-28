package com.orbitalsunworks.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.orbitalsunworks.client.gui.GuiControlRoom;
import com.orbitalsunworks.client.gui.GuiCraftingStation;
import com.orbitalsunworks.client.gui.GuiDispersionRocket;
import com.orbitalsunworks.client.gui.GuiManual;
import com.orbitalsunworks.client.gui.GuiNavRocket;
import com.orbitalsunworks.client.gui.GuiNavSelect;
import com.orbitalsunworks.client.gui.GuiOilExtractor;
import com.orbitalsunworks.client.gui.GuiOilRefiner;
import com.orbitalsunworks.client.gui.GuiOrbitSatellite;
import com.orbitalsunworks.client.gui.GuiReturnHome;
import com.orbitalsunworks.client.gui.GuiRocket;
import com.orbitalsunworks.client.gui.GuiSynthesizer;
import com.orbitalsunworks.client.gui.GuiTelescope;
import com.orbitalsunworks.craftingstation.ContainerCraftingStation;
import com.orbitalsunworks.craftingstation.TileCraftingStation;
import com.orbitalsunworks.machine.ContainerOilExtractor;
import com.orbitalsunworks.machine.ContainerOilRefiner;
import com.orbitalsunworks.machine.ContainerSynthesizer;
import com.orbitalsunworks.machine.TileOilExtractor;
import com.orbitalsunworks.machine.TileOilRefiner;
import com.orbitalsunworks.machine.TileSynthesizer;
import com.orbitalsunworks.rocket.ContainerDispersionRocket;
import com.orbitalsunworks.rocket.ContainerNavRocket;
import com.orbitalsunworks.rocket.ContainerReturnHome;
import com.orbitalsunworks.rocket.ContainerRocket;
import com.orbitalsunworks.rocket.TileDispersionRocket;
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
    public static final int ID_DISPERSION_ROCKET = 8;
    public static final int ID_CONTROL_ROOM = 9;
    public static final int ID_TELESCOPE = 10;
    public static final int ID_OIL_EXTRACTOR = 11;
    public static final int ID_OIL_REFINER = 12;
    public static final int ID_SYNTHESIZER = 13;

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
            case ID_NAV_SELECT:
            case ID_CONTROL_ROOM:
            case ID_TELESCOPE:
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
            case ID_DISPERSION_ROCKET:
                if (te instanceof TileDispersionRocket) {
                    return new ContainerDispersionRocket(player.inventory, (TileDispersionRocket) te);
                }
                return null;
            case ID_OIL_EXTRACTOR:
                if (te instanceof TileOilExtractor) {
                    return new ContainerOilExtractor(player.inventory, (TileOilExtractor) te);
                }
                return null;
            case ID_OIL_REFINER:
                if (te instanceof TileOilRefiner) {
                    return new ContainerOilRefiner(player.inventory, (TileOilRefiner) te);
                }
                return null;
            case ID_SYNTHESIZER:
                if (te instanceof TileSynthesizer) {
                    return new ContainerSynthesizer(player.inventory, (TileSynthesizer) te);
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
            case ID_DISPERSION_ROCKET:
                if (te instanceof TileDispersionRocket) {
                    return new GuiDispersionRocket(player.inventory, (TileDispersionRocket) te);
                }
                return null;
            case ID_CONTROL_ROOM:
                return new GuiControlRoom(x, y, z);
            case ID_TELESCOPE:
                return new GuiTelescope(x, y, z);
            case ID_OIL_EXTRACTOR:
                if (te instanceof TileOilExtractor) {
                    return new GuiOilExtractor(player.inventory, (TileOilExtractor) te);
                }
                return null;
            case ID_OIL_REFINER:
                if (te instanceof TileOilRefiner) {
                    return new GuiOilRefiner(player.inventory, (TileOilRefiner) te);
                }
                return null;
            case ID_SYNTHESIZER:
                if (te instanceof TileSynthesizer) {
                    return new GuiSynthesizer(player.inventory, (TileSynthesizer) te);
                }
                return null;
            default:
                return null;
        }
    }
}