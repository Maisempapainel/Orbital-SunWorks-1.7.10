package com.orbitalsunworks.client.render;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.imageio.ImageIO;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Gerador procedural de texturas para:
 *  - Itens petroquímicos e peças EVA (32x32)
 *  - Camadas fallback do Traje Espacial (64x32)
 *  - Blocos & máquinas com porta RF vermelha (16x16 / 32x32)
 *  - Fluidos animados: Petróleo Bruto e Petróleo Refinado (+ .mcmeta)
 */
@SideOnly(Side.CLIENT)
public final class OSPumpAndSuitTextures {

    private OSPumpAndSuitTextures() {
    }

    public static void populate(Map<String, byte[]> out) {
        // Novos itens (32x32)
        putPng(out, "textures/items/glassFiber.png", makeGlassFiber());
        putPng(out, "textures/items/kevlar.png", makeKevlar());
        putPng(out, "textures/items/kevlarFiberglass.png", makeKevlarFiberglass());
        putPng(out, "textures/items/oxygenTank.png", makeOxygenTank());
        putPng(out, "textures/items/gasThrusters.png", makeGasThrusters());
        putPng(out, "textures/items/oilSensor.png", makeOilSensor());
        putPng(out, "textures/items/bucketCrudeOil.png", makeOilBucket(0x161210, 0x3A312C));
        putPng(out, "textures/items/bucketRefinedOil.png", makeOilBucket(0xC87A10, 0xFFE26A));

        putPng(out, "textures/items/spaceHelmet.png", makeSpaceHelmetIcon());
        putPng(out, "textures/items/spaceChestplate.png", makeSpaceChestIcon());
        putPng(out, "textures/items/spaceLeggings.png", makeSpaceLegsIcon());
        putPng(out, "textures/items/spaceBoots.png", makeSpaceBootsIcon());

        // Armadura fallback 64x32
        putPng(out, "textures/models/space_suit_layer_1.png", makeSuitLayer(false));
        putPng(out, "textures/models/space_suit_layer_2.png", makeSuitLayer(true));

        // Blocos & faces de máquinas
        putPng(out, "textures/blocks/blockEnergyInput.png", makeRedRfFace(false));
        putPng(out, "textures/blocks/blockOilExtractor.png", makeExtractorFace());

        putPng(out, "textures/blocks/blockOilRefiner_front.png", makeRefinerFront());
        putPng(out, "textures/blocks/blockOilRefiner_back_rf.png", makeRedRfFace(true));
        putPng(out, "textures/blocks/blockOilRefiner_top.png", makeMachinePlate(0x283442, 0x4BE0FF));
        putPng(out, "textures/blocks/blockOilRefiner_side.png", makeMachineSide(0xD48A18));

        putPng(out, "textures/blocks/blockSynthesizer_front.png", makeSynthesizerFront());
        putPng(out, "textures/blocks/blockSynthesizer_back_rf.png", makeRedRfFace(true));
        putPng(out, "textures/blocks/blockSynthesizer_top.png", makeMachinePlate(0x232F3E, 0xF2BC3E));
        putPng(out, "textures/blocks/blockSynthesizer_side.png", makeMachineSide(0x4BE0FF));

        // Fluidos animados (+ .mcmeta)
        putPng(out, "textures/blocks/fluid_oil_still.png", makeFluidStill(16, 8, 0x0B0909, 0x26201D, true));
        out.put("textures/blocks/fluid_oil_still.png.mcmeta",
            "{\"animation\":{\"frametime\":4,\"interpolate\":true}}".getBytes(StandardCharsets.UTF_8));
        putPng(out, "textures/blocks/fluid_oil_flow.png", makeFluidFlow(32, 8, 0x0B0909, 0x2B2420, true));
        out.put("textures/blocks/fluid_oil_flow.png.mcmeta",
            "{\"animation\":{\"frametime\":2,\"interpolate\":true}}".getBytes(StandardCharsets.UTF_8));

        putPng(out, "textures/blocks/fluid_refined_oil_still.png", makeFluidStill(16, 8, 0xA86008, 0xF8CC48, false));
        out.put("textures/blocks/fluid_refined_oil_still.png.mcmeta",
            "{\"animation\":{\"frametime\":3,\"interpolate\":true}}".getBytes(StandardCharsets.UTF_8));
        putPng(out, "textures/blocks/fluid_refined_oil_flow.png", makeFluidFlow(32, 8, 0xB4680A, 0xFFE066, false));
        out.put("textures/blocks/fluid_refined_oil_flow.png.mcmeta",
            "{\"animation\":{\"frametime\":2,\"interpolate\":true}}".getBytes(StandardCharsets.UTF_8));
    }

