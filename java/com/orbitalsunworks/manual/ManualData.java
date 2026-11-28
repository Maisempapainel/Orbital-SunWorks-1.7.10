package com.orbitalsunworks.manual;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.orbitalsunworks.OSBlocks;
import com.orbitalsunworks.OSItems;

/**
 * Dados completos do Manual do Orbital Sunworks divididos em 3 sessões:
 *  - SEC_TUTORIAL
 *  - SEC_CRAFTINGS
 *  - SEC_MULTIBLOCKS
 */
public class ManualData {

    public static final int SEC_TUTORIAL = 0;
    public static final int SEC_CRAFTINGS = 1;
    public static final int SEC_MULTIBLOCKS = 2;

    public static final int TYPE_GUIDE = 0;
    public static final int TYPE_FURNACE = 1;
    public static final int TYPE_VANILLA = 2;
    public static final int TYPE_STATION = 3;
    public static final int TYPE_MULTIBLOCK = 4;

    public static final int MB_PLATFORM_7X7 = 1;
    public static final int MB_ROCKET_T1 = 2;
    public static final int MB_NAV_ROCKET = 3;
    public static final int MB_ORBIT_SAT = 4;

    public static class Page {
        public final int section;
        public final int type;
        public final String title;
        public final String badge;
        public final String description;
        public final ItemStack[] grid = new ItemStack[15];
        public ItemStack result;
        public int resultCount = 1;
        public int craftSeconds = 0;
        public boolean shapeless = false;
        public int multiblockId = 0;

        public Page(int section, int type, String title, String badge, String description) {
            this.section = section;
            this.type = type;
            this.title = title;
            this.badge = badge;
            this.description = description;
        }
    }

    public static List<Page> buildSection(int section) {
        if (section == SEC_TUTORIAL) {
            return buildTutorialPages();
        }
        if (section == SEC_MULTIBLOCKS) {
            return buildMultiblockPages();
        }
        return buildCraftingPages();
    }

