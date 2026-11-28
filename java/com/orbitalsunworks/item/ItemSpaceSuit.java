package com.orbitalsunworks.item;

import java.util.List;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;

import com.orbitalsunworks.OSCreativeTab;
import com.orbitalsunworks.Reference;
import com.orbitalsunworks.client.render.ModelSpaceSuit;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Peças do Traje Espacial Pressurizado EVA (Capacete, Peitoral, Calça, Botas).
 * Usa renderização 3D customizada em ModelSpaceSuit.
 */
public class ItemSpaceSuit extends ItemArmor {

    public static final ArmorMaterial SPACE_SUIT_MATERIAL = EnumHelper.addArmorMaterial(
        "ORBITAL_SPACE_SUIT", 36, new int[] { 3, 7, 5, 3 }, 12);

    public ItemSpaceSuit(int armorType, String name) {
        super(SPACE_SUIT_MATERIAL, 0, armorType);
        this.setUnlocalizedName(name);
        this.setTextureName(Reference.RESOURCE_PREFIX + name);
        this.setCreativeTab(OSCreativeTab.INSTANCE);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
        if (slot == 2) {
            return Reference.RESOURCE_PREFIX + "textures/models/space_suit_layer_2.png";
        }
        return Reference.RESOURCE_PREFIX + "textures/models/space_suit_layer_1.png";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {
        return ModelSpaceSuit.getModelForSlot(armorSlot, entityLiving);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
        list.add("\u00a7bTraje Pressurizado EVA (Set Completo)");
        list.add("\u00a77- Necess\u00e1rio para decolar na nave espacial");
        list.add("\u00a77- Impede sufocamento e libera voo na \u00d3rbita");
    }
}