    // ------------------------------------------------------------------
    // ITENS 32x32
    // ------------------------------------------------------------------
    private static BufferedImage makeGlassFiber() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 6; y < 26; y++) {
            for (int x = 5; x < 27; x++) {
                boolean border = (x == 5 || x == 26 || y == 6 || y == 25);
                if (border) {
                    setPx(img, x, y, 0x7ED8EE);
                    continue;
                }
                boolean strandA = ((x + y) % 4 == 0);
                boolean strandB = ((x - y + 32) % 4 == 0);
                int c = strandA ? 0xE2FAFF : (strandB ? 0x9BE4F7 : 0x5FBCD4);
                int alpha = (strandA || strandB) ? 0xE6 : 0xA8;
                img.setRGB(x, y, (alpha << 24) | c);
            }
        }
        return img;
    }

    private static BufferedImage makeKevlar() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 5; y < 27; y++) {
            for (int x = 5; x < 27; x++) {
                boolean border = (x == 5 || x == 26 || y == 5 || y == 26);
                if (border) {
                    setPx(img, x, y, 0x5E4108);
                    continue;
                }
                int tx = (x / 2) & 1;
                int ty = (y / 2) & 1;
                int c = (tx ^ ty) == 0 ? 0xE5B038 : 0xB37D16;
                if (x + y < 20) {
                    c = mix(c, 0xFFF0A6, 0.25f);
                }
                setPx(img, x, y, c);
            }
        }
        return img;
    }

    private static BufferedImage makeKevlarFiberglass() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 4; y < 28; y++) {
            for (int x = 4; x < 28; x++) {
                boolean outer = (x == 4 || x == 27 || y == 4 || y == 27);
                if (outer) {
                    setPx(img, x, y, 0x1A2430);
                    continue;
                }
                boolean inner = (x == 5 || x == 26 || y == 5 || y == 26);
                if (inner) {
                    setPx(img, x, y, 0x4BE0FF);
                    continue;
                }
                int diag = (x + y) % 5;
                int c;
                if (diag == 0) {
                    c = 0x7CEEFF;
                } else if (((x - y + 32) % 6) == 0) {
                    c = 0x262C34;
                } else {
                    c = ((x ^ y) & 2) == 0 ? 0xECC046 : 0xBD851B;
                }
                setPx(img, x, y, c);
            }
        }
        return img;
    }

    private static BufferedImage makeOxygenTank() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        // Dois cilindros prateados com faixa azul O2 e manômetro
        for (int t = 0; t < 2; t++) {
            int cx = (t == 0) ? 11 : 21;
            for (int y = 7; y <= 26; y++) {
                for (int dx = -4; dx <= 4; dx++) {
                    if (y == 7 && Math.abs(dx) > 2) continue;
                    if (y == 26 && Math.abs(dx) > 3) continue;
                    int x = cx + dx;
                    if (Math.abs(dx) == 4) {
                        setPx(img, x, y, 0x273444);
                    } else if (y >= 13 && y <= 16) {
                        setPx(img, x, y, dx < 0 ? 0x3FD2FF : 0x1680B8);
                    } else {
                        float shade = (dx + 4) / 8f;
                        setPx(img, x, y, mix(0xDCE6F2, 0x6F8296, shade));
                    }
                }
            }
            // Válvula superior
            fillRect(img, cx - 1, 5, cx + 1, 7, 0xC8962A);
        }
        // Ponte superior e manômetro central
        fillRect(img, 11, 5, 21, 6, 0x556478);
        fillRect(img, 14, 9, 18, 13, 0x1A2534);
        fillRect(img, 15, 10, 17, 12, 0x7CFF6B);
        return img;
    }

    private static BufferedImage makeGasThrusters() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        // Bloco central + 2 bocais inclinados com brilho azul de propulsão
        fillRect(img, 10, 8, 22, 16, 0x64748B);
        fillRect(img, 11, 9, 21, 15, 0x9BB0C6);
        fillRect(img, 14, 10, 18, 13, 0x3CE0FF);
        // Bocais esquerdo e direito
        for (int i = 0; i < 9; i++) {
            int w = 2 + i / 3;
            fillRect(img, 9 - i / 2 - w, 14 + i, 9 - i / 2 + 1, 15 + i, 0x455264);
            fillRect(img, 23 + i / 2 - 1, 14 + i, 23 + i / 2 + w, 15 + i, 0x455264);
        }
        // Jato azul frio abaixo dos bocais
        fillRect(img, 4, 23, 7, 28, 0x4FE8FF);
        fillRect(img, 5, 24, 6, 29, 0xFFFFFF);
        fillRect(img, 25, 23, 28, 28, 0x4FE8FF);
        fillRect(img, 26, 24, 27, 29, 0xFFFFFF);
        return img;
    }

    private static BufferedImage makeOilSensor() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        // Antena
        fillRect(img, 21, 3, 23, 9, 0xB8C7D6);
        setPx(img, 22, 2, 0xFF4545);
        // Corpo scanner
        fillRect(img, 8, 9, 24, 27, 0x1F2B38);
        fillRect(img, 9, 10, 23, 26, 0x394D63);
        // Visor radar
        fillRect(img, 11, 12, 21, 19, 0x0A1A14);
        fillRect(img, 12, 15, 20, 16, 0x36F08A);
        setPx(img, 16, 14, 0xFFD95A);
        setPx(img, 16, 17, 0xFFD95A);
        // Botões + gota de petróleo
        fillRect(img, 11, 21, 13, 23, 0xF2B938);
        fillRect(img, 15, 21, 17, 23, 0x4BE0FF);
        fillRect(img, 19, 21, 21, 24, 0x151210);
        return img;
    }

    private static BufferedImage makeOilBucket(int darkCol, int hiCol) {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        // Alça metálica
        for (int x = 10; x <= 21; x++) {
            setPx(img, x, 5, 0x90A0B2);
        }
        setPx(img, 9, 6, 0x90A0B2);
        setPx(img, 22, 6, 0x90A0B2);
        // Corpo do balde
        for (int y = 8; y <= 26; y++) {
            int inset = (y - 8) / 6;
            for (int x = 7 + inset; x <= 24 - inset; x++) {
                boolean rim = (x == 7 + inset || x == 24 - inset || y == 26);
                if (rim) {
                    setPx(img, x, y, 0x384452);
                } else if (y <= 12) {
                    setPx(img, x, y, (x < 15) ? hiCol : darkCol);
                } else {
                    float t = (x - (7 + inset)) / (float) Math.max(1, 17 - 2 * inset);
                    setPx(img, x, y, mix(0xD8E2EC, 0x627284, t));
                }
            }
        }
        // Gota escorrendo pela borda
        fillRect(img, 18, 12, 20, 19, darkCol);
        setPx(img, 19, 13, hiCol);
        return img;
    }

    private static BufferedImage makeSpaceHelmetIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 5; y <= 26; y++) {
            for (int x = 6; x <= 25; x++) {
                double dx = (x - 15.5) / 9.5;
                double dy = (y - 15.5) / 10.5;
                if (dx * dx + dy * dy > 1.0) continue;
                if (dx * dx + dy * dy > 0.82) {
                    setPx(img, x, y, 0x526275);
                } else if (y >= 10 && y <= 20 && x >= 9 && x <= 22) {
                    float vt = (x - 9) / 13f;
                    int visor = mix(0xFFE77A, 0xC67B0C, vt);
                    if (y == 12 && x < 14) visor = 0xFFFAD0;
                    setPx(img, x, y, visor);
                } else {
                    setPx(img, x, y, mix(0xF6F9FC, 0xB8C5D4, (float) ((x - 6) / 20.0)));
                }
            }
        }
        fillRect(img, 14, 6, 17, 8, 0x55E8FF);
        return img;
    }

    private static BufferedImage makeSpaceChestIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 5, 6, 27, 13, 0xE6EEF7);
        fillRect(img, 9, 12, 23, 27, 0xEDF4FC);
        fillRect(img, 12, 12, 20, 19, 0x3D4C5E);
        setPx(img, 14, 14, 0x4BE0FF);
        setPx(img, 17, 14, 0xFF4F4F);
        fillRect(img, 13, 16, 19, 18, 0x7CFF6B);
        fillRect(img, 9, 24, 23, 26, 0x425264);
        return img;
    }

    private static BufferedImage makeSpaceLegsIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 9, 5, 23, 10, 0x4A5A6C);
        fillRect(img, 9, 10, 15, 27, 0xEDF4FC);
        fillRect(img, 17, 10, 23, 27, 0xDCE6F2);
        fillRect(img, 9, 17, 15, 20, 0x5B6C80);
        fillRect(img, 17, 17, 23, 20, 0x4B5A6C);
        return img;
    }

    private static BufferedImage makeSpaceBootsIcon() {
        BufferedImage img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        for (int b = 0; b < 2; b++) {
            int bx = (b == 0) ? 6 : 18;
            fillRect(img, bx, 10, bx + 8, 22, 0xE6EEF7);
            fillRect(img, bx - 1, 22, bx + 9, 26, 0x425060);
            fillRect(img, bx + 2, 14, bx + 6, 18, 0x4BE0FF);
        }
        return img;
    }

    private static BufferedImage makeSuitLayer(boolean legs) {
        BufferedImage img = new BufferedImage(64, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 64; x++) {
                int c = ((x + y) % 8 == 0) ? 0xC6D2E0 : 0xEDF4FC;
                if (legs && y >= 18 && y <= 21) {
                    c = 0x526378;
                }
                setPx(img, x, y, c);
            }
        }
        return img;
    }

    // ------------------------------------------------------------------
    // BLOCOS E FACES DAS MÁQUINAS (16x16)
    // ------------------------------------------------------------------
    private static BufferedImage makeRedRfFace(boolean backPlate) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                boolean frame = (x == 0 || x == 15 || y == 0 || y == 15);
                if (frame) {
                    setPx(img, x, y, backPlate ? 0x2A3542 : 0x1D2632);
                } else {
                    setPx(img, x, y, ((x + y) % 4 == 0) ? 0x354354 : 0x2A3646);
                }
            }
        }
        // Conector RF VERMELHO luminoso central
        fillRect(img, 3, 3, 13, 13, 0x6A1014);
        fillRect(img, 4, 4, 12, 12, 0xD92228);
        fillRect(img, 6, 6, 10, 10, 0xFF5555);
        setPx(img, 7, 7, 0xFFD6D6);
        setPx(img, 8, 8, 0xFFD6D6);
        return img;
    }

    private static BufferedImage makeExtractorFace() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 0, 0, 16, 16, 0x26313E);
        for (int x = 1; x < 15; x++) {
            int hazard = ((x + 16) % 4 < 2) ? 0xE6B022 : 0x1E242C;
            setPx(img, x, 1, hazard);
            setPx(img, x, 14, hazard);
        }
        fillRect(img, 4, 4, 12, 12, 0x111820);
        fillRect(img, 6, 6, 10, 10, 0x4BE0FF);
        return img;
    }

    private static BufferedImage makeRefinerFront() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 0, 0, 16, 16, 0x253240);
        fillRect(img, 1, 1, 15, 15, 0x364759);
        // Visor de destilação (bruto embaixo -> refinado dourado em cima)
        fillRect(img, 4, 3, 12, 13, 0x0C131C);
        fillRect(img, 5, 4, 11, 8, 0xF2BC3E);
        fillRect(img, 5, 8, 11, 12, 0x1D1816);
        setPx(img, 2, 3, 0x7CFF6B);
        setPx(img, 13, 3, 0xFF4848);
        return img;
    }

    private static BufferedImage makeSynthesizerFront() {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 0, 0, 16, 16, 0x1E2A38);
        fillRect(img, 1, 1, 15, 15, 0x304154);
        // Câmara ciano de polimerização de Kevlar
        fillRect(img, 3, 3, 13, 13, 0x0A1522);
        fillRect(img, 4, 4, 12, 12, 0x165E82);
        fillRect(img, 6, 6, 10, 10, 0x55E8FF);
        fillRect(img, 7, 7, 9, 9, 0xF2C244);
        return img;
    }

    private static BufferedImage makeMachinePlate(int base, int accent) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 0, 0, 16, 16, 0x18212C);
        fillRect(img, 1, 1, 15, 15, base);
        fillRect(img, 5, 5, 11, 11, 0x141C26);
        fillRect(img, 6, 6, 10, 10, accent);
        return img;
    }

    private static BufferedImage makeMachineSide(int stripeCol) {
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        fillRect(img, 0, 0, 16, 16, 0x202B38);
        fillRect(img, 1, 1, 15, 15, 0x314052);
        fillRect(img, 2, 7, 14, 9, stripeCol);
        return img;
    }

    // ------------------------------------------------------------------
    // FLUIDOS ANIMADOS
    // ------------------------------------------------------------------
    private static BufferedImage makeFluidStill(int size, int frames, int base, int hi, boolean opaque) {
        BufferedImage img = new BufferedImage(size, size * frames, BufferedImage.TYPE_INT_ARGB);
        for (int f = 0; f < frames; f++) {
            float phase = (float) (f * Math.PI * 2.0 / frames);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    double wave = Math.sin(x * 0.7 + phase) * Math.cos(y * 0.6 - phase) * 0.5 + 0.5;
                    int rgb = mix(base, hi, (float) wave);
                    int alpha = opaque ? 0xFF : 0xE8;
                    img.setRGB(x, f * size + y, (alpha << 24) | (rgb & 0xFFFFFF));
                }
            }
        }
        return img;
    }

    private static BufferedImage makeFluidFlow(int size, int frames, int base, int hi, boolean opaque) {
        BufferedImage img = new BufferedImage(size, size * frames, BufferedImage.TYPE_INT_ARGB);
        for (int f = 0; f < frames; f++) {
            float offset = f * (size / (float) frames);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    double streak = Math.sin((y - offset) * 0.55 + Math.sin(x * 0.45)) * 0.5 + 0.5;
                    int rgb = mix(base, hi, (float) streak);
                    int alpha = opaque ? 0xFF : 0xE0;
                    img.setRGB(x, f * size + y, (alpha << 24) | (rgb & 0xFFFFFF));
                }
            }
        }
        return img;
    }

    // ------------------------------------------------------------------
    // HELPERS
    // ------------------------------------------------------------------
    private static void putPng(Map<String, byte[]> out, String relPath, BufferedImage img) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", baos);
            out.put(relPath, baos.toByteArray());
        } catch (IOException ignored) {
        }
    }

    private static void fillRect(BufferedImage img, int x0, int y0, int x1, int y1, int rgb) {
        for (int y = y0; y < y1; y++) {
            for (int x = x0; x < x1; x++) {
                setPx(img, x, y, rgb);
            }
        }
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
}