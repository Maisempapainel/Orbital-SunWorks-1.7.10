package com.orbitalsunworks.client.render;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.imageio.ImageIO;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gera texturas procedurais em alta resolução (32x32 para itens/blocos,
 * 512x512 para Terra/nuvens e sprite animado para o Carvão Energizado).
 *
 * Mantém intactos os PNGs originais de:
 *  - antennaT1 .. antennaT5
 *  - circuit
 *  - controlModule
 *  - solarPanelT1
 */
@SideOnly(Side.CLIENT)
public final class OSTextureGenerator {

    private OSTextureGenerator() {
    }

    public static void populate(Map<String, byte[]> out) {
        // ------------------------------------------------------------------
        // ITENS: Lingotes e ligas metálicas (32x32)
        // ------------------------------------------------------------------
        putPng(out, "textures/items/ingotRedmeg.png",
            makeIngot(0xD62642, 0xFF7588, 0x6D1020, 0xFF3A52, true));
        putPng(out, "textures/items/ingotYellomite.png",
            makeIngot(0xECC02D, 0xFFF39C, 0x8C620C, 0xFFE65C, true));
        putPng(out, "textures/items/ingotCopper.png",
            makeIngot(0xDD7B44, 0xFFC08E, 0x7A3818, 0, false));
        putPng(out, "textures/items/ingotTin.png",
            makeIngot(0xB8C5D4, 0xF0F6FC, 0x687686, 0, false));
        putPng(out, "textures/items/ingotAluminium.png",
            makeIngot(0xDCE5EE, 0xFFFFFF, 0x8291A3, 0, false));
        putPng(out, "textures/items/ingotTitanium.png",
            makeIngot(0x7E87AD, 0xC6CEEE, 0x414765, 0, false));
        putPng(out, "textures/items/ingotSteel.png",
            makeIngot(0x636C78, 0xA6B2C0, 0x2F353D, 0, false));
        putPng(out, "textures/items/ingotAluminiumAlloy.png",
            makeAlloyIngot(0xDCE7F2, 0xFFFFFF, 0x768A9E, 0x2EE6FF, 0xE6C35A));
        putPng(out, "textures/items/ingotTitaniumAlloy.png",
            makeAlloyIngot(0x525B78, 0x99A6CB, 0x262B3D, 0x00F0FF, 0xF2B938));

        // ------------------------------------------------------------------
        // ITENS: Materiais especiais, módulos, manual e miniaturas
        // ------------------------------------------------------------------
        putPng(out, "textures/items/energizedCoal.png", makeEnergizedCoalSheet(16));
        out.put("textures/items/energizedCoal.png.mcmeta",
            "{\"animation\":{\"frametime\":2,\"interpolate\":true}}"
                .getBytes(StandardCharsets.UTF_8));

        putPng(out, "textures/items/carbonFiber.png", makeCarbonFiber());
        putPng(out, "textures/items/silicon.png", makeSilicon());
        putPng(out, "textures/items/manual.png", makeManual());
        putPng(out, "textures/items/moduleDebrisShield.png", makeDebrisShieldModule());
        putPng(out, "textures/items/rocketT1.png", makeRocketT1Icon());
        putPng(out, "textures/items/navRocket.png", makeNavRocketIcon());

        // ------------------------------------------------------------------
        // BLOCOS DE CHÃO: 8 minérios + blocos tecnológicos (32x32)
        // ------------------------------------------------------------------
        putPng(out, "textures/blocks/oreRedmeg.png",
            makeOreBlock(101, 0xC91F38, 0xFF6B80, 0x5E0B1A, true));
        putPng(out, "textures/blocks/oreYellomite.png",
            makeOreBlock(202, 0xE8B923, 0xFFF28A, 0x7A5708, true));
        putPng(out, "textures/blocks/oreCopper.png",
            makeOreBlock(303, 0xDA733C, 0xFFBA84, 0x6F3315, false));
        putPng(out, "textures/blocks/oreTin.png",
            makeOreBlock(404, 0xB8C6D6, 0xEFF6FF, 0x5E6B7B, false));
        putPng(out, "textures/blocks/oreAluminium.png",
            makeOreBlock(505, 0xDAE4EE, 0xFFFFFF, 0x7B8A9C, false));
        putPng(out, "textures/blocks/oreTitanium.png",
            makeOreBlock(606, 0x7A83AA, 0xC5CEF0, 0x3D435F, false));
        putPng(out, "textures/blocks/oreSilicon.png",
            makeOreBlock(707, 0x314E7A, 0x6BB7FF, 0x17243A, true));

        putPng(out, "textures/blocks/oreEnergizedCoal.png", makeEnergizedCoalOreSheet(8));
        out.put("textures/blocks/oreEnergizedCoal.png.mcmeta",
            "{\"animation\":{\"frametime\":3,\"interpolate\":true}}"
                .getBytes(StandardCharsets.UTF_8));

        putPng(out, "textures/blocks/blockCladded.png", makeCladdedBlock());
        putPng(out, "textures/blocks/blockCraftingStation.png", makeCraftingStationBlock());
        putPng(out, "textures/blocks/blockComputer.png", makeComputerBlock());

        // ------------------------------------------------------------------
        // AMBIENTE ORBITAL: Planeta Terra e nuvens (512x512 sem emenda)
        // ------------------------------------------------------------------
        putPng(out, "textures/environment/earth.png", makeEarthTexture(512, 512));
        putPng(out, "textures/environment/earth_clouds.png", makeEarthCloudsTexture(512, 512));
    }

