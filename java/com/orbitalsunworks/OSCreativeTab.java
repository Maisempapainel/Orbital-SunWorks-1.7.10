package com.orbitalsunworks;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Aba criativa do Orbital Sunworks na guia de inventário.
 *
 * AULA APRENDIDA (antenas fora de ordem): a GUI criativa lista os itens na
 * ordem do ITERADOR do registry, que segue a lista de IDs NUMÉRICOS
 * (RegistryNamespaced -> ObjectIntIdentityMap) — e o Forge atribui esses
 * IDs numa ordem que NÃO é a ordem de registro das chamadas. Por isso as
 * antenas apareciam 3,2,1,5,4. O sort "estável por mod" da v0.5.1 não
 * resolveu porque ele PRESERVA a ordem de ID entre os nossos itens.
 *
 * SOLUÇÃO: RANK EXPLÍCITO. Depois que o vanilla preenche a lista, o
 * displayAllReleventItems ordena por um rank determinado pelo NOME DE
 * REGISTRO do item (determinístico entre mundos/saves). Antenas ficam
 * T1..T5 SEQUENCIAIS, sempre. Itens sem rank vão depois, estáveis.
 */
public class OSCreativeTab extends CreativeTabs {

    public static final OSCreativeTab INSTANCE = new OSCreativeTab();

    private OSCreativeTab() {
        super(Reference.MOD_ID);
    }

    @Override
    public Item getTabIconItem() {
        return OSItems.ingotRedmeg != null ? OSItems.ingotRedmeg : Item.getItemFromBlock(OSBlocks.blockCladded);
    }

    /**
     * Rank de ordenação (menor = antes na aba). Leitura pensada pro
     * criativo: lingotes da progressão -> ligas -> componentes -> antenas
     * T1..T5 em sequência -> módulos -> foguete -> manual no fim.
     */
    private static int itemRank(String name) {
        // Lingotes base (progressão do mod)
        if (name.equals("ingotRedmeg")) return 0;
        if (name.equals("ingotYellomite")) return 1;
        if (name.equals("ingotCopper")) return 2;
        if (name.equals("ingotTin")) return 3;
        if (name.equals("ingotAluminium")) return 4;
        if (name.equals("ingotTitanium")) return 5;
        if (name.equals("ingotSteel")) return 6;
        // Ligas
        if (name.equals("ingotAluminiumAlloy")) return 7;
        if (name.equals("ingotTitaniumAlloy")) return 8;
        // Componentes
        if (name.equals("energizedCoal")) return 9;
        if (name.equals("silicon")) return 10;
        if (name.equals("carbonFiber")) return 11;
        if (name.equals("circuit")) return 12;
        if (name.equals("controlModule")) return 13;
        if (name.equals("solarPanelT1")) return 14;
        // Antenas T1..T5 SEQUENCIAIS (o bug reportado)
        if (name.startsWith("antennaT")) {
            return 20 + Integer.parseInt(name.substring(8));
        }
        // Módulos
        if (name.equals("moduleDebrisShield")) return 30;
        // Foguete
        if (name.equals("rocketT1")) return 31;
        // Manual por último
        if (name.equals("manual")) return 40;
        // ItemBlocks e outros: depois dos ranqueados.
        return 50;
    }

    @Override
    @cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
    public void displayAllReleventItems(List items) {
        super.displayAllReleventItems(items);
        // Sort estável por rank: antenas T1..T5 em sequência, sempre,
        // independentemente da ordem dos IDs numéricos do registry.
        Collections.sort(items, new Comparator() {
            @Override
            public int compare(Object a, Object b) {
                return Integer.valueOf(rankOf(a)).compareTo(Integer.valueOf(rankOf(b)));
            }
        });
    }

    /** Rank de um ItemStack da lista (não-ItemStack vai pro fim). */
    private static int rankOf(Object o) {
        if (!(o instanceof ItemStack)) {
            return 99;
        }
        ItemStack stack = (ItemStack) o;
        if (stack == null || stack.getItem() == null) {
            return 99;
        }
        String name = cpw.mods.fml.common.registry.GameData.getItemRegistry()
            .getNameForObject(stack.getItem());
        if (name == null) {
            return 99;
        }
        // Nome vem "modid:nome" — descarta o prefixo.
        int colon = name.indexOf(':');
        if (colon >= 0) {
            name = name.substring(colon + 1);
        }
        return itemRank(name);
    }
}