    // ======================================================================
    // SESSÃO 1: TUTORIAL
    // ======================================================================
    private static List<Page> buildTutorialPages() {
        List<Page> list = new ArrayList<Page>();

        list.add(guide("Vis\u00e3o Geral & Min\u00e9rios", "PROGRESS\u00c3O",
            new ItemStack(OSBlocks.oreRedmeg),
            "Bem-vindo ao Orbital Sunworks! Gere energia solar espacial lan\u00e7ando sat\u00e9lites em \u00f3rbita.\n\n"
            + "Camadas dos min\u00e9rios (picareta de ferro+):\n"
            + "- Cobre (Y 40-70) e Estanho (Y 30-60)\n"
            + "- Alum\u00ednio (Y 20-50) e Sil\u00edcio (Y 10-60)\n"
            + "- Carv\u00e3o Energizado (Y 10-45, dropa 2x)\n"
            + "- Tit\u00e2nio (Y 5-25), Redmeg (5-30), Yellomite (5-35)"));

        list.add(guide("Esta\u00e7\u00e3o de Crafting", "BLOCO 3x5",
            new ItemStack(OSBlocks.blockCraftingStation),
            "A Esta\u00e7\u00e3o Orbital possui grade 3x5 para construir ligas, componentes, m\u00f3dulos, foguetes e a Esta\u00e7\u00e3o de Controle.\n\n"
            + "1. Monte os itens na grade.\n"
            + "2. Clique no slot de RESULTADO para iniciar (3s, 6s ou 25s).\n"
            + "3. A fabrica\u00e7\u00e3o continua mesmo com a janela fechada!"));

        list.add(guide("Plataforma de Lan\u00e7amento", "MULTIBLOCK",
            new ItemStack(OSBlocks.blockLaunchPlatform),
            "Base obrigat\u00f3ria 7x7 (49 blocos no mesmo n\u00edvel) para posicionar e lan\u00e7ar qualquer foguete.\n\n"
            + "Deixe 1 bloco livre ao redor das bordas. Quando o centro destacar, clique segurando o Foguete Orbital, de Navega\u00e7\u00e3o ou Dispersor."));

        list.add(guide("Foguete Tier 1", "SAT\u00c9LITE",
            new ItemStack(OSItems.itemRocketT1),
            "Coloque no centro da plataforma 7x7 e clique nele para abrir o painel de voo.\n\n"
            + "- Instale at\u00e9 2 m\u00f3dulos antes do lan\u00e7amento.\n"
            + "- Mantenha o c\u00e9u vertical 100% livre.\n"
            + "- Gera at\u00e9 1.00M RF/t em \u00f3rbita (mais com m\u00f3dulos de efici\u00eancia)!"));

        list.add(guide("Foguete de Navega\u00e7\u00e3o", "TRANSPORTE",
            new ItemStack(OSItems.itemNavRocket),
            "Viaje pessoalmente at\u00e9 seus sat\u00e9lites na dimens\u00e3o orbital.\n\n"
            + "1. Posicione na 7x7 e escolha o sat\u00e9lite.\n"
            + "2. Em \u00f3rbita voc\u00ea ganha voo livre e colisão real com o sat\u00e9lite.\n"
            + "3. Clique no foguete ancorado para retornar \u00e0 base com o item no invent\u00e1rio."));

        list.add(guide("Sat\u00e9lite & Kit de Reparo", "MANUTEN\u00c7\u00c3O",
            new ItemStack(OSItems.itemRepairKit),
            "Clique no n\u00facleo do sat\u00e9lite em \u00f3rbita para gerenci\u00e1-lo:\n\n"
            + "- Use o Kit de Reparo (+10% de vida cada).\n"
            + "- Instale ou clique nos chips para ejetar m\u00f3dulos fisicamente.\n"
            + "- Se a vida chegar a 0%, o sat\u00e9lite \u00e9 destru\u00eddo permanentemente!"));

        list.add(guide("Detritos, Escudos & Efici\u00eancia", "M\u00d3DULOS",
            new ItemStack(OSItems.itemModuleDebrisShieldT3),
            "Detritos espaciais reduzem a vida ao longo de 24h reais:\n"
            + "- Sem escudo: cai p/ 20% | Escudo T1: 60% | T2: 80% | T3: 95%.\n"
            + "- Com 40% de vida a gera\u00e7\u00e3o cai pela metade.\n"
            + "- Efici\u00eancia Energ\u00e9tica: T1 (+8%), T2 (+12%) e T3 (+18% RF/t)."));

        list.add(guide("Esta\u00e7\u00e3o de Controle (15x11x13)", "BASE RF",
            new ItemStack(OSItems.itemControlStation),
            "Constru\u00e7\u00e3o autom\u00e1tica em 1 minuto (m\u00e1ximo 1 por jogador; requer terreno 15x13 plano e 11 blocos livres).\n\n"
            + "- Observat\u00f3rio: Telesc\u00f3pio 3D para visualizar sat\u00e9lites.\n"
            + "- Sala de Controle: telemetria e m\u00f3dulos (somente leitura).\n"
            + "- Antena Receptora (5 GRF): puxe cabos RF/Mekanism pelo bloco logo abaixo da torre!"));

        list.add(guide("Chuva de Meteoros & Dispersor", "DEFESA",
            new ItemStack(OSItems.itemDispersionRocket),
            "Eventos raros (~1x/dia logado) miram 2-3 sat\u00e9lites com contagem de 10 minutos (alerta vermelho no canto).\n\n"
            + "Se o tempo acabar, os alvos perdem 50% da vida atual! Posicione o Foguete Dispersor na 7x7 e clique em LAN\u00c7AR para cancelar na hora."));

        return list;
    }

