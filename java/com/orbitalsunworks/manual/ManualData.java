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
 *  - SEC_TUTORIAL (guias detalhados de cada item/bloco interagível)
 *  - SEC_CRAFTINGS (todas as 28 receitas: Fornalha, Mesa 3x3, Estação 3x5)
 *  - SEC_MULTIBLOCKS (esquemas visuais de Plataforma 7x7, Foguetes e Satélite)
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
        /** Grade 3x5 (linha * 5 + coluna). Para 3x3 usa colunas 0..2. */
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
    // SESSÃO 1: TUTORIAL INTERATIVO DE TODOS OS SISTEMAS DO MOD
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
            "A Esta\u00e7\u00e3o Orbital possui grade 3x5 para construir ligas, componentes, plataformas e foguetes (e tamb\u00e9m aceita receitas 3x3).\n\n"
            + "Como usar:\n"
            + "1. Monte os itens na grade.\n"
            + "2. Clique no slot de RESULTADO para iniciar a fabrica\u00e7\u00e3o (3s, 6s ou 25s).\n"
            + "3. Ela continua trabalhando mesmo se voc\u00ea fechar a HUD! Clique novamente quando ficar PRONTO."));

        list.add(guide("Plataforma de Lan\u00e7amento", "MULTIBLOCK",
            new ItemStack(OSBlocks.blockLaunchPlatform),
            "Base obrigat\u00f3ria para posicionar e lan\u00e7ar qualquer foguete.\n\n"
            + "Construa um quadrado plano de 7x7 blocos (49 ao todo) no mesmo n\u00edvel, deixando 1 bloco de margem sem outra plataforma encostada.\n\n"
            + "Quando montar corretamente, o centro se destaca. Clique com o bot\u00e3o direito segurando um Foguete para posicion\u00e1-lo no centro!"));

        list.add(guide("Foguete Tier 1", "SAT\u00c9LITE",
            new ItemStack(OSItems.itemRocketT1),
            "Coloque o Foguete Tier 1 no centro da plataforma 7x7 e clique nele para abrir o painel de voo.\n\n"
            + "- Instale at\u00e9 2 m\u00f3dulos antes do lan\u00e7amento.\n"
            + "- Deixe o c\u00e9u acima livre (at\u00e9 o limite de altura), sen\u00e3o o trajeto fica bloqueado.\n"
            + "- Ao decolar, voc\u00ea ver\u00e1 a cinem\u00e1tica de implanta\u00e7\u00e3o e seu sat\u00e9lite gerar\u00e1 at\u00e9 1.00M RF/t!"));

        list.add(guide("Foguete de Navega\u00e7\u00e3o", "TRANSPORTE",
            new ItemStack(OSItems.itemNavRocket),
            "Permite viajar pessoalmente at\u00e9 seus sat\u00e9lites na dimens\u00e3o orbital.\n\n"
            + "1. Posicione-o no centro da plataforma 7x7.\n"
            + "2. Clique nele para abrir a lista dos seus sat\u00e9lites e pressione LAN\u00c7AR.\n"
            + "3. Na \u00f3rbita voc\u00ea ganha voo livre. Para voltar \u00e0 sua base com o item no invent\u00e1rio, clique no foguete ancorado!"));

        list.add(guide("Sat\u00e9lite em \u00d3rbita", "MANUTEN\u00c7\u00c3O",
            new ItemStack(OSItems.itemSolarPanelT1),
            "Cada sat\u00e9lite existe fisicamente em \u00f3rbita com pain\u00e9is solares girat\u00f3rios e estrutura indestrut\u00edvel.\n\n"
            + "Voe at\u00e9 o n\u00facleo central e clique com o bot\u00e3o direito para abrir o painel:\n"
            + "- Veja a vida \u00fatil (%) e gera\u00e7\u00e3o atual em RF/t.\n"
            + "- Coloque Liga de Tit\u00e2nio no slot de reparo (+10% de vida cada).\n"
            + "- Instale ou equipe M\u00f3dulos a qualquer momento."));

        list.add(guide("Escudo de Detrito & Desgaste", "M\u00d3DULO",
            new ItemStack(OSItems.itemModuleDebrisShield),
            "Micrometeoritos e poeira espacial desgastam a vida \u00fatil do sat\u00e9lite lentamente (~1% por hora de jogo).\n\n"
            + "Com vida abaixo de 100% a gera\u00e7\u00e3o cai gradualmente at\u00e9 o piso de 50% (500K RF/t no Tier 1).\n\n"
            + "O M\u00f3dulo Escudo de Detrito reduz o desgaste pela metade (50%). Pode ser instalado no foguete antes de lan\u00e7ar ou direto no sat\u00e9lite."));

        list.add(guide("Computador & Materiais", "COMPONENTES",
            new ItemStack(OSBlocks.blockComputer),
            "Muitos itens combinam recursos b\u00e1sicos e ligas energizadas:\n\n"
            + "- Carv\u00e3o Energizado catalisa ligas de Alum\u00ednio e Tit\u00e2nio na Esta\u00e7\u00e3o.\n"
            + "- Revestimentos (Block Cladded) formam a fuselagem estrutural.\n"
            + "- Computador Central concentra os sistemas eletr\u00f4nicos da esta\u00e7\u00e3o e futuras expans\u00f5es orbitais."));

        return list;
    }

    // ======================================================================
    // SESSÃO 2: CRAFTINGS (TODAS AS 28 RECEITAS CORRIGIDAS 100%)
    // ======================================================================
    private static List<Page> buildCraftingPages() {
        List<Page> pages = new ArrayList<Page>();

        // --- FORNALHA (8) ---
        pages.add(furnace("Lingote de Redmeg", OSBlocks.oreRedmeg, OSItems.ingotRedmeg, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Redmeg (encontrado entre camadas Y 5 e 30)."));
        pages.add(furnace("Lingote de Yellomite", OSBlocks.oreYellomite, OSItems.ingotYellomite, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Yellomite (encontrado entre camadas Y 5 e 35)."));
        pages.add(furnace("Lingote de Cobre", OSBlocks.oreCopper, OSItems.ingotCopper, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Cobre (encontrado entre camadas Y 40 e 70)."));
        pages.add(furnace("Lingote de Estanho", OSBlocks.oreTin, OSItems.ingotTin, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Estanho (encontrado entre camadas Y 30 e 60)."));
        pages.add(furnace("Lingote de Alum\u00ednio", OSBlocks.oreAluminium, OSItems.ingotAluminium, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Alum\u00ednio (encontrado entre camadas Y 20 e 50)."));
        pages.add(furnace("Lingote de Tit\u00e2nio", OSBlocks.oreTitanium, OSItems.ingotTitanium, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Tit\u00e2nio (encontrado entre camadas Y 5 e 25)."));
        pages.add(furnace("Carv\u00e3o Energizado (2x)", OSBlocks.oreEnergizedCoal, OSItems.energizedCoal, 2,
            "Fundido na fornalha rende 2x Carv\u00e3o Energizado (min\u00e9rio entre Y 10 e 45)."));
        pages.add(furnace("Sil\u00edcio", OSBlocks.oreSilicon, OSItems.itemSilicon, 1,
            "Fundido na fornalha a partir do Min\u00e9rio de Sil\u00edcio (encontrado entre camadas Y 10 e 60)."));

        // --- MESA VANILLA 3x3 (4) — índices corrigidos: col 0..2 em cada linha (row*5 + col) ---
        Page p = vanilla("Fibra de Carbono (2x)",
            "Base estrutural leve feita na mesa de trabalho comum.");
        p.grid[0] = new ItemStack(Items.coal);
        p.grid[1] = new ItemStack(Items.coal);
        p.grid[5] = new ItemStack(Blocks.glass_pane);
        p.grid[6] = new ItemStack(Blocks.glass_pane);
        p.result = new ItemStack(OSItems.carbonFiber);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("Lingote de A\u00e7o (2x)",
            "A\u00e7o refor\u00e7ado com fibra de carbono para maquin\u00e1rio e plataforma.");
        p.grid[0] = new ItemStack(Items.iron_ingot);
        p.grid[1] = new ItemStack(Items.iron_ingot);
        p.grid[5] = new ItemStack(OSItems.carbonFiber);
        p.result = new ItemStack(OSItems.ingotSteel);
        p.resultCount = 2;
        pages.add(p);

        p = vanilla("Esta\u00e7\u00e3o de Crafting Orbital",
            "Mesa avan\u00e7ada 3x5 necess\u00e1ria para foguetes, ligas e m\u00f3dulos.");
        p.grid[0] = ingotSteel(); p.grid[1] = circuit(); p.grid[2] = ingotSteel();
        p.grid[5] = circuit();    p.grid[6] = new ItemStack(Blocks.crafting_table); p.grid[7] = circuit();
        p.grid[10] = ingotSteel(); p.grid[11] = circuit(); p.grid[12] = ingotSteel();
        p.result = new ItemStack(OSBlocks.blockCraftingStation);
        p.resultCount = 1;
        pages.add(p);

        p = vanilla("Manual Orbital Sunworks",
            "Receita sem formato fixo (shapeless): coloque em qualquer lugar da grade.");
        p.shapeless = true;
        p.grid[0] = new ItemStack(Items.book);
        p.grid[1] = circuit();
        p.result = new ItemStack(OSItems.itemManual);
        p.resultCount = 1;
        pages.add(p);

        // --- ESTAÇÃO DE CRAFTING ORBITAL 3x5 (16) ---
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
            "Liga de alta resist\u00eancia para foguetes e reparo de sat\u00e9lites (+10%).");
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
            "Aprimoramento da antena para tiers superiores.");
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT1); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT2);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T3", 6,
            "Aprimoramento de longo alcance Tier 3.");
        p.grid[1] = new ItemStack(OSItems.ingotRedmeg);
        p.grid[5] = ingotAlloyTi(); p.grid[6] = new ItemStack(OSItems.itemAntennaT2); p.grid[7] = ingotAlloyTi();
        p.grid[11] = new ItemStack(OSItems.ingotRedmeg);
        p.result = new ItemStack(OSItems.itemAntennaT3);
        p.resultCount = 1;
        pages.add(p);

        p = station("Antena de Transmiss\u00e3o T4", 6,
            "Aprimoramento de longo alcance Tier 4.");
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
            "Unidade central de processamento orbital.");
        p.grid[0] = circuit(); p.grid[1] = ingotSteel(); p.grid[2] = circuit();
        p.grid[5] = ingotSteel(); p.grid[6] = new ItemStack(OSItems.itemControlModule); p.grid[7] = ingotSteel();
        p.grid[10] = circuit(); p.grid[11] = ingotSteel(); p.grid[12] = circuit();
        p.result = new ItemStack(OSBlocks.blockComputer);
        p.resultCount = 1;
        pages.add(p);

        p = station("M\u00f3dulo: Escudo de Detrito", 6,
            "Reduz em 50% o desgaste dos pain\u00e9is por poeira espacial.");
        p.grid[0] = ingotAlloyTi(); p.grid[1] = new ItemStack(OSItems.carbonFiber); p.grid[2] = ingotAlloyTi();
        p.grid[5] = new ItemStack(OSItems.carbonFiber); p.grid[6] = circuit(); p.grid[7] = new ItemStack(OSItems.carbonFiber);
        p.grid[10] = ingotAlloyTi(); p.grid[11] = new ItemStack(OSItems.carbonFiber); p.grid[12] = ingotAlloyTi();
        p.result = new ItemStack(OSItems.itemModuleDebrisShield);
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

        return pages;
    }

    // ======================================================================
    // SESSÃO 3: MULTIBLOCKS (DIAGRAMAS E REGRAS)
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
            "Clique com o direito no centro da 7x7 pronta. Requer 256 blocos livres acima do centro para lan\u00e7ar.");
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
            "Sat\u00e9lite Solar em \u00d3rbita", "ESTRUTURA ORBITAL",
            "Gerado automaticamente em \u00f3rbita. Clique na coluna central 1x3 para reparar com Liga de Tit\u00e2nio.");
        mb4.multiblockId = MB_ORBIT_SAT;
        mb4.result = new ItemStack(OSItems.ingotTitaniumAlloy);
        list.add(mb4);

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