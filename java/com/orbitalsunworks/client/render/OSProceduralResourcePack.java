package com.orbitalsunworks.client.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import com.orbitalsunworks.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Resource pack virtual injetado em código que fornece as texturas
 * melhoradas de itens, blocos e Terra sobre o resource pack padrão do mod.
 */
@SideOnly(Side.CLIENT)
public class OSProceduralResourcePack implements IResourcePack {

    private static OSProceduralResourcePack instance;

    private final Map<String, byte[]> resources = new HashMap<String, byte[]>();

    public OSProceduralResourcePack() {
        OSTextureGenerator.populate(this.resources);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void install() {
        if (instance != null) {
            return;
        }
        instance = new OSProceduralResourcePack();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) {
            return;
        }
        try {
            for (Field f : Minecraft.class.getDeclaredFields()) {
                if (List.class.isAssignableFrom(f.getType())) {
                    f.setAccessible(true);
                    Object val = f.get(mc);
                    if (val instanceof List) {
                        List list = (List) val;
                        if (!list.isEmpty() && list.get(0) instanceof IResourcePack) {
                            if (!list.contains(instance)) {
                                list.add(instance);
                            }
                            return;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public InputStream getInputStream(ResourceLocation loc) throws IOException {
        if (loc != null && Reference.MOD_ID.equals(loc.getResourceDomain())) {
            byte[] data = this.resources.get(loc.getResourcePath());
            if (data != null) {
                return new ByteArrayInputStream(data);
            }
        }
        throw new FileNotFoundException(loc != null ? loc.toString() : "null");
    }

    @Override
    public boolean resourceExists(ResourceLocation loc) {
        return loc != null
            && Reference.MOD_ID.equals(loc.getResourceDomain())
            && this.resources.containsKey(loc.getResourcePath());
    }

    @Override
    public Set getResourceDomains() {
        return Collections.singleton(Reference.MOD_ID);
    }

    @Override
    public IMetadataSection getPackMetadata(IMetadataSerializer serializer, String sectionName) throws IOException {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException {
        return null;
    }

    @Override
    public String getPackName() {
        return "OrbitalSunworksProceduralTextures";
    }
}