    private static void putPng(Map<String, byte[]> out, String relPath, BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            out.put(relPath, baos.toByteArray());
        } catch (IOException ignored) {
        }
    }

    // ======================================================================
    // LINGOTES METÁLICOS E LIGAS AEROESPACIAIS (32x32)
    // ======================================================================
    private static BufferedImage makeIngot(int baseRgb, int hiRgb, int darkRgb,
            int veinRgb, boolean energyVein) {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                float dx = x - 15.5f;
                float dy = y - 15.5f;
                // Forma isométrica de barraChanfrada
                float u = (dx * 0.72f + dy * 1.15f);
                float v = (dx * 0.65f - dy * 0.92f);

                // Face superior e lateral isométrica clássica porém nítida em 32x32
                float topDist = Math.max(Math.abs(u) / 13.0f, Math.abs(v) / 6.2f);
                float frontDist = Math.max(Math.abs(u - 1.2f) / 13.0f, Math.abs(v + 3.2f) / 6.2f);

                boolean inTop = topDist <= 1.0f;
                boolean inBody = inTop || (frontDist <= 1.0f && dy > -3.0f);
                if (!inBody) {
                    continue;
                }

                // Borda escura nítida
                float edgeTop = Math.max(Math.abs(u) / 12.1f, Math.abs(v) / 5.3f);
                float edgeFront = Math.max(Math.abs(u - 1.2f) / 12.1f, Math.abs(v + 3.2f) / 5.3f);
                if (!inTop && frontDist > 0.90f && edgeFront > 1.0f) {
                    img.setRGB(x, y, mix(darkRgb, 0x090C12, 0.75f) | 0xFF000000);
                    continue;
                }
                if (inTop && topDist > 0.91f && dy < -1.0f) {
                    img.setRGB(x, y, mix(darkRgb, 0x0B0E14, 0.68f) | 0xFF000000);
                    continue;
                }

                int col;
                if (inTop) {
                    // Brilho metálico diagonal na face superior
                    float sheen = clamp01(1.0f - Math.abs((x + y) - 27.0f) / 8.5f);
                    float grain = (hash2D(x, y, 17) - 0.5f) * 0.08f;
                    float bevelHi = clamp01((0.92f - topDist) * 4.0f);
                    col = mix(baseRgb, hiRgb, clamp01(0.20f + sheen * 0.55f + grain));
                    if (v > 3.6f && topDist < 0.91f) {
                        col = mix(col, hiRgb, 0.55f * bevelHi);
                    }
                    if (energyVein) {
                        float vein = clamp01(1.0f - Math.abs(u * 0.35f - v * 1.15f) * 0.85f);
                        if (vein > 0.45f && topDist < 0.78f) {
                            col = mix(col, veinRgb, (vein - 0.45f) * 1.6f);
                        }
                    }
                } else {
                    // Face chanfrada frontal/inferior
                    float shade = clamp01((x - 6.0f) / 20.0f);
                    col = mix(baseRgb, darkRgb, 0.45f + shade * 0.42f);
                    if (v < -5.8f) {
                        col = mix(col, 0x0A0D14, 0.55f);
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        // Reflexo especular pontual na quina esquerda
        setPx(img, 9, 13, mix(hiRgb, 0xFFFFFF, 0.55f));
        setPx(img, 10, 12, 0xFFFFFFFF);
        setPx(img, 11, 12, mix(hiRgb, 0xFFFFFF, 0.65f));
        return img;
    }

    private static BufferedImage makeAlloyIngot(int baseRgb, int hiRgb, int darkRgb,
            int stripeRgb, int rivetRgb) {
        BufferedImage img = makeIngot(baseRgb, hiRgb, darkRgb, stripeRgb, false);
        // Faixa energética transversal e rebites aeroespaciais na barra
        for (int i = -4; i <= 4; i++) {
            int sx = 16 + i;
            int sy = 14 - (i / 2);
            if ((img.getRGB(sx, sy) >>> 24) > 0) {
                img.setRGB(sx, sy, stripeRgb | 0xFF000000);
            }
            if ((img.getRGB(sx, sy + 1) >>> 24) > 0) {
                img.setRGB(sx, sy + 1, mix(stripeRgb, hiRgb, 0.45f) | 0xFF000000);
            }
        }
        setPx(img, 10, 15, rivetRgb);
        setPx(img, 21, 12, rivetRgb);
        setPx(img, 14, 21, rivetRgb);
        return img;
    }

    // ======================================================================
    // CARVÃO ENERGIZADO ANIMADO (16 FRAMES x 32x32 = 32x512)
    // ======================================================================
    private static BufferedImage makeEnergizedCoalSheet(int frames) {
        BufferedImage sheet = new BufferedImage(32, 32 * frames, BufferedImage.TYPE_INT_ARGB);
        for (int f = 0; f < frames; f++) {
            float phase = (float) (f * Math.PI * 2.0 / frames);
            float pulse = 0.5f + 0.5f * (float) Math.sin(phase);
            float pulse2 = 0.5f + 0.5f * (float) Math.cos(phase * 2.0f);

            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    float nx = (x - 15.5f) / 11.5f;
                    float ny = (y - 15.5f) / 11.5f;
                    // Forma facetada irregular de carvão cristalino
                    float ang = (float) Math.atan2(ny, nx);
                    float facet = 1.0f + 0.14f * (float) Math.cos(ang * 5.0f + 0.6f)
                        + 0.08f * (float) Math.sin(ang * 3.0f - 0.9f);
                    float dist = (float) Math.sqrt(nx * nx + ny * ny) / facet;

                    if (dist > 1.08f) {
                        continue;
                    }
                    // Halo energético externo suave
                    if (dist > 0.96f) {
                        float alpha = clamp01((1.08f - dist) / 0.12f) * (0.28f + 0.30f * pulse);
                        int halo = mix(0x00B8FF, 0xFF9C2E, pulse2 * 0.35f);
                        int a = (int) (alpha * 255.0f);
                        sheet.setRGB(x, f * 32 + y, (a << 24) | halo);
                        continue;
                    }
                    // Borda cristalina escura
                    if (dist > 0.88f) {
                        sheet.setRGB(x, f * 32 + y, 0xFF0A0D12);
                        continue;
                    }

                    // Facetas escuras de antracite + iluminação superior esquerda
                    float light = clamp01(0.55f - nx * 0.42f - ny * 0.48f);
                    float stepFacet = ((int) ((nx * 2.8f - ny * 2.2f) + 6.0f)) % 2 == 0 ? 0.08f : -0.04f;
                    int coalCol = mix(0x13171F, 0x353E4E, clamp01(light + stepFacet));

                    // Fissuras elétricas ramificadas (veios ciano + âmbar pulsante)
                    float crackA = Math.abs(nx * 0.72f + ny * 0.68f - 0.10f * (float) Math.sin(ny * 4.5f));
                    float crackB = Math.abs(nx * 0.55f - ny * 0.85f + 0.12f * (float) Math.cos(nx * 5.0f));
                    float coreDist = (float) Math.sqrt(nx * nx * 1.2f + ny * ny * 1.2f);

                    float vein = Math.max(
                        clamp01(1.0f - crackA * 6.5f) * clamp01(1.1f - dist),
                        clamp01(1.0f - crackB * 7.2f) * clamp01(1.05f - dist));
                    float centerCore = clamp01(1.0f - coreDist * 2.2f);
                    float energy = clamp01(vein * (0.68f + 0.32f * pulse) + centerCore * (0.55f + 0.45f * pulse));

                    if (energy > 0.15f) {
                        float wave = 0.5f + 0.5f * (float) Math.sin(phase - dist * 5.0f);
                        int plasma = mix(0x00C8FF, 0x8AF7FF, clamp01(energy * 1.15f));
                        plasma = mix(plasma, 0xFFB84D, wave * 0.25f * pulse2);
                        if (energy > 0.78f) {
                            plasma = mix(plasma, 0xFFFFFF, (energy - 0.78f) * 3.5f);
                        }
                        coalCol = mix(coalCol, plasma, clamp01(energy * 1.15f));
                    }

                    sheet.setRGB(x, f * 32 + y, coalCol | 0xFF000000);
                }
            }
        }
        return sheet;
    }

    // ======================================================================
    // FIBRA DE CARBONO, SILÍCIO, MANUAL E ESCUDO DE DETRITO (32x32)
    // ======================================================================
    private static BufferedImage makeCarbonFiber() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 4; y <= 27; y++) {
            for (int x = 4; x <= 27; x++) {
                // Corte diagonal chanfrado nos cantos para parecer placa de compósito
                if ((x + y) < 11 || (x + y) > 51) {
                    continue;
                }
                boolean border = (x == 4 || x == 27 || y == 4 || y == 27 || (x + y) == 11 || (x + y) == 51);
                if (border) {
                    img.setRGB(x, y, 0xFF10151D);
                    continue;
                }
                // Trama diagonal 2x2 de fibra de carbono
                int pattern = ((x / 2) + (y / 2)) & 1;
                boolean warp = ((x + y) % 4) < 2;
                int base = pattern == 0 ? (warp ? 0x262C36 : 0x1D222B) : (warp ? 0x363F4D : 0x2B333F);
                float sheen = clamp01(1.0f - Math.abs((x - y) + 4.0f) / 14.0f) * 0.35f;
                int col = mix(base, 0x62738C, sheen);
                if (x >= 23 && y <= 8) {
                    col = mix(col, 0x2BE0FF, 0.55f);
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    private static BufferedImage makeSilicon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                float dx = Math.abs(x - 15.5f);
                float dy = Math.abs(y - 15.5f);
                float oct = Math.max(Math.max(dx, dy), (dx + dy) * 0.72f);
                if (oct > 11.8f) {
                    continue;
                }
                if (oct > 10.6f) {
                    img.setRGB(x, y, 0xFF0C1422);
                    continue;
                }
                // Wafer semicondutor escuro azulado com brilho iridescente e trilhas microscópicas
                float grad = clamp01(1.0f - ((x + y) - 10.0f) / 38.0f);
                int col = mix(0x162438, 0x3D6A9C, grad);
                if ((x % 4 == 0 && y > 8 && y < 24) || (y % 4 == 0 && x > 8 && x < 24)) {
                    col = mix(col, 0x5CC8FF, 0.25f);
                }
                if (Math.abs((x - y) - 2) <= 1 && oct < 8.5f) {
                    col = mix(col, 0x99E8FF, 0.45f);
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        setPx(img, 10, 9, 0xFFFFFFFF);
        setPx(img, 11, 9, 0xFFB8F2FF);
        return img;
    }

    private static BufferedImage makeManual() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 3; y <= 28; y++) {
            for (int x = 5; x <= 26; x++) {
                if ((x == 5 && y == 3) || (x == 26 && y == 3) || (x == 5 && y == 28) || (x == 26 && y == 28)) {
                    continue;
                }
                boolean border = (x == 5 || x == 26 || y == 3 || y == 28);
                if (border) {
                    img.setRGB(x, y, 0xFF0A101B);
                    continue;
                }
                // Lombada metálica esquerda + capa azul espacial + fecho dourado
                int col;
                if (x <= 8) {
                    col = mix(0x283A52, 0x4A6484, (float) (x - 5) / 3.0f);
                } else {
                    float grad = clamp01((y - 3) / 25.0f);
                    col = mix(0x1D406B, 0x10243E, grad);
                    if (x == 9) {
                        col = 0xFF0E1828;
                    }
                    if (x == 25 || y == 4 || y == 27) {
                        col = mix(col, 0x3D7EB3, 0.65f);
                    }
                }
                // Emblema holográfico central (órbita ciano + planeta)
                float ex = (x - 17.5f);
                float ey = (y - 15.0f);
                float r = (float) Math.sqrt(ex * ex + ey * ey);
                float ring = Math.abs((float) Math.sqrt((ex * 0.72f) * (ex * 0.72f) + (ey * 1.45f) * (ey * 1.45f)) - 5.2f);
                if (r < 2.6f) {
                    col = mix(0x2299E6, 0x80F2FF, clamp01((2.6f - r) / 2.6f));
                } else if (ring < 0.85f) {
                    col = 0xFF55E0FF;
                }
                // Fecho dourado à direita
                if (x >= 23 && y >= 13 && y <= 17) {
                    col = (y == 15) ? 0xFFFFE788 : 0xFFD9A12B;
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    private static BufferedImage makeDebrisShieldModule() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 4; y <= 27; y++) {
            for (int x = 4; x <= 27; x++) {
                float dx = Math.abs(x - 15.5f);
                float dy = Math.abs(y - 15.5f);
                if (dx + dy > 19.5f) {
                    continue;
                }
                if (dx + dy > 18.2f || x == 4 || x == 27 || y == 4 || y == 27) {
                    img.setRGB(x, y, 0xFF0B111A);
                    continue;
                }
                // Placa de titânio
                int col = mix(0x3B485E, 0x202938, clamp01((x + y - 8.0f) / 38.0f));
                if (x == 5 || x == 26 || y == 5 || y == 26) {
                    col = 0xFF6B819E;
                }
                // Pinos dourados inferiores
                if (y >= 25 && (x % 3 == 0) && x >= 9 && x <= 22) {
                    col = 0xFFF2BE46;
                }
                // Emblema hexagonal de escudo ciano brilhante no centro
                float sd = Math.max(Math.abs(dx) * 1.05f, Math.abs(dx) * 0.55f + (y - 15.0f) * 0.72f);
                if (dy < 7.0f && sd < 7.0f) {
                    if (sd > 5.3f || Math.abs(dy - 6.0f) < 0.8f) {
                        col = 0xFF6FF3FF;
                    } else {
                        col = mix(0x0D6F96, 0x35DCFF, clamp01(1.0f - sd / 6.0f));
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    // ======================================================================
    // MINIATURAS DOS FOGUETES (32x32)
    // ======================================================================
    private static BufferedImage makeRocketT1Icon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        // Rotacionado em 45° (apontando para cima-direita)
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                // Eixo longitudinal s: -12 (bocal) .. +13 (ponta)
                float s = ((x - 15.5f) + (15.5f - y)) * 0.7071f;
                // Eixo lateral p: distância perpendicular ao eixo
                float p = ((x - 15.5f) - (15.5f - y)) * 0.7071f;
                float ap = Math.abs(p);

                int col = 0;
                // Chama sutil de ignição na base
                if (s >= -14.2f && s < -9.8f && ap < (2.4f - (-9.8f - s) * 0.45f)) {
                    float t = clamp01((s + 14.2f) / 4.4f);
                    col = mix(0xFF731E, 0xFFF288, t);
                    img.setRGB(x, y, col | 0xFF000000);
                    continue;
                }
                // Aletas laterais na cauda
                boolean fin = s >= -9.8f && s <= -3.5f && ap >= 2.6f && ap <= (6.2f - (s + 9.8f) * 0.55f);
                // Propulsor / bocal escuro
                boolean nozzle = s >= -10.2f && s < -8.0f && ap <= (2.3f - (s + 10.2f) * 0.15f);
                // Fuselagem principal
                boolean body = s >= -8.0f && s <= 6.2f && ap <= 3.1f;
                // Coifa / ogiva dourada no topo
                float coneHalf = Math.max(0.0f, (13.2f - s) * 0.46f);
                boolean nose = s > 6.2f && s <= 13.2f && ap <= coneHalf;

                if (!fin && !nozzle && !body && !nose) {
                    continue;
                }
                if (fin) {
                    col = (ap > 5.0f || s < -9.0f) ? 0x1F2733 : 0x526176;
                } else if (nozzle) {
                    col = mix(0x2D3440, 0x5A6678, clamp01(1.0f - p / 2.5f));
                } else if (nose) {
                    float shade = clamp01(0.55f - p / 4.5f);
                    col = mix(0x9E6814, 0xFFE27A, shade);
                    if (ap > coneHalf - 0.9f) {
                        col = mix(col, 0x261B0A, 0.65f);
                    }
                } else {
                    // Corpo metálico branco/titânio com sombra na direita e faixa ciano
                    float shade = clamp01(0.55f - p / 5.0f);
                    col = mix(0x8899AD, 0xF5FAFF, shade);
                    if (s >= 2.2f && s <= 4.0f) {
                        col = mix(0x0088BB, 0x45ECFF, shade);
                    } else if (s >= -7.8f && s <= -6.6f) {
                        col = 0xFF3E4859;
                    }
                    if (ap > 2.4f) {
                        col = mix(col, 0x1C2430, 0.72f);
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    private static BufferedImage makeNavRocketIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                float s = ((x - 15.5f) + (15.5f - y)) * 0.7071f;
                float p = ((x - 15.5f) - (15.5f - y)) * 0.7071f;
                float ap = Math.abs(p);

                int col = 0;
                // Brilho iônico azul-ciano duplo na base
                if (s >= -13.8f && s < -9.8f && ap >= 0.5f && ap <= 2.9f) {
                    float t = clamp01((s + 13.8f) / 4.0f);
                    col = mix(0x1075B8, 0x8AF7FF, t);
                    img.setRGB(x, y, col | 0xFF000000);
                    continue;
                }
                boolean fin = s >= -9.8f && s <= -2.0f && ap >= 2.8f && ap <= (6.8f - (s + 9.8f) * 0.48f);
                boolean thruster = s >= -10.2f && s < -7.5f && ap <= 3.3f;
                boolean body = s >= -7.5f && s <= 4.5f && ap <= 3.0f;
                // Cápsula tripulada larga + ponta cônica
                float capHalf = s <= 7.8f ? 3.6f : Math.max(0.0f, (13.2f - s) * 0.67f);
                boolean capsule = s > 4.5f && s <= 13.2f && ap <= capHalf;

                if (!fin && !thruster && !body && !capsule) {
                    continue;
                }
                if (fin) {
                    col = (ap > 5.8f) ? 0x1A2330 : 0x4E6178;
                } else if (thruster) {
                    col = mix(0x262E3B, 0x617087, clamp01(0.5f - p / 4.0f));
                } else if (capsule) {
                    float shade = clamp01(0.55f - p / 5.0f);
                    col = mix(0x8899AD, 0xFFFFFF, shade);
                    // Visor panorâmico ciano na cápsula
                    if (s >= 6.4f && s <= 9.2f && ap <= 2.0f) {
                        col = mix(0x0E8BB8, 0x7CF5FF, shade + 0.2f);
                    } else if (s >= 4.5f && s <= 5.5f) {
                        col = 0xFF394250; // escudo térmico
                    }
                    if (ap > capHalf - 0.85f) {
                        col = mix(col, 0x1A222E, 0.72f);
                    }
                } else {
                    float shade = clamp01(0.55f - p / 5.0f);
                    col = mix(0x7B8C9F, 0xEDF4FB, shade);
                    if (s >= 1.2f && s <= 2.8f) {
                        col = 0xFF28D8FF;
                    }
                    if (ap > 2.3f) {
                        col = mix(col, 0x1B2330, 0.70f);
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    // ======================================================================
    // BLOCOS DE CHÃO (MINÉRIOS E MÁQUINAS 32x32)
    // ======================================================================
    private static BufferedImage makeOreBlock(int seed, int oreMain, int oreHi, int oreDark, boolean glowing) {
        BufferedImage img = makeStoneBase(seed);
        // 7 bolsas minerais facetadas distribuídas sem cortar nas bordas
        int[][] pockets = {
            { 7, 8, 4 }, { 21, 7, 5 }, { 13, 16, 5 }, { 25, 19, 4 },
            { 7, 24, 4 }, { 18, 26, 4 }, { 14, 6, 3 }
        };
        for (int[] pk : pockets) {
            int cx = pk[0], cy = pk[1], rad = pk[2];
            for (int y = Math.max(1, cy - rad - 1); y <= Math.min(30, cy + rad + 1); y++) {
                for (int x = Math.max(1, cx - rad - 1); x <= Math.min(30, cx + rad + 1); x++) {
                    float dx = x - cx;
                    float dy = y - cy;
                    float j = (hash2D(x, y, seed + 9) - 0.5f) * 1.15f;
                    float d = (float) Math.sqrt(dx * dx + dy * dy) + j;
                    if (d <= rad * 0.72f) {
                        float light = clamp01(0.55f - (dx + dy) / (rad * 1.6f));
                        int c = mix(oreMain, oreHi, light);
                        if (glowing && d < rad * 0.35f) {
                            c = mix(c, 0xFFFFFF, 0.35f);
                        }
                        img.setRGB(x, y, c | 0xFF000000);
                    } else if (d <= rad * 1.05f) {
                        // Cavidade escura na pedra ao redor do minério
                        int stone = img.getRGB(x, y);
                        int cavity = mix(stone & 0xFFFFFF, oreDark, 0.55f);
                        img.setRGB(x, y, mix(cavity, 0x2A2D34, 0.45f) | 0xFF000000);
                    }
                }
            }
        }
        return img;
    }

    private static BufferedImage makeEnergizedCoalOreSheet(int frames) {
        BufferedImage base = makeStoneBase(808);
        BufferedImage sheet = new BufferedImage(32, 32 * frames, BufferedImage.TYPE_INT_ARGB);
        int[][] pockets = {
            { 8, 8, 5 }, { 22, 9, 4 }, { 14, 17, 5 }, { 24, 23, 5 }, { 7, 24, 4 }
        };
        for (int f = 0; f < frames; f++) {
            float pulse = 0.5f + 0.5f * (float) Math.sin(f * Math.PI * 2.0 / frames);
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    sheet.setRGB(x, f * 32 + y, base.getRGB(x, y));
                }
            }
            for (int[] pk : pockets) {
                int cx = pk[0], cy = pk[1], rad = pk[2];
                for (int y = Math.max(1, cy - rad); y <= Math.min(30, cy + rad); y++) {
                    for (int x = Math.max(1, cx - rad); x <= Math.min(30, cx + rad); x++) {
                        float dx = x - cx, dy = y - cy;
                        float d = (float) Math.sqrt(dx * dx + dy * dy);
                        if (d <= rad * 0.78f) {
                            int coal = 0x1A2029;
                            if (Math.abs(dx - dy) < 1.15f) {
                                int glow = mix(0x00B8FF, 0x99F8FF, pulse);
                                coal = mix(coal, glow, 0.65f + 0.35f * pulse);
                            }
                            sheet.setRGB(x, f * 32 + y, coal | 0xFF000000);
                        } else if (d <= rad * 1.05f) {
                            sheet.setRGB(x, f * 32 + y, 0xFF3E424A);
                        }
                    }
                }
            }
        }
        return sheet;
    }

    private static BufferedImage makeStoneBase(int seed) {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                float n1 = hash2D(x, y, seed);
                float n2 = hash2D(x / 2, y / 2, seed + 31);
                float streak = hash2D(x / 4, y, seed + 77);
                float lum = 0.45f + n1 * 0.09f + n2 * 0.11f + streak * 0.06f;
                int v = (int) clamp(lum * 225.0f, 82, 162);
                int col = (v << 16) | (v << 8) | (int) Math.min(255, v + 3);
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    private static BufferedImage makeCladdedBlock() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                boolean outer = (x <= 2 || x >= 29 || y <= 2 || y >= 29);
                boolean seam = (x == 3 || x == 28 || y == 3 || y == 28);
                int col;
                if (outer) {
                    float bevel = (x <= 2 || y <= 2) ? 0.55f : 0.18f;
                    col = mix(0x2F3947, 0x62748B, bevel);
                } else if (seam) {
                    col = 0x161D27;
                } else {
                    float grad = clamp01((x + y - 8.0f) / 48.0f);
                    col = mix(0x74869B, 0x4E5C6E, grad);
                    // X discreto estrutural + detalhe central
                    if (Math.abs(x - y) <= 0 || Math.abs((31 - x) - y) <= 0) {
                        col = mix(col, 0x354150, 0.45f);
                    }
                    if (x >= 12 && x <= 19 && y >= 12 && y <= 19) {
                        col = (x == 12 || x == 19 || y == 12 || y == 19) ? 0x26313E : 0x5B6E84;
                    }
                    if (x >= 14 && x <= 17 && y >= 15 && y <= 16) {
                        col = 0x34DCFF;
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        // Rebites metálicos nos 4 cantos
        int[] pts = { 5, 26 };
        for (int px : pts) {
            for (int py : pts) {
                setPx(img, px, py, 0xFFB0C2D6);
                setPx(img, px + 1, py + 1, 0xFF28323F);
            }
        }
        return img;
    }

    private static BufferedImage makeCraftingStationBlock() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                if (x <= 2 || x >= 29 || y <= 2 || y >= 29) {
                    int bevel = (x <= 2 || y <= 2) ? 0x66788E : 0x2A3442;
                    img.setRGB(x, y, bevel | 0xFF000000);
                    continue;
                }
                if (x == 3 || x == 28 || y == 3 || y == 28) {
                    img.setRGB(x, y, 0xFF101722);
                    continue;
                }
                // Painel de trabalho escuro com grade holográfica 3x5 ciano
                int col = 0x162130;
                if (x >= 6 && x <= 25 && y >= 7 && y <= 18) {
                    col = 0x0C141F;
                    if ((x - 6) % 4 == 0 || (y - 7) % 4 == 0) {
                        col = 0x227A9C;
                    } else if (x >= 7 && x <= 24 && y >= 8 && y <= 17) {
                        col = 0x132638;
                    }
                } else if (y >= 21 && y <= 25 && x >= 6 && x <= 25) {
                    // Barra de status inferior + LEDs
                    col = 0x243244;
                    if (y == 23 && x >= 8 && x <= 19) {
                        col = 0x3EE5FF;
                    } else if (y == 23 && x == 22) {
                        col = 0x66FF7A;
                    } else if (y == 23 && x == 24) {
                        col = 0xFFCA42;
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    private static BufferedImage makeComputerBlock() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                if (x <= 2 || x >= 29 || y <= 2 || y >= 29) {
                    int edge = (x <= 2 || y <= 2) ? 0x5B6B7E : 0x262F3B;
                    img.setRGB(x, y, edge | 0xFF000000);
                    continue;
                }
                int col = 0x1A222E;
                // Tela de telemetria superior
                if (x >= 5 && x <= 26 && y >= 5 && y <= 17) {
                    col = 0x09131E;
                    if (x == 5 || x == 26 || y == 5 || y == 17) {
                        col = 0x354B63;
                    } else if (y == 9 && x >= 8 && x <= 21) {
                        col = 0x38E4FF;
                    } else if (y == 12 && x >= 8 && x <= 17) {
                        col = 0x52FF85;
                    } else if (y == 14 && x >= 8 && x <= 23) {
                        col = 0x2899C4;
                    }
                } else if (x >= 5 && x <= 26 && y >= 20 && y <= 26) {
                    // Grades de ventilação + LEDs frontais
                    if (x <= 18) {
                        col = (y % 2 == 0) ? 0x10161F : 0x2F3B4A;
                    } else {
                        col = 0x252F3C;
                        if (y == 22 && x == 21) col = 0x00F0FF;
                        if (y == 22 && x == 23) col = 0x70FF68;
                        if (y == 24 && x == 21) col = 0xFFB838;
                        if (y == 24 && x == 23) col = 0xFF4F5E;
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    // ======================================================================
    // TEXTURA PROCEDURAL REALISTA DA TERRA E NUVENS (512x512 SEM EMENDAS)
    // ======================================================================
    private static BufferedImage makeEarthTexture(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            double v = (y + 0.5) / h;
            double lat = (v - 0.5) * Math.PI; // -pi/2 .. +pi/2
            double cosLat = Math.cos(lat);
            double sinLat = Math.sin(lat);
            double absLat = Math.abs(sinLat);

            for (int x = 0; x < w; x++) {
                double u = (x + 0.5) / w;
                double lon = u * Math.PI * 2.0;

                // Coordenadas 3D contínuas na esfera => zero emenda em u=0/1
                double sx = cosLat * Math.cos(lon);
                double sy = sinLat;
                double sz = cosLat * Math.sin(lon);

                double cont = fbm3D(sx * 1.85 + 3.1, sy * 1.85 - 1.7, sz * 1.85 + 0.8, 5);
                double detail = fbm3D(sx * 7.2 - 2.0, sy * 7.2 + 4.3, sz * 7.2 - 1.5, 4);
                double elev = cont * 0.78 + detail * 0.22 - 0.49;

                // Calotas polares onduladas
                double iceThresh = 0.81 + (detail - 0.5) * 0.14;
                int col;
                if (absLat > iceThresh) {
                    col = mix(0xDCEAF5, 0xF7FBFF, clamp01((float) ((absLat - iceThresh) / 0.12)));
                } else if (elev < 0.0) {
                    // Oceanos profundos -> plataforma continental turquesa
                    float depth = clamp01((float) (-elev / 0.24));
                    float shelf = clamp01((float) (1.0 + elev / 0.055));
                    col = mix(0x0D4B8C, 0x061E45, depth);
                    if (shelf > 0.0f) {
                        col = mix(col, 0x2088BA, shelf * 0.72f);
                    }
                } else {
                    // Continentes: florestas tropicais, savana, desertos subtropicais e cordilheiras
                    double moisture = fbm3D(sx * 2.9 - 5.4, sy * 2.9 + 2.1, sz * 2.9 + 6.2, 3);
                    double aridBand = Math.exp(-Math.pow((absLat - 0.36) / 0.22, 2.0));
                    double aridity = clamp01((float) (aridBand * 0.68 + (0.55 - moisture) * 0.75));

                    int lush = mix(0x1D5C2B, 0x3D803A, clamp01((float) detail));
                    int desert = mix(0xC29F5D, 0xDFC484, clamp01((float) detail));
                    col = mix(lush, desert, (float) aridity);

                    // Cadeias montanhosas e picos nevados
                    if (elev > 0.13) {
                        float mt = clamp01((float) ((elev - 0.13) / 0.14));
                        col = mix(col, 0x7A7369, mt * 0.85f);
                        if (elev > 0.22) {
                            col = mix(col, 0xEEF5FC, clamp01((float) ((elev - 0.22) / 0.06)));
                        }
                    }
                    // Tundra subpolar
                    if (absLat > 0.62) {
                        col = mix(col, 0x7D8C7E, clamp01((float) ((absLat - 0.62) / 0.18)));
                    }
                }
                img.setRGB(x, y, col | 0xFF000000);
            }
        }
        return img;
    }

    private static BufferedImage makeEarthCloudsTexture(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            double v = (y + 0.5) / h;
            double lat = (v - 0.5) * Math.PI;
            double cosLat = Math.cos(lat);
            double sinLat = Math.sin(lat);

            for (int x = 0; x < w; x++) {
                double u = (x + 0.5) / w;
                double lon = u * Math.PI * 2.0;

                // Cisalhamento leve simulando ventos alísios
                double swirl = sinLat * 0.55;
                double sx = cosLat * Math.cos(lon + swirl);
                double sy = sinLat;
                double sz = cosLat * Math.sin(lon + swirl);

                double c = fbm3D(sx * 3.1 + 7.4, sy * 4.2 - 3.0, sz * 3.1 + 1.9, 5);
                double wisp = fbm3D(sx * 8.5 - 1.2, sy * 8.5 + 5.1, sz * 8.5 - 4.4, 3);
                double density = (c * 0.72 + wisp * 0.28) - 0.50;
                if (density <= 0.0) {
                    continue;
                }
                int alpha = (int) clamp((float) (density / 0.26 * 225.0), 0, 225);
                img.setRGB(x, y, (alpha << 24) | 0xF5FAFF);
            }
        }
        return img;
    }

    // ======================================================================
    // UTILITÁRIOS MATEMÁTICOS E RUÍDO 3D SUAVE
    // ======================================================================
    private static double fbm3D(double x, double y, double z, int octaves) {
        double sum = 0.0;
        double amp = 0.5;
        double norm = 0.0;
        for (int i = 0; i < octaves; i++) {
            sum += valueNoise3D(x, y, z) * amp;
            norm += amp;
            x *= 2.03;
            y *= 2.03;
            z *= 2.03;
            amp *= 0.5;
        }
        return sum / norm;
    }

    private static double valueNoise3D(double x, double y, double z) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);
        double fx = x - ix;
        double fy = y - iy;
        double fz = z - iz;
        double ux = fx * fx * (3.0 - 2.0 * fx);
        double uy = fy * fy * (3.0 - 2.0 * fy);
        double uz = fz * fz * (3.0 - 2.0 * fz);

        double c000 = hash3D(ix, iy, iz);
        double c100 = hash3D(ix + 1, iy, iz);
        double c010 = hash3D(ix, iy + 1, iz);
        double c110 = hash3D(ix + 1, iy + 1, iz);
        double c001 = hash3D(ix, iy, iz + 1);
        double c101 = hash3D(ix + 1, iy, iz + 1);
        double c011 = hash3D(ix, iy + 1, iz + 1);
        double c111 = hash3D(ix + 1, iy + 1, iz + 1);

        double x00 = c000 + (c100 - c000) * ux;
        double x10 = c010 + (c110 - c010) * ux;
        double x01 = c001 + (c101 - c001) * ux;
        double x11 = c011 + (c111 - c011) * ux;
        double y0 = x00 + (x10 - x00) * uy;
        double y1 = x01 + (x11 - x01) * uy;
        return y0 + (y1 - y0) * uz;
    }

    private static double hash3D(int x, int y, int z) {
        int n = x * 374761393 + y * 668265263 + z * 2147483647;
        n = (n ^ (n >> 13)) * 1274126177;
        return ((n ^ (n >> 16)) & 0x7FFFFFFF) / (double) 0x7FFFFFFF;
    }

    private static float hash2D(int x, int y, int seed) {
        int n = x * 374761393 + y * 668265263 + seed * 1442695041;
        n = (n ^ (n >> 13)) * 1274126177;
        return ((n ^ (n >> 16)) & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    private static int mix(int a, int b, float t) {
        t = clamp01(t);
        int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        int r = (int) (ar + (br - ar) * t);
        int g = (int) (ag + (bg - ag) * t);
        int bl = (int) (ab + (bb - ab) * t);
        return (r << 16) | (g << 8) | bl;
    }

    private static void setPx(BufferedImage img, int x, int y, int rgb) {
        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
            img.setRGB(x, y, rgb | 0xFF000000);
        }
    }

    private static float clamp01(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private static float clamp(float v, float min, float max) {
        return v < min ? min : (v > max ? max : v);
    }
}