    // ======================================================================
    // SESSÃO 2: CRAFTINGS
    // ======================================================================
    private static List<Page> buildCraftingPages() {
        List<Page> pages = new ArrayList<Page>();

        // --- FORNALHA (8) ---
        pages.add(furnace("Lingote de Redmeg", OSBlocks.oreRedmeg, OSItems.ingotRedmeg, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Redmeg (camadas Y 5-30)."));
        pages.add(furnace("Lingote de Yellomite", OSBlocks.oreYellomite, OSItems.ingotYellomite, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Yellomite (camadas Y 5-35)."));
        pages.add(furnace("Lingote de Cobre", OSBlocks.oreCopper, OSItems.ingotCopper, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Cobre (camadas Y 40-70)."));
        pages.add(furnace("Lingote de Estanho", OSBlocks.oreTin, OSItems.ingotTin, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Estanho (camadas Y 30-60)."));
        pages.add(furnace("Lingote de Alum\u00ednio", OSBlocks.oreAluminium, OSItems.ingotAluminium, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Alum\u00ednio (camadas Y 20-50)."));
        pages.add(furnace("Lingote de Tit\u00e2nio", OSBlocks.oreTitanium, OSItems.ingotTitanium, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Tit\u00e2nio (camadas Y 5-25)."));
        pages.add(furnace("Carv\u00e3o Energizado (2x)", OSBlocks.oreEnergizedCoal, OSItems.energizedCoal, 2,
            "Fundido na fornalha rende 2x Carv\u00e3o Energizado (Y 10-45)."));
        pages.add(furnace("Sil\u00edcio", OSBlocks.oreSilicon, OSItems.itemSilicon, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Sil\u00edcio (Y 10-60)."));

        // --- MESA DE TRABALHO 3x3 (5) ---
        Page p = vanilla("Fibra de Carbono (2x)",
            "2x Carv\u00e3o em cima e 2x Painel de Vidro embaixo.");
        p.grid[0] = new ItemStack(Items.coal); p.grid[1] = new ItemStack(Items.coal);
        p.grid[5] = new ItemStack(Blocks.glass_pane); p.grid[6] = new ItemStack(Blocks.glass_pane);
        p.result = new ItemStack(OSItems.carbonFiber);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("Lingote de A\u00e7o (2x)",
            "2x Ferro em cima e 1x Fibra de Carbono embaixo.");
        p.grid[0] = new ItemStack(Items.iron_ingot); p.grid[1] = new ItemStack(Items.iron_ingot);
        p.grid[5] = new ItemStack(OSItems.carbonFiber);
        p.result = new ItemStack(OSItems.ingotSteel);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("Esta\u00e7\u00e3o de Crafting Orbital",
            "Mesa avan\u00e7ada 3x5 necess\u00e1ria para foguetes, ligas e m\u00f3dulos.");
        p.grid[0] = ingotSteel(); p.grid[1] = circuit(); p.grid[2] = ingotSteel();
        p.grid[5] = circuit(); p.grid[6] = new ItemStack(Blocks.crafting_table); p.grid[7] = circuit();
        p.grid[10] = ingotSteel(); p.grid[11] = circuit(); p.grid[12] = ingotSteel();
        p.result = new ItemStack(OSBlocks.blockCraftingStation);
        p.resultCount = 1;
        pages.add(p);

        p = vanilla("Kit de Reparo (2x)",
            "Restaura +10% da vida \u00fatil do sat\u00e9lite ao ser colocado no slot de reparo.");
        p.grid[0] = ingotSteel(); p.grid[1] = ingotAlloyTi(); p.grid[2] = ingotSteel();
        p.grid[5] = ingotAlloyTi(); p.grid[6] = circuit(); p.grid[7] = ingotAlloyTi();
        p.grid[10] = ingotSteel(); p.grid[11] = new ItemStack(Items.redstone); p.grid[12] = ingotSteel();
        p.result = new ItemStack(OSItems.itemRepairKit);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("Manual Orbital Sunworks",
            "Receita sem formato fixo (shapeless): livro + circuito.");
        p.shapeless = true;
        p.grid[0] = new ItemStack(Items.book);
        p.grid[1] = circuit();
        p.result = new ItemStack(OSItems.itemManual);
        p.resultCount = 1;
        pages.add(p);

        // --- ESTAÇÃO DE CRAFTING ORBITAL 3x5 ---
        p = station("Liga de Alum\u00ednio (3x)", 3,
            "Liga aeroespacial leve catalisada com Carv\u00e3o Energizado.");
        p.grid[0] = new ItemStack(OSItems.ingotAluminium);
        p.grid[1] = new ItemStack(OSItems.ingotAluminium);
        p.grid[2] = new ItemStack(OSItems.ingotAluminium);
        p.grid[6] = new ItemStack(OSItems.energizedCoal);
        p.result = ingotAlloyAlu();
        p.resultCount = 3;
        pages.add(p);

        p = station("Liga de Tit\u00e2nio (3x)", 3,
            "Liga de alta resist\u00eancia para foguetes e estruturas orbitais.");
        p.grid[0] = new ItemStack(OSItems.ingotTitanium);
        p.grid[1] = new ItemStack(OSItems.ingotTitanium);
        p.grid[2] = new ItemStack(OSItems.ingotTitanium);
        p.grid[6] = new ItemStack(OSItems.energizedCoal);
        p.result = ingotAlloyTi();
        p.resultCount = 3;
        pages.add(p);

        p = station("Circuito Eletr\u00f4nico (2x)", 3,
            "Componente eletr\u00f4nico principal de controle e telemetria.");
        p.grid[0] = new ItemStack(OSItems.ingotCopper);
        p.grid[1] = new ItemStack(OSItems.ingotTin);
        p.grid[2] = new ItemStack(OSItems.ingotCopper);
        p.grid[5] = new ItemStack(OSItems.ingotAluminium);
        p.grid[6] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[7] = new ItemStack(OSItems.ingotAluminium);
        p.grid[10] = new ItemStack(OSItems.ingotCopper);
        p.grid[11] = new ItemStack(OSItems.ingotTin);
        p.grid[12] = new ItemStack(OSItems.ingotCopper);
        p.result = circuit();
        p.resultCount = 2;
        pages.add(p);

        p = station("M\u00f3dulo de Controle", 6,
            "Unidade central de navega\u00e7\u00e3o e pilotagem dos foguetes.");
        p.grid[0] = circuit(); p.grid[1] = circuit(); p.grid[2] = circuit();
        p.grid[5] = ingotSteel(); p.grid[6] = new ItemStack(OSItems.ingotRedmeg); p.grid[7] = ingotSteel();
        p.grid[11] = ingotSteel();
        p.result = new ItemStack(OSItems.itemControlModule);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T1", 6,
            "Transmite a energia solar captada em \u00f3rbita.");
        p.grid[1] = circuit();
        p.grid[6] = new ItemStack(OSItems.ingotCopper);
        p.grid[11] = new ItemStack(OSItems.ingotCopper);
        p.result = new ItemStack(OSItems.itemAntennaT1);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T2", 6,
            "Evolu\u00e7\u00e3o de transmiss\u00e3o com Redmeg e Tit\u00e2nio.");
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT1); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT2);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T3", 6,
            "Antena refor\u00e7ada de alta pot\u00eancia.");
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT2); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT3);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T4", 6,
            "Antena de longo alcance orbital.");
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT3); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT4);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T5", 6,
            "Antena m\u00e1xima energizada com Yellomite.");
        p.grid[1] = new ItemStack(OSItems.ingotYellomite);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT4); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotYellomite);
        p.result = new ItemStack(OSItems.itemAntennaT5);
        p.resultCount = 1;
        pages.add(p);

        p = station("Painel Solar Tier 1 (2x)", 6,
            "C\u00e9lulas fotovoltaicas de sil\u00edcio de alta efici\u00eancia.");
        p.grid[0] = new ItemStack(Blocks.glass_pane);
        p.grid[1] = new ItemStack(Blocks.glass_pane);
        p.grid[2] = new ItemStack(Blocks.glass_pane);
        p.grid[5] = new ItemStack(OSItems.itemSilicon);
        p.grid[6] = new ItemStack(OSItems.ingotYellomite);
        p.grid[7] = new ItemStack(OSItems.itemSilicon);
        p.grid[10] = ingotSteel();
        p.grid[11] = new ItemStack(OSItems.ingotTitanium);
        p.grid[12] = circuit();
        p.result = new ItemStack(OSItems.itemSolarPanelT1);
        p.resultCount = 2;
        pages.add(p);

        p = station("Bloco Revestido (4x)", 3,
            "Blindagem composta da fuselagem dos foguetes e plataformas.");
        p.grid[0] = ingotAlloyAlu(); p.grid[1] = new ItemStack(OSItems.carbonFiber); p.grid[2] = ingotAlloyAlu();
        p.grid[5] = new ItemStack(OSItems.carbonFiber); p.grid[6] = ingotAlloyTi(); p.grid[7] = new ItemStack(OSItems.carbonFiber);
        p.grid[10] = ingotAlloyAlu(); p.grid[11] = new ItemStack(OSItems.carbonFiber); p.grid[12] = ingotAlloyAlu();
        p.result = new ItemStack(OSBlocks.blockCladded);
        p.resultCount = 4;
        pages.add(p);

        p = station("Computador Central", 6,
            "Unidade central usada na constru\u00e7\u00e3o da Esta\u00e7\u00e3o de Controle.");
        p.grid[0] = circuit(); p.grid[1] = ingotSteel(); p.grid[2] = circuit();
        p.grid[5] = ingotSteel(); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = ingotSteel();
        p.grid[10] = circuit(); p.grid[11] = ingotSteel(); p.grid[12] = circuit();
        p.result = new ItemStack(OSBlocks.blockComputer);
        p.resultCount = 1;
        pages.add(p);

        p = station("Plataforma de Lan\u00e7amento (9x)", 6,
            "Construa 49 blocos em 7x7 para formar a base de decolagem.");
        p.grid[0] = ingotSteel(); p.grid[1] = new ItemStack(OSBlocks.blockCladded); p.grid[2] = ingotSteel();
        p.grid[5] = new ItemStack(OSBlocks.blockCladded); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotSteel(); p.grid[11] = new ItemStack(OSBlocks.blockCladded); p.grid[12] = ingotSteel();
        p.result = new ItemStack(OSBlocks.blockLaunchPlatform);
        p.resultCount = 9;
        pages.add(p);

        // Escudos T1-T3
        p = station("M\u00f3dulo: Escudo de Detritos T1", 6,
            "Reduz desgaste em 24h: mant\u00e9m at\u00e9 60% da vida \u00fatil.");
        p.grid[0] = ingotAlloyTi(); p.grid[1] = new ItemStack(OSItems.carbonFiber); p.grid[2] = ingotAlloyTi();
        p.grid[5] = new ItemStack(OSItems.carbonFiber); p.grid[6] = circuit(); p.grid[7] = new ItemStack(OSItems.carbonFiber);
        p.grid[10] = ingotAlloyTi(); p.grid[11] = new ItemStack(OSItems.carbonFiber); p.grid[12] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemModuleDebrisShield);
        p.resultCount = 1;
        pages.add(p);

        p = station("M\u00f3dulo: Escudo de Detritos T2", 6,
            "Escudo refor\u00e7ado com Yellomite: mant\u00e9m at\u00e9 80% de vida em 24h.");
        p.grid[0] = new ItemStack(OSItems.ingotYellomite); p.grid[1] = new ItemStack(OSItems.itemModuleDebrisShield); p.grid[2] = new ItemStack(OSItems.ingotYellomite);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = circuit(); p.grid[7] = ingotAlloyTi();
        p.grid[10] = new ItemStack(OSItems.ingotYellomite); p.grid[11] = new ItemStack(OSItems.carbonFiber); p.grid[12] = new ItemStack(OSItems.ingotYellomite);
        p.result = new ItemStack(OSItems.itemModuleDebrisShieldT2);
        p.resultCount = 1;
        pages.add(p);

        p = station("M\u00f3dulo: Escudo de Detritos T3", 6,
            "Blindagem orbital suprema: mant\u00e9m at\u00e9 95% de vida em 24h.");
        p.grid[0] = new ItemStack(OSItems.ingotRedmeg); p.grid[1] = new ItemStack(OSItems.itemModuleDebrisShieldT2); p.grid[2] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = ingotAlloyTi();
        p.grid[10] = new ItemStack(OSItems.ingotRedmeg); p.grid[11] = new ItemStack(OSItems.ingotYellomite); p.grid[12] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemModuleDebrisShieldT3);
        p.resultCount = 1;
        pages.add(p);

        // Eficiência T1-T3
        p = station("M\u00f3dulo: Efici\u00eancia T1 (+8%)", 6,
            "Aumenta a gera\u00e7\u00e3o de energia do sat\u00e9lite em +8% RF/t.");
        p.grid[0] = new ItemStack(OSItems.itemSilicon); p.grid[1] = new ItemStack(OSItems.ingotYellomite); p.grid[2] = new ItemStack(OSItems.itemSilicon);
        p.grid[5] = circuit(); p.grid[6] = new ItemStack(OSItems.itemSolarPanelT1); p.grid[7] = circuit();
        p.grid[10] = ingotAlloyAlu(); p.grid[11] = circuit(); p.grid[12] = ingotAlloyAlu();
        p.result = new ItemStack(OSItems.itemModuleEfficiencyT1);
        p.resultCount = 1;
        pages.add(p);

        p = station("M\u00f3dulo: Efici\u00eancia T2 (+12%)", 6,
            "Aumenta a gera\u00e7\u00e3o de energia do sat\u00e9lite em +12% RF/t.");
        p.grid[0] = new ItemStack(OSItems.ingotYellomite); p.grid[1] = new ItemStack(OSItems.itemModuleEfficiencyT1); p.grid[2] = new ItemStack(OSItems.ingotYellomite);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.ingotRedmeg); p.grid[7] = ingotAlloyTi();
        p.grid[10] = new ItemStack(OSItems.ingotYellomite); p.grid[11] = circuit(); p.grid[12] = new ItemStack(OSItems.ingotYellomite);
        p.result = new ItemStack(OSItems.itemModuleEfficiencyT2);
        p.resultCount = 1;
        pages.add(p);

        p = station("M\u00f3dulo: Efici\u00eancia T3 (+18%)", 6,
            "Aumenta a gera\u00e7\u00e3o de energia do sat\u00e9lite em +18% RF/t.");
        p.grid[0] = new ItemStack(OSItems.ingotRedmeg); p.grid[1] = new ItemStack(OSItems.itemModuleEfficiencyT2); p.grid[2] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = ingotAlloyTi();
        p.grid[10] = new ItemStack(OSItems.itemSolarPanelT1); p.grid[11] = new ItemStack(OSItems.ingotYellomite); p.grid[12] = new ItemStack(OSItems.itemSolarPanelT1);
        p.result = new ItemStack(OSItems.itemModuleEfficiencyT3);
        p.resultCount = 1;
        pages.add(p);

        // Foguetes e Estação de Controle
        p = station("Foguete Orbital Tier 1", 25,
            "Receita completa 3x5! Posicione no centro da plataforma 7x7.");
        p.grid[0] = new ItemStack(OSItems.itemAntennaT1);
        p.grid[1] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[2] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[3] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[4] = new ItemStack(OSItems.itemAntennaT1);
        p.grid[5] = new ItemStack(OSBlocks.blockCladded);
        p.grid[6] = new ItemStack(OSBlocks.blockCladded);
        p.grid[7] = new ItemStack(OSBlocks.blockCladded);
        p.grid[8] = new ItemStack(OSBlocks.blockCladded);
        p.grid[9] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotAlloyTi();
        p.grid[11] = ingotAlloyTi();
        p.grid[12] = new ItemStack(OSItems.itemControlModule);
        p.grid[13] = ingotAlloyTi();
        p.grid[14] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemRocketT1);
        p.resultCount = 1;
        pages.add(p);

        p = station("Foguete de Navega\u00e7\u00e3o", 25,
            "Nave tripulada para visitar e reparar seus sat\u00e9lites em \u00f3rbita.");
        p.grid[0] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[1] = new ItemStack(OSItems.itemAntennaT1);
        p.grid[2] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[5] = new ItemStack(OSBlocks.blockCladded);
        p.grid[6] = new ItemStack(OSItems.itemControlModule);
        p.grid[7] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotAlloyTi();
        p.grid[11] = ingotAlloyTi();
        p.grid[12] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemNavRocket);
        p.resultCount = 1;
        pages.add(p);

        p = station("Foguete Dispersor", 6,
            "Interceptador antimeteoro! Lan\u00e7ar cancela imediatamente a chuva de meteoros.");
        p.grid[0] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[1] = new ItemStack(Items.gunpowder);
        p.grid[2] = circuit();
        p.grid[3] = new ItemStack(Items.gunpowder);
        p.grid[4] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = new ItemStack(OSBlocks.blockCladded);
        p.grid[6] = new ItemStack(OSBlocks.blockCladded);
        p.grid[7] = new ItemStack(OSItems.itemControlModule);
        p.grid[8] = new ItemStack(OSBlocks.blockCladded);
        p.grid[9] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.carbonFiber);
        p.grid[12] = new ItemStack(OSItems.itemAntennaT1);
        p.grid[13] = new ItemStack(OSItems.carbonFiber);
        p.grid[14] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemDispersionRocket);
        p.resultCount = 1;
        pages.add(p);

        p = station("Esta\u00e7\u00e3o de Controle Orbital", 25,
            "Multiblock 15x11x13! Posicione no ch\u00e3o plano (limite 1 por jogador).");
        p.grid[0] = new ItemStack(OSItems.itemAntennaT2);
        p.grid[1] = new ItemStack(OSItems.ingotYellomite);
        p.grid[2] = new ItemStack(OSBlocks.blockComputer);
        p.grid[3] = new ItemStack(OSItems.ingotYellomite);
        p.grid[4] = new ItemStack(OSItems.itemAntennaT2);
        p.grid[5] = new ItemStack(OSBlocks.blockCladded);
        p.grid[6] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[7] = new ItemStack(OSItems.itemControlModule);
        p.grid[8] = new ItemStack(OSItems.itemSolarPanelT1);
        p.grid[9] = new ItemStack(OSBlocks.blockCladded);
        p.grid[10] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[12] = new ItemStack(OSBlocks.blockComputer);
        p.grid[13] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[14] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemControlStation);
        p.resultCount = 1;
        pages.add(p);

        return pages;
    }

    // ======================================================================
    // SESSÃO 3: MULTIBLOCKS
    // ======================================================================
    private static List<Page> buildMultiblockPages() {
        List<Page> list = new ArrayList<Page>();

        Page mb1 = new Page(SEC_MULTIBLOCKS, TYPE_MULTIBLOCK,
            "Plataforma de Lan\u00e7amento 7x7", "7x7x1 (49 BLOCOS)",
            "Vista de cima: preencha 7x7 blocos no mesmo n\u00edvel. Mantenha 1 bloco livre ao redor das bordas.");
        mb1.multiblockId = MB_PLATFORM_7X7;
        mb1.result = new ItemStack(OSBlocks.blockLaunchPlatform);
        list.add(mb1);

        Page mb2 = new Page(SEC_MULTIBLOCKS, TYPE_MULTIBLOCK,
            "Foguete Tier 1 (1x6)", "ALTURA: 6 BLOCOS",
            "Clique com o direito no centro da 7x7 pronta. Requer c\u00e9u livre acima do centro para lan\u00e7ar.");
        mb2.multiblockId = MB_ROCKET_T1;
        mb2.result = new ItemStack(OSItems.itemRocketT1);
        list.add(mb2);

        Page mb3 = new Page(SEC_MULTIBLOCKS, TYPE_MULTIBLOCK,
            "Foguete de Navega\u00e7\u00e3o (1x8)", "ALTURA: 8 BLOCOS",
            "Clique com o direito no centro da 7x7. Transporta voc\u00ea para a \u00f3rbita e ancora pr\u00f3ximo ao sat\u00e9lite.");
        mb3.multiblockId = MB_NAV_ROCKET;
        mb3.result = new ItemStack(OSItems.itemNavRocket);
        list.add(mb3);

        Page mb4 = new Page(SEC_MULTIBLOCKS, TYPE_MULTIBLOCK,
            "Foguete Dispersor (1x6)", "ALTURA: 6 BLOCOS",
            "Posicione no centro da 7x7 e clique em LAN\u00c7AR para encerrar imediatamente a chuva de meteoros.");
        mb4.multiblockId = MB_ROCKET_T1;
        mb4.result = new ItemStack(OSItems.itemDispersionRocket);
        list.add(mb4);

        Page mb5 = new Page(SEC_MULTIBLOCKS, TYPE_MULTIBLOCK,
            "Sat\u00e9lite Solar em \u00d3rbita", "ESTRUTURA ORBITAL",
            "Gerado em \u00f3rbita com colis\u00e3o din\u00e2mica. Use Kit de Reparo (+10%) e gerencie m\u00f3dulos no n\u00facleo.");
        mb5.multiblockId = MB_ORBIT_SAT;
        mb5.result = new ItemStack(OSItems.itemRepairKit);
        list.add(mb5);

        return list;
    }

    private static Page guide(String title, String badge, ItemStack icon, String desc) {
        Page p = new Page(SEC_TUTORIAL, TYPE_GUIDE, title, badge, desc);
        p.result = icon;
        return p;
    }

    private static Page furnace(String title, Block ore, Item out, int count, String desc) {
        Page p = new Page(SEC_CRAFTINGS, TYPE_FURNACE, title, "FORNALHA", desc);
        p.grid[0] = new ItemStack(ore);
        p.result = new ItemStack(out);
        p.resultCount = count;
        return p;
    }

    private static Page vanilla(String title, String desc) {
        return new Page(SEC_CRAFTINGS, TYPE_VANILLA, title, "MESA 3x3", desc);
    }

    private static Page station(String title, int seconds, String desc) {
        Page p = new Page(SEC_CRAFTINGS, TYPE_STATION, title, "ESTA\u00c7\u00c3O 3x5", desc);
        p.craftSeconds = seconds;
        return p;
    }

    private static ItemStack ingotSteel() {
        return new ItemStack(OSItems.ingotSteel);
    }

    private static ItemStack circuit() {
        return new ItemStack(OSItems.itemCircuit);
    }

    private static ItemStack ingotAlloyTi() {
        return new ItemStack(OSItems.ingotTitaniumAlloy);
    }

    private static ItemStack ingotAlloyAlu() {
        return new ItemStack(OSItems.ingotAluminiumAlloy);
    }
}